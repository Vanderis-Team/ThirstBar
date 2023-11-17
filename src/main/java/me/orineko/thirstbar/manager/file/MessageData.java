package me.orineko.thirstbar.manager.file;

import me.orineko.pluginspigottools.FileManager;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.Arrays;
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
        ConfigurationSection section = file.getConfigurationSection("Help");
        if(section != null) section.getKeys(false).forEach(sec -> {
            HELP.put(sec, MethodDefault.formatColor(file.getStringList("Help."+sec)));
        });
//        HELP.put("1", MethodDefault.formatColor(help1()));
//        HELP.put("2", MethodDefault.formatColor(help2()));
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

    private static List<String> help1(){
        String title;
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        int numVer = (int)MethodDefault.formatNumber(version.split("_")[1], 0.0);
        if(numVer >= 16) title = "           #0045FF&lT#034AFF&lH#0650FF&lI#0955FF&lR#0C5BFF&lS#1060FF&lT#1366FF&lB#166BFF&lA#1971FF&lR #1C76FF&lC#1F7BFF&lO#2281FF&lM#2586FF&lM#298CFF&lA#2C91FF&lN#2F97FF&lD#329CFF&lS #35A1FF&l[#38A7FF&lP#3BACFF&lA#3EB2FF&lG#42B7FF&lE #45BDFF&l1#48C2FF&l/#4BC8FF&l2#4ECDFF&l]";
        else title = "           &9&lTHIRSTBAR COMMANDS [PAGE 1/2]";
        return MethodDefault.formatColor(Arrays.asList(
                " ",
                title,
                "  &9/tb reload&f: Reload plugin.",
                "  &9/refresh [player]&f: Refresh a player.",
                "  &9/refreshall&f: Refresh all players.",
                "  &9/tb set <value> [player]&f: Set thirst value a player.",
                "  &9/tb add <value> [player]&f: Add thirst value a player.",
                "  &9/tb reduce <value> [player]&f: Add thirst value a player.",
                "  &9/tb max set <value> [player]&f: Set thirst max value a player.",
                " "
        ));
    }

    private static List<String> help2(){
        String title;
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        int numVer = (int)MethodDefault.formatNumber(version.split("_")[1], 0.0);
        if(numVer >= 16) title = "           #0045FF&lT#034AFF&lH#0650FF&lI#0955FF&lR#0C5BFF&lS#1060FF&lT#1366FF&lB#166BFF&lA#1971FF&lR #1C76FF&lC#1F7BFF&lO#2281FF&lM#2586FF&lM#298CFF&lA#2C91FF&lN#2F97FF&lD#329CFF&lS #35A1FF&l[#38A7FF&lP#3BACFF&lA#3EB2FF&lG#42B7FF&lE #45BDFF&l2#48C2FF&l/#4BC8FF&l2#4ECDFF&l]";
        else title = "           &9&lTHIRSTBAR COMMANDS [PAGE 2/2]";
        return MethodDefault.formatColor(Arrays.asList(
                " ",
                title,
                "  &9/tb reset&f: Reset default all player.",
                "  &9/tb disable [player]&f: Disable thirst a player.",
                "  &9/tb disableall&f: Disable thirst all player.",
                "  &9/tb stage <stage> [player]&f: Set stage a player.",
                "  &9/tb stageall <stage> &f: Set stage all player.",
                "  &9/tb item save <name> <value>&f: Save item.",
                "  &9/tb item give <name> [player]&f: Give item.",
                " "
        ));
    }

}
