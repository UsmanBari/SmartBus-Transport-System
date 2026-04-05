package ui.screens;

import dao.FeeChallanDAO;
import dao.StudentDAO;
import model.FeeChallan;
import model.Student;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * StudentFeeChallanPanel – Shows the student's fee challan with 4 states:
 *   A: No challan (PENDING/REJECTED)
 *   B: UNPAID — challan card + upload receipt
 *   C: PROOF_SUBMITTED — challan card + waiting message
 *   D: PAID — challan card + success banner
 */
public class StudentFeeChallanPanel extends JPanel {

    private final StudentDAO    studentDao = new StudentDAO();
    private final FeeChallanDAO challanDao = new FeeChallanDAO();
    private final String        studentRoll;
    private JPanel              contentPanel;
    private JFrame              parentFrame;

    private File selectedFile = null;

    public StudentFeeChallanPanel(JFrame parentFrame, String studentRoll) {
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
        JLabel title = new JLabel("My Fee Challan");
        title.setFont(AppFonts.SECTION);
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerLeft.add(title);
        JLabel sub = new JLabel("    View and manage your transport fee payment");
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

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        contentPanel.removeAll();
        selectedFile = null;

        try {
            Student student = studentDao.getStudentByRoll(studentRoll);
            if (student == null || !"APPROVED".equals(student.getStatus())) {
                // State A: No challan
                contentPanel.add(buildStateA(), BorderLayout.CENTER);
            } else {
                FeeChallan challan = challanDao.getChallanByStudentId(student.getStudentId());
                if (challan == null) {
                    contentPanel.add(buildStateA(), BorderLayout.CENTER);
                } else {
                    switch (challan.getStatus()) {
                        case "UNPAID":
                            contentPanel.add(buildStateB(challan), BorderLayout.CENTER);
                            break;
                        case "PROOF_SUBMITTED":
                            contentPanel.add(buildStateC(challan), BorderLayout.CENTER);
                            break;
                        case "PAID":
                            contentPanel.add(buildStateD(challan), BorderLayout.CENTER);
                            break;
                        default:
                            contentPanel.add(buildStateA(), BorderLayout.CENTER);
                    }
                }
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

    // ══════════════════════════════════════════════════════════════════════════
    //  STATE A: No challan yet
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildStateA() {
        GlassCard card = new GlassCard();
        card.setLayout(new GridBagLayout());
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(60, 40, 60, 40));

        JLabel icon = new JLabel("\uD83D\uDCCB");
        icon.setFont(new Font("Dialog", Font.PLAIN, 52));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(icon);
        inner.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel msg = new JLabel("No Fee Challan Issued Yet");
        msg.setFont(new Font(AppFonts.SECTION.getFamily(), Font.BOLD, 22));
        msg.setForeground(AppColors.TEXT_PRIMARY);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(msg);
        inner.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel hint = new JLabel("Your fee challan will appear here automatically");
        hint.setFont(AppFonts.BODY);
        hint.setForeground(AppColors.TEXT_SECONDARY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(hint);

        JLabel hint2 = new JLabel("once your registration request is approved by the admin.");
        hint2.setFont(AppFonts.BODY);
        hint2.setForeground(AppColors.TEXT_SECONDARY);
        hint2.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(hint2);

        card.add(inner);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  STATE B: UNPAID — challan card + upload section
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildStateB(FeeChallan challan) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        wrapper.add(buildChallanCard(challan));
        wrapper.add(Box.createRigidArea(new Dimension(0, 20)));
        wrapper.add(buildUploadSection(challan));

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  STATE C: PROOF_SUBMITTED
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildStateC(FeeChallan challan) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        wrapper.add(buildChallanCard(challan));
        wrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        // Info message card
        GlassCard infoCard = new GlassCard();
        infoCard.setLayout(new BorderLayout());
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        JPanel infoInner = new JPanel(new BorderLayout());
        infoInner.setOpaque(false);
        infoInner.setBorder(new EmptyBorder(20, 28, 20, 28));

        JLabel infoMsg = new JLabel("<html>\u23F3 Your payment proof has been submitted and is awaiting admin verification.<br>"
                + "Your seat will be confirmed once the admin approves your payment.</html>");
        infoMsg.setFont(AppFonts.BODY);
        infoMsg.setForeground(new Color(0x1E40AF));
        infoInner.add(infoMsg, BorderLayout.CENTER);
        infoCard.add(infoInner);
        wrapper.add(infoCard);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  STATE D: PAID
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildStateD(FeeChallan challan) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        // Success banner
        JPanel banner = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xD1FAE5));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0x059669));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 12));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel bannerText = new JLabel("\u2713  Your seat is confirmed. Payment verified by admin.");
        bannerText.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 15));
        bannerText.setForeground(new Color(0x059669));
        banner.add(bannerText);

        wrapper.add(banner);
        wrapper.add(Box.createRigidArea(new Dimension(0, 20)));
        wrapper.add(buildChallanCard(challan));

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CHALLAN CARD (shared)
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildChallanCard(FeeChallan challan) {
        GlassCard card = new GlassCard();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(32, 36, 32, 36));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        gc.gridwidth = 2;

        // Header: FEE CHALLAN
        gc.gridy = 0; gc.insets = new Insets(0, 0, 2, 0);
        JLabel header = new JLabel("FEE CHALLAN");
        header.setFont(new Font(AppFonts.TITLE.getFamily(), Font.BOLD, 26));
        header.setForeground(AppColors.TEXT_PRIMARY);
        inner.add(header, gc);

        gc.gridy = 1; gc.insets = new Insets(0, 0, 16, 0);
        JLabel challanNo = new JLabel(String.format("#CHN-%04d", challan.getChallanId()));
        challanNo.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 14));
        challanNo.setForeground(AppColors.TEXT_MUTED);
        inner.add(challanNo, gc);

        // Status badge
        gc.gridy = 2; gc.insets = new Insets(0, 0, 20, 0);
        inner.add(buildStatusBadge(challan), gc);

        // Info rows
        gc.gridwidth = 1;
        int row = 3;

        gc.gridy = row; gc.gridx = 0; gc.insets = new Insets(0, 0, 16, 40); gc.weightx = 0.5;
        inner.add(infoBlock("Student Name", challan.getStudentName(), AppColors.TEXT_PRIMARY), gc);
        gc.gridx = 1; gc.insets = new Insets(0, 0, 16, 0);
        inner.add(infoBlock("Roll Number", challan.getStudentRoll(), AppColors.ACCENT), gc);

        row++;
        gc.gridy = row; gc.gridx = 0; gc.insets = new Insets(0, 0, 16, 40);
        inner.add(infoBlock("Route Name", challan.getRouteName(), AppColors.TEXT_PRIMARY), gc);
        gc.gridx = 1; gc.insets = new Insets(0, 0, 16, 0);
        String dateStr = challan.getIssuedAt() != null ?
            new SimpleDateFormat("dd MMM yyyy").format(challan.getIssuedAt()) : "—";
        inner.add(infoBlock("Issue Date", dateStr, AppColors.TEXT_SECONDARY), gc);

        // Amount
        row++;
        gc.gridy = row; gc.gridx = 0; gc.gridwidth = 2; gc.insets = new Insets(8, 0, 8, 0);

        JPanel amountRow = new JPanel(new BorderLayout());
        amountRow.setOpaque(false);
        JLabel amountLabel = new JLabel("Amount Due");
        amountLabel.setFont(new Font(AppFonts.LABEL.getFamily(), Font.PLAIN, 12));
        amountLabel.setForeground(AppColors.TEXT_MUTED);
        amountRow.add(amountLabel, BorderLayout.NORTH);
        JLabel amountValue = new JLabel("PKR " + formatCurrency(challan.getAmountDue()));
        amountValue.setFont(new Font(AppFonts.TITLE.getFamily(), Font.BOLD, 28));
        amountValue.setForeground(AppColors.ACCENT);
        amountRow.add(amountValue, BorderLayout.CENTER);
        inner.add(amountRow, gc);

        // Dates based on status
        if ("PROOF_SUBMITTED".equals(challan.getStatus()) && challan.getProofSubmittedAt() != null) {
            row++;
            gc.gridy = row; gc.insets = new Insets(12, 0, 0, 0);
            String proofDate = new SimpleDateFormat("dd MMM yyyy").format(challan.getProofSubmittedAt());
            JLabel proofLbl = new JLabel("Proof submitted on: " + proofDate);
            proofLbl.setFont(AppFonts.BODY);
            proofLbl.setForeground(new Color(0x1E40AF));
            inner.add(proofLbl, gc);
        } else if ("PAID".equals(challan.getStatus()) && challan.getPaidAt() != null) {
            row++;
            gc.gridy = row; gc.insets = new Insets(12, 0, 0, 0);
            String paidDate = new SimpleDateFormat("dd MMM yyyy").format(challan.getPaidAt());
            JLabel paidLbl = new JLabel("Payment confirmed on: " + paidDate);
            paidLbl.setFont(AppFonts.BODY);
            paidLbl.setForeground(new Color(0x059669));
            inner.add(paidLbl, gc);
        }

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UPLOAD SECTION (State B only)
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildUploadSection(FeeChallan challan) {
        GlassCard uploadCard = new GlassCard();
        uploadCard.setLayout(new BorderLayout());
        uploadCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JPanel uInner = new JPanel(new GridBagLayout());
        uInner.setOpaque(false);
        uInner.setBorder(new EmptyBorder(24, 36, 24, 36));

        GridBagConstraints uc = new GridBagConstraints();
        uc.fill = GridBagConstraints.HORIZONTAL;
        uc.weightx = 1;
        uc.gridx = 0;
        uc.gridwidth = 2;

        uc.gridy = 0; uc.insets = new Insets(0, 0, 4, 0);
        JLabel sectionTitle = new JLabel("Submit Payment Proof");
        sectionTitle.setFont(new Font(AppFonts.SECTION.getFamily(), Font.BOLD, 18));
        sectionTitle.setForeground(AppColors.TEXT_PRIMARY);
        uInner.add(sectionTitle, uc);

        uc.gridy = 1; uc.insets = new Insets(0, 0, 16, 0);
        JLabel uploadLabel = new JLabel("Upload Bank Receipt (Image)");
        uploadLabel.setFont(AppFonts.BODY);
        uploadLabel.setForeground(AppColors.TEXT_SECONDARY);
        uInner.add(uploadLabel, uc);

        // File chooser row
        uc.gridy = 2; uc.gridwidth = 1; uc.weightx = 0;
        uc.insets = new Insets(0, 0, 16, 8);
        JButton chooseBtn = new JButton("  Choose File  ");
        chooseBtn.setFont(AppFonts.BUTTON);
        chooseBtn.setForeground(AppColors.ACCENT);
        chooseBtn.setBackground(AppColors.ACCENT_LIGHT);
        chooseBtn.setOpaque(true);
        chooseBtn.setBorderPainted(false);
        chooseBtn.setFocusPainted(false);
        chooseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chooseBtn.setPreferredSize(new Dimension(140, 38));
        uInner.add(chooseBtn, uc);

        uc.gridx = 1; uc.weightx = 1;
        uc.insets = new Insets(0, 0, 16, 0);
        JLabel fileLabel = new JLabel("No file selected");
        fileLabel.setFont(AppFonts.BODY);
        fileLabel.setForeground(AppColors.TEXT_MUTED);
        uInner.add(fileLabel, uc);

        chooseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(
                "Images & PDF (*.jpg, *.jpeg, *.png, *.pdf)", "jpg", "jpeg", "png", "pdf"));
            fc.setAcceptAllFileFilterUsed(false);
            int result = fc.showOpenDialog(parentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                fileLabel.setText(selectedFile.getName() + "  \u2713");
                fileLabel.setForeground(AppColors.SUCCESS);
            }
        });

        // Submit button
        uc.gridy = 3; uc.gridx = 0; uc.gridwidth = 2; uc.weightx = 1;
        uc.insets = new Insets(4, 0, 0, 0);
        NeonButton submitBtn = new NeonButton("  Submit Payment Proof  ");
        submitBtn.setPreferredSize(new Dimension(0, 44));
        uInner.add(submitBtn, uc);

        submitBtn.addActionListener(e -> handleSubmitProof(challan));

        uploadCard.add(uInner, BorderLayout.CENTER);
        return uploadCard;
    }

    private void handleSubmitProof(FeeChallan challan) {
        if (selectedFile == null) {
            showToast("Please select your bank receipt image before submitting.", ToastNotification.Type.ERROR);
            return;
        }

        try {
            // Create receipts folder if it doesn't exist
            Path receiptsDir = Paths.get("receipts");
            if (!Files.exists(receiptsDir)) {
                Files.createDirectories(receiptsDir);
            }

            // Build file name: challan_{id}_{roll}{ext}
            String originalName = selectedFile.getName();
            String ext = "";
            int dotIdx = originalName.lastIndexOf('.');
            if (dotIdx >= 0) {
                ext = originalName.substring(dotIdx);
            }
            String newName = "challan_" + challan.getChallanId() + "_" + challan.getStudentRoll() + ext;
            Path destPath = receiptsDir.resolve(newName);

            // Copy file
            Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

            // Update DB
            boolean ok = challanDao.submitProof(challan.getChallanId(), destPath.toAbsolutePath().toString());
            if (ok) {
                showToast("Payment proof submitted successfully!", ToastNotification.Type.SUCCESS);
                refreshData();
            } else {
                showToast("Could not submit proof. Please try again.", ToastNotification.Type.ERROR);
            }
        } catch (IOException ex) {
            showToast("File error: " + ex.getMessage(), ToastNotification.Type.ERROR);
        } catch (SQLException ex) {
            showToast("Could not submit proof. Please try again.", ToastNotification.Type.ERROR);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildStatusBadge(FeeChallan challan) {
        Color bgColor;
        String label;

        switch (challan.getStatus()) {
            case "UNPAID":
                bgColor = new Color(0xD97706);
                label = "UNPAID";
                break;
            case "PROOF_SUBMITTED":
                bgColor = new Color(0x1E40AF);
                label = "PROOF SUBMITTED";
                break;
            case "PAID":
                bgColor = new Color(0x059669);
                label = "PAID";
                break;
            default:
                bgColor = AppColors.TEXT_MUTED;
                label = challan.getStatus();
        }

        final Color fBg = bgColor;
        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        badge.setPreferredSize(new Dimension(200, 32));
        badge.setMaximumSize(new Dimension(200, 32));

        JLabel statusLbl = new JLabel(label);
        statusLbl.setFont(new Font(AppFonts.BUTTON.getFamily(), Font.BOLD, 13));
        statusLbl.setForeground(Color.WHITE);
        badge.add(statusLbl);

        return badge;
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
        lblVal.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 16));
        lblVal.setForeground(valueColor);
        lblVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(lblVal);

        return block;
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
