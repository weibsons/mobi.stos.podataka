package mobi.stos.podataka_lib.exception;

/**
 * Created by links_000 on 02/05/2016.
 */
public class ForeignKeyCandidateException extends RuntimeException {

    public ForeignKeyCandidateException() {
        super("Sua candidata a ForeignKey precisa ser INTEGER.");
    }
}
