package me.orineko.thirstbar.manager.stage;

import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.Method;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StageList {

    public enum KeyConfig {
        WATER("DrinkingRawWater"), RAIN("DrinkingRain");
        private final String name;
        KeyConfig(@Nonnull String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final List<StageConfig> stageConfigList;
    private final List<StageTimeline> stageTimelineList;

    public StageList(){
        this.stageConfigList = new ArrayList<>();
        this.stageTimelineList = new ArrayList<>();
        loadDataConfig(KeyConfig.WATER);
        loadDataConfig(KeyConfig.RAIN);
        loadDataStage();
    }

    public List<StageConfig> getStageConfigList() {
        return stageConfigList;
    }

    public List<StageTimeline> getStageTimelineList() {
        return stageTimelineList;
    }

    @Nullable
    public StageConfig getStageConfig(@Nonnull KeyConfig keyConfig){
        return stageConfigList.stream().filter(s -> s.getName().equals(keyConfig.getName())).findAny().orElse(null);
    }

    @Nullable
    public StageTimeline getStageTimeline(@Nonnull String name){
        return stageTimelineList.stream().filter(s -> s.getName().equals(name)).findAny().orElse(null);
    }

    private void loadDataConfig(@Nonnull KeyConfig keyConfig){
        String name = keyConfig.getName();
        FileConfiguration file = ThirstBar.getInstance().getConfig();
        StageConfig stageConfig = new StageConfig(name);

        boolean enable = file.getBoolean(name+".Enable", false);
        long delay = Math.max(1, file.getLong(name+".Delay", 1));
        double value = Math.max(0, file.getLong(name+".Value", 0));
        double reduce = Math.max(0, file.getDouble(name+".Reduce", 0));
        long duration = Math.max(1, file.getLong(name+".Duration", 1));

        String titleActionBar = MethodDefault.formatColor(file.getString(name+".TitleActionBar", ""));
        String titleBossBar = MethodDefault.formatColor(file.getString(name+".TitleBossBar", ""));

        BarColor color = null;
        try {color = BarColor.valueOf(file.getString(name+".Color", ""));
        } catch (IllegalArgumentException ignore){}
        BarStyle style = null;
        try {style = BarStyle.valueOf(file.getString(name+".Style", ""));
        } catch (IllegalArgumentException ignore){}
        List<PotionEffect> potionEffectList = file.getStringList(name+".Effects").stream()
                .map(Method::getPotionEffect).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> actionList = file.getStringList(name+".Actions");

        stageConfig.setEnable(enable);
        stageConfig.setDelay(delay);
        stageConfig.setValue(value);
        stageConfig.setReducePercent(reduce);
        stageConfig.setDuration(duration);
        stageConfig.setTitleActionBar(titleActionBar);
        stageConfig.setTitleBossBar(titleBossBar);
        stageConfig.setBarColor(color);
        stageConfig.setBarStyle(style);
        stageConfig.setPotionEffectList(potionEffectList);
        stageConfig.setActionList(actionList);

        stageConfigList.add(stageConfig);
    }

    public void loadDataStage(){
        FileConfiguration file = ThirstBar.getInstance().getStageFile();
        ConfigurationSection section = file.getConfigurationSection("");
        if(section != null) section.getKeys(false).forEach(name -> {
            StageTimeline stageTimeline = new StageTimeline(name);

            String range = file.getString(name+".Range", "");
            double reduce = Math.max(0, file.getDouble(name+".Reduce", 0));

            String titleActionBar = MethodDefault.formatColor(file.getString(name+".TitleActionBar", ""));
            String titleBossBar = MethodDefault.formatColor(file.getString(name+".TitleBossBar", ""));

            BarColor color = null;
            try {color = BarColor.valueOf(file.getString(name+".Color", ""));
            } catch (IllegalArgumentException ignore){}
            BarStyle style = null;
            try {style = BarStyle.valueOf(file.getString(name+".Style", ""));
            } catch (IllegalArgumentException ignore){}
            List<PotionEffect> potionEffectList = file.getStringList(name+".Effects").stream()
                    .map(Method::getPotionEffect).filter(Objects::nonNull).collect(Collectors.toList());
            List<String> actionList = file.getStringList(name+".Actions");

            String[] arrRange = range.split(":");
            double thirstMin = 0;
            double thirstMax;
            if(arrRange.length == 0) return;
            else if(arrRange.length == 1){
                if(!MethodDefault.checkFormatNumber(arrRange[0])) return;
                thirstMax = MethodDefault.formatNumber(arrRange[0], 0);
            } else {
                if(!MethodDefault.checkFormatNumber(arrRange[0])) return;
                if(!MethodDefault.checkFormatNumber(arrRange[1])) return;
                double arg1 = MethodDefault.formatNumber(arrRange[0], 0);
                double arg2 = MethodDefault.formatNumber(arrRange[1], 0);
                thirstMin = Math.min(arg1, arg2);
                thirstMax = Math.max(arg1, arg2);
            }

            stageTimeline.setThirstMin(thirstMin);
            stageTimeline.setThirstMax(thirstMax);
            stageTimeline.setReducePercent(reduce);
            stageTimeline.setTitleActionBar(titleActionBar);
            stageTimeline.setTitleBossBar(titleBossBar);
            stageTimeline.setBarColor(color);
            stageTimeline.setBarStyle(style);
            stageTimeline.setPotionEffectList(potionEffectList);
            stageTimeline.setActionList(actionList);

            stageTimelineList.add(stageTimeline);
        });
    }
}
