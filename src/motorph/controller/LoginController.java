package motorph.controller;

import motorph.model.Employee;
import motorph.model.Role;
import motorph.model.User;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoginController {

    public static final String HR_MANAGER_ID = "10006";
    private static final int   MAX_ATTEMPTS  = 3;

    private final DbAuthenticator    authService;
    private final EmployeeController employeeController;
    private       int                attemptCount = 0;

    // Role keyword mapping — add new position keywords here without touching deriveRole()
    private static final Map<String, Role> ROLE_KEYWORDS = new LinkedHashMap<>();
    static {
        ROLE_KEYWORDS.put("chief executive",  Role.ADMIN);
        ROLE_KEYWORDS.put("chief operating",  Role.ADMIN);
        ROLE_KEYWORDS.put("chief finance",    Role.FINANCE);
        ROLE_KEYWORDS.put("payroll",          Role.FINANCE);
        ROLE_KEYWORDS.put("accounting",       Role.FINANCE);
        ROLE_KEYWORDS.put("it operations",    Role.IT);
        ROLE_KEYWORDS.put("human resource",   Role.HR_ADMIN);
        ROLE_KEYWORDS.put("hr",               Role.HR_ADMIN);
    }

    public LoginController() {
        this.authService        = new DbAuthenticator();
        this.employeeController = new EmployeeController();
    }

    /**
     * Handles login authentication and session creation.
     * Attempt counter only increments on failure.
     * Counter resets automatically on successful login.
     *
     * @param employeeNumber the employee number entered at login
     * @param password       the password entered at login
     * @return true if authentication succeeded, false otherwise
     */
    public boolean handleLogin(String employeeNumber, String password) {
        if (isLocked()) return false;

        boolean ok = authService.authenticate(employeeNumber, password);

        if (ok) {
            // Successful login — reset counter and create session
            attemptCount = 0;
            Employee profile = employeeController.findByNumber(employeeNumber);
            Role role = deriveRole(profile);
            UserSession.getInstance().login(new User(employeeNumber, role, profile));
        } else {
            // Failed login — only increment on actual failure
            attemptCount++;
        }

        return ok;
    }

    /**
     * Maps an employee's position title to a system Role.
     * Role assignment is keyword-based using the ROLE_KEYWORDS map.
     * Add new position keywords to ROLE_KEYWORDS without changing this method.
     *
     * @param profile the employee whose position determines the role
     * @return the matching Role, defaults to EMPLOYEE if no match found
     */
    private Role deriveRole(Employee profile) {
        if (profile == null || profile.getPosition() == null)
            return Role.EMPLOYEE;
        String position = profile.getPosition().toLowerCase();
        for (Map.Entry<String, Role> entry : ROLE_KEYWORDS.entrySet()) {
            if (position.contains(entry.getKey()))
                return entry.getValue();
        }
        return Role.EMPLOYEE;
    }

    public void    resetAttempts()      { attemptCount = 0; }
    public boolean isLocked()           { return attemptCount >= MAX_ATTEMPTS; }
    public int     getAttemptCount()    { return attemptCount; }
    public int     getMaxAttempts()     { return MAX_ATTEMPTS; }
    public EmployeeController getEmployeeController() { return employeeController; }
}