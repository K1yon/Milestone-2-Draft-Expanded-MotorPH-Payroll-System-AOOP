package motorph.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "payslips")
public class PayslipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payslip_id")
    private Long payslipId;

    @Column(name = "employee_number", nullable = false)
    private String employeeNumber;

    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    @Column(name = "gross_pay", nullable = false)
    private double grossPay;

    @Column(name = "total_deductions", nullable = false)
    private double totalDeductions;

    @Column(name = "net_pay", nullable = false)
    private double netPay;

    @Column(name = "sss_deduction")
    private double sssDeduction;

    @Column(name = "philhealth_deduction")
    private double philhealthDeduction;

    @Column(name = "pagibig_deduction")
    private double pagibigDeduction;

    @Column(name = "withholding_tax")
    private double withholdingTax;


    public PayslipEntity() {
    }

    public PayslipEntity(
            String employeeNumber,
            LocalDate payPeriodStart,
            LocalDate payPeriodEnd,
            double grossPay,
            double totalDeductions,
            double netPay,
            double sssDeduction,
            double philhealthDeduction,
            double pagibigDeduction,
            double withholdingTax) {

        this.employeeNumber = employeeNumber;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
        this.grossPay = grossPay;
        this.totalDeductions = totalDeductions;
        this.netPay = netPay;
        this.sssDeduction = sssDeduction;
        this.philhealthDeduction = philhealthDeduction;
        this.pagibigDeduction = pagibigDeduction;
        this.withholdingTax = withholdingTax;
    }


    public Long getPayslipId() {
        return payslipId;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public LocalDate getPayPeriodStart() {
        return payPeriodStart;
    }

    public LocalDate getPayPeriodEnd() {
        return payPeriodEnd;
    }

    public double getGrossPay() {
        return grossPay;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public double getNetPay() {
        return netPay;
    }

    public double getSssDeduction() {
        return sssDeduction;
    }

    public double getPhilhealthDeduction() {
        return philhealthDeduction;
    }

    public double getPagibigDeduction() {
        return pagibigDeduction;
    }

    public double getWithholdingTax() {
        return withholdingTax;
    }
}