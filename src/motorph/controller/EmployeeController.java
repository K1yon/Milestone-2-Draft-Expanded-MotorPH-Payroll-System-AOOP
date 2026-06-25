package motorph.controller;

import motorph.model.Employee;
import motorph.repository.EmployeeRepository;
import java.util.List;

public class EmployeeController {

    private final EmployeeRepository repo = new EmployeeRepository();

    public EmployeeController() {}

    public List<Employee> loadAllEmployees() {
        return repo.findAll();
    }

    public Employee findByNumber(String employeeNumber) {
        return repo.findByNumber(employeeNumber);
    }

    public void createEmployee(Employee emp) {
        repo.save(emp);
    }

    public void updateEmployee(Employee updated) {
        repo.update(updated);
    }

    public void deleteEmployee(String employeeNumber) {
        repo.delete(employeeNumber);
    }
}