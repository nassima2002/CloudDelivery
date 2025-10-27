package com.project.deliveryms.services;

import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ClientService {

    @PersistenceContext
    private EntityManager em;

    public List<Utilisateur> getAllClients() {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.role = :role", Utilisateur.class)
                .setParameter("role", Role.CLIENT)
                .getResultList();
    }

    @Transactional
    public void addClient(Utilisateur client) {
        client.setRole(Role.CLIENT);
        client.setCreationDate(java.time.LocalDateTime.now());
        em.persist(client);
    }

    @Transactional
    public void updateClient(Utilisateur client) {
        em.merge(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        Utilisateur client = em.find(Utilisateur.class, id);
        if (client != null) {
            em.remove(client);
        }
    }
}
