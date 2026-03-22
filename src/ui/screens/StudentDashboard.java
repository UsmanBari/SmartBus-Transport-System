package ui.screens;

import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * StudentDashboard – Main student portal with sidebar navigation.
 * Sidebar items: Register for Transport, View Routes, Logout.
 */
public class StudentDashboard extends JFrame {

    private static final int SIDEBAR_W = 240;

    private SidebarItem itemRegister;
    private SidebarItem itemMyStatus;
    private SidebarItem itemViewRoutes;
    private SidebarItem itemLogout;

    private StudentRegistrationPanel registrationPanel;
    private StudentStatusPanel       statusPanel;
    private ViewRoutesPanel          viewRoutesPanel;
    private AnimatedPanelSwitcher    switcher;

    private final String studentRoll;

    public StudentDashboard(String studentRoll) {
        super("UniTransport – Student Portal");
        this.studentRoll = studentRoll;
        configureFrame();
        buildUI();
        activateItem(itemRegister, "register");
        setVisible(true);
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        getContentPane().setBackground(AppColors.BG_BASE);

        UIManager.put("ComboBox.background",          AppColors.BG_FIELD);
        UIManager.put("ComboBox.foreground",           AppColors.TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",  AppColors.ACCENT_LIGHT);
        UIManager.put("ComboBox.selectionForeground",  AppColors.ACCENT);
        UIManager.put("List.background",               Color.WHITE);
        UIManager.put("List.foreground",               AppColors.TEXT_PRIMARY);
        UIManager.put("List.selectionBackground",      AppColors.ACCENT_LIGHT);
        UIManager.put("List.selectionForeground",      AppColors.ACCENT);
        UIManager.put("Panel.background",              AppColors.BG_BASE);
    }

    private void buildUI() {
        GradientBackground root = new GradientBackground();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);
    }

    // ── Sidebar ─────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, AppColors.BG_SIDEBAR, 0, h,
                        new Color(0x312E81));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo
        sidebar.add(buildLogoArea());
        sidebar.add(buildDivider());

        // Section label
        sidebar.add(buildSectionLabel("STUDENT SERVICES"));

        // Nav items
        itemRegister   = new SidebarItem("\uD83D\uDCDD", "Register");
        itemMyStatus   = new SidebarItem("\uD83D\uDCCA", "My Status");
        itemViewRoutes = new SidebarItem("\uD83D\uDDFA", "View Routes");
        itemLogout     = new SidebarItem("\uD83D\uDEAA", "Logout");

        itemRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemMyStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemViewRoutes.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemLogout.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(itemRegister);
        sidebar.add(Box.createRigidArea(new Dimension(0, 2)));
        sidebar.add(itemMyStatus);
        sidebar.add(Box.createRigidArea(new Dimension(0, 2)));
        sidebar.add(itemViewRoutes);
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));
        sidebar.add(buildDivider());

        // Logout at the bottom
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildSectionLabel("SESSION"));
        sidebar.add(itemLogout);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(buildVersionBadge());

        // Wire clicks
        itemRegister.setOnClick(() -> activateItem(itemRegister, "register"));
        itemMyStatus.setOnClick(() -> {
            statusPanel.refreshData();
            activateItem(itemMyStatus, "myStatus");
        });
        itemViewRoutes.setOnClick(() -> {
            viewRoutesPanel.refreshData();
            activateItem(itemViewRoutes, "viewRoutes");
        });
        itemLogout.setOnClick(this::handleLogout);

        return sidebar;
    }

    private JPanel buildLogoArea() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int y = getHeight() - 1;
                GradientPaint glow = new GradientPaint(20, y, new Color(0xA5B4FC),
                        getWidth() - 20, y, new Color(0xC4B5FD));
                g2.setPaint(glow);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(20, y, getWidth() - 20, y);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(SIDEBAR_W, 80));
        panel.setMaximumSize(new Dimension(SIDEBAR_W, 80));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(0, 20, 0, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 2, 8);
        JLabel iconLbl = new JLabel("\u2B21");
        iconLbl.setFont(new Font("Dialog", Font.PLAIN, 18));
        iconLbl.setForeground(new Color(0xA5B4FC));
        panel.add(iconLbl, c);

        c.gridx = 1; c.insets = new Insets(0, 0, 2, 0);
        JLabel name = new JLabel("UniTransport");
        name.setFont(AppFonts.SIDEBAR);
        name.setForeground(Color.WHITE);
        panel.add(name, c);

        return panel;
    }

    private JPanel buildDivider() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(AppColors.DIVIDER);
                g.drawLine(16, 0, getWidth() - 16, 0);
            }
        };
        d.setOpaque(false);
        d.setPreferredSize(new Dimension(SIDEBAR_W, 1));
        d.setMaximumSize(new Dimension(SIDEBAR_W, 1));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        return d;
    }

    private JLabel buildSectionLabel(String txt) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 10));
        lbl.setForeground(new Color(0x818CF8));
        lbl.setBorder(new EmptyBorder(18, 24, 8, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildVersionBadge() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(SIDEBAR_W, 44));
        JLabel v = new JLabel("Student Panel  \u00b7  v2.0.0");
        v.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 11));
        v.setForeground(new Color(0x818CF8));
        p.add(v);
        return p;
    }

    // ── Main Area ───────────────────────────────────────────────────────────

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(0, 0, 0, 0));
        main.add(buildHeader(), BorderLayout.NORTH);
        main.add(buildContentArea(), BorderLayout.CENTER);
        return main;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppColors.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setLayout(new GridBagLayout());
        header.setBorder(new EmptyBorder(28, 48, 24, 48));
        header.setPreferredSize(new Dimension(0, 100));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.gridx = 0;

        c.gridy = 0; c.insets = new Insets(0, 0, 4, 0);
        JLabel title = new JLabel("Student Portal");
        title.setFont(AppFonts.TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);
        header.add(title, c);

        c.gridy = 1;
        JPanel subRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        subRow.setOpaque(false);
        JLabel sub1 = new JLabel("Student Dashboard");
        sub1.setFont(AppFonts.BODY);
        sub1.setForeground(AppColors.TEXT_SECONDARY);
        JLabel dot = new JLabel("  \u00b7  ");
        dot.setFont(AppFonts.BODY);
        dot.setForeground(AppColors.TEXT_MUTED);
        JLabel sub2 = new JLabel("Roll: " + studentRoll);
        sub2.setFont(AppFonts.BODY);
        sub2.setForeground(AppColors.ACCENT);
        subRow.add(sub1); subRow.add(dot); subRow.add(sub2);
        header.add(subRow, c);

        return header;
    }

    private JPanel buildContentArea() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(40, 48, 40, 48));

        registrationPanel = new StudentRegistrationPanel(this);
        statusPanel       = new StudentStatusPanel(this, studentRoll);
        viewRoutesPanel   = new ViewRoutesPanel(this);

        switcher = new AnimatedPanelSwitcher();
        switcher.addPanel("register",   registrationPanel);
        switcher.addPanel("myStatus",   statusPanel);
        switcher.addPanel("viewRoutes", viewRoutesPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        content.add(switcher, gbc);

        return content;
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private void activateItem(SidebarItem target, String panelKey) {
        itemRegister.setActive(false);
        itemMyStatus.setActive(false);
        itemViewRoutes.setActive(false);
        itemLogout.setActive(false);
        target.setActive(true);
        switcher.switchTo(panelKey);
    }

    private void handleLogout() {
        new LoginScreen();
        dispose();
    }
}
