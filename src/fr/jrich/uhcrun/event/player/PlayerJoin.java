package fr.jrich.uhcrun.event.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.scheduler.BeginCountdown;

public class PlayerJoin extends UHCRunListener {

    public PlayerJoin(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        plugin.loadData(player);
        if (!Step.canJoin() && player.hasPermission("games.join")) {
            event.setJoinMessage(null);
            plugin.setSpectator(player);
        } else if (Step.isStep(Step.LOBBY)) {
            event.setJoinMessage(UHCRunPlugin.prefix + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " a rejoint la partie " + ChatColor.GREEN + "(" + Bukkit.getOnlinePlayers().length + "/" + Bukkit.getMaxPlayers() + ")");
            player.setGameMode(GameMode.ADVENTURE);
            ItemStack itemStack = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + "Equipes " + ChatColor.YELLOW + "(clic droit)");
            itemStack.setItemMeta(meta);
            player.getInventory().setItem(0, itemStack);
            player.teleport(player.getWorld().getSpawnLocation());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online != player) {
                    plugin.getData(online).getSbBuilder().getScoreboard().getObjective("health").getScore(player).setScore(20);
                }
            }
            if (Bukkit.getOnlinePlayers().length >= Bukkit.getMaxPlayers() / 2 && !BeginCountdown.started) {
                new BeginCountdown(plugin);
            }
        }
    }
}
