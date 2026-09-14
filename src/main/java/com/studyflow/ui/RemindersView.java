package com.studyflow.ui;

import com.studyflow.model.Reminder;
import com.studyflow.model.Task;
import com.studyflow.service.ReminderService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The reminders page: schedule nudges for tasks, see which are due, mark them
 * sent, or delete them.
 *
 * <p>Reminders have no "find all" query — they hang off tasks — so this page
 * gathers them by walking each task's reminders. That keeps the data layer's API
 * small and honest; assembling a cross-task view is a UI concern.
 */
public class RemindersView implements ContentView {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final ReminderService reminderService;
    private final TaskService taskService;

    private final VBox root = new VBox();
    private final ObservableList<Reminder> reminders = FXCollections.observableArrayList();
    private final TableView<Reminder> table = new TableView<>(reminders);

    private final Button sentButton = new Button("Mark sent");
    private final Button deleteButton = new Button("Delete");

    private Map<Integer, String> taskTitles = Map.of();

    public RemindersView(ReminderService reminderService, TaskService taskService) {
        this.reminderService = Objects.requireNonNull(reminderService, "reminderService must not be null");
        this.taskService = Objects.requireNonNull(taskService, "taskService must not be null");

        Label heading = new Label("Reminders");
        heading.getStyleClass().add("page-title");

        Label subtitle = new Label("Nudges so nothing slips through");
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
        return "Reminders";
    }

    @Override
    public void refresh() {
        List<Task> tasks = taskService.findAll();

        Map<Integer, String> titles = new HashMap<>();
        List<Reminder> all = new ArrayList<>();
        for (Task task : tasks) {
            titles.put(task.getId(), task.getTitle());
            all.addAll(reminderService.findByTask(task.getId()));
        }

        taskTitles = titles;
        reminders.setAll(all);
    }

    private HBox buildToolbar() {
        Button addButton = new Button("Add reminder");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(event -> onAdd());

        sentButton.setOnAction(event -> onMarkSent());
        deleteButton.setOnAction(event -> onDelete());

        setSelectionButtonsDisabled(true);
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, was, now) -> setSelectionButtonsDisabled(now == null));

        HBox toolbar = new HBox(addButton, sentButton, deleteButton);
        toolbar.setSpacing(8);
        return toolbar;
    }

    private void setSelectionButtonsDisabled(boolean disabled) {
        sentButton.setDisable(disabled);
        deleteButton.setDisable(disabled);
    }

    private TableView<Reminder> buildTable() {
        table.getStyleClass().add("task-table");
        table.setPlaceholder(new Label("No reminders yet — add one for a task."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Reminder, String> taskColumn = new TableColumn<>("Task");
        taskColumn.setCellValueFactory(row -> new SimpleStringProperty(
                taskTitles.getOrDefault(row.getValue().getTaskId(), "—")));
        taskColumn.setPrefWidth(320);

        TableColumn<Reminder, String> whenColumn = new TableColumn<>("Remind at");
        whenColumn.setCellValueFactory(row -> new SimpleStringProperty(
                row.getValue().getRemindAt().format(DATE_FORMAT)));
        whenColumn.setPrefWidth(200);

        TableColumn<Reminder, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(row ->
                new SimpleStringProperty(describe(row.getValue())));
        statusColumn.setPrefWidth(140);

        table.getColumns().add(taskColumn);
        table.getColumns().add(whenColumn);
        table.getColumns().add(statusColumn);

        return table;
    }

    private String describe(Reminder reminder) {
        if (reminder.isSent()) {
            return "Sent";
        }
        return reminder.isDue(LocalDateTime.now()) ? "Due" : "Scheduled";
    }

    private void onAdd() {
        List<Task> tasks = taskService.findAll();
        if (tasks.isEmpty()) {
            info("Add a task first", "Reminders are attached to a task. Create one on the Assignments page.");
            return;
        }

        ReminderFormDialog.createNew(tasks).ifPresent(reminder -> {
            try {
                reminderService.schedule(reminder.getTaskId(), reminder.getRemindAt(), LocalDateTime.now());
                refresh();
            } catch (IllegalArgumentException e) {
                info("Cannot schedule", e.getMessage());
            }
        });
    }

    private void onMarkSent() {
        Reminder selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        reminderService.markSent(selected.getId());
        refresh();
    }

    private void onDelete() {
        Reminder selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (confirm("Delete reminder", "Delete this reminder? This cannot be undone.")) {
            reminderService.deleteReminder(selected.getId());
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

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
