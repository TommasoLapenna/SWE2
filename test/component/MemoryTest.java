package component;

import main.domain.model.attribute.MemoryGen;
import main.domain.model.component.Memory;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals(16, test.totalCapacityGb());
    }
}