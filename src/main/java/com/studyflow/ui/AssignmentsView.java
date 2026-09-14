package com.studyflow.ui;

import com.studyflow.model.Course;
import com.studyflow.model.Task;
import com.studyflow.service.CourseService;
import com.studyflow.service.TaskService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The assignments page: the full CRUD surface for tasks.
 *
 * <p>Buttons act on the table's current selection and disable themselves when
 * nothing is selected, so an "edit" or "delete" can never fire without a target.
 * Every mutation goes through {@link TaskService}; this class never sees a DAO.
 */
public class AssignmentsView implements ContentView {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final TaskService taskService;
    private final CourseService courseService;

    private final VBox root = new VBox();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final TableView<Task> table = new TableView<>(tasks);

    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("Delete");
    private final Button doneButton = new Button("Mark done");

    private Map<Integer, String> courseNames = Map.of();

    public AssignmentsView(TaskService taskService, CourseService courseService) {
        this.taskService = Objects.requireNonNull(taskService, "taskService must not be null");
        this.courseService = Objects.requireNonNull(courseService, "courseService must not be null");

        Label heading = new Label("Assignments");
        heading.getStyleClass().add("page-title");

        Label subtitle = new Label("Create, edit and track your tasks and exams");
        subtitle.getStyleClass().add("page-subtitle");

        root.getChildren().addAll(heading, subtitle, buildToolbar(), buildTable());
        root.getStyleClass().add("content");
        root.setPadding(new Insets(32));
        root.setSpacing(16);
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String title() {
        return "Assignments";
    }

    @Override
    public void refresh() {
        courseNames = loadCourseNames();
        tasks.setAll(taskService.findAll());
    }

    private Map<Integer, String> loadCourseNames() {
        Map<Integer, String> names = new HashMap<>();
        for (Course course : courseService.findAll()) {
            names.put(course.getId(), course.displayName());
        }
        return names;
    }

    private HBox buildToolbar() {
        Button addButton = new Button("Add assignment");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(event -> onAdd());

        editButton.setOnAction(event -> onEdit());
        deleteButton.setOnAction(event -> onDelete());
        doneButton.setOnAction(event -> onMarkDone());

        setSelectionButtonsDisabled(true);
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, was, now) -> setSelectionButtonsDisabled(now == null));

        HBox toolbar = new HBox(addButton, editButton, doneButton, deleteButton);
        toolbar.setSpacing(8);
        return toolbar;
    }

    private void setSelectionButtonsDisabled(boolean disabled) {
        editButton.setDisable(disabled);
        deleteButton.setDisable(disabled);
        doneButton.setDisable(disabled);
    }

    private TableView<Task> buildTable() {
        table.getStyleClass().add("task-table");
        table.setPlaceholder(new Label("No assignments yet — add one to get started."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Task, String> titleColumn = new TableColumn<>("Task");
        titleColumn.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getTitle()));
        titleColumn.setPrefWidth(280);

        TableColumn<Task, String> courseColumn = new TableColumn<>("Course");
        courseColumn.setCellValueFactory(row -> new SimpleStringProperty(
                courseNames.getOrDefault(row.getValue().getCourseId(), "—")));
        courseColumn.setPrefWidth(200);

        TableColumn<Task, String> deadlineColumn = new TableColumn<>("Deadline");
        deadlineColumn.setCellValueFactory(row -> new SimpleStringProperty(
                row.getValue().getDeadline() == null
                        ? "—"
                        : row.getValue().getDeadline().format(DATE_FORMAT)));
        deadlineColumn.setPrefWidth(180);

        TableColumn<Task, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getStatus().label()));
        statusColumn.setPrefWidth(120);

        table.getColumns().add(titleColumn);
        table.getColumns().add(courseColumn);
        table.getColumns().add(deadlineColumn);
        table.getColumns().add(statusColumn);

        return table;
    }

    private void onAdd() {
        List<Course> courses = courseService.findAll();
        if (courses.isEmpty()) {
            info("Add a course first", "Assignments belong to a course. Create one on the Courses page.");
            return;
        }

        TaskFormDialog.createNew(courses).ifPresent(task -> {
            taskService.saveNew(task);
            refresh();
        });
    }

    private void onEdit() {
        Task selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        TaskFormDialog.edit(courseService.findAll(), selected).ifPresent(edited -> {
            taskService.updateTask(edited);
            refresh();
        });
    }

    private void onDelete() {
        Task selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        if (confirm("Delete assignment", "Delete \"" + selected.getTitle() + "\"? This cannot be undone.")) {
            taskService.deleteTask(selected.getId());
            refresh();
        }
    }

    private void onMarkDone() {
        Task selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        taskService.completeTask(selected.getId());
        refresh();
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> answer = alert.showAndWait();
        return answer.isPresent() && answer.get() == ButtonType.OK;
    }

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
