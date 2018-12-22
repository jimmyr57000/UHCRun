package fr.jrich.uhcrun.event.player;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.util.ParticleEffect;

public class PlayerMove extends UHCRunListener {

    public PlayerMove(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            if (Step.isStep(Step.TELEPORTATION) && to.distance(to.getWorld().getSpawnLocation()) > 15) {
                event.setTo(event.getFrom());
            } else if (Step.isStep(Step.IN_GAME)) {
                Location spawn = from.getWorld().getSpawnLocation();
                if (!plugin.isInBorder(spawn, to) && plugin.isInBorder(spawn, from)) {
                    event.getPlayer().sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous avez atteint la limite de la carte !");
                    event.setTo(event.getFrom());
                }
            } else if (Step.isStep(Step.LOBBY) || Step.isStep(Step.TELEPORTATION)) {
                Block under = from.getBlock().getRelative(BlockFace.DOWN);
                if (under.getType() == Material.STAINED_GLASS) {
                    under.setData(DyeColor.values()[plugin.getRandom().nextInt(DyeColor.values().length)].getWoolData());
                    ParticleEffect.SPELL_WITCH.display(0.2F, 0.2F, 0.2F, 0.2f, 3, under.getLocation().add(0, 1, 0), 16);
                } else if (!event.getPlayer().getAllowFlight() && to.getBlockY() >= to.getWorld().getSpawnLocation().getBlockY() + 2) {
                    event.getPlayer().kickPlayer(ChatColor.RED + "Arrête le fly !");
                }
            }
        }
    }
}
