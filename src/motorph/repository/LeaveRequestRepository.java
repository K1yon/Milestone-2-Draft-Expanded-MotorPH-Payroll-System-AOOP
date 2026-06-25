package motorph.repository;

import jakarta.persistence.EntityManager;
import motorph.model.LeaveRequest;
import motorph.model.LeaveRequestEntity;
import motorph.util.HibernateUtil;
import java.util.List;

public class LeaveRequestRepository {

    public void save(LeaveRequestEntity entity) {
        EntityManager em = HibernateUtil.getEM();
        em.getTransaction().begin();
        try { em.persist(entity); em.getTransaction().commit(); }
        catch (Exception ex) { em.getTransaction().rollback(); throw ex; }
        finally { em.close(); }
    }

    public List<LeaveRequestEntity> findAll() {
        EntityManager em = HibernateUtil.getEM();
        try {
            return em.createQuery(
                "SELECT l FROM LeaveRequestEntity l",
                LeaveRequestEntity.class).getResultList();
        } finally { em.close(); }
    }

    public List<LeaveRequestEntity> findByEmployee(String empId) {
        EntityManager em = HibernateUtil.getEM();
        try {
            return em.createQuery(
                "SELECT l FROM LeaveRequestEntity l WHERE l.employeeId = :id",
                LeaveRequestEntity.class)
                .setParameter("id", empId).getResultList();
        } finally { em.close(); }
    }

    public void updateStatus(String requestId, LeaveRequest.LeaveStatus newStatus) {
        EntityManager em = HibernateUtil.getEM();
        em.getTransaction().begin();
        try {
            LeaveRequestEntity e = em.find(LeaveRequestEntity.class, requestId);
            if (e != null) { e.setStatus(newStatus); em.merge(e); }
            em.getTransaction().commit();
        } catch (Exception ex) { em.getTransaction().rollback(); throw ex; }
        finally { em.close(); }
    }
}