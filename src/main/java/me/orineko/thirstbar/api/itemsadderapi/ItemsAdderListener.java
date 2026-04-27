package me.orineko.thirstbar.api.itemsadderapi;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import me.orineko.thirstbar.ThirstBar;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderListener implements Listener {

    @EventHandler
    public void onItemsAdderLoad(ItemsAdderLoadDataEvent e) {
        if (me.orineko.thirstbar.api.itemsadderapi.ItemsAdderDeployer.pendingZip) {
            me.orineko.thirstbar.api.itemsadderapi.ItemsAdderDeployer.pendingZip = false;
            // Delay a tiny bit to ensure iareload is fully complete before iazip starts
            Bukkit.getScheduler().runTaskLater(ThirstBar.getInstance(), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iazip");
            }, 20L);
        }
    }
}
