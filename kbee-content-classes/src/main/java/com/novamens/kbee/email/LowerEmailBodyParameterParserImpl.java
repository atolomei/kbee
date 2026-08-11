package com.novamens.kbee.email;

import java.io.StringReader;
import java.util.*;

public class LowerEmailBodyParameterParserImpl implements EmailBodyParameterParser {
    @Override
    public Map<String, Object> parseParameters(String body) {
        List<String> invalidLines = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();

        try (Scanner scanner = new Scanner(body)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                final int separator = line.indexOf(":");
                if (separator > 0) {
                    String lwKey = line.substring(0, separator).toLowerCase().replaceFirst("^\\s++","");
                    String value = line.substring(separator+1, line.length());
                    includeValue(parameters, lwKey, value);
                } else {
                    invalidLines.add(line);
                }
            }
        }

        return parameters;
    }

    private void includeValue(Map<String, Object> parameters, String key, Object value) {
        Object obj = parameters.get(key);
        if (obj != null){
            if(obj instanceof List){
                ((List)obj).add(value);
            }else{
                List lVal= new ArrayList();
                lVal.add(obj);
                lVal.add(value);
                parameters.put(key, lVal);
            }
        }else{
            parameters.put(key, value);
        }
    }

}
