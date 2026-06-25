package motorph.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "work_hours")
public class WorkHoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_number", nullable = false, length = 20)
    private String employeeNumber;

    @Column(name = "work_date", nullable = false, length = 20)
    private String date;

    @Column(name = "log_in", length = 10)
    private String logIn;

    @Column(name = "log_out", length = 10)
    private String logOut;

    @Column(name = "regular_hours")
    private int regularHours;

    @Column(name = "overtime_hours")
    private int overtimeHours;

    public WorkHoursEntity() {}

    public WorkHoursEntity(String employeeNumber, String date,
                           String logIn, String logOut,
                           int regularHours, int overtimeHours) {
        this.employeeNumber = employeeNumber;
        this.date           = date;
        this.logIn          = logIn;
        this.logOut         = logOut;
        this.regularHours   = regularHours;
        this.overtimeHours  = overtimeHours;
    }

    public Long   getId()               { return id; }
    public String getEmployeeNumber()   { return employeeNumber; }
    public String getDate()             { return date; }
    public String getLogIn()            { return logIn; }
    public String getLogOut()           { return logOut; }
    public int    getRegularHours()     { return regularHours; }
    public int    getOvertimeHours()    { return overtimeHours; }
    public void   setLogOut(String v)   { this.logOut = v; }
    public void   setRegularHours(int v)  { this.regularHours = v; }
    public void   setOvertimeHours(int v) { this.overtimeHours = v; }

    public WorkHours toWorkHours() {
        WorkHours wh = new WorkHours(date, regularHours, overtimeHours, employeeNumber);
        wh.setLogIn(logIn);
        wh.setLogOut(logOut);
        return wh;
    }
}