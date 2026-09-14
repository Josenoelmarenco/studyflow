package com.studyflow.ui;

import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.service.TaskService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * The main screen: a fixed navigation sidebar plus a content panel showing
 * summary counters and the task list.
 *
 * <p>Built in plain Java rather than FXML on purpose — for a layout this size,
 * code is easier to read in a diff and easier to refactor than XML. FXML starts
 * paying off once a designer edits screens in Scene Builder.
 *
 * <p>The view never queries the database: it asks {@link TaskService} and
 * renders the answer. Swapping the data source would leave this class untouched.
 */
public class DashboardView {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private static final List<String> NAV_ITEMS = List.of(
            "Dashboard", "Schedule", "Study Plans", "Assignments", "Reminders", "Progress");

    private final TaskService taskService;
    private final BorderPane root = new BorderPane();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();

    public DashboardView(TaskService taskService) {
        this.taskService = Objects.requireNonNull(taskService, "taskService must not be null");

        root.getStyleClass().add("app-root");
        root.setLeft(buildSidebar());
        root.setCenter(buildContent());

        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    /** Reloads data from the service and updates the table. */
    public void refresh() {
        tasks.setAll(taskService.findAll());
    }

    private VBox buildSidebar() {
        Label brand = new Label("StudyFlow");
        brand.getStyleClass().add("brand");

        Label semester = new Label("Autumn 2026");
        semester.getStyleClass().add("brand-subtitle");

        VBox sidebar = new VBox(brand, semester);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setSpacing(4);

        Label navHeading = new Label("NAVIGATION");
        navHeading.getStyleClass().add("nav-heading");
        VBox.setMargin(navHeading, new Insets(32, 0, 8, 0));
        sidebar.getChildren().add(navHeading);

        for (String item : NAV_ITEMS) {
            Label navItem = new Label(item);
            navItem.getStyleClass().add("nav-item");
            navItem.setMaxWidth(Double.MAX_VALUE);
            if (item.equals("Dashboard")) {
                navItem.getStyleClass().add("nav-item-active");
            }
            sidebar.getChildren().add(navItem);
        }

        return sidebar;
    }

    private VBox buildContent() {
        Label heading = new Label("Dashboard");
        heading.getStyleClass().add("page-title");

        Label subtitle = new Label("Overview of your semester");
        subtitle.getStyleClass().add("page-subtitle");

        VBox content = new VBox(heading, subtitle, buildSummaryCards(), buildTaskTable());
        content.getStyleClass().add("content");
        content.setPadding(new Insets(32));
        content.setSpacing(16);

        return content;
    }

    private HBox buildSummaryCards() {
        LocalDateTime now = LocalDateTime.now();

        HBox cards = new HBox(
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
        cards.setSpacing(12);
        VBox.setMargin(cards, new Insets(16, 0, 8, 0));

        return cards;
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

    private TableView<Task> buildTaskTable() {
        TableView<Task> table = new TableView<>(tasks);
        table.getStyleClass().add("task-table");
        table.setPlaceholder(new Label("No tasks yet — add one to get started."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Task, String> titleColumn = new TableColumn<>("Task");
        titleColumn.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getTitle()));
        titleColumn.setPrefWidth(320);

        TableColumn<Task, String> deadlineColumn = new TableColumn<>("Deadline");
        deadlineColumn.setCellValueFactory(row -> new SimpleStringProperty(
                row.getValue().getDeadline() == null
                        ? "—"
                        : row.getValue().getDeadline().format(DATE_FORMAT)));
        deadlineColumn.setPrefWidth(200);

        TableColumn<Task, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getStatus().label()));
        statusColumn.setPrefWidth(140);

        table.getColumns().add(titleColumn);
        table.getColumns().add(deadlineColumn);
        table.getColumns().add(statusColumn);

        return table;
    }
}
