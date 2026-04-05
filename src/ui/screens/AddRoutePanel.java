package ui.screens;

import dao.RouteDAO;
import model.Route;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * AddRoutePanel – Glass-card form for entering and persisting a new Route.
 */
public class AddRoutePanel extends JPanel {

    private final RouteDAO dao = new RouteDAO();

    private final ModernTextField tfName     = new ModernTextField("e.g. Campus to City Center");
    private final ModernTextField tfStops    = new ModernTextField("e.g. 12");
    private final ModernTextField tfDistance = new ModernTextField("e.g. 18.5");
    private final ModernTextField tfFee      = new ModernTextField("e.g. 15000");
    private final NeonButton      btnSave    = new NeonButton("  Save Route");

    private JFrame parentFrame;

    public AddRoutePanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setOpaque(false);
        setLayout(new GridBagLayout());
        build();
    }

    private void build() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill   = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.gridx = 0; gbc.gridy = 0;
        add(buildCard(), gbc);
    }

    private JPanel buildCard() {
        GlassCard card = new GlassCard();
        card.setPreferredSize(new Dimension(520, 500));
        card.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(0, 0, 0, 0);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx   = 0;
        c.gridy   = 0;

        // ── Card padding wrapper ────────────────────────────────────────────
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new GridBagLayout());
        inner.setBorder(new EmptyBorder(36, 40, 36, 40));

        GridBagConstraints ic = new GridBagConstraints();
        ic.fill    = GridBagConstraints.HORIZONTAL;
        ic.weightx = 1;
        ic.gridx   = 0;
        ic.insets  = new Insets(0, 0, 0, 0);

        // Section title
        ic.gridy = 0;
        inner.add(sectionHeader("Add New Route"), ic);

        // Sub-label
        ic.gridy = 1;
        ic.insets = new Insets(4, 0, 28, 0);
        inner.add(sectionSub("Define route name, stop count and total distance"), ic);

        // Route Name
        ic.gridy  = 2; ic.insets = new Insets(0, 0, 4, 0);
        inner.add(fieldLabel("Route Name"), ic);
        ic.gridy  = 3; ic.insets = new Insets(0, 0, 18, 0);
        inner.add(tfName, ic);

        // Total Stops
        ic.gridy  = 4; ic.insets = new Insets(0, 0, 4, 0);
        inner.add(fieldLabel("Total Stops"), ic);
        ic.gridy  = 5; ic.insets = new Insets(0, 0, 18, 0);
        inner.add(tfStops, ic);

        // Distance
        ic.gridy  = 6; ic.insets = new Insets(0, 0, 4, 0);
        inner.add(fieldLabel("Distance (km)"), ic);
        ic.gridy  = 7; ic.insets = new Insets(0, 0, 18, 0);
        inner.add(tfDistance, ic);

        // Semester Fee
        ic.gridy  = 8; ic.insets = new Insets(0, 0, 4, 0);
        inner.add(fieldLabel("Semester Fee (PKR)"), ic);
        ic.gridy  = 9; ic.insets = new Insets(0, 0, 30, 0);
        inner.add(tfFee, ic);

        // Save button
        ic.gridy  = 10; ic.insets = new Insets(0, 0, 0, 0);
        ic.fill   = GridBagConstraints.NONE;
        ic.anchor = GridBagConstraints.WEST;
        inner.add(btnSave, ic);

        card.add(inner, c);
        btnSave.addActionListener(e -> handleSave());
        return card;
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
        String name     = tfName.getText().trim();
        String stopsStr = tfStops.getText().trim();
        String distStr  = tfDistance.getText().trim();
        String feeStr   = tfFee.getText().trim();

        if (name.isEmpty() || stopsStr.isEmpty() || distStr.isEmpty() || feeStr.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        int    stops;
        double dist;
        double fee;
        try { stops = Integer.parseInt(stopsStr); }
        catch (NumberFormatException ex) { showError("Total Stops must be a whole number."); return; }

        try { dist = Double.parseDouble(distStr); }
        catch (NumberFormatException ex) { showError("Distance must be a valid number."); return; }

        try { fee = Double.parseDouble(feeStr); }
        catch (NumberFormatException ex) { showError("Please enter a valid fee amount (must be greater than 0)."); return; }

        if (stops <= 0)  { showError("Total Stops must be greater than 0."); return; }
        if (dist  <= 0)  { showError("Distance must be greater than 0."); return; }
        if (fee   <= 0)  { showError("Please enter a valid fee amount (must be greater than 0)."); return; }

        try {
            boolean ok = dao.addRoute(new Route(name, stops, dist, fee));
            if (ok) {
                clearFields();
                showToast("Route \"" + name + "\" saved successfully.", ToastNotification.Type.SUCCESS);
            } else {
                showError("Failed to insert route. Please check the database.");
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        tfName.setText("");
        tfStops.setText("");
        tfDistance.setText("");
        tfFee.setText("");
    }

    private void showToast(String msg, ToastNotification.Type type) {
        if (parentFrame == null) return;
        ToastNotification toast = new ToastNotification(parentFrame, msg, type);
        toast.show(0);
    }

    private void showError(String msg) {
        JDialog dlg = new JDialog(parentFrame, "Validation Error", true);
        dlg.setUndecorated(false);
        dlg.setSize(380, 160);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(20, 24, 8, 24);
        c.gridx  = 0; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;

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
