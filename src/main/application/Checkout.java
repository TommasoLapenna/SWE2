package main.application;

import main.build.BuildView;
import main.build.PcBuilder;

public class Checkout {
    PcBuilder build;
    public Checkout(PcBuilder build) {
        this.build = build;
    }

    public BuildView getBuild() {
        return build.view();
    }
}
