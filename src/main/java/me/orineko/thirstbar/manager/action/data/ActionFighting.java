package me.orineko.thirstbar.manager.action.data;

import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.action.ActionRegister;
import me.orineko.thirstbar.manager.action.ActionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ActionFighting extends ActionRegister implements Listener {

    private final HashMap<String, Boolean> fightingMap = new HashMap<>();
    private final HashMap<String, Integer> delayMap = new HashMap<>();

    public ActionFighting() {
        super(ActionType.FIGHTING);
        ThirstBar.getInstance().getServer().getPluginManager().registerEvents(this, ThirstBar.getInstance());
        int idRepeat = Bukkit.getScheduler().scheduleSyncRepeatingTask(ThirstBar.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach(v -> {
                if(checkCanExecute(v)){
                    executeAction(v);
                } else if(checkCanNotExecute(v)) {
                    disableAction(v);
                }
            });
        }, 0, 60);
        setIdRepeat(idRepeat);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e){
        if(!(e.getDamager() instanceof Player)) return;
        Player player = (Player) e.getDamager();
        if(delayMap.getOrDefault(player.getName(), 0) != 0) return;
        fightingMap.put(player.getName(), true);
        int idDelay = Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(), () -> {
            fightingMap.remove(player.getName());
            delayMap.remove(player.getName());
        }, 60);
        delayMap.put(player.getName(), idDelay);
    }

    @Override
    public boolean checkCondition(@NotNull Player player) {
        return fightingMap.getOrDefault(player.getName(), false);
    }
}
