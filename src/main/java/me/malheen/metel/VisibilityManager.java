package me.malheen.metel;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VisibilityManager {
    private final Metel plugin;
    private Team hiddenTeam;

    // Кэш для предотвращения бесконечной перезагрузки чанков
    private final Map<UUID, Integer> activeViewDistances = new HashMap<>();

    public VisibilityManager(Metel plugin) {
        this.plugin = plugin;
        setupScoreboardTeams();
        startVisibilityTask();
    }

    private void setupScoreboardTeams() {
        try {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            hiddenTeam = scoreboard.getTeam("MetelHidden");

            if (hiddenTeam == null) {
                hiddenTeam = scoreboard.registerNewTeam("MetelHidden");
            }

            hiddenTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        } catch (Exception e) {
            // Игнорируем ошибку
        }
    }

    private void startVisibilityTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerVisibility(player);
                // Убрали вызовы изменения прорисовки отсюда,
                // теперь они будут безопасно обрабатываться через smartUpdateViewDistance
                smartUpdateViewDistance(player);
            }
        }, 0L, 20L);
    }

    public void updatePlayerVisibility(Player player) {
        try {
            int maxDistance = plugin.getPluginConfig().getMaxViewDistance() * 16;
            World playerWorld = player.getWorld();

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;

                if (!other.getWorld().equals(playerWorld)) {
                    if (player.canSee(other)) {
                        player.hidePlayer(plugin, other);
                    }
                    if (hiddenTeam != null) {
                        hiddenTeam.addEntry(other.getName());
                    }
                    continue;
                }

                try {
                    double distance = player.getLocation().distance(other.getLocation());

                    // Убрали тяжелый player.hasLineOfSight(other), оставили только дистанцию
                    if (distance <= maxDistance) {
                        if (!player.canSee(other)) {
                            player.showPlayer(plugin, other);
                        }
                        if (hiddenTeam != null) {
                            hiddenTeam.removeEntry(other.getName());
                        }
                    } else {
                        if (player.canSee(other)) {
                            player.hidePlayer(plugin, other);
                        }
                        if (hiddenTeam != null) {
                            hiddenTeam.addEntry(other.getName());
                        }
                    }
                } catch (IllegalArgumentException e) {
                    if (player.canSee(other)) {
                        player.hidePlayer(plugin, other);
                    }
                    if (hiddenTeam != null) {
                        hiddenTeam.addEntry(other.getName());
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем
        }
    }

    /**
     * Умный метод обновления прорисовки.
     * Защищает сервер от бесконечной отправки пакетов чанков.
     */
    private void smartUpdateViewDistance(Player player) {
        World world = player.getWorld();
        int targetDistance;

        if (world.getEnvironment() == World.Environment.NORMAL && plugin.getPluginConfig().shouldAlwaysFog()) {
            targetDistance = plugin.getPluginConfig().getFogDistance();
        } else {
            targetDistance = plugin.getPluginConfig().getMaxViewDistance();
        }

        // Защита от выхода за лимиты Bukkit (от 2 до 32 чанков)
        targetDistance = Math.max(2, Math.min(targetDistance, 32));

        // Отправляем запрос ядру ТОЛЬКО если дальность изменилась
        if (activeViewDistances.getOrDefault(player.getUniqueId(), -1) != targetDistance) {
            try {
                player.setViewDistance(targetDistance);
                activeViewDistances.put(player.getUniqueId(), targetDistance);
            } catch (Exception e) {
                // Игнорируем
            }
        }
    }

    // Эти методы оставлены для совместимости с Metel.java,
    // но теперь они безопасно перенаправляют логику в умный метод.
    public void applyFogEffects(Player player) {
        smartUpdateViewDistance(player);
    }

    public void forceViewDistance(Player player) {
        smartUpdateViewDistance(player);
    }

    public void hideNameTags(Player player) {
        if (plugin.getPluginConfig().shouldHideNametags() && hiddenTeam != null) {
            try {
                hiddenTeam.addEntry(player.getName());
            } catch (Exception e) {}
        }
    }

    public void showNameTags(Player player) {
        if (hiddenTeam != null) {
            try {
                hiddenTeam.removeEntry(player.getName());
            } catch (Exception e) {}
        }
    }

    public void applyPlayerSettings(Player player) {
        smartUpdateViewDistance(player);
        hideNameTags(player);
    }

    public void restoreNormalView(Player player) {
        try {
            int defaultDistance = 10;
            player.setViewDistance(defaultDistance);
            activeViewDistances.remove(player.getUniqueId());
        } catch (Exception e) {}
    }
}