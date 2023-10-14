package com.figaf.content.converter.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Kostas Charalambous
 */

@Getter
@NoArgsConstructor
public class ApplicationException extends RuntimeException {

    protected String possibleSolution;
    protected Object additionalData;

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, String possibleSolution) {
        super(message);
        this.possibleSolution = possibleSolution;
    }

    public ApplicationException(String message, Object additionalData) {
        super(message);
        this.additionalData = additionalData;
    }

    public ApplicationException(Throwable cause) {
        super(cause);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
