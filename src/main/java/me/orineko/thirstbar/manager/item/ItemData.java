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
    private boolean vanilla;
    private final FileManager file;

    public ItemData(@Nonnull String name){
        this.name = name;
        this.itemStack = null;
        this.value = 0;
        this.valuePercent = 0;
        this.vanilla = false;
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

    public boolean isVanilla() {
        return vanilla;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setValuePercent(double valuePercent) {
        this.valuePercent = valuePercent;
    }

    public void setVanilla(boolean vanilla) {
        this.vanilla = vanilla;
    }

    public void saveData(boolean percent){
        if(ThirstBar.getInstance().getSqlManager().getConnection() == null) {
            file.setAndSave(name+".Item", itemStack);
            if(!percent) file.setAndSave(name+".Value", value);
            else file.setAndSave(name+".Value", valuePercent+"%");
        } else {
            if(!percent)
                ThirstBar.getInstance().getSqlManager().runAddItems(name, itemStack, value, 0);
            else
                ThirstBar.getInstance().getSqlManager().runAddItems(name, itemStack, 0, valuePercent);
        }
    }
}
