package test.model.component;

import main.model.attribute.MemoryGen;
import main.model.component.Memory;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class MemoryTest {
    private Memory test;

    @BeforeEach
    void setUp() {
        test = new Memory(1, "Crosair", "Vengance", 180.00, MemoryGen.DDR5, 2, 8, 3200);
    }

    @Test
    void totalRamCapcity() {
        Assert.assertEquals(16, test.totalCapacityGb());
    }
}