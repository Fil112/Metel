package me.malheen.metel.listeners;

import me.malheen.metel.Metel;
import me.malheen.metel.VisibilityManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerVisibilityListener implements Listener {
    private final Metel plugin;
    private final VisibilityManager visibilityManager;

    public PlayerVisibilityListener(Metel plugin, VisibilityManager visibilityManager) {
        this.plugin = plugin;
        this.visibilityManager = visibilityManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        visibilityManager.hideNameTags(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.forcePlayerSettings(player);
            visibilityManager.updatePlayerVisibility(player);

            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.equals(player)) {
                    visibilityManager.updatePlayerVisibility(online);
                }
            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        visibilityManager.showNameTags(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.forcePlayerSettings(player);
            visibilityManager.updatePlayerVisibility(player);
        }, 10L);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.forcePlayerSettings(player);
            visibilityManager.updatePlayerVisibility(player);
        }, 10L);
    }
}