package io.lighty.network.codec;

public class EncoderException extends RuntimeException {

    public EncoderException(Throwable cause) {
        super(cause);
    }

    public EncoderException(String message) {
        super(message);
    }
}
