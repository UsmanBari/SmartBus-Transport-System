package ui.screens;

import dao.FeeChallanDAO;
import model.FeeChallan;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * FeeManagementPanel – Admin panel for reviewing/confirming student payments.
 * Shows all challans in a filterable table with receipt viewer and summary.
 */
public class FeeManagementPanel extends JPanel {

    private final FeeChallanDAO challanDao = new FeeChallanDAO();
    private DefaultTableModel  tableModel;
    private JTable             table;
    private JFrame             parentFrame;
    private List<FeeChallan>   currentChallans;

    private JLabel lblTotalCollected;
    private JLabel lblTotalPending;

    // Filter state: "ALL", "PROOF_SUBMITTED", "PAID"
    private String currentFilter = "ALL";
    private JButton btnAll, btnProof, btnPaid;

    private int hoveredRow = -1;

    public FeeManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setOpaque(false);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        // ── Header ──────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel headerLeft = new JPanel();
        headerLeft.setOpaque(false);
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Fee Management");
        title.setFont(AppFonts.SECTION);
        title.setForeground(AppColors.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerLeft.add(title);
        JLabel sub = new JLabel("Review and confirm student payment proofs");
        sub.setFont(AppFonts.BODY);
        sub.setForeground(AppColors.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
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

        // ── Center: filter bar + table ──────────────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        centerPanel.add(buildFilterBar(), BorderLayout.NORTH);

        // Table
        String[] columns = {"Challan ID", "Student Roll", "Student Name", "Route Name",
                            "Amount Due", "Status", "Issued Date", "Proof Submitted", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 8;
            }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // ── Summary row ─────────────────────────────────────────────────
        add(buildSummaryRow(), BorderLayout.SOUTH);
    }

    // ── Filter Bar ──────────────────────────────────────────────────────

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        btnAll   = createFilterButton("All",             "ALL");
        btnProof = createFilterButton("Proof Submitted", "PROOF_SUBMITTED");
        btnPaid  = createFilterButton("Paid",            "PAID");

        bar.add(btnAll);
        bar.add(btnProof);
        bar.add(btnPaid);

        updateFilterButtons();
        return bar;
    }

