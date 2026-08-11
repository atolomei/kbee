package com.novamens.kbee.email;

import java.util.Map;

public interface EmailBodyParameterParser {

    Map<String,Object> parseParameters(String body);
}
