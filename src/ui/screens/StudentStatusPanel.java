package ui.screens;

import dao.StudentDAO;
import model.Student;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * StudentStatusPanel – Shows the logged-in student's transport request status.
 * Displays route, status (PENDING / APPROVED / DECLINED), bus assignment,
 * and route capacity information.
 */
public class StudentStatusPanel extends JPanel {

    private final StudentDAO studentDao = new StudentDAO();
    private final String     studentRoll;
    private JPanel           contentPanel;
    private JFrame           parentFrame;

    private static final int BUS_CAPACITY = 30;

    public StudentStatusPanel(JFrame parentFrame, String studentRoll) {
        this.parentFrame = parentFrame;
        this.studentRoll = studentRoll;
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
        JLabel title = new JLabel("My Transport Status");
        title.setFont(AppFonts.SECTION);
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerLeft.add(title);
        JLabel sub = new JLabel("    View your transport request status and assignment");
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
        refreshBtn.addActionListener(e -> refreshData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Content
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        contentPanel.removeAll();

        try {
            Student student = studentDao.getStudentByRoll(studentRoll);

            if (student == null) {
                // No request found
                GlassCard emptyCard = new GlassCard();
                emptyCard.setLayout(new GridBagLayout());
                JPanel emptyInner = new JPanel();
                emptyInner.setOpaque(false);
                emptyInner.setLayout(new BoxLayout(emptyInner, BoxLayout.Y_AXIS));
                emptyInner.setBorder(new EmptyBorder(40, 40, 40, 40));

                JLabel icon = new JLabel("\uD83D\uDCCB");
                icon.setFont(new Font("Dialog", Font.PLAIN, 48));
                icon.setAlignmentX(Component.CENTER_ALIGNMENT);
                emptyInner.add(icon);
                emptyInner.add(Box.createRigidArea(new Dimension(0, 16)));

                JLabel msg = new JLabel("No Transport Request Found");
                msg.setFont(new Font(AppFonts.SECTION.getFamily(), Font.BOLD, 20));
                msg.setForeground(AppColors.TEXT_PRIMARY);
                msg.setAlignmentX(Component.CENTER_ALIGNMENT);
                emptyInner.add(msg);
                emptyInner.add(Box.createRigidArea(new Dimension(0, 8)));

                JLabel hint = new JLabel("Go to Register to submit a transport request.");
                hint.setFont(AppFonts.BODY);
                hint.setForeground(AppColors.TEXT_SECONDARY);
                hint.setAlignmentX(Component.CENTER_ALIGNMENT);
                emptyInner.add(hint);

                emptyCard.add(emptyInner);
                contentPanel.add(emptyCard, BorderLayout.CENTER);
            } else {
                // Build status card
                contentPanel.add(buildStatusCard(student), BorderLayout.CENTER);
            }
        } catch (SQLException ex) {
            GlassCard errCard = new GlassCard();
            errCard.setLayout(new GridBagLayout());
            JLabel msg = new JLabel("Database error: " + ex.getMessage());
            msg.setFont(AppFonts.BODY);
            msg.setForeground(AppColors.ERROR);
            errCard.add(msg);
            contentPanel.add(errCard, BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel buildStatusCard(Student student) {
        GlassCard card = new GlassCard();
        card.setLayout(new BorderLayout());

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(36, 44, 36, 44));

        GridBagConstraints rc = new GridBagConstraints();
        rc.fill = GridBagConstraints.HORIZONTAL;
        rc.weightx = 1;
        rc.gridx = 0;

        // ── Status Badge (large, colored) ──
        rc.gridy = 0; rc.gridwidth = 2; rc.insets = new Insets(0, 0, 28, 0);
        JPanel statusBadge = buildStatusBadge(student.getStatus());
        inner.add(statusBadge, rc);

        // ── Info Grid ──
        rc.gridwidth = 1;
        int row = 1;

        // Row 1: Name + Roll
        rc.gridy = row; rc.gridx = 0; rc.insets = new Insets(0, 0, 20, 40); rc.weightx = 0.5;
        inner.add(infoBlock("Student Name", student.getStudentName(), AppColors.TEXT_PRIMARY), rc);
        rc.gridx = 1; rc.insets = new Insets(0, 0, 20, 0);
        inner.add(infoBlock("Roll Number", student.getStudentRoll(), AppColors.ACCENT), rc);

        // Row 2: Requested Route + Status
        row++;
        rc.gridy = row; rc.gridx = 0; rc.insets = new Insets(0, 0, 20, 40);
        String routeName = student.getSelectedRouteName() != null
                ? student.getSelectedRouteName()
                : "Route #" + student.getSelectedRouteId();
        inner.add(infoBlock("Requested Route", routeName, AppColors.ACCENT), rc);

        rc.gridx = 1; rc.insets = new Insets(0, 0, 20, 0);
        Color statusColor = getStatusColor(student.getStatus());
        inner.add(infoBlock("Request Status", student.getStatus(), statusColor), rc);

        // Row 3: Conditional info based on status
        row++;
        if ("APPROVED".equals(student.getStatus())) {
            rc.gridy = row; rc.gridx = 0; rc.insets = new Insets(0, 0, 20, 40);
            inner.add(infoBlock("Assigned Bus", "Bus " + student.getBusNumber(), AppColors.SUCCESS), rc);

            // Show capacity for assigned bus
            rc.gridx = 1; rc.insets = new Insets(0, 0, 20, 0);
            int busCount = 0;
            try { busCount = studentDao.getBusCount(student.getAssignedRouteId(), student.getBusNumber()); }
            catch (SQLException ignored) {}
            inner.add(infoBlock("Bus Occupancy", busCount + " / " + BUS_CAPACITY + " seats", AppColors.TEXT_PRIMARY), rc);

            // Capacity bars
            row++;
            rc.gridy = row; rc.gridx = 0; rc.gridwidth = 2; rc.insets = new Insets(12, 0, 0, 0);
            inner.add(buildCapacitySection(student.getAssignedRouteId()), rc);

        } else if ("DECLINED".equals(student.getStatus())) {
            rc.gridy = row; rc.gridx = 0; rc.gridwidth = 2; rc.insets = new Insets(0, 0, 20, 0);
            JPanel declinedNote = new JPanel(new BorderLayout());
            declinedNote.setOpaque(false);
            JLabel note = new JLabel("\u26A0  Your request was declined by the administrator. You may register again with a different route.");
            note.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 14));
            note.setForeground(AppColors.ERROR);
            declinedNote.add(note);
            inner.add(declinedNote, rc);
        } else {
            // PENDING
            rc.gridy = row; rc.gridx = 0; rc.gridwidth = 2; rc.insets = new Insets(0, 0, 20, 0);
            JPanel pendingNote = new JPanel(new BorderLayout());
            pendingNote.setOpaque(false);
            JLabel note = new JLabel("\u23F3  Your request is being reviewed by the administrator. Please check back later.");
            note.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 14));
            note.setForeground(AppColors.WARNING);
            pendingNote.add(note);
            inner.add(pendingNote, rc);
        }

