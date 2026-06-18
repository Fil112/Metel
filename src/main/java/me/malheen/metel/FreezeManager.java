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

        if (!isPlayerOutdoors(player, config)) {
            return;
        }

        // Проверяем, согрет ли игрок блоками или полным комплектом кожаной брони
        boolean warmByBlocks = isPlayerWarm(player, config);
        boolean warmByArmor = hasFullProtectiveArmor(player);

        if (warmByBlocks || warmByArmor) {
            if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                player.removePotionEffect(PotionEffectType.SLOW);
            }
            lastDamageTime.remove(player.getUniqueId());

            // Если игрок греется только за счет брони (рядом нет теплых блоков), она изнашивается
            if (!warmByBlocks && warmByArmor) {
                damageLeatherArmor(player, config);
            }
            return;
        }

        // Если защиты нет — накладываем замедление и наносим урон со временем
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2, false, false, true));

        long currentTime = System.currentTimeMillis();
        long lastTime = lastDamageTime.getOrDefault(player.getUniqueId(), 0L);

        if (currentTime - lastTime >= config.getDamageInterval() * 50) {
            player.damage(config.getDamageAmount());
            player.sendMessage("§bВы замерзаете! Найдите источник тепла или наденьте кожаную одежду!");
            lastDamageTime.put(player.getUniqueId(), currentTime);
        }
    }

    private boolean isPlayerOutdoors(Player player, Config config) {
        Block block = player.getLocation().getBlock();
        // Используем динамическую настройку minLightLevel вместо хардкода
        return block.getLightFromSky() > config.getMinLightLevel();
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

    // Проверка на то, одет ли на игрока полный сет кожаной брони
    private boolean hasFullProtectiveArmor(Player player) {
        PlayerInventory inventory = player.getInventory();
        return isProtectivePiece(inventory.getHelmet()) &&
                isProtectivePiece(inventory.getChestplate()) &&
                isProtectivePiece(inventory.getLeggings()) &&
                isProtectivePiece(inventory.getBoots());
    }

    private boolean isProtectivePiece(ItemStack item) {
        return item != null && protectiveArmor.contains(item.getType());
    }

    public void damageLeatherArmor(Player player, Config config) {
        long currentTime = System.currentTimeMillis();
        long lastTime = lastArmorDamageTime.getOrDefault(player.getUniqueId(), 0L);

        if (currentTime - lastTime < config.getArmorDamageInterval() * 50) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        int damageAmount = config.getArmorDamageAmount();

        damageArmorPiece(inventory.getHelmet(), damageAmount);
        damageArmorPiece(inventory.getChestplate(), damageAmount);
        damageArmorPiece(inventory.getLeggings(), damageAmount);
        damageArmorPiece(inventory.getBoots(), damageAmount);

        lastArmorDamageTime.put(player.getUniqueId(), currentTime);
    }

    private void damageArmorPiece(ItemStack armor, int damageAmount) {
        if (armor == null || !protectiveArmor.contains(armor.getType())) {
            return;
        }

        ItemMeta meta = armor.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            int currentDamage = damageable.getDamage();
            int maxDurability = armor.getType().getMaxDurability();

            if (currentDamage < maxDurability) {
                // Наносим урон из конфига (с ограничением, чтобы не уйти в минус по прочности)
                damageable.setDamage(Math.min(maxDurability, currentDamage + damageAmount));
                armor.setItemMeta((ItemMeta) damageable);
            }
        }
    }
}