package me.orineko.thirstbar.manager;

import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.file.ConfigData;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ItemsAdderDeployer {

    public static void checkAndDeploy() {
        if (!ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_ENABLE) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            ThirstBar.getInstance().getLogger().warning("ItemsAdder is not enabled! Cannot auto-deploy resource pack.");
            return;
        }

        String sourceFolder = ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_AUTO_DEPLOY_SOURCE;
        if (!sourceFolder.endsWith("/")) {
            sourceFolder += "/";
        }
        
        File destDir = new File("plugins/ItemsAdder", ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_AUTO_DEPLOY_PATH);
        File cacheDir = new File(ThirstBar.getInstance().getDataFolder(), ".itemsadder_cache");
        boolean changed = false;

        try {
            URL jar = ThirstBar.class.getProtectionDomain().getCodeSource().getLocation();
            try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
                ZipEntry entry;
                
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName();
                    if (name.startsWith(sourceFolder) && !entry.isDirectory()) {
                        String relativePath = name.substring(sourceFolder.length());
                        File targetFile = new File(destDir, relativePath);
                        File cacheFile = new File(cacheDir, relativePath);
                        
                        targetFile.getParentFile().mkdirs();
                        cacheFile.getParentFile().mkdirs();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zip.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        byte[] jarBytes = baos.toByteArray();

                        boolean needCopy = true;
                        // Chỉ so sánh với file cache trong plugin của mình, bỏ qua file bên ItemsAdder vì panel có thể tự sửa file đó
                        if (cacheFile.exists()) {
                            byte[] cacheBytes = Files.readAllBytes(cacheFile.toPath());
                            if (Arrays.equals(jarBytes, cacheBytes)) {
                                needCopy = false;
                            }
                        }

                        if (needCopy) {
                            Files.write(cacheFile.toPath(), jarBytes);
                            Files.write(targetFile.toPath(), jarBytes);
                            changed = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (changed) {
            ThirstBar.getInstance().getLogger().info("ItemsAdder files have been updated! Triggering iareload and iazip...");
            Bukkit.getScheduler().runTask(ThirstBar.getInstance(), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iareload");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iazip");
            });
        }
    }
}
