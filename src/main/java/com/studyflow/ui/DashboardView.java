package com.studyflow.ui;

import com.studyflow.model.Course;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.service.CourseService;
import com.studyflow.service.TaskService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The home page: summary counters plus a table of the tasks that need attention
 * soonest (overdue and upcoming).
 *
 * <p>The view never queries the database. It asks {@link TaskService} and
 * {@link CourseService} and renders the answers, so swapping the data source
 * would leave this class untouched.
 */
public class DashboardView implements ContentView {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final int UPCOMING_DAYS = 14;

    private final TaskService taskService;
    private final CourseService courseService;

    private final VBox root = new VBox();
    private final ObservableList<Task> attention = FXCollections.observableArrayList();
    private final HBox cards = new HBox();

    private Map<Integer, String> courseNames = Map.of();

    public DashboardView(TaskService taskService, CourseService courseService) {
        this.taskService = Objects.requireNonNull(taskService, "taskService must not be null");
        this.courseService = Objects.requireNonNull(courseService, "courseService must not be null");

        Label heading = new Label("Dashboard");
        heading.getStyleClass().add("page-title");

        Label subtitle = new Label("What needs your attention this semester");
        subtitle.getStyleClass().add("page-subtitle");

        Label tableHeading = new Label("Overdue & upcoming");
        tableHeading.getStyleClass().add("section-title");

        cards.setSpacing(12);
        VBox.setMargin(cards, new Insets(16, 0, 8, 0));

        root.getChildren().addAll(heading, subtitle, cards, tableHeading, buildTable());
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
        return "Dashboard";
    }

    @Override
    public void refresh() {
        courseNames = loadCourseNames();
        rebuildCards();

        LocalDateTime now = LocalDateTime.now();
        List<Task> overdue = taskService.findOverdue(now);
        List<Task> upcoming = taskService.findUpcoming(now, UPCOMING_DAYS);

        attention.setAll(overdue);
        attention.addAll(upcoming);
    }

    private Map<Integer, String> loadCourseNames() {
        Map<Integer, String> names = new HashMap<>();
        for (Course course : courseService.findAll()) {
            names.put(course.getId(), course.displayName());
        }
        return names;
    }

    private void rebuildCards() {
        LocalDateTime now = LocalDateTime.now();
        cards.getChildren().setAll(
                summaryCard("Total", String.valueOf(taskService.findAll().size()), "card-neutral"),
                summaryCard("Pending",
                        String.valueOf(taskService.countByStatus(TaskStatus.PENDING)), "card-pending"),
                summaryCard("In progress",
                        String.valueOf(taskService.countByStatus(TaskStatus.IN_PROGRESS)), "card-progress"),
                summaryCard("Overdue",
                        String.valueOf(taskService.findOverdue(now).size()), "card-overdue"),
                summaryCard("Completed",
                        "%.0f%%".formatted(taskService.completionRate() * 100), "card-done")
        );
    }

    private VBox summaryCard(String caption, String value, String styleClass) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value");

        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("card-caption");

        VBox card = new VBox(valueLabel, captionLabel);
        card.getStyleClass().addAll("card", styleClass);
        card.setPadding(new Insets(16));
        card.setSpacing(4);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(160);
        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    private TableView<Task> buildTable() {
        TableView<Task> table = new TableView<>(attention);
        table.getStyleClass().add("task-table");
        table.setPlaceholder(new Label("Nothing overdue or due soon — you're on top of things."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Task, String> titleColumn = new TableColumn<>("Task");
        titleColumn.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getTitle()));
        titleColumn.setPrefWidth(300);

        TableColumn<Task, String> courseColumn = new TableColumn<>("Course");
        courseColumn.setCellValueFactory(row -> new SimpleStringProperty(
                courseNames.getOrDefault(row.getValue().getCourseId(), "—")));
        courseColumn.setPrefWidth(220);

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
}
