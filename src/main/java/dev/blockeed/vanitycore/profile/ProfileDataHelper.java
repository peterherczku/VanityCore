package dev.blockeed.vanitycore.profile;

import dev.blockeed.vanitycore.VanityCoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import org.bukkit.plugin.java.JavaPlugin;

public class ProfileDataHelper implements Listener {

    public static void registerHandler(VanityCoreAPI coreAPI, JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new ProfileHandler(coreAPI, plugin), plugin);
    }

}
