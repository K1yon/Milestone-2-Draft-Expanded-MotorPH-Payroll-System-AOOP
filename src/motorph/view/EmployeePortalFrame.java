package motorph.view;

import motorph.service.PayslipService;
import motorph.model.ClockResult;
import motorph.model.Employee;
import motorph.model.LeaveRequest;
import motorph.model.Payslip;
import motorph.model.User;
import motorph.model.WorkHours;
import motorph.service.LeaveService;
import motorph.ui.AppTheme;
import motorph.util.AttendanceReader;
import motorph.util.PayrollCalculator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployeePortalFrame extends BaseAppFrame {

    private static final DateTimeFormatter DATE_DISPLAY =
        DateTimeFormatter.ofPattern("MMMM d, yyyy");

    private final User              user;
    private final Employee          employee;
    private final LeaveService      leaveService      = new LeaveService();
    private final AttendanceReader  attendanceReader  = new AttendanceReader();
    private final PayrollCalculator payrollCalculator = new PayrollCalculator();
    private final PayslipService payslipService = new PayslipService();

    private JPanel     cardPanel;
    private CardLayout cardLayout;

    public EmployeePortalFrame(User user) {
        super("MotorPH \u2014 Employee Portal", 1100, 700);
        this.user     = user;
        this.employee = user.getEmployeeProfile();
        initFrame();
    }

    @Override
    protected void buildNavItems() {
        sidebarNavPanel.add(navItem("\uD83D\uDC64", "My Profile", true,  () -> cardLayout.show(cardPanel, "profile")));
        sidebarNavPanel.add(navItem("\uD83D\uDCB0", "Payroll",    false, () -> cardLayout.show(cardPanel, "payroll")));
        sidebarNavPanel.add(navItem("\uD83D\uDCCB", "Leave",      false, () -> cardLayout.show(cardPanel, "leave")));
        sidebarNavPanel.add(navItem("\uD83D\uDD50", "Attendance", false, this::openAttendanceDialog));
    }

    @Override
    protected JPanel buildContent() {
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(AppTheme.CONTENT_BG);
        cardPanel.add(buildProfileCard(), "profile");
        cardPanel.add(buildPayrollCard(), "payroll");
        cardPanel.add(buildLeaveCard(),   "leave");
        cardLayout.show(cardPanel, "profile");
        return cardPanel;
    }

    @Override
    protected String getUserAvatarText() {
        String f = employee != null && employee.getFirstName() != null ? employee.getFirstName() : "";
        String l = employee != null && employee.getLastName()  != null ? employee.getLastName()  : "";
        return ((f.isEmpty() ? "" : f.substring(0, 1))
              + (l.isEmpty() ? "" : l.substring(0, 1))).toUpperCase();
    }

    @Override
    protected String getUserDisplayName() {
        return employee != null ? employee.getFullName() : user.getEmployeeNumber();
    }

    @Override
    protected String getUserSubtitle() {
        return employee != null ? employee.getPosition() : user.getRole().getDisplayName();
    }

    private void openAttendanceDialog() {
        JDialog dialog = new JDialog(this, "Attendance \u2014 Time In / Time Out", true);
        dialog.setSize(640, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        AttendanceReader ar = new AttendanceReader();

        JLabel lblStatus   = new JLabel("Loading...", SwingConstants.CENTER);
        lblStatus.setFont(AppTheme.FONT_BOLD_SM);
        JLabel lblClockIn  = AppTheme.mutedLabel("Clock In:  --:--");
        JLabel lblClockOut = AppTheme.mutedLabel("Clock Out: --:--");

        JButton btnIn  = AppTheme.primaryButton("Time In");
        JButton btnOut = AppTheme.actionButton("Time Out", AppTheme.BRAND_BLUE_DARK);
        btnIn.setPreferredSize(new Dimension(160, 38));
        btnOut.setPreferredSize(new Dimension(160, 38));

        String[] cols = {"Date", "Time In", "Time Out", "Hours Worked"};
        DefaultTableModel historyModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable histTable = new JTable(historyModel);
        AppTheme.styleTable(histTable);

        Runnable refreshStatus = () -> {
            String clockIn  = ar.getTodayClockIn(user.getEmployeeNumber());
            String clockOut = ar.getTodayClockOut(user.getEmployeeNumber());
            if (clockIn == null) {
                lblStatus.setText("Not clocked in today");
                lblStatus.setForeground(AppTheme.TEXT_MUTED);
                lblClockIn.setText("Clock In:  --:--");
                lblClockOut.setText("Clock Out: --:--");
                btnIn.setEnabled(true);
                btnOut.setEnabled(false);
            } else if (clockOut == null) {
                lblStatus.setText("Currently clocked in");
                lblStatus.setForeground(AppTheme.GREEN_TEXT);
                lblClockIn.setText("Clock In:  " + clockIn);
                lblClockOut.setText("Clock Out: --:--");
                btnIn.setEnabled(false);
                btnOut.setEnabled(true);
            } else {
                lblStatus.setText("Attendance complete for today");
                lblStatus.setForeground(AppTheme.BRAND_BLUE);
                lblClockIn.setText("Clock In:  " + clockIn);
                lblClockOut.setText("Clock Out: " + clockOut);
                btnIn.setEnabled(false);
                btnOut.setEnabled(false);
            }
        };

        Runnable loadHistory = () -> {
            historyModel.setRowCount(0);
            List<WorkHours> all = ar.getAllForEmployee(user.getEmployeeNumber());
            int start = Math.max(0, all.size() - 20);
            for (int i = all.size() - 1; i >= start; i--) {
                WorkHours w   = all.get(i);
                int total     = w.getRegularHours() + w.getOvertimeHours();
                String logIn  = w.getLogIn()  != null ? w.getLogIn()  : "\u2014";
                String logOut = w.getLogOut() != null ? w.getLogOut() : "\u2014";
                historyModel.addRow(new Object[]{
                    w.getDate(), logIn, logOut,
                    total + " hrs" + (w.getOvertimeHours() > 0
                        ? " (" + w.getOvertimeHours() + " OT)" : "")
                });
            }
        };

        refreshStatus.run();
        loadHistory.run();

        btnIn.addActionListener(e -> {
            String lastName  = employee != null ? employee.getLastName()  : "";
            String firstName = employee != null ? employee.getFirstName() : "";
            String timeStr   = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));

ClockResult result = ar.clockIn(user.getEmployeeNumber(), lastName, firstName, timeStr);
if (result.isFailure())
    JOptionPane.showMessageDialog(dialog, result.getMessage(), "Cannot Clock In", JOptionPane.WARNING_MESSAGE);
else
    JOptionPane.showMessageDialog(dialog, "Time In recorded successfully!", "Clocked In", JOptionPane.INFORMATION_MESSAGE);
            
            refreshStatus.run();
            loadHistory.run();
        });

        btnOut.addActionListener(e -> {
            String timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));
     
ClockResult result = ar.clockOut(user.getEmployeeNumber(), timeStr);
if (result.isFailure())
    JOptionPane.showMessageDialog(dialog, result.getMessage(), "Cannot Clock Out", JOptionPane.WARNING_MESSAGE);
else
    JOptionPane.showMessageDialog(dialog, "Time Out recorded successfully!", "Clocked Out", JOptionPane.INFORMATION_MESSAGE);
            refreshStatus.run();
            loadHistory.run();
        });

        JPanel clockCard = AppTheme.card();
        clockCard.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 20, 10, 20);
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        clockCard.add(lblStatus, g);
        g.gridy = 1; g.gridwidth = 1; g.gridx = 0;
        clockCard.add(lblClockIn, g);
        g.gridx = 1;
        clockCard.add(lblClockOut, g);
        g.gridy = 2; g.gridx = 0;
        clockCard.add(btnIn, g);
        g.gridx = 1;
        clockCard.add(btnOut, g);

        JPanel histCard = AppTheme.card();
        histCard.setLayout(new BorderLayout());
        histCard.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel histTitle = AppTheme.boldLabel("Recent Attendance History");
        histTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        histCard.add(histTitle,                  BorderLayout.NORTH);
        histCard.add(new JScrollPane(histTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(AppTheme.CARD_WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_LIGHT));

        if (user.canRevertAttendance()) {
            JButton btnRevert = AppTheme.actionButton("Revert to Original CSV", AppTheme.BRAND_RED);
            btnRevert.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                    "This will restore attendance_record.csv to its original backup.\nAll clock-in/out entries after the backup will be lost.\n\nContinue?",
                    "Confirm Revert", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
                String err = ar.revertToBackup();
                if (err != null)
                    JOptionPane.showMessageDialog(dialog, err, "Revert Failed", JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(dialog, "Attendance CSV reverted to original backup.", "Reverted", JOptionPane.INFORMATION_MESSAGE);
                refreshStatus.run();
                loadHistory.run();
            });
            footer.add(btnRevert);
        }

        JButton btnClose = AppTheme.actionButton("Close", new Color(100, 100, 110));
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnClose);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(AppTheme.CONTENT_BG);
        center.setBorder(new EmptyBorder(16, 20, 0, 20));
        center.add(clockCard, BorderLayout.NORTH);
        center.add(histCard,  BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.CONTENT_BG);
        root.add(AppTheme.pageHeader("Attendance",
            "Today: " + LocalDate.now().format(DATE_DISPLAY)), BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JPanel buildProfileCard() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(AppTheme.CONTENT_BG);
        page.add(AppTheme.pageHeader("My Profile",
            "Your personal and employment information"), BorderLayout.NORTH);

        JPanel cols = new JPanel(new GridLayout(1, 2, 20, 0));
        cols.setBackground(AppTheme.CONTENT_BG);
        cols.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel left = verticalStack();
        left.add(infoCard("Personal Information", new String[][]{
            {"Employee ID",   employee.getEmployeeNumber()},
            {"Full Name",     employee.getFullName()},
            {"Birthday",      employee.getBirthday()},
            {"Phone",         employee.getPhoneNumber()},
            {"Address",       employee.getAddress()},
        }));
        left.add(Box.createVerticalStrut(16));
        left.add(infoCard("Government IDs", new String[][]{
            {"SSS Number",     employee.getSssNumber()},
            {"PhilHealth No.", employee.getPhilhealthNumber()},
            {"TIN",            employee.getTinNumber()},
            {"Pag-IBIG No.",   employee.getPagibigNumber()},
        }));

        JPanel right = verticalStack();
        right.add(infoCard("Employment", new String[][]{
            {"Position",   employee.getPosition()},
            {"Status",     employee.getStatus()},
            {"Supervisor", employee.getImmediateSupervisor()},
        }));
        right.add(Box.createVerticalStrut(16));
        right.add(infoCard("Compensation", new String[][]{
            {"Basic Salary",       fmt(employee.getBasicSalary())},
            {"Hourly Rate",        fmt(employee.getHourlyRate())},
            {"Rice Subsidy",       fmt(employee.getRiceSubsidy())},
            {"Phone Allowance",    fmt(employee.getPhoneAllowance())},
            {"Clothing Allowance", fmt(employee.getClothingAllowance())},
            {"Gross Semi-monthly", fmt(employee.getGrossSemiMonthlyRate())},
        }));

        cols.add(scrollOf(left));
        cols.add(scrollOf(right));
        page.add(cols, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildPayrollCard() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(AppTheme.CONTENT_BG);
        page.add(AppTheme.pageHeader("Payroll",
            "View your monthly salary computation"), BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controls.setBackground(AppTheme.CARD_WHITE);
        controls.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1),
            new EmptyBorder(14, 20, 14, 20)
        ));

        JComboBox<String> monthCombo = new JComboBox<>(new String[]{
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        });
        monthCombo.setFont(AppTheme.FONT_BODY);
        monthCombo.setPreferredSize(new Dimension(160, 36));

        JButton computeBtn = AppTheme.primaryButton("Compute Payroll");
        computeBtn.setPreferredSize(new Dimension(160, 36));

        controls.add(AppTheme.boldLabel("Select Month:"));
        controls.add(monthCombo);
        controls.add(computeBtn);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(AppTheme.FONT_MONO);
        area.setBackground(new Color(22, 24, 34));
        area.setForeground(new Color(185, 235, 185));
        area.setBorder(new EmptyBorder(20, 20, 20, 20));
        area.setText("\n   Select a month and click 'Compute Payroll'.");

        JScrollPane scrollArea = new JScrollPane(area);
        scrollArea.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1));

