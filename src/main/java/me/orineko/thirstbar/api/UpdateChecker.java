package me.orineko.thirstbar.api;


import me.orineko.thirstbar.ThirstBar;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    private final int resourceId;

    public UpdateChecker(int resourceId) {
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(ThirstBar.getInstance(), () -> {
            try (InputStream inputStream = java.net.URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).toURL().openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                Bukkit.getConsoleSender().sendMessage("§c[ThirstBar]Unable to check for updates: " + exception.getMessage());
            }
        });
    }
}
