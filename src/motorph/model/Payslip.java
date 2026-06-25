package motorph.model;
public class Payslip {

    private final String employeeNumber;
    private final String employeeName;
    private final String monthName;
    private final int    regularHours;
    private final int    overtimeHours;
    private final double regularPay;
    private final double overtimePay;
    private final double riceSubsidy;
    private final double phoneAllowance;
    private final double clothingAllowance;
    private final double sssDeduction;
    private final double philhealthDeduction;
    private final double pagibigDeduction;
    private final double withholdingTax;
    private final double grossPay;
    private final double totalDeductions;
    private final double netPay;

    public Payslip(String employeeNumber, String employeeName, String monthName,
                   int regularHours, int overtimeHours,
                   double regularPay, double overtimePay,
                   double riceSubsidy, double phoneAllowance, double clothingAllowance,
                   double sssDeduction, double philhealthDeduction,
                   double pagibigDeduction, double withholdingTax) {
        this.employeeNumber      = employeeNumber;
        this.employeeName        = employeeName;
        this.monthName           = monthName;
        this.regularHours        = regularHours;
        this.overtimeHours       = overtimeHours;
        this.regularPay          = regularPay;
        this.overtimePay         = overtimePay;
        this.riceSubsidy         = riceSubsidy;
        this.phoneAllowance      = phoneAllowance;
        this.clothingAllowance   = clothingAllowance;
        this.sssDeduction        = sssDeduction;
        this.philhealthDeduction = philhealthDeduction;
        this.pagibigDeduction    = pagibigDeduction;
        this.withholdingTax      = withholdingTax;
        this.grossPay        = regularPay + overtimePay + riceSubsidy + phoneAllowance + clothingAllowance;
        this.totalDeductions = sssDeduction + philhealthDeduction + pagibigDeduction + withholdingTax;
        this.netPay          = grossPay - totalDeductions;
    }

    public String getEmployeeNumber()      { return employeeNumber; }
    public String getEmployeeName()        { return employeeName; }
    public String getMonthName()           { return monthName; }
    public int    getRegularHours()        { return regularHours; }
    public int    getOvertimeHours()       { return overtimeHours; }
    public int    getTotalHours()          { return regularHours + overtimeHours; }
    public double getRegularPay()          { return regularPay; }
    public double getOvertimePay()         { return overtimePay; }
    public double getRiceSubsidy()         { return riceSubsidy; }
    public double getPhoneAllowance()      { return phoneAllowance; }
    public double getClothingAllowance()   { return clothingAllowance; }
    public double getTotalAllowances()     { return riceSubsidy + phoneAllowance + clothingAllowance; }
    public double getSssDeduction()        { return sssDeduction; }
    public double getPhilhealthDeduction() { return philhealthDeduction; }
    public double getPagibigDeduction()    { return pagibigDeduction; }
    public double getWithholdingTax()      { return withholdingTax; }
    public double getGrossPay()            { return grossPay; }
    public double getTotalDeductions()     { return totalDeductions; }
    public double getNetPay()              { return netPay; }

    public String toFormattedText() {
        String LINE  = "─".repeat(56);
        String DLINE = "═".repeat(56);
        return String.format(
            "\n  %-54s\n  PAYSLIP  —  %s\n  Employee : %s  (#%s)\n  %s\n\n" +
            "  ATTENDANCE\n    Regular Hours       :  %d hrs\n" +
            "    Overtime Hours      :  %d hrs\n    Total Hours Worked  :  %d hrs\n\n" +
            "  %s\n  EARNINGS\n  %s\n" +
            "    Regular Pay         :  ₱%,12.2f\n    Overtime Pay        :  ₱%,12.2f\n" +
            "    Rice Subsidy        :  ₱%,12.2f\n    Phone Allowance     :  ₱%,12.2f\n" +
            "    Clothing Allowance  :  ₱%,12.2f\n  %s\n    GROSS PAY           :  ₱%,12.2f\n\n" +
            "  %s\n  DEDUCTIONS\n  %s\n" +
            "    SSS Contribution    :  ₱%,12.2f\n    PhilHealth          :  ₱%,12.2f\n" +
            "    Pag-IBIG            :  ₱%,12.2f\n    Withholding Tax     :  ₱%,12.2f\n" +
            "  %s\n    Total Deductions    :  ₱%,12.2f\n\n" +
            "  %s\n  ★  NET PAY            :  ₱%,12.2f\n  %s\n",
            DLINE, monthName, employeeName, employeeNumber, LINE,
            regularHours, overtimeHours, getTotalHours(),
            LINE, LINE, regularPay, overtimePay, riceSubsidy, phoneAllowance, clothingAllowance,
            LINE, grossPay, LINE, LINE, sssDeduction, philhealthDeduction, pagibigDeduction, withholdingTax,
            LINE, totalDeductions, DLINE, netPay, DLINE
        );
    }

    @Override
    public String toString() {
        return String.format("Payslip[Emp#%s | %s | Net: ₱%,.2f]", employeeNumber, monthName, netPay);
    }
}