package no.sample.smartkube.common.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ApiModel
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Representation<T> {

    @ApiModelProperty(notes = "Data container")
    private T payload;

    @ApiModelProperty(hidden = true, readOnly = true, notes = "Container of errors list")
    private List<ErrorRepresentation> errors;

    public Representation(T payload){
        this.payload = payload;
    }

    public static <T> Representation of(T payload){
        return new Representation<>(payload);
    }

    public static Representation error(ErrorRepresentation errorRepresentation){
        return new Representation<>().addError(errorRepresentation);
    }

    public Representation addError(ErrorRepresentation errorRepresentation){
        if(this.errors == null){
            this.errors = Lists.newArrayList();
        }
        this.errors.add(errorRepresentation);
        return this;
    }
}

