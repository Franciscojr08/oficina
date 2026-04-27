package br.com.prime.oficina.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonStringUtil {

    public static String toJson(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
