package fr.jrich.uhcrun.scheduler;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.handler.Team;
import fr.jrich.uhcrun.util.ScoreboardBuilder;

public class GameRunnable extends BukkitRunnable {
    public static boolean godMode = true;
    public static int teleportTime = 1200, pvpTime = 30;
    public static double mapSize = 1000;
    private UHCRunPlugin plugin;
    private int totalSeconds;
    private int totalMinutes;

    public GameRunnable(UHCRunPlugin plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {

            @Override
            public void run() {
                GameRunnable.godMode = false;
            }
        }.runTaskLater(plugin, 20 * 30);
        this.runTaskTimer(UHCRunPlugin.instance, 0, 20);
    }

    @Override
    public void run() {
        if (!Step.isStep(Step.IN_GAME)) {
            this.cancel();
            return;
        } else {
            if (totalMinutes == 32) {
                Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.GOLD + ChatColor.BOLD + "Fin du jeu " + ChatColor.YELLOW + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|" + ChatColor.YELLOW + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.BOLD + " Félicitations " + ChatColor.YELLOW + ChatColor.MAGIC + " |" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|" + ChatColor.YELLOW + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|");
                plugin.stopGame();
                this.cancel();
                return;
            }
            int diffMinutes = 32 - totalMinutes, diffSeconds = 60 - totalSeconds;
            if (diffMinutes <= 2 && (diffSeconds == 0 || diffMinutes == 0 && (diffSeconds == 30 || diffSeconds == 10 || diffSeconds <= 5))) {
                Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Fin du jeu dans" + ChatColor.AQUA + (diffMinutes > 1 ? " " + diffMinutes + " minute" + (diffMinutes > 1 ? "s" : "") : "") + (diffSeconds > 0 ? " " + diffSeconds + " seconde" + (diffSeconds > 1 ? "s" : "") : ""));
                if (diffMinutes == 0 && diffSeconds <= 5) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.playSound(online.getLocation(), Sound.CLICK, 1, 1);
                    }
                }
            }
            totalSeconds++;
            if (totalSeconds >= 60) {
                totalMinutes++;
                totalSeconds = 0;
            }
        }
        int players = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!plugin.isSpectator(online) && !online.isDead()) {
                players++;
            }
        }
        Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        for (Player online : Bukkit.getOnlinePlayers()) {
            Location location = online.getLocation();
            Team team = Team.getPlayerTeam(online);
            ScoreboardBuilder builder = plugin.getData(online).getSbBuilder();
            builder.setLine(15, StringUtils.substring("Team " + (team == null ? ChatColor.GRAY + "Spectateur" : team.getColor() + team.getDisplayName()), 0, 32));
            if (team != null) {
                for (Player playerTeam : team.getAlivePlayers()) {
                    int score = 14;
                    if (playerTeam != online) {
                        builder.setLine(score--, " " + team.getColor() + playerTeam.getName());
                    }
                }
            }
            builder.setLine(11, ChatColor.GRAY + "--- Jeu ---");
            builder.setLine(10, players + "" + ChatColor.GREEN + " Joueurs");
            builder.setLine(9, Team.allTeams.size() + "" + ChatColor.GREEN + " Equipes");
            builder.setLine(8, ChatColor.GOLD + "Centre : " + ChatColor.YELLOW + (int) Math.ceil(location.distance(spawnLocation)));
            builder.setLine(7, ChatColor.GOLD + "Bordure : " + ChatColor.YELLOW + "+" + (int) Math.floor(GameRunnable.mapSize / 2) + " -" + (int) Math.floor(GameRunnable.mapSize / 2));
            builder.setLine(6, ChatColor.GRAY + "--- Timers ---");
            if (GameRunnable.teleportTime > 0) {
                int remainingMins = GameRunnable.teleportTime / 60 % 60;
                int remainingSecs = GameRunnable.teleportTime % 60;
                builder.setLine(5, ChatColor.RED + "Téléportation");
                builder.setLine(4, ChatColor.RED + "dans " + ChatColor.GRAY + (remainingMins < 10 ? "0" : "") + remainingMins + ":" + (remainingSecs < 10 ? "0" : "") + remainingSecs);
            } else if (GameRunnable.teleportTime == -1 && GameRunnable.pvpTime > 0) {
                builder.setLine(5, ChatColor.RED + "PVP dans");
                builder.setLine(4, ChatColor.GRAY + "" + GameRunnable.pvpTime + "s");
            } else {
                builder.removeLines(5, 4);
            }
        }
        if (GameRunnable.teleportTime > 0) {
            if (GameRunnable.teleportTime == 30 || GameRunnable.teleportTime == 10 || GameRunnable.teleportTime <= 5) {
                Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Téléportation des équipes dans " + ChatColor.AQUA + GameRunnable.teleportTime + " seconde" + (GameRunnable.teleportTime > 1 ? "s" : "") + ".");
                if (GameRunnable.teleportTime <= 5) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.playSound(online.getLocation(), Sound.CLICK, 1, 1);
                    }
                }
            }
            GameRunnable.teleportTime--;
        } else if (GameRunnable.teleportTime == 0) {
            GameRunnable.teleportTime = -1;
            GameRunnable.mapSize = 400;
            GameRunnable.godMode = true;
            World world = Bukkit.getWorlds().get(0);
            for (Team team : Team.allTeams) {
                team.setLocation(BeginCountdown.getRandomLocation(world).add(0, 20, 0));
                for (Player player : team.getAlivePlayers()) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.teleport(team.getLocation());
                    if (player.getInventory().firstEmpty() == -1) {
                        player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.COMPASS));
                    } else {
                        player.getInventory().addItem(new ItemStack(Material.COMPASS));
                    }
                }
            }
            Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "La map est désormais de " + ChatColor.AQUA + "400x400" + ChatColor.GRAY + ". Les coordonnées sont les suivantes : " + ChatColor.AQUA + "+200 -200" + ChatColor.GRAY + ". Les dégâts et le PVP seront actifs dans " + ChatColor.AQUA + "30 secondes.");
        } else if (GameRunnable.pvpTime > -1) {
            if (GameRunnable.pvpTime > 0) {
                if (GameRunnable.pvpTime == 30 || GameRunnable.pvpTime == 10 || GameRunnable.pvpTime <= 5) {
                    Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Activation du PVP dans " + ChatColor.AQUA + GameRunnable.pvpTime + " secondes.");
                    if (GameRunnable.pvpTime <= 5) {
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            online.playSound(online.getLocation(), Sound.CLICK, 1, 1);
                        }
                    }
                }
                GameRunnable.pvpTime--;
            } else {
                GameRunnable.pvpTime = -1;
                GameRunnable.godMode = false;
                Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.AQUA + "Le PVP est activé ! " + ChatColor.GRAY + "La map se réduira jusqu'en " + ChatColor.AQUA + "10x10.");
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (!Step.isStep(Step.IN_GAME) || GameRunnable.mapSize <= 10) {
                            GameRunnable.mapSize = 10;
                            this.cancel();
                            return;
                        }
                        Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (plugin.isInBorder(spawnLocation, player.getLocation())) {
                                player.removeMetadata("WORLD_BORDER", plugin);
                            } else {
                                if (plugin.isSpectator(player)) {
                                    player.teleport(spawnLocation);
                                } else {
                                    player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous êtes en dehors de la limite de la carte !!! Rentrez vite si vous ne voulez pas mourir...");
                                    if (!player.hasMetadata("WORLD_BORDER")) {
                                        player.setMetadata("WORLD_BORDER", new FixedMetadataValue(plugin, 0.25));
                                    } else {
                                        double value = player.getMetadata("WORLD_BORDER").get(0).asDouble();
                                        player.removeMetadata("WORLD_BORDER", plugin);
                                        player.setMetadata("WORLD_BORDER", new FixedMetadataValue(plugin, value + 0.25));
                                        player.damage(value);
                                    }
                                }
                            }
                        }
                        GameRunnable.mapSize -= 0.65;
                    }
                }.runTaskTimer(plugin, 0, 20);
            }
        }
    }
}
