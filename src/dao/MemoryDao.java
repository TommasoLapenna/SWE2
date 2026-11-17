package dao;

import db.DatabaseConnection;
import model.attribute.ComponentType;
import model.attribute.MemoryGen;
import model.component.Memory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tabella: memory (un record e' un KIT di sticksQty moduli). */
public class MemoryDao extends AbstractComponentDao<Memory> {

    public MemoryDao(DatabaseConnection database) {
        super(database, ComponentType.RAM, "memory",
                "memory_gen", "sticks_qty", "stick_capacity_gb", "frequency_mhz");
    }

    public MemoryDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Memory map(Connection connection, ResultSet rs) throws SQLException {
        return new Memory(rs.getInt(ID), rs.getString("brand"), rs.getString("model"), rs.getDouble("price"),
                MemoryGen.valueOf(rs.getString("memory_gen")),
                rs.getInt("sticks_qty"), rs.getInt("stick_capacity_gb"), rs.getInt("frequency_mhz"));
    }

    @Override
    protected void bindDetails(PreparedStatement ps, int first, Memory memory) throws SQLException {
        int i = first;
        ps.setString(i++, memory.memoryGen().name());
        ps.setInt(i++, memory.sticksQty());
        ps.setInt(i++, memory.stickCapacityGb());
        ps.setInt(i, memory.frequencyMhz());
    }

    @Override
    protected Memory withId(Memory m, int id) {
        return new Memory(id, m.brand(), m.model(), m.price(), m.memoryGen(),
                m.sticksQty(), m.stickCapacityGb(), m.frequencyMhz());
    }
}
