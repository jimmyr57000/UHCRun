package fr.jrich.uhcrun.event.player;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.handler.Team;

public class AsyncPlayerChat extends UHCRunListener {
    public AsyncPlayerChat(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Team playerTeam = Team.getPlayerTeam(player);
        if (playerTeam == null || !Step.isStep(Step.IN_GAME)) {
            event.setFormat((playerTeam == null ? ChatColor.GRAY : playerTeam.getColor()) + player.getName() + ChatColor.RESET + ": " + event.getMessage());
            if (Step.isStep(Step.IN_GAME)) {
                for (Player receiver : new ArrayList<>(event.getRecipients())) {
                    if (receiver != player && Team.getPlayerTeam(receiver) != null) {
                        event.getRecipients().remove(receiver);
                    }
                }
            }
        } else {
            if (event.getMessage().startsWith("@")) {
                event.setFormat("(Tous) " + playerTeam.getColor() + player.getName() + ChatColor.RESET + ": " + event.getMessage().replaceFirst("@", ""));
            } else {
                event.setFormat(ChatColor.WHITE + "[" + playerTeam.getColor() + StringUtils.capitalize(playerTeam.getDisplayName()) + ChatColor.WHITE + "] " + playerTeam.getColor() + player.getName() + ChatColor.RESET + ": " + event.getMessage());
                for (Player online : Bukkit.getOnlinePlayers()) {
                    Team team = Team.getPlayerTeam(online);
                    if (team != playerTeam) {
                        event.getRecipients().remove(online);
                    }
                }
            }
        }
    }
}