computeBtn.addActionListener(e -> {
    String month = (String) monthCombo.getSelectedItem();
    int monthNum = monthCombo.getSelectedIndex() + 1;

    Payslip slip = payrollCalculator.buildPayslip(
        employee,
        month,
        attendanceReader.getAttendanceForMonth(
            employee.getEmployeeNumber(),
            monthNum
        )
    );

    payslipService.saveGeneratedPayslip(
        slip,
        LocalDate.of(LocalDate.now().getYear(), monthNum, 1),
        LocalDate.of(
            LocalDate.now().getYear(),
            monthNum,
            LocalDate.of(LocalDate.now().getYear(), monthNum, 1)
                     .lengthOfMonth()
        )
    );

    area.setText(slip.toFormattedText());
    area.setCaretPosition(0);
});

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.CONTENT_BG);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.add(controls,   BorderLayout.NORTH);
        content.add(scrollArea, BorderLayout.CENTER);

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildLeaveCard() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(AppTheme.CONTENT_BG);
        page.add(AppTheme.pageHeader("Leave Requests",
            "Submit and track your leave requests"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppTheme.CARD_WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1),
            new EmptyBorder(18, 20, 18, 20)
        ));

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            "Sick Leave", "Vacation Leave", "Emergency Leave",
            "Maternity Leave", "Paternity Leave"
        });
        typeCombo.setFont(AppTheme.FONT_BODY);
        typeCombo.setPreferredSize(new Dimension(200, 34));

        JTextField fromField = new JTextField(12);
        JTextField toField   = new JTextField(12);
        fromField.setFont(AppTheme.FONT_BODY);
        toField.setFont(AppTheme.FONT_BODY);
        fromField.setToolTipText("YYYY-MM-DD");
        toField.setToolTipText("YYYY-MM-DD");

        JTextArea descArea = new JTextArea(3, 40);
        descArea.setFont(AppTheme.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1),
            new EmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(500, 70));
        descScroll.setBorder(BorderFactory.createEmptyBorder());

        JButton submitBtn = AppTheme.primaryButton("Submit Request");
        submitBtn.setPreferredSize(new Dimension(150, 36));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        gc.gridy = 0; gc.gridx = 0; gc.gridwidth = 6;
        JLabel formTitle = AppTheme.boldLabel("Leave Request Form");
        formTitle.setFont(AppTheme.FONT_HEADING);
        form.add(formTitle, gc);

        gc.gridy = 1; gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;
        gc.gridx = 0; form.add(AppTheme.boldLabel("Leave Type:"), gc);
        gc.gridx = 1; form.add(typeCombo, gc);
        gc.gridx = 2; form.add(AppTheme.boldLabel("From (YYYY-MM-DD):"), gc);
        gc.gridx = 3; form.add(fromField, gc);
        gc.gridx = 4; form.add(AppTheme.boldLabel("To:"), gc);
        gc.gridx = 5; form.add(toField, gc);

        gc.gridy = 2; gc.gridx = 0; gc.gridwidth = 6;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 8, 2, 8);
        form.add(AppTheme.boldLabel("Reason / Description:  \u26A0 Required"), gc);

        gc.gridy = 3; gc.insets = new Insets(0, 8, 8, 8);
        form.add(descScroll, gc);

        gc.gridy = 4; gc.gridx = 5; gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.EAST;
        gc.insets = new Insets(4, 8, 4, 8);
        form.add(submitBtn, gc);

        String[] cols = {"Leave Type", "Dates", "Reason", "Status"};
        DefaultTableModel leaveModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable leaveTable = new JTable(leaveModel);
        AppTheme.styleTable(leaveTable);
        leaveTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        leaveTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        leaveTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        leaveTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        leaveTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!sel) {
                    setBackground(row % 2 == 0 ? AppTheme.CARD_WHITE : AppTheme.TABLE_STRIPE);
                    if (col == 3 && v != null) {
                        String s = v.toString();
                        setForeground(s.equals("APPROVED") ? AppTheme.GREEN_TEXT
                                   : s.equals("DENIED")   ? AppTheme.RED_TEXT
                                   :                        AppTheme.AMBER_TEXT);
                        setFont(AppTheme.FONT_BOLD_SM);
                    } else {
                        setForeground(AppTheme.TEXT_PRIMARY);
                        setFont(AppTheme.FONT_BODY);
                    }
                }
                return this;
            }
        });

        JPanel historyCard = new JPanel(new BorderLayout(0, 8));
        historyCard.setBackground(AppTheme.CARD_WHITE);
        historyCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1),
            new EmptyBorder(14, 14, 14, 14)
        ));
        historyCard.add(AppTheme.boldLabel("My Leave History"), BorderLayout.NORTH);
        JScrollPane tableScroll = new JScrollPane(leaveTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        historyCard.add(tableScroll, BorderLayout.CENTER);

        Runnable reload = () -> {
            leaveModel.setRowCount(0);
            try {
                for (LeaveRequest r :
                        leaveService.getRequestsByEmployee(employee.getEmployeeNumber())) {
                    String type   = r.getLeaveType();
                    String dates  = "";
                    String reason = r.getDescription();
                    if (type.contains("(") && type.contains(")")) {
                        int s  = type.indexOf('(');
                        int e2 = type.indexOf(')');
                        dates = type.substring(s + 1, e2).trim();
                        type  = type.substring(0, s).trim();
                    }
                    leaveModel.addRow(new Object[]{ type, dates, reason, r.getStatusString() });
                }
            } catch (Exception ex) {
                System.err.println("[Portal] Could not load leave history: " + ex.getMessage());
            }
        };
        reload.run();

        submitBtn.addActionListener(e -> {
            String from = fromField.getText().trim();
            String to   = toField.getText().trim();
            String desc = descArea.getText().trim();
            if (from.isEmpty() || to.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter both From and To dates.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "A reason/description is required for your leave request.",
                    "Description Required", JOptionPane.WARNING_MESSAGE);
                descArea.requestFocus();
                return;
            }
            try {
                leaveService.submitRequest(employee.getEmployeeNumber(),
                    (String) typeCombo.getSelectedItem(), from, to, desc);
                JOptionPane.showMessageDialog(this, "Leave request submitted successfully!");
                fromField.setText("");
                toField.setText("");
                descArea.setText("");
                reload.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.CONTENT_BG);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.add(form,        BorderLayout.NORTH);
        content.add(historyCard, BorderLayout.CENTER);

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel infoCard(String title, String[][] rows) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppTheme.CARD_WHITE);
        card.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        JLabel titleLabel = AppTheme.boldLabel(title);
        titleLabel.setFont(AppTheme.FONT_HEADING);
        titleLabel.setBorder(new EmptyBorder(12, 16, 8, 16));
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        card.add(titleLabel, gc);
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.BORDER_LIGHT);
        gc.gridy = 1;
        card.add(sep, gc);
        gc.gridwidth = 1;
        for (int i = 0; i < rows.length; i++) {
            gc.gridy   = i + 2;
            gc.gridx   = 0; gc.weightx = 0.35;
            gc.insets  = new Insets(4, 16, 4, 8);
            card.add(AppTheme.mutedLabel(rows[i][0]), gc);
            gc.gridx   = 1; gc.weightx = 0.65;
            gc.insets  = new Insets(4, 0, 4, 16);
            card.add(AppTheme.boldLabel(orDash(rows[i][1])), gc);
        }
        return card;
    }

    private JPanel verticalStack() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        return p;
    }

    private JScrollPane scrollOf(JPanel inner) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(inner, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrapper);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setOpaque(false);
        return sp;
    }

    private String fmt(double v)    { return String.format("\u20B1%,.2f", v); }
    private String orDash(String s) { return (s != null && !s.isBlank()) ? s : "\u2014"; }
}