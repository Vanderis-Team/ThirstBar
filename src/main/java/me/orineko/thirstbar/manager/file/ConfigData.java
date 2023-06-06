package me.orineko.thirstbar.manager.file;

import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.Method;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class ConfigData {

    public static double THIRSTY_MAX;
    public static double THIRSTY_REDUCE;
    public static long THIRSTY_TIME;
    public static double THIRSTY_DAMAGE;
    public static long COOLDOWN_REFRESH;
    public static List<String> DISABLE_WORLDS;
    public static HashMap<String, Double> FLAG_REDUCE;
    public static boolean FOOD;
    public static boolean BOSS_BAR_ENABLE;
    private static String BOSS_BAR_TITLE;
    public static String BOSS_BAR_COLOR;
    public static String BOSS_BAR_STYLE;
    public static boolean ACTION_BAR_ENABLE;
    private static String ACTION_BAR_TITLE;

    private final FileConfiguration configFile;

    public ConfigData(){
        this.configFile = ThirstBar.getInstance().getConfig();
        THIRSTY_MAX = Math.max(1, configFile.getDouble("Thirsty.Max", 1));
        THIRSTY_REDUCE = Math.max(1, configFile.getDouble("Thirsty.Reduce", 1));
        THIRSTY_TIME = Math.max(1, configFile.getLong("Thirsty.Time", 1));
        THIRSTY_DAMAGE = Math.max(0, configFile.getDouble("Thirsty.Damage", 0));
        COOLDOWN_REFRESH = Math.max(0, configFile.getLong("CooldownRefresh", 0));
        DISABLE_WORLDS = configFile.getStringList("DisableWorlds");
        FLAG_REDUCE = new HashMap<>();
        configFile.getStringList("FlagReduce").forEach(s -> {
            String[] arr = s.split(":");
            if(arr.length <= 1) return;
            String flag = arr[0].trim();
            double reduce = MethodDefault.formatNumber(arr[1].trim(), 0);
            if(reduce <= 0) return;
            FLAG_REDUCE.put(flag, reduce);
        });
        FOOD = configFile.getBoolean("Food", false);
        BOSS_BAR_ENABLE = configFile.getBoolean("BossBar.Enable", false);
        BOSS_BAR_TITLE = MethodDefault.formatColor(configFile.getString("BossBar.Title", ""));
        BOSS_BAR_COLOR = configFile.getString("BossBar.Color", "BLUE");
        BOSS_BAR_STYLE = configFile.getString("BossBar.Style", "SEGMENTED_10");
        ACTION_BAR_ENABLE = configFile.getBoolean("ActionBar.Enable", false);
        ACTION_BAR_TITLE = MethodDefault.formatColor(configFile.getString("ActionBar.Title", ""));
    }

    public FileConfiguration getConfigFile() {
        return configFile;
    }

    public static String BOSS_BAR_TEXT(double value, double max, double reduce, long time){
        return replace(BOSS_BAR_TITLE, value, max, reduce, time);
    }

    public static String ACTION_BAR_TEXT(double value, double max, double reduce, long time){
        return replace(ACTION_BAR_TITLE, value, max, reduce, time);
    }

    public static String replace(@Nonnull String text, double value, double max, double reduce, long time){
        return text.replace("<value>", Method.changeDoubleToInt(value))
                .replace("<max>", Method.changeDoubleToInt(max))
                .replace("<reduce>", Method.changeDoubleToInt(reduce))
                .replace("<time>", Method.changeDoubleToInt(time));
    }
}
