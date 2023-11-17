package me.orineko.thirstbar;

import me.orineko.pluginspigottools.FileManager;
import me.orineko.thirstbar.command.CommandManager;
import me.orineko.thirstbar.command.MainCommand;
import me.orineko.thirstbar.listener.ThirstListener;
import me.orineko.thirstbar.manager.Method;
import me.orineko.thirstbar.manager.api.PlaceholderAPI;
import me.orineko.thirstbar.manager.api.worldguardapi.WorldGuardApi;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.file.MessageData;
import me.orineko.thirstbar.manager.item.ItemDataList;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.player.PlayerDataList;
import me.orineko.thirstbar.manager.stage.StageList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = this;
        itemsFile = new FileManager("customitems.db", this);
        messageFile = new FileManager("message.yml", this);
        playersFile = new FileManager("players.db", this);
        stageFile = new FileManager("stages.yml", this);
        itemsFile.createFile();
        messageFile.copyDefault();
        playersFile.createFile();
        stageFile.copyDefault();
        renewData();
        Bukkit.getOnlinePlayers().forEach(Method::disableGameMode);
        CommandManager.CommandRegistry.register(true, this, new MainCommand(this));
        getServer().getPluginManager().registerEvents(new ThirstListener(), this);
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) new PlaceholderAPI().register();
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

    public void renewData(){
        ThirstListener.armorStandMap.forEach((k, v) -> v.remove());
        reloadConfig();
        itemsFile.reloadWithoutCreateFile();
        messageFile.reload();
        playersFile.reloadWithoutCreateFile();
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

    private void registerFlag(){
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
}
