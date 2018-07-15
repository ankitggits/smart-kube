package no.sample.smartkube.common.web.config.swagger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import no.sample.smartkube.common.web.model.ErrorRepresentation;

import java.util.List;

@Getter
@Setter
@ApiModel
public class FailureModelRef{
    @ApiModelProperty(notes = "HTTP Status Code")
    private List<ErrorRepresentation> errors;
}


