package db;

/**
 * Errore del livello di persistenza (database non raggiungibile, vincolo violato, query fallita...).
 * E' non controllata (RuntimeException): chi usa i DAO non e' costretto a gestire SQLException,
 * ma puo' intercettarla per mostrare un messaggio all'utente.
 */
public class PersistenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(String message) {
        super(message);
    }
}
