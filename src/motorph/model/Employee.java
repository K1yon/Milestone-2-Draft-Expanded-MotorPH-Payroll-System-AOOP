package motorph.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee extends Person {

    public static final int ADDRESS_MAX_LENGTH = 100;

    @Id
    @Column(name = "employee_number", nullable = false, unique = true, length = 20)
    private String employeeNumber;

    @Column(name = "address", length = 100)
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "sss_number", length = 30)
    private String sssNumber;

    @Column(name = "philhealth_number", length = 30)
    private String philhealthNumber;

    @Column(name = "tin_number", length = 30)
    private String tinNumber;

    @Column(name = "pagibig_number", length = 30)
    private String pagibigNumber;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "position", length = 80)
    private String position;

    @Column(name = "immediate_supervisor", length = 80)
    private String immediateSupervisor;

    @Column(name = "basic_salary")
    private double basicSalary;

    @Column(name = "rice_subsidy")
    private double riceSubsidy;

    @Column(name = "phone_allowance")
    private double phoneAllowance;

    @Column(name = "clothing_allowance")
    private double clothingAllowance;

    @Column(name = "gross_semi_monthly_rate")
    private double grossSemiMonthlyRate;

    @Column(name = "hourly_rate")
    private double hourlyRate;

    public Employee(String employeeNumber,
                    String lastName, String firstName, String birthday,
                    String address, String phoneNumber,
                    String sssNumber, String philhealthNumber,
                    String tinNumber, String pagibigNumber,
                    String status, String position, String immediateSupervisor,
                    double basicSalary, double riceSubsidy,
                    double phoneAllowance, double clothingAllowance,
                    double grossSemiMonthlyRate, double hourlyRate) {
        super(firstName, lastName, birthday);
        this.employeeNumber      = employeeNumber;
        this.address             = truncateAddress(address);
        this.phoneNumber         = phoneNumber;
        this.sssNumber           = sssNumber;
        this.philhealthNumber    = philhealthNumber;
        this.tinNumber           = tinNumber;
        this.pagibigNumber       = pagibigNumber;
        this.status              = status;
        this.position            = position;
        this.immediateSupervisor = immediateSupervisor;
        this.basicSalary         = basicSalary;
        this.riceSubsidy         = riceSubsidy;
        this.phoneAllowance      = phoneAllowance;
        this.clothingAllowance   = clothingAllowance;
        this.grossSemiMonthlyRate = basicSalary / 2.0;
        this.hourlyRate           = this.grossSemiMonthlyRate / 96.0;
    }

    public Employee(String employeeNumber,
                    String lastName, String firstName, String birthday,
                    String address, String phoneNumber,
                    String sssNumber, String philhealthNumber,
                    String tinNumber, String pagibigNumber,
                    String status, String position, String immediateSupervisor,
                    double basicSalary) {
        this(employeeNumber, lastName, firstName, birthday,
             address, phoneNumber,
             sssNumber, philhealthNumber, tinNumber, pagibigNumber,
             status, position, immediateSupervisor,
             basicSalary, 0, 0, 0,
             basicSalary / 2.0, (basicSalary / 2.0) / 96.0);
    }

    public Employee(String employeeNumber, String lastName, String firstName) {
        super(firstName, lastName, "");
        this.employeeNumber = employeeNumber;
    }

    public Employee() { super(); }

    private void recalcDerivedSalary() {
        this.grossSemiMonthlyRate = basicSalary / 2.0;
        this.hourlyRate           = this.grossSemiMonthlyRate / 96.0;
    }

    private static String truncateAddress(String addr) {
        if (addr == null) return "";
        return addr.length() > ADDRESS_MAX_LENGTH
               ? addr.substring(0, ADDRESS_MAX_LENGTH)
               : addr;
    }

    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public String getEmployeeNumber()              { return employeeNumber; }
    public void   setEmployeeNumber(String v)      { this.employeeNumber = v; }
    public String getAddress()                     { return address; }
    public void   setAddress(String v)             { this.address = truncateAddress(v); }
    public String getPhoneNumber()                 { return phoneNumber; }
    public void   setPhoneNumber(String v)         { this.phoneNumber = v; }
    public String getSssNumber()                   { return sssNumber; }
    public void   setSssNumber(String v)           { this.sssNumber = v; }
    public String getPhilhealthNumber()            { return philhealthNumber; }
    public void   setPhilhealthNumber(String v)    { this.philhealthNumber = v; }
    public String getTinNumber()                   { return tinNumber; }
    public void   setTinNumber(String v)           { this.tinNumber = v; }
    public String getPagibigNumber()               { return pagibigNumber; }
    public void   setPagibigNumber(String v)       { this.pagibigNumber = v; }
    public String getStatus()                      { return status; }
    public void   setStatus(String v)              { this.status = v; }
    public String getPosition()                    { return position; }
    public void   setPosition(String v)            { this.position = v; }
    public String getImmediateSupervisor()         { return immediateSupervisor; }
    public void   setImmediateSupervisor(String v) { this.immediateSupervisor = v; }
    public double getBasicSalary()                 { return basicSalary; }
    public void   setBasicSalary(double v) {
        this.basicSalary = v;
        recalcDerivedSalary();
    }
    public double getRiceSubsidy()                 { return riceSubsidy; }
    public void   setRiceSubsidy(double v)         { this.riceSubsidy = v; }
    public double getPhoneAllowance()              { return phoneAllowance; }
    public void   setPhoneAllowance(double v)      { this.phoneAllowance = v; }
    public double getClothingAllowance()           { return clothingAllowance; }
    public void   setClothingAllowance(double v)   { this.clothingAllowance = v; }
    public double getGrossSemiMonthlyRate()        { return grossSemiMonthlyRate; }
    public double getHourlyRate()                  { return hourlyRate; }

    @Override
    public String toString() {
        return String.format("Employee[%s] %s — %s",
                             employeeNumber, getFullName(), position);
    }
}