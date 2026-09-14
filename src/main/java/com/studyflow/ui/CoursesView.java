package com.studyflow.ui;

import com.studyflow.model.Course;
import com.studyflow.service.CourseService;
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

import java.util.Objects;
import java.util.Optional;

/**
 * The courses page: CRUD for the courses that tasks are grouped under.
 *
 * <p>Deleting a course removes its tasks too — the schema's cascading foreign
 * keys take care of that — so the confirmation dialog says as much.
 */
public class CoursesView implements ContentView {

    private final CourseService courseService;
    private final int userId;

    private final VBox root = new VBox();
    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final TableView<Course> table = new TableView<>(courses);

    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("Delete");

    public CoursesView(CourseService courseService, int userId) {
        this.courseService = Objects.requireNonNull(courseService, "courseService must not be null");
        this.userId = userId;

        Label heading = new Label("Courses");
        heading.getStyleClass().add("page-title");

        Label subtitle = new Label("The courses in your semester");
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
        return "Courses";
    }

    @Override
    public void refresh() {
        courses.setAll(courseService.findByUser(userId));
    }

    private HBox buildToolbar() {
        Button addButton = new Button("Add course");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(event -> onAdd());

        editButton.setOnAction(event -> onEdit());
        deleteButton.setOnAction(event -> onDelete());

        setSelectionButtonsDisabled(true);
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, was, now) -> setSelectionButtonsDisabled(now == null));

        HBox toolbar = new HBox(addButton, editButton, deleteButton);
        toolbar.setSpacing(8);
        return toolbar;
    }

    private void setSelectionButtonsDisabled(boolean disabled) {
        editButton.setDisable(disabled);
        deleteButton.setDisable(disabled);
    }

    private TableView<Course> buildTable() {
        table.getStyleClass().add("task-table");
        table.setPlaceholder(new Label("No courses yet — add your first one."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Course, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getName()));
        nameColumn.setPrefWidth(320);

        TableColumn<Course, String> codeColumn = new TableColumn<>("Code");
        codeColumn.setCellValueFactory(row -> new SimpleStringProperty(
                row.getValue().getCode() == null ? "—" : row.getValue().getCode()));
        codeColumn.setPrefWidth(160);

        TableColumn<Course, String> semesterColumn = new TableColumn<>("Semester");
        semesterColumn.setCellValueFactory(row -> new SimpleStringProperty(
                row.getValue().getSemester() == null ? "—" : row.getValue().getSemester()));
        semesterColumn.setPrefWidth(180);

        table.getColumns().add(nameColumn);
        table.getColumns().add(codeColumn);
        table.getColumns().add(semesterColumn);

        return table;
    }

    private void onAdd() {
        CourseFormDialog.createNew(userId).ifPresent(course -> {
            courseService.createCourse(course.getUserId(), course.getName(),
                    course.getCode(), course.getSemester());
            refresh();
        });
    }

    private void onEdit() {
        Course selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        CourseFormDialog.edit(selected).ifPresent(edited -> {
            courseService.updateCourse(edited);
            refresh();
        });
    }

    private void onDelete() {
        Course selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        if (confirm("Delete course",
                "Delete \"" + selected.getName() + "\" and all its assignments? This cannot be undone.")) {
            courseService.deleteCourse(selected.getId());
            refresh();
        }
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> answer = alert.showAndWait();
        return answer.isPresent() && answer.get() == ButtonType.OK;
    }
}
