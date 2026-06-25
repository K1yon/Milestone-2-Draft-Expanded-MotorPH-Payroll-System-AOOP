package motorph.repository;

import jakarta.persistence.EntityManager;
import motorph.model.Employee;
import motorph.util.HibernateUtil;
import java.util.List;

public class EmployeeRepository {

    public List<Employee> findAll() {
        EntityManager em = HibernateUtil.getEM();
        try {
            return em.createQuery(
                "SELECT e FROM Employee e ORDER BY e.employeeNumber",
                Employee.class).getResultList();
        } finally { em.close(); }
    }

    public Employee findByNumber(String empNum) {
        EntityManager em = HibernateUtil.getEM();
        try { return em.find(Employee.class, empNum); }
        finally { em.close(); }
    }

    public void save(Employee emp) {
        EntityManager em = HibernateUtil.getEM();
        em.getTransaction().begin();
        try { em.persist(emp); em.getTransaction().commit(); }
        catch (Exception ex) { em.getTransaction().rollback(); throw ex; }
        finally { em.close(); }
    }

    public void update(Employee emp) {
        EntityManager em = HibernateUtil.getEM();
        em.getTransaction().begin();
        try { em.merge(emp); em.getTransaction().commit(); }
        catch (Exception ex) { em.getTransaction().rollback(); throw ex; }
        finally { em.close(); }
    }

    public void delete(String empNum) {
        EntityManager em = HibernateUtil.getEM();
        em.getTransaction().begin();
        try {
            Employee e = em.find(Employee.class, empNum);
            if (e != null) em.remove(e);
            em.getTransaction().commit();
        } catch (Exception ex) { em.getTransaction().rollback(); throw ex; }
        finally { em.close(); }
    }
}