package me.orineko.thirstbar.api.worldguardapi;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class WorldGuardApi {

    private static StateFlag stateFlag;
    private static DoubleFlag doubleFlag;

    public static void addFlagThirstBar() {
        String name = "disable-thirstbar";
        String name2 = "reduce-thirstbar";
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag(name, true);
            DoubleFlag flag2 = new DoubleFlag(name2);
            registry.register(flag);
            registry.register(flag2);
            stateFlag = flag;
            doubleFlag = flag2;
        } catch (FlagConflictException ex) {
            Flag<?> existing = registry.get(name);
            if (existing instanceof StateFlag) {
                stateFlag = (StateFlag) existing;
                //integerFlag = (IntegerFlag) existing;
            } else {
                ex.printStackTrace();
            }
        }
    }

    public static void registerFlag() {
        try {
            SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
            CustomFlagWorldGuard.Factory factory = CustomFlagWorldGuard.FACTORY;
            sessionManager.registerHandler(factory, null);
        } catch (NullPointerException ignored) {
        }
    }

    public static boolean isPlayerInFlag(Player player) {
        if(getStateFlag() == null) return false;
        try {
            Location location = BukkitAdapter.adapt(player.getLocation());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(location);
            return set.getRegions().stream()
                    .map(r -> r.getFlags().keySet().stream()
                            .map(Flag::getName).collect(Collectors.toList()).contains(getStateFlag().getName()))
                    .collect(Collectors.toList()).contains(true);
        } catch (IllegalAccessError e){
            return false;
        }
    }

    public static double getReduceValueLocationPlayer(@Nonnull Player player){
        try {
            Location location = BukkitAdapter.adapt(player.getLocation());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(location);

            DoubleFlag dFlag = getDoubleFlag();
            if(dFlag == null) return 0.0;
            return set.getRegions().stream().mapToDouble(region -> {
                Flag<?> flag = region.getFlags().keySet().stream().filter(f ->
                        f.getName().equals(dFlag.getName())).findAny().orElse(null);
                if(flag == null) return 0.0;
                return (double) region.getFlags().getOrDefault(flag, 0.0);
            }).max().orElse(0.0);
        } catch (IllegalAccessError e){
            return 0;
        }
    }
    
    @Nullable
    public static StateFlag getStateFlag() {
        return stateFlag;
    }

    public static DoubleFlag getDoubleFlag() {
        return doubleFlag;
    }
}
