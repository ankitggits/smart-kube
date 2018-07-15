package no.sample.smartkube.common.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ErrorRepresentation {

    private String errorCode;
    private String errorMessage;
    private String errorInsight;
    private String url;

    public ErrorRepresentation(String errorCode, String errorMessage){
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}