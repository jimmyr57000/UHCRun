package fr.jrich.uhcrun.event.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.scheduler.BeginCountdown;

public class PlayerQuit extends UHCRunListener {

    public PlayerQuit(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        BeginCountdown.resetPlayer(event.getPlayer());
        plugin.onPlayerLoose(event.getPlayer());
    }
}
