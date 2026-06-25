package motorph.service;

import motorph.model.LeaveRequest;
import motorph.model.LeaveRequest.LeaveStatus;
import motorph.model.LeaveRequestEntity;
import motorph.repository.LeaveRequestRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeaveService {

    private final LeaveRequestRepository repo = new LeaveRequestRepository();

    /**
     * Submits a new leave request with PENDING status.
     * Validates all required fields before saving to the database.
     * Leave type and date range are combined into a display string for storage.
     *
     * @throws IllegalArgumentException if any required field is null or empty
     */
    public void submitRequest(String employeeId, String leaveType,
                              String fromDate, String toDate,
                              String description) {
        if (employeeId == null || employeeId.trim().isEmpty())
            throw new IllegalArgumentException("Employee ID is required.");
        if (leaveType == null || leaveType.trim().isEmpty())
            throw new IllegalArgumentException("Leave type is required.");
        if (fromDate == null || fromDate.trim().isEmpty())
            throw new IllegalArgumentException("From date is required.");
        if (toDate == null || toDate.trim().isEmpty())
            throw new IllegalArgumentException("To date is required.");

        String displayType = leaveType + " (" + fromDate + " to " + toDate + ")";
        LeaveRequest req = new LeaveRequest(
                UUID.randomUUID().toString(),
                employeeId.trim(),
                displayType,
                description != null ? description : "",
                LeaveStatus.PENDING.name()
        );
        repo.save(LeaveRequestEntity.from(req));
    }

    /**
     * Submits a leave request without a description.
     * Delegates to the full submitRequest method with an empty description.
     */
    public void submitRequest(String employeeId, String leaveType,
                              String fromDate, String toDate) {
        submitRequest(employeeId, leaveType, fromDate, toDate, "");
    }

    /**
     * Returns all leave requests from the database.
     */
    public List<LeaveRequest> getAllRequests() {
        return repo.findAll().stream()
                .map(LeaveRequestEntity::toLeaveRequest)
                .collect(Collectors.toList());
    }

    /**
     * Returns all leave requests submitted by a specific employee.
     */
    public List<LeaveRequest> getRequestsByEmployee(String employeeId) {
        return repo.findByEmployee(employeeId).stream()
                .map(LeaveRequestEntity::toLeaveRequest)
                .collect(Collectors.toList());
    }

    /**
     * Returns all leave requests with PENDING status.
     */
    public List<LeaveRequest> getPendingRequests() {
        return repo.findAll().stream()
                .map(LeaveRequestEntity::toLeaveRequest)
                .filter(LeaveRequest::isPending)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a leave request (APPROVED or DENIED).
     *
     * @param requestId the UUID of the request to update
     * @param status    the new status string
     */
    public void updateStatus(String requestId, String status) {
        repo.updateStatus(requestId, LeaveStatus.fromString(status));
    }
}