package com.project.deliveryms.services;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import com.project.deliveryms.repositories.LivreureRepository;
import com.project.deliveryms.repositories.UserRepository;
import com.project.deliveryms.utils.PasswordUtils;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class LivreurService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LivreurService.class.getName());

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private LivreureRepository livreurRepository;

    @Inject
    private EmailService emailService;

    /**
     * Crée un nouveau livreur avec un compte utilisateur
     * @param Password Le mot de passe EN CLAIR (sera hashé automatiquement)
     */
    @Transactional
    public Livreur createLivreur(String email, String nom, String prenom,
                                 Double latitude, Double longitude,
                                 String disponibilite, String Password) {
        try {
            LOG.info("➕ Création livreur: " + nom + " " + prenom + " (" + email + ")");

            // ✅ Hasher le mot de passe EN CLAIR avec PasswordUtils
            String hashedPassword = PasswordUtils.hashPassword(Password);
            LOG.info("   Hash généré (début): " + hashedPassword.substring(0, 15) + "...");

            // Créer l'utilisateur
            Utilisateur user = new Utilisateur();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setMotDePasse(hashedPassword);
            user.setRole(Role.LIVREUR);
            user.setCreationDate(java.time.LocalDateTime.now());

            entityManager.persist(user);
            entityManager.flush();

            LOG.info("   ✅ Utilisateur créé (ID=" + user.getId() + ")");

            // Créer le livreur
            Livreur livreur = new Livreur();
            livreur.setLatitude(latitude);
            livreur.setLongitude(longitude);
            livreur.setDisponibiliter(disponibilite);
            livreur.setUser(user);

            entityManager.persist(livreur);
            entityManager.flush();

            LOG.info("   ✅ Livreur créé (ID=" + livreur.getId() + ")");
            return livreur;

        } catch (Exception e) {
            LOG.severe("❌ Erreur création: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur création livreur: " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour un livreur et son utilisateur
     * ⚠️ Ne modifie PAS le mot de passe lors de la mise à jour
     */
    @Transactional
    public void updateLivreur(Livreur livreur) {
        try {
            if (livreur == null || livreur.getId() == null) {
                throw new RuntimeException("Données invalides");
            }

            LOG.info("🛠️ Mise à jour livreur ID=" + livreur.getId());

            // Charger le livreur existant depuis la base
            Livreur existingLivreur = entityManager.find(Livreur.class, livreur.getId());
            if (existingLivreur == null) {
                throw new RuntimeException("Livreur introuvable: " + livreur.getId());
            }

            // Mettre à jour les données du livreur
            existingLivreur.setLatitude(livreur.getLatitude());
            existingLivreur.setLongitude(livreur.getLongitude());
            existingLivreur.setDisponibiliter(livreur.getDisponibiliter());

            // Mettre à jour l'utilisateur (SAUF le mot de passe)
            Utilisateur existingUser = existingLivreur.getUser();
            Utilisateur newUserData = livreur.getUser();

            if (newUserData != null && existingUser != null) {
                existingUser.setNom(newUserData.getNom());
                existingUser.setPrenom(newUserData.getPrenom());
                existingUser.setEmail(newUserData.getEmail());

                // ✅ NE JAMAIS modifier le mot de passe lors d'une mise à jour
                // Le mot de passe reste inchangé
                LOG.info("   ℹ️ Mot de passe conservé (non modifié)");
            }

            entityManager.merge(existingLivreur);
            entityManager.flush();

            LOG.info("✅ Livreur mis à jour: ID=" + livreur.getId());

        } catch (Exception e) {
            LOG.severe("❌ Erreur mise à jour: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur mise à jour: " + e.getMessage(), e);
        }
    }

    /**
     * Supprime un livreur et son utilisateur associé
     * ⚠️ IMPORTANT: Supprimer d'abord le livreur, puis l'utilisateur
     */
    @Transactional
    public void deleteLivreur(Long id) {
        try {
            LOG.info("🗑️ Suppression du livreur ID=" + id);

            // Charger le livreur avec son utilisateur
            Livreur livreur = entityManager.find(Livreur.class, id);

            if (livreur == null) {
                LOG.warning("⚠️ Livreur introuvable: ID=" + id);
                return;
            }

            // Récupérer l'utilisateur AVANT de supprimer le livreur
            Utilisateur user = livreur.getUser();
            Long userId = user != null ? user.getId() : null;

            LOG.info("   Livreur trouvé: " + livreur.getUser().getNom());
            LOG.info("   Utilisateur associé ID: " + userId);

            // ✅ ÉTAPE 1: Supprimer d'abord le livreur
            entityManager.remove(livreur);
            entityManager.flush(); // Forcer la suppression immédiate

            LOG.info("   ✅ Livreur supprimé");

            // ✅ ÉTAPE 2: Supprimer ensuite l'utilisateur
            if (userId != null) {
                // Recharger l'utilisateur depuis la base
                Utilisateur utilisateurToDelete = entityManager.find(Utilisateur.class, userId);

                if (utilisateurToDelete != null) {
                    entityManager.remove(utilisateurToDelete);
                    entityManager.flush();
                    LOG.info("   ✅ Utilisateur supprimé: ID=" + userId);
                }
            }

            LOG.info("✅ Suppression complète réussie: Livreur ID=" + id);

        } catch (Exception e) {
            LOG.severe("❌ Erreur suppression: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du livreur: " + e.getMessage(), e);
        }
    }

    public List<Livreur> getAllLivreurs() {
        return livreurRepository.findAll();
    }

    public List<Livreur> getLivreursIndisponibles() {
        return livreurRepository.findLivreursIndisponibles();
    }

    /**
     * Récupère un livreur par son ID avec chargement des associations
     */
    public Livreur getLivreurById(Long id) {
        try {
            if (id == null) {
                return null;
            }

            LOG.info("🔍 Récupération livreur ID=" + id);

            Livreur livreur = entityManager.find(Livreur.class, id);

            // Forcer le chargement des associations lazy
            if (livreur != null && livreur.getUser() != null) {
                livreur.getUser().getNom();
                livreur.getUser().getEmail();
            }

            return livreur;

        } catch (Exception e) {
            LOG.severe("❌ Erreur récupération: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Livreur findByEmail(String email) {
        return livreurRepository.findLivreurByEmail(email);
    }

}