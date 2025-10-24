package com.project.deliveryms.repositories;

import com.project.deliveryms.entities.User;
import com.project.deliveryms.entities.Utilisateur;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Stateless
public class UserRepository {

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    public void save(Utilisateur user) {
        entityManager.persist(user);
    }

    public Optional<Utilisateur> findById(Long id) {
        Utilisateur user = entityManager.find(Utilisateur.class, id);
        return Optional.ofNullable(user);
    }

    public Optional<Utilisateur> findByEmail(String email) {
        List<Utilisateur> results = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.email = :email", Utilisateur.class)
                .setParameter("email", email)
                .getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.get(0));
        }
    }

    public List<Utilisateur> findAll() {
        return entityManager.createQuery("SELECT u FROM Utilisateur u", Utilisateur.class)
                .getResultList();
    }

    public void update(Utilisateur user) {
        entityManager.merge(user);
    }

    public void delete(Utilisateur user) {
        entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
    }
}
