package motorph.view;

import java.time.LocalDate;
import motorph.service.PayslipService;
import motorph.controller.EmployeeController;
import motorph.controller.LoginController;
import motorph.controller.UserSession;
import motorph.model.Employee;
import motorph.model.LeaveRequest;
import motorph.model.Payslip;
import motorph.model.User;
import motorph.service.LeaveService;
import motorph.ui.AppTheme;
import motorph.util.AttendanceReader;
import motorph.util.PayrollCalculator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class HRDashboardFrame extends BaseAppFrame {

    private final LoginController    loginController;
    private final EmployeeController employeeController;
    private final User               currentUser;
    private final PayslipService payslipService = new PayslipService();

    private List<Employee>                    employeeList = new ArrayList<>();
    private DefaultTableModel                 tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTable                            table;
    private JTextField                        searchField;
    private JComboBox<String>                 positionCombo;
    private JComboBox<String>                 statusCombo;
    private JButton                           btnView, btnNew, btnEdit, btnDelete;
    private JLabel                            lblCount;

    private static class LengthFilter extends DocumentFilter {
        private final int max;
        LengthFilter(int max) { this.max = max; }

        @Override
        public void insertString(FilterBypass fb, int offset, String text,
                AttributeSet attr) throws BadLocationException {
            if (text == null) return;
            if (fb.getDocument().getLength() + text.length() <= max)
                super.insertString(fb, offset, text, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text,
                AttributeSet attr) throws BadLocationException {
            if (text == null) text = "";
            if (fb.getDocument().getLength() - length + text.length() <= max)
                super.replace(fb, offset, length, text, attr);
        }
    }

    /** Creates a JTextField with optional character limit and tooltip. */
    private JTextField limitedField(String value, int maxLen, String tooltip) {
        JTextField f = new JTextField(value != null ? value : "");
        f.setFont(AppTheme.FONT_BODY);
        f.setPreferredSize(new Dimension(280, 32));
        if (maxLen > 0)
            ((AbstractDocument) f.getDocument()).setDocumentFilter(new LengthFilter(maxLen));
        if (tooltip != null && !tooltip.isEmpty())
            f.setToolTipText(tooltip);
        return f;
    }

    public HRDashboardFrame(LoginController loginController) {
        super("MotorPH \u2014 HR Dashboard", 1360, 780);
        this.loginController    = loginController;
        this.employeeController = loginController.getEmployeeController();
        this.currentUser        = UserSession.getInstance().getCurrentUser();
        initFrame();
        reloadEmployees();
    }

    @Override
    protected void buildNavItems() {
        sidebarNavPanel.add(navItem("\uD83D\uDC65", "Employees",      true,  null));
        sidebarNavPanel.add(navItem("\uD83D\uDCCB", "Leave Requests", false, this::openLeaveDialog));
    }

    @Override
    protected JPanel buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.CONTENT_BG);
        panel.add(buildTopBar(),    BorderLayout.NORTH);
        panel.add(buildTableCard(), BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected String getUserAvatarText() {
        if (currentUser != null) {
            Employee e = currentUser.getEmployeeProfile();
            if (e != null) {
                String f = e.getFirstName() != null ? e.getFirstName() : "";
                String l = e.getLastName()  != null ? e.getLastName()  : "";
                return ((f.isEmpty() ? "" : f.substring(0, 1))
                      + (l.isEmpty() ? "" : l.substring(0, 1))).toUpperCase();
            }
        }
        return "HR";
    }

    @Override
    protected String getUserDisplayName() {
        if (currentUser != null) {
            Employee e = currentUser.getEmployeeProfile();
            if (e != null) return e.getFullName();
        }
        Employee hr = employeeController.findByNumber(LoginController.HR_MANAGER_ID);
        return hr != null ? hr.getFullName() : "HR Manager";
    }

    @Override
    protected String getUserSubtitle() {
        if (currentUser != null)
            return currentUser.getRole().getDisplayName()
                 + " \u00B7 #" + currentUser.getEmployeeNumber();
        return "HR Manager \u00B7 #10006";
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(20, 0));
        bar.setBackground(AppTheme.CARD_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_LIGHT),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Employee Management");
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        lblCount = AppTheme.mutedLabel("  \u00B7  0 employees");
        left.add(title);
        left.add(lblCount);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        searchField = new JTextField(22);
        searchField.setFont(AppTheme.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        positionCombo = new JComboBox<>();
        positionCombo.addItem("All Positions");
        AppTheme.styleCombo(positionCombo);

        statusCombo = new JComboBox<>();
        statusCombo.addItem("All Statuses");
        AppTheme.styleCombo(statusCombo);

        right.add(new JLabel("\uD83D\uDD0D "));
        right.add(searchField);
        right.add(positionCombo);
        right.add(statusCombo);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyFilters(); }
            public void removeUpdate(DocumentEvent e)  { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        positionCombo.addActionListener(e -> applyFilters());
        statusCombo.addActionListener(e   -> applyFilters());

        return bar;
    }

    private JPanel buildTableCard() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppTheme.CONTENT_BG);
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = AppTheme.card();
        card.setLayout(new BorderLayout());

        String[] cols = {"#", "Employee ID", "Last Name", "First Name",
                         "Position", "Status", "Basic Salary", "Supervisor"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table  = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        AppTheme.styleTable(table);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (sel) {
                    setBackground(AppTheme.ROW_SELECTED);
                    setForeground(AppTheme.TEXT_PRIMARY);
                    setFont(AppTheme.FONT_BODY);
                } else {
                    setBackground(row % 2 == 0 ? AppTheme.CARD_WHITE : AppTheme.TABLE_STRIPE);
                    if (col == 5) {
                        String s = v != null ? v.toString() : "";
                        setForeground(s.equalsIgnoreCase("Regular")     ? AppTheme.GREEN_TEXT
                                   : s.equalsIgnoreCase("Probationary") ? AppTheme.AMBER_TEXT
                                   : AppTheme.TEXT_MUTED);
                        setFont(AppTheme.FONT_BOLD_SM);
                    } else {
                        setForeground(AppTheme.TEXT_PRIMARY);
                        setFont(AppTheme.FONT_BODY);
                    }
                }
                return this;
            }
        });

        int[] widths = {40, 110, 130, 130, 200, 110, 130, 180};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppTheme.CARD_WHITE);
        card.add(scroll,           BorderLayout.CENTER);
        card.add(buildActionBar(), BorderLayout.SOUTH);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openDetailsDialog();
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel       = table.getSelectedRow() >= 0;
            boolean canManage = currentUser != null && currentUser.canManageEmployees();
            btnView.setEnabled(sel);
            btnEdit.setEnabled(sel && canManage);
            btnDelete.setEnabled(sel && canManage);
        });

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppTheme.CARD_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_LIGHT),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel left  = new JPanel(new FlowLayout(FlowLayout.LEFT,  8, 0));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        left.setOpaque(false);
        right.setOpaque(false);

        boolean canManage = currentUser != null && currentUser.canManageEmployees();

        btnView   = AppTheme.actionButton("\uD83D\uDC41  View",   AppTheme.BLUE_BTN);
        btnNew    = AppTheme.actionButton("\uFF0B New",            AppTheme.GREEN_TEXT);
        btnEdit   = AppTheme.actionButton("\u270E  Edit",         AppTheme.AMBER_TEXT);
        btnDelete = AppTheme.actionButton("\uD83D\uDDD1  Delete", AppTheme.RED_TEXT);

        btnView.setEnabled(false);
        btnNew.setEnabled(canManage);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        if (!canManage) {
            String tip = "Requires HR Admin, IT, or Admin role";
            btnNew.setToolTipText(tip);
            btnEdit.setToolTipText(tip);
            btnDelete.setToolTipText(tip);
        }

        btnView.addActionListener(e   -> openDetailsDialog());
        btnNew.addActionListener(e    -> { if (canManage) openNewEmployeeDialog(); });
        btnEdit.addActionListener(e   -> { if (canManage) openEditDialog(); });
        btnDelete.addActionListener(e -> { if (canManage) deleteSelected(); });

        left.add(btnView);
        left.add(btnNew);
        left.add(btnEdit);
        left.add(btnDelete);

        JButton btnLeave = AppTheme.actionButton("\uD83D\uDCCB Leave Requests", new Color(90, 60, 150));
        btnLeave.addActionListener(e -> openLeaveDialog());
        right.add(btnLeave);

        if (currentUser != null && currentUser.canRevertAttendance()) {
            JButton btnRevert = AppTheme.actionButton(
                "\u21BA  Revert Attendance CSV", AppTheme.BRAND_RED);
            btnRevert.setToolTipText("Restore attendance_record.csv to original backup \u2014 IT / Admin only");
            btnRevert.addActionListener(e -> doRevertAttendance());
            right.add(btnRevert);
        }

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void openDetailsDialog() {
        Employee emp = getSelectedEmployee();
        if (emp == null) return;

        AttendanceReader  ar           = new AttendanceReader();
        PayrollCalculator payrollCalc  = new PayrollCalculator();
        LeaveService      leaveService = new LeaveService();

        JDialog dialog = new JDialog(this, "Employee Details \u2014 " + emp.getFullName(), true);
        dialog.setSize(720, 840);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JComboBox<String> monthCombo = new JComboBox<>(new String[]{
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        });
        monthCombo.setFont(AppTheme.FONT_BODY);

        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        Integer[] years = new Integer[6];
        for (int i = 0; i < 6; i++) years[i] = currentYear - i;
        JComboBox<Integer> yearCombo = new JComboBox<>(years);
        yearCombo.setFont(AppTheme.FONT_BODY);
        yearCombo.setToolTipText("Select payroll year");

        JTextArea payrollArea = new JTextArea(14, 0);
        payrollArea.setEditable(false);
        payrollArea.setFont(AppTheme.FONT_MONO);
        payrollArea.setBackground(new Color(22, 24, 34));
        payrollArea.setForeground(new Color(185, 235, 185));
        payrollArea.setBorder(new EmptyBorder(14, 14, 14, 14));
        payrollArea.setText("\n   Select month and year above, then click 'Compute Payslip'.");

        JButton computeBtn = AppTheme.primaryButton("Compute Payslip");
        JButton leaveBtn   = AppTheme.actionButton("\uD83D\uDCCB Leave History", AppTheme.BLUE_BTN);
        JButton closeBtn   = AppTheme.actionButton("\u2715  Close", new Color(100, 100, 110));

computeBtn.addActionListener(e -> {
    String month = (String) monthCombo.getSelectedItem();
    int monthNum = monthCombo.getSelectedIndex() + 1;
    int year = (Integer) yearCombo.getSelectedItem();

    Payslip slip = payrollCalc.buildPayslip(
        emp,
        month + " " + year,
        ar.getAttendanceForMonth(emp.getEmployeeNumber(), monthNum)
    );

    LocalDate startDate = LocalDate.of(year, monthNum, 1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

    payslipService.saveGeneratedPayslip(
        slip,
        startDate,
        endDate
    );

    payrollArea.setText(slip.toFormattedText());
    payrollArea.setCaretPosition(0);
});

        leaveBtn.addActionListener(e -> {
            try {
                List<LeaveRequest> all = leaveService.getRequestsByEmployee(emp.getEmployeeNumber());
                if (all.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "No leave requests on record for this employee.",
                        "Leave History", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String[] lCols = {"Leave Type & Dates", "Reason", "Status"};
                Object[][] data = new Object[all.size()][3];
                for (int i = 0; i < all.size(); i++) {
                    data[i][0] = all.get(i).getLeaveType();
                    data[i][1] = all.get(i).getDescription();
                    data[i][2] = all.get(i).getStatusString();
                }
                JTable lt = new JTable(data, lCols) {
                    public boolean isCellEditable(int r, int c) { return false; }
                };
                AppTheme.styleTable(lt);
                JScrollPane lsp = new JScrollPane(lt);
                lsp.setPreferredSize(new Dimension(560, 220));
                JOptionPane.showMessageDialog(dialog, lsp,
                    "Leave History \u2014 " + emp.getFullName(), JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        String displayName = emp.getFullName().length() > 30
    ? emp.getFullName().substring(0, 30) + "..."
    : emp.getFullName();
JPanel header = AppTheme.pageHeader(displayName,
    "#" + emp.getEmployeeNumber() + "  \u00B7  " + emp.getPosition());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setBackground(AppTheme.CARD_WHITE);
        actions.add(AppTheme.boldLabel("Month:"));
        actions.add(monthCombo);
        actions.add(AppTheme.boldLabel("Year:"));
        actions.add(yearCombo);
        actions.add(computeBtn);
        actions.add(leaveBtn);
        actions.add(closeBtn);
        header.add(actions, BorderLayout.EAST);

        JPanel cards = new JPanel();
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(16, 16, 0, 16));
        cards.add(infoCard("Personal Information", new String[][]{
            {"Employee ID",  emp.getEmployeeNumber()},
            {"Full Name",    emp.getFullName()},
            {"Birthday",     emp.getBirthday()},
            {"Phone",        emp.getPhoneNumber()},
            {"Address",      emp.getAddress()},
        }));
        cards.add(Box.createVerticalStrut(12));
        cards.add(infoCard("Government IDs", new String[][]{
            {"SSS Number",     emp.getSssNumber()},
            {"PhilHealth No.", emp.getPhilhealthNumber()},
            {"TIN",            emp.getTinNumber()},
            {"Pag-IBIG No.",   emp.getPagibigNumber()},
        }));
        cards.add(Box.createVerticalStrut(12));
        cards.add(infoCard("Employment", new String[][]{
            {"Status",     emp.getStatus()},
            {"Position",   emp.getPosition()},
            {"Supervisor", emp.getImmediateSupervisor()},
        }));
        cards.add(Box.createVerticalStrut(12));
        cards.add(infoCard("Compensation", new String[][]{
            {"Basic Salary",       fmt(emp.getBasicSalary())},
            {"Gross Semi-monthly", fmt(emp.getGrossSemiMonthlyRate())},
            {"Hourly Rate",        fmt(emp.getHourlyRate())},
            {"Rice Subsidy",       fmt(emp.getRiceSubsidy())},
            {"Phone Allowance",    fmt(emp.getPhoneAllowance())},
            {"Clothing Allowance", fmt(emp.getClothingAllowance())},
        }));

        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setOpaque(false);
        infoWrapper.add(cards, BorderLayout.NORTH);
        JScrollPane infoScroll = new JScrollPane(infoWrapper);
        infoScroll.setBorder(BorderFactory.createEmptyBorder());
        infoScroll.getViewport().setBackground(AppTheme.CONTENT_BG);

        JScrollPane payScroll = new JScrollPane(payrollArea);
        payScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1));
        payScroll.setPreferredSize(new Dimension(0, 240));
        JPanel payWrapper = new JPanel(new BorderLayout());
        payWrapper.setBorder(new EmptyBorder(8, 16, 16, 16));
        payWrapper.setBackground(AppTheme.CONTENT_BG);
        payWrapper.add(payScroll, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(AppTheme.CONTENT_BG);
        main.add(header,     BorderLayout.NORTH);
        main.add(infoScroll, BorderLayout.CENTER);
        main.add(payWrapper, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setVisible(true);
    }

    private void openNewEmployeeDialog() {
        JDialog dialog = new JDialog(this, "Add New Employee", true);
        dialog.setSize(560, 780);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextField fEmpNum     = limitedField("", 10,  "Digits only  e.g. 10050");
        JTextField fLastName   = limitedField("", 50,  "Employee last name");
        JTextField fFirstName  = limitedField("", 50,  "Employee first name");
        JTextField fBirthday   = limitedField("", 10,  "Format: MM/DD/YYYY  e.g. 06/15/1990");
        JTextField fAddress    = limitedField("", Employee.ADDRESS_MAX_LENGTH,
                                    "Max " + Employee.ADDRESS_MAX_LENGTH + " characters");
        JTextField fPhone      = limitedField("", 20,  "Digits/dashes  e.g. 09171234567");
        JTextField fSSS        = limitedField("", 12,  "Format: ##-#######-#  e.g. 34-5678901-2");
        JTextField fPhilHealth = limitedField("", 14,  "Format: ##-#########-#  e.g. 12-123456789-0");
        JTextField fTIN        = limitedField("", 15,  "Format: ###-###-###-###  e.g. 123-456-789-000");
        JTextField fPagIBIG    = limitedField("", 14,  "Format: ####-####-####  e.g. 1234-5678-9012");
        JTextField fPosition   = limitedField("", 60,  "Job title / position");
        JTextField fSupervisor = limitedField("", 60,  "Immediate supervisor full name");
        JTextField fSalary     = limitedField("", 12,  "Monthly basic salary  e.g. 25000");
        JTextField fRice       = limitedField("", 10,  "Rice subsidy  e.g. 1500");
        JTextField fPhoneAllow = limitedField("", 10,  "Phone allowance  e.g. 500");
        JTextField fClothing   = limitedField("", 10,  "Clothing allowance  e.g. 500");

        JComboBox<String> fStatus = new JComboBox<>(new String[]{"Regular", "Probationary"});
        fStatus.setFont(AppTheme.FONT_BODY);
        fStatus.setToolTipText("Select employment status");

        JTextField fGross  = limitedField("(auto-computed)", 0,
                                "Derived automatically from Basic Salary \u00F7 2");
        JTextField fHourly = limitedField("(auto-computed)", 0,
                                "Derived automatically from Gross Semi-monthly \u00F7 96");
        fGross.setEditable(false);
        fHourly.setEditable(false);
        fGross.setForeground(Color.GRAY);
        fHourly.setForeground(Color.GRAY);

        fSalary.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updateDerived(); }
            public void removeUpdate(DocumentEvent e)  { updateDerived(); }
            public void changedUpdate(DocumentEvent e) { updateDerived(); }
            private void updateDerived() {
                try {
                    double basic = Double.parseDouble(
                        fSalary.getText().trim().replace(",", ""));
                    fGross.setText(String.format("%.2f", basic / 2.0));
                    fHourly.setText(String.format("%.5f", (basic / 2.0) / 96.0));
                } catch (NumberFormatException ex) {
                    fGross.setText("(auto-computed)");
                    fHourly.setText("(auto-computed)");
                }
            }
        });

        // Layout
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppTheme.CARD_WHITE);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(4, 8, 4, 8);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        addSection(form, g,  0, "Identity");
        addRow(form, g,  1, "Employee Number *",   fEmpNum);
        addRow(form, g,  2, "Last Name *",          fLastName);
        addRow(form, g,  3, "First Name *",         fFirstName);
        addRow(form, g,  4, "Birthday",             fBirthday);
        addRow(form, g,  5, "Address",              fAddress);
        addRow(form, g,  6, "Phone Number",         fPhone);
        addSection(form, g,  7, "Government IDs");
        addRow(form, g,  8, "SSS Number",           fSSS);
        addRow(form, g,  9, "PhilHealth Number",    fPhilHealth);
        addRow(form, g, 10, "TIN",                  fTIN);
        addRow(form, g, 11, "Pag-IBIG Number",      fPagIBIG);
        addSection(form, g, 12, "Employment");
        addRow(form, g, 13, "Status *",             fStatus);
        addRow(form, g, 14, "Position *",           fPosition);
        addRow(form, g, 15, "Immediate Supervisor", fSupervisor);
        addSection(form, g, 16, "Compensation");
        addRow(form, g, 17, "Basic Salary *",       fSalary);
        addRow(form, g, 18, "Rice Subsidy",         fRice);
        addRow(form, g, 19, "Phone Allowance",      fPhoneAllow);
        addRow(form, g, 20, "Clothing Allowance",   fClothing);
        addRow(form, g, 21, "Gross Semi-monthly",   fGross);
        addRow(form, g, 22, "Hourly Rate",          fHourly);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(AppTheme.CARD_WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_LIGHT));

        JButton cancelBtn = AppTheme.actionButton("Cancel", new Color(110, 110, 120));
        JButton addBtn    = AppTheme.primaryButton("Add Employee");

        cancelBtn.addActionListener(e -> dialog.dispose());

        addBtn.addActionListener(e -> {
            String empNum     = fEmpNum.getText().trim();
            String lastName   = fLastName.getText().trim();
            String firstName  = fFirstName.getText().trim();
            String birthday   = fBirthday.getText().trim();
            String phone      = fPhone.getText().trim();
            String sss        = fSSS.getText().trim();
            String philHealth = fPhilHealth.getText().trim();
            String tin        = fTIN.getText().trim();
            String pagibig    = fPagIBIG.getText().trim();
            String status     = fStatus.getSelectedItem().toString();
            String position   = fPosition.getText().trim();
            String salaryStr  = fSalary.getText().trim();

            if (empNum.isEmpty() || lastName.isEmpty() || firstName.isEmpty()
                    || position.isEmpty() || salaryStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please fill all required fields (*).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!empNum.matches("\\d+")) {
                JOptionPane.showMessageDialog(dialog,
                    "Employee Number must contain digits only.\nExample: 10050",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fEmpNum.requestFocus(); return;
            }
            if (employeeController.findByNumber(empNum) != null) {
                JOptionPane.showMessageDialog(dialog,
                    "Employee Number " + empNum + " already exists.\nPlease use a unique number.",
                    "Duplicate Employee", JOptionPane.WARNING_MESSAGE);
                fEmpNum.requestFocus(); return;
            }
            if (!birthday.isEmpty() && !birthday.matches("\\d{2}/\\d{2}/\\d{4}")) {
                JOptionPane.showMessageDialog(dialog,
                    "Birthday must be in MM/DD/YYYY format.\nExample: 06/15/1990",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fBirthday.requestFocus(); return;
            }
            if (!phone.isEmpty() && !phone.matches("[\\d\\-\\+\\s()]+")) {
                JOptionPane.showMessageDialog(dialog,
                    "Phone Number must contain digits, dashes, or spaces only.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fPhone.requestFocus(); return;
            }
            if (!sss.isEmpty() && !sss.matches("\\d{2}-\\d{7}-\\d")) {
                JOptionPane.showMessageDialog(dialog,
                    "SSS format: ##-#######-#\nExample: 34-5678901-2",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fSSS.requestFocus(); return;
            }
            if (!philHealth.isEmpty() && !philHealth.matches("\\d{2}-\\d{9}-\\d")) {
                JOptionPane.showMessageDialog(dialog,
                    "PhilHealth format: ##-#########-#\nExample: 12-123456789-0",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fPhilHealth.requestFocus(); return;
            }
            if (!tin.isEmpty() && !tin.matches("\\d{3}-\\d{3}-\\d{3}-\\d{3}")) {
                JOptionPane.showMessageDialog(dialog,
                    "TIN format: ###-###-###-###\nExample: 123-456-789-000",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fTIN.requestFocus(); return;
            }
            if (!pagibig.isEmpty() && !pagibig.matches("\\d{4}-\\d{4}-\\d{4}")) {
                JOptionPane.showMessageDialog(dialog,
                    "Pag-IBIG format: ####-####-####\nExample: 1234-5678-9012",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fPagIBIG.requestFocus(); return;
            }
            double basicSalary;
            try {
                basicSalary = Double.parseDouble(salaryStr.replace(",", ""));
                if (basicSalary <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Basic Salary must be a valid positive number.\nExample: 25000",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fSalary.requestFocus(); return;
            }
            JTextField[] allowFields = {fRice, fPhoneAllow, fClothing};
            String[]     allowNames  = {"Rice Subsidy", "Phone Allowance", "Clothing Allowance"};
            for (int i = 0; i < allowFields.length; i++) {
                String val = allowFields[i].getText().trim();
                if (!val.isEmpty()) {
                    try {
                        if (Double.parseDouble(val.replace(",", "")) < 0)
                            throw new NumberFormatException();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            allowNames[i] + " must be a valid positive number.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                        allowFields[i].requestFocus(); return;
                    }
                }
            }

            try {

                Employee emp = new Employee(
                    empNum, lastName, firstName, birthday,
                    fAddress.getText().trim(), phone,
                    sss, philHealth, tin, pagibig,
                    status, position, fSupervisor.getText().trim(),
                    basicSalary,
                    numField(fRice), numField(fPhoneAllow), numField(fClothing),
                    basicSalary / 2.0, (basicSalary / 2.0) / 96.0
                );
                employeeController.createEmployee(emp);
                reloadEmployees();
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                    firstName + " " + lastName + " has been added successfully!",
                    "Employee Added", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(cancelBtn);
        footer.add(addBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.CONTENT_BG);
        root.add(AppTheme.pageHeader("Add New Employee",
            "Fields marked * are required  \u00B7  Hover over fields for format hints"),
            BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void openLeaveDialog() {
        LeaveService leaveService = new LeaveService();
        List<LeaveRequest>[] requests = new List[1];

        JDialog dialog = new JDialog(this, "Leave Request Management", true);
        dialog.setSize(780, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        String[] cols = {"Employee ID", "Leave Type & Dates", "Reason / Description", "Status"};
        DefaultTableModel leaveModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable leaveTable = new JTable(leaveModel);
        AppTheme.styleTable(leaveTable);
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
        leaveTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        leaveTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        leaveTable.getColumnModel().getColumn(2).setPreferredWidth(280);
        leaveTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        Runnable reload = () -> {
            leaveModel.setRowCount(0);
            try {
                requests[0] = leaveService.getAllRequests();
                for (LeaveRequest r : requests[0]) {
                    leaveModel.addRow(new Object[]{
                        r.getEmployeeId(),
                        r.getLeaveType(),
                        r.getDescription().isEmpty() ? "\u2014" : r.getDescription(),
                        r.getStatusString()
                    });
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to load leave requests: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        reload.run();

        JButton refreshBtn = AppTheme.actionButton("\u21BA  Refresh", AppTheme.BLUE_BTN);
        JButton approveBtn = AppTheme.actionButton("\u2713  Approve", AppTheme.GREEN_TEXT);
        JButton denyBtn    = AppTheme.actionButton("\u2717  Deny",    AppTheme.RED_TEXT);
        JButton closeBtn   = AppTheme.actionButton("Close",           new Color(100, 100, 110));

        refreshBtn.addActionListener(e -> reload.run());
        approveBtn.addActionListener(e -> {
            int row = leaveTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a leave request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                leaveService.updateStatus(requests[0].get(row).getRequestId(),
                    LeaveRequest.LeaveStatus.APPROVED.name());
                reload.run();
                JOptionPane.showMessageDialog(dialog, "Leave request APPROVED.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        denyBtn.addActionListener(e -> {
            int row = leaveTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a leave request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                leaveService.updateStatus(requests[0].get(row).getRequestId(),
                    LeaveRequest.LeaveStatus.DENIED.name());
                reload.run();
                JOptionPane.showMessageDialog(dialog, "Leave request DENIED.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionBar.setBackground(AppTheme.CARD_WHITE);
        actionBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_LIGHT));
        actionBar.add(refreshBtn);
        actionBar.add(approveBtn);
        actionBar.add(denyBtn);
        actionBar.add(closeBtn);

        JPanel card = AppTheme.card();
        card.setLayout(new BorderLayout());
        card.add(new JScrollPane(leaveTable), BorderLayout.CENTER);
        card.add(actionBar,                   BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppTheme.CONTENT_BG);
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        wrapper.add(card, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.CONTENT_BG);
        root.add(AppTheme.pageHeader("Leave Request Management",
            "Review and respond to employee leave requests"), BorderLayout.NORTH);
        root.add(wrapper, BorderLayout.CENTER);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        Employee emp = getSelectedEmployee();
        if (emp == null) return;

        JTextField fFirstName  = limitedField(emp.getFirstName(),  50, "Employee first name");
        JTextField fLastName   = limitedField(emp.getLastName(),   50, "Employee last name");
        JTextField fBirthday   = limitedField(emp.getBirthday(),   10, "Format: MM/DD/YYYY  e.g. 06/15/1990");
        JTextField fAddress    = limitedField(emp.getAddress(),    Employee.ADDRESS_MAX_LENGTH,
                                    "Max " + Employee.ADDRESS_MAX_LENGTH + " characters");
        JTextField fPhone      = limitedField(emp.getPhoneNumber(), 20, "Digits/dashes  e.g. 09171234567");
        JTextField fSSS        = limitedField(emp.getSssNumber(),   12, "Format: ##-#######-#  e.g. 34-5678901-2");
        JTextField fPhilHealth = limitedField(emp.getPhilhealthNumber(), 14, "Format: ##-#########-#  e.g. 12-123456789-0");
        JTextField fTIN        = limitedField(emp.getTinNumber(),   15, "Format: ###-###-###-###  e.g. 123-456-789-000");
        JTextField fPagIBIG    = limitedField(emp.getPagibigNumber(), 14, "Format: ####-####-####  e.g. 1234-5678-9012");
        JTextField fPosition   = limitedField(emp.getPosition(),   60, "Job title / position");
        JTextField fSupervisor = limitedField(emp.getImmediateSupervisor(), 60, "Immediate supervisor name");
        JTextField fSalary     = limitedField(String.valueOf(emp.getBasicSalary()), 12,
                                    "Monthly basic salary");
        JTextField fRice       = limitedField(String.valueOf(emp.getRiceSubsidy()),       10, "e.g. 1500");
        JTextField fPhoneAllow = limitedField(String.valueOf(emp.getPhoneAllowance()),    10, "e.g. 500");
        JTextField fClothing   = limitedField(String.valueOf(emp.getClothingAllowance()), 10, "e.g. 500");

        JTextField fGross  = limitedField(
            String.format("%.2f", emp.getGrossSemiMonthlyRate()), 0,
            "Derived automatically from Basic Salary \u00F7 2");
        JTextField fHourly = limitedField(
            String.format("%.5f", emp.getHourlyRate()), 0,
            "Derived automatically from Gross Semi-monthly \u00F7 96");
        fGross.setEditable(false);
        fHourly.setEditable(false);
        fGross.setForeground(Color.GRAY);
        fHourly.setForeground(Color.GRAY);

        fSalary.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updateDerived(); }
            public void removeUpdate(DocumentEvent e)  { updateDerived(); }
            public void changedUpdate(DocumentEvent e) { updateDerived(); }
            private void updateDerived() {
                try {
                    double basic = Double.parseDouble(
                        fSalary.getText().trim().replace(",", ""));
                    fGross.setText(String.format("%.2f", basic / 2.0));
                    fHourly.setText(String.format("%.5f", (basic / 2.0) / 96.0));
                } catch (NumberFormatException ex) { /* ignore mid-type */ }
            }
        });

        JComboBox<String> fStatus = new JComboBox<>(new String[]{"Regular", "Probationary"});
        fStatus.setFont(AppTheme.FONT_BODY);
        fStatus.setSelectedItem(emp.getStatus());
        fStatus.setToolTipText("Select employment status");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppTheme.CARD_WHITE);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(4, 8, 4, 8);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        addSection(form, g,  0, "Personal Information");
        addRow(form, g,  1, "First Name *",        fFirstName);
        addRow(form, g,  2, "Last Name *",          fLastName);
        addRow(form, g,  3, "Birthday",             fBirthday);
        addRow(form, g,  4, "Address",              fAddress);
        addRow(form, g,  5, "Phone Number",         fPhone);
        addSection(form, g,  6, "Government IDs");
        addRow(form, g,  7, "SSS Number",           fSSS);
        addRow(form, g,  8, "PhilHealth No.",       fPhilHealth);
        addRow(form, g,  9, "TIN",                  fTIN);
        addRow(form, g, 10, "Pag-IBIG No.",         fPagIBIG);
        addSection(form, g, 11, "Employment");
        addRow(form, g, 12, "Position *",           fPosition);
        addRow(form, g, 13, "Status *",             fStatus);
        addRow(form, g, 14, "Supervisor",           fSupervisor);
        addSection(form, g, 15, "Compensation");
        addRow(form, g, 16, "Basic Salary *",       fSalary);
        addRow(form, g, 17, "Rice Subsidy",         fRice);
        addRow(form, g, 18, "Phone Allowance",      fPhoneAllow);
        addRow(form, g, 19, "Clothing Allowance",   fClothing);
        addRow(form, g, 20, "Gross Semi-monthly",   fGross);
        addRow(form, g, 21, "Hourly Rate",          fHourly);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setPreferredSize(new Dimension(540, 500));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JDialog dialog = new JDialog(this, "Edit Employee \u2014 " + emp.getFullName(), true);
        dialog.setSize(580, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(AppTheme.CARD_WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_LIGHT));

        JButton btnCancel = AppTheme.actionButton("Cancel", new Color(100, 100, 110));
        JButton btnSave   = AppTheme.primaryButton("Save Changes");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            if (fFirstName.getText().trim().isEmpty()
             || fLastName.getText().trim().isEmpty()
             || fPosition.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "First Name, Last Name, and Position are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String birthday  = fBirthday.getText().trim();
            String phone     = fPhone.getText().trim();
            String sss       = fSSS.getText().trim();
            String philHealth= fPhilHealth.getText().trim();
            String tin       = fTIN.getText().trim();
            String pagibig   = fPagIBIG.getText().trim();

            if (!birthday.isEmpty() && !birthday.matches("\\d{2}/\\d{2}/\\d{4}")) {
                JOptionPane.showMessageDialog(dialog,
                    "Birthday must be in MM/DD/YYYY format.\nExample: 06/15/1990",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fBirthday.requestFocus(); return;
            }
            if (!phone.isEmpty() && !phone.matches("[\\d\\-\\+\\s()]+")) {
                JOptionPane.showMessageDialog(dialog,
                    "Phone Number must contain digits, dashes, or spaces only.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fPhone.requestFocus(); return;
            }
            if (!sss.isEmpty() && !sss.matches("\\d{2}-\\d{7}-\\d")) {
                JOptionPane.showMessageDialog(dialog,
                    "SSS format: ##-#######-#\nExample: 34-5678901-2",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fSSS.requestFocus(); return;
            }
            if (!philHealth.isEmpty() && !philHealth.matches("\\d{2}-\\d{9}-\\d")) {
                JOptionPane.showMessageDialog(dialog,
                    "PhilHealth format: ##-#########-#\nExample: 12-123456789-0",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fPhilHealth.requestFocus(); return;
            }
            if (!tin.isEmpty() && !tin.matches("\\d{3}-\\d{3}-\\d{3}-\\d{3}")) {
                JOptionPane.showMessageDialog(dialog,
                    "TIN format: ###-###-###-###\nExample: 123-456-789-000",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fTIN.requestFocus(); return;
            }
            if (!pagibig.isEmpty() && !pagibig.matches("\\d{4}-\\d{4}-\\d{4}")) {
                JOptionPane.showMessageDialog(dialog,
                    "Pag-IBIG format: ####-####-####\nExample: 1234-5678-9012",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                fPagIBIG.requestFocus(); return;
            }

            try {
                emp.setFirstName(fFirstName.getText().trim());
                emp.setLastName(fLastName.getText().trim());
                emp.setBirthday(birthday);
                emp.setAddress(fAddress.getText().trim());
                emp.setPhoneNumber(phone);
                emp.setSssNumber(sss);
                emp.setPhilhealthNumber(philHealth);
                emp.setTinNumber(tin);
                emp.setPagibigNumber(pagibig);
                emp.setPosition(fPosition.getText().trim());
                emp.setStatus(fStatus.getSelectedItem().toString());
                emp.setImmediateSupervisor(fSupervisor.getText().trim());
                emp.setRiceSubsidy(parseDouble(fRice.getText()));
                emp.setPhoneAllowance(parseDouble(fPhoneAllow.getText()));
                emp.setClothingAllowance(parseDouble(fClothing.getText()));
                emp.setBasicSalary(parseDouble(fSalary.getText()));
                employeeController.updateEmployee(emp);
                reloadEmployees();
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                    emp.getFullName() + " updated successfully.",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Invalid number in compensation fields.\nUse digits only, e.g. 25000",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.CONTENT_BG);
        root.add(AppTheme.pageHeader("Edit Employee",
            "Employee ID: " + emp.getEmployeeNumber()
            + "  \u00B7  Hover over fields for format hints"), BorderLayout.NORTH);
        root.add(scroll,  BorderLayout.CENTER);
        root.add(footer,  BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void doRevertAttendance() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "This will overwrite attendance_record.csv with the original backup.\n"
            + "All Time In / Time Out entries after the backup will be lost.\n\nAre you sure?",
            "Confirm Revert to Original",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        String err = new AttendanceReader().revertToBackup();
        if (err != null)
            JOptionPane.showMessageDialog(this, err, "Revert Failed", JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(this,
                "attendance_record.csv has been restored to the original backup.",
                "Revert Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelected() {
        Employee emp = getSelectedEmployee();
        if (emp == null) return;
        int c = JOptionPane.showConfirmDialog(this,
            "Delete " + emp.getFullName() + "?\nThis cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            String deletedNumber = emp.getEmployeeNumber();
            employeeController.deleteEmployee(deletedNumber);

            User current = UserSession.getInstance().getCurrentUser();
            if (current != null && current.getEmployeeNumber().equals(deletedNumber)) {
                UserSession.getInstance().logout();
            }

            reloadEmployees();
            JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
        }
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
            gc.gridy = i + 2;
            gc.gridx = 0; gc.weightx = 0.35;
            gc.insets = new Insets(4, 16, 4, 8);
            card.add(AppTheme.mutedLabel(rows[i][0]), gc);
            gc.gridx = 1; gc.weightx = 0.65;
            gc.insets = new Insets(4, 0, 4, 16);
            String val = (rows[i][1] != null && !rows[i][1].isBlank()) ? rows[i][1] : "\u2014";
            card.add(AppTheme.boldLabel(val), gc);
        }
        return card;
    }

    private JTextField field(String value) {
        return limitedField(value, 0, null);
    }

    private void addSection(JPanel form, GridBagConstraints g, int row, String title) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 2;
        g.insets = new Insets(14, 8, 2, 8);
        JPanel sec = new JPanel(new BorderLayout(8, 0));
        sec.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(AppTheme.FONT_HEADING);
        lbl.setForeground(AppTheme.BRAND_BLUE);
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.BORDER_LIGHT);
        sec.add(lbl, BorderLayout.WEST);
        sec.add(sep, BorderLayout.CENTER);
        form.add(sec, g);
        g.gridwidth = 1;
        g.insets = new Insets(4, 8, 4, 8);
    }

    private void addRow(JPanel form, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row; g.gridx = 0; g.weightx = 0.35;
        form.add(AppTheme.boldLabel(label), g);
        g.gridx = 1; g.weightx = 0.65;
        form.add(field, g);
    }

    private double parseDouble(String s) {
        String v = s.trim().replace(",", "");
        if (v.isEmpty() || v.startsWith("(")) return 0.0;
        try { return Double.parseDouble(v); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private double numField(JTextField f) { return parseDouble(f.getText()); }

    private String fmt(double v) { return String.format("\u20B1%,.2f", v); }

    private void reloadEmployees() {
        employeeList = employeeController.loadAllEmployees();
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        int row = 1;
        for (Employee e : employeeList) {
            tableModel.addRow(new Object[]{
                row++,
                e.getEmployeeNumber(),
                e.getLastName(),
                e.getFirstName(),
                e.getPosition(),
                e.getStatus(),
                String.format("\u20B1%,.2f", e.getBasicSalary()),
                e.getImmediateSupervisor()
            });
        }
        lblCount.setText("  \u00B7  " + employeeList.size() + " employees");
        populateFilterCombos();
    }

    private void populateFilterCombos() {
        positionCombo.removeAllItems();
        statusCombo.removeAllItems();
        positionCombo.addItem("All Positions");
        statusCombo.addItem("All Statuses");
        Set<String> positions = new TreeSet<>();
        Set<String> statuses  = new TreeSet<>();
        for (Employee e : employeeList) {
            if (e.getPosition() != null && !e.getPosition().isBlank()) positions.add(e.getPosition());
            if (e.getStatus()   != null && !e.getStatus().isBlank())   statuses.add(e.getStatus());
        }
        positions.forEach(positionCombo::addItem);
        statuses.forEach(statusCombo::addItem);
    }

    private void applyFilters() {
        String search   = searchField.getText().trim();
        String position = (String) positionCombo.getSelectedItem();
        String status   = (String) statusCombo.getSelectedItem();
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!search.isEmpty())
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(search), 1, 2, 3));
        if (position != null && !position.equals("All Positions"))
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(position) + "$", 4));
        if (status != null && !status.equals("All Statuses"))
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(status) + "$", 5));
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private Employee getSelectedEmployee() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        String empId = tableModel.getValueAt(
            table.convertRowIndexToModel(viewRow), 1).toString();
        return employeeList.stream()
            .filter(e -> e.getEmployeeNumber().equals(empId))
            .findFirst().orElse(null);
    }

    public void refreshEmployeeList()                 { reloadEmployees(); }
    public EmployeeController getEmployeeController() { return employeeController; }
}