package ui.screens;

import config.DatabaseConnection;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * AdminDashboard – Main admin application window.
 *
 * Sidebar Items:
 *   TRANSPORT NETWORK:  Add Route, Assign Driver, View Routes
 *   STUDENT MANAGEMENT: Student Requests, Student Lookup
 *   SESSION:            Logout
 */
public class AdminDashboard extends JFrame {

    private static final int SIDEBAR_W = 240;

    private SidebarItem itemAddRoute;
    private SidebarItem itemAssignDriver;
    private SidebarItem itemViewRoutes;
    private SidebarItem itemStudentRequests;
    private SidebarItem itemStudentLookup;
    private SidebarItem itemLogout;

    private AddRoutePanel            addRoutePanel;
    private AssignDriverPanel        assignDriverPanel;
    private AdminViewRoutesPanel     viewRoutesPanel;
    private AdminApprovalPanel       approvalPanel;
    private AdminStudentLookupPanel  lookupPanel;
    private AnimatedPanelSwitcher    switcher;

    public AdminDashboard() {
        super("Smart University Transport Management System");
        configureFrame();
        buildUI();
        activateItem(itemAddRoute, "addRoute");
        setVisible(true);
    }

    // ── Frame setup ─────────────────────────────────────────────────────────

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        getContentPane().setBackground(AppColors.BG_BASE);

        UIManager.put("ComboBox.background",              AppColors.BG_FIELD);
        UIManager.put("ComboBox.foreground",              AppColors.TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",     AppColors.ACCENT_LIGHT);
        UIManager.put("ComboBox.selectionForeground",     AppColors.ACCENT);
        UIManager.put("ComboBox.disabledForeground",      AppColors.TEXT_MUTED);
        UIManager.put("List.background",                  Color.WHITE);
        UIManager.put("List.foreground",                  AppColors.TEXT_PRIMARY);
        UIManager.put("List.selectionBackground",         AppColors.ACCENT_LIGHT);
        UIManager.put("List.selectionForeground",         AppColors.ACCENT);
        UIManager.put("ScrollBar.background",             AppColors.BG_BASE);
        UIManager.put("ScrollBar.thumb",                  AppColors.BORDER);
        UIManager.put("ScrollBar.track",                  AppColors.BG_BASE);
        UIManager.put("Panel.background",                 AppColors.BG_BASE);
        UIManager.put("OptionPane.background",            Color.WHITE);
        UIManager.put("OptionPane.messageForeground",     AppColors.TEXT_PRIMARY);
    }

    // ── UI construction ──────────────────────────────────────────────────────

