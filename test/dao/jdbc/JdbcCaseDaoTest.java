package dao.jdbc;

import dao.ComponentDao;
import db.DatabaseConnection;
import model.attribute.FormFactor;
import model.attribute.PsuFormFactor;
import model.component.Case;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcCaseDaoTest extends ComponentDaoContractTest<Case> {

    @Override ComponentDao<Case> createDao(DatabaseConnection db) { return new JdbcCaseDao(db); }

    @Override Case sample(String model) {
        return new Case(0, "NZXT", model, 89.0, 5,
                Set.of(FormFactor.ATX, FormFactor.MICRO_ATX), Set.of(PsuFormFactor.ATX),
                365, 165, 7, 2, Set.of(240, 280));
    }

    /** Cambia anche gli insiemi: meno form factor, due psu, nessun radiatore. */
    @Override Case modified(Case saved) {
        return new Case(saved.id(), "Fractal", "North", 139.0, 8,
                Set.of(FormFactor.MINI_ITX), Set.of(PsuFormFactor.SFX, PsuFormFactor.SFX_L),
                330, 170, 7, 4, Set.of());
    }

    @Override Case missing() {
        return new Case(999, "X", "Y", 1.0, 0, Set.of(FormFactor.ATX), Set.of(PsuFormFactor.ATX), 300, 150, 7, 1, Set.of());
    }

    @Override String buildTable() { return "build_pc_case"; }
    @Override String buildIdColumn() { return "id_pc_case"; }

    @Test
    void setsAreStoredInTheLinkedTables() {
        dao.insert(sample("Uno"));

        assertEquals(2, count("SELECT count(*) FROM case_form_factor"));
        assertEquals(1, count("SELECT count(*) FROM case_psu_form_factor"));
        assertEquals(2, count("SELECT count(*) FROM case_radiator"));
    }

    @Test
    void deleteRemovesTheLinkedRows() {
        Case saved = dao.insert(sample("Uno"));
        dao.delete(saved.id());

        assertEquals(0, count("SELECT count(*) FROM case_form_factor"));
        assertEquals(0, count("SELECT count(*) FROM case_radiator"));
    }

    @Test
    void aCaseWithoutFormFactorsOrPsuFormsIsRejected() {
        Case noForms = new Case(0, "T", "NoForms", 1.0, 0, Set.of(), Set.of(PsuFormFactor.ATX), 300, 150, 7, 1, Set.of());
        Case noPsu = new Case(0, "T", "NoPsu", 1.0, 0, Set.of(FormFactor.ATX), Set.of(), 300, 150, 7, 1, Set.of());

        assertThrows(IllegalArgumentException.class, () -> dao.insert(noForms));
        assertThrows(IllegalArgumentException.class, () -> dao.insert(noPsu));
        assertEquals(0, count("SELECT count(*) FROM pc_case"));
    }
}
