package me.malheen.metel;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class FreezeManager {
    private final Metel plugin;
    private final Map<UUID, Long> lastDamageTime = new HashMap<>();
    private final Map<UUID, Long> lastArmorDamageTime = new HashMap<>();
    private final Set<Material> protectiveArmor = EnumSet.of(
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS
    );

    public FreezeManager(Metel plugin) {
        this.plugin = plugin;
    }

    public void checkPlayerFreeze(Player player) {
        Config config = plugin.getPluginConfig();

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (!config.isFreezingEnabled() || player.hasPermission("metel.bypass")) {
            return;
        }

        if (!isPlayerOutdoors(player)) {
            return;
        }

        if (isPlayerWarm(player, config)) {
            if (player.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
            }
            lastDamageTime.remove(player.getUniqueId());
            return;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2, false, false, true));

        long currentTime = System.currentTimeMillis();
        long lastTime = lastDamageTime.getOrDefault(player.getUniqueId(), 0L);

        if (currentTime - lastTime >= config.getDamageInterval() * 50) {
            player.damage(config.getDamageAmount());
            player.sendMessage("§bВы замерзаете! Найдите источник тепла!");
            lastDamageTime.put(player.getUniqueId(), currentTime);
        }
    }

    private boolean isPlayerOutdoors(Player player) {
        // Получаем уровень "небесного" света.
        // На открытом поле = 15. Под одним блоком = ~14.
        // В полностью закрытом помещении или пещере = 0.
        // Если свет > 8, значит игрок явно недостаточно укрыт со всех сторон.
        Block block = player.getLocation().getBlock();
        return block.getLightFromSky() > 8;
    }

    private boolean isPlayerWarm(Player player, Config config) {
        Location playerLoc = player.getLocation();
        Map<Material, Integer> warmBlocks = config.getWarmBlocks();

        int checkRadius = 15;

        for (int x = -checkRadius; x <= checkRadius; x += 2) {
            for (int y = -checkRadius; y <= checkRadius; y += 2) {
                for (int z = -checkRadius; z <= checkRadius; z += 2) {
                    Location checkLoc = playerLoc.clone().add(x, y, z);
                    Material blockType = checkLoc.getBlock().getType();

                    if (warmBlocks.containsKey(blockType)) {
                        int requiredRadius = warmBlocks.get(blockType);
                        if (playerLoc.distance(checkLoc) <= requiredRadius) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public void damageLeatherArmor(Player player) {
        Config config = plugin.getPluginConfig();

        long currentTime = System.currentTimeMillis();
        long lastTime = lastArmorDamageTime.getOrDefault(player.getUniqueId(), 0L);

        if (currentTime - lastTime < config.getArmorDamageInterval() * 50) {
            return;
        }

        PlayerInventory inventory = player.getInventory();

        damageArmorPiece(inventory.getHelmet());
        damageArmorPiece(inventory.getChestplate());
        damageArmorPiece(inventory.getLeggings());
        damageArmorPiece(inventory.getBoots());

        lastArmorDamageTime.put(player.getUniqueId(), currentTime);
    }

    private void damageArmorPiece(ItemStack armor) {
        if (armor == null || !protectiveArmor.contains(armor.getType())) {
            return;
        }

        ItemMeta meta = armor.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            int currentDamage = damageable.getDamage();
            int maxDurability = armor.getType().getMaxDurability();

            if (currentDamage < maxDurability) {
                damageable.setDamage(currentDamage + new Random().nextInt(2) + 1);
                armor.setItemMeta((ItemMeta) damageable);
            }
        }
    }
}