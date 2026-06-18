package me.malheen.metel;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.WeatherType;

public final class Metel extends JavaPlugin {
    private Config config;
    private BukkitTask environmentTask;
    private BiomeManager biomeManager;
    private VisibilityManager visibilityManager;
    private FreezeManager freezeManager;
    private CommunicationManager communicationManager;
    private DeathMessageBlocker deathMessageBlocker;
    private MetelCommand metelCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = new Config(this);

        // Инициализация менеджеров
        this.freezeManager = new FreezeManager(this);
        this.biomeManager = new BiomeManager(this);
        this.visibilityManager = new VisibilityManager(this);
        this.communicationManager = new CommunicationManager(this);
        this.deathMessageBlocker = new DeathMessageBlocker();
        this.metelCommand = new MetelCommand(this);

        // Регистрация всех слушателей
        getServer().getPluginManager().registerEvents(new me.malheen.metel.listeners.PlayerEnvironmentListener(this), this);
        getServer().getPluginManager().registerEvents(new me.malheen.metel.listeners.PlayerVisibilityListener(this, visibilityManager), this);
        getServer().getPluginManager().registerEvents(communicationManager, this);
        getServer().getPluginManager().registerEvents(deathMessageBlocker, this);

        // Регистрация команд
        getCommand("metel").setExecutor(metelCommand);
        getCommand("metel").setTabCompleter(metelCommand);

        // Запуск задач
        startEnvironmentTask();

        getLogger().info("Плагин Metel включен! Автор: me.malheen");
        getLogger().info("Кастомные блоки: тепло-генератор (100 блоков), ледяной камень (20 блоков)");

        if (config.isWinterWorldEnabled()) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() == World.Environment.NORMAL) {
                        biomeManager.makeWorldWintery(world);
                        setWorldSnowWeather(world);
                    }
                }
            }, 100L);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                forcePlayerSettings(player);
            }
        }, 20L);
    }

    @Override
    public void onDisable() {
        if (environmentTask != null) {
            environmentTask.cancel();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            restorePlayerSettings(player);
        }

        getLogger().info("Плагин Metel выключен.");
    }

    private void startEnvironmentTask() {
        environmentTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("metel.bypass")) continue;

                freezeManager.checkPlayerFreeze(player);
                freezeManager.damageLeatherArmor(player);
                applyWeatherEffects(player);
                visibilityManager.applyFogEffects(player);
            }

            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() == World.Environment.NORMAL) {
                    setWorldSnowWeather(world);
                }
            }
        }, 0L, 20L);
    }

    private void applyWeatherEffects(Player player) {
        if (player.getWorld().getEnvironment() == World.Environment.NORMAL) {
            player.setPlayerWeather(WeatherType.DOWNFALL);
        } else {
            player.resetPlayerWeather();
        }
    }

    private void setWorldSnowWeather(World world) {
        if (world.getEnvironment() == World.Environment.NORMAL) {
            world.setStorm(true);
            world.setThundering(false);
            world.setWeatherDuration(Integer.MAX_VALUE);
        }
    }

    public void forcePlayerSettings(Player player) {
        visibilityManager.forceViewDistance(player);
        visibilityManager.applyFogEffects(player);
        visibilityManager.hideNameTags(player);
    }

    private void restorePlayerSettings(Player player) {
        try {
            visibilityManager.restoreNormalView(player);
            player.resetPlayerWeather();
            visibilityManager.showNameTags(player);
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    public Config getPluginConfig() {
        return config;
    }

    public BiomeManager getBiomeManager() {
        return biomeManager;
    }

    public VisibilityManager getVisibilityManager() {
        return visibilityManager;
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public CommunicationManager getCommunicationManager() {
        return communicationManager;
    }
}