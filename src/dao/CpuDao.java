package dao;

import db.DatabaseConnection;
import model.attribute.ComponentType;
import model.attribute.MemoryGen;
import model.attribute.Socket;
import model.component.Cpu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/** Tabella: cpu. Le generazioni di memoria supportate stanno in component_support (MEMORY_GEN). */
public class CpuDao extends AbstractComponentDao<Cpu> {

    private static final String MEMORY_GENS = "MEMORY_GEN";

    public CpuDao(DatabaseConnection database) {
        super(database, ComponentType.CPU, "cpu",
                "socket", "cores", "threads", "base_clock_ghz", "tdp_watts", "integrated_gpu", "cooler_included");
    }

    public CpuDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Cpu map(Connection connection, ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        double price = rs.getDouble("price");
        Socket socket = Socket.valueOf(rs.getString("socket"));
        int cores = rs.getInt("cores");
        int threads = rs.getInt("threads");
        double baseClockGhz = rs.getDouble("base_clock_ghz");
        int tdpWatts = rs.getInt("tdp_watts");
        boolean integratedGpu = rs.getInt("integrated_gpu") == 1;
        boolean coolerIncluded = rs.getInt("cooler_included") == 1;
        Set<MemoryGen> memoryGens = loadEnumSupport(connection, id, MEMORY_GENS, MemoryGen.class);
        return new Cpu(id, brand, model, price, tdpWatts, socket, cores, threads, baseClockGhz,
                memoryGens, integratedGpu, coolerIncluded);
    }

    @Override
    protected void bindDetails(PreparedStatement ps, int first, Cpu cpu) throws SQLException {
        int i = first;
        ps.setString(i++, cpu.socket().name());
        ps.setInt(i++, cpu.cores());
        ps.setInt(i++, cpu.threads());
        ps.setDouble(i++, cpu.baseClockGhz());
        ps.setInt(i++, cpu.tdpWatts());
        ps.setInt(i++, cpu.integratedGpu() ? 1 : 0);
        ps.setInt(i, cpu.coolerIncluded() ? 1 : 0);
    }

    @Override
    protected Cpu withId(Cpu cpu, int id) {
        return new Cpu(id, cpu.brand(), cpu.model(), cpu.price(), cpu.tdpWatts(), cpu.socket(),
                cpu.cores(), cpu.threads(), cpu.baseClockGhz(), cpu.memoryGen(),
                cpu.integratedGpu(), cpu.coolerIncluded());
    }

    /** Minimo 1 generazione: nel database non si puo' esprimere, quindi lo garantisce il DAO. */
    @Override
    protected void validate(Cpu cpu) {
        if (cpu.memoryGen() == null || cpu.memoryGen().isEmpty()) {
            throw new IllegalArgumentException(
                    "Una CPU deve supportare almeno una generazione di memoria: " + cpu.name());
        }
    }

    @Override
    protected void insertSupports(Connection connection, int id, Cpu cpu) throws SQLException {
        insertSupport(connection, id, MEMORY_GENS, cpu.memoryGen());
    }
}
