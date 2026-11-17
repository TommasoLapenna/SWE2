package main.dao;

import main.domain.model.component.Component;

import java.util.List;
import java.util.Optional;

/**
 * Salvataggio delle build. Lavora su liste di componenti e non dipende da PcBuilder:
 * chi usa il DAO converte da e verso la propria build.
 *
 * <p>Ogni componente deve gia' essere nel catalogo (id maggiore di 0). Una build puo' essere vuota o
 * incompleta, e puo' contenere piu' esemplari dello stesso componente: le regole di compatibilita'
 * e i limiti per tipo (per esempio una sola CPU) non sono controllati qui, ma dalle regole Java.
 *
 * @throws IllegalArgumentException se il nome e' vuoto o un componente non e' nel catalogo (id 0)
 * @throws db.PersistenceException  se un componente con quell'id non esiste o il database fallisce
 */
public interface BuildDao {

    /** Salva una nuova build in una transazione (build e tutti i suoi componenti, o niente). */
    SavedBuild save(String name, List<? extends Component> components);

    /** Sostituisce nome e componenti di una build esistente. Restituisce false se non esiste. */
    boolean update(int id, String name, List<? extends Component> components);

    Optional<SavedBuild> findById(int id);

    /** Tutte le build salvate, ordinate per id (senza componenti). */
    List<BuildSummary> findAll();

    /** Cancella la build (i componenti restano nel catalogo). Restituisce false se non esiste. */
    boolean delete(int id);
}
