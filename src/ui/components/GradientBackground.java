package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * GradientBackground – Clean light SaaS page background with subtle
 * decorative colour blobs for modern visual depth.
 */
public class GradientBackground extends JPanel {

    public GradientBackground() {
        setOpaque(true);
        setBackground(AppColors.BG_BASE);
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        int w = getWidth(), h = getHeight();

        // ── Clean light gradient ────────────────────────────────────────────
        GradientPaint base = new GradientPaint(0, 0, new Color(0xF4F6FB), w, h, new Color(0xEBEEF6));
        g2.setPaint(base);
        g2.fillRect(0, 0, w, h);

        // ── Subtle decorative blobs (very low opacity) ──────────────────────
        paintBlob(g2, w * 0.82f, h * 0.10f, 340, new Color(0x6366F1), 0.06f);
        paintBlob(g2, w * 0.15f, h * 0.80f, 300, new Color(0x8B5CF6), 0.05f);
        paintBlob(g2, w * 0.50f, h * 0.02f, 220, new Color(0x10B981), 0.03f);

        g2.dispose();
        super.paintComponent(g);
    }

    private void paintBlob(Graphics2D g2, float cx, float cy, float r, Color c, float alpha) {
        RadialGradientPaint p = new RadialGradientPaint(
            cx, cy, r, new float[]{0f, 1f},
            new Color[]{
                new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255)),
                new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)
            }
        );
        g2.setPaint(p);
        g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
    }
}
