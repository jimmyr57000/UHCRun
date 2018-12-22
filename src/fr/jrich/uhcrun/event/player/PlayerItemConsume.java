package fr.jrich.uhcrun.event.player;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;

public class PlayerItemConsume extends UHCRunListener {

    public PlayerItemConsume(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent evt) {
        if (Step.isStep(Step.LOBBY) || Step.isStep(Step.TELEPORTATION) || plugin.isSpectator(evt.getPlayer())) {
            evt.setCancelled(true);
        } else if (evt.getItem().getType() == Material.GOLDEN_APPLE && evt.getItem().getDurability() == 0) {
            evt.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
        }
    }
}
