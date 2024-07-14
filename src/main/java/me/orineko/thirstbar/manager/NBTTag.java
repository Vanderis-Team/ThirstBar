package me.orineko.thirstbar.manager;

import me.orineko.pluginspigottools.MethodDefault;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

public class NBTTag {
	
	public static ItemStack setKey(@Nonnull ItemStack item, @Nonnull String key, String value) {
		try {
			Class<?> itemStackClass = getItemStackClass();
			Class<?> craftItemStackClass = getCraftItemStackClass();
			Class<?> nbtTagCompoundClass = getNBTTagCompoundClass();
			Object itemCraft = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
			
			Object tag;
			try {
				String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
				int numVer = (int) MethodDefault.formatNumber(version.split("_")[1], 0);
				int numVer2 = (int) MethodDefault.formatNumber(version.split("_")[2], 0);
				if(numVer >= 8 && numVer <= 17) {
					tag = ((boolean) itemCraft.getClass().getMethod("hasTag").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("getTag").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return item;
					tag.getClass().getMethod("setString", String.class, String.class).invoke(tag, key, value);
					itemCraft.getClass().getMethod("setTag", nbtTagCompoundClass).invoke(itemCraft, tag);
				} else if (numVer == 18) {
					tag = ((boolean) itemCraft.getClass().getMethod("s").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("t").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return item;
					tag.getClass().getMethod("a", String.class, String.class).invoke(tag, key, value);
					itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
				} else if (numVer == 19) {
					tag = ((boolean) itemCraft.getClass().getMethod("t").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("u").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return item;
					tag.getClass().getMethod("a", String.class, String.class).invoke(tag, key, value);
					itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
				} else {
					tag = ((boolean) itemCraft.getClass().getMethod("u").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("w").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return item;
					tag.getClass().getMethod("a", String.class, String.class).invoke(tag, key, value);
					itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
				}
			} catch (ArrayIndexOutOfBoundsException ignore) {
				tag = ((boolean) itemCraft.getClass().getMethod("u").invoke(itemCraft)) ?
						itemCraft.getClass().getMethod("w").invoke(itemCraft) :
						nbtTagCompoundClass.newInstance();
				if (tag == null) return item;
				tag.getClass().getMethod("a", String.class, String.class).invoke(tag, key, value);
				itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
			}
			return (ItemStack) craftItemStackClass.getMethod("asBukkitCopy", itemStackClass).invoke(null, itemCraft);
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
		return item;
	}
	
	public static ItemStack removeKey(@Nonnull ItemStack item, @Nonnull String key) {
		try {
			Class<?> itemStackClass = getItemStackClass();
			Class<?> craftItemStackClass = getCraftItemStackClass();
			Class<?> nbtTagCompoundClass = getNBTTagCompoundClass();
			Object itemCraft = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
			
			Object tag;
			try {
				
				String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
				int numVer = (int) MethodDefault.formatNumber(version.split("_")[1], 0);
				if(numVer >= 8 && numVer <= 17) {
					tag = ((boolean) itemCraft.getClass().getMethod("hasTag").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("getTag").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return item;
					tag.getClass().getMethod("remove", String.class).invoke(tag, key);
					itemCraft.getClass().getMethod("setTag", nbtTagCompoundClass).invoke(itemCraft, tag);
				} else if (numVer == 18) {
					tag = ((boolean) itemCraft.getClass().getMethod("s").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("t").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return item;
					tag.getClass().getMethod("r", String.class).invoke(tag, key);
					itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
				} else if (numVer == 19) {
					tag = ((boolean) itemCraft.getClass().getMethod("t").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("u").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return item;
					tag.getClass().getMethod("r", String.class).invoke(tag, key);
					itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
				} else {
					tag = ((boolean) itemCraft.getClass().getMethod("u").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("w").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return item;
					tag.getClass().getMethod("r", String.class).invoke(tag, key);
					itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
				}
			} catch (ArrayStoreException ignore) {
				tag = ((boolean) itemCraft.getClass().getMethod("u").invoke(itemCraft)) ?
						itemCraft.getClass().getMethod("w").invoke(itemCraft) :
						nbtTagCompoundClass.newInstance();
				if (tag == null) return item;
				tag.getClass().getMethod("r", String.class).invoke(tag, key);
				itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
			}
			return (ItemStack) craftItemStackClass.getMethod("asBukkitCopy", itemStackClass).invoke(null, itemCraft);
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
		return item;
	}
	
	@Nullable
	public static String getKey(@Nonnull ItemStack item, @Nonnull String key) {
		try {
			Class<?> craftItemStackClass = getCraftItemStackClass();
			Class<?> nbtTagCompoundClass = getNBTTagCompoundClass();
			Object itemCraft = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
			
			Object tag;
			try {
				String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
				int numVer = (int) MethodDefault.formatNumber(version.split("_")[1], 0);
				if(numVer >= 8 && numVer <= 17) {
					tag = ((boolean) itemCraft.getClass().getMethod("hasTag").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("getTag").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return null;
					return (String) tag.getClass().getMethod("getString", String.class).invoke(tag, key);
				} else if (numVer == 18) {
					tag = ((boolean) itemCraft.getClass().getMethod("s").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("t").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return null;
					return (String) tag.getClass().getMethod("l", String.class).invoke(tag, key);
				} else if (numVer == 19) {
					tag = ((boolean) itemCraft.getClass().getMethod("t").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("u").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return null;
					return (String) tag.getClass().getMethod("l", String.class).invoke(tag, key);
				} else {
					tag = ((boolean) itemCraft.getClass().getMethod("u").invoke(itemCraft)) ?
							itemCraft.getClass().getMethod("w").invoke(itemCraft) :
							nbtTagCompoundClass.newInstance();
					if (tag == null) return null;
					return (String) tag.getClass().getMethod("l", String.class).invoke(tag, key);
				}
			} catch (ArrayIndexOutOfBoundsException ignore) {
				tag = ((boolean) itemCraft.getClass().getMethod("u").invoke(itemCraft)) ?
						itemCraft.getClass().getMethod("w").invoke(itemCraft) :
						nbtTagCompoundClass.newInstance();
				if (tag == null) return null;
				return (String) tag.getClass().getMethod("l", String.class).invoke(tag, key);
			}
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Class<?> getNBTTagCompoundClass() throws ClassNotFoundException {
		try {
			
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
			switch (version) {
				case "v1_8_R1":
				case "v1_8_R2":
				case "v1_8_R3":
				case "v1_9_R1":
				case "v1_9_R2":
				case "v1_10_R1":
				case "v1_11_R1":
				case "v1_12_R1":
				case "v1_13_R1":
				case "v1_13_R2":
				case "v1_14_R1":
				case "v1_15_R1":
				case "v1_16_R1":
				case "v1_16_R2":
				case "v1_16_R3":
					return Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
				default:
					return Class.forName("net.minecraft.nbt.NBTTagCompound");
			}
		} catch (ArrayIndexOutOfBoundsException ignore){
			return Class.forName("net.minecraft.nbt.NBTTagCompound");
		}
	}
	
	private static Class<?> getCraftItemStackClass() throws ClassNotFoundException {
		String version;
		try {
			version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		} catch (ArrayIndexOutOfBoundsException ignore) {
			version = "v1_20_R3";
		}
		return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
	}
	
	private static Class<?> getItemStackClass() throws ClassNotFoundException {
		try {
			
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
			switch (version) {
				case "v1_8_R1":
				case "v1_8_R2":
				case "v1_8_R3":
				case "v1_9_R1":
				case "v1_9_R2":
				case "v1_10_R1":
				case "v1_11_R1":
				case "v1_12_R1":
				case "v1_13_R1":
				case "v1_13_R2":
				case "v1_14_R1":
				case "v1_15_R1":
				case "v1_16_R1":
				case "v1_16_R2":
				case "v1_16_R3":
					return Class.forName("net.minecraft.server." + version + ".ItemStack");
				default:
					return Class.forName("net.minecraft.world.item.ItemStack");
			}
		} catch (ArrayIndexOutOfBoundsException ignore) {
			return Class.forName("net.minecraft.world.item.ItemStack");
		}
	}
	
}
