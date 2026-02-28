package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ToastNotification – Floating slide-in toast that auto-dismisses after 3 s.
 * Shown relative to a parent JFrame.
 */
public class ToastNotification extends JWindow {

    public enum Type { SUCCESS, ERROR, INFO }

    private float alpha     = 0f;
    private float targetY;
    private float currentY;
    private Timer slideTimer;
    private Timer dismissTimer;

    private final String  message;
    private final Type    type;
    private final JFrame  parent;

    private static final int W = 340;
    private static final int H = 54;

    public ToastNotification(JFrame parent, String message, Type type) {
        super(parent);
        this.parent  = parent;
        this.message = message;
        this.type    = type;
        setSize(W, H);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        JPanel pane = buildPane();
        add(pane);
    }

    private JPanel buildPane() {
        return new JPanel() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Background
                Color fill = type == Type.SUCCESS ? new Color(0xD1FAE5)
                           : type == Type.ERROR   ? new Color(0xFEE2E2)
                           :                        new Color(0xEEF2FF);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);

                // Accent border
                Color accent = type == Type.SUCCESS ? AppColors.SUCCESS
                             : type == Type.ERROR   ? AppColors.ERROR
                             :                        AppColors.NEON_CYAN;
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(accent);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

                // Dot indicator
                g2.setColor(accent);
                g2.fillOval(14, h / 2 - 5, 10, 10);

                // Message
                Color textC = type == Type.SUCCESS ? new Color(0x065F46)
                            : type == Type.ERROR   ? new Color(0x991B1B)
                            :                        new Color(0x3730A3);
                g2.setColor(textC);
                g2.setFont(AppFonts.TOAST);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(message, 34, (h + fm.getAscent() - fm.getDescent()) / 2);

                g2.dispose();
            }
        };
    }

    /** Show the toast anchored to bottom-right of parent frame. */
    public void show(int offsetFromBottom) {
        if (parent == null) return;
        Point loc = parent.getLocationOnScreen();
        Dimension ps = parent.getSize();
        int x = loc.x + ps.width  - W - 24;
        int startY = loc.y + ps.height + H;       // off-screen below
        targetY    = loc.y + ps.height - (H + 16) - offsetFromBottom;

        currentY   = startY;
        setLocation(x, (int) currentY);
        setVisible(true);

        // Slide-in + fade-in
        slideTimer = new Timer(14, null);
        slideTimer.addActionListener(e -> {
            currentY  = lerp(currentY, targetY, 0.18f);
            alpha     = Math.min(1f, alpha + 0.06f);
            setLocation(x, (int) currentY);
            repaint();
            if (Math.abs(currentY - targetY) < 1f && alpha >= 1f) {
                currentY = targetY;
                alpha    = 1f;
                slideTimer.stop();
                scheduleDismiss(x);
            }
        });
        slideTimer.start();
    }

    private void scheduleDismiss(int x) {
        dismissTimer = new Timer(3000, null);
        dismissTimer.setRepeats(false);
        dismissTimer.addActionListener(e -> {
            dismissTimer.stop();
            Timer fadeOut = new Timer(14, null);
            fadeOut.addActionListener(fe -> {
                alpha -= 0.06f;
                currentY += 2f;
                setLocation(x, (int) currentY);
                repaint();
                if (alpha <= 0f) {
                    fadeOut.stop();
                    setVisible(false);
                    dispose();
                }
            });
            fadeOut.start();
        });
        dismissTimer.start();
    }

    private float lerp(float a, float b, float t) { return a + (b - a) * t; }
}
