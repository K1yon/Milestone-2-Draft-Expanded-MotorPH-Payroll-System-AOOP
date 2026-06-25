package motorph.model;

public class LeaveRequest {

    public enum LeaveStatus {
        PENDING, APPROVED, DENIED;

        public static LeaveStatus fromString(String s) {
            if (s == null) return PENDING;
            switch (s.toUpperCase()) {
                case "APPROVED": return APPROVED;
                case "DENIED":   return DENIED;
                default:         return PENDING;
            }
        }
    }

    private final String      requestId;
    private final String      employeeId;
    private final String      leaveType;   
    private final String      description;  
    private       LeaveStatus status;

    public LeaveRequest(String requestId, String employeeId,
                        String leaveType, String description, String statusString) {
        this.requestId   = requestId;
        this.employeeId  = employeeId;
        this.leaveType   = leaveType;
        this.description = description != null ? description : "";
        this.status      = LeaveStatus.fromString(statusString);
    }

    public LeaveRequest(String requestId, String employeeId,
                        String leaveType, String statusString) {
        this(requestId, employeeId, leaveType, "", statusString);
    }

    public String      getRequestId()  { return requestId; }
    public String      getEmployeeId() { return employeeId; }
    public String      getLeaveType()  { return leaveType; }
    public String      getDescription(){ return description; }
    public LeaveStatus getStatus()     { return status; }

    public String getStatusString()    { return status.name(); }

    public void setStatus(LeaveStatus status) { this.status = status; }
    public void setStatus(String statusString){ this.status = LeaveStatus.fromString(statusString); }

    public boolean isPending() { return status == LeaveStatus.PENDING; }

    @Override
    public String toString() {
        return String.format("[%s] Emp#%s | %s | %s",
                requestId.substring(0, 8), employeeId, leaveType, status.name());
    }
}