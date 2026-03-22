package ui.screens;

import dao.StudentDAO;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

/**
 * LoginScreen – Full-screen role-based login with Admin / Student tabs.
 * Student tab has Login and Register sub-tabs.
 * Admin: password "admin123"
 * Student: register with name+roll+password, then login with roll+password.
 */
public class LoginScreen extends JFrame {

    private static final String ADMIN_PASSWORD = "admin123";

    private JPanel     cardContainer;
    private CardLayout cardLayout;

    // Admin fields
    private ModernPasswordField pfAdminPass;

    // Student Login fields
    private ModernTextField     tfLoginRoll;
    private ModernPasswordField pfLoginPass;

    // Student Register fields
    private ModernTextField     tfRegName;
    private ModernTextField     tfRegRoll;
    private ModernPasswordField pfRegPass;

    // Student sub-card
    private JPanel     studentCardContainer;
    private CardLayout studentCardLayout;

    public LoginScreen() {
        super("Smart University Transport Management System \u2013 Login");
        configureFrame();
        buildUI();
        setVisible(true);
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        getContentPane().setBackground(AppColors.BG_BASE);

        UIManager.put("Panel.background",             AppColors.BG_BASE);
        UIManager.put("OptionPane.background",         Color.WHITE);
        UIManager.put("OptionPane.messageForeground",  AppColors.TEXT_PRIMARY);
    }

    private void buildUI() {
        GradientBackground root = new GradientBackground();
        root.setLayout(new GridBagLayout());
        setContentPane(root);

        GlassCard card = new GlassCard();
        card.setPreferredSize(new Dimension(480, 580));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new GridBagLayout());
        inner.setBorder(new EmptyBorder(32, 44, 32, 44));

        GridBagConstraints ic = new GridBagConstraints();
        ic.fill = GridBagConstraints.HORIZONTAL;
        ic.weightx = 1;
        ic.gridx = 0;

        // Logo
        ic.gridy = 0; ic.insets = new Insets(0, 0, 2, 0);
        JLabel icon = new JLabel("\u2B21  UniTransport", SwingConstants.CENTER);
        icon.setFont(new Font(AppFonts.TITLE.getFamily(), Font.BOLD, 26));
        icon.setForeground(AppColors.ACCENT);
        inner.add(icon, ic);

        ic.gridy = 1; ic.insets = new Insets(0, 0, 24, 0);
        JLabel subtitle = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subtitle.setFont(AppFonts.BODY);
        subtitle.setForeground(AppColors.TEXT_SECONDARY);
        inner.add(subtitle, ic);

        // Role Tabs (Admin / Student)
        ic.gridy = 2; ic.insets = new Insets(0, 0, 20, 0);
        inner.add(buildRoleTabs(), ic);

