package me.orineko.thirstbar.manager.action;

import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.api.PlaceholderAPI;
import me.orineko.thirstbar.manager.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class ActionRegister {

    private final String name;
    private boolean enable;
    private double multiple;
    private boolean hideActionBar;
    private boolean executing;
    private int idRepeat;
    private final List<Condition> conditionList;

    public ActionRegister(@Nonnull String name){
        this.name = name;
        this.conditionList = new ArrayList<>();
    }

    public ActionRegister(@Nonnull ActionType actionType){
        this(actionType.name());
    }

    public String getName() {
        return name.toUpperCase();
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public double getMultiple() {
        return multiple;
    }

    public void setMultiple(double multiple) {
        this.multiple = multiple;
    }

    public boolean isHideActionBar() {
        return hideActionBar;
    }

    public void setHideActionBar(boolean hideActionBar) {
        this.hideActionBar = hideActionBar;
    }

    public boolean isExecuting() {
        return executing;
    }

    public void setExecuting(boolean executing) {
        this.executing = executing;
    }

    public abstract boolean checkCondition(@Nonnull Player player);

    public void setIdRepeat(int idRepeat) {
        this.idRepeat = idRepeat;
    }

    public int getIdRepeat() {
        return idRepeat;
    }

    public List<Condition> getConditionList() {
        return conditionList;
    }

    public void removeScheduleRepeat(){
        if(getIdRepeat() != 0) {
            Bukkit.getScheduler().cancelTask(getIdRepeat());
            setIdRepeat(0);
        }
    }

    public ActionRegister loadFileData(@Nonnull FileConfiguration file){
        if(file.getConfigurationSection(name.toLowerCase()) != null) {
            setEnable(file.getBoolean(name.toLowerCase()+".enable", false));
            setMultiple(file.getDouble(name.toLowerCase()+".multiply", 0));

            ConfigurationSection section = file.getConfigurationSection(name.toLowerCase()+".cases");
            if(section != null) section.getKeys(false).forEach(v -> {
                String conditionString = file.getString(name.toLowerCase()+".cases."+v+".condition", "");
                double multiply = file.getDouble(name.toLowerCase()+".cases."+v+".multiply", 1);
                Condition condition = new Condition(conditionString, multiply);
                getConditionList().add(condition);
            });

            setHideActionBar(file.getBoolean(name.toLowerCase()+".hideActionBar", false));
            Bukkit.getConsoleSender().sendMessage("§b[ThirstBar] §fLoaded action "+name.toLowerCase());
        }
        return this;
    }

    public boolean checkCanExecute(@Nonnull Player player){
        return isEnable() && !isExecuting() && checkCondition(player);
    }

    public boolean checkCanNotExecute(@Nonnull Player player){
        return isEnable() && isExecuting() && !checkCondition(player);
    }

    public void executeAction(@Nonnull Player player){
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());
        if(playerData.getActionRegisterList().stream().anyMatch(v -> v.getName().equals(this.getName()))) return;
        playerData.getActionRegisterList().add(this);
        if(isHideActionBar()) playerData.setEnableActionBar(false);
        playerData.updateAll(player);
        setExecuting(true);
    }

    public void disableAction(@Nonnull Player player){
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player.getName());
        playerData.getActionRegisterList().remove(this);
        if(isHideActionBar()) playerData.setEnableActionBar(true);
        playerData.updateAll(player);
        setExecuting(false);
    }

    @Nullable
    public Condition getCondition(@Nonnull Player player) {
        PlaceholderAPI placeholderAPI = ThirstBar.getInstance().getPlaceholderAPI();
        if(placeholderAPI == null) return null;
        return getConditionList().stream().filter(v -> {
            double valueA;
            double valueB;
            String[] arr;
            if(v.getCondition().contains(">")) {
                arr = v.getCondition().split(">");
                if(arr.length < 2) return false;
                valueA = placeholderAPI.getValuePlaceholder(player, arr[0].trim());
                valueB = placeholderAPI.getValuePlaceholder(player, arr[1].trim());
                return valueA > valueB;
            } else if(v.getCondition().contains(">=")) {
                arr = v.getCondition().split(">=");
                if(arr.length < 2) return false;
                valueA = placeholderAPI.getValuePlaceholder(player, arr[0].trim());
                valueB = placeholderAPI.getValuePlaceholder(player, arr[1].trim());
                return valueA >= valueB;
            } else if(v.getCondition().contains("<")) {
                arr = v.getCondition().split("<");
                if(arr.length < 2) return false;
                valueA = placeholderAPI.getValuePlaceholder(player, arr[0].trim());
                valueB = placeholderAPI.getValuePlaceholder(player, arr[1].trim());
                return valueA < valueB;
            } else if(v.getCondition().contains("==")) {
                arr = v.getCondition().split("==");
                if(arr.length < 2) return false;
                valueA = placeholderAPI.getValuePlaceholder(player, arr[0].trim());
                valueB = placeholderAPI.getValuePlaceholder(player, arr[1].trim());
                return valueA == valueB;
            } else if(v.getCondition().contains("<=")) {
                arr = v.getCondition().split("<=");
                if(arr.length < 2) return false;
                valueA = placeholderAPI.getValuePlaceholder(player, arr[0].trim());
                valueB = placeholderAPI.getValuePlaceholder(player, arr[1].trim());
                return valueA <= valueB;
            }
            return false;
        }).findAny().orElse(null);
    }
}
