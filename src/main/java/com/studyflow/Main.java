package com.studyflow;

import com.studyflow.config.MariaDbConnectionProvider;
import com.studyflow.dao.JdbcTaskDao;
import com.studyflow.service.TaskService;
import com.studyflow.ui.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Application entry point.
 *
 * <p>This class is where the object graph is assembled: it builds the
 * connection provider, hands it to the DAO, hands the DAO to the service, and
 * hands the service to the UI. Every layer below receives its dependencies
 * instead of creating them, which is what keeps all of them testable.
 *
 * <p>Referenced by {@code javafx-maven-plugin} in {@code pom.xml} — if this
 * class is renamed or moved, {@code mvn javafx:run} stops working.
 */
public class Main extends Application {

    private static final String TITLE = "StudyFlow";
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;

    @Override
    public void start(Stage stage) {
        try {
            TaskService taskService = buildTaskService();

            DashboardView dashboard = new DashboardView(taskService);
            Scene scene = new Scene(dashboard.getRoot(), WIDTH, HEIGHT);
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

    /** Wires the persistence stack together. */
    private TaskService buildTaskService() {
        var connections = MariaDbConnectionProvider.fromEnvironment();
        var taskDao = new JdbcTaskDao(connections);
        return new TaskService(taskDao);
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
