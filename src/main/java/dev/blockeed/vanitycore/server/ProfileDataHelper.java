package dev.blockeed.vanitycore.server;

import dev.blockeed.vanitycore.VanityCoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ProfileDataHelper implements Listener {

    private VanityCoreAPI coreAPI;
    private Runnable runnable;

    public ProfileDataHelper(VanityCoreAPI coreAPI, JavaPlugin plugin, Runnable runnable) {
        this.coreAPI=coreAPI;
        this.runnable=runnable;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        coreAPI.getProfileManager().loadProfile(player.getUniqueId(), player.getName(), runnable);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        coreAPI.getProfileManager().getProfile(event.getPlayer().getUniqueId()).save(coreAPI.getDatabaseManager(), () -> {});
    }

}
