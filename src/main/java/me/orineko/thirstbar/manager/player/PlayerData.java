package me.orineko.thirstbar.manager.player;

import com.cryptomorin.xseries.messages.ActionBar;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.Method;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.stage.Stage;
import me.orineko.thirstbar.manager.stage.StageList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PlayerData extends PlayerSetting implements PlayerThirstValue, PlayerThirstyDisplay, PlayerThirstScheduler {

    private final String name;
    private double thirst;
    private double thirstMax;
    private double thirstReduce;
    private long thirstTime;
    private double thirstDamage;
    private long cooldownRefresh;

    private final List<Stage> stageCurrentList;

    private long delayRefresh;
    private int idRepeating;
    private int idRefresh;
    private int idDelayRefresh;
    public int idDelayDisable;

    public PlayerData(@Nonnull String name){
        super();
        this.name = name;
        this.thirstMax = ConfigData.THIRSTY_MAX;
        this.thirstReduce = ConfigData.THIRSTY_REDUCE;
        this.thirstTime = ConfigData.THIRSTY_TIME;
        this.thirstDamage = ConfigData.THIRSTY_DAMAGE;
        this.thirst = this.thirstMax;
        this.cooldownRefresh = ConfigData.COOLDOWN_REFRESH;
        this.delayRefresh = 0;
        this.stageCurrentList = new ArrayList<>();
        executeReduce();
        Player player = getPlayer();
        if(player != null) {
            showBossBar(player);
        }
    }

    public void refresh(){
        setThirst(getThirstMax());
    }

    @Override
    public long getCooldownRefresh() {
        return cooldownRefresh;
    }

    @Override
    public void setCooldownRefresh(long value) {
        this.cooldownRefresh = value;
    }

    @Override
    public void disableExecuteReduce(){
        if(this.idRepeating == 0) return;
        Bukkit.getScheduler().cancelTask(this.idRepeating);
        this.idRepeating = 0;
    }

    @Override
    public void executeReduce(){
        disableExecuteReduce();
        this.idRepeating = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(), () -> {
            Player player = Bukkit.getPlayer(name);
            if(player == null) return;
            if(player.isDead()) return;
            if(!isDisableAll() && !isDisable()) {
                addThirst(- getReduceTotal());
                if(thirst > thirstMax) setThirst(getThirstMax());
                if (thirst <= 0) {
                    setThirst(0);
                    if(player.getHealth()-thirstDamage < 0) player.setHealth(0);
                    else player.setHealth(player.getHealth() - thirstDamage);
                }
            }
            executeStage(player);
            checkAndAddEffect(player);
            updateAll(player);
        }, 0L, thirstTime);
    }

    @Override
    public void disableExecuteRefresh() {
        if(idRefresh != 0) {
            Bukkit.getScheduler().cancelTask(idRefresh);
            idRefresh = 0;
        }
        if(idDelayRefresh != 0){
            Bukkit.getScheduler().cancelTask(idDelayRefresh);
            idDelayRefresh = 0;
        }
    }

    @Override
    public void executeRefresh() {
        delayRefresh = cooldownRefresh;
        disableExecuteRefresh();
        idRefresh = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(),
                () -> delayRefresh -= 1, 0, 20);
        idDelayRefresh = Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                () -> Bukkit.getScheduler().cancelTask(this.idRefresh), cooldownRefresh*20);
    }

    @Override
    public long delayRefresh() {
        return delayRefresh;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public Player getPlayer(){
        Player player = Bukkit.getPlayer(name);
        return (player != null) ? player : Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getPlayer).filter(Objects::nonNull)
                .filter(p -> p.getName().equals(name)).findAny().orElse(null);
    }

    @Override
    public void addThirst(double value) {
        this.thirst += value;
    }

    @Override
    public void setThirst(double value) {
        this.thirst = value;
    }

    @Override
    public double getThirst() {
        return thirst;
    }

    @Override
    public void setThirstMax(double value) {
        this.thirstMax = value;
    }

    @Override
    public double getThirstMax() {
        return thirstMax;
    }

    @Override
    public void setThirstReduce(double value) {
        thirstReduce = value;
    }

    @Override
    public double getThirstReduce() {
        return thirstReduce;
    }

    @Override
    public void setThirstTime(long value) {
        thirstTime = value;
        executeReduce();
    }

    @Override
    public long getThirstTime() {
        return thirstTime;
    }

    @Override
    public void setThirstDamage(double value) {
        thirstDamage = value;
    }

    @Override
    public double getThirstDamage() {
        return thirstDamage;
    }

    public List<Stage> getStageCurrentList() {
        return stageCurrentList;
    }

    @Override
    public void showBossBar(@Nonnull Player player) {
        setDisplayBossBar(isEnableBossBar(), player);
    }

    @Override
    public void updateBossBar(@Nonnull Player player) {
        if(isDisableAll()){
            setDisplayBossBar(false, player);
            return;
        } else showBossBar(player);
        if(!isEnableBossBar()) return;
        if(stageCurrentList.size() > 0 &&
                (stageCurrentList.get(stageCurrentList.size()-1).getTitleBossBar() != null &&
                        !stageCurrentList.get(stageCurrentList.size()-1).getTitleBossBar().isEmpty())) {
            Stage stage = stageCurrentList.get(stageCurrentList.size()-1);
            if(stage.getTitleBossBar() != null && !stage.getTitleBossBar().isEmpty())
                setTitleBossBar(stage.getTitleBossBar(), thirst, thirstMax, getReduceTotal(), thirstTime/20);
            if(stage.getBarColor() != null) getBossBar().setColor(stage.getBarColor());
            if(stage.getBarStyle() != null) getBossBar().setStyle(stage.getBarStyle());
        } else {
            setTitleBossBar(thirst, thirstMax, getReduceTotal(), thirstTime/20);
            getBossBar().setColor(getColorBossBar());
            getBossBar().setStyle(getStyleBossBar());
        }
        getBossBar().setProgress(thirst/thirstMax);
    }

    @Override
    public void updateFood(@Nonnull Player player) {
        if(isDisableAll() || isDisable() || !isEnableFood()) return;
        BigDecimal a = BigDecimal.valueOf(thirst);
        BigDecimal b = BigDecimal.valueOf(20);
        BigDecimal c = BigDecimal.valueOf(thirstMax);
        BigDecimal d = a.multiply(b).divide(c, 2, RoundingMode.HALF_DOWN);
        int value = d.intValue();
        if(value == 0 && thirst > 0)
            player.setFoodLevel(1);
        else
            player.setFoodLevel(value);
    }

    @Override
    public void updateActionBar(@Nonnull Player player) {
        if(isDisableAll() || !isEnableActionBar()) return;
        if(stageCurrentList.size() > 0 && (stageCurrentList.get(stageCurrentList.size()-1).getTitleActionBar() != null &&
                !stageCurrentList.get(stageCurrentList.size()-1).getTitleActionBar().isEmpty())){
            Stage stage = stageCurrentList.get(stageCurrentList.size()-1);
            if(stage.getTitleActionBar() != null && !stage.getTitleActionBar().isEmpty())
                setTitleActionBar(stage.getTitleActionBar(), thirst, thirstMax, getReduceTotal(), thirstTime/20);
        } else
            setTitleActionBar(thirst, thirstMax, getReduceTotal(), thirstTime/20);
        ActionBar.sendActionBar(ThirstBar.getInstance(), player, getTitleActionBar(), thirstTime/20);
    }

    @Override
    public void updateAll(Player player) {
        updateFood(player);
        updateBossBar(player);
        updateActionBar(player);
    }

    @Override
    public void setDisplayBossBar(boolean bool, @Nonnull Player player) {
        boolean hasBossBar = bossBar.getPlayers().stream().anyMatch(p -> p.getName().equals(name));
        if(bool && !hasBossBar){
            bossBar.addPlayer(player);
        } else if(!bool && hasBossBar) {
            bossBar.removePlayer(player);
        }
    }

    @Override
    public void setDisable(boolean disable) {
        super.setDisable(disable);
        ThirstBar.getInstance().getPlayersFile().setAndSave(name+".Disable", (disable) ? true : null);
    }

    public void disableStage(@Nonnull Player player, StageList.KeyConfig keyConfig){
        Stage stage;
        if(keyConfig != null) stage = this.stageCurrentList.stream()
                .filter(s -> s.getName().equals(keyConfig.getName())).findAny().orElse(null);
        else stage = this.stageCurrentList.stream()
                .filter(s -> Arrays.stream(StageList.KeyConfig.values()).noneMatch(k -> s.getName().equals(k.getName())))
                .findAny().orElse(null);
        if(stage == null) return;
        stage.getPotionEffectList().forEach(s -> player.removePotionEffect(s.getType()));
        this.stageCurrentList.remove(stage);
    }

    public void setStage(@Nonnull Player player, @Nonnull Stage stage){
        this.stageCurrentList.add(stage);
        stage.getPotionEffectList().forEach(player::addPotionEffect);
        Method.executeAction(player, stage.getActionList());
    }

    public void checkAndAddEffect(@Nonnull Player player){
        if(stageCurrentList.isEmpty()) return;
        if(isDisableAll()){
            stageCurrentList.forEach(s -> s.getPotionEffectList().forEach(p -> player.removePotionEffect(p.getType())));
            stageCurrentList.clear();
            return;
        }
        List<PotionEffect> potionEffectList = new ArrayList<>();
        stageCurrentList.stream().map(Stage::getPotionEffectList).forEach(eList -> {
            eList.forEach(e -> {
                if(potionEffectList.stream().noneMatch(p -> p.getType().equals(e.getType())))
                    potionEffectList.add(e);
                else {
                    PotionEffect potionEffect = potionEffectList.stream()
                            .filter(p -> p.getType().equals(e.getType())).findAny().orElse(null);
                    if(potionEffect == null) return;
                    if(potionEffect.getAmplifier() > e.getAmplifier()) return;
                    potionEffectList.remove(potionEffect);
                    potionEffectList.add(e);
                }
            });
        });
        potionEffectList.forEach(pe -> {
            PotionEffect potionEffect = player.getPotionEffect(pe.getType());
            if(potionEffect == null)
                player.addPotionEffect(pe);
            else {
                if (potionEffect.getAmplifier() > pe.getAmplifier()) return;
                player.removePotionEffect(potionEffect.getType());
                player.addPotionEffect(pe);
            }
        });
    }

    public void executeStage(@Nonnull Player player){
        if(isDisableAll()) return;
        Stage stage = ThirstBar.getInstance().getStageList().getStageTimelineList().stream()
                .filter(s -> s.getThirstMin() <= thirst && thirst <= s.getThirstMax())
                .findAny().orElse(null);
        if(stage == null){
            disableStage(player, null);
            return;
        }
        if(stageCurrentList.stream().anyMatch(s -> s.getName().equals(stage.getName()))) return;
        disableStage(player, null);
        setStage(player, stage);
    }

    public double getReduceTotal(){
        double percent = this.stageCurrentList.stream().mapToDouble(Stage::getReducePercent).sum();
        return thirstReduce + thirstReduce*percent/100;
    }
}
