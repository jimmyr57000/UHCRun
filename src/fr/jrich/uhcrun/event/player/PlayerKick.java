package fr.jrich.uhcrun.event.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.scheduler.BeginCountdown;

public class PlayerKick extends UHCRunListener {

    public PlayerKick(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
        BeginCountdown.resetPlayer(event.getPlayer());
        plugin.onPlayerLoose(event.getPlayer());
    }
}
