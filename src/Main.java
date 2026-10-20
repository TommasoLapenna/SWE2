import application.PcBuildSession;
import build.rule.Severity;
import build.rule.Violation;
import model.attribute.*;
import model.component.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Walkthrough of a realistic build session. Component data is illustrative, not vendor data.
 * The story deliberately triggers SOME rules (wrong RAM generation, wrong case, oversized GPU, undersized PSU)
 * and leaves most of the catalogue untouched, while exercising Memento-based undo/redo.
 */
public class Main {

    void main() {
        PcBuildSession session = new PcBuildSession("Gaming rig");

        // ---------------------------------------------------------------- catalogue
        Component ryzen   = new Cpu(1, "AMD", "Ryzen 7 7700X", 299.0, 105, Socket.AM5, 8, 16, 4.5, Set.of(MemoryGen.DDR5), true, false);
        Component b850    = new Motherboard(2, "ASUS", "TUF GAMING B850-PLUS", 239.0, Socket.AM5, Chipset.B850.name(), FormFactor.ATX, MemoryGen.DDR5, 4, 192, 8000, 2, 3, 4);
        Component ddr5    = new Memory(3, "Corsair", "Vengeance DDR5-6000 2x16GB", 109.0, MemoryGen.DDR5, 2, 16, 6000);
        Component ddr4    = new Memory(4, "Kingston", "Fury DDR4-3200 2x8GB", 49.0, MemoryGen.DDR4, 2, 8, 3200);
        Component nvme    = new Storage(5, "Samsung", "990 Pro 1TB", 99.0, StorageType.SDD, StorageTech.M2_NVME, 1000);
        Component hdd     = new Storage(6, "Seagate", "Barracuda 2TB", 55.0, StorageType.HDD, StorageTech.SATA, 2000);
        Component itxCase = new Case(7, "Cooler Master", "NR200P", 99.0, 5, Set.of(FormFactor.MINI_ITX), Set.of(PsuFormFactor.SFX, PsuFormFactor.SFX_L), 330, 155, 2, 2, Set.of(240, 280));
        Component atxCase = new Case(8, "Fractal Design", "Meshify 2 Compact", 129.0, 6, Set.of(FormFactor.MICRO_ATX, FormFactor.ATX), Set.of(PsuFormFactor.ATX), 330, 169, 7, 4, Set.of(240, 280, 360));
        Component rtx4090 = new Gpu(9, "ASUS", "RTX 4090 TUF", 1799.0, 450, 24, 348, 3.65, 850, 3);
        Component rtx4070 = new Gpu(10, "Gigabyte", "RTX 4070 Super WindForce", 619.0, 220, 12, 261, 2.5, 650, 1);
        Component psu550  = new PowerSupply(11, "Corsair", "CX550", 69.0, 550, EfficencyRating.BRONZE, PsuFormFactor.ATX, 2);
        Component psu750  = new PowerSupply(12, "Corsair", "RM750e", 109.0, 750, EfficencyRating.GOLD, PsuFormFactor.ATX, 4);
        Component noctua  = new Cooler(13, "Noctua", "NH-D15", 109.0, 5, CoolerType.AIR, Set.of(Socket.AM5, Socket.LGA1700), 250, 165, 0);
        Component monitor = new Peripherals(14, "LG", "UltraGear 27GP850", 329.0, 40);

        // ---------------------------------------------------------------- 1. start: only completeness rules can fire
        step("1. Empty build: only the cardinality rules fire (evaluateAll at construction)");
        session.print();

        // ---------------------------------------------------------------- 2. core platform
        step("2. Add CPU and motherboard (the CPU ships without a cooler, so MISSING-COOLER appears)");
        session.add(ryzen);
        session.add(b850);
        session.print();

        // ---------------------------------------------------------------- 3. mistake + undo
        step("3. Mistake: DDR4 memory on a DDR5 platform -> RAM generation rules fire");
        session.add(ddr4);
        session.print();

        step("4. undo(): the Memento taken before add(ddr4) is restored, the violations disappear");
        System.out.println("undo -> " + session.undo());
        session.print();

        // ---------------------------------------------------------------- 4. correct parts
        step("5. Add the right RAM and two drives (1 NVMe + 1 SATA HDD: slots and ports are enough)");
        session.add(ddr5);
        session.add(nvme);
        session.add(hdd);
        session.print();

        // ---------------------------------------------------------------- 5. wrong case, undo/redo walk
        step("6. Mistake: a mini-ITX case for an ATX board");
        session.add(itxCase);
        session.print();

        step("7. undo, redo, undo: walking the history back and forth");
        System.out.println("undo -> " + session.undo() + "   (case removed)");
        System.out.println("redo -> " + session.redo() + "   (case is back, FORM-FACTOR-MISMATCH again)");
        System.out.println("undo -> " + session.undo() + "   (case removed again)");
        summary(session);

        step("8. A NEW action after an undo discards the redo branch (Memento history is linear)");
        session.add(atxCase);
        System.out.println("redo -> " + session.redo() + "   (nothing to redo: the itx-case branch is gone)");
        session.print();

        // ---------------------------------------------------------------- 6. GPU and PSU trouble
        step("9. Add an RTX 4090 and a 550 W PSU: length, wattage, recommendation and connector rules fire");
        session.add(rtx4090);
        session.add(psu550);
        session.print();

        step("10. Swap the GPU: remove the 4090, add the 4070 Super (two history entries)");
        session.remove(rtx4090);
        session.add(rtx4070);
        session.print();

        // ---------------------------------------------------------------- 7. finishing touches
        step("11. Add the air cooler and the monitor");
        session.add(noctua);
        session.add(monitor);
        session.print();

        step("12. Try a bigger PSU, then compare via undo/redo");
        session.remove(psu550);
        session.add(psu750);
        summary(session);
        System.out.println("undo -> " + session.undo() + "   (psu750 removed)");
        System.out.println("undo -> " + session.undo() + "   (psu550 is back)");
        summary(session);
        System.out.println("redo -> " + session.redo());
        System.out.println("redo -> " + session.redo() + "   (psu750 installed again)");

        step("Final build");
        session.print();
        summary(session);
    }

    private static void step(String title) {
        System.out.println("\n>>> " + title);
    }

    /** One-line digest: draw and number of violations per severity. */
    private static void summary(PcBuildSession session) {
        Map<Severity, Integer> bySeverity = new EnumMap<>(Severity.class);
        for (Violation v : session.getViolations())
            bySeverity.merge(v.severity(), 1, Integer::sum);
        System.out.println("   [draw=" + session.getBuild().totalWattage() + " W, violations=" + bySeverity + "]");
    }
}