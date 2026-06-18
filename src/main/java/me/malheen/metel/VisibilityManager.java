package me.malheen.metel;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class VisibilityManager {
    private final Metel plugin;
    private Team hiddenTeam;

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
                applyFogEffects(player);
                forceViewDistance(player);
            }
        }, 0L, 20L);
    }

    public void updatePlayerVisibility(Player player) {
        try {
            int maxDistance = plugin.getPluginConfig().getMaxViewDistance() * 16;
            World playerWorld = player.getWorld();

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;

                // Проверяем, что игроки в одном мире
                if (!other.getWorld().equals(playerWorld)) {
                    // Игроки в разных мирах - скрываем друг друга
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

                    if (distance <= maxDistance && player.hasLineOfSight(other)) {
                        // Показываем игрока
                        if (!player.canSee(other)) {
                            player.showPlayer(plugin, other);
                        }
                        if (hiddenTeam != null) {
                            hiddenTeam.removeEntry(other.getName());
                        }
                    } else {
                        // Скрываем игрока
                        if (player.canSee(other)) {
                            player.hidePlayer(plugin, other);
                        }
                        if (hiddenTeam != null) {
                            hiddenTeam.addEntry(other.getName());
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Игнорируем ошибки измерения расстояния
                    // Просто скрываем игрока при ошибке
                    if (player.canSee(other)) {
                        player.hidePlayer(plugin, other);
                    }
                    if (hiddenTeam != null) {
                        hiddenTeam.addEntry(other.getName());
                    }
                }
            }

        } catch (Exception e) {
            // Игнорируем все ошибки без вывода в консоль
        }
    }

    public void applyFogEffects(Player player) {
        World world = player.getWorld();

        if (world.getEnvironment() == World.Environment.NETHER ||
                world.getEnvironment() == World.Environment.THE_END) {
            restoreNormalView(player);
            return;
        }

        if (plugin.getPluginConfig().shouldAlwaysFog()) {
            try {
                int fogDistance = plugin.getPluginConfig().getFogDistance();
                player.setViewDistance(Math.min(fogDistance, 32));
            } catch (Exception e) {
                // Игнорируем ошибку
            }
        }
    }

    public void forceViewDistance(Player player) {
        try {
            player.setViewDistance(7);
        } catch (Exception e) {
            // Игнорируем ошибку
        }
    }

    public void hideNameTags(Player player) {
        if (plugin.getPluginConfig().shouldHideNametags() && hiddenTeam != null) {
            try {
                hiddenTeam.addEntry(player.getName());
            } catch (Exception e) {
                // Игнорируем ошибку
            }
        }
    }

    public void showNameTags(Player player) {
        if (hiddenTeam != null) {
            try {
                hiddenTeam.removeEntry(player.getName());
            } catch (Exception e) {
                // Игнорируем ошибку
            }
        }
    }

    public void applyPlayerSettings(Player player) {
        forceViewDistance(player);
        applyFogEffects(player);
        hideNameTags(player);
    }

    public void restoreNormalView(Player player) {
        try {
            player.setViewDistance(10);
        } catch (Exception e) {
            // Игнорируем ошибку
        }
    }
}