package ru.duskhunter.contacsapp.common.util;

import java.util.Locale;

public class EmailNormalizer {
    private EmailNormalizer() {
    }

    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}