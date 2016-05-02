package mobi.stos.podataka_lib.exception;

public class PrimaryKeyCandidateException extends RuntimeException {

    public PrimaryKeyCandidateException() {
        super("Sua candidata a PrimaryKey precisa ser INTEGER.");
    }

}
