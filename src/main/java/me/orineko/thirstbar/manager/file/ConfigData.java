package me.orineko.thirstbar.manager.file;

import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.Method;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class ConfigData {

    public static boolean STOP_DRINKING;
    public static double THIRSTY_MAX;
    public static double THIRSTY_REDUCE;
    public static long THIRSTY_TIME;
    public static double THIRSTY_DAMAGE;
    public static long COOLDOWN_REFRESH;
    public static List<String> DISABLED_GAMEMODE;
    public static List<String> DISABLED_WORLDS;
    public static HashMap<String, Double> FLAG_REDUCE;
    public static boolean REPLACE_HUNGER;
    public static boolean BOSS_BAR_ENABLE;
    private static String BOSS_BAR_TITLE;
    private static String BOSS_BAR_DISABLE_TITLE;
    public static String BOSS_BAR_COLOR;
    public static String BOSS_BAR_STYLE;
    public static boolean ACTION_BAR_ENABLE;
    private static String ACTION_BAR_TITLE;
    private static String ACTION_BAR_DISABLE_TITLE;
    public static List<String> MATERIALS;

    private final FileConfiguration configFile;

    public ConfigData(){
        this.configFile = ThirstBar.getInstance().getConfig();
        STOP_DRINKING = configFile.getBoolean("StopDrinking", false);
        THIRSTY_MAX = Math.max(1, configFile.getDouble("Thirsty.Max", 1));
        THIRSTY_REDUCE = Math.max(1, configFile.getDouble("Thirsty.Reduce", 1));
        THIRSTY_TIME = Math.max(1, configFile.getLong("Thirsty.Time", 1));
        THIRSTY_DAMAGE = Math.max(0, configFile.getDouble("Thirsty.Damage", 0));
        COOLDOWN_REFRESH = Math.max(0, configFile.getLong("CooldownRefresh", 0));
        DISABLED_GAMEMODE = configFile.getStringList("DisabledGamemode");
        DISABLED_WORLDS = configFile.getStringList("DisabledWorlds");
        FLAG_REDUCE = new HashMap<>();
        configFile.getStringList("FlagReduce").forEach(s -> {
            String[] arr = s.split(":");
            if(arr.length <= 1) return;
            String flag = arr[0].trim();
            double reduce = MethodDefault.formatNumber(arr[1].trim(), 0);
            if(reduce <= 0) return;
            FLAG_REDUCE.put(flag, reduce);
        });
        REPLACE_HUNGER = configFile.getBoolean("ReplaceHunger", false);
        BOSS_BAR_ENABLE = configFile.getBoolean("BossBar.Enable", false);
        BOSS_BAR_TITLE = MethodDefault.formatColor(configFile.getString("BossBar.Title", ""));
        BOSS_BAR_DISABLE_TITLE = MethodDefault.formatColor(configFile.getString("BossBar.DisableTitle", ""));
        BOSS_BAR_COLOR = configFile.getString("BossBar.Color", "BLUE");
        BOSS_BAR_STYLE = configFile.getString("BossBar.Style", "SEGMENTED_10");
        ACTION_BAR_ENABLE = configFile.getBoolean("ActionBar.Enable", false);
        ACTION_BAR_TITLE = MethodDefault.formatColor(configFile.getString("ActionBar.Title", ""));
        ACTION_BAR_DISABLE_TITLE = MethodDefault.formatColor(configFile.getString("ActionBar.DisableTitle", ""));
        MATERIALS = configFile.getStringList("Materials");
    }

    public FileConfiguration getConfigFile() {
        return configFile;
    }

    public static String BOSS_BAR_TEXT(double value, double max, double reduce, double time){
        return replace(BOSS_BAR_TITLE, value, max, reduce, time);
    }

    public static String BOSS_BAR_DISABLE_TEXT(double value, double max, double reduce, double time){
        return replace(BOSS_BAR_DISABLE_TITLE, value, max, reduce, time);
    }

    public static String ACTION_BAR_TEXT(double value, double max, double reduce, double time){
        return replace(ACTION_BAR_TITLE, value, max, reduce, time);
    }

    public static String ACTION_BAR_DISABLE_TEXT(double value, double max, double reduce, double time){
        return replace(ACTION_BAR_DISABLE_TITLE, value, max, reduce, time);
    }

    public static String replace(@Nonnull String text, double value, double max, double reduce, double time){
        String timeChange;
        if(time == (long) time) timeChange = String.valueOf((long) time);
        else timeChange = String.format("%.2f", time).replaceAll("0*$", "").replaceAll("\\.$", "");
        return text.replace("<value>", Method.changeDoubleToInt(Math.max(value, 0)))
                .replace("<max>", Method.changeDoubleToInt(max))
                .replace("<reduce>", Method.changeDoubleToInt(reduce))
                .replace("<time>", timeChange);
    }
}
