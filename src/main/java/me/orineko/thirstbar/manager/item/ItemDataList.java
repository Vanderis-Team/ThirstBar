package me.orineko.thirstbar.manager.item;

import me.orineko.pluginspigottools.core.FileManager;
import me.orineko.pluginspigottools.data.MultiIndexDataMap;
import me.orineko.pluginspigottools.utils.MethodDefault;
import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.api.sql.SqlManager;
import me.orineko.thirstbar.manager.file.ConfigData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.attribute.Attribute;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ItemDataList extends MultiIndexDataMap<ItemData> {
    private final NamespacedKey customItemIdKey = new NamespacedKey(ThirstBar.getInstance(), "custom_item_id");

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

        // Match by persistent custom id first (stable even when durability/damage changes)
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            String customId = itemMeta.getPersistentDataContainer().get(customItemIdKey, PersistentDataType.STRING);
            if (customId != null && !customId.isEmpty()) {
                ItemData byId = getData(customId);
                if (byId != null) return byId;
            }
        }

        // Then check custom items (exact match)
        ItemData itemDataCustom = getData(d -> d.getItemStack() != null && !d.isVanilla() && d.getItemStack().isSimilar(itemStack));
        if(itemDataCustom != null) return itemDataCustom;

        // Fallback for legacy custom items where durability changed but identity should stay
        ItemData itemDataIgnoreDamage = getData(d -> {
            if (d.isVanilla() || d.getItemStack() == null) return false;
            ItemStack base = d.getItemStack().clone();
            ItemStack compare = itemStack.clone();
            ItemMeta baseMeta = base.getItemMeta();
            ItemMeta compareMeta = compare.getItemMeta();
            if (baseMeta instanceof Damageable) ((Damageable) baseMeta).setDamage(0);
            if (compareMeta instanceof Damageable) ((Damageable) compareMeta).setDamage(0);
            base.setItemMeta(baseMeta);
            compare.setItemMeta(compareMeta);
            return base.isSimilar(compare);
        });
        if (itemDataIgnoreDamage != null) return itemDataIgnoreDamage;

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
                itemData.setShowDurability(file.getBoolean(path + "show-durability", false));
                itemData.setHideAttribute(file.getBoolean(path + "hide-attribute", false));
                itemData.setRightClickType(file.getString(path + "events.right_click.type"));
                itemData.setRightClickReplace(file.getString(path + "events.right_click.replace"));
                itemData.setConsumeType(file.getString(path + "events.consume.type"));
                itemData.setConsumeMaxUsage(Math.max(0, file.getInt(path + "events.consume.max-usage", 0)));
                itemData.setConsumeRestore(file.getDouble(path + "events.consume.restore", 0));
                itemData.setConsumeWaterType(file.getString(path + "events.consume.water-type"));
                itemData.setConsumeReplace(file.getString(path + "events.consume.replace"));
                itemData.setCookType(file.getString(path + "events.cook.type"));
                itemData.setCookExp((float) file.getDouble(path + "events.cook.exp", 0));
                itemData.setCookTime(Math.max(1, file.getInt(path + "events.cook.cooking-time", 1)));
                itemData.setCookReplace(file.getString(path + "events.cook.replace"));
                itemData.setCraftingType(file.getString(path + "events.crafting.type"));
                itemData.setUsageBarLeft(file.getString(path + "usage-bar.left", ""));
                itemData.setUsageBarFill(file.getString(path + "usage-bar.fill", ""));
                itemData.setUsageBarEmpty(file.getString(path + "usage-bar.empty", ""));
                itemData.setUsageBarRight(file.getString(path + "usage-bar.right", ""));
                itemData.setUsageBarFont(file.getString(path + "usage-bar.font", "minecraft:default"));
                // Load drinking-model visual override
                String drinkMat = file.getString(path + "drinking-model.material", "");
                if (!drinkMat.isEmpty()) {
                    itemData.setDrinkingModelMaterial(drinkMat);
                    itemData.setDrinkingModelData(file.getInt(path + "drinking-model.custom-model-data", 0));
                }
                java.util.Map<Character, String> craftingVar = new java.util.HashMap<>();
                ConfigurationSection variableSec = file.getConfigurationSection(path + "events.crafting.variable");
                if (variableSec != null) {
                    for (String vKey : variableSec.getKeys(false)) {
                        if (vKey.length() != 1) continue;
                        craftingVar.put(vKey.charAt(0), variableSec.getString(vKey, "NONE"));
                    }
                }
                itemData.setCraftingVariable(craftingVar);
                itemData.setCraftingRecipe(file.getStringList(path + "events.crafting.recipe"));

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
                    meta.getPersistentDataContainer().set(customItemIdKey, PersistentDataType.STRING, key);
                    if (file.contains(path + "custom-model-data")) {
                        meta.setCustomModelData(file.getInt(path + "custom-model-data"));
                    }
                    if (file.contains(path + "display_name")) {
                        String displayTemplate = MethodDefault.formatColor(file.getString(path + "display_name"));
                        itemData.setDisplayNameTemplate(displayTemplate);
                        meta.setDisplayName(displayTemplate);
                    }
                    if (file.contains(path + "lore")) {
                        List<String> loreTemplate = MethodDefault.formatColor(file.getStringList(path + "lore"));
                        itemData.setLoreTemplate(new ArrayList<>(loreTemplate));
                        meta.setLore(loreTemplate);
                    }
                    if ("on_player".equalsIgnoreCase(itemData.getConsumeType())) {
                        applyDrinkConsumable(meta);
                    }
                    if (itemData.isHideAttribute()) {
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        try {
                            meta.addItemFlags(ItemFlag.valueOf("HIDE_ATTRIBUTE_MODIFIERS"));
                        } catch (IllegalArgumentException ignore) {}
                        try {
                            meta.addItemFlags(ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP"));
                        } catch (IllegalArgumentException ignore) {}
                        try {
                            meta.getClass().getMethod("setHideTooltip", boolean.class).invoke(meta, true);
                        } catch (Throwable ignore) {}
                        try {
                            meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
                            meta.removeAttributeModifier(Attribute.ATTACK_SPEED);
                        } catch (Throwable ignore) {}
                        // 1.20.5+: base weapon attributes (attack damage, attack speed) are stored
                        // as a Data Component and won't be hidden by ItemFlag alone.
                        // On 1.21+, setAttributeModifiers(null) completely removes the attribute
                        // component so nothing appears in the tooltip.
                        // On older versions the Guava multimap approach is used as fallback.
                        boolean attributeCleared = false;
                        try {
                            // 1.21+ Paper API: passing null removes the attribute data component entirely
                            meta.getClass().getMethod("setAttributeModifiers",
                                    com.google.common.collect.Multimap.class).invoke(meta, (Object) null);
                            attributeCleared = true;
                        } catch (Throwable ignore) {}
                        if (!attributeCleared) {
                            try {
                                com.google.common.collect.Multimap<Attribute, org.bukkit.attribute.AttributeModifier> emptyMap =
                                        com.google.common.collect.LinkedHashMultimap.create();
                                meta.setAttributeModifiers(emptyMap);
                            } catch (Throwable ignore) {}
                        }
                    }
                    item.setItemMeta(meta);
                }
                itemData.setItemStack(item);
                addData(itemData);
            });
        }
    }

    private void applyDrinkConsumable(ItemMeta meta) {
        try {
            Object food = meta.getClass().getMethod("getFood").invoke(meta);
            food.getClass().getMethod("setCanAlwaysEat", boolean.class).invoke(food, true);
            food.getClass().getMethod("setNutrition", int.class).invoke(food, 0);
            food.getClass().getMethod("setSaturation", float.class).invoke(food, 0.0f);
            try {
                food.getClass().getMethod("setEatSeconds", float.class).invoke(food, 1.2f);
            } catch (NoSuchMethodException ignore) {}
            meta.getClass().getMethod("setFood", food.getClass()).invoke(meta, food);
        } catch (Throwable ignore) {
        }
    }

}
