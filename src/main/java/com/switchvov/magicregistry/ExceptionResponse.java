package com.switchvov.magicregistry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @author switch
 * @since 2024/4/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
    private HttpStatus httpStatus;
    private String message;
}
