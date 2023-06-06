package me.orineko.thirstbar.manager.player;

import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public interface PlayerThirstyDisplay {

    void showBossBar(Player player);
    void updateBossBar(Player player);
    void updateFood(Player player);
    void updateActionBar(Player player);
    void updateAll(Player player);
    void setDisplayBossBar(boolean bool, @Nonnull Player player);

}
