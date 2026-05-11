package me.orineko.thirstbar.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.stage.Stage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;

public class ThirstBarExpansion extends PlaceholderExpansion {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

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
        switch (identifier) {
            case "stage":
                List<Stage> stageList = playerData.getStageCurrentList();
                return String.valueOf((!stageList.isEmpty()) ? stageList.get(stageList.size() - 1) : "");
            case "current_int":
                return String.valueOf((int) playerData.getThirst());
            case "current_float":
                return String.format("%.2f", playerData.getThirst());
            case "max_int":
                return String.valueOf((int) playerData.getThirstMax());
            case "max_float":
                return String.format("%.2f", playerData.getThirstMax());
            case "reducevalue_int":
                return String.valueOf((int) playerData.getReduceTotal(player));
            case "reducevalue_float":
                return String.format("%.2f", playerData.getReduceTotal(player));
            case "reducetime_int":
                return String.valueOf((int) playerData.getThirstTime() / 20);
            case "reducetime_float":
                return String.format("%.2f", (double) playerData.getThirstTime() / 20);
            case "reducepersec_int":
                return String.valueOf((int) playerData.getReduceTotal(player) / (playerData.getThirstTime() / 20));
            case "reducepersec_float":
                return String.format("%.2f", playerData.getReduceTotal(player) / (playerData.getThirstTime() / 20));
            case "isdisabled":
                return String.valueOf(playerData.isDisable());
            case "thirst_pack":
                return ConfigData.getThirstCustomText(player, playerData);
            case "bar":
                String bar = ConfigData.getThirstCustomText(player, playerData);
                if (bar != null) {
                    return bar.replace("\uF82A", "");
                }
                if (playerData.isDisable()) {
                    return ConfigData.ACTION_BAR_DISABLE_TEXT(playerData.getThirst(), playerData.getThirstMax(), playerData.getReduceTotal(player), playerData.getThirstTime() / 20.0);
                }
                return ConfigData.ACTION_BAR_TEXT(playerData.getThirst(), playerData.getThirstMax(), playerData.getReduceTotal(player), playerData.getThirstTime() / 20.0);
            case "mainhand_item_name":
                return getMainHandItemName(player);
        }
        return null;
    }

    private String getMainHandItemName(@NotNull Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (itemStack.getType() == Material.AIR) {
            return "AIR";
        }

        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {

            // PRIORITAS 1
            // minecraft:custom_name
            if (meta.hasCustomName()) {
                Component customName = meta.customName();

                if (customName != null) {
                    String serialized = LEGACY_SERIALIZER.serialize(customName);

                    if (!serialized.isEmpty()) {
                        return serialized;
                    }
                }
            }

            // PRIORITAS 2
            // minecraft:item_name
            String componentString = meta.getAsComponentString();

            String itemName = extractItemName(componentString);

            if (itemName != null && !itemName.isEmpty()) {
                return itemName;
            }

            // PRIORITAS 3
            // legacy api compatibility
            try {
                String legacyName = meta.getItemName();

                if (legacyName != null && !legacyName.isEmpty()) {
                    return legacyName;
                }
            } catch (Throwable ignored) {
            }
        }

        // PRIORITAS TERAKHIR
        // fallback ke material name
        return toTitleCase(
                itemStack.getType()
                        .name()
                        .toLowerCase()
                        .replace("_", " ")
        );
    }

    private @Nullable String extractItemName(@NotNull String componentString) {
        try {

            String key = "minecraft:item_name='";

            int start = componentString.indexOf(key);

            if (start == -1) {
                return null;
            }

            start += key.length();

            int end = componentString.indexOf("'", start);

            if (end == -1) {
                return null;
            }

            String rawJson = componentString.substring(start, end);

            Component component = GsonComponentSerializer.gson().deserialize(rawJson);

            return LEGACY_SERIALIZER.serialize(component);

        } catch (Throwable ignored) {
        }

        return null;
    }

    private String toTitleCase(@NotNull String input) {
        if (input.isEmpty()) return input;
        String[] words = input.split(" ");
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) out.append(word.substring(1));
        }
        return out.toString();
    }
}
