package me.orineko.thirstbar.manager.player;

import lombok.Getter;
import lombok.Setter;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.ThirstBarMethod;
import me.orineko.thirstbar.manager.action.ActionRegister;
import me.orineko.thirstbar.manager.action.Condition;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.stage.Stage;
import me.orineko.thirstbar.manager.stage.StageConfig;
import me.orineko.thirstbar.manager.stage.StageList;
import me.orineko.xseries.messages.ActionBar;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ArmorStand;
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

@Getter
@Setter
public class PlayerData extends PlayerSetting implements PlayerThirstValue, PlayerThirstyDisplay, PlayerThirstScheduler {

    private final String name;
    private double thirst;
    private double thirstMax;
    private double thirstReduce;
    private double thirstReduceOrigin;
    private long thirstTime;
    private double thirstDamage;
    private long cooldownRefresh;
    @Nullable
    private ArmorStand armorStandFrontPlayer;
    private boolean armorStandBehindPlayer;

    private final List<Stage> stageCurrentList;
    private final List<ActionRegister> actionRegisterList;

    private long delayRefresh;
    private int idRepeating;
    private int idDamage;
    private int idRefresh;
    private int idDelayRefresh;
    public int idDelayDisable;
    private int idDelayActionBar;
    private int idRepeatActionBar;
    private int idRepeat2ActionBar;

    public PlayerData(@Nonnull String name) {
        super();
        this.name = name;
        this.thirstMax = ConfigData.THIRSTY_MAX;
        this.thirstReduce = ConfigData.THIRSTY_REDUCE;
        this.thirstReduceOrigin = ConfigData.THIRSTY_REDUCE;
        this.thirstTime = ConfigData.THIRSTY_TIME;
        this.thirstDamage = ConfigData.THIRSTY_DAMAGE;
        this.thirst = this.thirstMax;
        this.cooldownRefresh = ConfigData.COOLDOWN_REFRESH;
        this.delayRefresh = 0;
        this.stageCurrentList = new ArrayList<>();
        this.actionRegisterList = new ArrayList<>();
        executeReduce();
        Player player = getPlayer();
        if (player != null) {
            showBossBar(player);
        }
    }

