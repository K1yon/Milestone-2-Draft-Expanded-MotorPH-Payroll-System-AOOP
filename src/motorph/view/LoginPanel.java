package motorph.view;

import motorph.controller.LoginController;
import motorph.controller.UserSession;
import motorph.model.User;
import motorph.ui.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel extends JPanel {

    private final JFrame          parentFrame;
    private final LoginController loginController;

    private JTextField     txtEmpNumber;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JCheckBox      chkShowPw;
    private JLabel         lblStatus;

    public LoginPanel(JFrame parentFrame) {
        this.parentFrame     = parentFrame;
        this.loginController = new LoginController();
        setLayout(new BorderLayout());
        setBackground(AppTheme.CONTENT_BG);
        buildUI();
    }

    private void buildUI() {
        add(buildBrandPanel(), BorderLayout.WEST);
        add(buildFormPanel(),  BorderLayout.CENTER);
    }

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, AppTheme.BRAND_BLUE, 0, getHeight(), AppTheme.BRAND_BLUE_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(getWidth() - 110, -90, 220, 220);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(getWidth() - 50, getHeight() - 100, 180, 180);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(360, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.gridy  = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel emoji   = new JLabel("\uD83C\uDFCD");
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        emoji.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel brand   = styledLabel("MotorPH", new Font("Segoe UI", Font.BOLD, 32), Color.WHITE);
        JLabel tagline = styledLabel("Payroll Management System",
            new Font("Segoe UI", Font.PLAIN, 14), new Color(255, 200, 200));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 50));
        sep.setPreferredSize(new Dimension(220, 1));

        JLabel desc = new JLabel(
            "<html><div style='text-align:center;color:#FFCCCC;"
            + "font-size:11px;line-height:1.6'>Streamlined payroll processing<br>"
            + "for MotorPH employees</div></html>");
        desc.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.insets = new Insets(4, 20, 4, 20);
        panel.add(emoji,   gbc);
        panel.add(brand,   gbc);
        panel.add(tagline, gbc);

        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(14, 20, 14, 20);
        panel.add(sep, gbc);

        gbc.fill   = GridBagConstraints.NONE;
        gbc.insets = new Insets(4, 20, 4, 20);
        panel.add(desc, gbc);

        JLabel version = styledLabel("v2.0", AppTheme.FONT_SMALL, new Color(255, 180, 180));
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);
        gbc.weighty = 0;
        gbc.insets  = new Insets(0, 0, 20, 0);
        panel.add(version, gbc);

        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(AppTheme.CONTENT_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppTheme.CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            new EmptyBorder(40, 40, 40, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.gridy   = GridBagConstraints.RELATIVE;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel title = styledLabel("Welcome Back",         AppTheme.FONT_TITLE, AppTheme.TEXT_PRIMARY);
        JLabel sub   = styledLabel("Sign in to your account", AppTheme.FONT_BODY, AppTheme.TEXT_MUTED);

        gbc.insets = new Insets(0, 0, 3, 0);
        card.add(title, gbc);
        gbc.insets = new Insets(0, 0, 26, 0);
        card.add(sub, gbc);

        txtEmpNumber = new JTextField();
        txtPassword  = new JPasswordField();
        applyFieldStyle(txtEmpNumber, "Employee number (e.g. 10001)");
        applyFieldStyle(txtPassword,  "Password");

        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(AppTheme.boldLabel("Employee Number"), gbc);
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(txtEmpNumber, gbc);

        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(AppTheme.boldLabel("Password"), gbc);
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(txtPassword, gbc);

        chkShowPw = new JCheckBox("Show password");
        chkShowPw.setFont(AppTheme.FONT_SMALL);
        chkShowPw.setForeground(AppTheme.TEXT_MUTED);
        chkShowPw.setBackground(AppTheme.CARD_WHITE);
        chkShowPw.setFocusPainted(false);
        chkShowPw.addActionListener(e ->
            txtPassword.setEchoChar(chkShowPw.isSelected() ? (char) 0 : '\u2022'));
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(chkShowPw, gbc);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(AppTheme.FONT_SMALL);
        lblStatus.setForeground(AppTheme.BRAND_RED);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(lblStatus, gbc);

        btnLogin = AppTheme.primaryButton("Sign In");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(0, 46));
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(btnLogin, gbc);

        JLabel footer = new JLabel(
            "<html><div style='text-align:center;color:#AAAAAA;font-size:10px'>"
            + "\u00A9 2024 MotorPH Corporation. All rights reserved.</div></html>");
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(footer, gbc);

        outer.add(card);

        btnLogin.addActionListener(e    -> performLogin());
        txtPassword.addActionListener(e  -> performLogin());
        txtEmpNumber.addActionListener(e -> txtPassword.requestFocus());

        return outer;
    }

    private void performLogin() {
        String empNum = txtEmpNumber.getForeground().equals(AppTheme.TEXT_MUTED)
            ? "" : txtEmpNumber.getText().trim();
        String password = txtPassword.getForeground().equals(AppTheme.TEXT_MUTED)
            ? "" : new String(txtPassword.getPassword()).trim();

        if (empNum.isEmpty() || password.isEmpty()) {
            showStatus("\u26A0  Please fill in both fields.");
            shakeWindow();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in\u2026");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                return loginController.handleLogin(empNum, password);
            }
            @Override protected void done() {
                try {
                    if (get()) onLoginSuccess();
                    else       onLoginFailure();
               } catch (Exception ex) {
                    ex.printStackTrace();
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showStatus("Error: " + cause.getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");
                }
            }
        };
        worker.execute();
    }

    private void onLoginSuccess() {
        loginController.resetAttempts();
        User user = UserSession.getInstance().getCurrentUser();
        parentFrame.dispose();
        if (user.isHRAdmin() || user.isFinance() || user.isIT() || user.isAdmin()) {
            new HRDashboardFrame(loginController);
        } else {
            new EmployeePortalFrame(user);
        }
    }

    private void onLoginFailure() {
        if (loginController.isLocked()) {
            showStatus("\uD83D\uDD12  Account locked after 3 failed attempts.");
            btnLogin.setEnabled(false);
            btnLogin.setText("Locked");
        } else {
            showStatus(String.format("\u2717  Invalid credentials. Attempt %d of %d.",
                loginController.getAttemptCount(), loginController.getMaxAttempts()));
            btnLogin.setEnabled(true);
            btnLogin.setText("Sign In");
        }
        txtPassword.setText("");
        shakeWindow();
    }

    private void showStatus(String msg) { lblStatus.setText(msg); }

    private void shakeWindow() {
        int[]  dx   = {-10, 10, -8, 8, -5, 5, -2, 2, 0};
        Point  orig = parentFrame.getLocation();
        Timer  t    = new Timer(28, null);
        int[]  idx  = {0};
        t.addActionListener(e -> {
            if (idx[0] < dx.length) {
                parentFrame.setLocation(orig.x + dx[idx[0]++], orig.y);
            } else {
                parentFrame.setLocation(orig);
                ((Timer) e.getSource()).stop();
            }
        });
        t.start();
    }

    private JLabel styledLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private void applyFieldStyle(JTextField field, String placeholder) {
        field.setFont(AppTheme.FONT_BODY);
        field.setPreferredSize(new Dimension(0, 42));
        field.setForeground(AppTheme.TEXT_MUTED);
        field.setText(placeholder);

        Color normalBorder = new Color(210, 215, 225);
        Color focusBorder  = AppTheme.BRAND_BLUE;

        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(normalBorder, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getForeground().equals(AppTheme.TEXT_MUTED)) {
                    if (field instanceof JPasswordField)
                        ((JPasswordField) field).setEchoChar('\u2022');
                    field.setText("");
                    field.setForeground(AppTheme.TEXT_PRIMARY);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(focusBorder, 2, true),
                    new EmptyBorder(7, 11, 7, 11)
                ));
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    if (field instanceof JPasswordField)
                        ((JPasswordField) field).setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(AppTheme.TEXT_MUTED);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(normalBorder, 1, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    private static class ShadowBorder extends AbstractBorder {
        private static final int SHADOW = 8;
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = SHADOW; i >= 0; i--) {
                float alpha = 0.04f * (SHADOW - i);
                g2.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
                g2.setStroke(new BasicStroke(i));
                g2.draw(new RoundRectangle2D.Float(x+i, y+i, w-i*2, h-i*2, 12, 12));
            }
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(SHADOW, SHADOW, SHADOW, SHADOW);
        }
    }
}