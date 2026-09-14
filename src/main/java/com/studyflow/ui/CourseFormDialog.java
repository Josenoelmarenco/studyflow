package com.studyflow.ui;

import com.studyflow.model.Course;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * A modal form for creating or editing a {@link Course}.
 *
 * <p>Same validation approach as {@link TaskFormDialog}: the OK button is
 * filtered so an empty name keeps the dialog open with an inline message.
 */
public final class CourseFormDialog {

    private final Dialog<Course> dialog = new Dialog<>();

    private final TextField nameField = new TextField();
    private final TextField codeField = new TextField();
    private final TextField semesterField = new TextField();

    private final int userId;
    private final Course existing;

    private CourseFormDialog(int userId, Course existing) {
        this.userId = userId;
        this.existing = existing;

        dialog.setTitle(existing == null ? "New course" : "Edit course");
        dialog.setHeaderText(existing == null
                ? "Add a course to your semester"
                : "Edit \"" + existing.getName() + "\"");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(buildForm());

        prefill();
        installValidation();

        dialog.setResultConverter(button -> button == ButtonType.OK ? buildCourse() : null);
    }

    public static Optional<Course> createNew(int userId) {
        return new CourseFormDialog(userId, null).dialog.showAndWait();
    }

    public static Optional<Course> edit(Course course) {
        return new CourseFormDialog(course.getUserId(), course).dialog.showAndWait();
    }

    private GridPane buildForm() {
        nameField.setPromptText("e.g. Ohjelmistotuotantoprojekti 1");
        codeField.setPromptText("e.g. TX00EY27");
        semesterField.setPromptText("e.g. Autumn 2026");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Code"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Semester"), 0, 2);
        grid.add(semesterField, 1, 2);

        nameField.setPrefColumnCount(24);
        return grid;
    }

    private void prefill() {
        if (existing == null) {
            return;
        }
        nameField.setText(existing.getName());
        codeField.setText(existing.getCode());
        semesterField.setText(existing.getSemester());
    }

    private void installValidation() {
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (nameField.getText() == null || nameField.getText().isBlank()) {
                showError("Please enter a course name.");
                event.consume();
            }
        });
    }

    private Course buildCourse() {
        String code = blankToNull(codeField.getText());
        String semester = blankToNull(semesterField.getText());

        if (existing == null) {
            return new Course(userId, nameField.getText().trim(), code, semester);
        }
        return new Course(existing.getId(), userId, nameField.getText().trim(), code, semester);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Check the form");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