        // Card container
        ic.gridy = 3; ic.insets = new Insets(0, 0, 0, 0);
        ic.fill = GridBagConstraints.BOTH;
        ic.weighty = 1;
        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false);
        cardContainer.add(buildAdminForm(), "admin");
        cardContainer.add(buildStudentPanel(), "student");
        inner.add(cardContainer, ic);

        gc.gridy = 0;
        card.add(inner, gc);
        root.add(card, new GridBagConstraints());

        cardLayout.show(cardContainer, "admin");
    }

    // ── Role Tabs ───────────────────────────────────────────────────────────

    private JPanel buildRoleTabs() {
        JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
        tabs.setOpaque(false);
        tabs.setPreferredSize(new Dimension(0, 42));
        NeonButton tabAdmin   = new NeonButton("Admin");
        NeonButton tabStudent = new NeonButton("Student");
        tabAdmin.setPreferredSize(new Dimension(0, 42));
        tabStudent.setPreferredSize(new Dimension(0, 42));
        tabAdmin.addActionListener(e -> cardLayout.show(cardContainer, "admin"));
        tabStudent.addActionListener(e -> cardLayout.show(cardContainer, "student"));
        tabs.add(tabAdmin);
        tabs.add(tabStudent);
        return tabs;
    }

    // ── Admin Form ──────────────────────────────────────────────────────────

    private JPanel buildAdminForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.gridx = 0;

        c.gridy = 0; c.insets = new Insets(0, 0, 4, 0);
        panel.add(fieldLabel("Admin Password"), c);

        c.gridy = 1; c.insets = new Insets(0, 0, 24, 0);
        pfAdminPass = new ModernPasswordField("Enter admin password");
        panel.add(pfAdminPass, c);

        c.gridy = 2; c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.CENTER;
        NeonButton btnLogin = new NeonButton("  Login as Admin  ");
        btnLogin.addActionListener(e -> handleAdminLogin());
        panel.add(btnLogin, c);

        pfAdminPass.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleAdminLogin();
            }
        });

        c.gridy = 3; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), c);
        return panel;
    }

    // ── Student Panel (Login + Register sub-tabs) ───────────────────────────

    private JPanel buildStudentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.gridx = 0;

        // Sub-tabs: Login / Register
        c.gridy = 0; c.insets = new Insets(0, 0, 16, 0);
        panel.add(buildStudentSubTabs(), c);

        // Sub-card container
        c.gridy = 1; c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        studentCardLayout    = new CardLayout();
        studentCardContainer = new JPanel(studentCardLayout);
        studentCardContainer.setOpaque(false);
        studentCardContainer.add(buildStudentLoginForm(), "login");
        studentCardContainer.add(buildStudentRegisterForm(), "register");
        panel.add(studentCardContainer, c);

        studentCardLayout.show(studentCardContainer, "login");
        return panel;
    }

    private JPanel buildStudentSubTabs() {
        JPanel tabs = new JPanel(new GridLayout(1, 2, 6, 0));
        tabs.setOpaque(false);
        tabs.setPreferredSize(new Dimension(0, 34));

        JButton btnLogin = createSubTab("Login");
        JButton btnRegister = createSubTab("Register");

        btnLogin.addActionListener(e -> studentCardLayout.show(studentCardContainer, "login"));
        btnRegister.addActionListener(e -> studentCardLayout.show(studentCardContainer, "register"));

        tabs.add(btnLogin);
        tabs.add(btnRegister);
        return tabs;
    }

    private JButton createSubTab(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(AppColors.ACCENT_LIGHT);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(AppColors.ACCENT);
                g2.setFont(new Font(AppFonts.LABEL.getFamily(), Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 34));
        return btn;
    }

    // ── Student Login Form ──────────────────────────────────────────────────

    private JPanel buildStudentLoginForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.gridx = 0;

        c.gridy = 0; c.insets = new Insets(0, 0, 4, 0);
        panel.add(fieldLabel("Roll Number"), c);

        c.gridy = 1; c.insets = new Insets(0, 0, 14, 0);
        tfLoginRoll = new ModernTextField("e.g. i230680");
        panel.add(tfLoginRoll, c);

        c.gridy = 2; c.insets = new Insets(0, 0, 4, 0);
        panel.add(fieldLabel("Password"), c);

        c.gridy = 3; c.insets = new Insets(0, 0, 20, 0);
        pfLoginPass = new ModernPasswordField("Enter your password");
        panel.add(pfLoginPass, c);

        c.gridy = 4; c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.CENTER;
        NeonButton btnLogin = new NeonButton("  Login  ");
        btnLogin.addActionListener(e -> handleStudentLogin());
        panel.add(btnLogin, c);

        pfLoginPass.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleStudentLogin();
            }
        });

        c.gridy = 5; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), c);
        return panel;
    }

    // ── Student Register Form ───────────────────────────────────────────────

    private JPanel buildStudentRegisterForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.gridx = 0;

        c.gridy = 0; c.insets = new Insets(0, 0, 4, 0);
        panel.add(fieldLabel("Full Name"), c);

        c.gridy = 1; c.insets = new Insets(0, 0, 12, 0);
        tfRegName = new ModernTextField("e.g. Ahmad Ali");
        panel.add(tfRegName, c);

        c.gridy = 2; c.insets = new Insets(0, 0, 4, 0);
        panel.add(fieldLabel("Roll Number"), c);

        c.gridy = 3; c.insets = new Insets(0, 0, 12, 0);
        tfRegRoll = new ModernTextField("e.g. i230680");
        panel.add(tfRegRoll, c);

        c.gridy = 4; c.insets = new Insets(0, 0, 4, 0);
        panel.add(fieldLabel("Create Password"), c);

        c.gridy = 5; c.insets = new Insets(0, 0, 20, 0);
        pfRegPass = new ModernPasswordField("Choose a strong password");
        panel.add(pfRegPass, c);

        c.gridy = 6; c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.CENTER;
        NeonButton btnRegister = new NeonButton("  Create Account  ");
        btnRegister.addActionListener(e -> handleStudentRegister());
        panel.add(btnRegister, c);

        pfRegPass.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleStudentRegister();
            }
        });

        c.gridy = 7; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), c);
        return panel;
    }

    // ── Login Handlers ──────────────────────────────────────────────────────

    private void handleAdminLogin() {
        String pass = new String(pfAdminPass.getPassword()).trim();
        if (pass.isEmpty()) {
            showToast("Please enter the admin password.", ToastNotification.Type.ERROR);
            return;
        }
        if (!pass.equals(ADMIN_PASSWORD)) {
            showToast("Invalid admin password.", ToastNotification.Type.ERROR);
            return;
        }
        showToast("Welcome, Admin!", ToastNotification.Type.SUCCESS);
        Timer delay = new Timer(600, e -> {
            ((Timer) e.getSource()).stop();
            new AdminDashboard();
            dispose();
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void handleStudentLogin() {
        String roll = tfLoginRoll.getText().trim();
        String pass = new String(pfLoginPass.getPassword()).trim();

        if (roll.isEmpty() || pass.isEmpty()) {
            showToast("Please fill in all fields.", ToastNotification.Type.ERROR);
            return;
        }

        try {
            StudentDAO dao = new StudentDAO();
            String name = dao.authenticate(roll, pass);
            if (name == null) {
                showToast("Invalid roll number or password.", ToastNotification.Type.ERROR);
                return;
            }
            showToast("Welcome, " + name + "!", ToastNotification.Type.SUCCESS);
            Timer delay = new Timer(600, e -> {
                ((Timer) e.getSource()).stop();
                new StudentDashboard(roll);
                dispose();
            });
            delay.setRepeats(false);
            delay.start();
        } catch (SQLException ex) {
            showToast("Database error: " + ex.getMessage(), ToastNotification.Type.ERROR);
        }
    }

    private void handleStudentRegister() {
        String name = tfRegName.getText().trim();
        String roll = tfRegRoll.getText().trim();
        String pass = new String(pfRegPass.getPassword()).trim();

        if (name.isEmpty() || roll.isEmpty() || pass.isEmpty()) {
            showToast("All fields are required.", ToastNotification.Type.ERROR);
            return;
        }

        if (pass.length() < 4) {
            showToast("Password must be at least 4 characters.", ToastNotification.Type.ERROR);
            return;
        }

        try {
            StudentDAO dao = new StudentDAO();
            if (dao.isAccountExists(roll)) {
                showToast("Account with this roll already exists. Please login.", ToastNotification.Type.ERROR);
                return;
            }
            boolean ok = dao.registerAccount(name, roll, pass);
            if (ok) {
                tfRegName.setText("");
                tfRegRoll.setText("");
                pfRegPass.setText("");
                showToast("Account created! You can now login.", ToastNotification.Type.SUCCESS);
                // Switch to login tab
                studentCardLayout.show(studentCardContainer, "login");
            } else {
                showToast("Registration failed. Please try again.", ToastNotification.Type.ERROR);
            }
        } catch (SQLException ex) {
            showToast("Database error: " + ex.getMessage(), ToastNotification.Type.ERROR);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private JLabel fieldLabel(String txt) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(AppFonts.LABEL);
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        return lbl;
    }

    private void showToast(String msg, ToastNotification.Type type) {
        new ToastNotification(this, msg, type).show(0);
    }
}
