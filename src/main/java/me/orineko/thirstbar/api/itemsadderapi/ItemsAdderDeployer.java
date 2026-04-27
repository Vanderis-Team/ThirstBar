package me.orineko.thirstbar.api.itemsadderapi;

import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.file.ConfigData;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ItemsAdderDeployer {
    public static boolean pendingZip = false;

    public static void checkAndDeploy() {
        if (!ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_ENABLE)
            return;
        if (!Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            Bukkit.getConsoleSender()
                    .sendMessage("§b[ThirstBar] §cItemsAdder is not enabled! Cannot auto-deploy resource pack.");
            return;
        }

        URL jarUrl = ThirstBar.class.getProtectionDomain().getCodeSource().getLocation();
        if (jarUrl == null || !jarUrl.getFile().endsWith(".jar")) {
            return; // Not running from a jar file (e.g. testing)
        }

        String sourceFolder = "ItemsAdder/";

        File destDir = new File("plugins/ItemsAdder", ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_AUTO_DEPLOY_PATH);
        File cacheDir = new File(ThirstBar.getInstance().getDataFolder(), ".itemsadder_cache");
        boolean changed = false;

        try {
            Set<String> deployedFiles = new HashSet<>();
            File pluginJar = new File(jarUrl.toURI());
            try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(pluginJar)) {
                java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (name.startsWith(sourceFolder) && !entry.isDirectory()) {
                        String relativePath = name.substring(sourceFolder.length());
                        deployedFiles.add(relativePath);

                        File targetFile = new File(destDir, relativePath);
                        File cacheFile = new File(cacheDir, relativePath);

                        targetFile.getParentFile().mkdirs();
                        cacheFile.getParentFile().mkdirs();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        try (java.io.InputStream is = jarFile.getInputStream(entry)) {
                            while ((len = is.read(buffer)) > 0) {
                                baos.write(buffer, 0, len);
                            }
                        }
                        byte[] jarBytes = baos.toByteArray();

                        if (name.endsWith(".yml")) {
                            String content = new String(jarBytes, java.nio.charset.StandardCharsets.UTF_8);
                            content = content.replaceAll("y_position:\\s*-?\\d+", "y_position: " + ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_Y_POSITION)
                                             .replaceAll("scale_ratio:\\s*-?\\d+", "scale_ratio: " + ConfigData.CUSTOM_ACTION_BAR_ITEMSADDER_SCALE_RATIO);
                            jarBytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        }

                        boolean needCopy = true;
                        // Chỉ so sánh với file cache trong plugin của mình, bỏ qua file bên ItemsAdder
                        // vì panel có thể tự sửa file đó
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

            if (cleanObsoleteFiles(destDir, deployedFiles))
                changed = true;
            if (cleanObsoleteFiles(cacheDir, deployedFiles))
                changed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (changed) {
            Bukkit.getConsoleSender().sendMessage(
                    "§b[ThirstBar] §aItemsAdder files have been updated! Triggering iareload (iazip will follow when ready)...");
            pendingZip = true;
            Bukkit.getScheduler().runTask(ThirstBar.getInstance(), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iareload");
            });
        } else {
            Bukkit.getConsoleSender()
                    .sendMessage("§b[ThirstBar] §fItemsAdder files are up to date! Skipping iareload.");
        }
    }

    private static boolean cleanObsoleteFiles(File dir, Set<String> keepFiles) {
        if (!dir.exists())
            return false;
        boolean changed = false;
        try {
            java.util.List<File> toDelete = new java.util.ArrayList<>();
            Files.walk(dir.toPath())
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String relative = dir.toPath().relativize(path).toString().replace('\\', '/');
                        if (!keepFiles.contains(relative)) {
                            toDelete.add(path.toFile());
                        }
                    });
            for (File f : toDelete) {
                f.delete();
                changed = true;
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return changed;
    }
}
