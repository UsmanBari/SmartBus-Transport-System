package ui.screens;

import dao.StudentDAO;
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
 * AdminApprovalPanel – Modern styled table for reviewing, approving,
 * and declining student transport requests with bus assignment logic.
 */
public class AdminApprovalPanel extends JPanel {

    private final StudentDAO   studentDao = new StudentDAO();
    private DefaultTableModel  tableModel;
    private JTable             table;
    private JFrame             parentFrame;
    private List<Student>      pendingStudents;

    private static final int BUS_CAPACITY = 30;

    public AdminApprovalPanel(JFrame parentFrame) {
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

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerLeft.setOpaque(false);
        JLabel title = new JLabel("Student Requests");
        title.setFont(AppFonts.SECTION);
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerLeft.add(title);
        JLabel sub = new JLabel("    Review, approve, or decline transport registrations");
        sub.setFont(AppFonts.BODY);
        sub.setForeground(AppColors.TEXT_SECONDARY);
        headerLeft.add(sub);
        headerPanel.add(headerLeft, BorderLayout.WEST);

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

        // Table
        String[] columns = {"Student Name", "Roll Number", "Selected Route", "Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 4; // only action column
            }
        };

        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void styleTable() {
        table.setOpaque(false);
        table.setBackground(AppColors.BG_CARD);
        table.setForeground(AppColors.TEXT_PRIMARY);
        table.setFont(AppFonts.BODY);
        table.setRowHeight(52);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(AppColors.ACCENT_LIGHT);
        table.setSelectionForeground(AppColors.ACCENT);
        table.setGridColor(AppColors.BORDER);
        table.setShowHorizontalLines(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setBackground(AppColors.BG_SECONDARY);
        header.setForeground(AppColors.TEXT_SECONDARY);
        header.setFont(new Font(AppFonts.LABEL.getFamily(), Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppColors.ACCENT));

        // Custom header renderer
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value,
                        isSelected, hasFocus, row, col);
                lbl.setBackground(AppColors.BG_SECONDARY);
                lbl.setForeground(AppColors.TEXT_SECONDARY);
                lbl.setFont(new Font(AppFonts.LABEL.getFamily(), Font.BOLD, 13));
                lbl.setBorder(new EmptyBorder(0, 16, 0, 16));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        // Cell renderer with hover
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value,
                        isSelected, hasFocus, row, col);
                lbl.setBorder(new EmptyBorder(0, 16, 0, 16));
                lbl.setFont(AppFonts.BODY);

                if (isSelected) {
                    lbl.setBackground(AppColors.ACCENT_LIGHT);
                    lbl.setForeground(AppColors.ACCENT);
                } else if (row == hoveredRow) {
                    lbl.setBackground(AppColors.BG_HOVER);
                    lbl.setForeground(AppColors.TEXT_PRIMARY);
                } else {
                    lbl.setBackground(row % 2 == 0 ? AppColors.BG_CARD : AppColors.BG_FIELD);
                    lbl.setForeground(AppColors.TEXT_PRIMARY);
                }

