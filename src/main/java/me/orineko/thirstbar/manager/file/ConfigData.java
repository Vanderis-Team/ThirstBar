package me.orineko.thirstbar.manager.file;

import lombok.Getter;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.ThirstBarMethod;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigData {

    public enum TypeResourceThirst{
        NORMAL, DEBUFF, RAW_WATTER
    }

    public static boolean STOP_DRINKING;
    public static boolean CUSTOM_ACTION_BAR_ENABLE;
    public static String CUSTOM_ACTION_BAR_ORIENTATION;
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
    public static List<String> MATERIALS;
    public static String NAME_RAW_POTION;
    public static List<String> LORE_RAW_POTION;
    public static int RED_COLOR_RAW_POTION;
    public static int GREEN_COLOR_RAW_POTION;
    public static int BLUE_COLOR_RAW_POTION;

    private final FileConfiguration configFile;

    public static final HashMap<TypeResourceThirst,List<ThirstCustomText>> resourcePackThirstMap = new HashMap<>();

    public ConfigData(){
        this.configFile = ThirstBar.getInstance().getConfig();

        CUSTOM_ACTION_BAR_ENABLE = configFile.getBoolean("CustomActionBar.Enable", false);
        CUSTOM_ACTION_BAR_ORIENTATION = configFile.getString("CustomActionBar.Orientation", "");
        if(CUSTOM_ACTION_BAR_ENABLE){
            setResourceThirst(TypeResourceThirst.NORMAL,
                    configFile.getString("CustomActionBar.Characters.Normal.ThirstChar", "eea1"),
                    configFile.getString("CustomActionBar.Characters.Normal.ThirstHalfLeftChar", "eea2"),
                    configFile.getString("CustomActionBar.Characters.Normal.ThirstHalfRightChar", "eeb1"),
                    configFile.getString("CustomActionBar.Characters.Normal.ThirstEmptyChar", "eea3")
            );
            setResourceThirst(TypeResourceThirst.DEBUFF,
                    configFile.getString("CustomActionBar.Characters.Debuff.ThirstChar", "eea4"),
                    configFile.getString("CustomActionBar.Characters.Debuff.ThirstHalfLeftChar", "eea5"),
                    configFile.getString("CustomActionBar.Characters.Debuff.ThirstHalfRightChar", "eeb2"),
                    configFile.getString("CustomActionBar.Characters.Debuff.ThirstEmptyChar", "eea6")
            );
            setResourceThirst(TypeResourceThirst.RAW_WATTER,
                    configFile.getString("CustomActionBar.Characters.Raw.ThirstChar", "eea7"),
                    configFile.getString("CustomActionBar.Characters.Raw.ThirstHalfLeftChar", "eea8"),
                    configFile.getString("CustomActionBar.Characters.Raw.ThirstHalfRightChar", "eeb3"),
                    configFile.getString("CustomActionBar.Characters.Raw.ThirstEmptyChar", "eea9")
            );
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
        NAME_RAW_POTION = MethodDefault.formatColor(configFile.getString("RawPotion.Name", ""));
        LORE_RAW_POTION = MethodDefault.formatColor(configFile.getStringList("RawPotion.Lore"));
        RED_COLOR_RAW_POTION = configFile.getInt("RawPotion.Color.Red", 0);
        GREEN_COLOR_RAW_POTION = configFile.getInt("RawPotion.Color.Green", 0);
        BLUE_COLOR_RAW_POTION = configFile.getInt("RawPotion.Color.Blue", 0);
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
        String resourceThirstText = getThirstCustomText(TypeResourceThirst.NORMAL, value, max, reduce, time);
        if(resourceThirstText != null) return resourceThirstText;
        return replace(ACTION_BAR_TITLE, value, max, reduce, time);
    }

    public static String ACTION_BAR_DISABLE_TEXT(double value, double max, double reduce, double time){
        String resourceThirstText = getThirstCustomText(TypeResourceThirst.NORMAL, value, max, reduce, time);
        if(resourceThirstText != null) return resourceThirstText;
        return replace(ACTION_BAR_DISABLE_TITLE, value, max, reduce, time);
    }

    private static void setResourceThirst(@Nonnull TypeResourceThirst typeResourceThirst, @Nonnull String thirstChar,
                                          @Nonnull String thirstHalfLeftChar, @Nonnull String thirstHalfRightChar, @Nonnull String thirstEmptyChar){
        int numberOfItems = 20;
        List<String> stringList = new ArrayList<>();

        String shiftRightChar = convertUnicodeEscape("\\uf82a\\uf82b\\uf824");
        String waterChar = convertUnicodeEscape("\\u"+thirstChar);
        String waterHalfLeftChar = convertUnicodeEscape("\\u"+thirstHalfLeftChar);
        String waterHalfRightChar = convertUnicodeEscape("\\u"+thirstHalfRightChar);
        String waterEmptyChar = convertUnicodeEscape("\\u"+thirstEmptyChar);

        for (int i = numberOfItems; i >= 0; i--) {
            String percentage = "[" + (i * 5) + "%]"+shiftRightChar;

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

            String result;
            if(CUSTOM_ACTION_BAR_ORIENTATION.equalsIgnoreCase("LEFT_TO_RIGHT")){
                String part2 = (i % 2 == 1) ? waterHalfRightChar : "";
                result = percentage + part3 + part2 + part1;
            } else {
                String part2 = (i % 2 == 1) ? waterHalfLeftChar : "";
                result = percentage + part1 + part2 + part3;
            }
            stringList.add(result);
        }

        ConfigData.resourcePackThirstMap.put(typeResourceThirst, stringList.stream().map(ThirstCustomText::new)
                .sorted(Comparator.comparing(ThirstCustomText::getValue)).collect(Collectors.toList()));
    }

    public static String getThirstCustomText(@Nonnull TypeResourceThirst typeResourceThirst, final double value, double valueMax, double reduce, double time){
        List<ThirstCustomText> thirstCustomTextList = resourcePackThirstMap.getOrDefault(typeResourceThirst, null);
        if(thirstCustomTextList == null) return null;
        ThirstCustomText thirstCustomText = thirstCustomTextList.stream()
                .filter(v -> {
                    double finalValue = value;
                    if(v.isPercent()){
                        finalValue = (finalValue*100)/valueMax;
                    }
                    boolean a = v.getValue() <= finalValue;
                    int index = thirstCustomTextList.indexOf(v);
                    boolean b = index != -1 && index + 1 < thirstCustomTextList.size();
                    if(!b) return a;
                    boolean c = thirstCustomTextList.get(index+1).getValue() > finalValue;
                    return a & c;
                }).findAny().orElse(null);
        return (thirstCustomText != null) ? replace(thirstCustomText.getText(), value, valueMax, reduce, time) : "None";
    }

    public static String replace(@Nonnull String text, double value, double max, double reduce, double time){
        String timeChange;
        if(time == (long) time) timeChange = String.valueOf((long) time);
        else timeChange = String.format("%.2f", time).replaceAll("0*$", "").replaceAll("\\.$", "");
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

    @Getter
    public static class ThirstCustomText {
        private double value;
        private boolean percent;
        private final String text;

        public ThirstCustomText(@Nonnull String text){
            String regex = "\\[(\\d+)%?]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {

                String value = matcher.group(1);
                double doubleValue = MethodDefault.formatNumber(value, 0);

                if (matcher.group(0).contains("%")) this.percent = true;
                this.value = doubleValue;
            }
            this.text = MethodDefault.formatColor(text.replaceAll(regex, ""));
        }

    }
}
