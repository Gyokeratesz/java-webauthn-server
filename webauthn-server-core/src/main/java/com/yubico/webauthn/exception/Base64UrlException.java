package com.yubico.webauthn.exception;

public class Base64UrlException extends Exception {

    public Base64UrlException(String s, Throwable throwable) {
        super(s, throwable);
    }

}