    private JButton createFilterButton(String text, String filterKey) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(AppFonts.BUTTON.getFamily(), Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 32));
        btn.addActionListener(e -> {
            currentFilter = filterKey;
            updateFilterButtons();
            loadData();
        });
        return btn;
    }

    private void updateFilterButtons() {
        styleFilterBtn(btnAll,   "ALL");
        styleFilterBtn(btnProof, "PROOF_SUBMITTED");
        styleFilterBtn(btnPaid,  "PAID");
    }

    private void styleFilterBtn(JButton btn, String key) {
        if (currentFilter.equals(key)) {
            btn.setBackground(AppColors.ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
        } else {
            btn.setBackground(AppColors.BG_SECONDARY);
            btn.setForeground(AppColors.TEXT_SECONDARY);
            btn.setOpaque(true);
        }
    }

    // ── Summary Row ─────────────────────────────────────────────────────

    private JPanel buildSummaryRow() {
        JPanel row = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(AppColors.BORDER);
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 40, 14));
        row.setPreferredSize(new Dimension(0, 56));
        row.setBorder(new EmptyBorder(4, 8, 4, 8));

        lblTotalCollected = new JLabel("Total Collected: PKR 0.00");
        lblTotalCollected.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 14));
        lblTotalCollected.setForeground(new Color(0x059669));
        row.add(lblTotalCollected);

        lblTotalPending = new JLabel("Total Pending: PKR 0.00");
        lblTotalPending.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 14));
        lblTotalPending.setForeground(new Color(0xD97706));
        row.add(lblTotalPending);

        return row;
    }

    private void refreshSummary() {
        try {
            double collected = challanDao.getTotalCollected();
            double pending   = challanDao.getTotalPending();
            lblTotalCollected.setText("Total Collected: PKR " + formatCurrency(collected));
            lblTotalPending.setText("Total Pending: PKR " + formatCurrency(pending));
        } catch (SQLException ex) {
            lblTotalCollected.setText("Total Collected: —");
            lblTotalPending.setText("Total Pending: —");
        }
    }

    // ── Data Loading ────────────────────────────────────────────────────

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            if ("ALL".equals(currentFilter)) {
                currentChallans = challanDao.getAllChallans();
            } else {
                currentChallans = challanDao.getChallansByStatus(currentFilter);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

            for (FeeChallan c : currentChallans) {
                String issuedDate = c.getIssuedAt() != null ? sdf.format(c.getIssuedAt()) : "—";
                String proofDate = c.getProofSubmittedAt() != null ? sdf.format(c.getProofSubmittedAt()) : "—";

                tableModel.addRow(new Object[]{
                    String.format("#CHN-%04d", c.getChallanId()),
                    c.getStudentRoll(),
                    c.getStudentName(),
                    c.getRouteName(),
                    "PKR " + formatCurrency(c.getAmountDue()),
                    c.getStatus(),
                    issuedDate,
                    proofDate,
                    "action"
                });
            }

            if (currentChallans.isEmpty()) {
                tableModel.addRow(new Object[]{"No challans found", "", "", "", "", "", "", "", ""});
            }
        } catch (SQLException ex) {
            tableModel.addRow(new Object[]{"Error: " + ex.getMessage(), "", "", "", "", "", "", "", ""});
        }
        refreshSummary();
    }

    // ── Table Styling ───────────────────────────────────────────────────

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

        JTableHeader header = table.getTableHeader();
        header.setBackground(AppColors.BG_SECONDARY);
        header.setForeground(AppColors.TEXT_SECONDARY);
        header.setFont(new Font(AppFonts.LABEL.getFamily(), Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppColors.ACCENT));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setBackground(AppColors.BG_SECONDARY);
                lbl.setForeground(AppColors.TEXT_SECONDARY);
                lbl.setFont(new Font(AppFonts.LABEL.getFamily(), Font.BOLD, 12));
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        // Cell renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                lbl.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 12));

                if (sel) {
                    lbl.setBackground(AppColors.ACCENT_LIGHT);
                    lbl.setForeground(AppColors.ACCENT);
                } else if (row == hoveredRow) {
                    lbl.setBackground(AppColors.BG_HOVER);
                    lbl.setForeground(AppColors.TEXT_PRIMARY);
                } else {
                    lbl.setBackground(row % 2 == 0 ? AppColors.BG_CARD : AppColors.BG_FIELD);
                    lbl.setForeground(AppColors.TEXT_PRIMARY);
                }

                // Status coloring
                if (col == 5) {
                    String status = v != null ? v.toString() : "";
                    if ("UNPAID".equals(status)) lbl.setForeground(new Color(0xD97706));
                    else if ("PROOF_SUBMITTED".equals(status)) lbl.setForeground(new Color(0x1E40AF));
                    else if ("PAID".equals(status)) lbl.setForeground(new Color(0x059669));
                }
                return lbl;
            }
        });

        // Action column renderer
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new ActionEditor());

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(170);

        // Hover
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
        });
    }

    // ── Action Renderer ─────────────────────────────────────────────────

    private class ActionRenderer extends JPanel implements TableCellRenderer {
        ActionRenderer() { setOpaque(false); setLayout(new FlowLayout(FlowLayout.CENTER, 0, 8)); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            removeAll();
            if (currentChallans == null || row >= currentChallans.size()) return this;

            FeeChallan c = currentChallans.get(row);
            switch (c.getStatus()) {
                case "PROOF_SUBMITTED":
                    JPanel btn = new JPanel() {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            GradientPaint gp = new GradientPaint(0, 0, AppColors.ACCENT, getWidth(), 0, AppColors.VIOLET);
                            g2.setPaint(gp);
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                            g2.setColor(Color.WHITE);
                            g2.setFont(new Font(AppFonts.BUTTON.getFamily(), Font.BOLD, 10));
                            FontMetrics fm = g2.getFontMetrics();
                            String txt = "View Receipt & Confirm";
                            g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                                (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                            g2.dispose();
                        }
                    };
                    btn.setOpaque(false);
                    btn.setPreferredSize(new Dimension(155, 30));
                    add(btn);
                    break;
                case "PAID":
                    JLabel paidLbl = new JLabel("Paid  \u2713");
                    paidLbl.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 12));
                    paidLbl.setForeground(new Color(0x059669));
                    add(paidLbl);
                    break;
                default:
                    JLabel awaitLbl = new JLabel("Awaiting Proof");
                    awaitLbl.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 12));
                    awaitLbl.setForeground(AppColors.TEXT_MUTED);
                    add(awaitLbl);
            }
            return this;
        }
    }

    // ── Action Editor ───────────────────────────────────────────────────

    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton actionBtn;
        private int currentRow;

        ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
            panel.setOpaque(false);

            actionBtn = new JButton("View Receipt & Confirm");
            actionBtn.setFont(new Font(AppFonts.BUTTON.getFamily(), Font.BOLD, 10));
            actionBtn.setBackground(AppColors.ACCENT);
            actionBtn.setForeground(Color.WHITE);
            actionBtn.setOpaque(true);
            actionBtn.setBorderPainted(false);
            actionBtn.setFocusPainted(false);
            actionBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            actionBtn.setPreferredSize(new Dimension(155, 30));
            actionBtn.addActionListener(e -> {
                fireEditingStopped();
                if (currentChallans != null && currentRow < currentChallans.size()) {
                    showReceiptDialog(currentChallans.get(currentRow));
                }
            });
            panel.add(actionBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            currentRow = row;
            if (currentChallans == null || row >= currentChallans.size()) {
                JPanel empty = new JPanel();
                empty.setOpaque(false);
                return empty;
            }
            FeeChallan c = currentChallans.get(row);
            if (!"PROOF_SUBMITTED".equals(c.getStatus())) {
                JPanel empty = new JPanel();
                empty.setOpaque(false);
                return empty;
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return "action"; }
    }

    // ── Receipt Dialog ──────────────────────────────────────────────────

    private void showReceiptDialog(FeeChallan challan) {
        JDialog dlg = new JDialog(parentFrame, "Payment Proof Review", true);
        dlg.setSize(600, 650);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout());

        // Info panel
        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(20, 24, 16, 24));

        GridBagConstraints ic = new GridBagConstraints();
        ic.fill = GridBagConstraints.HORIZONTAL;
        ic.weightx = 1; ic.gridx = 0; ic.gridwidth = 2;

        ic.gridy = 0; ic.insets = new Insets(0, 0, 12, 0);
        JLabel dlgTitle = new JLabel("Payment Proof Review");
        dlgTitle.setFont(new Font(AppFonts.SECTION.getFamily(), Font.BOLD, 20));
        dlgTitle.setForeground(AppColors.TEXT_PRIMARY);
        info.add(dlgTitle, ic);

        ic.gridwidth = 1;
        ic.gridy = 1; ic.gridx = 0; ic.weightx = 0.5;
        ic.insets = new Insets(0, 0, 8, 20);
        info.add(infoItem("Student Name", challan.getStudentName()), ic);

        ic.gridx = 1; ic.insets = new Insets(0, 0, 8, 0);
        info.add(infoItem("Roll Number", challan.getStudentRoll()), ic);

        ic.gridy = 2; ic.gridx = 0; ic.insets = new Insets(0, 0, 8, 20);
        info.add(infoItem("Route", challan.getRouteName()), ic);

        ic.gridx = 1; ic.insets = new Insets(0, 0, 8, 0);
        info.add(infoItem("Amount Due", "PKR " + formatCurrency(challan.getAmountDue())), ic);

        dlg.add(info, BorderLayout.NORTH);

        // Receipt image
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false);
        imagePanel.setBorder(new EmptyBorder(0, 24, 16, 24));

        String path = challan.getReceiptImagePath();
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists() && (path.toLowerCase().endsWith(".pdf"))) {
                JLabel pdfMsg = new JLabel("<html>PDF receipt uploaded — open file to view:<br><b>" + file.getAbsolutePath() + "</b></html>");
                pdfMsg.setFont(AppFonts.BODY);
                pdfMsg.setForeground(AppColors.TEXT_SECONDARY);
                pdfMsg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                pdfMsg.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        try { Desktop.getDesktop().open(file); } catch (Exception ignored) {}
                    }
                });
                imagePanel.add(pdfMsg, BorderLayout.CENTER);
            } else if (file.exists()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        // Scale to fit
                        int maxW = 540, maxH = 350;
                        int iw = img.getWidth(), ih = img.getHeight();
                        double scale = Math.min((double) maxW / iw, (double) maxH / ih);
                        int sw = (int)(iw * scale), sh = (int)(ih * scale);
                        Image scaled = img.getScaledInstance(sw, sh, Image.SCALE_SMOOTH);
                        JLabel imgLabel = new JLabel(new ImageIcon(scaled));
                        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        JScrollPane imgScroll = new JScrollPane(imgLabel);
                        imgScroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
                        imgScroll.setPreferredSize(new Dimension(maxW, maxH));
                        imagePanel.add(imgScroll, BorderLayout.CENTER);
                    }
                } catch (Exception ex) {
                    imagePanel.add(new JLabel("Could not load image: " + ex.getMessage()), BorderLayout.CENTER);
                }
            } else {
                imagePanel.add(new JLabel("Receipt file not found: " + path), BorderLayout.CENTER);
            }
        } else {
            imagePanel.add(new JLabel("No receipt uploaded"), BorderLayout.CENTER);
        }
        dlg.add(imagePanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JButton confirmBtn = new JButton("  \u2713 Confirm Payment  ");
        confirmBtn.setFont(AppFonts.BUTTON);
        confirmBtn.setBackground(new Color(0x059669));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setOpaque(true);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmBtn.setPreferredSize(new Dimension(200, 40));
        confirmBtn.addActionListener(e -> {
            try {
                boolean ok = challanDao.confirmPayment(challan.getChallanId());
                if (ok) {
                    dlg.dispose();
                    loadData();
                    showToast("Payment confirmed. Student's seat is now fully secured.", ToastNotification.Type.SUCCESS);
                } else {
                    JOptionPane.showMessageDialog(dlg, "Failed to confirm payment.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(confirmBtn);

        JButton closeBtn = new JButton("  Close  ");
        closeBtn.setFont(AppFonts.BUTTON);
        closeBtn.setForeground(AppColors.TEXT_SECONDARY);
        closeBtn.setBackground(AppColors.BG_SECONDARY);
        closeBtn.setOpaque(true);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(120, 40));
        closeBtn.addActionListener(e -> dlg.dispose());
        btnPanel.add(closeBtn);

        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private JPanel infoItem(String label, String value) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(AppFonts.LABEL.getFamily(), Font.PLAIN, 11));
        lbl.setForeground(AppColors.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        JLabel val = new JLabel(value);
        val.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 14));
        val.setForeground(AppColors.TEXT_PRIMARY);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(val);
        return p;
    }

    private String formatCurrency(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount);
    }

    private void showToast(String msg, ToastNotification.Type type) {
        if (parentFrame == null) return;
        new ToastNotification(parentFrame, msg, type).show(0);
    }
}
