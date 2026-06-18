package me.malheen.metel;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.HashMap;
import java.util.Map;

public class BiomeManager implements Listener {
    private final Metel plugin;
    private final Map<Biome, Biome> biomeMap = new HashMap<>();
    private final Map<World, Boolean> processedWorlds = new HashMap<>();

    public BiomeManager(Metel plugin) {
        this.plugin = plugin;
        loadBiomeMappings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadBiomeMappings() {
        biomeMap.clear();

        try {
            // ВСЕ биомы верхнего мира -> зимние аналоги
            biomeMap.put(Biome.PLAINS, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.SUNFLOWER_PLAINS, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.FOREST, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.FLOWER_FOREST, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.BIRCH_FOREST, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.OLD_GROWTH_BIRCH_FOREST, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.DARK_FOREST, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.TAIGA, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.OLD_GROWTH_PINE_TAIGA, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.SWAMP, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.MANGROVE_SWAMP, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.JUNGLE, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.SPARSE_JUNGLE, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.BAMBOO_JUNGLE, Biome.SNOWY_TAIGA);
            biomeMap.put(Biome.SAVANNA, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.SAVANNA_PLATEAU, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.WINDSWEPT_SAVANNA, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.DESERT, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.BADLANDS, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.ERODED_BADLANDS, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.WOODED_BADLANDS, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.BEACH, Biome.SNOWY_BEACH);
            biomeMap.put(Biome.STONY_SHORE, Biome.SNOWY_BEACH);
            biomeMap.put(Biome.RIVER, Biome.FROZEN_RIVER);
            biomeMap.put(Biome.OCEAN, Biome.FROZEN_OCEAN);
            biomeMap.put(Biome.DEEP_OCEAN, Biome.DEEP_FROZEN_OCEAN);
            biomeMap.put(Biome.LUKEWARM_OCEAN, Biome.FROZEN_OCEAN);
            biomeMap.put(Biome.WARM_OCEAN, Biome.FROZEN_OCEAN);
            biomeMap.put(Biome.COLD_OCEAN, Biome.FROZEN_OCEAN);
            biomeMap.put(Biome.DEEP_LUKEWARM_OCEAN, Biome.DEEP_FROZEN_OCEAN);
            biomeMap.put(Biome.DEEP_COLD_OCEAN, Biome.DEEP_FROZEN_OCEAN);
            biomeMap.put(Biome.MUSHROOM_FIELDS, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.DRIPSTONE_CAVES, Biome.SNOWY_SLOPES);
            biomeMap.put(Biome.LUSH_CAVES, Biome.SNOWY_SLOPES);
            biomeMap.put(Biome.DEEP_DARK, Biome.SNOWY_SLOPES);
            biomeMap.put(Biome.MEADOW, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.CHERRY_GROVE, Biome.SNOWY_PLAINS);
            biomeMap.put(Biome.WINDSWEPT_HILLS, Biome.SNOWY_SLOPES);
            biomeMap.put(Biome.WINDSWEPT_GRAVELLY_HILLS, Biome.SNOWY_SLOPES);
            biomeMap.put(Biome.WINDSWEPT_FOREST, Biome.SNOWY_SLOPES);
            biomeMap.put(Biome.STONY_PEAKS, Biome.FROZEN_PEAKS);

        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при загрузке маппинга биомов: " + e.getMessage());
        }
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if (plugin.getPluginConfig().isWinterWorldEnabled()) {
            makeWorldWintery(event.getWorld());
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (plugin.getPluginConfig().isWinterWorldEnabled()) {
            makeWorldWintery(event.getWorld());
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // Обрабатываем каждый загружаемый чанк
        if (plugin.getPluginConfig().isWinterWorldEnabled() &&
                event.getWorld().getEnvironment() == World.Environment.NORMAL) {
            processChunk(event.getChunk());
        }
    }

    public void makeWorldWintery(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (processedWorlds.containsKey(world)) {
            return;
        }

        processedWorlds.put(world, true);
        plugin.getLogger().info("Преобразование верхнего мира " + world.getName() + " в зимний...");

        setWorldSnowWeather(world);

        // Обрабатываем все уже загруженные чанки
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            processAllLoadedChunks(world);
        }, 100L);
    }

    private void setWorldSnowWeather(World world) {
        if (world.getEnvironment() == World.Environment.NORMAL) {
            world.setStorm(true);
            world.setThundering(false);
            world.setWeatherDuration(Integer.MAX_VALUE);
        }
    }

    private void processAllLoadedChunks(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        int chunkCount = 0;
        int biomeCount = 0;

        org.bukkit.Chunk[] loadedChunks = world.getLoadedChunks();
        chunkCount = loadedChunks.length;

        for (org.bukkit.Chunk chunk : loadedChunks) {
            biomeCount += processChunk(chunk);
        }

        plugin.getLogger().info("Обработано чанков: " + chunkCount + ", изменено биомов: " + biomeCount);

        // Запускаем мониторинг новых чанков
        startChunkMonitor(world);
    }

    public int processChunk(org.bukkit.Chunk chunk) {
        int biomeCount = 0;
        World world = chunk.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            return 0;
        }

        int minY = Math.max(world.getMinHeight(), -64);
        int maxY = Math.min(world.getMaxHeight(), 320);

        // Быстрая обработка чанка - каждый 4й блок
        for (int x = 0; x < 16; x += 2) {
            for (int z = 0; z < 16; z += 2) {
                for (int y = minY; y < maxY; y += 4) {
                    try {
                        org.bukkit.block.Block block = chunk.getBlock(x, y, z);
                        Biome originalBiome = block.getBiome();

                        if (biomeMap.containsKey(originalBiome)) {
                            Biome winterBiome = biomeMap.get(originalBiome);
                            block.setBiome(winterBiome);
                            biomeCount++;
                        }
                    } catch (Exception e) {
                        // Игнорируем ошибки
                    }
                }
            }
        }

        return biomeCount;
    }

    private void startChunkMonitor(World world) {
        // Постоянно обрабатываем новые чанки
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (world.getEnvironment() != World.Environment.NORMAL) return;

            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                processChunk(chunk);
            }
        }, 100L, 200L); // Каждые 10 секунд
    }

    public void refreshWorldBiomes(World world) {
        if (world.getEnvironment() == World.Environment.NORMAL) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                processAllLoadedChunks(world);
            });
        }
    }
}