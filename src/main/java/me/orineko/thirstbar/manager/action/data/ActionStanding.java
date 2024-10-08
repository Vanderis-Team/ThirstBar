package me.orineko.thirstbar.manager.action.data;

import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.action.ActionRegister;
import me.orineko.thirstbar.manager.action.ActionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ActionStanding extends ActionRegister{

    private final HashMap<String, Location> locationHashMap = new HashMap<>();

    public ActionStanding() {
        super(ActionType.STANDING);
        int idRepeat = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach(v -> {
                if(checkCanExecute(v)){
                    executeAction(v);
                } else if(checkCanNotExecute(v)) {
                    disableAction(v);
                }
                locationHashMap.put(v.getName(), v.getLocation());
            });
        }, 0, 20);
        setIdRepeat(idRepeat);
    }

    @Override
    public boolean checkCondition(@NotNull Player player) {
        Location locationOld = locationHashMap.getOrDefault(player.getName(), null);
        Location locationNew = player.getLocation();
        if(locationOld == null) return false;
        World worldOld = locationOld.getWorld();
        int xOld = locationOld.getBlockX();
        int yOld = locationOld.getBlockY();
        int zOld = locationOld.getBlockZ();
        World worldNew = locationNew.getWorld();
        int xNew = locationNew.getBlockX();
        int yNew = locationNew.getBlockY();
        int zNew = locationNew.getBlockZ();
        return xOld == xNew && yOld == yNew && zOld == zNew &&
                (worldOld != null && worldOld.equals(worldNew));
    }
}
