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
 * StudentRegistrationPanel – GlassCard form for student transport registration.
 * Inserts a new row with status=PENDING.
 */
public class StudentRegistrationPanel extends JPanel {

    private final StudentDAO studentDao = new StudentDAO();
    private final RouteDAO   routeDao   = new RouteDAO();

    private final ModernTextField        tfName = new ModernTextField("e.g. Ahmad Ali");
    private final ModernTextField        tfRoll = new ModernTextField("e.g. i230680");
    private final ModernComboBox<Route>  cbRoutes = new ModernComboBox<>();
    private final NeonButton             btnSubmit = new NeonButton("  Submit Request  ");

    private JFrame parentFrame;

    public StudentRegistrationPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setOpaque(false);
        setLayout(new GridBagLayout());
        build();
        loadRoutes();
    }

    private void build() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.gridx = 0; gbc.gridy = 0;
        add(buildCard(), gbc);
    }

    private JPanel buildCard() {
        GlassCard card = new GlassCard();
        card.setPreferredSize(new Dimension(520, 480));
        card.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0; c.gridy = 0;

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new GridBagLayout());
        inner.setBorder(new EmptyBorder(36, 40, 36, 40));

        GridBagConstraints ic = new GridBagConstraints();
        ic.fill = GridBagConstraints.HORIZONTAL;
        ic.weightx = 1;
        ic.gridx = 0;

        // Title
        ic.gridy = 0; ic.insets = new Insets(0, 0, 0, 0);
        inner.add(sectionHeader("Register for Transport"), ic);

        ic.gridy = 1; ic.insets = new Insets(4, 0, 28, 0);
        inner.add(sectionSub("Submit a transport request for admin approval"), ic);

        // Name
        ic.gridy = 2; ic.insets = new Insets(0, 0, 4, 0);
        inner.add(fieldLabel("Full Name"), ic);
        ic.gridy = 3; ic.insets = new Insets(0, 0, 18, 0);
        inner.add(tfName, ic);

        // Roll
        ic.gridy = 4; ic.insets = new Insets(0, 0, 4, 0);
        inner.add(fieldLabel("Roll Number"), ic);
        ic.gridy = 5; ic.insets = new Insets(0, 0, 18, 0);
        inner.add(tfRoll, ic);

        // Route
        ic.gridy = 6; ic.insets = new Insets(0, 0, 4, 0);
        inner.add(fieldLabel("Select Route"), ic);
        ic.gridy = 7; ic.insets = new Insets(0, 0, 4, 0);
        JPanel cbWrapper = new JPanel(new BorderLayout());
        cbWrapper.setOpaque(false);
        cbWrapper.setPreferredSize(new Dimension(440, 46));
        cbRoutes.setPreferredSize(new Dimension(440, 46));
        cbWrapper.add(cbRoutes, BorderLayout.CENTER);
        inner.add(cbWrapper, ic);

        // Refresh
        ic.gridy = 8; ic.insets = new Insets(4, 0, 28, 0);
        ic.fill = GridBagConstraints.NONE;
        ic.anchor = GridBagConstraints.EAST;
        JButton refreshBtn = buildRefreshBtn();
        inner.add(refreshBtn, ic);

        // Submit
        ic.gridy = 9; ic.insets = new Insets(0, 0, 0, 0);
        ic.fill = GridBagConstraints.NONE;
        ic.anchor = GridBagConstraints.WEST;
        inner.add(btnSubmit, ic);

        card.add(inner, c);
        btnSubmit.addActionListener(e -> handleSubmit());
        return card;
    }

    private void loadRoutes() {
        cbRoutes.removeAllItems();
        try {
            List<Route> routes = routeDao.getAllRoutes();
            if (routes.isEmpty()) {
                cbRoutes.addItem(new Route(0, "(No routes available)", 0, 0));
            } else {
                for (Route r : routes) cbRoutes.addItem(r);
            }
        } catch (SQLException ex) {
            cbRoutes.addItem(new Route(0, "(DB error)", 0, 0));
        }
    }

    private JButton buildRefreshBtn() {
        JButton btn = new JButton("\u21BB  Refresh Routes");
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(AppFonts.BODY);
        btn.setForeground(AppColors.ACCENT);
        btn.addActionListener(e -> loadRoutes());
        return btn;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JLabel sectionHeader(String txt) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(AppFonts.SECTION);
        lbl.setForeground(AppColors.TEXT_PRIMARY);
        return lbl;
    }

    private JLabel sectionSub(String txt) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(AppFonts.BODY);
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        return lbl;
    }

    private JLabel fieldLabel(String txt) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(AppFonts.LABEL);
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        return lbl;
    }

    // ── Business Logic ──────────────────────────────────────────────────────

    private void handleSubmit() {
        String name = tfName.getText().trim();
        String roll = tfRoll.getText().trim();

        if (name.isEmpty() || roll.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        Route selected = (Route) cbRoutes.getSelectedItem();
        if (selected == null || selected.getId() == 0) {
            showError("Please select a valid route.");
            return;
        }

        try {
            // Duplicate check – block if already PENDING or APPROVED
            if (studentDao.hasPendingOrApprovedRequest(roll)) {
                showError("You already have a pending or approved transport request.\nYou cannot register again.");
                return;
            }

            boolean ok = studentDao.registerStudent(
                new model.Student(name, roll, selected.getId())
            );
            if (ok) {
                tfName.setText("");
                tfRoll.setText("");
                showToast("Request submitted. Awaiting admin approval.", ToastNotification.Type.SUCCESS);
            } else {
                showError("Failed to submit request. Please try again.");
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showToast(String msg, ToastNotification.Type type) {
        if (parentFrame == null) return;
        new ToastNotification(parentFrame, msg, type).show(0);
    }

    private void showError(String msg) {
        JDialog dlg = new JDialog(parentFrame, "Validation Error", true);
        dlg.setSize(420, 160);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(20, 24, 8, 24);
        c.gridx = 0; c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;

        JLabel icon = new JLabel("\u26A0  " + msg);
        icon.setFont(AppFonts.BODY);
        icon.setForeground(AppColors.ERROR);
        dlg.add(icon, c);

        c.gridy = 1; c.insets = new Insets(8, 24, 20, 24);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        NeonButton ok = new NeonButton("    OK    ");
        ok.addActionListener(e -> dlg.dispose());
        dlg.add(ok, c);

        dlg.setVisible(true);
    }
}
