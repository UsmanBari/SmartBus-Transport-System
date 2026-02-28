package ui.screens;

import dao.DriverDAO;
import dao.RouteDAO;
import model.Driver;
import model.Route;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * AssignDriverPanel – Glass-card form for assigning a driver to a route.
 */
public class AssignDriverPanel extends JPanel {

    private final DriverDAO driverDAO = new DriverDAO();
    private final RouteDAO  routeDAO  = new RouteDAO();

    private final ModernTextField    tfDriver   = new ModernTextField("e.g. Ali Hassan");
    private final ModernComboBox<Route> cbRoutes = new ModernComboBox<>();
    private final NeonButton         btnSave    = new NeonButton("  Assign Driver");

    private JFrame parentFrame;

    public AssignDriverPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setOpaque(false);
        setLayout(new GridBagLayout());
        build();
        loadRoutes();
    }

    private void build() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(0, 0, 0, 0);
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.gridx   = 0; gbc.gridy   = 0;
        add(buildCard(), gbc);
    }

    private JPanel buildCard() {
        GlassCard card = new GlassCard();
        card.setPreferredSize(new Dimension(520, 380));
        card.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx   = 0; c.gridy = 0;

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new GridBagLayout());
        inner.setBorder(new EmptyBorder(36, 40, 36, 40));

        GridBagConstraints ic = new GridBagConstraints();
        ic.fill    = GridBagConstraints.HORIZONTAL;
        ic.weightx = 1;
        ic.gridx   = 0;

        // Section title
        ic.gridy  = 0; ic.insets = new Insets(0, 0, 0, 0);
        inner.add(sectionHeader("Assign Driver"), ic);

        ic.gridy  = 1; ic.insets = new Insets(4, 0, 28, 0);
        inner.add(sectionSub("Link a driver to an existing transport route"), ic);

        // Driver name
        ic.gridy  = 2; ic.insets = new Insets(0, 0, 4, 0);
        inner.add(fieldLabel("Driver Name"), ic);
        ic.gridy  = 3; ic.insets = new Insets(0, 0, 18, 0);
        inner.add(tfDriver, ic);

        // Route selector
        ic.gridy  = 4; ic.insets = new Insets(0, 0, 4, 0);
        inner.add(fieldLabel("Assign to Route"), ic);
        ic.gridy  = 5; ic.insets = new Insets(0, 0, 4, 0);

        JPanel cbWrapper = new JPanel(new BorderLayout());
        cbWrapper.setOpaque(false);
        cbWrapper.setPreferredSize(new Dimension(440, 46));
        cbRoutes.setPreferredSize(new Dimension(440, 46));
        cbWrapper.add(cbRoutes, BorderLayout.CENTER);
        inner.add(cbWrapper, ic);

        // Refresh button (small)
        ic.gridy  = 6; ic.insets = new Insets(4, 0, 28, 0);
        ic.fill   = GridBagConstraints.NONE;
        ic.anchor = GridBagConstraints.EAST;
        JButton refreshBtn = buildRefreshBtn();
        inner.add(refreshBtn, ic);

        // Save button
        ic.gridy  = 7; ic.insets = new Insets(0, 0, 0, 0);
        ic.fill   = GridBagConstraints.NONE;
        ic.anchor = GridBagConstraints.WEST;
        inner.add(btnSave, ic);

        card.add(inner, c);
        btnSave.addActionListener(e -> handleSave());
        return card;
    }

    private JButton buildRefreshBtn() {
        JButton btn = new JButton("↻  Refresh");
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(AppFonts.BODY);
        btn.setForeground(AppColors.NEON_CYAN);
        btn.addActionListener(e -> loadRoutes());
        return btn;
    }

    // ── Data loading ────────────────────────────────────────────────────────

    public void loadRoutes() {
        cbRoutes.removeAllItems();
        try {
            List<Route> routes = routeDAO.getAllRoutes();
            if (routes.isEmpty()) {
                // Add a placeholder
                cbRoutes.addItem(new Route(0, "(No routes found – add one first)", 0, 0));
            } else {
                for (Route r : routes) cbRoutes.addItem(r);
            }
        } catch (SQLException ex) {
            cbRoutes.addItem(new Route(0, "(DB error – " + ex.getMessage() + ")", 0, 0));
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

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

    // ── Business logic ──────────────────────────────────────────────────────

    private void handleSave() {
        String driverName = tfDriver.getText().trim();

        if (driverName.isEmpty()) { showError("Driver name cannot be empty."); return; }

        Route selected = (Route) cbRoutes.getSelectedItem();
        if (selected == null || selected.getId() == 0) {
            showError("Please select a valid route.");
            return;
        }

        try {
            boolean ok = driverDAO.addDriver(new Driver(driverName, selected.getId()));
            if (ok) {
                tfDriver.setText("");
                loadRoutes();
                showToast("Driver \"" + driverName + "\" assigned to \"" + selected.getRouteName() + "\".",
                          ToastNotification.Type.SUCCESS);
            } else {
                showError("Failed to assign driver. Please check the database.");
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showToast(String msg, ToastNotification.Type type) {
        if (parentFrame == null) return;
        ToastNotification toast = new ToastNotification(parentFrame, msg, type);
        toast.show(0);
    }

    private void showError(String msg) {
        JDialog dlg = new JDialog(parentFrame, "Validation Error", true);
        dlg.setSize(400, 160);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(20, 24, 8, 24);
        c.gridx   = 0; c.gridy = 0;
        c.fill    = GridBagConstraints.HORIZONTAL; c.weightx = 1;

        JLabel icon = new JLabel("⚠  " + msg);
        icon.setFont(AppFonts.BODY);
        icon.setForeground(AppColors.ERROR);
        dlg.add(icon, c);

        c.gridy  = 1; c.insets = new Insets(8, 24, 20, 24);
        c.fill   = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        NeonButton ok = new NeonButton("    OK    ");
        ok.addActionListener(e -> dlg.dispose());
        dlg.add(ok, c);

        dlg.setVisible(true);
    }
}
