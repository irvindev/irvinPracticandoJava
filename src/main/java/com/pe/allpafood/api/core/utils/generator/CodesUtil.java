package com.pe.allpafood.api.core.utils.generator;

import java.security.SecureRandom;
import java.util.Random;

public class CodesUtil {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 12;
    private static final int ID_LENGTH_PASS = 8;
    private static final SecureRandom random = new SecureRandom();

    public static String randomId() {
        StringBuilder id = new StringBuilder(ID_LENGTH);

        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            id.append(CHARACTERS.charAt(index));
        }

        return id.toString();
    }

    public static String randomNumber() {
        Random random = new Random();
        int randomNum = 100000 + random.nextInt(900000);
        return String.valueOf(randomNum);
    }
}
