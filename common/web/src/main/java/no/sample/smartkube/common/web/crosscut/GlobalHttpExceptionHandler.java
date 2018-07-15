package no.sample.smartkube.common.web.crosscut;

import no.sample.smartkube.common.web.model.ErrorRepresentation;
import no.sample.smartkube.common.web.model.Representation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalHttpExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(handleBinderException(ex.getBindingResult()), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String unsupported = "Unsupported content type: " + ex.getContentType();
        String supported = "Supported content types: " + MediaType.toString(ex.getSupportedMediaTypes());
        String errorMessage = unsupported+", please use - " + supported;
        return new ResponseEntity<>(new Representation().addError(new ErrorRepresentation("MediaTypeNotSupported", errorMessage )), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String unsupported = "Unsupported content type: " + ex.getMessage();
        String supported = "Supported content types: " + MediaType.toString(ex.getSupportedMediaTypes());
        String errorMessage = unsupported+", please use - " + supported;
        return new ResponseEntity<>(new Representation().addError(new ErrorRepresentation("MediaTypeNotAcceptable", errorMessage )), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        ErrorRepresentation errorRepresentation;
        if (mostSpecificCause != null) {
            String exceptionName = mostSpecificCause.getClass().getName();
            String message = mostSpecificCause.getMessage();
            errorRepresentation = new ErrorRepresentation(exceptionName, message);
        } else {
            errorRepresentation = new ErrorRepresentation("MessageNotReadable", ex.getMessage());
        }
        return new ResponseEntity<>(new Representation().addError(errorRepresentation), headers, status);
    }

    static Representation handleBinderException(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        List<ObjectError> globalErrors = bindingResult.getGlobalErrors();
        Representation representation = new Representation();
        ErrorRepresentation error;
        for (FieldError fieldError : fieldErrors) {
            error = new ErrorRepresentation(fieldError.getField() , fieldError.getDefaultMessage());
            representation.addError(error);
        }
        for (ObjectError objectError : globalErrors) {
            error = new ErrorRepresentation(objectError.getObjectName() , objectError.getDefaultMessage());
            representation.addError(error);
        }
        return representation;
    }

}
