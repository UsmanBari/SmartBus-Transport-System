import config.DatabaseConnection;
import ui.screens.AdminDashboard;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Application entry point.
 * Verifies DB connectivity, then launches the AdminDashboard on the EDT.
 */
public class Main {

    public static void main(String[] args) {

        // ── Global rendering hints ───────────────────────────────────────────
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext",                "true");
        System.setProperty("sun.java2d.opengl",           "false");

        // ── Dark popup menus (best-effort on Windows) ────────────────────────
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // ── DB connectivity probe ────────────────────────────────────────────
        try {
            DatabaseConnection.getInstance();
            System.out.println("Database connected successfully");
        } catch (SQLException e) {
            System.err.println("[WARN] DB not reachable at startup: " + e.getMessage());
            System.err.println("       The dashboard will still launch; DB errors will be shown inline.");
        }

        System.out.println("Premium Admin Dashboard Running");

        // ── Launch on EDT ────────────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            // Improve rendering pipeline
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
            System.setProperty("sun.awt.noerasebackground", "true");
            new AdminDashboard();
        });
    }
}
