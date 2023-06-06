package me.orineko.thirstbar.manager.stage;

import org.jetbrains.annotations.NotNull;

public class StageTimeline extends Stage{

    private double thirstMin;
    private double thirstMax;

    public StageTimeline(@NotNull String name) {
        super(name);
    }

    public double getThirstMin() {
        return thirstMin;
    }

    public void setThirstMin(double thirstMin) {
        this.thirstMin = thirstMin;
    }

    public double getThirstMax() {
        return thirstMax;
    }

    public void setThirstMax(double thirstMax) {
        this.thirstMax = thirstMax;
    }
}
