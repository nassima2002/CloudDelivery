package com.project.deliveryms.services;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import com.project.deliveryms.repositories.LivreureRepository;
import com.project.deliveryms.repositories.UserRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class LivreurService implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Logger placé au bon endroit
    private static final Logger LOG = Logger.getLogger(LivreurService.class.getName());

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private LivreureRepository livreurRepository;

    @Inject
    private EmailService emailService;

    public LivreurService() {
        // Constructeur vide requis pour EJB
    }


    @Transactional
    public Livreur createLivreur(String email, String nom, String prenom,
                                 Double latitude, Double longitude, String disponibilite, String Password) {
        try {

            // Hacher le mot de passe
            String hashedPassword = BCrypt.hashpw(Password, BCrypt.gensalt());

            // Créer l'utilisateur
            Utilisateur user = new Utilisateur();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setMotDePasse(hashedPassword);
            user.setRole(Role.LIVREUR);

            entityManager.persist(user);
            entityManager.flush(); // Génère l'ID

            // Créer le livreur
            Livreur livreur = new Livreur();
            livreur.setLatitude(latitude);
            livreur.setLongitude(longitude);
            livreur.setDisponibiliter(disponibilite);
            livreur.setUser(user);

            entityManager.persist(livreur);

            return livreur;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création du livreur : " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour un livreur et son utilisateur associé
     */
    @Transactional
    public void updateLivreur(Livreur livreur) {
        try {
            if (livreur == null || livreur.getId() == null) {
                throw new IllegalArgumentException("Données de livreur invalides");
            }

            LOG.info("=== MISE À JOUR LIVREUR ===");
            LOG.info("ID: " + livreur.getId());

            // ✅ Si un nouveau mot de passe est fourni, le hasher
            if (livreur.getUser() != null && livreur.getUser().getMotDePasse() != null) {
                String password = livreur.getUser().getMotDePasse();

                // Vérifier si ce n'est pas déjà un hash BCrypt
                if (!password.matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$")) {
                    LOG.info("🔐 Nouveau mot de passe détecté, hachage en cours...");
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
                    livreur.getUser().setMotDePasse(hashedPassword);
                }
            }

            livreurRepository.update(livreur);
            livreurRepository.getEntityManager().flush();

            LOG.info("✅ Livreur mis à jour avec succès - ID: " + livreur.getId());
            LOG.info("=== FIN MISE À JOUR LIVREUR ===");
        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la mise à jour: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour du livreur: " + e.getMessage(), e);
        }
    }

    /**
     * Supprime un livreur et son utilisateur associé
     */
    @Transactional
    public void deleteLivreur(Long id) {
        LOG.info("Suppression du livreur ID: " + id);
        Livreur livreur = entityManager.find(Livreur.class, id);
        if (livreur != null) {
            Utilisateur user = livreur.getUser();
            entityManager.remove(livreur);

            if (user != null) {
                entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
            }
            LOG.info("✅ Livreur supprimé avec succès");
        }
    }

    public List<Livreur> getAllLivreurs() {
        return livreurRepository.findAll();
    }

    public List<Livreur> getLivreursIndisponibles() {
        return livreurRepository.findLivreursIndisponibles();
    }

    public Livreur getLivreurById(Long id) {
        try {
            if (id == null) {
                return null;
            }

            LOG.info("Récupération du livreur avec ID: " + id);
            Livreur livreur = entityManager.find(Livreur.class, id);

            if (livreur != null && livreur.getUser() != null) {
                // Forcer le chargement des associations
                livreur.getUser().getNom();
                livreur.getUser().getEmail();
            }

            return livreur;
        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la récupération du livreur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Livreur findByEmail(String email) {
        LOG.info("🔍 Recherche livreur par email: " + email);

        try {
            Livreur livreur = livreurRepository.findLivreurByEmail(email);

            if (livreur != null) {
                LOG.info("✅ Livreur trouvé - ID: " + livreur.getId());
                if (livreur.getUser() != null) {
                    LOG.info("   User associé - ID: " + livreur.getUser().getId() +
                            ", Email: " + livreur.getUser().getEmail());
                } else {
                    LOG.warning("   ⚠️ User NULL pour ce livreur!");
                }
            } else {
                LOG.warning("❌ Aucun livreur trouvé pour l'email: " + email);
            }

            return livreur;
        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la recherche du livreur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}