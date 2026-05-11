package me.orineko.thirstbar;

import lombok.Getter;
import me.orineko.pluginspigottools.core.FileManager;
import me.orineko.pluginspigottools.nbtapi.NBT;
import me.orineko.pluginspigottools.utils.MethodDefault;
import me.orineko.thirstbar.command.CommandManager;
import me.orineko.thirstbar.command.MainCommand;
import me.orineko.thirstbar.listener.ThirstListener;
import me.orineko.thirstbar.manager.ThirstBarMethod;
import me.orineko.thirstbar.manager.action.ActionManager;
import me.orineko.thirstbar.manager.player.ThirstGlobalTask;
import me.orineko.thirstbar.api.PlaceholderAPI;
import me.orineko.thirstbar.api.ThirstBarExpansion;
import me.orineko.thirstbar.api.UpdateChecker;
import me.orineko.thirstbar.api.sql.SqlManager;
import me.orineko.thirstbar.api.worldguardapi.WorldGuardApi;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.file.MessageData;
import me.orineko.thirstbar.manager.item.ItemData;
import me.orineko.thirstbar.manager.item.ItemDataList;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.player.PlayerDataList;
import me.orineko.thirstbar.manager.stage.StageList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ThirstBar extends JavaPlugin {

    private static ThirstBar plugin;
    private PlayerDataList playerDataList;
    private StageList stageList;
    private ItemDataList itemDataList;
    private ConfigData configData;
    private MessageData messageData;
    private FileManager itemsFile;
    private FileManager messageFile;
    private FileManager playersFile;
    private FileManager stageFile;
    private FileManager actionsFile;
    private boolean worldGuardApiEnable = false;
    private SqlManager sqlManager;
    private ActionManager actionManager;
    @Nullable
    private PlaceholderAPI placeholderAPI;
    private int versionBukkit;
    private ThirstGlobalTask globalTask;

    @Override
    public void onLoad() {
        super.onLoad();
        registerFlag();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = this;
        versionBukkit = (int) MethodDefault.formatNumber(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1], 0);
        sqlManager = new SqlManager();

        messageFile = new FileManager("message.yml", this);
        stageFile = new FileManager("stages.yml", this);
        actionsFile = new FileManager("actions.yml", this);
        itemsFile = new FileManager("items.yml", this);
        messageFile.copyDefault();
        stageFile.copyDefault();
        actionsFile.copyDefault();
        itemsFile.copyDefault();
        loadResourcePackFile();

        if (sqlManager.getConnection() == null) {
            playersFile = new FileManager("players.db", this);
            playersFile.createFile();
        }

        renewData();
        Bukkit.getOnlinePlayers().forEach(ThirstBarMethod::disableGameMode);

        CommandManager.CommandRegistry.register(true, this, new MainCommand(this));
        getServer().getPluginManager().registerEvents(new ThirstListener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPI = new PlaceholderAPI();
            new ThirstBarExpansion().register();
        }
        
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            getServer().getPluginManager().registerEvents(new me.orineko.thirstbar.api.itemsadderapi.ItemsAdderListener(), this);
        }
        checkForUpdate();

        ItemStack bottle = new ItemStack(Material.POTION, 1);
        ItemMeta meta = bottle.getItemMeta();
        PotionMeta pmeta = (PotionMeta) meta;
        if (pmeta != null) {
            if (getVersionBukkit() >= 20) {
                pmeta.setBasePotionType(PotionType.WATER);
            } else {
                try {
                    Object pdata = Class.forName("org.bukkit.potion.PotionData")
                            .getConstructor(PotionType.class).newInstance(PotionType.WATER);
                    pmeta.getClass().getMethod("setBasePotionData", Class.forName("org.bukkit.potion.PotionData"))
                            .invoke(pmeta, pdata);
                } catch (Exception e) {
                    pmeta.setBasePotionType(PotionType.WATER);
                }
            }
        }
        bottle.setItemMeta(meta);
        ItemStack potionRawItem = MethodDefault.getItemAllVersion("POTION");
        NamespacedKey key = new NamespacedKey(this, "raw_water_furnace");
        try {
            Bukkit.removeRecipe(key);
        } catch (NoSuchMethodError | Exception ignore) {}
        FurnaceRecipe furnaceRecipe = new FurnaceRecipe(
                key,
                bottle,
                potionRawItem.getType(),
                ConfigData.CUSTOM_FURNACE_EXP,
                ConfigData.CUSTOM_FURNACE_COOKING_TIME
        );
        try {
            Bukkit.addRecipe(furnaceRecipe);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable() {
        if (globalTask != null) {
            try {
                globalTask.cancel();
            } catch(Exception ignore) {}
        }
        if (actionManager != null)
            actionManager.removeRegister();
        if (getPlayerDataList() == null)
            return;
        getPlayerDataList().removeDataPlayers();
    }

    public void renewData() {
        reloadConfig();
        if (sqlManager.getConnection() == null) {
            playersFile.reloadWithoutCreateFile();
        }
        try {
            itemsFile.reload();
            messageFile.reload();
            stageFile.reload();
            actionsFile.reload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        configData = new ConfigData();
        messageData = new MessageData();
        
        me.orineko.thirstbar.api.itemsadderapi.ItemsAdderDeployer.checkAndDeploy();
        
        itemDataList = new ItemDataList();
        itemDataList.loadData();
        registerCustomCookRecipes();
        registerCustomCraftRecipes();
        stageList = new StageList();

        // Clean up old player data BEFORE creating new list to prevent leaks
        if (playerDataList != null) {
            playerDataList.removeDataPlayers();
        }

        playerDataList = new PlayerDataList();
        Bukkit.getOnlinePlayers().forEach(p -> {
            PlayerData playerData = getPlayerDataList().addData(p);
            boolean check = ConfigData.DISABLED_WORLDS.stream()
                    .anyMatch(w -> p.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
            playerData.setDisableAll(check);
            playerData.createArmorStand(p);
        });
        if (actionManager != null)
            actionManager.removeRegister();
        actionManager = new ActionManager();
        playerDataList.loadData();

        if (globalTask != null) {
            try {
                globalTask.cancel();
            } catch(Exception ignore) {}
        }
        globalTask = new ThirstGlobalTask();
        globalTask.runTaskTimer(this, 0L, 1L);

        me.orineko.thirstbar.api.itemsadderapi.ItemsAdderDeployer.checkAndDeploy();
    }

    private void loadResourcePackFile() {
        try {
            FileManager tutorialFile = new FileManager("tutorial.txt", this);
            tutorialFile.copyDefault();
        } catch (Exception ignore) {
        }
    }

    private void registerFlag() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            worldGuardApiEnable = true;
        } catch (ClassNotFoundException e) {
            worldGuardApiEnable = false;
            return;
        }
        WorldGuardApi.addFlagThirstBar();
        WorldGuardApi.registerFlag();
    }

    private void checkForUpdate() {
        List<String> textList = new ArrayList<>();
        new UpdateChecker(113587).getVersion(version -> {
            // String versions =
            // Bukkit.getServer().getClass().getPackage().getName().replace(".",
            // ",").split(",")[3];
            List<Player> playerList = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.isOp() || p.hasPermission("thirstbar.admin"))
                    .collect(Collectors.toList());
            String textTitle;
            if (getVersionBukkit() < 16) {
                textTitle = "§b[ThirstBar]";
            } else {
                textTitle = MethodDefault.formatColor(
                        "§7§l[§r#007fff§lT#008afe§lH#0095fe§lI#009ffd§lR#00aafd§lS#00b5fc§lT#17bdf8 #2ec5f3§lB#44ccef§lA#5bd4ea§lR§7§l]");
            }
            if (this.getDescription().getVersion().equals(version)) {
                textList.add(textTitle + " §aThere is not a new update available.");
            } else {
                textList.add(textTitle + " §7The plugin version you are using is §4out of date§7!");
                textList.add(textTitle + " §7There is a new update available.");
                textList.add(textTitle
                        + " §7Download it here: §6https://www.spigotmc.org/resources/1-9-1-20-1-%E2%9A%A1-thirst-bar-%E2%9A%A1-add-thirst-unit-for-player-%E2%AD%90-placeholderapi-and-worldguard-support.113587/");
            }
            textList.forEach(t -> Bukkit.getConsoleSender().sendMessage(t));
            playerList.forEach(p -> textList.forEach(p::sendMessage));
        });
    }

    public static ThirstBar getInstance() {
        return plugin;
    }

    private void registerCustomCookRecipes() {
        List<NamespacedKey> toRemove = new ArrayList<>();
        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof FurnaceRecipe) {
                NamespacedKey key = ((FurnaceRecipe) recipe).getKey();
                if (key != null && key.getNamespace().equalsIgnoreCase(getName().toLowerCase())
                        && key.getKey().startsWith("custom_cook_")) {
                    toRemove.add(key);
                }
            }
        });
        toRemove.forEach(Bukkit::removeRecipe);

        for (ItemData itemData : itemDataList.getDataList()) {
            if (itemData.getCookType() == null || !itemData.getCookType().equalsIgnoreCase("cooking")) continue;
            if (itemData.getCookReplace() == null || itemData.getCookReplace().trim().isEmpty()) continue;
            if (itemData.getItemStack() == null) continue;
            ItemData target = itemDataList.getData(itemData.getCookReplace());
            if (target == null || target.getItemStack() == null) continue;

            NamespacedKey key = new NamespacedKey(this, "custom_cook_" + itemData.getName().toLowerCase());
            FurnaceRecipe recipe = new FurnaceRecipe(
                    key,
                    target.getItemStack().clone(),
                    new RecipeChoice.MaterialChoice(itemData.getItemStack().getType()),
                    itemData.getCookExp(),
                    Math.max(1, itemData.getCookTime())
            );
            try {
                Bukkit.addRecipe(recipe);
            } catch (Exception ignore) {}
        }
    }

    private void registerCustomCraftRecipes() {
        List<NamespacedKey> toRemove = new ArrayList<>();
        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof ShapedRecipe) {
                NamespacedKey key = ((ShapedRecipe) recipe).getKey();
                if (key != null && key.getNamespace().equalsIgnoreCase(getName().toLowerCase())
                        && key.getKey().startsWith("custom_craft_")) {
                    toRemove.add(key);
                }
            }
        });
        toRemove.forEach(Bukkit::removeRecipe);

        for (ItemData itemData : itemDataList.getDataList()) {
            if (itemData.getCraftingType() == null || !itemData.getCraftingType().equalsIgnoreCase("crafting")) continue;
            if (itemData.getItemStack() == null) continue;
            List<String> recipeRows = itemData.getCraftingRecipe();
            if (recipeRows == null || recipeRows.size() != 3) continue;

            String[] shape = new String[3];
            boolean validShape = true;
            for (int i = 0; i < 3; i++) {
                String[] parts = recipeRows.get(i).split("\\|");
                if (parts.length != 3) {
                    validShape = false;
                    break;
                }
                StringBuilder row = new StringBuilder();
                for (String part : parts) {
                    String token = part.trim();
                    if (token.length() != 1) {
                        validShape = false;
                        break;
                    }
                    row.append(token.charAt(0));
                }
                if (!validShape) break;
                shape[i] = row.toString();
            }
            if (!validShape) continue;

            NamespacedKey key = new NamespacedKey(this, "custom_craft_" + itemData.getName().toLowerCase());
            ShapedRecipe shaped = new ShapedRecipe(key, itemData.getItemStack().clone());
            shaped.shape(shape[0], shape[1], shape[2]);

            Map<Character, String> vars = itemData.getCraftingVariable();
            if (vars == null) vars = java.util.Collections.emptyMap();
            for (Map.Entry<Character, String> entry : vars.entrySet()) {
                Character ch = entry.getKey();
                if (ch == null) continue;
                String materialName = entry.getValue();
                if (materialName == null || materialName.trim().isEmpty() || materialName.equalsIgnoreCase("NONE")) continue;
                Material mat = Material.matchMaterial(materialName.trim().toUpperCase());
                if (mat == null || mat == Material.AIR) continue;
                shaped.setIngredient(ch, mat);
            }

            try {
                Bukkit.addRecipe(shaped);
            } catch (Exception ignore) {}
        }
    }
}
