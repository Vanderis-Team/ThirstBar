package me.orineko.thirstbar;

import me.orineko.pluginspigottools.FileManager;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.command.CommandManager;
import me.orineko.thirstbar.command.MainCommand;
import me.orineko.thirstbar.listener.ThirstListener;
import me.orineko.thirstbar.manager.Method;
import me.orineko.thirstbar.manager.api.PlaceholderAPI;
import me.orineko.thirstbar.manager.api.UpdateChecker;
import me.orineko.thirstbar.manager.api.sql.SqlManager;
import me.orineko.thirstbar.manager.api.worldguardapi.WorldGuardApi;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.file.MessageData;
import me.orineko.thirstbar.manager.item.ItemDataList;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.player.PlayerDataList;
import me.orineko.thirstbar.manager.stage.StageList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ThirstBar extends JavaPlugin {

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
    private boolean worldGuardApiEnable = false;
    private SqlManager sqlManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = this;
        sqlManager = new SqlManager();

        messageFile = new FileManager("message.yml", this);
        stageFile = new FileManager("stages.yml", this);
        messageFile.copyDefault();
        stageFile.copyDefault();

        if (sqlManager.getConnection() == null) {
            itemsFile = new FileManager("customitems.db", this);
            playersFile = new FileManager("players.db", this);
            itemsFile.createFile();
            playersFile.createFile();
        }

        renewData();
        Bukkit.getOnlinePlayers().forEach(Method::disableGameMode);
        CommandManager.CommandRegistry.register(true, this, new MainCommand(this));
        getServer().getPluginManager().registerEvents(new ThirstListener(), this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) new PlaceholderAPI().register();
        checkForUpdate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerFlag();
    }

    @Override
    public void onDisable() {
        ThirstListener.armorStandMap.forEach((k, v) -> v.remove());
        getPlayerDataList().removeDataPlayersOnline();
    }

    public void renewData() {
        ThirstListener.armorStandMap.forEach((k, v) -> v.remove());
        Bukkit.getOnlinePlayers().forEach(player -> {
            ArmorStand armorStand = player.getWorld().spawn(
                    new Location(player.getWorld(), 0, 0, 0), ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            ThirstListener.armorStandMap.put(player.getUniqueId(), armorStand);
        });

        reloadConfig();
        if (sqlManager.getConnection() == null) {
            itemsFile.reloadWithoutCreateFile();
            playersFile.reloadWithoutCreateFile();
        }
        messageFile.reload();
        stageFile.reload();
        configData = new ConfigData();
        messageData = new MessageData();
        itemDataList = new ItemDataList();
        itemDataList.loadData();
        stageList = new StageList();
        playerDataList = new PlayerDataList();
        Bukkit.getOnlinePlayers().forEach(p -> {
            PlayerData playerData = getPlayerDataList().addData(p);
            boolean check = ConfigData.DISABLED_WORLDS.stream().anyMatch(w ->
                    p.getWorld().getName().trim().equalsIgnoreCase(w.trim()));
            playerData.setDisableAll(check);
            playerData.updateAll(p);
        });
        playerDataList.loadData();
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
            String versions = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            List<Player> playerList = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.isOp() || p.hasPermission("thirstbar.admin"))
                    .collect(Collectors.toList());
            switch (versions) {
                case "v1_9_R1":
                case "v1_9_R2":
                case "v1_10_R1":
                case "v1_11_R1":
                case "v1_12_R1":
                case "v1_13_R1":
                case "v1_13_R2":
                case "v1_14_R1":
                case "v1_15_R1":
                    if (this.getDescription().getVersion().equals(version)) {
                        textList.add("§b[ThirstBar] §aThere is not a new update available.");
                    } else {
                        textList.add("§b[ThirstBar] §7The plugin version you are using is §4out of date§7!");
                        textList.add("§b[ThirstBar] §7There is a new update available.");
                        textList.add("§b[ThirstBar] §7Download it here: §6https://www.spigotmc.org/resources/1-9-1-20-1-%E2%9A%A1-thirst-bar-%E2%9A%A1-add-thirst-unit-for-player-%E2%AD%90-placeholderapi-and-worldguard-support.113587/");
                    }
                    textList.forEach(t -> Bukkit.getConsoleSender().sendMessage(t));
                    playerList.forEach(p -> textList.forEach(p::sendMessage));
                    break;
                default:
                    String textTitle = MethodDefault.formatColor("§7§l[§r#007fff§lT#008afe§lH#0095fe§lI#009ffd§lR#00aafd§lS#00b5fc§lT#17bdf8 #2ec5f3§lB#44ccef§lA#5bd4ea§lR§7§l]");
                    if (this.getDescription().getVersion().equals(version)) {
                        textList.add(textTitle + " §aThere is not a new update available.");
                    } else {
                        textList.add(textTitle + " §7The plugin version you are using is §4out of date§7!");
                        textList.add(textTitle + " §7There is a new update available.");
                        textList.add(textTitle + " §7Download it here: §6https://www.spigotmc.org/resources/1-9-1-20-1-%E2%9A%A1-thirst-bar-%E2%9A%A1-add-thirst-unit-for-player-%E2%AD%90-placeholderapi-and-worldguard-support.113587/");
                    }
                    textList.forEach(t -> Bukkit.getConsoleSender().sendMessage(t));
                    playerList.forEach(p -> textList.forEach(p::sendMessage));
            }
        });
    }

    public static ThirstBar getInstance() {
        return plugin;
    }

    public PlayerDataList getPlayerDataList() {
        return playerDataList;
    }

    public StageList getStageList() {
        return stageList;
    }

    public ItemDataList getItemDataList() {
        return itemDataList;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public MessageData getMessageData() {
        return messageData;
    }

    public FileManager getItemsFile() {
        return itemsFile;
    }

    public FileManager getMessageFile() {
        return messageFile;
    }

    public FileManager getPlayersFile() {
        return playersFile;
    }

    public FileManager getStageFile() {
        return stageFile;
    }

    public boolean isWorldGuardApiEnable() {
        return worldGuardApiEnable;
    }

    public SqlManager getSqlManager() {
        return sqlManager;
    }
}
