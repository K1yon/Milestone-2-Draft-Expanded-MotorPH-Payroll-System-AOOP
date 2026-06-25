package motorph.model;

public class WorkHours {

    private final String date;
    private final int    regularHours;
    private final int    overtimeHours;
    private final String employeeNumber;

    private String logIn;
    private String logOut;

    public WorkHours(String date, int regularHours,
                     int overtimeHours, String employeeNumber) {
        this.date           = date;
        this.regularHours   = regularHours;
        this.overtimeHours  = overtimeHours;
        this.employeeNumber = employeeNumber;
    }

    public String getDate()           { return date;           }
    public int    getRegularHours()   { return regularHours;   }
    public int    getOvertimeHours()  { return overtimeHours;  }
    public String getEmployeeNumber() { return employeeNumber; }

    public String getLogIn()           { return logIn;   }
    public String getLogOut()          { return logOut;  }
    public void   setLogIn(String t)   { this.logIn  = t; }
    public void   setLogOut(String t)  { this.logOut = t; }
}