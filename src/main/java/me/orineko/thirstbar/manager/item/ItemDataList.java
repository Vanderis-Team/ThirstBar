package me.orineko.thirstbar.manager.item;

import me.orineko.pluginspigottools.DataList;
import me.orineko.pluginspigottools.FileManager;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.api.sql.SqlManager;
import me.orineko.thirstbar.manager.file.ConfigData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ItemDataList extends DataList<ItemData> {

    public ItemData addData(@Nonnull String name) {
        return super.addData(new ItemData(name), getData(name));
    }

    public ItemData addData(@Nonnull String name, @Nonnull ItemStack itemStack){
        ItemData itemData = addData(name);
        itemData.setItemStack(itemStack);
        return itemData;
    }

    @Nullable
    public ItemData getData(@Nonnull String name) {
        return super.getData(d -> d.getName().equals(name));
    }

    @Nullable
    public ItemData getData(@Nonnull ItemStack itemStack) {
        ItemData itemDataCustom = super.getData(d -> d.getItemStack() != null && d.getItemStack().isSimilar(itemStack));
        if(itemDataCustom != null) return itemDataCustom;
        return super.getData(d -> {
            ItemStack finalItemStack = itemStack;
            if(d.getItemStack() != null && d.getItemStack().getType().equals(Material.POTION)){
                finalItemStack = finalItemStack.clone();
                finalItemStack.setItemMeta(null);
            }
            return d.getItemStack() != null
                    && d.getItemStack().isSimilar(finalItemStack) && d.isVanilla();
        });
    }

    public void removeData(@Nonnull String name){
        super.getDataList().removeIf(d -> d.getName().equals(name));
    }

    public void loadData(){
        dataList.clear();
        if(ThirstBar.getInstance().getSqlManager().getConnection() == null){
            FileManager file = ThirstBar.getInstance().getItemsFile();
            ConfigurationSection section = file.getConfigurationSection("");
            if(section != null) section.getKeys(false).forEach(name -> {
                ItemStack itemStack = file.getItemStack(name+".Item", null);
                if(itemStack == null) return;
                String valueString = file.getString(name+".Value", "");
                if(valueString.isEmpty()) return;
                ItemData itemData = addData(name, itemStack);
                double value;
                if(valueString.endsWith("%")){
                    value = MethodDefault.formatNumber(valueString.replace("%", ""), 0);
                    itemData.setValuePercent(value);
                } else {
                    value = MethodDefault.formatNumber(valueString, 0);
                    itemData.setValue(value);
                }
            });
        } else {
            List<HashMap<String, Object>> list = ThirstBar.getInstance().getSqlManager().runGetItems();
            list.forEach(row -> {
                String name = (String) row.getOrDefault("name", null);
                ItemStack[] itemList = SqlManager.ItemSerialization.itemStackArrayFromBase64((String) row.get("item"));
                if(itemList == null) return;
                ItemStack item = itemList[0];
                double value = (double) row.get("value");
                double valuePercent = (double) row.get("value_percent");
                ItemData itemData = addData(name, item);
                itemData.setValue(value);
                itemData.setValuePercent(valuePercent);
            });
        }
        dataList.addAll(getItemDataVanilla());
    }

    public static List<ItemData> getItemDataVanilla(){
        List<ItemData> itemDataList = new ArrayList<>();
        ConfigData.MATERIALS.forEach(v -> {
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
            if (valuePercent > 0) itemData.setValue(valuePercent);
            else itemData.setValue(value);
            itemDataList.add(itemData);
        });
        return itemDataList;
    }

}
