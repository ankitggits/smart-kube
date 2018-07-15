package no.sample.smartkube.common.web.crosscut;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import no.sample.smartkube.common.web.exception.ItemNotFoundException;
import no.sample.smartkube.common.web.model.ErrorRepresentation;
import no.sample.smartkube.common.web.model.Representation;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.naming.AuthenticationException;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalApiExceptionHandler {

    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<Representation> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        Representation representation = new Representation();
        ex.getConstraintViolations().forEach(violation->representation.addError(new ErrorRepresentation(((PathImpl)violation.getPropertyPath()).getLeafNode().getName(),violation.getMessage())));
        return new ResponseEntity<>(representation, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ InvalidFormatException.class })
    public ResponseEntity<Representation> handleFormatException(InvalidFormatException ex, WebRequest request) {
        Representation representation = new Representation();
        representation.addError(new ErrorRepresentation("invalid enum", ex.getMessage()));
        return new ResponseEntity<>(representation, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ BindException.class })
    public ResponseEntity<Representation> handleBindException(BindException ex) {
        return new ResponseEntity<>(GlobalHttpExceptionHandler.handleBinderException(ex.getBindingResult()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ItemNotFoundException.class})
    public ResponseEntity<Representation> handleItemNotFoundException(ItemNotFoundException ex) {
        return new ResponseEntity<>(new Representation().addError(ex.getErrorRepresentation()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<Representation> any(AuthenticationException ex) {
        return new ResponseEntity<>(new Representation().addError(new ErrorRepresentation(ex.getClass().getSimpleName() ,ex.getMessage())), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Representation> any(Exception ex) {
        return new ResponseEntity<>(new Representation().addError(new ErrorRepresentation(ex.getClass().getSimpleName() ,ex.getMessage())), HttpStatus.UNAUTHORIZED);
    }
}