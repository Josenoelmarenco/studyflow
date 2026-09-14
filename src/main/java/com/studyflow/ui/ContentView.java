package com.studyflow.ui;

import javafx.scene.Parent;

/**
 * A page that can live inside the {@link MainView} navigation shell.
 *
 * <p>Keeping every screen behind this small interface is what lets
 * {@link MainView} treat them uniformly: it can list them in the sidebar, show
 * one at a time, and refresh whichever is on screen — without knowing anything
 * about what each page actually does.
 */
public interface ContentView {

    /** The page's root node, placed in the centre of the shell. */
    Parent getRoot();

    /** The label shown for this page in the navigation sidebar. */
    String title();

    /**
     * Reloads this page's data from its service(s). Called every time the page
     * is shown, so the user always sees current data after edits made elsewhere.
     */
    default void refresh() {
        // Pages with no dynamic data can ignore this.
    }
}
