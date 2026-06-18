package me.malheen.metel;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import java.util.*;

public class Config {
    private final Metel plugin;
    private final FileConfiguration config;

    // Основные настройки
    private int maxViewDistance;
    private boolean hideNametags;
    private boolean hideCoordinates;
    private int fogDistance;
    private boolean alwaysFog;
    private boolean alwaysSnow;
    private boolean noRain;
    private boolean freezingEnabled;
    private int damageInterval;
    private double damageAmount;
    private Map<Material, Integer> warmBlocks = new HashMap<>();
    private int minLightLevel;
    private double chatMaxDistance;
    private boolean winterWorldEnabled;
    private boolean freezeWater;
    private int armorDamageInterval;
    private int armorDamageAmount;

    // Настройки кастомных блоков
    private int heatGeneratorRange;
    private int iceStoneRange;
    private int heatMeltRange;
    private int iceFreezeRange;
    private double iceDamage;
    private boolean customBlockEffectsEnabled;
    private boolean customBlockParticlesEnabled;

    public Config(Metel plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();

        // Основные настройки
        maxViewDistance = config.getInt("visibility.max-view-distance", 7);
        hideNametags = config.getBoolean("visibility.hide-nametags", true);
        hideCoordinates = config.getBoolean("visibility.hide-coordinates", true);
        fogDistance = config.getInt("visibility.fog-distance", 2);
        alwaysFog = config.getBoolean("visibility.always-fog", true);
        alwaysSnow = config.getBoolean("weather.always-snow", true);
        noRain = config.getBoolean("weather.no-rain", true);
        freezingEnabled = config.getBoolean("freezing.enabled", true);
        damageInterval = config.getInt("freezing.damage-interval-ticks", 100);
        damageAmount = config.getDouble("freezing.damage-amount", 1.0);
        minLightLevel = config.getInt("freezing.min-light-level", 10);
        chatMaxDistance = config.getDouble("chat.max-distance", 25.0);
        winterWorldEnabled = config.getBoolean("winter-world.enabled", true);
        freezeWater = config.getBoolean("winter-world.freeze-water", true);
        armorDamageInterval = config.getInt("freezing.armor-damage-interval", 12000);
        armorDamageAmount = config.getInt("freezing.armor-damage-amount", 1);

        // Загрузка теплых блоков
        warmBlocks.clear();
        if (config.contains("freezing.warm-blocks")) {
            for (String key : config.getConfigurationSection("freezing.warm-blocks").getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    int radius = config.getInt("freezing.warm-blocks." + key);
                    warmBlocks.put(material, radius);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неизвестный материал: " + key);
                }
            }
        }

        // Настройки кастомных блоков
        heatGeneratorRange = config.getInt("custom-blocks.heat-generator.range", 500);
        iceStoneRange = config.getInt("custom-blocks.ice-stone.range", 50);
        heatMeltRange = config.getInt("custom-blocks.heat-generator.melt-range", 50);
        iceFreezeRange = config.getInt("custom-blocks.ice-stone.freeze-range", 25);
        iceDamage = config.getDouble("custom-blocks.ice-stone.damage", 2.0);
        customBlockEffectsEnabled = config.getBoolean("custom-blocks.enable-effects", true);
        customBlockParticlesEnabled = config.getBoolean("custom-blocks.enable-particles", true);

        plugin.getLogger().info("Загружено теплых блоков: " + warmBlocks.size());
        plugin.getLogger().info("Теплогенератор: радиус " + heatGeneratorRange + ", плавление " + heatMeltRange);
        plugin.getLogger().info("Ледяной камень: радиус " + iceStoneRange + ", заморозка " + iceFreezeRange);
    }

    // Геттеры основных настроек
    public int getMaxViewDistance() { return maxViewDistance; }
    public boolean shouldHideNametags() { return hideNametags; }
    public boolean shouldHideCoordinates() { return hideCoordinates; }
    public int getFogDistance() { return fogDistance; }
    public boolean shouldAlwaysFog() { return alwaysFog; }
    public boolean isAlwaysSnow() { return alwaysSnow; }
    public boolean isNoRain() { return noRain; }
    public boolean isFreezingEnabled() { return freezingEnabled; }
    public int getDamageInterval() { return damageInterval; }
    public double getDamageAmount() { return damageAmount; }
    public Map<Material, Integer> getWarmBlocks() { return warmBlocks; }
    public int getMinLightLevel() { return minLightLevel; }
    public double getChatMaxDistance() { return chatMaxDistance; }
    public boolean isWinterWorldEnabled() { return winterWorldEnabled; }
    public boolean shouldFreezeWater() { return freezeWater; }
    public int getArmorDamageInterval() { return armorDamageInterval; }
    public int getArmorDamageAmount() { return armorDamageAmount; }

    // Геттеры настроек кастомных блоков
    public int getHeatGeneratorRange() { return heatGeneratorRange; }
    public int getIceStoneRange() { return iceStoneRange; }
    public int getHeatMeltRange() { return heatMeltRange; }
    public int getIceFreezeRange() { return iceFreezeRange; }
    public double getIceDamage() { return iceDamage; }
    public boolean areCustomBlockEffectsEnabled() { return customBlockEffectsEnabled; }
    public boolean areCustomBlockParticlesEnabled() { return customBlockParticlesEnabled; }
}