        // Spacer
        row++;
        rc.gridy = row; rc.gridx = 0; rc.gridwidth = 2;
        rc.weighty = 1; rc.fill = GridBagConstraints.BOTH;
        inner.add(Box.createGlue(), rc);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatusBadge(String status) {
        Color bgColor;
        String icon, label;

        switch (status) {
            case "APPROVED":
                bgColor = new Color(0x059669, false);
                icon = "\u2705"; label = "APPROVED";
                break;
            case "DECLINED":
                bgColor = new Color(0xDC2626, false);
                icon = "\u274C"; label = "DECLINED";
                break;
            default:
                bgColor = new Color(0xD97706, false);
                icon = "\u23F3"; label = "PENDING";
                break;
        }

        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 12));
        badge.setPreferredSize(new Dimension(0, 56));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Dialog", Font.PLAIN, 24));
        badge.add(iconLbl);

        JLabel statusLbl = new JLabel("Transport Status:  " + label);
        statusLbl.setFont(new Font(AppFonts.SECTION.getFamily(), Font.BOLD, 18));
        statusLbl.setForeground(bgColor);
        badge.add(statusLbl);

        return badge;
    }

    private JPanel buildCapacitySection(int routeId) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JLabel capTitle = new JLabel("Route Capacity");
        capTitle.setFont(new Font(AppFonts.LABEL.getFamily(), Font.BOLD, 13));
        capTitle.setForeground(AppColors.TEXT_SECONDARY);
        capTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(capTitle);
        section.add(Box.createRigidArea(new Dimension(0, 8)));

        int bus1 = 0, bus2 = 0;
        try {
            bus1 = studentDao.getBusCount(routeId, 1);
            bus2 = studentDao.getBusCount(routeId, 2);
        } catch (SQLException ignored) {}

        section.add(buildCapacityBar("Bus 1", bus1, BUS_CAPACITY));
        section.add(Box.createRigidArea(new Dimension(0, 6)));
        section.add(buildCapacityBar("Bus 2", bus2, BUS_CAPACITY));

        return section;
    }

    private JPanel buildCapacityBar(String label, int current, int max) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 28));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean isFull = current >= max;
        String text = label + ":  " + current + " / " + max + (isFull ? "  (FULL)" : "");
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 12));
        lbl.setForeground(isFull ? AppColors.ERROR : AppColors.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(180, 22));
        row.add(lbl, BorderLayout.WEST);

        float ratio = Math.min(1f, (float) current / max);
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(AppColors.BG_SECONDARY);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                int fillW = (int)(w * ratio);
                if (fillW > 0) {
                    Color fillColor = ratio < 0.7f ? AppColors.SUCCESS
                                    : ratio < 0.9f ? AppColors.WARNING
                                    :                AppColors.ERROR;
                    GradientPaint gp = new GradientPaint(0, 0, fillColor, fillW, 0, fillColor.brighter());
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

    private JPanel infoBlock(String label, String value, Color valueColor) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font(AppFonts.LABEL.getFamily(), Font.PLAIN, 12));
        lblKey.setForeground(AppColors.TEXT_MUTED);
        lblKey.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(lblKey);

        block.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 18));
        lblVal.setForeground(valueColor);
        lblVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(lblVal);

        return block;
    }

    private Color getStatusColor(String status) {
        switch (status) {
            case "APPROVED": return AppColors.SUCCESS;
            case "DECLINED": return AppColors.ERROR;
            case "PENDING":  return AppColors.WARNING;
            default:         return AppColors.TEXT_PRIMARY;
        }
    }
}
