package ui.screens;

import dao.RouteDAO;
import dao.StudentDAO;
import model.Route;
import model.Student;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

/**
 * AdminViewRoutesPanel – Shows all routes with their students in expandable cards.
 * For each route: name, stops, distance, capacity, and a table of assigned students.
 */
public class AdminViewRoutesPanel extends JPanel {

    private final RouteDAO   routeDao   = new RouteDAO();
    private final StudentDAO studentDao = new StudentDAO();
    private JPanel           cardsContainer;
    private JFrame           parentFrame;

    private static final int BUS_CAPACITY = 30;

    public AdminViewRoutesPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setOpaque(false);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("All Routes & Students");
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
        refreshBtn.addActionListener(e -> loadData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Cards container
        cardsContainer = new JPanel();
        cardsContainer.setOpaque(false);
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void loadData() {
        cardsContainer.removeAll();
        try {
            List<Route> routes = routeDao.getAllRoutes();
            if (routes.isEmpty()) {
                cardsContainer.add(emptyLabel("No routes found."));
            } else {
                for (Route r : routes) {
                    cardsContainer.add(buildRouteSection(r));
                    cardsContainer.add(Box.createRigidArea(new Dimension(0, 20)));
                }
            }
        } catch (SQLException ex) {
            cardsContainer.add(emptyLabel("Database error: " + ex.getMessage()));
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel buildRouteSection(Route route) {
        GlassCard card = new GlassCard();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Route header
        JPanel routeHeader = new JPanel(new BorderLayout());
        routeHeader.setOpaque(false);
        routeHeader.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel nameLabel = new JLabel(route.getRouteName());
        nameLabel.setFont(new Font(AppFonts.SECTION.getFamily(), Font.BOLD, 18));
        nameLabel.setForeground(AppColors.TEXT_PRIMARY);
        routeHeader.add(nameLabel, BorderLayout.WEST);

        // Capacity badges
        int bus1 = 0, bus2 = 0;
        try {
            bus1 = studentDao.getBusCount(route.getId(), 1);
            bus2 = studentDao.getBusCount(route.getId(), 2);
        } catch (SQLException ignored) {}

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badges.setOpaque(false);
        badges.add(statBadge("\uD83D\uDCCD " + route.getTotalStops() + " Stops"));
        badges.add(statBadge("\uD83D\uDCCF " + String.format("%.1f", route.getDistance()) + " km"));
        badges.add(statBadge("Bus1: " + bus1 + "/" + BUS_CAPACITY));
        badges.add(statBadge("Bus2: " + bus2 + "/" + BUS_CAPACITY));
        routeHeader.add(badges, BorderLayout.EAST);

        inner.add(routeHeader, BorderLayout.NORTH);

        // Students table for this route
        List<Student> students = null;
        try {
            students = studentDao.getStudentsByRoute(route.getId());
        } catch (SQLException ignored) {}

        if (students == null || students.isEmpty()) {
            JLabel noStudents = new JLabel("No students assigned to this route yet.");
            noStudents.setFont(AppFonts.BODY);
            noStudents.setForeground(AppColors.TEXT_MUTED);
            noStudents.setBorder(new EmptyBorder(10, 0, 0, 0));
            inner.add(noStudents, BorderLayout.CENTER);
        } else {
            String[] cols = {"Student Name", "Roll Number", "Bus #", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Student s : students) {
                model.addRow(new Object[]{
                    s.getStudentName(),
                    s.getStudentRoll(),
                    "Bus " + s.getBusNumber(),
                    s.getStatus()
                });
            }
            JTable table = new JTable(model);
            styleSmallTable(table);
            JScrollPane tScroll = new JScrollPane(table);
            tScroll.setOpaque(false);
            tScroll.getViewport().setOpaque(false);
            tScroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
            tScroll.setPreferredSize(new Dimension(0, Math.min(students.size() * 40 + 44, 200)));
            inner.add(tScroll, BorderLayout.CENTER);
        }

        card.add(inner);
        return card;
    }

    private void styleSmallTable(JTable table) {
        table.setOpaque(false);
        table.setBackground(AppColors.BG_CARD);
        table.setForeground(AppColors.TEXT_PRIMARY);
        table.setFont(AppFonts.BODY);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(AppColors.BORDER);
        table.setSelectionBackground(AppColors.ACCENT_LIGHT);
        table.setSelectionForeground(AppColors.ACCENT);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(AppColors.BG_SECONDARY);
        header.setForeground(AppColors.TEXT_SECONDARY);
        header.setFont(new Font(AppFonts.LABEL.getFamily(), Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppColors.ACCENT));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setBackground(AppColors.BG_SECONDARY);
                lbl.setForeground(AppColors.TEXT_SECONDARY);
                lbl.setFont(new Font(AppFonts.LABEL.getFamily(), Font.BOLD, 12));
                lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
                lbl.setFont(AppFonts.BODY);
                if (sel) {
                    lbl.setBackground(AppColors.ACCENT_LIGHT);
                    lbl.setForeground(AppColors.ACCENT);
                } else {
                    lbl.setBackground(row % 2 == 0 ? AppColors.BG_CARD : AppColors.BG_FIELD);
                    lbl.setForeground(AppColors.TEXT_PRIMARY);
                }
                if (col == 3) {
                    String s = v != null ? v.toString() : "";
                    if ("APPROVED".equals(s)) lbl.setForeground(AppColors.SUCCESS);
                    else if ("PENDING".equals(s)) lbl.setForeground(AppColors.WARNING);
                }
                return lbl;
            }
        });
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
        lbl.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 11));
        lbl.setForeground(AppColors.ACCENT);
        lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
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
