package me.orineko.thirstbar.manager.stage;

import org.jetbrains.annotations.NotNull;

public class StageConfig extends Stage{

    private boolean enable;
    private long delay;
    private double value;
    private long duration;

    public StageConfig(@NotNull String name) {
        super(name);
    }

    public boolean isEnable() {
        return enable;
    }

    public long getDelay() {
        return delay;
    }

    public double getValue() {
        return value;
    }

    public long getDuration() {
        return duration;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
