package com.project.deliveryms.repositories;

import com.project.deliveryms.entities.Utilisateur;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class UtilisateurRepository {

    @PersistenceContext(unitName = "default") // adapte le nom
    private EntityManager em;

    public Utilisateur save(Utilisateur utilisateur) {
        em.persist(utilisateur);
        return utilisateur;
    }

    public Utilisateur findById(Long id) {
        return em.find(Utilisateur.class, id);
    }

    /*
    public Utilisateur findByEmail(String email) {
        TypedQuery<Utilisateur> query = em.createQuery(
                "SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class
        );
        query.setParameter("email", email);
        return query.getResultStream().findFirst().orElse(null);

    }*/
    public Utilisateur findByEmail(String email) {
        try {
            TypedQuery<Utilisateur> query = em.createQuery(
                    "SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class
            );
            query.setParameter("email", email);
            return query.getSingleResult(); // Utilisation de getSingleResult pour une gestion plus stricte
        } catch (NoResultException e) {
            // Log de l'exception si nécessaire, et retour de null
            return null;
        }
    }



}