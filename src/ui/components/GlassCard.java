package ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * GlassCard – Clean white card with layered drop shadow.
 * Modern SaaS card inspired by Stripe/Linear.
 */
public class GlassCard extends JPanel {

    private static final int ARC = 16;
    private final Color fillColor;
    private final Color borderColor;

    public GlassCard() {
        this(AppColors.BG_CARD, AppColors.BORDER);
    }

    public GlassCard(Color fill, Color border) {
        this.fillColor   = fill;
        this.borderColor = border;
        setOpaque(false);
        setLayout(new GridBagLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // ── Layered soft shadow ─────────────────────────────────────────────
        int[] alphas  = {4, 6, 8, 10, 8, 6, 4, 3};
        int[] offsets = {1, 2, 3, 5, 7, 10, 13, 16};
        for (int i = 0; i < alphas.length; i++) {
            g2.setColor(new Color(99, 102, 241, alphas[i]));
            g2.fillRoundRect(-offsets[i] / 2, offsets[i], w + offsets[i], h, ARC + 4, ARC + 4);
        }

        // ── White card fill ─────────────────────────────────────────────────
        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, w, h, ARC, ARC);

        // ── Hairline border ─────────────────────────────────────────────────
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public boolean isOpaque() { return false; }
}
