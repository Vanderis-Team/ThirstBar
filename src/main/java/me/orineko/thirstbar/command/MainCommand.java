package me.orineko.thirstbar.command;

import me.orineko.pluginspigottools.FileManager;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.Method;
import me.orineko.thirstbar.manager.file.MessageData;
import me.orineko.thirstbar.manager.item.ItemData;
import me.orineko.thirstbar.manager.player.PlayerData;
import me.orineko.thirstbar.manager.stage.Stage;
import me.orineko.thirstbar.manager.stage.StageTimeline;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CommandManager.CommandInfo(aliases = {"ThirstBar", "TB", "Refresh", "RefreshAll"}, permissions = {"thirstbar.admin"})
public class MainCommand extends CommandManager {
    public MainCommand(@Nonnull Plugin plugin) {
        super(plugin);
    }

    @Nullable
    @Override
    public List<String> executeTabCompleter(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (checkEqualArgs(args, 0, "item")) {
            if (checkEqualArgs(args, 1, "save", "give")) {
                if (args.length == 3) return ThirstBar.getInstance().getItemDataList().getDataList()
                        .stream().filter(v -> !v.isVanilla()).map(ItemData::getName).collect(Collectors.toList());
            }
        }
        if (checkEqualArgs(args, 0, "help")) {
            if (args.length == 2) return new ArrayList<>(MessageData.HELP.keySet());
        }
        if (checkEqualArgs(args, 0, "stage", "stageall")) {
            if (args.length == 2) return ThirstBar.getInstance().getStageList().getStageTimelineList()
                    .stream().map(Stage::getName).collect(Collectors.toList());
        }
        return null;
    }

    @CommandSub(length = 0, names = "help", permissions = "thirstbar.help")
    public void onHelp(CommandSender sender, String[] args) {
        if (MessageData.HELP.isEmpty()) return;

        List<String> list = null;
        if(args.length >= 1) {
            if(checkEqualArgs(args, 0, "help")) {
                if(args.length >= 2){
                    if(args[1].equalsIgnoreCase("2")){
                        list = MessageData.HELP.getOrDefault("2", null);
                    } else {
                        list = MessageData.HELP.getOrDefault("1", null);
                    }
                }
            } else if (args[0].equalsIgnoreCase("1")) {
                list = MessageData.HELP.getOrDefault("1", null);
            } else {
                list = MessageData.HELP.getOrDefault("1", null);
            }
        } else {
            list = MessageData.HELP.getOrDefault("1", null);
        }
        if (list == null) return;
        list.forEach(sender::sendMessage);
    }

    @CommandSub(length = 0, names = "1", permissions = "thirstbar.help")
    public void onHelp1(CommandSender sender, String[] args) {
        if (MessageData.HELP.isEmpty()) return;

        List<String> list = MessageData.HELP.getOrDefault("1", null);
        if (list == null) return;
        list.forEach(sender::sendMessage);
    }

    @CommandSub(length = 0, names = "2", permissions = "thirstbar.help")
    public void onHelp2(CommandSender sender, String[] args) {
        if (MessageData.HELP.isEmpty()) return;

        List<String> list = MessageData.HELP.getOrDefault("2", null);
        if (list == null) return;
        list.forEach(sender::sendMessage);
    }

    @CommandSub(length = 1, names = "reload", permissions = "thirstbar.reload")
    public void onReload(CommandSender sender, String[] args) {
        ThirstBar.getInstance().getPlayerDataList().removeDataPlayersOnline();
        ThirstBar.getInstance().renewData();
        sender.sendMessage(MessageData.RELOAD);
    }

