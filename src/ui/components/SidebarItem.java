package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * SidebarItem – Pill-shaped nav button with animated hover/active states
 * and a neon left-border indicator when active.
 */
public class SidebarItem extends JPanel {

    private final String  label;
    private final String  iconText;   // Unicode glyph
    private boolean active  = false;
    private boolean hovered = false;
    private float   hoverAlpha = 0f;
    private Timer   hoverTimer;

    private Runnable onClick;

    public SidebarItem(String iconText, String label) {
        this.iconText = iconText;
        this.label    = label;
        setOpaque(false);
        setPreferredSize(new Dimension(220, 48));
        setMaximumSize(new Dimension(220, 48));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                hovered = true;
                animateHover(true);
            }
            @Override public void mouseExited(MouseEvent e) {
                hovered = false;
                animateHover(false);
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }
        });
    }

    public void setOnClick(Runnable r)   { this.onClick = r; }
    public void setActive(boolean state) { this.active = state; repaint(); }
    public boolean isActive()            { return active; }

    private void animateHover(boolean in) {
        if (hoverTimer != null && hoverTimer.isRunning()) hoverTimer.stop();
        hoverTimer = new Timer(16, null);
        hoverTimer.addActionListener(e -> {
            if (in) {
                hoverAlpha = Math.min(1f, hoverAlpha + 0.1f);
                if (hoverAlpha >= 1f) hoverTimer.stop();
            } else {
                hoverAlpha = Math.max(0f, hoverAlpha - 0.1f);
                if (hoverAlpha <= 0f) hoverTimer.stop();
            }
            repaint();
        });
        hoverTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int pad = 8;

        // ── Hover / Active background ───────────────────────────────────────
        if (active) {
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillRoundRect(pad, 4, w - pad * 2, h - 8, 10, 10);
        } else if (hoverAlpha > 0f) {
            g2.setColor(new Color(255, 255, 255, (int)(hoverAlpha * 12)));
            g2.fillRoundRect(pad, 4, w - pad * 2, h - 8, 10, 10);
        }

        // ── Left indicator bar (active only) ───────────────────────────────
        if (active) {
            g2.setColor(new Color(0xA5B4FC)); // indigo-300
            g2.fillRoundRect(pad, 12, 3, h - 24, 3, 3);
        }

        // ── Icon glyph ─────────────────────────────────────────────────────
        Color iconColor = active ? Color.WHITE : new Color(0xA5B4FC);
        if (hoverAlpha > 0f && !active)
            iconColor = blend(new Color(0xA5B4FC), Color.WHITE, hoverAlpha);

        g2.setColor(iconColor);
        g2.setFont(new Font("Dialog", Font.PLAIN, 16));
        g2.drawString(iconText, pad + 16, h / 2 + 6);

        // ── Label ──────────────────────────────────────────────────────────
        Color textColor = active ? Color.WHITE :
                          (hoverAlpha > 0 ? blend(new Color(0xA5B4FC), Color.WHITE, hoverAlpha)
                                          : new Color(0xA5B4FC));
        g2.setColor(textColor);
        g2.setFont(AppFonts.SIDEBAR);
        g2.drawString(label, pad + 42, h / 2 + 5);

        g2.dispose();
    }

    private Color blend(Color a, Color b, float t) {
        int r = (int)(a.getRed()   * (1 - t) + b.getRed()   * t);
        int gr= (int)(a.getGreen() * (1 - t) + b.getGreen() * t);
        int bl= (int)(a.getBlue()  * (1 - t) + b.getBlue()  * t);
        return new Color(r, gr, bl);
    }
}
