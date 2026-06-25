package motorph.repository;

import jakarta.persistence.EntityManager;
import motorph.model.UserCredential;
import motorph.util.HibernateUtil;

public class UserRepository {

    public UserCredential findByEmployeeNumber(String empNum) {
        EntityManager em = HibernateUtil.getEM();
        try { return em.find(UserCredential.class, empNum); }
        finally { em.close(); }
    }

    public void save(UserCredential uc) {
        EntityManager em = HibernateUtil.getEM();
        em.getTransaction().begin();
        try { em.persist(uc); em.getTransaction().commit(); }
        catch (Exception ex) { em.getTransaction().rollback(); throw ex; }
        finally { em.close(); }
    }
}