    @CommandSub(length = 0, command = "Refresh")
    public void onRefresh(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (!(sender instanceof Player &&
                    (sender.isOp() ||
                            sender.hasPermission("thirstbar.refresh.other")) ||
                    sender.hasPermission("thirstbar.admin"))) {
                sender.sendMessage(MessageData.ERROR_PERMISSION);
                return;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (checkObjectIsNull(player, sender, MessageData.ERROR_PLAYER_NOT_FOUND) || player == null) return;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (data.getName().equals(player.getName()))
                if (checkObjectIsFalse(data.delayRefresh() <= 0, sender, MessageData.DELAY_REFRESH(String.valueOf(data.delayRefresh()))))
                    return;
            data.refresh();
            if (data.getName().equals(player.getName()))
                data.executeRefresh();
            data.updateAll(player);
            player.sendMessage(MessageData.PLAYER_REFRESH);
            if (!sender.getName().equals(player.getName()))
                sender.sendMessage(MessageData.PLAYER_REFRESH_OTHER(player.getName()));
        } else {
            if (checkObjectIsFalse(sender instanceof Player, sender, MessageData.ERROR_CONSOLE_USE_COMMAND) || !(sender instanceof Player))
                return;
            if (!(sender.isOp() ||
                    sender.hasPermission("thirstbar.refresh") ||
                    sender.hasPermission("thirstbar.admin"))) {
                sender.sendMessage(MessageData.ERROR_PERMISSION);
                return;
            }
            Player player = (Player) sender;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (checkObjectIsFalse(data.delayRefresh() <= 0, sender, MessageData.DELAY_REFRESH(String.valueOf(data.delayRefresh()))))
                return;
            data.refresh();
            data.executeRefresh();
            data.updateAll(player);
            player.sendMessage(MessageData.PLAYER_REFRESH);
        }
    }

    @CommandSub(length = 0, command = "RefreshAll", permissions = "thirstbar.refreshall")
    public void onRefreshAll(CommandSender sender, String[] args) {
        ThirstBar.getInstance().getPlayerDataList().getDataList().forEach(playerData -> {
            playerData.refresh();
            Player player = Bukkit.getPlayer(playerData.getName());
            if (player == null) return;
            playerData.updateAll(player);
            if (player.getName().equals(sender.getName())) return;
            player.sendMessage(MessageData.PLAYER_REFRESH);
        });
        sender.sendMessage(MessageData.PLAYER_REFRESH_ALL);
    }

    @CommandSub(length = 2, names = "set", permissions = "thirstbar.set.current")
    public void onSet(CommandSender sender, String[] args) {
        if (checkObjectIsFalse(MethodDefault.checkFormatNumber(args[1]), sender, MessageData.ERROR_FORMAT)) return;
        double value = MethodDefault.formatNumber(args[1], 0);
        if (value < 0) value = 0;
        if (args.length > 2) {
            Player player = Bukkit.getPlayer(args[2]);
            if (checkObjectIsNull(player, sender, MessageData.ERROR_PLAYER_NOT_FOUND) || player == null) return;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (value > data.getThirstMax()) value = data.getThirstMax();
            data.setThirst(value);
            data.updateAll(player);
            player.sendMessage(MessageData.PLAYER_SET(String.valueOf(value)));
            if (!sender.getName().equals(player.getName()))
                sender.sendMessage(MessageData.PLAYER_SET_OTHER(player.getName(), String.valueOf(value)));
        } else {
            if (checkObjectIsFalse(sender instanceof Player, sender, MessageData.ERROR_CONSOLE_USE_COMMAND) || !(sender instanceof Player))
                return;
            Player player = (Player) sender;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (value > data.getThirstMax()) value = data.getThirstMax();
            data.setThirst(value);
            data.updateAll(player);
            player.sendMessage(MessageData.PLAYER_SET(String.valueOf(value)));
        }
    }

