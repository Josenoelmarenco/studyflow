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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The schedule page: every dated task laid out in chronological order — the
 * "when is everything happening" view of the semester.
 *
 * <p>Tasks with no deadline are left out here on purpose: a schedule is about
 * dates, and an undated task has no place on a timeline (it still appears on the
 * Assignments page).
 */
public class ScheduleView implements ContentView {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE dd MMM yyyy, HH:mm");

    private final TaskService taskService;
    private final CourseService courseService;

    private final VBox root = new VBox();
    private final ObservableList<Task> dated = FXCollections.observableArrayList();

    private Map<Integer, String> courseNames = Map.of();

    public ScheduleView(TaskService taskService, CourseService courseService) {
        this.taskService = Objects.requireNonNull(taskService, "taskService must not be null");
        this.courseService = Objects.requireNonNull(courseService, "courseService must not be null");

        Label heading = new Label("Schedule");
        heading.getStyleClass().add("page-title");

        Label subtitle = new Label("Everything with a deadline, in order");
        subtitle.getStyleClass().add("page-subtitle");

        root.getChildren().addAll(heading, subtitle, buildTable());
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
        return "Schedule";
    }

    @Override
    public void refresh() {
        courseNames = loadCourseNames();

        List<Task> withDeadline = taskService.findAll().stream()
                .filter(task -> task.getDeadline() != null)
                .toList();
        dated.setAll(withDeadline);
    }

    private Map<Integer, String> loadCourseNames() {
        Map<Integer, String> names = new HashMap<>();
        for (Course course : courseService.findAll()) {
            names.put(course.getId(), course.displayName());
        }
        return names;
    }

    private TableView<Task> buildTable() {
        TableView<Task> table = new TableView<>(dated);
        table.getStyleClass().add("task-table");
        table.setPlaceholder(new Label("Nothing scheduled — add a deadline to a task."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Task, String> whenColumn = new TableColumn<>("When");
        whenColumn.setCellValueFactory(row -> new SimpleStringProperty(
                row.getValue().getDeadline().format(DATE_FORMAT)));
        whenColumn.setPrefWidth(240);

        TableColumn<Task, String> titleColumn = new TableColumn<>("Task");
        titleColumn.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getTitle()));
        titleColumn.setPrefWidth(280);

        TableColumn<Task, String> courseColumn = new TableColumn<>("Course");
        courseColumn.setCellValueFactory(row -> new SimpleStringProperty(
                courseNames.getOrDefault(row.getValue().getCourseId(), "—")));
        courseColumn.setPrefWidth(200);

        TableColumn<Task, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getStatus().label()));
        statusColumn.setPrefWidth(120);

        table.getColumns().add(whenColumn);
        table.getColumns().add(titleColumn);
        table.getColumns().add(courseColumn);
        table.getColumns().add(statusColumn);

        return table;
    }
}
