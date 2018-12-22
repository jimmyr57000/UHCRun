package fr.jrich.uhcrun.handler;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.jrich.uhcrun.util.ScoreboardBuilder;

@Data
@AllArgsConstructor
public class PlayerData {
    private UUID uuid;
    private String name;
    private double coins;
    private ScoreboardBuilder sbBuilder;

    public void addCoins(double coins) {
        this.addCoins(coins, true);
    }

    public void addCoins(double coins, boolean msg) {
        Player player = Bukkit.getPlayer(name);
        if (player != null && player.isOnline()) {
            this.coins += player.hasPermission("funcoins.mvpplus") ? coins * 4 : player.hasPermission("funcoins.mvp") ? coins * 3 : player.hasPermission("funcoins.vip") ? coins * 2 : coins;
            if (msg) {
                Bukkit.getPlayer(name).sendMessage(ChatColor.GRAY + "Gain de FunCoins + " + ChatColor.GOLD + String.valueOf(coins).replace(".", ","));
            }
        }
    }
}