                // Status column coloring
                if (col == 3) {
                    String status = value != null ? value.toString() : "";
                    if ("PENDING".equals(status)) {
                        lbl.setForeground(AppColors.WARNING);
                    } else if ("APPROVED".equals(status)) {
                        lbl.setForeground(AppColors.SUCCESS);
                    }
                }
                return lbl;
            }
        });

        // Action column – dual button renderer & editor
        table.getColumnModel().getColumn(4).setCellRenderer(new DualButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new DualButtonEditor());

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(220);

        // Hover tracking
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });
    }

    private int hoveredRow = -1;

    // ── Data Loading ────────────────────────────────────────────────────────

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            pendingStudents = studentDao.getAllPendingStudents();
            for (Student s : pendingStudents) {
                tableModel.addRow(new Object[]{
                    s.getStudentName(),
                    s.getStudentRoll(),
                    s.getSelectedRouteName() != null ? s.getSelectedRouteName() : "Route #" + s.getSelectedRouteId(),
                    s.getStatus(),
                    "actions"
                });
            }
            if (pendingStudents.isEmpty()) {
                // Show empty message
                tableModel.addRow(new Object[]{"No pending requests", "", "", "", ""});
            }
        } catch (SQLException ex) {
            tableModel.addRow(new Object[]{"Error: " + ex.getMessage(), "", "", "", ""});
        }
    }

    // ── Bus Assignment Logic ────────────────────────────────────────────────

    private void handleApproval(int tableRow) {
        if (pendingStudents == null || tableRow < 0 || tableRow >= pendingStudents.size()) {
            return;
        }

        Student student = pendingStudents.get(tableRow);
        int routeId = student.getSelectedRouteId();

        try {
            // Check Bus 1
            int bus1Count = studentDao.getBusCount(routeId, 1);
            if (bus1Count < BUS_CAPACITY) {
                // Assign to Bus 1
                boolean ok = studentDao.assignStudentToBus(student.getStudentId(), routeId, 1);
                if (ok) {
                    showToast(student.getStudentName() + " assigned to Bus 1.", ToastNotification.Type.SUCCESS);
                    loadData();
                    return;
                }
            }

            // Check Bus 2
            int bus2Count = studentDao.getBusCount(routeId, 2);
            if (bus2Count < BUS_CAPACITY) {
                boolean ok = studentDao.assignStudentToBus(student.getStudentId(), routeId, 2);
                if (ok) {
                    showToast(student.getStudentName() + " assigned to Bus 2.", ToastNotification.Type.SUCCESS);
                    loadData();
                    return;
                }
            }

            // Both full
            showToast("Both buses are FULL for this route! Request cannot be approved.", ToastNotification.Type.ERROR);

        } catch (SQLException ex) {
            showToast("Database error: " + ex.getMessage(), ToastNotification.Type.ERROR);
        }
    }

    // ── Decline Logic ───────────────────────────────────────────────────────

    private void handleDecline(int tableRow) {
        if (pendingStudents == null || tableRow < 0 || tableRow >= pendingStudents.size()) {
            return;
        }

        Student student = pendingStudents.get(tableRow);

        int confirm = JOptionPane.showConfirmDialog(parentFrame,
                "Decline transport request for " + student.getStudentName() + " (" + student.getStudentRoll() + ")?",
                "Confirm Decline", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = studentDao.declineStudent(student.getStudentId());
            if (ok) {
                showToast(student.getStudentName() + "'s request has been declined.", ToastNotification.Type.ERROR);
                loadData();
            }
        } catch (SQLException ex) {
            showToast("Database error: " + ex.getMessage(), ToastNotification.Type.ERROR);
        }
    }

    private void showToast(String msg, ToastNotification.Type type) {
        if (parentFrame == null) return;
        new ToastNotification(parentFrame, msg, type).show(0);
    }

    // ── Dual Button Renderer ────────────────────────────────────────────────

    private class DualButtonRenderer extends JPanel implements TableCellRenderer {

        DualButtonRenderer() {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            removeAll();
            String text = value != null ? value.toString() : "";

            if (text.isEmpty() || "No pending requests".equals(tableModel.getValueAt(row, 0))) {
                return this;
            }

            // Approve button
            JPanel approveBtn = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    GradientPaint gp = new GradientPaint(0, 0, AppColors.SUCCESS, w, 0, new Color(0x059669));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(AppFonts.BUTTON.getFamily(), Font.BOLD, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    String txt = "\u2713 Approve";
                    g2.drawString(txt, (w - fm.stringWidth(txt)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            approveBtn.setOpaque(false);
            approveBtn.setPreferredSize(new Dimension(90, 30));

            // Decline button
            JPanel declineBtn = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    GradientPaint gp = new GradientPaint(0, 0, AppColors.ERROR, w, 0, new Color(0xB91C1C));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(AppFonts.BUTTON.getFamily(), Font.BOLD, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    String txt = "\u2717 Decline";
                    g2.drawString(txt, (w - fm.stringWidth(txt)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            declineBtn.setOpaque(false);
            declineBtn.setPreferredSize(new Dimension(90, 30));

            add(approveBtn);
            add(declineBtn);
            return this;
        }
    }

    // ── Dual Button Editor ──────────────────────────────────────────────────

    private class DualButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton approveButton;
        private final JButton declineButton;
        private int currentRow;

        DualButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
            panel.setOpaque(false);

            approveButton = new JButton("\u2713 Approve");
            approveButton.setFont(new Font(AppFonts.BUTTON.getFamily(), Font.BOLD, 11));
            approveButton.setBackground(AppColors.SUCCESS);
            approveButton.setForeground(Color.WHITE);
            approveButton.setOpaque(true);
            approveButton.setBorderPainted(false);
            approveButton.setFocusPainted(false);
            approveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            approveButton.setPreferredSize(new Dimension(90, 30));
            approveButton.addActionListener(e -> {
                fireEditingStopped();
                handleApproval(currentRow);
            });

            declineButton = new JButton("\u2717 Decline");
            declineButton.setFont(new Font(AppFonts.BUTTON.getFamily(), Font.BOLD, 11));
            declineButton.setBackground(AppColors.ERROR);
            declineButton.setForeground(Color.WHITE);
            declineButton.setOpaque(true);
            declineButton.setBorderPainted(false);
            declineButton.setFocusPainted(false);
            declineButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            declineButton.setPreferredSize(new Dimension(90, 30));
            declineButton.addActionListener(e -> {
                fireEditingStopped();
                handleDecline(currentRow);
            });

            panel.add(approveButton);
            panel.add(declineButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object value,
                boolean isSelected, int row, int col) {
            currentRow = row;
            String text = value != null ? value.toString() : "";

            if (text.isEmpty() || pendingStudents == null || row >= pendingStudents.size()) {
                JPanel empty = new JPanel();
                empty.setOpaque(false);
                return empty;
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "actions";
        }
    }
}
