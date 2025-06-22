package me.orineko.thirstbar.api;

import me.orineko.pluginspigottools.MethodDefault;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class PlaceholderAPI {

    public double getValuePlaceholder(@Nonnull Player player, @Nonnull String text){
        String value = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        return MethodDefault.formatNumber(value, 0);
    }

    public String parse(@Nonnull Player player, String text){
        if(text == null) return null;
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }

}
