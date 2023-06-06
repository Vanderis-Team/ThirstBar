package me.orineko.thirstbar.manager.item;

import me.orineko.pluginspigottools.FileManager;
import me.orineko.thirstbar.ThirstBar;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemData {

    private final String name;
    private ItemStack itemStack;
    private double value;
    private double valuePercent;
    private final FileManager file;

    public ItemData(@Nonnull String name){
        this.name = name;
        this.itemStack = null;
        this.value = 0;
        this.valuePercent = 0;
        this.file = ThirstBar.getInstance().getItemsFile();
    }

    public String getName() {
        return name;
    }

    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getValue() {
        return value;
    }

    public double getValuePercent() {
        return valuePercent;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setValuePercent(double valuePercent) {
        this.valuePercent = valuePercent;
    }

    public void saveData(){
        file.setAndSave(name+".Item", itemStack);
        if(value > 0) file.setAndSave(name+".Value", value);
        else file.setAndSave(name+".Value", valuePercent+"%");
    }
}
