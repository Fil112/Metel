package me.malheen.metel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MetelCommand implements CommandExecutor, TabCompleter {
    private final Metel plugin;

    public MetelCommand(Metel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "=== Плагин Metel ===");
            sender.sendMessage(ChatColor.YELLOW + "Автор: me.malheen");
            sender.sendMessage(ChatColor.YELLOW + "Версия: 1.0");
            sender.sendMessage(ChatColor.YELLOW + "Используйте: /metel reload - перезагрузить конфиг");
            sender.sendMessage(ChatColor.YELLOW + "Используйте: /metel winterize - сделать мир зимним");
            sender.sendMessage(ChatColor.YELLOW + "Используйте: /metel refresh - обновить биомы");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("metel.command")) {
                sender.sendMessage(ChatColor.RED + "Недостаточно прав!");
                return true;
            }

            plugin.getPluginConfig().reload();
            sender.sendMessage(ChatColor.GREEN + "Конфигурация Metel перезагружена!");
            return true;
        }

        if (args[0].equalsIgnoreCase("winterize")) {
            if (!sender.hasPermission("metel.winterize")) {
                sender.sendMessage(ChatColor.RED + "Недостаточно прав!");
                return true;
            }

            World targetWorld = null;

            if (sender instanceof Player) {
                targetWorld = ((Player) sender).getWorld();
            } else if (args.length > 1) {
                targetWorld = Bukkit.getWorld(args[1]);
            }

            if (targetWorld == null) {
                for (World world : Bukkit.getWorlds()) {
                    plugin.getBiomeManager().makeWorldWintery(world);
                }
                sender.sendMessage(ChatColor.GREEN + "Все миры преобразованы в зимние!");
            } else {
                plugin.getBiomeManager().makeWorldWintery(targetWorld);
                sender.sendMessage(ChatColor.GREEN + "Мир " + targetWorld.getName() + " преобразован в зимний!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("refresh")) {
            if (!sender.hasPermission("metel.winterize")) {
                sender.sendMessage(ChatColor.RED + "Недостаточно прав!");
                return true;
            }

            World targetWorld = null;

            if (sender instanceof Player) {
                targetWorld = ((Player) sender).getWorld();
            } else if (args.length > 1) {
                targetWorld = Bukkit.getWorld(args[1]);
            }

            if (targetWorld == null) {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() == World.Environment.NORMAL) {
                        plugin.getBiomeManager().refreshWorldBiomes(world);
                    }
                }
                sender.sendMessage(ChatColor.GREEN + "Биомы во всех верхних мирах обновлены!");
            } else {
                if (targetWorld.getEnvironment() == World.Environment.NORMAL) {
                    plugin.getBiomeManager().refreshWorldBiomes(targetWorld);
                    sender.sendMessage(ChatColor.GREEN + "Биомы в мире " + targetWorld.getName() + " обновлены!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Можно обновлять только верхние миры!");
                }
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Неизвестная команда. Используйте /metel для справки.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("metel.command")) {
                completions.add("reload");
            }
            if ("winterize".startsWith(args[0].toLowerCase()) && sender.hasPermission("metel.winterize")) {
                completions.add("winterize");
            }
            if ("refresh".startsWith(args[0].toLowerCase()) && sender.hasPermission("metel.winterize")) {
                completions.add("refresh");
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("winterize") || args[0].equalsIgnoreCase("refresh"))) {
            for (World world : Bukkit.getWorlds()) {
                if (world.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(world.getName());
                }
            }
        }

        return completions;
    }
}