package dao;

import db.DatabaseConnection;
import model.attribute.ComponentType;
import model.attribute.FormFactor;
import model.attribute.MemoryGen;
import model.attribute.Socket;
import model.component.Motherboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Tabella: motherboard. Il chipset e' una String nel record: un valore che non e' tra quelli
 * dell'enum Chipset (elencati nel CHECK della tabella) viene rifiutato dal database
 * (PersistenceException).
 */
public class MotherboardDao extends AbstractComponentDao<Motherboard> {

    public MotherboardDao(DatabaseConnection database) {
        super(database, ComponentType.MOTHERBOARD, "motherboard",
                "socket", "chipset", "form_factor", "memory_gen", "ram_slots",
                "max_memory_gb", "max_memory_speed_mhz", "pcie_x16_slots", "m2_slots", "sata_ports");
    }

    public MotherboardDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Motherboard map(Connection connection, ResultSet rs) throws SQLException {
        return new Motherboard(rs.getInt(ID), rs.getString("brand"), rs.getString("model"),
                rs.getDouble("price"),
                Socket.valueOf(rs.getString("socket")),
                rs.getString("chipset"),
                FormFactor.valueOf(rs.getString("form_factor")),
                MemoryGen.valueOf(rs.getString("memory_gen")),
                rs.getInt("ram_slots"), rs.getInt("max_memory_gb"), rs.getInt("max_memory_speed_mhz"),
                rs.getInt("pcie_x16_slots"), rs.getInt("m2_slots"), rs.getInt("sata_ports"));
    }

    @Override
    protected void bindDetails(PreparedStatement ps, int first, Motherboard m) throws SQLException {
        int i = first;
        ps.setString(i++, m.socket().name());
        ps.setString(i++, m.supportedChipset());
        ps.setString(i++, m.formFactor().name());
        ps.setString(i++, m.supportedMemoryGen().name());
        ps.setInt(i++, m.ramSlots());
        ps.setInt(i++, m.maxMemoryGb());
        ps.setInt(i++, m.maxMemorySpeedMhz());
        ps.setInt(i++, m.pcieX16Slots());
        ps.setInt(i++, m.m2Slots());
        ps.setInt(i, m.sataPorts());
    }

    @Override
    protected Motherboard withId(Motherboard m, int id) {
        return new Motherboard(id, m.brand(), m.model(), m.price(), m.socket(), m.supportedChipset(),
                m.formFactor(), m.supportedMemoryGen(), m.ramSlots(), m.maxMemoryGb(), m.maxMemorySpeedMhz(),
                m.pcieX16Slots(), m.m2Slots(), m.sataPorts());
    }
}
