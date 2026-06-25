package motorph.view;

import motorph.ui.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class BaseAppFrame extends JFrame {

    protected JPanel sidebarNavPanel;

    protected BaseAppFrame(String title, int width, int height) {
        setTitle(title);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    protected final void initFrame() {
        setLayout(new BorderLayout(0, 0));
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
        setVisible(true);
    }

    protected abstract void buildNavItems();
    protected abstract JPanel buildContent();
    protected abstract String getUserAvatarText();
    protected abstract String getUserDisplayName();
    protected abstract String getUserSubtitle();

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(215, 0));
        sidebar.setBackground(AppTheme.SIDEBAR_BG);

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logoRow.setBackground(AppTheme.SIDEBAR_BG);
        logoRow.setBorder(new EmptyBorder(24, 4, 24, 4));
        JLabel logoIcon = new JLabel("\uD83C\uDFCD");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        JLabel logoText = new JLabel("MotorPH");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logoText.setForeground(Color.WHITE);
        logoRow.add(logoIcon);
        logoRow.add(logoText);
        sidebar.add(logoRow, BorderLayout.NORTH);

        sidebarNavPanel = new JPanel();
        sidebarNavPanel.setBackground(AppTheme.SIDEBAR_BG);
        sidebarNavPanel.setLayout(new BoxLayout(sidebarNavPanel, BoxLayout.Y_AXIS));
        sidebarNavPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        buildNavItems();
        sidebar.add(sidebarNavPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(20, 22, 32));
        bottom.add(buildUserBadge(),     BorderLayout.CENTER);
        bottom.add(buildSignOutButton(), BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildUserBadge() {
        JPanel badge = new JPanel(new GridBagLayout());
        badge.setBackground(new Color(20, 22, 32));
        badge.setBorder(new EmptyBorder(14, 14, 8, 14));

        JLabel avatar = new JLabel(getUserAvatarText());
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setOpaque(true);
        avatar.setBackground(AppTheme.BRAND_RED);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(getUserDisplayName());
        nameLabel.setFont(AppTheme.FONT_BOLD_SM);
        nameLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel(getUserSubtitle());
        roleLabel.setFont(AppTheme.FONT_SMALL);
        roleLabel.setForeground(AppTheme.TEXT_MUTED);

        info.add(nameLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(roleLabel);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.insets = new Insets(0, 0, 0, 10);
        badge.add(avatar, gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        badge.add(info, gc);

        return badge;
    }

    private JButton buildSignOutButton() {
        JButton btn = new JButton("\u23FB  Sign Out");
        btn.setFont(AppTheme.FONT_SMALL);
        btn.setForeground(new Color(200, 180, 180));
        btn.setBackground(new Color(20, 22, 32));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 14, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.addActionListener(e -> signOut());
        return btn;
    }

    protected void signOut() {
        motorph.controller.UserSession.getInstance().logout();
        dispose();
        new MainFrame().setVisible(true);
    }

    protected JPanel navItem(String icon, String label, boolean active, Runnable onClick) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        item.setPreferredSize(new Dimension(215, 44));
        item.setBackground(active ? AppTheme.BRAND_RED : AppTheme.SIDEBAR_BG);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconL = new JLabel(icon);
        iconL.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JLabel textL = new JLabel(label);
        textL.setFont(AppTheme.FONT_BOLD_SM);
        textL.setForeground(active ? Color.WHITE : new Color(180, 185, 200));

        item.add(iconL);
        item.add(textL);

        Color normalBg = active ? AppTheme.BRAND_RED      : AppTheme.SIDEBAR_BG;
        Color hoverBg  = active ? AppTheme.BRAND_DARK_RED : AppTheme.SIDEBAR_HOVER;

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { item.setBackground(hoverBg);  }
            public void mouseExited(MouseEvent e)  { item.setBackground(normalBg); }
            public void mouseClicked(MouseEvent e) { if (!active && onClick != null) onClick.run(); }
        });

        return item;
    }
}