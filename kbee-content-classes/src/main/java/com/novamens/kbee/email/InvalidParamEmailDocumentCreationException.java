package com.novamens.kbee.email;

public class InvalidParamEmailDocumentCreationException extends EmailDocumentCreationException{

    public InvalidParamEmailDocumentCreationException(String parameterName) {
        super("Exception while parsing parameter '" + parameterName + "'");
    }
}
