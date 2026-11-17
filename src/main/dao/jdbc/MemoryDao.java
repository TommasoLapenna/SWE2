package main.dao.jdbc;

import db.DatabaseConnection;
import main.domain.model.attribute.MemoryGen;
import main.domain.model.component.Memory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tabella: memory (un record e' un KIT di sticksQty moduli). */
public class MemoryDao extends ComponentDao<Memory> {

    public MemoryDao(DatabaseConnection database) {
        super(database, "memory", "id_memory", "memory_gen", "sticks_qty", "stick_capacity_gb", "frequency_mhz");
    }

    public MemoryDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Memory map(Connection connection, ResultSet rs) throws SQLException {
        return new Memory(rs.getInt("id_memory"), rs.getString("brand"), rs.getString("model"), rs.getDouble("price"),
                MemoryGen.valueOf(rs.getString("memory_gen")),
                rs.getInt("sticks_qty"), rs.getInt("stick_capacity_gb"), rs.getInt("frequency_mhz"));
    }

    @Override
    protected void bind(PreparedStatement ps, Memory memory) throws SQLException {
        int i = bindCommon(ps, memory);
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
