package motorph.model;

public class User {
    private final String   employeeNumber;
    private final Role     role;
    private final Employee employeeProfile;

    public User(String employeeNumber, Role role, Employee employeeProfile) {
        this.employeeNumber  = employeeNumber;
        this.role            = role;
        this.employeeProfile = employeeProfile;
    }

    public boolean isHRAdmin()           { return role == Role.HR_ADMIN; }
    public boolean isFinance()           { return role == Role.FINANCE;  }
    public boolean isIT()                { return role == Role.IT;       }
    public boolean isAdmin()             { return role == Role.ADMIN;    }
    public boolean isRegularEmployee()   { return role == Role.EMPLOYEE; }

    public boolean canManageEmployees()  { return role.canManageEmployees();  }
    public boolean canApproveLeaves()    { return role.canApproveLeaves();    }
    public boolean canProcessPayroll()   { return role.canProcessPayroll();   }
    public boolean canRevertAttendance() { return role.canRevertAttendance(); }

    public String   getEmployeeNumber()  { return employeeNumber; }
    public Role     getRole()            { return role; }
    public Employee getEmployeeProfile() { return employeeProfile; }

    public String getDisplayName() {
        return employeeProfile != null
            ? employeeProfile.getFirstName() + " " + employeeProfile.getLastName()
            : employeeNumber;
    }

    @Override
    public String toString() {
        return String.format("User[%s] %s (%s)", employeeNumber, getDisplayName(), role.getDisplayName());
    }
}