package no.sample.smartkube.common.web.exception;

import no.sample.smartkube.common.web.model.ErrorRepresentation;

public class BusinessException extends RuntimeException {

    private ErrorRepresentation errorRepresentation;

    public BusinessException(String errorCode, String message) {
        super(message);
        errorRepresentation = new ErrorRepresentation(errorCode, message);
    }

    public ErrorRepresentation getErrorRepresentation() {
        return errorRepresentation;
    }

}