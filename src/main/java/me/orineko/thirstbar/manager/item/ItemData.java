package me.orineko.thirstbar.manager.item;

import lombok.Getter;
import lombok.Setter;
import me.orineko.pluginspigottools.core.FileManager;
import me.orineko.thirstbar.ThirstBar;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Getter
@Setter
public class ItemData {

    private final String name;
    private ItemStack itemStack;
    private double value;
    private double valuePercent;
    private boolean vanilla;
    private String mmoitemsId;
    private String itemsadderId;
    private final FileManager file;

    public ItemData(@Nonnull String name){
        this.name = name;
        this.itemStack = null;
        this.value = 0;
        this.valuePercent = 0;
        this.vanilla = false;
        this.mmoitemsId = null;
        this.itemsadderId = null;
        this.file = ThirstBar.getInstance().getItemsFile();
    }

    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);
    }

    public void saveData(boolean percent){
        if(mmoitemsId != null || itemsadderId != null) return; // Cannot save external items via command easily
        String path = "custom-items." + name + ".";
        file.set(path + "material", itemStack.getType().name());
        if(itemStack.hasItemMeta()){
            org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();
            if(meta.hasCustomModelData()){
                file.set(path + "custom-model-data", meta.getCustomModelData());
            }
            if(meta.hasDisplayName()){
                file.set(path + "display_name", meta.getDisplayName().replace("§", "&"));
            }
            if(meta.hasLore() && meta.getLore() != null){
                java.util.List<String> lore = new java.util.ArrayList<>();
                for(String l : meta.getLore()) lore.add(l.replace("§", "&"));
                file.set(path + "lore", lore);
            }
        }
        if(!percent) file.set(path + "restore", value);
        else file.set(path + "restore", valuePercent+"%");
        file.save();
    }
}
