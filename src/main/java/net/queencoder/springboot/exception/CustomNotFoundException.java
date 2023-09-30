package net.queencoder.springboot.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CustomNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;
    public CustomNotFoundException(String message) {
        super(message);
    }
}

