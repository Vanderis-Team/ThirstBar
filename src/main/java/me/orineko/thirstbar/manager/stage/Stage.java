package me.orineko.thirstbar.manager.stage;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Stage {

    private final String name;
    private double reducePercent;
    private String titleActionBar;
    private String titleBossBar;
    private BarColor barColor;
    private BarStyle barStyle;
    private List<PotionEffect> potionEffectList;
    private List<String> actionList;

    public Stage(@Nonnull String name){
        this.name = name;
        this.potionEffectList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public double getReducePercent() {
        return reducePercent;
    }

    @Nullable
    public String getTitleActionBar() {
        return titleActionBar;
    }

    @Nullable
    public String getTitleBossBar() {
        return titleBossBar;
    }

    @Nullable
    public BarColor getBarColor() {
        return barColor;
    }

    @Nullable
    public BarStyle getBarStyle() {
        return barStyle;
    }

    public List<PotionEffect> getPotionEffectList() {
        return potionEffectList;
    }

    public List<String> getActionList() {
        return actionList;
    }

    public void setReducePercent(double reducePercent) {
        this.reducePercent = reducePercent;
    }

    public void setTitleActionBar(String titleActionBar) {
        this.titleActionBar = titleActionBar;
    }

    public void setTitleBossBar(String titleBossBar) {
        this.titleBossBar = titleBossBar;
    }

    public void setBarColor(BarColor barColor) {
        this.barColor = barColor;
    }

    public void setBarStyle(BarStyle barStyle) {
        this.barStyle = barStyle;
    }

    public void setPotionEffectList(List<PotionEffect> potionEffectList) {
        this.potionEffectList = potionEffectList;
    }

    public void setActionList(List<String> actionList) {
        this.actionList = actionList;
    }
}
