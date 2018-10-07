package br.com.cwust.celebrity;

public class CelebrityException extends RuntimeException {
    public CelebrityException(String message, Object ... msgParams) {
        super(String.format(message, msgParams));
    }

    public CelebrityException(Throwable cause, String message, Object ... msgParams) {
        super(String.format(message, msgParams), cause);
    }
}
