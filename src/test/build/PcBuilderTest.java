package test.build;

import main.domain.model.PcBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PcBuilderTest {
    private PcBuilder build;

    @BeforeEach
    void setUp() {
        build = new PcBuilder("testbuild");
    }

    @Test
    void name() {
        Assertions.assertEquals("testbuild", build.name());
    }
}
