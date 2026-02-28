package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * NeonButton – Clean indigo gradient button with smooth hover/press animations.
 * Modern SaaS style inspired by Linear / Stripe.
 */
public class NeonButton extends JButton {

    private static final Color GRAD_START  = new Color(0x6366F1);   // indigo
    private static final Color GRAD_END    = new Color(0x8B5CF6);   // violet
    private static final Color GRAD_HOVER  = new Color(0x4F46E5);   // darker indigo

    private float glowAlpha   = 0f;
    private float pressOffset = 0f;
    private Timer glowTimer;
    private boolean hovered   = false;

    public NeonButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(Color.WHITE);
        setFont(AppFonts.BUTTON);
        setPreferredSize(new Dimension(180, 44));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                hovered = true;
                animateGlow(true);
            }
            @Override public void mouseExited(MouseEvent e) {
                hovered = false;
                animateGlow(false);
            }
            @Override public void mousePressed(MouseEvent e) {
                pressOffset = 2f;
                repaint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                pressOffset = 0f;
                repaint();
            }
        });
    }

    private void animateGlow(boolean fadeIn) {
        if (glowTimer != null && glowTimer.isRunning()) glowTimer.stop();
        glowTimer = new Timer(16, null);
        glowTimer.addActionListener(e -> {
            if (fadeIn) {
                glowAlpha = Math.min(1f, glowAlpha + 0.08f);
                if (glowAlpha >= 1f) glowTimer.stop();
            } else {
                glowAlpha = Math.max(0f, glowAlpha - 0.08f);
                if (glowAlpha <= 0f) glowTimer.stop();
            }
            repaint();
        });
        glowTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int arc = 25;

        // Hover translation
        int ty = (int) pressOffset;
        g2.translate(0, ty);

        // Hover shadow glow
        if (glowAlpha > 0f) {
            int spread = (int)(8 * glowAlpha);
            for (int i = spread; i > 0; i--) {
                float a = glowAlpha * (i / (float) spread) * 0.12f;
                g2.setColor(new Color(99, 102, 241, (int)(a * 255)));
                g2.fillRoundRect(-i, i + 2, w + i * 2, h + i, arc + 4, arc + 4);
            }
        }

        // Gradient fill
        Color start = hovered ? GRAD_HOVER : GRAD_START;
        GradientPaint gp = new GradientPaint(0, 0, start, w, 0, GRAD_END);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w, h - (int)pressOffset, arc, arc);

        // Top subtle highlight
        g2.setColor(new Color(255, 255, 255, 40));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(arc / 2, 1, w - arc / 2, 1);

        // Text
        g2.setColor(Color.WHITE);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx2 = (w - fm.stringWidth(getText())) / 2;
        int ty2 = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx2, ty2);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(180, 44);
    }
}
