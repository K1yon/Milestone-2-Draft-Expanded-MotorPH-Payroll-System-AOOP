package motorph.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_credentials")
public class UserCredential {

    @Id
    @Column(name = "employee_number", length = 20)
    private String employeeNumber;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    public UserCredential() {}

    public UserCredential(String employeeNumber, String password, Role role) {
        this.employeeNumber = employeeNumber;
        this.password       = password;
        this.role           = role;
    }

    public String getEmployeeNumber() { return employeeNumber; }
    public String getPassword()       { return password; }
    public Role   getRole()           { return role; }
    public void   setPassword(String p) { this.password = p; }
    public void   setRole(Role r)       { this.role = r; }
}