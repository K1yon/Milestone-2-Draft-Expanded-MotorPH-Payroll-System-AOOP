package motorph.repository;

import jakarta.persistence.EntityManager;
import motorph.model.WorkHoursEntity;
import motorph.util.HibernateUtil;
import java.util.List;

public class WorkHoursRepository {

    public List<WorkHoursEntity> findByEmployee(String empNum) {
        EntityManager em = HibernateUtil.getEM();
        try {
            return em.createQuery(
                "SELECT w FROM WorkHoursEntity w WHERE w.employeeNumber = :num",
                WorkHoursEntity.class)
                .setParameter("num", empNum).getResultList();
        } finally { em.close(); }
    }

    public List<WorkHoursEntity> findByEmployeeAndMonth(String empNum, int month) {
        EntityManager em = HibernateUtil.getEM();
        try {
            String prefix = String.format("%02d/", month);
            return em.createQuery(
                "SELECT w FROM WorkHoursEntity w WHERE w.employeeNumber = :num" +
                " AND w.date LIKE :pfx",
                WorkHoursEntity.class)
                .setParameter("num", empNum)
                .setParameter("pfx", prefix + "%")
                .getResultList();
        } finally { em.close(); }
    }

    public WorkHoursEntity findByEmployeeAndDate(String empNum, String date) {
        EntityManager em = HibernateUtil.getEM();
        try {
            List<WorkHoursEntity> r = em.createQuery(
                "SELECT w FROM WorkHoursEntity w WHERE w.employeeNumber = :num" +
                " AND w.date = :d", WorkHoursEntity.class)
                .setParameter("num", empNum).setParameter("d", date)
                .getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { em.close(); }
    }

    public void save(WorkHoursEntity whe) {
        EntityManager em = HibernateUtil.getEM();
        em.getTransaction().begin();
        try { em.persist(whe); em.getTransaction().commit(); }
        catch (Exception ex) { em.getTransaction().rollback(); throw ex; }
        finally { em.close(); }
    }

    public void update(WorkHoursEntity whe) {
        EntityManager em = HibernateUtil.getEM();
        em.getTransaction().begin();
        try { em.merge(whe); em.getTransaction().commit(); }
        catch (Exception ex) { em.getTransaction().rollback(); throw ex; }
        finally { em.close(); }
    }
}