package no.sample.smartkube.common.web.exception;

public class ItemNotFoundException extends InfrastructureExceptions{

    public ItemNotFoundException(String errorCode, String developerMsg) {
        super(errorCode, developerMsg);
    }

}
