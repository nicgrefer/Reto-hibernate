package com.sede.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.sede.entities.Entidad;

public class EntidadDAO {

    public List<Entidad> listarEntidades() {
        List<Entidad> entidades = new ArrayList<>();
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query<Entidad> query = session.createQuery(
                "FROM Entidad e ORDER BY e.nombreEntidad", Entidad.class);
            entidades = query.list();

            transaction.commit();
            System.out.println("Se obtuvieron " + entidades.size() + " entidades");

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error al listar entidades: " + e.getMessage());
            e.printStackTrace();
        }

        return entidades;
    }

    public Entidad obtenerPorId(int id) {
        Entidad entidad = null;
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            entidad = session.get(Entidad.class, id);

            transaction.commit();

            if (entidad != null) {
                System.out.println("Entidad encontrada: " + entidad.getNombreEntidad());
            } else {
                System.out.println("No se encontró entidad con ID: " + id);
            }

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error al obtener entidad por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return entidad;
    }
}
