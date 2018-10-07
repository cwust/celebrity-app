package br.com.cwust.celebrity.engine;

import br.com.cwust.celebrity.CelebrityException;

public class CelebrityEngineException extends CelebrityException {
    public CelebrityEngineException(String message, Object... msgParams) {
        super(message, msgParams);
    }

    public CelebrityEngineException(Throwable cause, String message, Object... msgParams) {
        super(cause, message, msgParams);
    }
}
