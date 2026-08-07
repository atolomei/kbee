package com.novamens.kbee.security.oauth2;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class KbeeOauthStateKeyGen {

	static private ObjectMapper mapper = new ObjectMapper();

	public static String encodeState(Map<String, String> attributes) throws JsonProcessingException {
        Payload payload = initPayload(attributes);
        final byte[] jsonPayload = mapper.writeValueAsBytes(payload);
        return Base64.getUrlEncoder().encodeToString(jsonPayload);
    }

    public static Payload decodeState(String state) throws IOException {
        final byte[] jsonPayload = Base64.getUrlDecoder().decode(state);
        return mapper.readValue(jsonPayload,Payload.class);
    }

    private static Payload initPayload(Map<String, String> attributes){
        final StringKeyGenerator generator = new Base64StringKeyGenerator(Base64.getEncoder());

        return new Payload(generator.generateKey(), attributes);
    }


    public static class Payload{
        String key;
        Map<String, String> attributes;

        public Payload() {
        }

        public Payload(String key, Map<String, String> attributes) {
            this.key = key;
            this.attributes = attributes;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(HashMap<String, String> attributes) {
            this.attributes = attributes;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

}
