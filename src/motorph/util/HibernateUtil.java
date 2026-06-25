package motorph.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HibernateUtil {

    private static final Logger LOGGER = Logger.getLogger(HibernateUtil.class.getName());
    private static final String PERSISTENCE_UNIT = "motorph-pu";

    private static final EntityManagerFactory EMF;

    static {
        try {
            LOGGER.info("Initializing EntityManagerFactory for persistence unit: " + PERSISTENCE_UNIT);
            EMF = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            LOGGER.info("EntityManagerFactory initialized successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize EntityManagerFactory for unit: "
                    + PERSISTENCE_UNIT, e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Creates and returns a new EntityManager instance.
     * Callers are responsible for closing the EntityManager after use.
     *
     * @return a new EntityManager from the shared factory
     */
    public static EntityManager getEM() {
        LOGGER.fine("Creating new EntityManager.");
        return EMF.createEntityManager();
    }

    /**
     * Closes the EntityManagerFactory on application shutdown.
     * Should be called once when the application exits.
     */
    public static void close() {
        if (EMF != null && EMF.isOpen()) {
            LOGGER.info("Closing EntityManagerFactory.");
            EMF.close();
        }
    }

    private HibernateUtil() {}
}