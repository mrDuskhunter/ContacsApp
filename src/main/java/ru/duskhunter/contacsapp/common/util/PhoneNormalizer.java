package ru.duskhunter.contacsapp.common.util;

public final class PhoneNormalizer {
    private PhoneNormalizer() {
    }

    public static String normalize(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("[()\\s-]", "");
    }
}