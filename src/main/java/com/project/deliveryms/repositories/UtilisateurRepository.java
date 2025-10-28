package com.project.deliveryms.repositories;

import com.project.deliveryms.entities.Utilisateur;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

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
        TypedQuery<Utilisateur> query = em.createQuery(
                "SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class);
        query.setParameter("email", email);
        List<Utilisateur> results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);  // retourner le premier si plusieurs
    }




}


