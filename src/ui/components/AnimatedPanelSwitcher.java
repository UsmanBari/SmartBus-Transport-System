package ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * AnimatedPanelSwitcher – Fades between child panels over 300 ms.
 * Uses a CardLayout internally with an alpha overlay transition.
 */
public class AnimatedPanelSwitcher extends JPanel {

    private final CardLayout cardLayout;
    private float  alpha        = 1f;
    private String nextCard     = null;
    private Timer  fadeTimer;

    public AnimatedPanelSwitcher() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setOpaque(false);
    }

    /** Add a named panel to the switcher. */
    public void addPanel(String name, JPanel panel) {
        add(panel, name);
    }

    /** Switch to the named panel with a fade transition. */
    public void switchTo(String name) {
        if (name.equals(nextCard)) return;
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();

        // Fade out
        nextCard = name;
        Timer fadeOut = new Timer(12, null);
        fadeOut.addActionListener(e -> {
            alpha = Math.max(0f, alpha - 0.08f);
            repaint();
            if (alpha <= 0f) {
                fadeOut.stop();
                cardLayout.show(AnimatedPanelSwitcher.this, nextCard);
                // Fade in
                Timer fadeIn = new Timer(12, null);
                fadeIn.addActionListener(ie -> {
                    alpha = Math.min(1f, alpha + 0.08f);
                    repaint();
                    if (alpha >= 1f) { fadeIn.stop(); }
                });
                fadeIn.start();
            }
        });
        fadeOut.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paintChildren(g2);
        g2.dispose();
    }
}
