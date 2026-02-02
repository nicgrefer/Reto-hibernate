package com.sede.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.sede.entities.Registros;

public class RegistrosDAO {

    public String guardarRegistro(Registros registro) {
        Transaction transaction = null;
        String numRegistro = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(registro);
            numRegistro = registro.getNumRegistro();

            transaction.commit();
            System.out.println("Registro guardado exitosamente: " + numRegistro);

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error al guardar registro: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al guardar el registro", e);
        }

        return numRegistro;
    }

    public Registros buscarPorNumero(String numRegistro) {
        Registros registro = null;
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            registro = session.get(Registros.class, numRegistro);

            if (registro != null) {
                registro.getEntidad().getNombreEntidad();
            }

            transaction.commit();

            if (registro != null) {
                System.out.println("Registro encontrado: " + numRegistro);
            } else {
                System.out.println("No se encontró registro con número: " + numRegistro);
            }

        } catch (HibernateException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error al buscar registro: " + e.getMessage());
            e.printStackTrace();
        }

        return registro;
    }

    public long obtenerContador() {
        long contador = 0;
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query<Long> query = session.createQuery(
                "SELECT COUNT(r) FROM Registros r", Long.class);
            contador = query.uniqueResult();

            transaction.commit();

        } catch (HibernateException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error al obtener contador: " + e.getMessage());
            e.printStackTrace();
        }

        return contador;
    }
}
