package motorph.model;

/**
 *Defines all system permissions used for role-based access control.
 * To ass a new permission: add it here and assign it to the relevant
 * roles in Role.java - no other code needs to change
 */

public enum Permission {
    PROCESS_PAYROLL,
    MANAGE_EMPLOYEES,
    APPROVE_LEAVES,
    REVERT_ATTENDANCE,   
}
