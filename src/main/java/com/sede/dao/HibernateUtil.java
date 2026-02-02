package com.sede.dao;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
            System.out.println("SessionFactory creada exitosamente");
        } catch (Exception ex) {
            System.err.println("Error al crear SessionFactory: " + ex.getMessage());
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                sessionFactory.close();
                System.out.println("SessionFactory cerrada correctamente");
            } catch (Exception e) {
                System.err.println("Error al cerrar SessionFactory: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
