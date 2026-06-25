package motorph.util;

import motorph.model.Employee;
import motorph.model.Payslip;
import motorph.model.WorkHours;
import java.util.List;

public class PayrollCalculator {

    // ── Work hour constants ──────────────────────────────────────────────────
    private static final double OVERTIME_RATE = 1.25;  // OT is paid at 1.25x hourly rate

    // ── PhilHealth constants (2024 schedule) ────────────────────────────────
    private static final double PHILHEALTH_RATE     = 0.05;     // 5% of covered salary
    private static final double PHILHEALTH_SHARE    = 0.50;     // employee pays 50%
    private static final double PHILHEALTH_MIN_BASE = 10_000;   // minimum covered salary
    private static final double PHILHEALTH_MAX_BASE = 100_000;  // maximum covered salary

    // ── Pag-IBIG constants ──────────────────────────────────────────────────
    private static final double PAGIBIG_LOW_RATE  = 0.01;   // 1% for salary <= 1,500
    private static final double PAGIBIG_HIGH_RATE = 0.02;   // 2% for salary > 1,500
    private static final double PAGIBIG_THRESHOLD = 1_500;

    // ── BIR withholding tax brackets (annualized) ───────────────────────────
    private static final double TAX_BRACKET_1 =   250_000;
    private static final double TAX_BRACKET_2 =   400_000;
    private static final double TAX_BRACKET_3 =   800_000;
    private static final double TAX_BRACKET_4 = 2_000_000;
    private static final double TAX_BRACKET_5 = 8_000_000;
    private static final double TAX_BASE_2    =    22_500;
    private static final double TAX_BASE_3    =   102_500;
    private static final double TAX_BASE_4    =   402_500;
    private static final double TAX_BASE_5    = 2_202_500;

    // ── SSS contribution bracket table {salary ceiling, contribution} ────

    private static final double[][] SSS_BRACKETS = {
        {  4250, 180.00}, {  4750, 202.50}, {  5250, 225.00}, {  5750, 247.50},
        {  6250, 270.00}, {  6750, 292.50}, {  7250, 315.00}, {  7750, 337.50},
        {  8250, 360.00}, {  8750, 382.50}, {  9250, 405.00}, {  9750, 427.50},
        {10250,  450.00}, {10750,  472.50}, {11250,  495.00}, {11750,  517.50},
        {12250,  540.00}, {12750,  562.50}, {13250,  585.00}, {13750,  607.50},
        {14250,  630.00}, {14750,  652.50}, {15250,  675.00}, {15750,  697.50},
        {16250,  720.00}, {16750,  742.50}, {17250,  765.00}, {17750,  787.50},
        {18250,  810.00}, {18750,  832.50}, {19250,  855.00}, {19750,  877.50}
    };
    private static final double SSS_MAX_CONTRIBUTION = 900.00;

    // ────────────────────────────────────────────────────────────────────────

    /**
     * Builds a complete payslip for the given employee and month.
     *
     * Overtime is paid at 1.25x the regular hourly rate.
     * Government deductions (SSS, PhilHealth, Pag-IBIG) are based on basic salary.
     * Withholding tax is computed on monthly gross pay using annualized BIR brackets.
     *
     * @param emp        the employee whose payslip is being computed
     * @param monthName  display name of the month (e.g. "June")
     * @param attendance list of WorkHours records for the selected month
     * @return a fully computed Payslip value object
     */
    public Payslip buildPayslip(Employee emp, String monthName, List<WorkHours> attendance) {
        int regularHrs = 0, overtimeHrs = 0;
        for (WorkHours wh : attendance) {
            regularHrs  += wh.getRegularHours();
            overtimeHrs += wh.getOvertimeHours();
        }

        if (attendance.isEmpty()) {
            return new Payslip(
                emp.getEmployeeNumber(), emp.getFullName(), monthName,
                0, 0, 0, 0,
                0, 0, 0,
                0, 0, 0, 0
            );
        }

        double regularPay  = regularHrs  * emp.getHourlyRate();
        double overtimePay = overtimeHrs * emp.getHourlyRate() * OVERTIME_RATE;
        double grossPay    = regularPay + overtimePay + emp.getTotalAllowances();

        return new Payslip(
            emp.getEmployeeNumber(), emp.getFullName(), monthName,
            regularHrs, overtimeHrs, regularPay, overtimePay,
            emp.getRiceSubsidy(), emp.getPhoneAllowance(), emp.getClothingAllowance(),
            calculateSSS(emp.getBasicSalary()),
            calculatePhilHealth(emp.getBasicSalary()),
            calculatePagIbig(emp.getBasicSalary()),
            calculateWithholdingTax(grossPay)
        );
    }

    /**
     * Computes SSS monthly contribution using the 2024 bracket table.
     * Salary below ₱4,250 contributes ₱180.00; maximum is ₱900.00.
     * Update SSS_BRACKETS above when the government schedule changes.
     *
     * @param salary the employee's basic monthly salary
     * @return the SSS contribution amount
     */
    public double calculateSSS(double salary) {
        for (double[] bracket : SSS_BRACKETS) {
            if (salary < bracket[0]) return bracket[1];
        }
        return SSS_MAX_CONTRIBUTION;
    }

    /**
     * Computes the employee share of PhilHealth contribution.
     * Rate: 5% of covered salary, employee pays 50% of that.
     * Covered salary is capped between ₱10,000 and ₱100,000.
     *
     * @param salary the employee's basic monthly salary
     * @return the PhilHealth contribution amount
     */
    public double calculatePhilHealth(double salary) {
        double coveredSalary = Math.max(PHILHEALTH_MIN_BASE,
                               Math.min(salary, PHILHEALTH_MAX_BASE));
        return coveredSalary * PHILHEALTH_RATE * PHILHEALTH_SHARE;
    }

    /**
     * Computes the employee Pag-IBIG contribution.
     * Rate: 1% for salary up to ₱1,500; 2% above ₱1,500.
     *
     * @param salary the employee's basic monthly salary
     * @return the Pag-IBIG contribution amount
     */
    public double calculatePagIbig(double salary) {
        return salary <= PAGIBIG_THRESHOLD
               ? salary * PAGIBIG_LOW_RATE
               : salary * PAGIBIG_HIGH_RATE;
    }

    /**
     * Computes monthly withholding tax using annualized BIR tax brackets.
     * Monthly gross is multiplied by 12 to get the annual figure,
     * tax is computed on the annual amount, then divided by 12.
     *
     * @param monthlyGross the employee's total monthly gross pay
     * @return the monthly withholding tax amount
     */
    public double calculateWithholdingTax(double monthlyGross) {
        double annual = monthlyGross * 12;
        double tax;

        if      (annual <= TAX_BRACKET_1) tax = 0;
        else if (annual <= TAX_BRACKET_2) tax = (annual - TAX_BRACKET_1) * 0.15;
        else if (annual <= TAX_BRACKET_3) tax = TAX_BASE_2 + (annual - TAX_BRACKET_2) * 0.20;
        else if (annual <= TAX_BRACKET_4) tax = TAX_BASE_3 + (annual - TAX_BRACKET_3) * 0.25;
        else if (annual <= TAX_BRACKET_5) tax = TAX_BASE_4 + (annual - TAX_BRACKET_4) * 0.30;
        else                              tax = TAX_BASE_5 + (annual - TAX_BRACKET_5) * 0.35;

        return tax / 12;
    }
}