package com.rif.authentication.exceptions;

public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException() {

        super("Échec de l'authentification: l'utilisateur est désactivé" );
    }

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
