package me.orineko.thirstbar.manager.item;

import me.orineko.pluginspigottools.core.FileManager;
import me.orineko.pluginspigottools.data.MultiIndexDataMap;
import me.orineko.pluginspigottools.utils.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.api.sql.SqlManager;
import me.orineko.thirstbar.manager.file.ConfigData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ItemDataList extends MultiIndexDataMap<ItemData> {

    @Override
    protected void setupIndexes() {
        // No secondary indexes needed — ItemStack lookups use predicate-based search
    }

    @Override
    protected Object getPrimaryKey(ItemData itemData) {
        return itemData.getName();
    }

    public ItemData addData(@Nonnull String name) {
        ItemData existing = getData(name);
        if (existing != null) return existing;
        ItemData itemData = new ItemData(name);
        addData(itemData);
        return itemData;
    }

    public ItemData addData(@Nonnull String name, @Nonnull ItemStack itemStack){
        ItemData itemData = addData(name);
        itemData.setItemStack(itemStack);
        return itemData;
    }

    @Nullable
    public ItemData getData(@Nonnull String name) {
        return getDataByPrimaryKey(name);
    }

    @Nullable
    public ItemData getData(@Nonnull ItemStack itemStack) {
        // First check external items (MMOItems, ItemsAdder)
        ItemData externalData = getData(d -> {
            if (d.getMmoitemsId() != null) {
                try {
                    me.orineko.pluginspigottools.nbtapi.NBTItem nbt = new me.orineko.pluginspigottools.nbtapi.NBTItem(itemStack);
                    if (nbt.hasKey("MMOITEMS_ITEM_TYPE") && nbt.hasKey("MMOITEMS_ITEM_ID")) {
                        String type = nbt.getString("MMOITEMS_ITEM_TYPE");
                        String id = nbt.getString("MMOITEMS_ITEM_ID");
                        String full = type + ":" + id;
                        return full.equalsIgnoreCase(d.getMmoitemsId());
                    }
                } catch (Throwable ignore) {}
            }
            if (d.getItemsadderId() != null) {
                if (!org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) return false;
                try {
                    Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
                    Object customStack = customStackClass.getMethod("byItemStack", ItemStack.class).invoke(null, itemStack);
                    if (customStack != null) {
                        String id = (String) customStackClass.getMethod("getNamespacedID").invoke(customStack);
                        return id != null && id.equalsIgnoreCase(d.getItemsadderId());
                    }
                } catch (Exception ignore) {}
            }
            return false;
        });
        if (externalData != null) return externalData;

        // Then check custom items (exact match)
        ItemData itemDataCustom = getData(d -> d.getItemStack() != null && !d.isVanilla() && d.getItemStack().isSimilar(itemStack));
        if(itemDataCustom != null) return itemDataCustom;
        
        // Then check vanilla items
        return getData(d -> {
            ItemStack finalItemStack = itemStack;
            if(d.getItemStack() != null && d.getItemStack().getType().equals(Material.POTION)){
                finalItemStack = finalItemStack.clone();
                finalItemStack.setItemMeta(null);
            }
            boolean similar = d.getItemStack() != null && d.getItemStack().isSimilar(finalItemStack);
            if (itemStack.getType() == Material.APPLE && d.getItemStack() != null && d.getItemStack().getType() == Material.APPLE) {
                System.out.println("Checking APPLE: similar=" + similar + ", isVanilla=" + d.isVanilla());
            }
            return similar && d.isVanilla();
        });
    }

    public void removeData(@Nonnull String name){
        removeDataByPrimaryKey(name);
    }

    /**
     * Provides backward-compatible List view of all data.
     * Used by commands that iterate/filter items.
     */
    public List<ItemData> getDataList() {
        return new ArrayList<>(getAllData());
    }

    public void loadData(){
        clear();
        FileManager file = ThirstBar.getInstance().getItemsFile();
        
        // Load vanilla items
        List<String> vanillaItems = file.getStringList("vanilla-items");
        vanillaItems.forEach(v -> {
            String[] arr = v.split(":");
            if(arr.length == 0) return;
            String itemString = arr[0].trim();
            ItemStack item = MethodDefault.getItemAllVersion(itemString);
            if(item == null || item.getType().equals(Material.AIR)) return;

            double value = 0;
            double valuePercent = 0;
            if(arr.length >= 2) {
                if(arr[1].trim().endsWith("%")) valuePercent = MethodDefault
                        .formatNumber(arr[1].trim().replace("%", ""), 0);
                else value = MethodDefault.formatNumber(arr[1].trim(), 0);
            }

            ItemData itemData = new ItemData(UUID.randomUUID().toString());
            itemData.setVanilla(true);
            itemData.setItemStack(item);
            if (valuePercent != 0) itemData.setValuePercent(valuePercent);
            else itemData.setValue(value);
            addData(itemData);
        });

        // Load custom items
        ConfigurationSection customSection = file.getConfigurationSection("custom-items");
        if (customSection != null) {
            customSection.getKeys(false).forEach(key -> {
                String path = "custom-items." + key + ".";
                String materialStr = file.getString(path + "material", "");
                String mmoitemsId = file.getString(path + "mmoitems-id", "");
                String restoreStr = file.getString(path + "restore", "0");
                
                double value = 0;
                double valuePercent = 0;
                if(restoreStr.endsWith("%")){
                    valuePercent = MethodDefault.formatNumber(restoreStr.replace("%", ""), 0);
                } else {
                    value = MethodDefault.formatNumber(restoreStr, 0);
                }
                
                ItemData itemData = new ItemData(key);
                if (valuePercent != 0) itemData.setValuePercent(valuePercent);
                else itemData.setValue(value);

                if (!mmoitemsId.isEmpty()) {
                    itemData.setMmoitemsId(mmoitemsId);
                    addData(itemData);
                    return;
                }
                
                if (materialStr.startsWith("itemsadder-")) {
                    itemData.setItemsadderId(materialStr.substring(11));
                    addData(itemData);
                    return;
                }
                
                ItemStack item = MethodDefault.getItemAllVersion(materialStr);
                if(item == null) item = new ItemStack(Material.STONE);
                
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    if (file.contains(path + "custom-model-data")) {
                        meta.setCustomModelData(file.getInt(path + "custom-model-data"));
                    }
                    if (file.contains(path + "display_name")) {
                        meta.setDisplayName(MethodDefault.formatColor(file.getString(path + "display_name")));
                    }
                    if (file.contains(path + "lore")) {
                        meta.setLore(MethodDefault.formatColor(file.getStringList(path + "lore")));
                    }
                    item.setItemMeta(meta);
                }
                itemData.setItemStack(item);
                addData(itemData);
            });
        }
    }

}
