package com.studyflow.ui;

import com.studyflow.model.Course;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
 * A modal form for creating or editing a {@link Task}.
 *
 * <p>Validation follows the idiomatic JavaFX pattern: an event filter on the OK
 * button checks the input and consumes the click when something is wrong, so the
 * dialog stays open and the user sees an inline message instead of a stack
 * trace. The domain model's own constructor validation is therefore never
 * reached with bad data.
 */
public final class TaskFormDialog {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Dialog<Task> dialog = new Dialog<>();

    private final ComboBox<Course> courseBox = new ComboBox<>();
    private final TextField titleField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final DatePicker deadlineDate = new DatePicker();
    private final TextField deadlineTime = new TextField();
    private final ComboBox<TaskStatus> statusBox = new ComboBox<>();

    private final Task existing;

    private TaskFormDialog(List<Course> courses, Task existing) {
        this.existing = existing;

        dialog.setTitle(existing == null ? "New assignment" : "Edit assignment");
        dialog.setHeaderText(existing == null
                ? "Add an assignment or exam"
                : "Edit \"" + existing.getTitle() + "\"");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(buildForm(courses));

        prefill();
        installValidation();

        dialog.setResultConverter(button -> button == ButtonType.OK ? buildTask() : null);
    }

    /** Opens a dialog for a brand-new task. */
    public static Optional<Task> createNew(List<Course> courses) {
        return new TaskFormDialog(courses, null).dialog.showAndWait();
    }

    /** Opens a dialog pre-filled with an existing task for editing. */
    public static Optional<Task> edit(List<Course> courses, Task task) {
        return new TaskFormDialog(courses, task).dialog.showAndWait();
    }

    private GridPane buildForm(List<Course> courses) {
        courseBox.getItems().setAll(courses);
        courseBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Course course) {
                return course == null ? "" : course.displayName();
            }

            @Override
            public Course fromString(String string) {
                return null;
            }
        });
        courseBox.setMaxWidth(Double.MAX_VALUE);

        titleField.setPromptText("e.g. Sprint 2 report");

        descriptionArea.setPromptText("Optional notes");
        descriptionArea.setPrefRowCount(3);

        deadlineTime.setPromptText("HH:mm (defaults to 23:59)");
        deadlineTime.setPrefColumnCount(6);

        statusBox.getItems().setAll(TaskStatus.values());
        statusBox.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Course"), 0, 0);
        grid.add(courseBox, 1, 0, 2, 1);
        grid.add(new Label("Title"), 0, 1);
        grid.add(titleField, 1, 1, 2, 1);
        grid.add(new Label("Description"), 0, 2);
        grid.add(descriptionArea, 1, 2, 2, 1);
        grid.add(new Label("Deadline"), 0, 3);
        grid.add(deadlineDate, 1, 3);
        grid.add(deadlineTime, 2, 3);
        grid.add(new Label("Status"), 0, 4);
        grid.add(statusBox, 1, 4, 2, 1);

        return grid;
    }

    private void prefill() {
        if (existing == null) {
            statusBox.setValue(TaskStatus.PENDING);
            return;
        }

        for (Course course : courseBox.getItems()) {
            if (course.getId() == existing.getCourseId()) {
                courseBox.setValue(course);
                break;
            }
        }
        titleField.setText(existing.getTitle());
        descriptionArea.setText(existing.getDescription());
        statusBox.setValue(existing.getStatus());

        LocalDateTime deadline = existing.getDeadline();
        if (deadline != null) {
            deadlineDate.setValue(deadline.toLocalDate());
            deadlineTime.setText(deadline.toLocalTime().format(TIME_FORMAT));
        }
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

    /** Returns an error message, or {@code null} when the form is valid. */
    private String validate() {
        if (courseBox.getValue() == null) {
            return "Please choose a course.";
        }
        if (titleField.getText() == null || titleField.getText().isBlank()) {
            return "Please enter a title.";
        }
        if (deadlineDate.getValue() == null && !deadlineTime.getText().isBlank()) {
            return "You entered a time but no date. Pick a date, or clear the time.";
        }
        if (deadlineDate.getValue() != null) {
            try {
                parseTime();
            } catch (Exception e) {
                return "Time must be in HH:mm format, e.g. 23:59.";
            }
        }
        return null;
    }

    private LocalTime parseTime() {
        if (deadlineTime.getText() == null || deadlineTime.getText().isBlank()) {
            return LocalTime.of(23, 59);
        }
        return LocalTime.parse(deadlineTime.getText().trim(), TIME_FORMAT);
    }

    private Task buildTask() {
        Course course = courseBox.getValue();

        LocalDateTime deadline = null;
        LocalDate date = deadlineDate.getValue();
        if (date != null) {
            deadline = LocalDateTime.of(date, parseTime());
        }

        String description = descriptionArea.getText() == null || descriptionArea.getText().isBlank()
                ? null
                : descriptionArea.getText().trim();

        if (existing == null) {
            return new Task(course.getId(), titleField.getText().trim(), description,
                    deadline, statusBox.getValue());
        }

        return new Task(existing.getId(), course.getId(), titleField.getText().trim(),
                description, deadline, statusBox.getValue());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Check the form");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
