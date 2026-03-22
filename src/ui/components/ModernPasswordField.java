package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * ModernPasswordField – Styled password field with animated focus ring,
 * matching the ModernTextField design system.
 */
public class ModernPasswordField extends JPasswordField {

    private float   focusProgress = 0f;
    private Timer   focusTimer;
    private boolean focused       = false;
    private final String placeholder;

    public ModernPasswordField(String placeholder) {
        super(20);
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(new EmptyBorder(10, 4, 10, 4));
        setBackground(AppColors.BG_FIELD);
        setForeground(AppColors.TEXT_PRIMARY);
        setCaretColor(AppColors.ACCENT);
        setFont(AppFonts.FIELD);
        setSelectionColor(new Color(0xC7D2FE));
        setEchoChar('\u2022'); // bullet

        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                focused = true;
                animateFocus(true);
            }
            @Override public void focusLost(FocusEvent e) {
                focused = false;
                animateFocus(false);
            }
        });
    }

    public ModernPasswordField() { this(""); }

    private void animateFocus(boolean in) {
        if (focusTimer != null && focusTimer.isRunning()) focusTimer.stop();
        focusTimer = new Timer(12, null);
        focusTimer.addActionListener(e -> {
            if (in) {
                focusProgress = Math.min(1f, focusProgress + 0.07f);
                if (focusProgress >= 1f) focusTimer.stop();
            } else {
                focusProgress = Math.max(0f, focusProgress - 0.07f);
                if (focusProgress <= 0f) focusTimer.stop();
            }
            repaint();
        });
        focusTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background fill
        g2.setColor(AppColors.BG_FIELD);
        g2.fillRoundRect(0, 0, w, h, 10, 10);

        super.paintComponent(g2);

        // Placeholder
        if (getPassword().length == 0 && !focused) {
            if (placeholder != null && !placeholder.isEmpty()) {
                g2.setFont(AppFonts.FIELD);
                g2.setColor(AppColors.TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                Insets ins = getInsets();
                g2.drawString(placeholder, ins.left + 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            }
        }

        // Border
        if (focusProgress > 0f) {
            Color fc = new Color(99, 102, 241, (int)(focusProgress * 200));
            g2.setColor(fc);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            int lineLen = (int)(w * focusProgress);
            int startX  = (w - lineLen) / 2;
            g2.setColor(AppColors.ACCENT);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(startX, h - 1, startX + lineLen, h - 1);
        } else {
            g2.setColor(AppColors.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
        }

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) { /* suppress default border */ }

    @Override
    public boolean isOpaque() { return false; }
}
