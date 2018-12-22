package fr.jrich.uhcrun.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.handler.Team;
import fr.jrich.uhcrun.util.ScoreboardBuilder;

public class BeginCountdown extends BukkitRunnable {
    public static boolean started = false;
    public static int timeUntilStart = 60;
    private UHCRunPlugin plugin;

    public BeginCountdown(UHCRunPlugin plugin) {
        this.plugin = plugin;
        BeginCountdown.started = true;
        this.runTaskTimer(plugin, 0, 20);
    }

    @Override
    public void run() {
        if (BeginCountdown.timeUntilStart > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setExp(BeginCountdown.timeUntilStart / 60.0F);
                player.setLevel(BeginCountdown.timeUntilStart);
            }
        } else {
            this.cancel();
            if (Bukkit.getOnlinePlayers().length < Bukkit.getMaxPlayers() / 2) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Il n'y a pas assez de joueurs !");
                    ScoreboardBuilder builder = plugin.getData(online).getSbBuilder();
                    builder.setLine(5, ChatColor.GREEN + "Attente");
                    builder.removeLine(4);
                }
                BeginCountdown.timeUntilStart = 60;
                BeginCountdown.started = false;
            } else {
                Step.setCurrentStep(Step.TELEPORTATION);
                Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.AQUA + "Les téléportations commencent. " + ChatColor.RED + ChatColor.BOLD + "NE VOUS DECONNECTEZ PAS !!");
                Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Une équipe sera téléportée toutes les 3 secondes.");
                List<Player> players = new ArrayList<>(Arrays.asList(Bukkit.getOnlinePlayers()));
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (Team.getPlayerTeam(online) != null) {
                        players.remove(online);
                    }
                    ScoreboardBuilder sbBuilder = plugin.getData(online).getSbBuilder();
                    sbBuilder.removeLine(4);
                    sbBuilder.setLine(5, ChatColor.AQUA + "Téléportation");
                }
                for (int i = 0; i < players.size(); i++) {
                    Team team = new Team(Material.STONE, "solo" + (i + 1), "Solo" + (i + 1), ChatColor.GRAY);
                    team.addPlayer(players.get(i));
                }
                int seconds = 3;
                final World world = Bukkit.getWorlds().get(0);
                for (final Team team : Team.allTeams) {
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            if (!Step.isStep(Step.TELEPORTATION)) { return; }
                            Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Téléportation de l'équipe " + team.getDisplayName());
                            if (team.isAlive()) {
                                Location location = plugin.getRandomSpawn();
                                team.setLocation(location);
                                for (int x = -1; x <= 1; x++) {
                                    for (int z = -1; z <= 1; z++) {
                                        Block block = location.clone().add(x, -6, z).getBlock();
                                        block.setType(Material.GLASS);
                                        block.setData(DyeColor.WHITE.getWoolData());
                                    }
                                }
                                for (Player player : team.getAlivePlayers()) {
                                    player.getInventory().clear();
                                    player.teleport(team.getLocation());
                                }
                            }
                        }
                    }.runTaskLater(UHCRunPlugin.instance, 20 * seconds);
                    seconds += 3;
                }
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (!Step.isStep(Step.TELEPORTATION)) { return; }
                        for (Team team : Team.allTeams) {
                            for (Player teamPlayer : team.getAlivePlayers()) {
                                plugin.getData(teamPlayer).getSbBuilder().removeLine(5);
                                BeginCountdown.resetPlayer(teamPlayer);
                                teamPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                            }
                            Location location = team.getLocation();
                            if (location != null) {
                                for (int x = -1; x <= 1; x++) {
                                    for (int z = -1; z <= 1; z++) {
                                        Block block = location.clone().add(x, -6, z).getBlock();
                                        block.setType(Material.AIR);
                                        block.setData((byte) 0);
                                    }
                                }
                            }
                        }
                        Step.setCurrentStep(Step.IN_GAME);
                        world.setGameRuleValue("doDaylightCycle", "true");
                        Location spawnLoc = world.getSpawnLocation();
                        for (int x = -9; x <= 9; x++) {
                            for (int y = -1; y <= 2; y++) {
                                for (int z = -9; z <= 9; z++) {
                                    Block block = spawnLoc.clone().add(x, y, z).getBlock();
                                    if (block.getType() != Material.AIR) {
                                        block.setType(Material.AIR);
                                    }
                                }
                            }
                        }
                        Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.YELLOW + ChatColor.ITALIC + "Pour envoyer un message à tous les joueurs mettez un " + ChatColor.GOLD + "@" + ChatColor.YELLOW + ChatColor.ITALIC + " devant.");
                        new GameRunnable(plugin);
                    }
                }.runTaskLater(plugin, seconds * 20);
            }
            return;
        }
        int remainingMins = BeginCountdown.timeUntilStart / 60 % 60;
        int remainingSecs = BeginCountdown.timeUntilStart % 60;
        for (Player online : Bukkit.getOnlinePlayers()) {
            ScoreboardBuilder builder = plugin.getData(online).getSbBuilder();
            builder.setLine(5, ChatColor.RED + "Début du jeu");
            builder.setLine(4, ChatColor.RED + "dans " + ChatColor.GRAY + (remainingMins > 0 ? remainingMins + "min" : "") + (remainingSecs > 0 ? (remainingMins > 0 ? " " : "") + remainingSecs + "s" : ""));
        }
        if (BeginCountdown.timeUntilStart % 30 == 0 || remainingMins == 0 && (remainingSecs % 10 == 0 || remainingSecs <= 5)) {
            String message = ChatColor.GOLD + "Démarrage du jeu dans " + ChatColor.YELLOW + (remainingMins > 0 ? remainingMins + " minute" + (remainingMins > 1 ? "s" : "") : "") + (remainingSecs > 0 ? (remainingMins > 0 ? " " : "") + remainingSecs + " seconde" + (remainingSecs > 1 ? "s" : "") : "") + ".";
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(UHCRunPlugin.prefix + message);
                if (remainingMins == 0 && remainingSecs <= 10) {
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
            }
        }
        BeginCountdown.timeUntilStart--;
    }

    public static Location getRandomLocation(World world) {
        int lower = (int) -(GameRunnable.mapSize / 2);
        int higher = (int) (GameRunnable.mapSize / 2);
        int x = (int) (Math.random() * (higher - lower)) + lower;
        int z = (int) (Math.random() * (higher - lower)) + lower;
        Location location = new Location(world, x, world.getHighestBlockYAt(x, z), z);
        location.getChunk().load(true);
        return location;
    }

    public static void resetPlayer(Player player) {
        player.setFireTicks(0);
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setExhaustion(5.0F);
        player.setFallDistance(0);
        player.setExp(0.0F);
        player.setLevel(0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.closeInventory();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
}
