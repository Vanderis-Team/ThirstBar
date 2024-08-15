package me.orineko.thirstbar.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.stage.Stage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ThirstBarExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "thirstbar";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Vanderis";
    }

    @Override
    public @NotNull String getVersion() {
        return ThirstBar.getInstance().getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().addData(player);
        String identifier = params.toLowerCase();
        switch (identifier){
            case "stage":
                List<Stage> stageList = playerData.getStageCurrentList();
                return String.valueOf((stageList.size() > 0) ? stageList.get(stageList.size()-1) : "");
            case "current_int":
                return String.valueOf((int) playerData.getThirst());
            case "current_float":
                return String.format("%.2f", playerData.getThirst());
            case "max_int":
                return String.valueOf((int) playerData.getThirstMax());
            case "max_float":
                return String.format("%.2f", playerData.getThirstMax());
            case "reducevalue_int":
                return String.valueOf((int) playerData.getReduceTotal());
            case "reducevalue_float":
                return String.format("%.2f", playerData.getReduceTotal());
            case "reducetime_int":
                return String.valueOf((int) playerData.getThirstTime()/20);
            case "reducetime_float":
                return String.format("%.2f", (double) playerData.getThirstTime()/20);
            case "reducepersec_int":
                return String.valueOf((int) playerData.getReduceTotal()/(playerData.getThirstTime()/20));
            case "reducepersec_float":
                return String.format("%.2f", playerData.getReduceTotal() /(playerData.getThirstTime()/20));
            case "isdisabled":
                return String.valueOf(playerData.isDisable());
        }
        return null;
    }
}
