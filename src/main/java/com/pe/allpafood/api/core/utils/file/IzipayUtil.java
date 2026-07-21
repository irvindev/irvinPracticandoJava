package com.pe.allpafood.api.core.utils.file;


import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@Slf4j
public class IzipayUtil {

    public static String getBasicAuth(String user, String password) {
        String data = user.concat(":").concat(password);
        return "Basic "+ Base64.getEncoder().encodeToString(data.getBytes());
    }
}
