package motorph.model;

import java.util.EnumSet;
import java.util.Set;

public enum Role {

    HR_ADMIN ("HR Manager",
        EnumSet.of(Permission.MANAGE_EMPLOYEES, Permission.APPROVE_LEAVES)),
    FINANCE  ("Finance",
        EnumSet.of(Permission.PROCESS_PAYROLL)),
    IT       ("IT",
        EnumSet.of(Permission.MANAGE_EMPLOYEES, Permission.REVERT_ATTENDANCE)),
    ADMIN    ("Admin",
        EnumSet.allOf(Permission.class)),
    EMPLOYEE ("Employee",
        EnumSet.noneOf(Permission.class));

    private final String          displayName;
    private final Set<Permission> permissions;

    Role(String displayName, Set<Permission> permissions) {
        this.displayName = displayName;
        this.permissions = permissions;
    }

    /**
     * Checks whether this role has a specific permission.
     * Add new permissions to the Permission enum and assign them
     * to roles above without changing this method.
     *
     * @param permission the permission to check
     * @return true if this role has the given permission
     */
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    // Existing permission methods kept for backward compatibility
    public boolean canProcessPayroll()   { return hasPermission(Permission.PROCESS_PAYROLL);   }
    public boolean canManageEmployees()  { return hasPermission(Permission.MANAGE_EMPLOYEES);  }
    public boolean canApproveLeaves()    { return hasPermission(Permission.APPROVE_LEAVES);    }
    public boolean canRevertAttendance() { return hasPermission(Permission.REVERT_ATTENDANCE); }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}