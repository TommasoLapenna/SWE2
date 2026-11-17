package main.dao;

import main.domain.model.component.Component;

import java.util.List;
import java.util.Optional;

/**
 * Accesso ai dati di un tipo di componente. Chi usa i DAO dipende solo da queste interfacce.
 *
 * <p>Convenzione sull'id: un record non ancora salvato ha {@code id == 0}.
 * {@link #insert} ignora l'id ricevuto e restituisce una COPIA con l'id assegnato dal database
 * (i record sono immutabili, quindi non si puo' modificare quello originale).
 *
 * <p>Gli errori del database (vincoli violati, file non raggiungibile...) sono segnalati con
 * {@link db.PersistenceException}; i dati non validi che il database non puo' controllare
 * (per esempio un insieme vuoto che deve avere almeno un elemento) con
 * {@link IllegalArgumentException}.
 */
public interface ComponentDao<T extends Component> {

    Optional<T> findById(int id);

    /** Tutti i componenti di questo tipo, ordinati per id. */
    List<T> findAll();

    /** Salva un nuovo componente (con le sue tabelle collegate) in un'unica transazione. */
    T insert(T component);

    /** Aggiorna il componente con quell'id. Restituisce false se non esiste. */
    boolean update(T component);

    /**
     * Cancella il componente. Restituisce false se non esiste.
     *
     * @throws db.PersistenceException se il componente e' usato in una build (non si puo' cancellare)
     */
    boolean delete(int id);
}
