package com.studyflow.ui;

import com.studyflow.model.Course;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import com.studyflow.service.CourseService;
import com.studyflow.service.TaskService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

/**
 * The progress page: how far along the semester is, overall and per course.
 *
 * <p>Each bar is the share of a course's tasks that are done. It reads straight
 * from the services, so it always reflects the latest state of the planner.
 */
public class ProgressView implements ContentView {

    private final TaskService taskService;
    private final CourseService courseService;

    private final VBox root = new VBox();
    private final VBox rows = new VBox();
    private final Label overallLabel = new Label();
    private final ProgressBar overallBar = new ProgressBar(0);

    public ProgressView(TaskService taskService, CourseService courseService) {
        this.taskService = Objects.requireNonNull(taskService, "taskService must not be null");
        this.courseService = Objects.requireNonNull(courseService, "courseService must not be null");

        Label heading = new Label("Progress");
        heading.getStyleClass().add("page-title");

        Label subtitle = new Label("How far along you are");
        subtitle.getStyleClass().add("page-subtitle");

        Label overallCaption = new Label("Overall completion");
        overallCaption.getStyleClass().add("section-title");

        overallBar.setPrefWidth(420);

        HBox overallRow = new HBox(overallBar, overallLabel);
        overallRow.setSpacing(12);
        overallRow.setAlignment(Pos.CENTER_LEFT);

        Label perCourseCaption = new Label("By course");
        perCourseCaption.getStyleClass().add("section-title");

        rows.setSpacing(12);

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("progress-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(
                heading, subtitle, overallCaption, overallRow, perCourseCaption, scroll);
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
        return "Progress";
    }

    @Override
    public void refresh() {
        double overall = taskService.completionRate();
        overallBar.setProgress(overall);
        overallLabel.setText("%.0f%%".formatted(overall * 100));

        rows.getChildren().clear();
        List<Course> courses = courseService.findAll();
        if (courses.isEmpty()) {
            rows.getChildren().add(new Label("No courses yet — add one to see progress."));
            return;
        }

        for (Course course : courses) {
            rows.getChildren().add(courseRow(course));
        }
    }

    private VBox courseRow(Course course) {
        List<Task> tasks = taskService.findByCourse(course.getId());
        long done = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        double rate = tasks.isEmpty() ? 0.0 : (double) done / tasks.size();

        Label name = new Label(course.displayName());
        name.getStyleClass().add("progress-course-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label count = new Label(done + " / " + tasks.size() + " done");
        count.getStyleClass().add("progress-count");

        HBox header = new HBox(name, spacer, count);
        header.setAlignment(Pos.CENTER_LEFT);

        ProgressBar bar = new ProgressBar(rate);
        bar.setMaxWidth(Double.MAX_VALUE);

        VBox row = new VBox(header, bar);
        row.setSpacing(6);
        row.getStyleClass().add("progress-row");
        return row;
    }
}
