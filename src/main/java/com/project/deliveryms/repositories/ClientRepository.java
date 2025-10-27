package com.project.deliveryms.repositories;

import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class ClientRepository {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    public List<Utilisateur> findClientsByRole(Role role) {
        TypedQuery<Utilisateur> query = em.createQuery(
                "SELECT u FROM Utilisateur u WHERE u.role = :role", Utilisateur.class
        );
        query.setParameter("role", role);
        return query.getResultList();
    }

    public Utilisateur save(Utilisateur client) {
        em.persist(client);
        return client;
    }

    public Utilisateur update(Utilisateur client) {
        return em.merge(client);
    }

    public void delete(Long id) {
        Utilisateur client = em.find(Utilisateur.class, id);
        if (client != null) {
            em.remove(client);
        }
    }

    public Utilisateur findById(Long id) {
        return em.find(Utilisateur.class, id);
    }
}