    private void buildUI() {
        GradientBackground root = new GradientBackground();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────

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

        // ── Logo ────────────────────────────────────────────────────────────
        sidebar.add(buildLogoArea());
        sidebar.add(buildDivider());

        // ── TRANSPORT NETWORK ───────────────────────────────────────────────
        sidebar.add(buildSectionLabel("TRANSPORT NETWORK"));

        itemAddRoute     = new SidebarItem("\uD83D\uDEE3", "Add Route");
        itemAssignDriver = new SidebarItem("\uD83D\uDC64", "Assign Driver");
        itemViewRoutes   = new SidebarItem("\uD83D\uDDFA", "View Routes");

        itemAddRoute.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemAssignDriver.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemViewRoutes.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(itemAddRoute);
        sidebar.add(Box.createRigidArea(new Dimension(0, 2)));
        sidebar.add(itemAssignDriver);
        sidebar.add(Box.createRigidArea(new Dimension(0, 2)));
        sidebar.add(itemViewRoutes);
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));
        sidebar.add(buildDivider());

        // ── STUDENT MANAGEMENT ──────────────────────────────────────────────
        sidebar.add(buildSectionLabel("STUDENT MANAGEMENT"));

        itemStudentRequests = new SidebarItem("\uD83D\uDCCB", "Student Requests");
        itemStudentLookup   = new SidebarItem("\uD83D\uDD0D", "Student Lookup");

        itemStudentRequests.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemStudentLookup.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(itemStudentRequests);
        sidebar.add(Box.createRigidArea(new Dimension(0, 2)));
        sidebar.add(itemStudentLookup);
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));
        sidebar.add(buildDivider());

        // ── SESSION ─────────────────────────────────────────────────────────
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildSectionLabel("SESSION"));

        itemLogout = new SidebarItem("\uD83D\uDEAA", "Logout");
        itemLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(itemLogout);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(buildVersionBadge());

        // Wire clicks
        itemAddRoute.setOnClick(() -> activateItem(itemAddRoute, "addRoute"));
        itemAssignDriver.setOnClick(() -> {
            assignDriverPanel.loadRoutes();
            activateItem(itemAssignDriver, "assignDriver");
        });
        itemViewRoutes.setOnClick(() -> {
            viewRoutesPanel.loadData();
            activateItem(itemViewRoutes, "viewRoutes");
        });
        itemStudentRequests.setOnClick(() -> {
            approvalPanel.loadData();
            activateItem(itemStudentRequests, "studentRequests");
        });
        itemStudentLookup.setOnClick(() -> activateItem(itemStudentLookup, "studentLookup"));
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
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;

        JLabel icon = new JLabel("\u2B21");
        icon.setFont(new Font("Dialog", Font.PLAIN, 18));
        icon.setForeground(new Color(0xA5B4FC));
        c.insets = new Insets(0, 0, 2, 8);
        panel.add(icon, c);

        c.gridx = 1;
        JLabel name = new JLabel("UniTransport");
        name.setFont(AppFonts.SIDEBAR);
        name.setForeground(Color.WHITE);
        c.insets = new Insets(0, 0, 2, 0);
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
        JLabel v = new JLabel("Admin Panel  \u00b7  v2.0.0");
        v.setFont(new Font(AppFonts.BODY.getFamily(), Font.PLAIN, 11));
        v.setForeground(new Color(0x818CF8));
        p.add(v);
        return p;
    }

    // ── Main area ────────────────────────────────────────────────────────────

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
        c.fill   = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.gridx = 0;

        c.gridy = 0; c.insets = new Insets(0, 0, 4, 0);
        JLabel title = new JLabel("Transport Network Setup");
        title.setFont(AppFonts.TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);
        header.add(title, c);

        c.gridy = 1;
        JPanel subRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        subRow.setOpaque(false);
        JLabel sub1 = new JLabel("Admin Dashboard");
        sub1.setFont(AppFonts.BODY);
        sub1.setForeground(AppColors.TEXT_SECONDARY);

        JLabel dot = new JLabel("  \u00b7  ");
        dot.setFont(AppFonts.BODY);
        dot.setForeground(AppColors.TEXT_MUTED);

        JLabel sub2 = new JLabel("Admin Module");
        sub2.setFont(AppFonts.BODY);
        sub2.setForeground(AppColors.ACCENT);

        JLabel dbBadge = buildDbBadge();
        subRow.add(sub1); subRow.add(dot); subRow.add(sub2);
        subRow.add(Box.createHorizontalStrut(20)); subRow.add(dbBadge);
        header.add(subRow, c);

        return header;
    }

    private JLabel buildDbBadge() {
        boolean dbOk = false;
        try { DatabaseConnection.getInstance(); dbOk = true; } catch (SQLException ignored) {}
        final String statusText = dbOk ? "\u25cf DB Connected"    : "\u25cf DB Disconnected";
        final Color  badgeColor = dbOk ? AppColors.SUCCESS        : AppColors.ERROR;
        JLabel badge = new JLabel(statusText) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 80));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setFont(new Font(AppFonts.BODY.getFamily(), Font.BOLD, 11));
        badge.setForeground(badgeColor);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        return badge;
    }

    private JPanel buildContentArea() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(40, 48, 40, 48));

        addRoutePanel     = new AddRoutePanel(this);
        assignDriverPanel = new AssignDriverPanel(this);
        viewRoutesPanel   = new AdminViewRoutesPanel(this);
        approvalPanel     = new AdminApprovalPanel(this);
        lookupPanel       = new AdminStudentLookupPanel(this);

        switcher = new AnimatedPanelSwitcher();
        switcher.addPanel("addRoute",        addRoutePanel);
        switcher.addPanel("assignDriver",    assignDriverPanel);
        switcher.addPanel("viewRoutes",      viewRoutesPanel);
        switcher.addPanel("studentRequests", approvalPanel);
        switcher.addPanel("studentLookup",   lookupPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0; gbc.gridy  = 0;
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        content.add(switcher, gbc);

        return content;
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private void activateItem(SidebarItem target, String panelKey) {
        itemAddRoute.setActive(false);
        itemAssignDriver.setActive(false);
        itemViewRoutes.setActive(false);
        itemStudentRequests.setActive(false);
        itemStudentLookup.setActive(false);
        itemLogout.setActive(false);
        target.setActive(true);
        switcher.switchTo(panelKey);
    }

    private void handleLogout() {
        new LoginScreen();
        dispose();
    }
}
