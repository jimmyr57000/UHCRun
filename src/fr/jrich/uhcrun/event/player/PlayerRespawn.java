package fr.jrich.uhcrun.event.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;

public class PlayerRespawn extends UHCRunListener {

    public PlayerRespawn(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (!Step.isStep(Step.LOBBY) && !Step.isStep(Step.TELEPORTATION)) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    PlayerRespawn.this.plugin.setSpectator(event.getPlayer());
                }
            }.runTaskLater(plugin, 1);
        }
    }
}
