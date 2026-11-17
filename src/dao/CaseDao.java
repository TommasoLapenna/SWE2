package dao;

import db.DatabaseConnection;
import model.attribute.ComponentType;
import model.attribute.FormFactor;
import model.attribute.PsuFormFactor;
import model.component.Case;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * Tabella: pc_case. I tre insiemi del record stanno in component_support (FORM_FACTOR,
 * PSU_FORM_FACTOR, RADIATOR_MM). Form factor della scheda madre e dell'alimentatore: almeno uno
 * ciascuno; radiatori: anche nessuno.
 */
public class CaseDao extends AbstractComponentDao<Case> {

    private static final String FORMS = "FORM_FACTOR";
    private static final String PSU_FORMS = "PSU_FORM_FACTOR";
    private static final String RADIATORS = "RADIATOR_MM";

    public CaseDao(DatabaseConnection database) {
        super(database, ComponentType.CASE, "pc_case",
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
        Set<FormFactor> forms = loadEnumSupport(connection, id, FORMS, FormFactor.class);
        Set<PsuFormFactor> psuForms = loadEnumSupport(connection, id, PSU_FORMS, PsuFormFactor.class);
        Set<Integer> radiators = loadIntSupport(connection, id, RADIATORS);
        return new Case(id, brand, model, price, powerDrawWatts, forms, psuForms,
                maxGpuLengthMm, maxCoolerHeightMm, expansionSlots, driveBays, radiators);
    }

    @Override
    protected void bindDetails(PreparedStatement ps, int first, Case c) throws SQLException {
        int i = first;
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
    protected void insertSupports(Connection connection, int id, Case c) throws SQLException {
        insertSupport(connection, id, FORMS, c.supportedForms());
        insertSupport(connection, id, PSU_FORMS, c.supportedPsuForms());
        insertSupport(connection, id, RADIATORS, c.supportedRadiatorsMm());
    }
}
