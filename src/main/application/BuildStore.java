package main.application;

import main.domain.model.BuildView;
import main.dao.jdbc.BuildDao;
import main.dao.BuildSummary;
import main.dao.SavedBuild;
import main.domain.model.component.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Collega le build dell'applicazione al database: converte una BuildView (la vista di sola lettura
 * di PcBuilder) in una lista di componenti per {@link BuildDao} e viceversa.
 *
 * <p>Uso tipico (in Main o nel controller): {@code new BuildStore(new JdbcBuildDao())}.
 *
 * <p>Caricare una build: {@link #find} restituisce un {@link SavedBuild}; per rimetterla in una
 * sessione serve il costruttore {@code PcBuildSession(String, List<? extends Component>)}
 * (vedi la nota nella classe PcBuildSession).
 *
 * <p>Limiti: i componenti devono venire dal catalogo nel database (id maggiore di 0); dentro lo stesso
 * tipo l'ordine di lettura e' per id, non per ordine di inserimento nella build.
 */
public class BuildStore {

    private final BuildDao dao;

    public BuildStore(BuildDao dao) {
        this.dao = Objects.requireNonNull(dao, "dao");
    }

    /** Salva la build (con il suo nome) come nuova build salvata. */
    public SavedBuild save(BuildView build) {
        return dao.save(build.name(), flatten(build));
    }

    /** Sovrascrive nome e componenti di una build gia' salvata. Restituisce false se non esiste. */
    public boolean update(int id, BuildView build) {
        return dao.update(id, build.name(), flatten(build));
    }

    public Optional<SavedBuild> find(int id) {
        return dao.findById(id);
    }

    public List<BuildSummary> list() {
        return dao.findAll();
    }

    public boolean delete(int id) {
        return dao.delete(id);
    }

    /** Tutti i componenti della build, un elemento per esemplare (lo stesso componente due volte = due elementi). */
    private static List<Component> flatten(BuildView build) {
        return build.components().values().stream().flatMap(List::stream).toList();
    }
}
