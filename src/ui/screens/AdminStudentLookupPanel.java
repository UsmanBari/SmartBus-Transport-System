package ui.screens;

import dao.FeeChallanDAO;
import dao.StudentDAO;
import model.FeeChallan;
import model.Student;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * AdminStudentLookupPanel – Full-width search panel.
 * Admin enters a roll number to look up route assignment, bus, and status.
 */
public class AdminStudentLookupPanel extends JPanel {

    private final StudentDAO    studentDao = new StudentDAO();
    private final FeeChallanDAO challanDao = new FeeChallanDAO();
    private ModernTextField  tfRoll;
    private JPanel           resultPanel;
    private JFrame           parentFrame;

    public AdminStudentLookupPanel(JFrame parentFrame) {
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

        JLabel title = new JLabel("Student Lookup");
        title.setFont(AppFonts.SECTION);
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("  Enter roll number to find student route assignment");
        sub.setFont(AppFonts.BODY);
        sub.setForeground(AppColors.TEXT_SECONDARY);
        headerPanel.add(sub, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Main content – fills entire available width
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);

        // Search bar (top)
        GlassCard searchCard = new GlassCard();
        searchCard.setLayout(new BorderLayout());
        searchCard.setPreferredSize(new Dimension(0, 90));

        JPanel searchInner = new JPanel(new BorderLayout(12, 0));
        searchInner.setOpaque(false);
        searchInner.setBorder(new EmptyBorder(20, 28, 20, 28));

        JLabel rollLabel = new JLabel("Roll Number:");
        rollLabel.setFont(AppFonts.LABEL);
        rollLabel.setForeground(AppColors.TEXT_SECONDARY);
        rollLabel.setPreferredSize(new Dimension(110, 44));
        searchInner.add(rollLabel, BorderLayout.WEST);

        tfRoll = new ModernTextField("e.g. i230680");
        searchInner.add(tfRoll, BorderLayout.CENTER);

        NeonButton btnSearch = new NeonButton("  Search  ");
        btnSearch.setPreferredSize(new Dimension(120, 44));
        btnSearch.addActionListener(e -> handleSearch());
        searchInner.add(btnSearch, BorderLayout.EAST);

        tfRoll.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleSearch();
            }
        });

        searchCard.add(searchInner, BorderLayout.CENTER);
        mainContent.add(searchCard, BorderLayout.NORTH);

        // Result area (expands to fill remaining space)
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setOpaque(false);
        resultPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel hint = new JLabel("Enter a roll number above and click Search to find student details",
                SwingConstants.CENTER);
        hint.setFont(AppFonts.BODY);
        hint.setForeground(AppColors.TEXT_MUTED);
        resultPanel.add(hint, BorderLayout.CENTER);

        mainContent.add(resultPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private void handleSearch() {
        String roll = tfRoll.getText().trim();
        if (roll.isEmpty()) {
            showToast("Please enter a roll number.", ToastNotification.Type.ERROR);
            return;
        }

        resultPanel.removeAll();

        try {
            Student student = studentDao.getStudentByRoll(roll);
            if (student == null) {
                // Not found card
                GlassCard notFoundCard = new GlassCard();
                notFoundCard.setLayout(new GridBagLayout());
                JLabel msg = new JLabel("\u274C  No transport registration found for: " + roll);
                msg.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 16));
                msg.setForeground(AppColors.ERROR);
                notFoundCard.add(msg);
                resultPanel.add(notFoundCard, BorderLayout.CENTER);
            } else {
                // Found – build result card
                GlassCard resultCard = new GlassCard();
                resultCard.setLayout(new BorderLayout());

                JPanel inner = new JPanel(new GridBagLayout());
                inner.setOpaque(false);
                inner.setBorder(new EmptyBorder(32, 36, 32, 36));

                GridBagConstraints rc = new GridBagConstraints();
                rc.fill = GridBagConstraints.HORIZONTAL;
                rc.weightx = 1;
                rc.gridx = 0;

                // Name header
                rc.gridy = 0; rc.insets = new Insets(0, 0, 20, 0);
                rc.gridwidth = 2;
                JLabel nameLabel = new JLabel("\u2705  " + student.getStudentName());
                nameLabel.setFont(new Font(AppFonts.SECTION.getFamily(), Font.BOLD, 24));
                nameLabel.setForeground(AppColors.SUCCESS);
                inner.add(nameLabel, rc);

                // Info in 2 columns
                rc.gridwidth = 1;
                int row = 1;

                rc.gridy = row; rc.gridx = 0; rc.insets = new Insets(0, 0, 12, 40);
                rc.weightx = 0.5;
                inner.add(infoBlock("Roll Number", student.getStudentRoll(), AppColors.TEXT_PRIMARY), rc);
                rc.gridx = 1; rc.insets = new Insets(0, 0, 12, 0);
                String routeName = student.getSelectedRouteName() != null
                    ? student.getSelectedRouteName()
                    : "Route #" + student.getSelectedRouteId();
                inner.add(infoBlock("Requested Route", routeName, AppColors.ACCENT), rc);

                row++;
                rc.gridy = row; rc.gridx = 0; rc.insets = new Insets(0, 0, 12, 40);
                Color statusColor = "APPROVED".equals(student.getStatus()) ? AppColors.SUCCESS
                                  : "PENDING".equals(student.getStatus()) ? AppColors.WARNING
                                  : "DECLINED".equals(student.getStatus()) ? AppColors.ERROR
                                  : AppColors.TEXT_PRIMARY;
                inner.add(infoBlock("Status", student.getStatus(), statusColor), rc);

                if ("APPROVED".equals(student.getStatus())) {
                    rc.gridx = 1; rc.insets = new Insets(0, 0, 12, 0);
                    inner.add(infoBlock("Assigned Bus", "Bus " + student.getBusNumber(), AppColors.SUCCESS), rc);

                    row++;
                    rc.gridy = row; rc.gridx = 0; rc.insets = new Insets(0, 0, 12, 40);
                    inner.add(infoBlock("Assigned Route ID", String.valueOf(student.getAssignedRouteId()), AppColors.TEXT_PRIMARY), rc);
                } else if ("DECLINED".equals(student.getStatus())) {
                    rc.gridx = 1; rc.insets = new Insets(0, 0, 12, 0);
                    inner.add(infoBlock("Note", "Request was declined by admin", AppColors.ERROR), rc);
                } else {
                    rc.gridx = 1; rc.insets = new Insets(0, 0, 12, 0);
                    inner.add(infoBlock("Note", "Awaiting admin review", AppColors.WARNING), rc);
                }

                // Fee Status from challan
                row++;
                rc.gridy = row; rc.gridx = 0; rc.gridwidth = 2;
                rc.insets = new Insets(12, 0, 12, 0);
                try {
                    FeeChallan challan = challanDao.getChallanByStudentId(student.getStudentId());
                    if (challan == null) {
                        inner.add(infoBlock("Fee Status", "No Challan Issued", AppColors.TEXT_MUTED), rc);
                    } else {
                        switch (challan.getStatus()) {
                            case "UNPAID":
                                inner.add(buildFeeBadgeBlock("UNPAID", new Color(0xD97706), null), rc);
                                break;
                            case "PROOF_SUBMITTED":
                                String proofDate = challan.getProofSubmittedAt() != null ?
                                    new SimpleDateFormat("dd MMM yyyy").format(challan.getProofSubmittedAt()) : "";
                                inner.add(buildFeeBadgeBlock("PROOF SUBMITTED", new Color(0x1E40AF),
                                    "Submitted on: " + proofDate), rc);
                                break;
                            case "PAID":
                                String paidDate = challan.getPaidAt() != null ?
                                    new SimpleDateFormat("dd MMM yyyy").format(challan.getPaidAt()) : "";
                                inner.add(buildFeeBadgeBlock("PAID", new Color(0x059669),
                                    "Confirmed on: " + paidDate), rc);
                                break;
                            default:
                                inner.add(infoBlock("Fee Status", challan.getStatus(), AppColors.TEXT_MUTED), rc);
                        }
                    }
                } catch (SQLException ignored) {
                    inner.add(infoBlock("Fee Status", "Unable to load", AppColors.TEXT_MUTED), rc);
                }

                // Spacer
                row++;
                rc.gridy = row; rc.gridx = 0; rc.gridwidth = 2;
                rc.weighty = 1; rc.fill = GridBagConstraints.BOTH;
                inner.add(Box.createGlue(), rc);

                resultCard.add(inner, BorderLayout.CENTER);
                resultPanel.add(resultCard, BorderLayout.CENTER);
            }
        } catch (SQLException ex) {
            GlassCard errCard = new GlassCard();
            errCard.setLayout(new GridBagLayout());
            JLabel msg = new JLabel("Database error: " + ex.getMessage());
            msg.setFont(AppFonts.BODY);
            msg.setForeground(AppColors.ERROR);
            errCard.add(msg);
            resultPanel.add(errCard, BorderLayout.CENTER);
        }

        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private JPanel infoBlock(String label, String value, Color valueColor) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font(AppFonts.LABEL.getFamily(), Font.PLAIN, 13));
        lblKey.setForeground(AppColors.TEXT_MUTED);
        lblKey.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(lblKey);

        block.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 18));
        lblVal.setForeground(valueColor);
        lblVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(lblVal);

        return block;
    }

    private JPanel buildFeeBadgeBlock(String statusText, Color badgeColor, String subText) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

        JLabel lblKey = new JLabel("Fee Status");
        lblKey.setFont(new Font(AppFonts.LABEL.getFamily(), Font.PLAIN, 13));
        lblKey.setForeground(AppColors.TEXT_MUTED);
        lblKey.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(lblKey);
        block.add(Box.createRigidArea(new Dimension(0, 6)));

        // Badge
        final Color fColor = badgeColor;
        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
        badge.setPreferredSize(new Dimension(180, 28));
        badge.setMaximumSize(new Dimension(180, 28));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel(statusText);
        badgeLbl.setFont(new Font(AppFonts.BUTTON.getFamily(), Font.BOLD, 12));
        badgeLbl.setForeground(Color.WHITE);
        badge.add(badgeLbl);
        block.add(badge);

        if (subText != null && !subText.isEmpty()) {
            block.add(Box.createRigidArea(new Dimension(0, 4)));
            JLabel subLbl = new JLabel(subText);
            subLbl.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 12));
            subLbl.setForeground(badgeColor);
            subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            block.add(subLbl);
        }

        return block;
    }

    private void showToast(String msg, ToastNotification.Type type) {
        if (parentFrame == null) return;
        new ToastNotification(parentFrame, msg, type).show(0);
    }
}
