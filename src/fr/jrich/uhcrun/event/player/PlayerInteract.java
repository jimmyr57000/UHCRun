package fr.jrich.uhcrun.event.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.handler.Team;
import fr.jrich.uhcrun.scheduler.GameRunnable;
import fr.jrich.uhcrun.util.ItemBuilder;

public class PlayerInteract extends UHCRunListener {

    public PlayerInteract(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Step.isStep(Step.LOBBY) && plugin.isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        } else if (event.hasItem() && event.getAction().name().contains("RIGHT")) {
            if (event.getMaterial() == Material.NETHER_STAR && Step.isStep(Step.LOBBY)) {
                Player player = event.getPlayer();
                Team team = Team.getPlayerTeam(player);
                if (team != null && team.isAlive()) {
                    PlayerInteract.openTeamMenu(player);
                } else {
                    PlayerInteract.openMainMenu(player);
                }
            } else if (Step.isStep(Step.IN_GAME)) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && GameRunnable.pvpTime > -1 && event.getMaterial() == Material.LAVA_BUCKET) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous ne pouvez pas poser de seau de lave avant la téléportation.");
                    event.getPlayer().updateInventory();
                } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getMaterial().isBlock()) {
                    for (Entity entity : event.getPlayer().getNearbyEntities(1, 1, 1)) {
                        if (entity instanceof Player && plugin.isSpectator((Player) entity)) {
                            Block newBlock = event.getClickedBlock().getRelative(event.getBlockFace());
                            newBlock.setType(event.getItem().getType());
                            newBlock.setData(event.getItem().getData().getData());
                            if (event.getItem().getAmount() > 1) {
                                event.getItem().setAmount(event.getItem().getAmount() - 1);
                            } else {
                                event.getPlayer().setItemInHand(null);
                            }
                            event.getPlayer().updateInventory();
                        }
                    }
                } else if (event.getMaterial() == Material.COMPASS) {
                    Player player = event.getPlayer();
                    boolean found = false;
                    for (ItemStack itemStack : player.getInventory().getContents()) {
                        if (itemStack != null && itemStack.getType() == Material.ROTTEN_FLESH) {
                            if (itemStack.getAmount() > 1) {
                                itemStack.setAmount(itemStack.getAmount() - 1);
                            } else {
                                player.getInventory().remove(itemStack);
                            }
                            player.updateInventory();
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        player.playSound(player.getLocation(), Sound.STEP_WOOD, 1.0F, 1.0F);
                        player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous n'avez pas de chair de zombie.");
                    } else {
                        player.playSound(player.getLocation(), Sound.BURP, 1.0F, 1.0F);
                        Player nearest = null;
                        for (Player onPlayer : Bukkit.getOnlinePlayers()) {
                            if (!plugin.isSpectator(onPlayer) && !Team.getPlayerTeam(onPlayer).getAlivePlayers().contains(player)) {
                                double distance = player.getLocation().distance(onPlayer.getLocation());
                                if (distance > 1.0D && distance < 99999.0D) {
                                    nearest = onPlayer;
                                }
                            }
                        }
                        if (nearest == null) {
                            player.sendMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "" + ChatColor.ITALIC + "Seul le silence comble votre requête.");
                        } else {
                            player.sendMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "La boussole pointe sur le joueur le plus proche.");
                            player.setCompassTarget(nearest.getLocation());
                        }
                    }
                }
            }
        }
    }

    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.GREEN + "Equipes");
        ItemStack createTeam = new ItemBuilder(Material.DIAMOND, 1).setTitle(ChatColor.GREEN + "Créer une équipe").build();
        ItemStack listTeams = new ItemBuilder(Material.ENCHANTED_BOOK, 1).setTitle(ChatColor.GREEN + "Liste des équipes").addLores(ChatColor.GRAY + "Pour pouvoir rejoindre", ChatColor.GRAY + "une équipe !").build();
        ItemStack exitMenu = new ItemBuilder(Material.ARROW, 1).setTitle(ChatColor.RED + "Sortir du menu").build();
        ItemStack acceptInv = new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.LIME.getWoolData()).setTitle(ChatColor.GREEN + "Accepter une invitation").build();
        ItemStack declineInv = new ItemBuilder(Material.STAINED_GLASS_PANE, DyeColor.RED.getWoolData()).setTitle(ChatColor.RED + "Refuser une invitation").build();
        inv.setItem(12, createTeam);
        inv.setItem(14, listTeams);
        inv.setItem(18, acceptInv);
        inv.setItem(19, declineInv);
        inv.setItem(26, exitMenu);
        player.openInventory(inv);
    }

    public static void openTeamListMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_PURPLE + "--- " + ChatColor.YELLOW + "Liste des équipes" + ChatColor.DARK_PURPLE + " ---");
        for (Team loopTeam : Team.allTeams) {
            inv.addItem(loopTeam.getIcon());
        }
        inv.setItem(26, new ItemBuilder(Material.GLOWSTONE_DUST, 1).setTitle(ChatColor.RED + "Retour arrière").build());
        player.openInventory(inv);
    }

    public static void openTeamMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 45, ChatColor.DARK_PURPLE + "--- " + ChatColor.YELLOW + "Gestion" + ChatColor.DARK_PURPLE + " ---");
        ItemStack players = new ItemBuilder(Material.NETHER_STAR, 1).setTitle(ChatColor.GREEN + "Dans votre équipe").build();
        ItemStack requests = new ItemBuilder(Material.REDSTONE, 1).setTitle(ChatColor.RED + "Requêtes").build();
        ItemStack invitePlayer = new ItemBuilder(Material.GOLD_INGOT, 1).setTitle(ChatColor.GREEN + "Inviter des joueurs").build();
        ItemStack leaveTeam = new ItemBuilder(Material.COAL, 1).setTitle(ChatColor.RED + "Quitter l'équipe").build();
        ItemStack exitMenu = new ItemBuilder(Material.ARROW, 1).setTitle(ChatColor.RED + "Sortir du menu").build();
        ItemStack teamsList = new ItemBuilder(Material.BOOK, 1).setTitle(ChatColor.GREEN + "Liste des équipes").build();
        inv.setItem(2, players);
        inv.setItem(6, requests);
        inv.setItem(22, invitePlayer);
        inv.setItem(38, teamsList);
        inv.setItem(42, leaveTeam);
        inv.setItem(44, exitMenu);
        player.openInventory(inv);
    }

    public static void openPlayersMenu(Player player, Team team) {
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "--- " + ChatColor.YELLOW + "Joueurs" + ChatColor.DARK_PURPLE + " ---");
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online != player) {
                Team onlineTeam = Team.getPlayerTeam(online);
                if (onlineTeam == team) {
                    continue;
                }
                inv.addItem(new ItemBuilder(Material.SKULL_ITEM, (short) SkullType.PLAYER.ordinal()).setTitle(ChatColor.ITALIC + online.getName()).addLores(onlineTeam == null ? ChatColor.GREEN + "Aucune équipe !" : ChatColor.RED + "Dans une équipe").build());
            }
        }
        inv.setItem(53, new ItemBuilder(Material.GLOWSTONE_DUST, 1).setTitle(ChatColor.RED + "Retour arrière").build());
        player.openInventory(inv);
    }

    public static void openRequestsMenu(Player player, Team team) {
        List<String> requests = Team.playerRequests.get(team);
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "--- " + ChatColor.YELLOW + "Requêtes" + ChatColor.DARK_PURPLE + " ---");
        if (requests != null) {
            for (String name : new ArrayList<>(requests)) {
                Player target = Bukkit.getPlayer(name);
                if (target == null || !target.isOnline()) {
                    requests.remove(name);
                } else {
                    inv.addItem(new ItemBuilder(Material.SKULL_ITEM, (short) SkullType.PLAYER.ordinal()).setTitle(ChatColor.ITALIC + name).build());
                }
            }
        }
        inv.setItem(53, new ItemBuilder(Material.GLOWSTONE_DUST, 1).setTitle(ChatColor.RED + "Retour arrière").build());
        player.openInventory(inv);
    }
}
