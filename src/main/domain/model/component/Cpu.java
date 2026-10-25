package main.domain.model.component;

import main.domain.model.attribute.ComponentType;
import main.domain.model.attribute.MemoryGen;
import main.domain.model.attribute.Socket;

import java.util.Set;

public record Cpu
        (
                int id, String brand, String model, double price, int tdpWatts,
                Socket socket,
                int cores,
                int threads,
                double baseClockGhz,
                Set<MemoryGen> memoryGen,
                boolean integratedGpu,
                boolean coolerIncluded

        ) implements Component {

    @Override
    public ComponentType type() {
        return ComponentType.CPU;
    }

    @Override
    public int powerDrawWatts() { return tdpWatts; }
}