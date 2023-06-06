package me.orineko.thirstbar.manager.item;

import me.orineko.pluginspigottools.DataList;
import me.orineko.pluginspigottools.FileManager;
import me.orineko.pluginspigottools.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
        return super.getData(d -> d.getItemStack() != null && d.getItemStack().isSimilar(itemStack));
    }

    public void removeData(@Nonnull String name){
        super.getDataList().removeIf(d -> d.getName().equals(name));
    }

    public void loadData(){
        FileManager file = ThirstBar.getInstance().getItemsFile();
        ConfigurationSection section = file.getConfigurationSection("");
        if(section != null) section.getKeys(false).forEach(name -> {
            ItemStack itemStack = file.getItemStack(name+".Item", null);
            if(itemStack == null) return;
            String valueString = file.getString(name+".Value", "");
            if(valueString.isEmpty()) return;
            ItemData itemData = addData(name, itemStack);
            double value = 0;
            if(valueString.endsWith("%")){
                value = MethodDefault.formatNumber(valueString.replace("%", ""), 0);
                itemData.setValuePercent(value);
            } else {
                value = MethodDefault.formatNumber(valueString, 0);
                itemData.setValue(value);
            }

        });
    }

}