    @CommandSub(length = 2, names = "restore", permissions = "thirstbar.restore")
    public void onRestore(CommandSender sender, String[] args) {
        if (checkObjectIsFalse(MethodDefault.checkFormatNumber(args[1]), sender, MessageData.ERROR_FORMAT)) return;
        double value = MethodDefault.formatNumber(args[1], 0);
        if (args.length > 2) {
            Player player = Bukkit.getPlayer(args[2]);
            if (checkObjectIsNull(player, sender, MessageData.ERROR_PLAYER_NOT_FOUND) || player == null) return;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (value < 0 && data.getThirst() + value < 0) value = -data.getThirst();
            else if (value > 0 && data.getThirst() + value > data.getThirstMax()) value = data.getThirstMax();
            data.addThirst(value);
            data.updateAll(player);
            player.sendMessage(MessageData.PLAYER_ADD(String.valueOf(value)));
            if (!sender.getName().equals(player.getName()))
                sender.sendMessage(MessageData.PLAYER_ADD_OTHER(player.getName(), String.valueOf(value)));
        } else {
            if (checkObjectIsFalse(sender instanceof Player, sender, MessageData.ERROR_CONSOLE_USE_COMMAND) || !(sender instanceof Player))
                return;
            Player player = (Player) sender;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (value < 0 && data.getThirst() + value < 0) value = -data.getThirst();
            else if (value > 0 && data.getThirst() + value > data.getThirstMax()) value = data.getThirstMax();
            data.addThirst(value);
            data.updateAll(player);
            player.sendMessage(MessageData.PLAYER_ADD(String.valueOf(value)));
        }
    }

    @CommandSub(length = 2, names = "reduce", permissions = "thirstbar.reduce")
    public void onReduce(CommandSender sender, String[] args) {
        if (checkObjectIsFalse(MethodDefault.checkFormatNumber(args[1]), sender, MessageData.ERROR_FORMAT)) return;
        double value = -MethodDefault.formatNumber(args[1], 0);
        if (args.length > 2) {
            Player player = Bukkit.getPlayer(args[2]);
            if (checkObjectIsNull(player, sender, MessageData.ERROR_PLAYER_NOT_FOUND) || player == null) return;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (value < 0 && data.getThirst() + value < 0) value = -data.getThirst();
            else if (value > 0 && data.getThirst() + value > data.getThirstMax())
                value = data.getThirstMax() - data.getThirst();
            data.addThirst(value);
            data.updateAll(player);
            player.sendMessage(MessageData.PLAYER_REDUCE(String.valueOf(-value)));
            if (!sender.getName().equals(player.getName()))
                sender.sendMessage(MessageData.PLAYER_REDUCE_OTHER(player.getName(), String.valueOf(-value)));
        } else {
            if (checkObjectIsFalse(sender instanceof Player, sender, MessageData.ERROR_CONSOLE_USE_COMMAND) || !(sender instanceof Player))
                return;
            Player player = (Player) sender;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (value < 0 && data.getThirst() + value < 0) value = -data.getThirst();
            else if (value > 0 && data.getThirst() + value > data.getThirstMax())
                value = data.getThirstMax() - data.getThirst();
            data.addThirst(value);
            data.updateAll(player);
            player.sendMessage(MessageData.PLAYER_REDUCE(String.valueOf(-value)));
        }
    }

    @CommandSub(length = 1, names = "disable", permissions = "thirstbar.disable")
    public void onDisable(CommandSender sender, String[] args) {
        if (args.length > 1) {
            Player player = Bukkit.getPlayer(args[1]);
            if (checkObjectIsNull(player, sender, MessageData.ERROR_PLAYER_NOT_FOUND) || player == null) return;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (data.isDisable()) {
                data.setDisable(false);
                player.sendMessage(MessageData.PLAYER_ENABLE);
                if (!sender.getName().equals(player.getName()))
                    sender.sendMessage(MessageData.PLAYER_ENABLE_OTHER(player.getName()));
            } else {
                data.setDisable(true);
                player.sendMessage(MessageData.PLAYER_DISABLE);
                if (!sender.getName().equals(player.getName()))
                    sender.sendMessage(MessageData.PLAYER_DISABLE_OTHER(player.getName()));
            }
        } else {
            if (checkObjectIsFalse(sender instanceof Player, sender, MessageData.ERROR_CONSOLE_USE_COMMAND) || !(sender instanceof Player))
                return;
            Player player = (Player) sender;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            if (data.isDisable()) {
                data.setDisable(false);
                player.sendMessage(MessageData.PLAYER_ENABLE);
            } else {
                data.setDisable(true);
                player.sendMessage(MessageData.PLAYER_DISABLE);
            }
        }
    }

