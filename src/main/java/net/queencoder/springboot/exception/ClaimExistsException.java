package net.queencoder.springboot.exception;

public class ClaimExistsException extends RuntimeException {
    public ClaimExistsException(String message) {
        super(message);
    }
}

