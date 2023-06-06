package me.orineko.thirstbar.manager.api.worldguardapi;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.SessionManager;
import me.orineko.thirstbar.manager.file.ConfigData;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldGuardApi {

    private static StateFlag stateFlag;

    public static void addFlagThirstBar() {
        String name = "disable-thirstbar";
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag(name, true);
            registry.register(flag);
            stateFlag = flag;
        } catch (FlagConflictException ex) {
            Flag<?> existing = registry.get(name);
            if (existing instanceof StateFlag) {
                stateFlag = (StateFlag) existing;
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

            List<Double> reduceList = new ArrayList<>();
            ConfigData.FLAG_REDUCE.forEach((k, v) -> {
                ProtectedRegion region = set.getRegions().stream().filter(r ->
                        r.getId().equalsIgnoreCase(k)).findAny().orElse(null);
                if(region == null) return;
                reduceList.add(v);
            });
            return reduceList.stream().mapToDouble(r -> r).max().orElse(0);
        } catch (IllegalAccessError e){
            return 0;
        }
    }

    public static StateFlag getStateFlag() {
        return stateFlag;
    }
}
