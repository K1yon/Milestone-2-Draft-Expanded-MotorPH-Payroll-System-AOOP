package motorph.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "leave_requests")
public class LeaveRequestEntity {

    @Id
    @Column(name = "request_id", length = 36)
    private String requestId;

    @Column(name = "employee_id", nullable = false, length = 20)
    private String employeeId;

    @Column(name = "leave_type", length = 80)
    private String leaveType;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private LeaveRequest.LeaveStatus status;

    public LeaveRequestEntity() {}

    public static LeaveRequestEntity from(LeaveRequest r) {
        LeaveRequestEntity e = new LeaveRequestEntity();
        e.requestId   = r.getRequestId();
        e.employeeId  = r.getEmployeeId();
        e.leaveType   = r.getLeaveType();
        e.description = r.getDescription();
        e.status      = r.getStatus();
        return e;
    }

    public LeaveRequest toLeaveRequest() {
        return new LeaveRequest(requestId, employeeId, leaveType,
                                description, status.name());
    }

    public String                   getRequestId()   { return requestId; }
    public String                   getEmployeeId()  { return employeeId; }
    public LeaveRequest.LeaveStatus getStatus()      { return status; }
    public void setStatus(LeaveRequest.LeaveStatus s){ this.status = s; }
}