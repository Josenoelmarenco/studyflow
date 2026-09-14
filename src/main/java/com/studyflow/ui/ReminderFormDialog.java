package com.studyflow.ui;

import com.studyflow.model.Reminder;
import com.studyflow.model.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * A modal form for scheduling a reminder against a task.
 *
 * <p>Produces a {@link Reminder} carrying the chosen task id and moment; the
 * "not in the past" rule itself is enforced by {@code ReminderService} when the
 * caller schedules it, so the dialog only checks that the fields are complete
 * and well-formed.
 */
public final class ReminderFormDialog {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Dialog<Reminder> dialog = new Dialog<>();

    private final ComboBox<Task> taskBox = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker();
    private final TextField timeField = new TextField();

    private final Reminder existing;

    private ReminderFormDialog(List<Task> tasks, Reminder existing) {
        this.existing = existing;

        dialog.setTitle(existing == null ? "New reminder" : "Edit reminder");
        dialog.setHeaderText("Schedule a reminder for a task");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(buildForm(tasks));

        prefill(tasks);
        installValidation();

        dialog.setResultConverter(button -> button == ButtonType.OK ? buildReminder() : null);
    }

    public static Optional<Reminder> createNew(List<Task> tasks) {
        return new ReminderFormDialog(tasks, null).dialog.showAndWait();
    }

    public static Optional<Reminder> edit(List<Task> tasks, Reminder reminder) {
        return new ReminderFormDialog(tasks, reminder).dialog.showAndWait();
    }

    private GridPane buildForm(List<Task> tasks) {
        taskBox.getItems().setAll(tasks);
        taskBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Task task) {
                return task == null ? "" : task.getTitle();
            }

            @Override
            public Task fromString(String string) {
                return null;
            }
        });
        taskBox.setMaxWidth(Double.MAX_VALUE);

        timeField.setPromptText("HH:mm");
        timeField.setPrefColumnCount(6);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Task"), 0, 0);
        grid.add(taskBox, 1, 0, 2, 1);
        grid.add(new Label("Date"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Time"), 0, 2);
        grid.add(timeField, 1, 2);

        return grid;
    }

    private void prefill(List<Task> tasks) {
        if (existing == null) {
            return;
        }
        for (Task task : tasks) {
            if (task.getId() == existing.getTaskId()) {
                taskBox.setValue(task);
                break;
            }
        }
        datePicker.setValue(existing.getRemindAt().toLocalDate());
        timeField.setText(existing.getRemindAt().toLocalTime().format(TIME_FORMAT));
    }

    private void installValidation() {
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String problem = validate();
            if (problem != null) {
                showError(problem);
                event.consume();
            }
        });
    }

    private String validate() {
        if (taskBox.getValue() == null) {
            return "Please choose a task.";
        }
        if (datePicker.getValue() == null) {
            return "Please pick a date.";
        }
        try {
            parseTime();
        } catch (Exception e) {
            return "Time must be in HH:mm format, e.g. 09:00.";
        }
        return null;
    }

    private LocalTime parseTime() {
        if (timeField.getText() == null || timeField.getText().isBlank()) {
            return LocalTime.of(9, 0);
        }
        return LocalTime.parse(timeField.getText().trim(), TIME_FORMAT);
    }

    private Reminder buildReminder() {
        LocalDate date = datePicker.getValue();
        LocalDateTime remindAt = LocalDateTime.of(date, parseTime());
        int taskId = taskBox.getValue().getId();

        if (existing == null) {
            return new Reminder(taskId, remindAt);
        }
        return new Reminder(existing.getId(), taskId, remindAt, existing.isSent());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Check the form");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
