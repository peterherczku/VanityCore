package dev.blockeed.vanitycore.profile;

import dev.blockeed.vanitycore.VanityCoreAPI;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class ProfileHandler implements Listener {

    private VanityCoreAPI coreAPI;
    private JavaPlugin plugin;

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            coreAPI.getProfileManager().handleProfileCreation(event.getUniqueId());
        } catch (NullPointerException exception) {
            System.out.println(exception);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "§cERROR: Could not create profile");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ProfileData profileData = coreAPI.getProfileManager().getProfile(player.getUniqueId());
        if (profileData==null) {
            player.kickPlayer(ChatColor.RED+"ERROR: "+ChatColor.WHITE+"Profile returned null.");
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                profileData.load(plugin, coreAPI.getDatabaseManager());
            }
        }, 20);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        coreAPI.getProfileManager().getProfile(event.getPlayer().getUniqueId()).save(coreAPI.getDatabaseManager(), () -> {});
    }

}
