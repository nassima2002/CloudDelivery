package com.project.deliveryms.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // 🔒 Hacher le mot de passe
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // ✅ Vérifier le mot de passe
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    // 🧩 Détection si déjà haché
    public static boolean isBCryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$");
    }
}
