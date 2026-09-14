package com.studyflow.ui;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The application shell: a fixed navigation sidebar on the left and, in the
 * centre, whichever {@link ContentView} the user has selected.
 *
 * <p>This is the one place that knows the app has multiple pages. Each page is
 * just a {@link ContentView}; the shell lists them, swaps them, and refreshes
 * the visible one. Adding a new screen is a one-line change in {@code Main}.
 */
public class MainView {

    private final BorderPane root = new BorderPane();
    private final List<ContentView> views;
    private final Map<ContentView, Label> navLabels = new LinkedHashMap<>();

    public MainView(List<ContentView> views) {
        this.views = Objects.requireNonNull(views, "views must not be null");
        if (views.isEmpty()) {
            throw new IllegalArgumentException("at least one view is required");
        }

        root.getStyleClass().add("app-root");
        root.setLeft(buildSidebar());

        select(views.get(0));
    }

    public Parent getRoot() {
        return root;
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

        for (ContentView view : views) {
            Label item = new Label(view.title());
            item.getStyleClass().add("nav-item");
            item.setMaxWidth(Double.MAX_VALUE);
            item.setOnMouseClicked(event -> select(view));
            navLabels.put(view, item);
            sidebar.getChildren().add(item);
        }

        return sidebar;
    }

    /** Shows a page, refreshing its data and highlighting its sidebar entry. */
    private void select(ContentView view) {
        navLabels.forEach((candidate, label) ->
                label.getStyleClass().remove("nav-item-active"));
        navLabels.get(view).getStyleClass().add("nav-item-active");

        view.refresh();
        root.setCenter(view.getRoot());
    }
}
