package me.orineko.thirstbar.manager.file;

import me.orineko.pluginspigottools.FileManager;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class MessageData {

    public static HashMap<String, List<String>> HELP;
    public static String SET_ITEM_SUCCESS;
    public static String PLAYER_REFRESH;
    private static String PLAYER_REFRESH_OTHER;
    public static String PLAYER_REFRESH_ALL;
    private static String PLAYER_SET;
    private static String PLAYER_SET_OTHER;
    private static String PLAYER_ADD;
    private static String PLAYER_ADD_OTHER;
    private static String PLAYER_REDUCE;
    private static String PLAYER_REDUCE_OTHER;
    private static String PLAYER_LOAD;
    private static String PLAYER_LOAD_OTHER;
    public static String PLAYER_DISABLE;
    private static String PLAYER_DISABLE_OTHER;
    public static String PLAYER_ENABLE;
    private static String PLAYER_ENABLE_OTHER;
    public static String PLAYER_DISABLE_ALL;
    private static String PLAYER_MAX_SET;
    private static String PLAYER_MAX_SET_OTHER;
    private static String PLAYER_SET_STAGE;
    private static String PLAYER_SET_STAGE_OTHER;
    private static String PLAYER_SET_STAGE_ALL;
    public static String RELOAD;
    public static String RESET;
    public static String ERROR_COMMAND;
    public static String ERROR_ITEM_NOT_FOUND;
    public static String ERROR_STAGE_NOT_FOUND;
    public static String ERROR_PLAYER_NOT_FOUND;
    public static String ERROR_NEED_ITEM_IN_HAND;
    private static String DELAY_REFRESH;
    public static String ERROR_FORMAT;
    public static String ERROR_CONSOLE_USE_COMMAND;
    public static String ERROR_PERMISSION;

    private final FileManager file;

    public MessageData(){
        this.file = ThirstBar.getInstance().getMessageFile();
        HELP = new HashMap<>();
        ConfigurationSection sectionHelp = file.getConfigurationSection("Help");
        if(sectionHelp != null) sectionHelp.getKeys(false).forEach(sec ->
                HELP.put(sec, MethodDefault.formatColor(file.getStringList("Help."+sec))));
        SET_ITEM_SUCCESS = MethodDefault.formatColor(file.getString("SetItemSuccess", ""));
        PLAYER_REFRESH = MethodDefault.formatColor(file.getString("PlayerRefresh", ""));
        PLAYER_REFRESH_OTHER = MethodDefault.formatColor(file.getString("PlayerRefreshOther", ""));
        PLAYER_REFRESH_ALL = MethodDefault.formatColor(file.getString("PlayerRefreshAll", ""));
        PLAYER_SET = MethodDefault.formatColor(file.getString("PlayerSet", ""));
        PLAYER_SET_OTHER = MethodDefault.formatColor(file.getString("PlayerSetOther", ""));
        PLAYER_ADD = MethodDefault.formatColor(file.getString("PlayerAdd", ""));
        PLAYER_ADD_OTHER = MethodDefault.formatColor(file.getString("PlayerAddOther", ""));
        PLAYER_REDUCE = MethodDefault.formatColor(file.getString("PlayerReduce", ""));
        PLAYER_REDUCE_OTHER = MethodDefault.formatColor(file.getString("PlayerReduceOther", ""));
        PLAYER_LOAD = MethodDefault.formatColor(file.getString("PlayerLoad", ""));
        PLAYER_LOAD_OTHER = MethodDefault.formatColor(file.getString("PlayerLoadOther", ""));
        PLAYER_DISABLE = MethodDefault.formatColor(file.getString("PlayerDisable", ""));
        PLAYER_DISABLE_OTHER = MethodDefault.formatColor(file.getString("PlayerDisableOther", ""));
        PLAYER_ENABLE = MethodDefault.formatColor(file.getString("PlayerEnable", ""));
        PLAYER_ENABLE_OTHER = MethodDefault.formatColor(file.getString("PlayerEnableOther", ""));
        PLAYER_DISABLE_ALL = MethodDefault.formatColor(file.getString("PlayerDisableAll", ""));
        PLAYER_MAX_SET = MethodDefault.formatColor(file.getString("PlayerMaxSet", ""));
        PLAYER_MAX_SET_OTHER = MethodDefault.formatColor(file.getString("PlayerMaxSetOther", ""));
        PLAYER_SET_STAGE = MethodDefault.formatColor(file.getString("PlayerSetStage", ""));
        PLAYER_SET_STAGE_OTHER = MethodDefault.formatColor(file.getString("PlayerSetStageOther", ""));
        PLAYER_SET_STAGE_ALL = MethodDefault.formatColor(file.getString("PlayerSetStageAll", ""));
        RELOAD = MethodDefault.formatColor(file.getString("Reload", ""));
        RESET = MethodDefault.formatColor(file.getString("Reset", ""));
        ERROR_COMMAND = MethodDefault.formatColor(file.getString("ErrorCommand", ""));
        ERROR_ITEM_NOT_FOUND = MethodDefault.formatColor(file.getString("ErrorItemNotFound", ""));
        ERROR_STAGE_NOT_FOUND = MethodDefault.formatColor(file.getString("ErrorStageNotFound", ""));
        ERROR_PLAYER_NOT_FOUND = MethodDefault.formatColor(file.getString("ErrorPlayerNotFound", ""));
        ERROR_NEED_ITEM_IN_HAND = MethodDefault.formatColor(file.getString("ErrorNeedItemInHand", ""));
        DELAY_REFRESH = MethodDefault.formatColor(file.getString("DelayRefresh", ""));
        ERROR_FORMAT = MethodDefault.formatColor(file.getString("ErrorFormat", ""));
        ERROR_CONSOLE_USE_COMMAND = MethodDefault.formatColor(file.getString("ErrorConsoleUseCommand", ""));
        ERROR_PERMISSION = MethodDefault.formatColor(file.getString("ErrorPermission", ""));
    }

    public FileManager getFile() {
        return file;
    }

    public static String PLAYER_REFRESH_OTHER(@Nonnull String player) {
        return PLAYER_REFRESH_OTHER.replace("<player>", player);
    }

    public static String PLAYER_SET(@Nonnull String value) {
        return PLAYER_SET.replace("<value>", value);
    }

    public static String PLAYER_SET_OTHER(@Nonnull String player, @Nonnull String value) {
        return PLAYER_SET_OTHER.replace("<player>", player).replace("<value>", value);
    }

    public static String PLAYER_ADD(@Nonnull String value) {
        return PLAYER_ADD.replace("<value>", value);
    }

    public static String PLAYER_ADD_OTHER(@Nonnull String player, @Nonnull String value) {
        return PLAYER_ADD_OTHER.replace("<player>", player).replace("<value>", value);
    }

    public static String PLAYER_REDUCE(@Nonnull String value) {
        return PLAYER_REDUCE.replace("<value>", value);
    }

    public static String PLAYER_REDUCE_OTHER(@Nonnull String player, @Nonnull String value) {
        return PLAYER_REDUCE_OTHER.replace("<player>", player).replace("<value>", value);
    }

    public static String PLAYER_LOAD(@Nonnull String item) {
        return PLAYER_LOAD.replace("<item>", item);
    }

    public static String PLAYER_LOAD_OTHER(@Nonnull String player, @Nonnull String item) {
        return PLAYER_LOAD_OTHER.replace("<player>", player).replace("<item>", item);
    }

    public static String PLAYER_DISABLE_OTHER(@Nonnull String player){
        return PLAYER_DISABLE_OTHER.replace("<player>", player);
    }

    public static String PLAYER_ENABLE_OTHER(@Nonnull String player){
        return PLAYER_ENABLE_OTHER.replace("<player>", player);
    }

    public static String PLAYER_MAX_SET(@Nonnull String value) {
        return PLAYER_MAX_SET.replace("<value>", value);
    }

    public static String PLAYER_MAX_SET_OTHER(@Nonnull String player, @Nonnull String value) {
        return PLAYER_MAX_SET_OTHER.replace("<player>", player).replace("<value>", value);
    }

    public static String PLAYER_SET_STAGE(@Nonnull String stage) {
        return PLAYER_SET_STAGE.replace("<stage>", stage);
    }

    public static String PLAYER_SET_STAGE_OTHER(@Nonnull String player, @Nonnull String stage) {
        return PLAYER_SET_STAGE_OTHER.replace("<player>", player).replace("<stage>", stage);
    }

    public static String PLAYER_SET_STAGE_ALL(@Nonnull String stage) {
        return PLAYER_SET_STAGE_ALL.replace("<stage>", stage);
    }

    public static String DELAY_REFRESH(@Nonnull String time) {
        return DELAY_REFRESH.replace("<time>", time);
    }
}