    @CommandSub(length = 1, names = "disableall", permissions = "thirstbar.disableall")
    public void onDisableAll(CommandSender sender, String[] args) {
        ThirstBar.getInstance().getPlayerDataList().getDataList().forEach(playerData -> {
            playerData.setDisable(true);
            Player player = Bukkit.getPlayer(playerData.getName());
            if (player == null) return;
            if (player.getName().equals(sender.getName())) return;
            sender.sendMessage(MessageData.PLAYER_DISABLE_OTHER(playerData.getName()));
        });
        sender.sendMessage(MessageData.PLAYER_DISABLE_ALL);
    }

    @CommandSub(length = 3, names = {"max", "set"}, permissions = "thirstbar.set.max")
    public void onMaxSet(CommandSender sender, String[] args) {
        if (checkObjectIsFalse(MethodDefault.checkFormatNumber(args[2]), sender, MessageData.ERROR_FORMAT)) return;
        double value = MethodDefault.formatNumber(args[2], 0);
        if (value < 1) value = 1;
        value = BigDecimal.valueOf(value).min(BigDecimal.valueOf(Double.MAX_VALUE)).doubleValue();
        Player player;
        if (args.length > 3) {
            player = Bukkit.getPlayer(args[3]);
            if (checkObjectIsNull(player, sender, MessageData.ERROR_PLAYER_NOT_FOUND) || player == null) return;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            data.setThirstMax(value);
            player.sendMessage(MessageData.PLAYER_MAX_SET(data.getName()));
            if (!sender.getName().equals(player.getName()))
                sender.sendMessage(MessageData.PLAYER_MAX_SET_OTHER(player.getName(), data.getName()));
        } else {
            if (checkObjectIsFalse(sender instanceof Player, sender, MessageData.ERROR_CONSOLE_USE_COMMAND) || !(sender instanceof Player))
                return;
            player = (Player) sender;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            data.setThirstMax(value);
            player.sendMessage(MessageData.PLAYER_MAX_SET(data.getName()));
        }
        ThirstBar.getInstance().getPlayersFile().setAndSave(player.getName() + ".Max", value);
    }

    @CommandSub(length = 4, names = {"item", "save"}, justPlayerUseCmd = true, permissions = "thirstbar.item.save")
    public void onItemSave(Player player, String[] args) {
        ItemStack item = player.getItemInHand();
        if (checkObjectIsFalse(!item.getType().equals(Material.AIR), player, MessageData.ERROR_NEED_ITEM_IN_HAND))
            return;
        String name = args[2];
        String valueString = args[3];
        boolean percent = false;
        if (valueString.endsWith("%")) {
            percent = true;
            valueString = valueString.replace("%", "");
        }
        if (checkObjectIsFalse(MethodDefault.checkFormatNumber(valueString), player, MessageData.ERROR_FORMAT)) return;
        double value = MethodDefault.formatNumber(valueString, 0);
        ItemData itemData = ThirstBar.getInstance().getItemDataList().addData(name, item);
        if (percent) itemData.setValuePercent(value);
        else itemData.setValue(value);
        itemData.saveData();
        player.sendMessage(MessageData.SET_ITEM_SUCCESS);
    }

    @CommandSub(length = 3, names = {"item", "give"}, permissions = "thirstbar.item.give")
    public void onItemGive(CommandSender sender, String[] args) {
        String name = args[2];
        ItemData data = ThirstBar.getInstance().getItemDataList().getData(name);
        if (checkObjectIsNull(data, sender, MessageData.ERROR_ITEM_NOT_FOUND) || data == null) return;
        if (checkObjectIsNull(data.getItemStack(), sender, MessageData.ERROR_ITEM_NOT_FOUND) || data.getItemStack() == null)
            return;
        if (args.length > 3) {
            Player player = Bukkit.getPlayer(args[3]);
            if (checkObjectIsNull(player, sender, MessageData.ERROR_PLAYER_NOT_FOUND) || player == null) return;
            ItemStack item = data.getItemStack();
            if(args.length > 4){
                if(checkObjectIsFalse(MethodDefault.checkFormatNumber(args[4]), sender, MessageData.ERROR_FORMAT)) return;
                item.setAmount((int) MethodDefault.formatNumber(args[4], 1));
            }
            Method.sendItemToInv(player, item);

            player.sendMessage(MessageData.PLAYER_LOAD(data.getName()));
            if (!sender.getName().equals(player.getName()))
                sender.sendMessage(MessageData.PLAYER_LOAD_OTHER(player.getName(), data.getName()));
        } else {
            if (checkObjectIsFalse(sender instanceof Player, sender, MessageData.ERROR_CONSOLE_USE_COMMAND) || !(sender instanceof Player))
                return;
            Player player = (Player) sender;
            Method.sendItemToInv(player, data.getItemStack());
            player.sendMessage(MessageData.PLAYER_LOAD(data.getName()));
        }
    }

