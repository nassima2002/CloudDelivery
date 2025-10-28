package com.project.deliveryms.beans;

import com.project.deliveryms.utils.PasswordUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.logging.Logger;

/**
 * Bean de test pour vérifier le système de mot de passe
 * À utiliser temporairement pour le débogage
 */
@Named
@RequestScoped
public class PasswordTestBean {

    private static final Logger LOG = Logger.getLogger(PasswordTestBean.class.getName());

    private String plainPassword;
    private String hashedPassword;
    private String testResult;

    /**
     * Test de hashage
     */
    public void testHash() {
        LOG.info("═══════════════════════════════════");
        LOG.info("🧪 TEST HASHAGE MOT DE PASSE");
        LOG.info("═══════════════════════════════════");

        if (plainPassword == null || plainPassword.isEmpty()) {
            testResult = "❌ Veuillez entrer un mot de passe";
            return;
        }

        try {
            String hash = PasswordUtils.hashPassword(plainPassword);

            LOG.info("Mot de passe en clair: " + plainPassword);
            LOG.info("Hash BCrypt généré: " + hash);
            LOG.info("Longueur du hash: " + hash.length());
            LOG.info("Format BCrypt valide: " + PasswordUtils.isBCryptHash(hash));

            // Test de vérification immédiate
            boolean valid = PasswordUtils.checkPassword(plainPassword, hash);
            LOG.info("Vérification immédiate: " + valid);

            testResult = "✅ Hash généré avec succès:\n" + hash + "\n\nVérification: " + valid;
            hashedPassword = hash;

            LOG.info("═══════════════════════════════════");

        } catch (Exception e) {
            LOG.severe("❌ Erreur: " + e.getMessage());
            testResult = "❌ Erreur: " + e.getMessage();
        }
    }

    /**
     * Test de vérification
     */
    public void testVerify() {
        LOG.info("═══════════════════════════════════");
        LOG.info("🔐 TEST VÉRIFICATION MOT DE PASSE");
        LOG.info("═══════════════════════════════════");

        if (plainPassword == null || hashedPassword == null) {
            testResult = "❌ Veuillez remplir les deux champs";
            return;
        }

        try {
            LOG.info("Mot de passe en clair: " + plainPassword);
            LOG.info("Hash à vérifier: " + hashedPassword);
            LOG.info("Format BCrypt valide: " + PasswordUtils.isBCryptHash(hashedPassword));

            boolean valid = PasswordUtils.checkPassword(plainPassword, hashedPassword);

            LOG.info("Résultat vérification: " + valid);
            LOG.info("═══════════════════════════════════");

            if (valid) {
                testResult = "✅ MOT DE PASSE VALIDE";
            } else {
                testResult = "❌ MOT DE PASSE INVALIDE";
            }

        } catch (Exception e) {
            LOG.severe("❌ Erreur: " + e.getMessage());
            testResult = "❌ Erreur: " + e.getMessage();
        }
    }

    // Getters et Setters
    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }
}


