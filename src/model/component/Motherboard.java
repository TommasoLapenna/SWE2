package model.component;

import model.attribute.*;

public record Motherboard (
        int id, String brand, String model, double price,
        Socket socket,
        String supportedChipset,
        FormFactor formFactor,
        MemoryGen supportedMemoryGen,
        //int cpuSockets,
        int ramSlots,
        int maxMemoryGb,
        int maxMemorySpeedMhz,
        int pcieX16Slots,
        int m2Slots,
        int sataPorts
    ) implements Component {

    @Override
    public ComponentType type() { return ComponentType.MOTHERBOARD; }

    @Override
    public int powerDrawWatts() { return ConstantPowerDraw.MOBO_DEFAULT_POWER_DRAW; }
}
