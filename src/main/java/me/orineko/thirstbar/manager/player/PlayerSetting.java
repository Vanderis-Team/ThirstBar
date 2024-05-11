package me.orineko.thirstbar.manager.player;

import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.manager.file.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import javax.annotation.Nonnull;

public class PlayerSetting {

    private boolean disable;
    private boolean disableAll;
    protected boolean enableFood;
    protected boolean enableBossBar;
    private String titleBossBar;
    private BarColor colorBossBar;
    private BarStyle styleBossBar;
    protected boolean enableActionBar;
    protected String titleActionBar;
    protected BossBar bossBar;

    public PlayerSetting(){
        disable = false;
        disableAll = false;
        enableFood = ConfigData.REPLACE_HUNGER;
        enableBossBar = ConfigData.BOSS_BAR_ENABLE;
        titleBossBar = "";
        try {colorBossBar = BarColor.valueOf(ConfigData.BOSS_BAR_COLOR);
        } catch (IllegalArgumentException ignore){colorBossBar = BarColor.BLUE;}
        try {styleBossBar = BarStyle.valueOf(ConfigData.BOSS_BAR_STYLE);
        } catch (IllegalArgumentException ignore){styleBossBar = BarStyle.SEGMENTED_10;}
        enableActionBar = ConfigData.ACTION_BAR_ENABLE;
        titleActionBar = "";
        this.bossBar = Bukkit.createBossBar(titleBossBar, colorBossBar, styleBossBar);
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public void setDisableAll(boolean disableAll) {
        this.disableAll = disableAll;
    }

    public void setEnableFood(boolean enableFood) {
        this.enableFood = enableFood;
    }

    public void setEnableBossBar(boolean enableBossBar){
        this.enableActionBar = enableBossBar;
    }

    public void setEnableActionBar(boolean enableActionBar){
        this.enableActionBar = enableActionBar;
        if(!enableActionBar) setTitleActionBar("");
    }

    public void setColorBossBar(@Nonnull BarColor colorBossBar) {
        this.colorBossBar = colorBossBar;
        bossBar.setColor(colorBossBar);
    }

    public void setStyleBossBar(@Nonnull BarStyle styleBossBar) {
        this.styleBossBar = styleBossBar;
        bossBar.setStyle(styleBossBar);
    }

    public void setTitleBossBar(@Nonnull String titleBossBar) {
        this.titleBossBar = titleBossBar;
        bossBar.setTitle(titleBossBar);
    }

    public void setTitleActionBar(@Nonnull String titleActionBar) {
        this.titleActionBar = titleActionBar;
    }

    public void setTitleBossBar(double value, double max, double reduce, double time) {
        setTitleBossBar(ConfigData.BOSS_BAR_TEXT(value, max, reduce, time));
    }

    public void setTitleDisableBossBar(double value, double max, double reduce, double time) {
        setTitleBossBar(ConfigData.BOSS_BAR_DISABLE_TEXT(value, max, reduce, time));
    }

    public void setTitleBossBar(@Nonnull String text, double value, double max, double reduce, double time) {
        setTitleBossBar(MethodDefault.formatColor(ConfigData.replace(text, value, max, reduce, time)));
    }

    public void setTitleActionBar(double value, double max, double reduce, double time) {
        setTitleActionBar(ConfigData.ACTION_BAR_TEXT(value, max, reduce, time));
    }

    public void setTitleDisableActionBar(double value, double max, double reduce, double time) {
        setTitleActionBar(ConfigData.ACTION_BAR_DISABLE_TEXT(value, max, reduce, time));
    }

    public void setTitleActionBar(@Nonnull String text, double value, double max, double reduce, double time) {
        setTitleActionBar(MethodDefault.formatColor(ConfigData.replace(text, value, max, reduce, time)));
    }

    public boolean isDisable() {
        return disable;
    }

    public boolean isDisableAll() {
        return disableAll;
    }

    public boolean isEnableFood() {
        return enableFood;
    }

    public boolean isEnableBossBar() {
        return enableBossBar;
    }

    public boolean isEnableActionBar() {
        return enableActionBar;
    }

    public String getTitleBossBar() {
        return titleBossBar;
    }

    public BarColor getColorBossBar() {
        return colorBossBar;
    }

    public BarStyle getStyleBossBar() {
        return styleBossBar;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public String getTitleActionBar() {
        return titleActionBar;
    }
}