    public void refresh() {
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
    public void disableExecuteReduce() {
        if (this.idRepeating != 0) {
            Bukkit.getScheduler().cancelTask(this.idRepeating);
            this.idRepeating = 0;
        }
        if (this.idDamage != 0) {
            Bukkit.getScheduler().cancelTask(this.idDamage);
            this.idDamage = 0;
        }
    }

    @Override
    public void executeReduce() {
        disableExecuteReduce();
        this.idRepeating = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(), () -> {
            Player player = Bukkit.getPlayer(name);
            if (player == null) return;
            if (player.isDead()) return;
            if (!isDisableAll() && !isDisable()) {
                addThirst(-getReduceTotal());
                if (thirst > thirstMax) setThirst(getThirstMax());
            }
            updateAll(player);
        }, 0L, thirstTime);
        this.idDamage = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(), () -> {
            Player player = Bukkit.getPlayer(name);
            if (player == null) return;
            if (player.isDead()) return;
            if (isDisableAll()) return;
            if (isDisable()) return;
            if (player.getGameMode().equals(GameMode.CREATIVE) ||
                    player.getGameMode().equals(GameMode.SPECTATOR)) return;
            if (thirst <= 0) {
                setThirst(0);
                if (player.getHealth() - thirstDamage < 0) player.setHealth(0);
                else player.setHealth(player.getHealth() - thirstDamage);
                player.damage(0.000000000001);
            }
        }, 0, 30);
    }

    @Override
    public void disableExecuteRefresh() {
        if (idRefresh != 0) {
            Bukkit.getScheduler().cancelTask(idRefresh);
            idRefresh = 0;
        }
        if (idDelayRefresh != 0) {
            Bukkit.getScheduler().cancelTask(idDelayRefresh);
            idDelayRefresh = 0;
        }
        if (idDelayActionBar != 0) {
            Bukkit.getScheduler().cancelTask(idDelayActionBar);
            idDelayActionBar = 0;
        }
        if (idRepeatActionBar != 0) {
            Bukkit.getScheduler().cancelTask(idRepeatActionBar);
            idRepeatActionBar = 0;
        }
        if (idRepeat2ActionBar != 0) {
            Bukkit.getScheduler().cancelTask(idRepeat2ActionBar);
            idRepeat2ActionBar = 0;
        }
    }

    @Override
    public void executeRefresh() {
        delayRefresh = cooldownRefresh;
        disableExecuteRefresh();
        idRefresh = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(),
                () -> delayRefresh -= 1, 0, 20);
        idDelayRefresh = Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                () -> Bukkit.getScheduler().cancelTask(this.idRefresh), cooldownRefresh * 20);
    }

    @Override
    public long delayRefresh() {
        return delayRefresh;
    }

    @Nullable
    public Player getPlayer() {
        Player player = Bukkit.getPlayer(name);
        return (player != null) ? player : Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getPlayer).filter(Objects::nonNull)
                .filter(p -> p.getName().equals(name)).findAny().orElse(null);
    }

    @Override
    public void addThirst(double value) {
        this.thirst += value;
        if (this.thirst < 0) this.thirst = 0;
    }


    @Override
    public void setThirstTime(long value) {
        thirstTime = value;
        executeReduce();
    }

    @Override
    public void showBossBar(@Nonnull Player player) {
        setDisplayBossBar(isEnableBossBar(), player);
    }

    @Override
    public void updateBossBar(@Nonnull Player player) {
        if (isDisableAll()) {
            //setDisplayBossBar(false, player);
            setTitleDisableBossBar(thirst, thirstMax, getReduceTotal(), thirstTime / 20.0);
            return;
        } else showBossBar(player);
        if (!isEnableBossBar()) return;
        if (!stageCurrentList.isEmpty()) {
            Stage stage = stageCurrentList.get(stageCurrentList.size() - 1);
            if (stage.getTitleBossBar() != null && !stage.getTitleBossBar().isEmpty())
                setTitleBossBar(stage.getTitleBossBar(), thirst, thirstMax, getReduceTotal(), thirstTime / 20.0);
            if (stage.getBarColor() != null) getBossBar().setColor(stage.getBarColor());
            if (stage.getBarStyle() != null) getBossBar().setStyle(stage.getBarStyle());
        } else {
            setTitleBossBar(thirst, thirstMax, getReduceTotal(), thirstTime / 20.0);
            getBossBar().setColor(getColorBossBar());
            getBossBar().setStyle(getStyleBossBar());
        }
        getBossBar().setProgress(Math.max(Math.min(thirst / thirstMax, 1), 0));
    }

    @Override
    public void updateFood(@Nonnull Player player) {
        if (isDisableAll() || isDisable() || !isEnableFood()) return;
        BigDecimal a = BigDecimal.valueOf(thirst);
        BigDecimal b = BigDecimal.valueOf(20);
        BigDecimal c = BigDecimal.valueOf(thirstMax);
        BigDecimal d = a.multiply(b).divide(c, 2, RoundingMode.HALF_UP);
        int value = d.intValue();
        if (value == 0 && thirst > 0)
            player.setFoodLevel(1);
        else
            player.setFoodLevel(value);
    }

    @Override
    public void updateActionBar(@Nonnull Player player) {
        if (idDelayActionBar != 0) {
            Bukkit.getScheduler().cancelTask(idDelayActionBar);
            idDelayActionBar = 0;
        }
        if (idRepeatActionBar != 0) {
            Bukkit.getScheduler().cancelTask(idRepeatActionBar);
            idRepeatActionBar = 0;
        }
        if (idRepeat2ActionBar != 0) {
            Bukkit.getScheduler().cancelTask(idRepeat2ActionBar);
            idRepeat2ActionBar = 0;
        }
        if (!isEnableActionBar()) return;
        if (isDisableAll()) {
            setTitleDisableActionBar(thirst, thirstMax, getReduceTotal(), thirstTime / 20.0);
        } else {
            if (!stageCurrentList.isEmpty()) {
                String actionBar = ConfigData.getThirstCustomText(player, this);
                if (actionBar != null) {
                    setTitleActionBar(actionBar);
                } else {
                    Stage stage = stageCurrentList.get(stageCurrentList.size() - 1);
                    if (stage.getTitleActionBar() != null && !stage.getTitleActionBar().isEmpty())
                        setTitleActionBar(stage.getTitleActionBar(), thirst, thirstMax, getReduceTotal(), thirstTime / 20.0);
                }
            } else
                setTitleActionBar(thirst, thirstMax, getReduceTotal(), thirstTime / 20.0);
        }
        long thirstTimeRemain = thirstTime % 20;
        idRepeatActionBar = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(), () -> {
            if (idRepeat2ActionBar != 0) {
                Bukkit.getScheduler().cancelTask(idRepeat2ActionBar);
                idRepeat2ActionBar = 0;
            }
            if(!isEnableActionBar()) return;
            ActionBar.sendActionBar(ThirstBar.getInstance(), player, getTitleActionBar(), thirstTimeRemain);
            if ((int) thirstTime / 20 == 0) return;
            idDelayActionBar = Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(), () -> {
                idRepeat2ActionBar = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(), () -> {
                    if(!isEnableActionBar()) return;
                    ActionBar.sendActionBar(ThirstBar.getInstance(), player, getTitleActionBar(), 20);
                }, 0, 20);
            }, thirstTimeRemain);
        }, 0, thirstTime);
    }

    @Override
    public void updateAll(Player player) {
        updateFood(player);
        executeStage(player);
        updateBossBar(player);
        updateActionBar(player);
        checkAndAddEffect(player);
    }

    @Override
    public void setDisplayBossBar(boolean bool, @Nonnull Player player) {
        boolean hasBossBar = bossBar.getPlayers().stream().anyMatch(p -> p.getName().equals(name));
        if (bool && !hasBossBar) {
            bossBar.addPlayer(player);
        } else if (!bool && hasBossBar) {
            bossBar.removePlayer(player);
        }
    }

    @Override
    public void setDisable(boolean disable) {
        super.setDisable(disable);
        if (ThirstBar.getInstance().getSqlManager().getConnection() == null) {
            ThirstBar.getInstance().getPlayersFile().setAndSave(name + ".Disable", (disable) ? true : null);
        } else {
            ThirstBar.getInstance().getSqlManager().runSetDisablePlayer(name, (disable) ? 1 : 0);
        }
    }

    public void disableStage(@Nonnull Player player, StageList.KeyConfig keyConfig) {
        Stage stage;
        if (keyConfig != null) stage = this.stageCurrentList.stream()
                .filter(s -> s.getName().equals(keyConfig.getName())).findAny().orElse(null);
        else stage = this.stageCurrentList.stream()
                .filter(s -> Arrays.stream(StageList.KeyConfig.values()).noneMatch(k -> s.getName().equals(k.getName())))
                .findAny().orElse(null);
        if (stage == null) return;
        stage.getPotionEffectList().forEach(s -> player. removePotionEffect(s.getType()));
        this.stageCurrentList.remove(stage);
    }

    public void setStage(@Nonnull Player player, @Nonnull Stage stage) {
        this.stageCurrentList.add(stage);
        stage.getPotionEffectList().forEach(player::addPotionEffect);
        ThirstBarMethod.executeAction(player, stage.getActionList(), stage instanceof StageConfig);
    }

    public void checkAndAddEffect(@Nonnull Player player) {
        if (stageCurrentList.isEmpty()) return;
        if (isDisableAll()) {
            stageCurrentList.forEach(s -> s.getPotionEffectList().forEach(p -> player.removePotionEffect(p.getType())));
            stageCurrentList.clear();
            return;
        }
        List<PotionEffect> potionEffectList = new ArrayList<>();
        stageCurrentList.stream().map(Stage::getPotionEffectList).forEach(eList -> {
            eList.forEach(e -> {
                PotionEffect potionEffect = potionEffectList.stream()
                        .filter(p -> p.getType().equals(e.getType())).findAny().orElse(null);
                if (potionEffect == null)
                    potionEffectList.add(e);
                else {
                    if (potionEffect.getAmplifier() > e.getAmplifier()) return;
                    potionEffectList.remove(potionEffect);
                    potionEffectList.add(e);
                }
            });
        });
        potionEffectList.forEach(pe -> {
            if (player.getActivePotionEffects().stream().anyMatch(v -> v.getType().equals(pe.getType()) && v.getAmplifier() == pe.getAmplifier()))
                return;
            PotionEffect potionEffect = player.getPotionEffect(pe.getType());
            if (potionEffect == null)
                player.addPotionEffect(pe);
            else {
                if (potionEffect.getAmplifier() > pe.getAmplifier()) return;
                player.removePotionEffect(potionEffect.getType());
                player.addPotionEffect(pe);
            }
        });
    }

    public void executeStage(@Nonnull Player player) {
        if (isDisableAll()) return;
        Stage stage = ThirstBar.getInstance().getStageList().getStageTimelineList().stream()
                .filter(s -> s.getThirstMin() <= thirst && thirst <= s.getThirstMax())
                .findAny().orElse(null);
        if (stage == null) {
            disableStage(player, null);
            return;
        }
        if (stageCurrentList.stream().anyMatch(s -> s.getName().equals(stage.getName()))) return;
        disableStage(player, null);
        setStage(player, stage);
    }

    public double getReduceTotal() {
        Player player = getPlayer();
        double percent = this.stageCurrentList.stream().mapToDouble(Stage::getReducePercent).sum();
        double thirstReduce = this.thirstReduceOrigin + this.thirstReduceOrigin * percent / 100;
        for (ActionRegister actionRegister : actionRegisterList) {
            if(player != null && player.isOnline()) {
                Condition condition = actionRegister.getCondition(player);
                if (condition != null) {
                    thirstReduce += thirstReduce * (condition.getMultiply() - 1);
                } else {
                    thirstReduce += thirstReduce * (actionRegister.getMultiple() - 1);
                }
            } else {
                thirstReduce += thirstReduce * (actionRegister.getMultiple() - 1);
            }
        }
        return thirstReduce;
    }

    public void createArmorStand(@Nonnull Player player){
        if(getArmorStandFrontPlayer()!= null) return;
        Location location = new Location(player.getWorld(), 0, 255, 0);
        if(!location.getChunk().isLoaded()) location.getChunk().load();
        ArmorStand armorStand = player.getWorld().spawn(location, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        setArmorStandFrontPlayer(armorStand);
    }

}
