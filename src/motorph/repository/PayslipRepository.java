package motorph.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import motorph.model.PayslipEntity;
import motorph.util.HibernateUtil;

public class PayslipRepository {

    public void save(PayslipEntity payslip) {
        EntityManager em = HibernateUtil.getEM();

        try {
            em.getTransaction().begin();
            em.persist(payslip);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<PayslipEntity> findByEmployeeNumber(String employeeNumber) {
        EntityManager em = HibernateUtil.getEM();

        try {
            return em.createQuery(
                    "SELECT p FROM PayslipEntity p " +
                    "WHERE p.employeeNumber = :employeeNumber " +
                    "ORDER BY p.payPeriodStart DESC",
                    PayslipEntity.class
            )
            .setParameter("employeeNumber", employeeNumber)
            .getResultList();
        } finally {
            em.close();
        }
    }

    public List<PayslipEntity> findAll() {
        EntityManager em = HibernateUtil.getEM();

        try {
            return em.createQuery(
                    "SELECT p FROM PayslipEntity p " +
                    "ORDER BY p.payPeriodStart DESC",
                    PayslipEntity.class
            ).getResultList();
        } finally {
            em.close();
        }
    }
}