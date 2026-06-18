package me.malheen.metel;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BiomeManager implements Listener {
    private final Metel plugin;
    private final Map<Biome, Biome> biomeMap = new HashMap<>();
    private final Map<World, Boolean> processedWorlds = new HashMap<>();

    // Кэшируем методы высоты для оптимизации (чтобы рефлексия не тормозила сервер)
    private Method getMinHeightMethod;
    private Method getMaxHeightMethod;

    public BiomeManager(Metel plugin) {
        this.plugin = plugin;
        setupHeightMethods();
        loadBiomeMappings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void setupHeightMethods() {
        try {
            getMinHeightMethod = World.class.getMethod("getMinHeight");
            getMaxHeightMethod = World.class.getMethod("getMaxHeight");
        } catch (NoSuchMethodException e) {
            // Версия 1.16.5 или ниже, этих методов нет, оставляем null
        }
    }

    private int getWorldMinHeight(World world) {
        if (getMinHeightMethod != null) {
            try {
                return (int) getMinHeightMethod.invoke(world);
            } catch (Exception ignored) {}
        }
        return 0; // Стандартная минимальная высота для 1.16.5
    }

    private int getWorldMaxHeight(World world) {
        if (getMaxHeightMethod != null) {
            try {
                return (int) getMaxHeightMethod.invoke(world);
            } catch (Exception ignored) {}
        }
        return 256; // Стандартная максимальная высота для 1.16.5
    }

    private void loadBiomeMappings() {
        biomeMap.clear();

        // Базовые равнины
        addBiomeMapping("PLAINS", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("SUNFLOWER_PLAINS", "SNOWY_PLAINS", "SNOWY_TUNDRA");

        // Леса
        addBiomeMapping("FOREST", "SNOWY_TAIGA");
        addBiomeMapping("FLOWER_FOREST", "SNOWY_TAIGA");
        addBiomeMapping("BIRCH_FOREST", "SNOWY_TAIGA");
        addBiomeMapping("OLD_GROWTH_BIRCH_FOREST", "SNOWY_TAIGA", "TALL_BIRCH_FOREST");
        addBiomeMapping("DARK_FOREST", "SNOWY_TAIGA");
        addBiomeMapping("TAIGA", "SNOWY_TAIGA");
        addBiomeMapping("OLD_GROWTH_PINE_TAIGA", "SNOWY_TAIGA", "GIANT_TREE_TAIGA");
        addBiomeMapping("OLD_GROWTH_SPRUCE_TAIGA", "SNOWY_TAIGA", "GIANT_SPRUCE_TAIGA");

        // Болота и джунгли
        addBiomeMapping("SWAMP", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("MANGROVE_SWAMP", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("JUNGLE", "SNOWY_TAIGA");
        addBiomeMapping("SPARSE_JUNGLE", "SNOWY_TAIGA", "JUNGLE_EDGE");
        addBiomeMapping("BAMBOO_JUNGLE", "SNOWY_TAIGA");

        // Саванны и пустыни
        addBiomeMapping("SAVANNA", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("SAVANNA_PLATEAU", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("WINDSWEPT_SAVANNA", "SNOWY_PLAINS", "SHATTERED_SAVANNA");
        addBiomeMapping("DESERT", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("BADLANDS", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("ERODED_BADLANDS", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("WOODED_BADLANDS", "SNOWY_PLAINS", "WOODED_BADLANDS_PLATEAU");

        // Водоемы и пляжи
        addBiomeMapping("BEACH", "SNOWY_BEACH");
        addBiomeMapping("STONY_SHORE", "SNOWY_BEACH", "STONE_SHORE");
        addBiomeMapping("RIVER", "FROZEN_RIVER");
        addBiomeMapping("OCEAN", "FROZEN_OCEAN");
        addBiomeMapping("DEEP_OCEAN", "DEEP_FROZEN_OCEAN");
        addBiomeMapping("LUKEWARM_OCEAN", "FROZEN_OCEAN");
        addBiomeMapping("WARM_OCEAN", "FROZEN_OCEAN");
        addBiomeMapping("COLD_OCEAN", "FROZEN_OCEAN");
        addBiomeMapping("DEEP_LUKEWARM_OCEAN", "DEEP_FROZEN_OCEAN");
        addBiomeMapping("DEEP_COLD_OCEAN", "DEEP_FROZEN_OCEAN");

        // Особые биомы 1.18+
        addBiomeMapping("MUSHROOM_FIELDS", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("DRIPSTONE_CAVES", "SNOWY_SLOPES", "SNOWY_TUNDRA");
        addBiomeMapping("LUSH_CAVES", "SNOWY_SLOPES", "SNOWY_TUNDRA");
        addBiomeMapping("DEEP_DARK", "SNOWY_SLOPES", "SNOWY_TUNDRA");
        addBiomeMapping("MEADOW", "SNOWY_PLAINS", "SNOWY_TUNDRA");
        addBiomeMapping("CHERRY_GROVE", "SNOWY_PLAINS", "SNOWY_TUNDRA");

        // Горы
        addBiomeMapping("WINDSWEPT_HILLS", "SNOWY_SLOPES", "MOUNTAINS");
        addBiomeMapping("WINDSWEPT_GRAVELLY_HILLS", "SNOWY_SLOPES", "GRAVELLY_MOUNTAINS");
        addBiomeMapping("WINDSWEPT_FOREST", "SNOWY_SLOPES", "WOODED_MOUNTAINS");
        addBiomeMapping("STONY_PEAKS", "FROZEN_PEAKS", "SNOWY_TUNDRA");
    }

    /**
     * Безопасно добавляет маппинг биома. Если биома не существует в текущей версии сервера,
     * он просто игнорируется без вызова ошибок.
     * @param sourceName Исходный биом (например, CHERRY_GROVE)
     * @param targetNames Варианты зимнего биома (по приоритету, от новых версий к старым)
     */
    private void addBiomeMapping(String sourceName, String... targetNames) {
        try {
            Biome source = Biome.valueOf(sourceName);
            for (String targetName : targetNames) {
                try {
                    Biome target = Biome.valueOf(targetName);
                    biomeMap.put(source, target);
                    return; // Успешно добавили, выходим
                } catch (IllegalArgumentException ignored) {
                    // Зимнего биома с таким названием нет в этой версии, пробуем следующий вариант
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Исходного биома нет в этой версии, пропускаем (например, CHERRY_GROVE на 1.16)
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

        int chunkCount;
        int biomeCount = 0;

        org.bukkit.Chunk[] loadedChunks = world.getLoadedChunks();
        chunkCount = loadedChunks.length;

        for (org.bukkit.Chunk chunk : loadedChunks) {
            biomeCount += processChunk(chunk);
        }

        plugin.getLogger().info("Обработано чанков: " + chunkCount + ", изменено биомов: " + biomeCount);
        startChunkMonitor(world);
    }

    public int processChunk(org.bukkit.Chunk chunk) {
        int biomeCount = 0;
        World world = chunk.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            return 0;
        }

        int minY = Math.max(getWorldMinHeight(world), -64);
        int maxY = Math.min(getWorldMaxHeight(world), 320);

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
                    } catch (Exception ignored) {
                        // Игнорируем ошибки при получении/установке биома
                    }
                }
            }
        }

        return biomeCount;
    }

    private void startChunkMonitor(World world) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (world.getEnvironment() != World.Environment.NORMAL) return;

            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                processChunk(chunk);
            }
        }, 100L, 200L);
    }

    public void refreshWorldBiomes(World world) {
        if (world.getEnvironment() == World.Environment.NORMAL) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                processAllLoadedChunks(world);
            });
        }
    }
}