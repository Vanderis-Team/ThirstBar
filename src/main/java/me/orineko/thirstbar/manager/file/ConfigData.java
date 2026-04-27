package me.orineko.thirstbar.manager.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.orineko.pluginspigottools.utils.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.api.PlaceholderAPI;
import me.orineko.thirstbar.manager.ThirstBarMethod;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.stage.Stage;
import me.orineko.thirstbar.manager.stage.StageConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class ConfigData {

    public enum TypeResourceThirst {
        NORMAL, DEBUFF, RAW_WATTER
    }

    public static boolean STOP_DRINKING;
    public static boolean CUSTOM_ACTION_BAR_ENABLE;
    public static boolean CUSTOM_ACTION_BAR_ITEMSADDER_ENABLE;
    public static String CUSTOM_ACTION_BAR_ITEMSADDER_AUTO_DEPLOY_PATH;
    public static int CUSTOM_ACTION_BAR_ITEMSADDER_Y_POSITION;
    public static int CUSTOM_ACTION_BAR_ITEMSADDER_SCALE_RATIO;
    public static int CUSTOM_ACTION_BAR_ITEMSADDER_X_POSITION;
    public static String CUSTOM_ACTION_BAR_ITEMSADDER_ORIENTATION;
    public static float CUSTOM_FURNACE_EXP;
    public static int CUSTOM_FURNACE_COOKING_TIME;
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
    public static String NAME_RAW_POTION;
    public static List<String> LORE_RAW_POTION;
    public static int RED_COLOR_RAW_POTION;
    public static int GREEN_COLOR_RAW_POTION;
    public static int BLUE_COLOR_RAW_POTION;

    private final FileConfiguration configFile;

    public static final HashMap<TypeResourceThirst, List<ThirstCustomText>> resourcePackThirstMap = new HashMap<>();

    public ConfigData() {
        this.configFile = ThirstBar.getInstance().getConfig();

        CUSTOM_ACTION_BAR_ENABLE = configFile.getBoolean("CustomActionBar.Enable", false);
        CUSTOM_ACTION_BAR_ITEMSADDER_ENABLE = configFile.getString("CustomActionBar.Mode", "ITEMSADDER").equalsIgnoreCase("ITEMSADDER");
        CUSTOM_ACTION_BAR_ITEMSADDER_AUTO_DEPLOY_PATH = configFile
                .getString("CustomActionBar.AutoDeploy", "contents/thirstbar");
        CUSTOM_ACTION_BAR_ITEMSADDER_Y_POSITION = configFile.getInt("CustomActionBar.Y_Position", -7);
        CUSTOM_ACTION_BAR_ITEMSADDER_SCALE_RATIO = configFile.getInt("CustomActionBar.Scale_Ratio", 8);
        CUSTOM_ACTION_BAR_ITEMSADDER_X_POSITION = configFile.getInt("CustomActionBar.X_Position", 0);
        CUSTOM_ACTION_BAR_ITEMSADDER_ORIENTATION = configFile.getString("CustomActionBar.Orientation", "");
        if (CUSTOM_ACTION_BAR_ENABLE) {
            String thirst_normal = formatItemsAdder("normal_thirst", "\\ueea1");
            String thirstHalfLeft_normal = formatItemsAdder("normal_thirst_half_left", "\\ueea2");
            String thirstHalfRight_normal = formatItemsAdder("normal_thirst_half_right", "\\ueeb1");
            String thirstEmpty_normal = formatItemsAdder("normal_thirst_empty", "\\ueea3");

            String thirst_debuff = formatItemsAdder("debuff_thirst", "\\ueea4");
            String thirstHalfLeft_debuff = formatItemsAdder("debuff_thirst_half_left", "\\ueea5");
            String thirstHalfRight_debuff = formatItemsAdder("debuff_thirst_half_right", "\\ueeb2");
            String thirstEmpty_debuff = formatItemsAdder("debuff_thirst_empty", "\\ueea6");

            String thirst_raw = formatItemsAdder("raw_water_thirst", "\\ueea7");
            String thirstHalfLeft_raw = formatItemsAdder("raw_water_thirst_half_left", "\\ueea8");
            String thirstHalfRight_raw = formatItemsAdder("raw_water_thirst_half_right", "\\ueeb3");
            String thirstEmpty_raw = formatItemsAdder("raw_water_thirst_empty", "\\ueea9");

            setResourceThirst(TypeResourceThirst.NORMAL, thirst_normal, thirstHalfLeft_normal, thirstHalfRight_normal,
                    thirstEmpty_normal);
            setResourceThirst(TypeResourceThirst.DEBUFF, thirst_debuff, thirstHalfLeft_debuff, thirstHalfRight_debuff,
                    thirstEmpty_debuff);
            setResourceThirst(TypeResourceThirst.RAW_WATTER, thirst_raw, thirstHalfLeft_raw, thirstHalfRight_raw,
                    thirstEmpty_raw);
        } else {
            resourcePackThirstMap.clear();
        }
        CUSTOM_FURNACE_EXP = (float) configFile.getDouble("CustomFurnace.Exp", 0);
        CUSTOM_FURNACE_COOKING_TIME = configFile.getInt("CustomFurnace.CookingTime", 1);

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
            if (arr.length <= 1)
                return;
            String flag = arr[0].trim();
            double reduce = MethodDefault.formatNumber(arr[1].trim(), 0);
            if (reduce <= 0)
                return;
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
        NAME_RAW_POTION = MethodDefault.formatColor(configFile.getString("RawPotion.Name", ""));
        LORE_RAW_POTION = MethodDefault.formatColor(configFile.getStringList("RawPotion.Lore"));
        RED_COLOR_RAW_POTION = configFile.getInt("RawPotion.Color.Red", 0);
        GREEN_COLOR_RAW_POTION = configFile.getInt("RawPotion.Color.Green", 0);
        BLUE_COLOR_RAW_POTION = configFile.getInt("RawPotion.Color.Blue", 0);
    }

    public static String BOSS_BAR_TEXT(double value, double max, double reduce, double time) {
        return replace(BOSS_BAR_TITLE, value, max, reduce, time);
    }

    public static String BOSS_BAR_DISABLE_TEXT(double value, double max, double reduce, double time) {
        return replace(BOSS_BAR_DISABLE_TITLE, value, max, reduce, time);
    }

    public static String ACTION_BAR_TEXT(double value, double max, double reduce, double time) {
        if (ConfigData.CUSTOM_ACTION_BAR_ENABLE) {
            String resourceThirstText = getThirstCustomText(TypeResourceThirst.NORMAL, value, max, reduce, time);
            if (resourceThirstText != null)
                return resourceThirstText;
        }
        return replace(ACTION_BAR_TITLE, value, max, reduce, time);
    }

    public static String ACTION_BAR_DISABLE_TEXT(double value, double max, double reduce, double time) {
        if (ConfigData.CUSTOM_ACTION_BAR_ENABLE) {
            String resourceThirstText = getThirstCustomText(TypeResourceThirst.NORMAL, value, max, reduce, time);
            if (resourceThirstText != null)
                return resourceThirstText;
        }
        return replace(ACTION_BAR_DISABLE_TITLE, value, max, reduce, time);
    }

    private static void setResourceThirst(@Nonnull TypeResourceThirst typeResourceThirst, @Nonnull String thirstChar,
            @Nonnull String thirstHalfLeftChar, @Nonnull String thirstHalfRightChar, @Nonnull String thirstEmptyChar) {
        int numberOfItems = 20;
        List<ThirstCustomText> thirstCustomTextList = new ArrayList<>();
        String shiftRightChar = "";
        if (ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_X_POSITION != 0) {
            if (ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_ENABLE) {
                shiftRightChar = "%img_offset_" + ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_X_POSITION + "%";
            }
        }
        String waterChar = convertUnicodeEscape(thirstChar);
        String waterHalfLeftChar = convertUnicodeEscape(thirstHalfLeftChar);
        String waterHalfRightChar = convertUnicodeEscape(thirstHalfRightChar);
        String waterEmptyChar = convertUnicodeEscape(thirstEmptyChar);

        for (int i = numberOfItems; i >= 0; i--) {

            StringBuilder part1Builder = new StringBuilder();
            for (int j = 0; j < i / 2; j++) {
                part1Builder.append(waterChar);
            }
            String part1 = part1Builder.toString();

            StringBuilder part3Builder = new StringBuilder();
            if (i != numberOfItems) {
                for (int j = 0; j < (numberOfItems - i) / 2; j++) {
                    part3Builder.append(waterEmptyChar);
                }
            }
            String part3 = part3Builder.toString();

            ThirstCustomText thirstCustomText;
            if (CUSTOM_ACTION_BAR_ITEMSADDER_ORIENTATION.equalsIgnoreCase("LEFT_TO_RIGHT")) {
                String part2 = (i % 2 == 1) ? waterHalfRightChar : "";
                thirstCustomText = new ThirstCustomText(i * 5, true,
                        MethodDefault.formatColor("&r" + shiftRightChar + part3 + part2 + part1));
            } else {
                String part2 = (i % 2 == 1) ? waterHalfLeftChar : "";
                thirstCustomText = new ThirstCustomText(i * 5, true,
                        MethodDefault.formatColor("&r" + shiftRightChar + part1 + part2 + part3));
            }
            thirstCustomTextList.add(thirstCustomText);
        }

        ConfigData.resourcePackThirstMap.put(typeResourceThirst, thirstCustomTextList.stream()
                .sorted(Comparator.comparing(ThirstCustomText::getValue)).collect(Collectors.toList()));
    }

    @Nullable
    public static String getThirstCustomText(@Nonnull Player player, @Nonnull PlayerData playerData) {
        if (!ConfigData.CUSTOM_ACTION_BAR_ENABLE)
            return null;
        PlaceholderAPI placeholderAPI = ThirstBar.getInstance().getPlaceholderAPI();
        if (placeholderAPI == null)
            return null;
        List<Stage> stageList = playerData.getStageCurrentList();
        String text;
        if (stageList.isEmpty()) {
            text = ConfigData.getThirstCustomText(TypeResourceThirst.NORMAL,
                    playerData.getThirst(), playerData.getThirstMax(), playerData.getReduceTotal(player),
                    playerData.getThirstTime() / 20.0);
            return text;
        }
        Stage stage = stageList.get(stageList.size() - 1);
        if (stage instanceof StageConfig) {
            text = ConfigData.getThirstCustomText(ConfigData.TypeResourceThirst.RAW_WATTER,
                    playerData.getThirst(), playerData.getThirstMax(), playerData.getReduceTotal(player),
                    playerData.getThirstTime() / 20.0);
        } else {
            text = ConfigData.getThirstCustomText(ConfigData.TypeResourceThirst.DEBUFF,
                    playerData.getThirst(), playerData.getThirstMax(), playerData.getReduceTotal(player),
                    playerData.getThirstTime() / 20.0);
        }
        return text;
    }

    @Nullable
    public static String getThirstCustomText(@Nonnull TypeResourceThirst typeResourceThirst, final double value,
            double valueMax, double reduce, double time) {
        List<ThirstCustomText> thirstCustomTextList = resourcePackThirstMap.getOrDefault(typeResourceThirst, null);
        if (thirstCustomTextList == null)
            return null;
        ThirstCustomText thirstCustomText = thirstCustomTextList.stream()
                .filter(v -> {
                    double finalValue = value;
                    if (v.isPercent()) {
                        finalValue = (finalValue * 100) / valueMax;
                    }
                    boolean a = v.getValue() <= finalValue;
                    int index = thirstCustomTextList.indexOf(v);
                    boolean b = index != -1 && index + 1 < thirstCustomTextList.size();
                    if (!b)
                        return a;
                    boolean c = thirstCustomTextList.get(index + 1).getValue() > finalValue;
                    return a & c;
                }).findAny().orElse(null);
        return (thirstCustomText != null) ? replace(thirstCustomText.getText(), value, valueMax, reduce, time) : "None";
    }

    public static String replace(@Nonnull String text, double value, double max, double reduce, double time) {
        String timeChange;
        if (time == (long) time)
            timeChange = String.valueOf((long) time);
        else
            timeChange = String.format("%.2f", time).replaceAll("0*$", "").replaceAll("\\.$", "");
        return text.replace("<value>", ThirstBarMethod.changeDoubleToInt(Math.max(value, 0)))
                .replace("<max>", ThirstBarMethod.changeDoubleToInt(max))
                .replace("<reduce>", ThirstBarMethod.changeDoubleToInt(reduce))
                .replace("<time>", timeChange);
    }

    public static String convertUnicodeEscape(String input) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == '\\' && i < input.length() - 1 && input.charAt(i + 1) == 'u') {
                String unicodeValue = input.substring(i + 2, i + 6);
                char unicodeChar = (char) Integer.parseInt(unicodeValue, 16);
                builder.append(unicodeChar);
                i += 5;
            } else {
                builder.append(currentChar);
            }
        }

        return builder.toString();
    }

    private static String formatItemsAdder(@Nonnull String text, @Nonnull String whenNull) {
        if (!ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_ENABLE)
            return whenNull;
        if (!text.isEmpty())
            return "%img_" + text + "%";
        return whenNull;
    }

    @AllArgsConstructor
    @Getter
    public static class ThirstCustomText {
        private double value;
        private boolean percent;
        private final String text;

        public ThirstCustomText(@Nonnull String text) {
            String regex = "\\[(\\d+)%?]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {

                String value = matcher.group(1);
                double doubleValue = MethodDefault.formatNumber(value, 0);

                if (matcher.group(0).contains("%"))
                    this.percent = true;
                this.value = doubleValue;
            }
            this.text = MethodDefault.formatColor(text.replaceAll(regex, ""));
        }

    }
}
