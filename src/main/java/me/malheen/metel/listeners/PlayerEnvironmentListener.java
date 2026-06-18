package me.malheen.metel.listeners;

import me.malheen.metel.Metel;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerEnvironmentListener implements Listener {
    private final Metel plugin;

    public PlayerEnvironmentListener(Metel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        if (!plugin.getPluginConfig().shouldFreezeWater()) {
            return;
        }

        Material newType = event.getNewState().getType();
        if (newType == Material.ICE) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.COMPASS) {
            if (plugin.getPluginConfig().shouldHideCoordinates()) {
                String coords = String.format("§eКоординаты: §7%d, %d, %d",
                        (int)player.getLocation().getX(),
                        (int)player.getLocation().getY(),
                        (int)player.getLocation().getZ());

                player.sendActionBar(coords);
            }
        }
    }
}