    @CommandSub(length = 1, names = "reset", permissions = "thirstbar.reset")
    public void onReset(CommandSender sender, String[] args) {
        FileManager file = ThirstBar.getInstance().getPlayersFile();
        ConfigurationSection section = file.getConfigurationSection("");
        if (section != null) section.getKeys(false).forEach(sec -> file.set(sec, null));
        file.save();
        ThirstBar.getInstance().getPlayerDataList().removeDataPlayersOnline();
        ThirstBar.getInstance().renewData();
        sender.sendMessage(MessageData.RESET);
    }

    @CommandSub(length = 2, names = "stage", permissions = "thirstbar.stage")
    public void onStage(CommandSender sender, String[] args) {
        String stageString = args[1];
        StageTimeline stageTimeline = ThirstBar.getInstance().getStageList().getStageTimeline(stageString);
        if (checkObjectIsNull(stageTimeline, sender, MessageData.ERROR_STAGE_NOT_FOUND) || stageTimeline == null)
            return;
        if (args.length > 2) {
            Player player = Bukkit.getPlayer(args[2]);
            if (checkObjectIsNull(player, sender, MessageData.ERROR_PLAYER_NOT_FOUND) || player == null) return;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            data.setThirst(stageTimeline.getThirstMax());
            data.updateAll(player);
            player.sendMessage(MessageData.PLAYER_SET_STAGE(stageTimeline.getName()));
            if (!sender.getName().equals(player.getName()))
                sender.sendMessage(MessageData.PLAYER_SET_STAGE_OTHER(player.getName(), stageTimeline.getName()));
        } else {
            if (checkObjectIsFalse(sender instanceof Player, sender, MessageData.ERROR_CONSOLE_USE_COMMAND) || !(sender instanceof Player))
                return;
            Player player = (Player) sender;
            PlayerData data = ThirstBar.getInstance().getPlayerDataList().addData(player);
            data.setThirst(stageTimeline.getThirstMax());
            data.updateAll(player);
            sender.sendMessage(MessageData.PLAYER_SET_STAGE(stageTimeline.getName()));
        }
    }

    @CommandSub(length = 2, names = "stageall", permissions = "thirstbar.stageall")
    public void onStageAll(CommandSender sender, String[] args) {
        String stageString = args[1];
        StageTimeline stageTimeline = ThirstBar.getInstance().getStageList().getStageTimeline(stageString);
        if (checkObjectIsNull(stageTimeline, sender, MessageData.ERROR_STAGE_NOT_FOUND) || stageTimeline == null)
            return;
        ThirstBar.getInstance().getPlayerDataList().getDataList().forEach(playerData -> {
            playerData.setThirst(stageTimeline.getThirstMax());
            Player player = Bukkit.getPlayer(playerData.getName());
            if (player == null) return;
            playerData.updateAll(player);
            if (player.getName().equals(sender.getName())) return;
            player.sendMessage(MessageData.PLAYER_SET_STAGE(stageTimeline.getName()));
        });
    }

    @Nonnull
    @Override
    protected String getErrorCommandMessage() {
        return MessageData.ERROR_COMMAND;
    }

    @Nullable
    @Override
    protected String getJustPlayerCanUseMessage() {
        return MessageData.ERROR_CONSOLE_USE_COMMAND;
    }

    @Nullable
    @Override
    protected String getErrorPermissionMessage() {
        return MessageData.ERROR_PERMISSION;
    }
}
