package motorph.util;

import motorph.model.ClockResult;
import motorph.model.WorkHours;
import motorph.model.WorkHoursEntity;
import motorph.repository.WorkHoursRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceReader {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // Lunch break deducted from total work hours before computing regular/overtime
    private static final int LUNCH_BREAK_MINS  = 60;
    private static final int MAX_REGULAR_HOURS = 8;

    private final WorkHoursRepository repo = new WorkHoursRepository();

    public AttendanceReader() {}

    /**
     * Returns today's date formatted as MM/dd/yyyy.
     * Extracted to avoid repeating LocalDate.now().format(DATE_FMT) across methods.
     */
    private String today() {
        return LocalDate.now().format(DATE_FMT);
    }

    /**
     * Returns all attendance records for the given employee in the specified month.
     *
     * @param empNum the employee number to filter by
     * @param month  the month number (1 = January, 12 = December)
     */
    public List<WorkHours> getAttendanceForMonth(String empNum, int month) {
        return repo.findByEmployeeAndMonth(empNum, month).stream()
                .map(WorkHoursEntity::toWorkHours)
                .collect(Collectors.toList());
    }

    /**
     * Returns all attendance records for the given employee across all months.
     *
     * @param empNum the employee number to filter by
     */
    public List<WorkHours> getAllForEmployee(String empNum) {
        return repo.findByEmployee(empNum).stream()
                .map(WorkHoursEntity::toWorkHours)
                .collect(Collectors.toList());
    }

    /**
     * Returns today's clock-in time for the given employee, or null if not clocked in.
     *
     * @param empNum the employee number to check
     */
    public String getTodayClockIn(String empNum) {
        WorkHoursEntity whe = repo.findByEmployeeAndDate(empNum, today());
        return whe != null ? whe.getLogIn() : null;
    }

    /**
     * Returns today's clock-out time for the given employee, or null if not clocked out.
     *
     * @param empNum the employee number to check
     */
    public String getTodayClockOut(String empNum) {
        WorkHoursEntity whe = repo.findByEmployeeAndDate(empNum, today());
        return (whe != null && whe.getLogOut() != null && !whe.getLogOut().isEmpty())
                ? whe.getLogOut() : null;
    }

    /**
     * Records a clock-in entry for today.
     * Returns ClockResult.ok() on success or ClockResult.fail(message) on failure.
     *
     * @param empNum    the employee number
     * @param lastName  the employee's last name
     * @param firstName the employee's first name
     * @param timeStr   the clock-in time string (format: H:mm)
     * @return ClockResult indicating success or failure with a message
     */
    public ClockResult clockIn(String empNum, String lastName,
                               String firstName, String timeStr) {
        String today = today();
        if (repo.findByEmployeeAndDate(empNum, today) != null)
            return ClockResult.fail("You have already clocked in today (" + today + ").");
        WorkHoursEntity whe = new WorkHoursEntity(empNum, today, timeStr, null, 0, 0);
        repo.save(whe);
        return ClockResult.ok();
    }

    /**
     * Records a clock-out entry for today and computes regular and overtime hours.
     * Deducts a lunch break of 60 minutes from total time worked.
     * Returns ClockResult.ok() on success or ClockResult.fail(message) on failure.
     *
     * @param empNum  the employee number
     * @param timeStr the clock-out time string (format: H:mm)
     * @return ClockResult indicating success or failure with a message
     */
    public ClockResult clockOut(String empNum, String timeStr) {
        String today = today();
        WorkHoursEntity whe = repo.findByEmployeeAndDate(empNum, today);
        if (whe == null)
            return ClockResult.fail("No clock-in record found for today (" + today + ").");
        if (whe.getLogOut() != null && !whe.getLogOut().isEmpty())
            return ClockResult.fail("You have already clocked out today (" + today + ").");
        double hours = calcHours(whe.getLogIn(), timeStr);
        whe.setLogOut(timeStr);
        whe.setRegularHours((int) Math.min(hours, MAX_REGULAR_HOURS));
        whe.setOvertimeHours((int) Math.max(hours - MAX_REGULAR_HOURS, 0));
        repo.update(whe);
        return ClockResult.ok();
    }

    /**
     * Revert functionality is unavailable after database migration.
     * CSV backup no longer exists in the ORM-based implementation.
     */
    public String revertToBackup() {
        return "Revert is not available after database migration.";
    }

    /**
     * Calculates total working hours between clock-in and clock-out.
     * Deducts LUNCH_BREAK_MINS minutes for the mandatory lunch break.
     *
     * @param logIn  the clock-in time string (format: H:mm)
     * @param logOut the clock-out time string (format: H:mm)
     * @return total billable hours after lunch deduction, minimum 0
     */
    private double calcHours(String logIn, String logOut) {
        try {
            if (logIn == null || logOut == null || logIn.isEmpty() || logOut.isEmpty())
                return 0;
            long minutes = LocalTime.parse(logIn, TIME_FMT)
                .until(LocalTime.parse(logOut, TIME_FMT), ChronoUnit.MINUTES);
            return Math.max((minutes - LUNCH_BREAK_MINS) / 60.0, 0);
        } catch (Exception e) { return 0; }
    }
}