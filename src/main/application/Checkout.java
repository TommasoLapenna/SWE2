package main.application;

import main.domain.model.BuildView;
import main.domain.model.PcBuilder;

public class Checkout {
    PcBuilder build;
    public Checkout(PcBuilder build) {
        this.build = build;
    }

    public BuildView getBuild() {
        return build.view();
    }
}
