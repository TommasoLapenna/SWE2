package main.dao.jdbc;

import db.DatabaseConnection;
import main.domain.model.attribute.FormFactor;
import main.domain.model.attribute.PsuFormFactor;
import main.domain.model.component.Case;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * Tabelle: pc_case e tre tabelle collegate per gli insiemi del record:
 * case_form_factor, case_psu_form_factor e case_radiator.
 * Form factor della scheda madre e dell'alimentatore: almeno uno ciascuno; radiatori: anche nessuno.
 */
public class CaseDao extends ComponentDao<Case> {

    private static final String ID = "id_pc_case";

    public CaseDao(DatabaseConnection database) {
        super(database, "pc_case", ID,
                "power_draw_watts", "max_gpu_length_mm", "max_cooler_height_mm", "expansion_slots", "drive_bays");
    }

    public CaseDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Case map(Connection connection, ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        double price = rs.getDouble("price");
        int powerDrawWatts = rs.getInt("power_draw_watts");
        int maxGpuLengthMm = rs.getInt("max_gpu_length_mm");
        int maxCoolerHeightMm = rs.getInt("max_cooler_height_mm");
        int expansionSlots = rs.getInt("expansion_slots");
        int driveBays = rs.getInt("drive_bays");
        Set<FormFactor> forms = ChildRows.loadEnums(connection, "case_form_factor", ID, "form_factor", id, FormFactor.class);
        Set<PsuFormFactor> psuForms = ChildRows.loadEnums(connection, "case_psu_form_factor", ID, "psu_form_factor", id, PsuFormFactor.class);
        Set<Integer> radiators = ChildRows.loadInts(connection, "case_radiator", ID, "radiator_mm", id);
        return new Case(id, brand, model, price, powerDrawWatts, forms, psuForms,
                maxGpuLengthMm, maxCoolerHeightMm, expansionSlots, driveBays, radiators);
    }

    @Override
    protected void bind(PreparedStatement ps, Case c) throws SQLException {
        int i = bindCommon(ps, c);
        ps.setInt(i++, c.powerDrawWatts());
        ps.setInt(i++, c.maxGpuLengthMm());
        ps.setInt(i++, c.maxCoolerHeightMm());
        ps.setInt(i++, c.expansionSlots());
        ps.setInt(i, c.driveBays());
    }

    @Override
    protected Case withId(Case c, int id) {
        return new Case(id, c.brand(), c.model(), c.price(), c.powerDrawWatts(), c.supportedForms(),
                c.supportedPsuForms(), c.maxGpuLengthMm(), c.maxCoolerHeightMm(), c.expansionSlots(),
                c.driveBays(), c.supportedRadiatorsMm());
    }

    @Override
    protected void validate(Case c) {
        if (c.supportedForms() == null || c.supportedForms().isEmpty()) {
            throw new IllegalArgumentException("Un case deve supportare almeno un form factor di scheda madre: " + c.name());
        }
        if (c.supportedPsuForms() == null || c.supportedPsuForms().isEmpty()) {
            throw new IllegalArgumentException("Un case deve supportare almeno un form factor di alimentatore: " + c.name());
        }
        if (c.supportedRadiatorsMm() == null) {
            throw new IllegalArgumentException("supportedRadiatorsMm non puo' essere null (al massimo vuoto): " + c.name());
        }
    }

    @Override
    protected void insertChildren(Connection connection, int id, Case c) throws SQLException {
        ChildRows.insertEnums(connection, "case_form_factor", ID, "form_factor", id, c.supportedForms());
        ChildRows.insertEnums(connection, "case_psu_form_factor", ID, "psu_form_factor", id, c.supportedPsuForms());
        ChildRows.insertInts(connection, "case_radiator", ID, "radiator_mm", id, c.supportedRadiatorsMm());
    }

    @Override
    protected void deleteChildren(Connection connection, int id) throws SQLException {
        ChildRows.deleteAll(connection, "case_form_factor", ID, id);
        ChildRows.deleteAll(connection, "case_psu_form_factor", ID, id);
        ChildRows.deleteAll(connection, "case_radiator", ID, id);
    }
}
