package ui.components;

import java.awt.*;

/**
 * Application font registry.
 * Uses "Segoe UI" on Windows (clean SaaS look) with "Arial" fallback.
 */
public class AppFonts {

    private static final String PRIMARY = resolveFont();

    private static String resolveFont() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String name : ge.getAvailableFontFamilyNames()) {
            if (name.equalsIgnoreCase("Segoe UI"))    return "Segoe UI";
            if (name.equalsIgnoreCase("Inter"))        return "Inter";
            if (name.equalsIgnoreCase("Montserrat"))   return "Montserrat";
        }
        return "Arial";
    }

    public static final Font TITLE    = new Font(PRIMARY, Font.BOLD,   30);
    public static final Font SUBTITLE = new Font(PRIMARY, Font.PLAIN,  14);
    public static final Font SECTION  = new Font(PRIMARY, Font.BOLD,   20);
    public static final Font BODY     = new Font(PRIMARY, Font.PLAIN,  14);
    public static final Font SIDEBAR  = new Font(PRIMARY, Font.BOLD,   15);
    public static final Font BUTTON   = new Font(PRIMARY, Font.BOLD,   14);
    public static final Font FIELD    = new Font(PRIMARY, Font.PLAIN,  15);
    public static final Font LABEL    = new Font(PRIMARY, Font.BOLD,   13);
    public static final Font TOAST    = new Font(PRIMARY, Font.BOLD,   13);
}
