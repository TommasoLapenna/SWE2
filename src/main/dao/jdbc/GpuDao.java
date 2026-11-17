package main.dao.jdbc;

import db.DatabaseConnection;
import main.domain.model.component.Gpu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Tabella: gpu. Nel record la lunghezza si chiama lenghtMm (refuso per lengthMm) e la capacita'
 * della VRAM vramCapacityGb: nel database le colonne sono length_mm e vram_gb.
 */
public class GpuDao extends ComponentDao<Gpu> {

    public GpuDao(DatabaseConnection database) {
        super(database, "gpu", "id_gpu",
                "tdp_watts", "vram_gb", "length_mm", "slot_width", "recommended_psu_watts", "power_connectors");
    }

    public GpuDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Gpu map(Connection connection, ResultSet rs) throws SQLException {
        return new Gpu(rs.getInt("id_gpu"), rs.getString("brand"), rs.getString("model"), rs.getDouble("price"),
                rs.getInt("tdp_watts"), rs.getInt("vram_gb"), rs.getInt("length_mm"),
                rs.getDouble("slot_width"), rs.getInt("recommended_psu_watts"), rs.getInt("power_connectors"));
    }

    @Override
    protected void bind(PreparedStatement ps, Gpu gpu) throws SQLException {
        int i = bindCommon(ps, gpu);
        ps.setInt(i++, gpu.tdpWatts());
        ps.setInt(i++, gpu.vramCapacityGb());
        ps.setInt(i++, gpu.lenghtMm());
        ps.setDouble(i++, gpu.slotWidth());
        ps.setInt(i++, gpu.recommendedPsuWatts());
        ps.setInt(i, gpu.powerConnectors());
    }

    @Override
    protected Gpu withId(Gpu g, int id) {
        return new Gpu(id, g.brand(), g.model(), g.price(), g.tdpWatts(), g.vramCapacityGb(),
                g.lenghtMm(), g.slotWidth(), g.recommendedPsuWatts(), g.powerConnectors());
    }
}
