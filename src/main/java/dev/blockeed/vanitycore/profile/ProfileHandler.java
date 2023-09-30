package dev.blockeed.vanitycore.profile;

import dev.blockeed.vanitycore.VanityCoreAPI;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class ProfileHandler implements Listener {

    private VanityCoreAPI coreAPI;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        coreAPI.getProfileManager().loadProfile(player.getUniqueId(), player.getName(), () -> {});
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        coreAPI.getProfileManager().getProfile(event.getPlayer().getUniqueId()).save(coreAPI.getDatabaseManager(), () -> {});
    }

}
