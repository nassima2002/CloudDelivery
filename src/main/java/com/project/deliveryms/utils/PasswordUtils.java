package com.project.deliveryms.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // Méthode pour hacher un mot de passe
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Méthode pour vérifier si le mot de passe correspond au hachage
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
