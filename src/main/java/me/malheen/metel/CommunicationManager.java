package me.malheen.metel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class CommunicationManager implements Listener {
    private final Metel plugin;

    public CommunicationManager(Metel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        double maxDistance = plugin.getPluginConfig().getChatMaxDistance();

        // Если расстояние 0 или меньше - чат не ограничен
        if (maxDistance <= 0) {
            return;
        }

        Player sender = event.getPlayer();

        // Очищаем стандартных получателей
        event.getRecipients().clear();

        // Добавляем отправителя
        event.getRecipients().add(sender);

        // Ищем других игроков в радиусе
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient.equals(sender)) continue;

            // Проверяем что игроки в одном мире
            if (!recipient.getWorld().getName().equals(sender.getWorld().getName())) {
                continue;
            }

            // Проверяем расстояние
            double distance = sender.getLocation().distance(recipient.getLocation());
            if (distance <= maxDistance) {
                event.getRecipients().add(recipient);
            }
        }
    }
}