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
     * Crée un nouveau livreur avec un compte utilisateur associé
     */
    @Transactional
    public Livreur createLivreur(String email, String nom, String prenom,
                                 Double latitude, Double longitude, String disponibilite, String motDePasse) {
        try {
            LOG.info("═══════════════════════════════════");
            LOG.info("📦 CRÉATION D'UN LIVREUR");
            LOG.info("═══════════════════════════════════");
            LOG.info("Email: " + email);
            LOG.info("Nom: " + nom + " " + prenom);

            // Vérifier que le mot de passe n'est pas vide
            if (motDePasse == null || motDePasse.trim().isEmpty()) {
                LOG.severe("❌ Mot de passe NULL ou vide !");
                throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
            }

            LOG.info("Mot de passe reçu (longueur): " + motDePasse.length());

            // ✅ HACHER LE MOT DE PASSE UNE SEULE FOIS AVEC BCRYPT
            LOG.info("🔐 Hashage du mot de passe avec BCrypt...");
            String hashedPassword = BCrypt.hashpw(motDePasse, BCrypt.gensalt());

            LOG.info("✅ Hash généré (début): " + hashedPassword.substring(0, Math.min(30, hashedPassword.length())) + "...");
            LOG.info("   Longueur du hash: " + hashedPassword.length());

            // Vérification de sécurité
            if (hashedPassword.length() != 60) {
                LOG.severe("❌ ERREUR: Le hash BCrypt doit faire 60 caractères, pas " + hashedPassword.length());
                throw new RuntimeException("Hash BCrypt invalide");
            }

            // Créer l'utilisateur
            Utilisateur user = new Utilisateur();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setMotDePasse(hashedPassword); // ✅ Hash BCrypt de 60 caractères
            user.setRole(Role.LIVREUR);
            user.setCreationDate(java.time.LocalDateTime.now());

            entityManager.persist(user);
            entityManager.flush(); // Génère l'ID

            LOG.info("✅ Utilisateur créé avec ID: " + user.getId());

            // Créer le livreur
            Livreur livreur = new Livreur();
            livreur.setLatitude(latitude);
            livreur.setLongitude(longitude);
            livreur.setDisponibiliter(disponibilite);
            livreur.setUser(user);

            entityManager.persist(livreur);
            entityManager.flush();

            LOG.info("✅ Livreur créé avec ID: " + livreur.getId());
            LOG.info("✅ Le livreur peut maintenant se connecter avec:");
            LOG.info("   Email: " + email);
            LOG.info("   Mot de passe: [celui que vous avez saisi]");
            LOG.info("═══════════════════════════════════");

            return livreur;

        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la création du livreur : " + e.getMessage());
            e.printStackTrace();
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
                throw new RuntimeException("Données de livreur invalides");
            }

            livreurRepository.update(livreur);
            livreurRepository.getEntityManager().flush();

            LOG.info("Livreur mis à jour avec succès - ID: " + livreur.getId());
        } catch (Exception e) {
            LOG.severe("Erreur lors de la mise à jour: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour du livreur: " + e.getMessage(), e);
        }
    }

    /**
     * Supprime un livreur et son utilisateur associé
     */
    @Transactional
    public void deleteLivreur(Long id) {
        Livreur livreur = entityManager.find(Livreur.class, id);
        if (livreur != null) {
            Utilisateur user = livreur.getUser();
            entityManager.remove(livreur);

            if (user != null) {
                entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
            }
        }
    }

    /**
     * Récupère tous les livreurs
     */
    public List<Livreur> getAllLivreurs() {
        return livreurRepository.findAll();
    }

    /**
     * Récupère les livreurs disponibles
     */
    public List<Livreur> getLivreursIndisponibles() {
        return livreurRepository.findLivreursIndisponibles();
    }

    /**
     * Récupère un livreur par son ID
     */
    public Livreur getLivreurById(Long id) {
        try {
            if (id == null) {
                return null;
            }

            LOG.info("Récupération du livreur avec ID: " + id);

            Livreur livreur = entityManager.find(Livreur.class, id);

            if (livreur != null && livreur.getUser() != null) {
                livreur.getUser().getNom();
                livreur.getUser().getEmail();
            }

            return livreur;
        } catch (Exception e) {
            LOG.severe("Erreur lors de la récupération du livreur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Trouve un livreur par email
     */
    public Livreur findByEmail(String email) {
        return livreurRepository.findLivreurByEmail(email);
    }
}