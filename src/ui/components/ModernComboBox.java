package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * ModernComboBox – Dark-themed, rounded combo box with neon focus outline
 * and a custom renderer.
 */
public class ModernComboBox<T> extends JComboBox<T> {

    private boolean focused = false;

    public ModernComboBox() {
        super();
        configure();
    }

    public ModernComboBox(T[] items) {
        super(items);
        configure();
    }

    private void configure() {
        setOpaque(false);
        setBackground(AppColors.BG_FIELD);
        setForeground(AppColors.TEXT_PRIMARY);
        setFont(AppFonts.FIELD);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setRenderer(new DarkRenderer());

        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▾") {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(AppColors.BG_FIELD);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(AppColors.ACCENT);
                        g2.setFont(new Font("Dialog", Font.PLAIN, 13));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString("▾", (getWidth() - fm.stringWidth("▾")) / 2,
                                (getHeight() + fm.getAscent()) / 2 - 2);
                        g2.dispose();
                    }
                };
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setPreferredSize(new Dimension(28, 28));
                return btn;
            }

            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                comboBox.setBackground(AppColors.BG_FIELD);
            }
        });

        // Track focus for outline
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { focused = true;  repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e)   { focused = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background
        g2.setColor(AppColors.BG_FIELD);
        g2.fillRoundRect(0, 0, w, h, 12, 12);

        // Focus outline
        if (focused) {
            g2.setColor(new Color(99, 102, 241, 200));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 12, 12);
        } else {
            g2.setColor(AppColors.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 12, 12);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    @Override public boolean isOpaque() { return false; }

    // ── Inner renderer ──────────────────────────────────────────────────────

    private static class DarkRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            lbl.setFont(AppFonts.FIELD);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(8, 14, 8, 14));
            if (isSelected) {
                lbl.setBackground(AppColors.ACCENT_LIGHT);
                lbl.setForeground(AppColors.ACCENT);
            } else {
                lbl.setBackground(Color.WHITE);
                lbl.setForeground(AppColors.TEXT_PRIMARY);
            }
            return lbl;
        }
    }
}
