package com.project.deliveryms.services;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.repositories.LivreureRepository;
import com.project.deliveryms.repositories.UtilisateurRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.logging.Logger;

@Named
@RequestScoped
public class UtilisateurService {

    private static final Logger LOG = Logger.getLogger(UtilisateurService.class.getName());

    @Inject
    private UtilisateurRepository utilisateurRepository;

    @Inject
    private LivreureRepository livreurRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Authentifie un utilisateur par email et mot de passe
     */
    public String authentifier(String email, String motDePasse) {
        LOG.info("═══════════════════════════════════");
        LOG.info("🔐 AUTHENTIFICATION");
        LOG.info("═══════════════════════════════════");
        LOG.info("Email: " + email);

        try {
            // ✅ 1. Chercher l'utilisateur par email
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email);

            if (utilisateur == null) {
                LOG.warning("❌ Utilisateur introuvable: " + email);
                return "Email ou mot de passe incorrect";
            }

            LOG.info("✅ Utilisateur trouvé:");
            LOG.info("   ID: " + utilisateur.getId());
            LOG.info("   Nom: " + utilisateur.getNom() + " " + utilisateur.getPrenom());
            LOG.info("   Rôle: " + utilisateur.getRole());

            String storedPassword = utilisateur.getMotDePasse();

            // ✅ 2. Vérifier que le mot de passe existe
            if (storedPassword == null || storedPassword.isEmpty()) {
                LOG.severe("❌ Mot de passe NULL ou vide en base pour: " + email);
                return "Erreur de configuration. Contactez l'administrateur.";
            }

            LOG.info("   Hash stocké (30 premiers car): " + storedPassword.substring(0, Math.min(30, storedPassword.length())) + "...");
            LOG.info("   Longueur hash: " + storedPassword.length());

            // ✅ 3. Vérifier le format BCrypt
            boolean isBCryptFormat = storedPassword.matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$");
            LOG.info("   Format BCrypt valide: " + isBCryptFormat);

            if (!isBCryptFormat) {
                LOG.warning("⚠️  Format BCrypt invalide (longueur: " + storedPassword.length() + ")");
                LOG.warning("⚠️  Le hash devrait commencer par $2a$ ou $2b$ et faire 60 caractères");

                // Mode de compatibilité : comparaison en texte clair
                if (storedPassword.equals(motDePasse)) {
                    LOG.warning("⚠️  Mot de passe en TEXTE CLAIR détecté pour: " + email);
                    LOG.warning("   URGENT : Hash automatiquement ce mot de passe");

                    // Hash automatiquement
                    try {
                        hashUserPasswordAsync(email, motDePasse);
                    } catch (Exception e) {
                        LOG.warning("⚠️  Impossible de hasher automatiquement: " + e.getMessage());
                    }

                    return "Connexion réussie";
                }

                LOG.severe("❌ Mot de passe incorrect (format invalide)");
                return "Email ou mot de passe incorrect";
            }

            // ✅ 4. Vérifier avec BCrypt
            LOG.info("🔐 Vérification BCrypt...");
            boolean valide = BCrypt.checkpw(motDePasse, storedPassword);
            LOG.info("   BCrypt.checkpw() = " + valide);

            if (!valide) {
                LOG.warning("❌ Mot de passe incorrect pour: " + email);
                return "Email ou mot de passe incorrect";
            }

            LOG.info("✅ ═══════════════════════════════════");
            LOG.info("✅ AUTHENTIFICATION RÉUSSIE");
            LOG.info("✅ ═══════════════════════════════════");

            return "Connexion réussie";

        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
            return "Erreur système. Veuillez réessayer.";
        }
    }

    /**
     * Hash automatiquement un mot de passe en texte clair
     */
    @Transactional
    public void hashUserPasswordAsync(String email, String plainPassword) {
        try {
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
            if (utilisateur != null) {
                String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                utilisateur.setMotDePasse(hash);
                entityManager.merge(utilisateur);
                entityManager.flush();
                LOG.info("✅ Mot de passe automatiquement hashé pour: " + email);
            }
        } catch (Exception e) {
            LOG.warning("⚠️  Impossible de hasher automatiquement: " + e.getMessage());
        }
    }

    /**
     * Inscription d'un nouvel utilisateur
     */
    @Transactional
    public String inscrire(Utilisateur utilisateur) {
        try {
            if (utilisateurRepository.findByEmail(utilisateur.getEmail()) != null) {
                LOG.warning("Email déjà existant: " + utilisateur.getEmail());
                return "Email déjà utilisé.";
            }

            utilisateur.setCreationDate(java.time.LocalDateTime.now());
            utilisateur.setLastConnectionDate(java.time.LocalDateTime.now());

            // ✅ Utiliser BCrypt directement
            String hash = BCrypt.hashpw(utilisateur.getMotDePasse(), BCrypt.gensalt());
            utilisateur.setMotDePasse(hash);

            utilisateurRepository.save(utilisateur);

            LOG.info("✅ Inscription réussie pour: " + utilisateur.getEmail());
            return "Inscription réussie";

        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
            return "Erreur lors de l'inscription.";
        }
    }

    /**
     * Trouve un utilisateur par email
     */
    public Utilisateur findUserByEmail(String email) {
        try {
            return utilisateurRepository.findByEmail(email);
        } catch (Exception e) {
            LOG.severe("Erreur lors de la recherche d'utilisateur par email: " + e.getMessage());
            return null;
        }
    }

    /**
     * Hash le mot de passe d'un utilisateur (migration manuelle)
     */
    @Transactional
    public void hashUserPassword(String email, String plainPassword) {
        try {
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
            if (utilisateur != null) {
                String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                utilisateur.setMotDePasse(hash);
                entityManager.merge(utilisateur);
                entityManager.flush();
                LOG.info("✅ Mot de passe hashé pour: " + email);
            } else {
                LOG.warning("⚠️  Utilisateur introuvable: " + email);
            }
        } catch (Exception e) {
            LOG.severe("❌ Erreur lors du hashage du mot de passe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Migre tous les mots de passe en texte clair vers BCrypt
     */
    @Transactional
    public int migrateAllPlainPasswords() {
        int count = 0;
        try {
            List<Utilisateur> users = entityManager.createQuery(
                    "SELECT u FROM Utilisateur u", Utilisateur.class
            ).getResultList();

            for (Utilisateur user : users) {
                String pwd = user.getMotDePasse();

                // Vérifier si le mot de passe n'est pas déjà hashé
                if (pwd != null && !pwd.matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$")) {
                    user.setMotDePasse(BCrypt.hashpw(pwd, BCrypt.gensalt()));
                    entityManager.merge(user);
                    count++;
                    LOG.info("🔒 Migré: " + user.getEmail());
                }
            }

            entityManager.flush();
            LOG.info("✅ Migration terminée (" + count + " utilisateur(s) migré(s))");

        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la migration: " + e.getMessage());
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Récupère l'utilisateur connecté depuis la session
     */
    public Utilisateur getUtilisateurConnecte() {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx == null) return null;

            HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
            if (session == null) return null;

            return (Utilisateur) session.getAttribute("utilisateurConnecte");
        } catch (Exception e) {
            LOG.warning("Erreur lors de la récupération de l'utilisateur connecté: " + e.getMessage());
            return null;
        }
    }

    /**
     * Récupère le livreur connecté depuis la session
     */
    public Livreur getLivreurConnecte() {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx == null) return null;

            HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
            if (session == null) return null;

            // Vérifier l'attribut "livreurConnecte" spécifique
            Object livreurObj = session.getAttribute("livreurConnecte");
            if (livreurObj instanceof Livreur) {
                return (Livreur) livreurObj;
            }

            // Sinon, récupérer via l'utilisateur
            Object userObj = session.getAttribute("utilisateurConnecte");
            if (userObj instanceof Utilisateur) {
                Utilisateur utilisateur = (Utilisateur) userObj;
                return livreurRepository.findLivreurByEmail(utilisateur.getEmail());
            }

            return null;
        } catch (Exception e) {
            LOG.warning("Erreur lors de la récupération du livreur connecté: " + e.getMessage());
            return null;
        }
    }

    // ===== CRUD de base =====

    @Transactional
    public void save(Utilisateur utilisateur) {
        entityManager.persist(utilisateur);
    }

    @Transactional
    public void update(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getId() == null) {
            throw new IllegalArgumentException("Utilisateur ou ID manquant");
        }
        entityManager.merge(utilisateur);
        entityManager.flush();
    }

    @Transactional
    public void delete(Long id) {
        Utilisateur utilisateur = entityManager.find(Utilisateur.class, id);
        if (utilisateur != null) {
            entityManager.remove(utilisateur);
        }
    }

    public Utilisateur findById(Long id) {
        return entityManager.find(Utilisateur.class, id);
    }

    public List<Utilisateur> findAll() {
        return entityManager.createQuery(
                "SELECT u FROM Utilisateur u ORDER BY u.creationDate DESC",
                Utilisateur.class
        ).getResultList();
    }

    public List<Utilisateur> findByRole(String role) {
        return entityManager.createQuery(
                        "SELECT u FROM Utilisateur u WHERE u.role = :role ORDER BY u.creationDate DESC",
                        Utilisateur.class
                )
                .setParameter("role", role)
                .getResultList();
    }

    public Long countAll() {
        return entityManager.createQuery(
                "SELECT COUNT(u) FROM Utilisateur u",
                Long.class
        ).getSingleResult();
    }

    public Long countByRole(String role) {
        return entityManager.createQuery(
                        "SELECT COUNT(u) FROM Utilisateur u WHERE u.role = :role",
                        Long.class
                )
                .setParameter("role", role)
                .getSingleResult();
    }

    public boolean emailExists(String email) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(u) FROM Utilisateur u WHERE u.email = :email",
                        Long.class
                )
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    public List<Utilisateur> searchByName(String term) {
        return entityManager.createQuery(
                        "SELECT u FROM Utilisateur u WHERE LOWER(u.nom) LIKE LOWER(:t) OR LOWER(u.prenom) LIKE LOWER(:t) ORDER BY u.nom",
                        Utilisateur.class
                )
                .setParameter("t", "%" + term + "%")
                .getResultList();
    }
}