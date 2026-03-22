package ui.screens;

import dao.RouteDAO;
import dao.StudentDAO;
import model.Route;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * ViewRoutesPanel – Scrollable grid of glass-style route cards with capacity display.
 * Each card shows route name, stops, distance, and Bus 1/Bus 2 capacity bars.
 */
public class ViewRoutesPanel extends JPanel {

    private final RouteDAO   routeDao   = new RouteDAO();
    private final StudentDAO studentDao = new StudentDAO();
    private JPanel           cardsGrid;
    private JFrame           parentFrame;

    private static final int BUS_CAPACITY = 30;

    public ViewRoutesPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setOpaque(false);
        setLayout(new BorderLayout());
        build();
        refreshData();
    }

    private void build() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Available Routes");
        title.setFont(AppFonts.SECTION);
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("\u21BB  Refresh");
        refreshBtn.setOpaque(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.setFont(AppFonts.BODY);
        refreshBtn.setForeground(AppColors.ACCENT);
        refreshBtn.addActionListener(e -> refreshData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Cards container
        cardsGrid = new JPanel();
        cardsGrid.setOpaque(false);
        cardsGrid.setLayout(new BoxLayout(cardsGrid, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(cardsGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void refreshData() {
        cardsGrid.removeAll();
        try {
            List<Route> routes = routeDao.getAllRoutes();
            if (routes.isEmpty()) {
                cardsGrid.add(emptyLabel("No routes available yet."));
            } else {
                // Wrap cards in a flow panel for grid layout
                JPanel grid = new JPanel(new GridLayout(0, 2, 20, 20));
                grid.setOpaque(false);
                for (Route r : routes) {
                    grid.add(buildRouteCard(r));
                }
                // If odd number of routes, add filler
                if (routes.size() % 2 != 0) {
                    JPanel filler = new JPanel();
                    filler.setOpaque(false);
                    grid.add(filler);
                }
                cardsGrid.add(grid);
            }
        } catch (SQLException ex) {
            cardsGrid.add(emptyLabel("Database error: " + ex.getMessage()));
        }
        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    private JPanel buildRouteCard(Route route) {
        GlassCard card = new GlassCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(380, 240));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new GridBagLayout());
        inner.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints ic = new GridBagConstraints();
        ic.fill = GridBagConstraints.HORIZONTAL;
        ic.weightx = 1;
        ic.gridx = 0;

        // Route name
        ic.gridy = 0; ic.insets = new Insets(0, 0, 6, 0);
        JLabel nameLabel = new JLabel(route.getRouteName());
        nameLabel.setFont(new Font(AppFonts.SECTION.getFamily(), Font.BOLD, 18));
        nameLabel.setForeground(AppColors.TEXT_PRIMARY);
        inner.add(nameLabel, ic);

        // Stats row
        ic.gridy = 1; ic.insets = new Insets(0, 0, 16, 0);
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statsRow.setOpaque(false);
        statsRow.add(statBadge("\uD83D\uDCCD " + route.getTotalStops() + " Stops"));
        statsRow.add(Box.createHorizontalStrut(16));
        statsRow.add(statBadge("\uD83D\uDCCF " + String.format("%.1f", route.getDistance()) + " km"));
        inner.add(statsRow, ic);

        // Capacity section
        ic.gridy = 2; ic.insets = new Insets(0, 0, 4, 0);
        JLabel capTitle = new JLabel("Seat Capacity");
        capTitle.setFont(AppFonts.LABEL);
        capTitle.setForeground(AppColors.TEXT_SECONDARY);
        inner.add(capTitle, ic);

        // Bus 1
        int bus1Count = 0, bus2Count = 0;
        try {
            bus1Count = studentDao.getBusCount(route.getId(), 1);
            bus2Count = studentDao.getBusCount(route.getId(), 2);
        } catch (SQLException ignored) {}

        ic.gridy = 3; ic.insets = new Insets(4, 0, 4, 0);
        inner.add(buildCapacityBar("Bus 1", bus1Count, BUS_CAPACITY), ic);

        ic.gridy = 4; ic.insets = new Insets(0, 0, 0, 0);
        inner.add(buildCapacityBar("Bus 2", bus2Count, BUS_CAPACITY), ic);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1; gc.weighty = 1;
        card.add(inner, gc);

        return card;
    }

    private JPanel buildCapacityBar(String label, int current, int max) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 28));

        JLabel lbl = new JLabel(label + ":  " + current + " / " + max);
        lbl.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 12));
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(120, 22));
        row.add(lbl, BorderLayout.WEST);

        // Progress bar
        float ratio = Math.min(1f, (float) current / max);
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Track
                g2.setColor(AppColors.BG_SECONDARY);
                g2.fillRoundRect(0, 0, w, h, 8, 8);

                // Fill
                int fillW = (int)(w * ratio);
                if (fillW > 0) {
                    Color fillColor = ratio < 0.7f ? AppColors.SUCCESS
                                    : ratio < 0.9f ? AppColors.WARNING
                                    :                AppColors.ERROR;
                    GradientPaint gp = new GradientPaint(0, 0, fillColor, fillW, 0,
                            fillColor.brighter());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, fillW, h, 8, 8);
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 14));
        row.add(bar, BorderLayout.CENTER);

        return row;
    }

    private JLabel statBadge(String text) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.ACCENT_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        lbl.setOpaque(false);
        lbl.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 12));
        lbl.setForeground(AppColors.ACCENT);
        lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
        return lbl;
    }

    private JLabel emptyLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(AppFonts.BODY);
        lbl.setForeground(AppColors.TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(40, 0, 0, 0));
        return lbl;
    }
}
