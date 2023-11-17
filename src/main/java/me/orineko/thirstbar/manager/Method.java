package me.orineko.thirstbar.manager;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.file.ConfigData;
import me.orineko.thirstbar.manager.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Method {

    private static int idDelayMessage = 0;
    private static int idDelayMainTitle = 0;
    private static int idDelaySubTitle = 0;
    private static int idDelaySound = 0;

    /**
     * Send item to inventory player
     *
     * @param player    is player to take
     * @param itemStack is item to give
     * @return true (false if full inventory)
     */
    public static boolean sendItemToInv(@Nonnull Player player, @Nonnull ItemStack itemStack) {
        PlayerInventory inventory = player.getInventory();
        if (inventory.firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation().add(0, 0.5, 0), itemStack);
            return false;
        }
        inventory.addItem(itemStack);
        return true;
    }

    public static PotionEffect getPotionEffect(@Nonnull String text) {
        String[] arr = text.split(":");
        if (arr.length == 0) return null;
        String effString = arr[0].trim();
        int power = (arr.length > 1) ? (int) MethodDefault.formatNumber(arr[1].trim(), 1) : 1;
        XPotion.Effect effect = XPotion.parseEffect(effString);
        if (effect == null) return null;
        return new PotionEffect(effect.getEffect().getType(), Integer.MAX_VALUE, power - 1);
    }

    public static String changeDoubleToInt(double value) {
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(value);
    }

    public static void disableGameMode(@Nonnull Player player){
        GameMode gameMode = player.getGameMode();
        PlayerData playerData = ThirstBar.getInstance().getPlayerDataList().getData(player.getName());
        if(playerData == null) return;
        List<String> gamemodeList = ConfigData.DISABLED_GAMEMODE;
        try {
            playerData.setDisableAll(gamemodeList.stream()
                    .anyMatch(g -> gameMode.equals(GameMode.valueOf(g.toUpperCase()))));
        } catch (IllegalArgumentException ignore){

        }
    }

    public static void executeAction(@Nonnull Player player, @Nonnull List<String> textList, boolean stageConfig) {
        List<String> titleMain = new ArrayList<>();
        List<String> titleSub = new ArrayList<>();
        textList.forEach(text -> {
            int index1 = text.indexOf("[");
            int index2 = text.indexOf("]");
            if (index1 == -1 || index2 == -1) return;
            String key = text.substring(index1 + 1, index2).toLowerCase();
            String value = MethodDefault.formatColor(text.substring(index2 + 1)).trim();
            switch (key) {
                case "title":
                    titleMain.add(value);
                    if (titleSub.size() > 0) {
                        String main = titleMain.get(0);
                        String sub = titleSub.get(0);
                        if (main == null || sub == null) return;
                        if(idDelayMainTitle == 0) Titles.sendTitle(player, main, sub);
                        if(stageConfig && idDelayMainTitle == 0) idDelayMainTitle =
                                Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                        () -> idDelayMainTitle = 0, 100);
                        titleMain.remove(0);
                        titleSub.remove(0);
                    }
                    break;
                case "title-sub":
                    titleSub.add(value);
                    if (titleMain.size() > 0) {
                        String main = titleMain.get(0);
                        String sub = titleSub.get(0);
                        if (main == null || sub == null) return;
                        if(idDelaySubTitle == 0) Titles.sendTitle(player, main, sub);
                        if(stageConfig && idDelaySubTitle == 0) idDelaySubTitle =
                                Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                        () -> idDelaySubTitle = 0, 100);
                        titleMain.remove(0);
                        titleSub.remove(0);
                    }
                    break;
                case "message":
                    if(idDelayMessage == 0) player.sendMessage(value);
                    if(stageConfig && idDelayMessage == 0) idDelayMessage =
                            Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                    () -> idDelayMessage = 0, 100);
                    break;
                case "sound":
                    Optional<XSound> xSound = XSound.matchXSound(value);
                    if (!xSound.isPresent()) break;
                    if(idDelaySound == 0) xSound.get().play(player);
                    if(stageConfig && idDelaySound == 0) idDelaySound =
                            Bukkit.getScheduler().scheduleSyncDelayedTask(ThirstBar.getInstance(),
                                    () -> idDelaySound = 0, 100);
                    break;
                case "player":
                    Bukkit.dispatchCommand(player, value);
                    break;
                case "console":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value.replace("<player>", player.getName()));
                    break;
            }
            String titleMainRemain = (titleMain.size() > 0) ? titleMain.get(0) : null;
            String titleSubRemain = (titleSub.size() > 0) ? titleSub.get(0) : null;
            if (titleMainRemain != null) Titles.sendTitle(player, titleMainRemain, "");
            if (titleSubRemain != null) Titles.sendTitle(player, "", titleSubRemain);
        });
    }

}
