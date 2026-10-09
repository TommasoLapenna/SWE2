package model.component;

import model.attribute.ComponentType;
import model.attribute.CoolerType;
import model.attribute.Socket;

import java.util.Set;

public record Cooler (
        int id, String brand, String model, double price, int powerDrawWatts,
        CoolerType coolingType,
        Set<Socket> supportedSocktes,
        int maxTdp,
        int heightMm, // For AIR ones
        int radiatorSizeMm // For LIQUID ones, TODO checks on them
    ) implements Component {

    @Override
    public ComponentType type() { return ComponentType.COOLING; }

    @Override
    public int powerDrawWatts() { return 5; }
}
