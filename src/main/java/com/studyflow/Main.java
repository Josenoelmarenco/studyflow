package com.studyflow;

import com.studyflow.config.MariaDbConnectionProvider;
import com.studyflow.dao.JdbcCourseDao;
import com.studyflow.dao.JdbcReminderDao;
import com.studyflow.dao.JdbcTaskDao;
import com.studyflow.dao.JdbcUserDao;
import com.studyflow.model.User;
import com.studyflow.service.CourseService;
import com.studyflow.service.ReminderService;
import com.studyflow.service.TaskService;
import com.studyflow.service.UserService;
import com.studyflow.ui.AssignmentsView;
import com.studyflow.ui.ContentView;
import com.studyflow.ui.CoursesView;
import com.studyflow.ui.DashboardView;
import com.studyflow.ui.MainView;
import com.studyflow.ui.ProgressView;
import com.studyflow.ui.RemindersView;
import com.studyflow.ui.ScheduleView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.List;

/**
 * Application entry point.
 *
 * <p>This class is where the object graph is assembled: it builds one connection
 * provider, hands it to every DAO, hands the DAOs to the services, and hands the
 * services to the views. Every layer receives its dependencies instead of
 * creating them, which is what keeps all of them testable.
 *
 * <p>Referenced by {@code javafx-maven-plugin} in {@code pom.xml} — if this class
 * is renamed or moved, {@code mvn javafx:run} stops working.
 */
public class Main extends Application {

    private static final String TITLE = "StudyFlow";
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;

    // Single-user desktop app: this is the local account created on first launch.
    private static final String DEFAULT_NAME = "Demo Student";
    private static final String DEFAULT_EMAIL = "demo@studyflow.local";
    private static final String DEFAULT_PASSWORD_HASH = "not-a-real-hash";

    @Override
    public void start(Stage stage) {
        try {
            MainView mainView = buildMainView();

            Scene scene = new Scene(mainView.getRoot(), WIDTH, HEIGHT);
            scene.getStylesheets().add(
                    Main.class.getResource("/com/studyflow/ui/styles.css").toExternalForm());

            stage.setTitle(TITLE);
            stage.setScene(scene);
            stage.setMinWidth(1024);
            stage.setMinHeight(640);
            stage.show();

        } catch (RuntimeException e) {
            showStartupError(e);
        }
    }

    /** Wires the whole stack together and returns the shell, ready to show. */
    private MainView buildMainView() {
        var connections = MariaDbConnectionProvider.fromEnvironment();

        var userDao = new JdbcUserDao(connections);
        var courseDao = new JdbcCourseDao(connections);
        var taskDao = new JdbcTaskDao(connections);
        var reminderDao = new JdbcReminderDao(connections);

        var userService = new UserService(userDao);
        var courseService = new CourseService(courseDao);
        var taskService = new TaskService(taskDao);
        var reminderService = new ReminderService(reminderDao);

        User currentUser = userService.findOrCreateDefault(
                DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD_HASH);
        int userId = currentUser.getId();

        List<ContentView> views = List.of(
                new DashboardView(taskService, courseService),
                new CoursesView(courseService, userId),
                new AssignmentsView(taskService, courseService),
                new ScheduleView(taskService, courseService),
                new RemindersView(reminderService, taskService),
                new ProgressView(taskService, courseService)
        );

        return new MainView(views);
    }

    /**
     * Shows a readable dialog instead of dumping a stack trace to a console the
     * user will never see. The most common cause is a missing
     * {@code config.properties} or a database that is not running.
     */
    private void showStartupError(RuntimeException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("StudyFlow could not start");
        alert.setHeaderText("Database connection failed");
        alert.setContentText("""
                %s

                Check that:
                  • MariaDB is running
                  • db/schema.sql has been applied
                  • src/main/resources/config.properties exists
                    (copy it from config.properties.example)
                """.formatted(e.getMessage()));
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
