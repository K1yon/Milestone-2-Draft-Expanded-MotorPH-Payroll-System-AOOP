package motorph.controller;

import motorph.model.Role;
import motorph.model.UserCredential;
import motorph.repository.UserRepository;

public class DbAuthenticator implements AuthService {

    private final UserRepository repo = new UserRepository();

    @Override
    public boolean authenticate(String employeeNumber, String password) {
        UserCredential uc = repo.findByEmployeeNumber(employeeNumber);
        return uc != null && uc.getPassword().equals(password);
    }

    @Override
    public boolean userExists(String employeeNumber) {
        return repo.findByEmployeeNumber(employeeNumber) != null;
    }

    public Role getRoleFor(String employeeNumber) {
        UserCredential uc = repo.findByEmployeeNumber(employeeNumber);
        return uc != null ? uc.getRole() : Role.EMPLOYEE;
    }
}