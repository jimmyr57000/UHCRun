package fr.jrich.uhcrun.event.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.handler.Team;

public class PlayerDeath extends UHCRunListener {

    public PlayerDeath(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (Step.isStep(Step.LOBBY) || Step.isStep(Step.TELEPORTATION) || Team.getPlayerTeam(event.getEntity()) == null) {
            event.setDeathMessage(null);
            event.getDrops().clear();
            event.setDroppedExp(0);
        } else {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.WITHER_SPAWN, 1.0F, 1.0F);
            }
            Player player = event.getEntity();
            Player killer = player.getKiller();
            if (killer != null) {
                plugin.getData(killer).addCoins(2);
                killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
            }
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwner(player.getName());
            skullMeta.setDisplayName(player.getName());
            skull.setItemMeta(skullMeta);
            event.getDrops().add(skull);
            event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE));
            plugin.onPlayerLoose(player);
        }
    }
}
