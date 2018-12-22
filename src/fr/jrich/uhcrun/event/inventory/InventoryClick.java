package fr.jrich.uhcrun.event.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.event.player.PlayerInteract;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.handler.Team;
import fr.jrich.uhcrun.util.ItemBuilder;

public class InventoryClick extends UHCRunListener {

    public InventoryClick(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (Step.isStep(Step.LOBBY) && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            event.setCancelled(true);
            if (event.getSlot() == event.getRawSlot()) {
                Player player = (Player) event.getWhoClicked();
                ItemStack item = event.getCurrentItem();
                if (event.getInventory().getTitle().equals(ChatColor.GREEN + "Equipes")) {
                    switch (item.getType()) {
                    default:
                        event.setCancelled(true);
                    case DIAMOND:
                        Team team = Team.createRandomTeam(player);
                        if (team != null) {
                            player.sendMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Vous avez créé l'équipe " + team.getColor() + team.getDisplayName());
                            PlayerInteract.openTeamMenu(player);
                        }
                        break;
                    case ARROW:
                        player.closeInventory();
                        break;
                    case ENCHANTED_BOOK:
                        PlayerInteract.openTeamListMenu(player);
                        break;
                    case STAINED_GLASS_PANE:
                        if (item.getData().getData() == DyeColor.RED.getWoolData()) {
                            if (!Team.teamInvitations.containsKey(player.getName())) {
                                player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous n'avez aucune invitation !");
                                player.closeInventory();
                            } else {
                                Team invit = Team.teamInvitations.remove(player.getName());
                                player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous avez refusé l'invitation de l'équipe " + invit.getDisplayName());
                            }
                        } else {
                            if (!Team.teamInvitations.containsKey(player.getName())) {
                                player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous n'avez aucune invitation !");
                                player.closeInventory();
                            } else {
                                Team invit = Team.teamInvitations.remove(player.getName());
                                if (!invit.isAlive()) {
                                    player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "L'équipe n'existe plus...");
                                } else if (invit.getAlivePlayers().size() >= 3) {
                                    player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "L'équipe a atteint le nombre maximum de joueurs !");
                                } else {
                                    Team targetTeam = Team.getPlayerTeam(player);
                                    if (targetTeam != null) {
                                        targetTeam.removePlayer(player);
                                    }
                                    invit.addPlayer(player);
                                    player.sendMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Vous rejoignez l'équipe " + invit.getDisplayName());
                                    PlayerInteract.openTeamMenu(player);
                                }
                            }
                        }
                        break;
                    }
                } else if (event.getInventory().getTitle().contains("Liste")) {
                    Team playerTeam = Team.getPlayerTeam(player);
                    if (item.getType() == Material.GLOWSTONE_DUST) {
                        if (playerTeam != null) {
                            PlayerInteract.openTeamMenu(player);
                        } else {
                            PlayerInteract.openMainMenu(player);
                        }
                    } else if (playerTeam == null) {
                        for (Team team : Team.allTeams) {
                            if (team.getMaterial() == item.getType()) {
                                for (Entry<Team, List<String>> requests : new HashMap<>(Team.playerRequests).entrySet()) {
                                    if (requests.getValue().contains(player.getName())) {
                                        Team.playerRequests.get(requests.getKey()).remove(player.getName());
                                        break;
                                    }
                                }
                                if (!Team.playerRequests.containsKey(team)) {
                                    Team.playerRequests.put(team, new ArrayList<>(Arrays.asList(player.getName())));
                                } else {
                                    Team.playerRequests.get(team).add(player.getName());
                                }
                                player.sendMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Vous avez envoyé une " + ChatColor.GREEN + "demande d'invitation" + ChatColor.GRAY + " à l'équipe " + team.getDisplayName());
                                for (Player teamPlayer : team.getAlivePlayers()) {
                                    teamPlayer.sendMessage(UHCRunPlugin.prefix + ChatColor.GREEN + player.getName() + ChatColor.GRAY + " souhaite rejoindre votre équipe.");
                                }
                                break;
                            }
                        }
                    }
                } else if (event.getInventory().getTitle().contains("Gestion")) {
                    switch (item.getType()) {
                    default:
                        event.setCancelled(true);
                        break;
                    case NETHER_STAR:
                        Team team = Team.getPlayerTeam(player);
                        if (team == null || !team.isAlive()) {
                            Team.allTeams.remove(team);
                            player.closeInventory();
                        } else {
                            Inventory inv = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "--- " + ChatColor.YELLOW + "Votre équipe" + ChatColor.DARK_PURPLE + " ---");
                            for (Player alivePlayer : team.getAlivePlayers()) {
                                inv.addItem(new ItemBuilder(Material.SKULL_ITEM, (short) SkullType.PLAYER.ordinal()).setTitle(ChatColor.ITALIC + alivePlayer.getName()).build());
                            }
                            inv.setItem(53, new ItemBuilder(Material.GLOWSTONE_DUST, 1).setTitle(ChatColor.RED + "Retour arrière").build());
                            player.openInventory(inv);
                        }
                        break;
                    case REDSTONE:
                        Team playerTeam = Team.getPlayerTeam(player);
                        if (playerTeam == null || !playerTeam.isAlive()) {
                            Team.allTeams.remove(playerTeam);
                            player.closeInventory();
                        } else {
                            PlayerInteract.openRequestsMenu(player, playerTeam);
                        }
                        break;
                    case GOLD_INGOT:
                        Team myTeam = Team.getPlayerTeam(player);
                        if (myTeam == null || !myTeam.isAlive()) {
                            Team.allTeams.remove(myTeam);
                            player.closeInventory();
                        } else {
                            PlayerInteract.openPlayersMenu(player, myTeam);
                        }
                        break;
                    case COAL:
                        Team oldTeam = Team.getPlayerTeam(player);
                        if (oldTeam == null || !oldTeam.isAlive()) {
                            Team.allTeams.remove(oldTeam);
                            player.closeInventory();
                        } else {
                            Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_PURPLE + "--- " + ChatColor.RED + "Confirmer" + ChatColor.DARK_PURPLE + " ---");
                            inv.setItem(11, new ItemBuilder(Material.STAINED_CLAY, DyeColor.RED.getWoolData()).setTitle(ChatColor.RED + "" + ChatColor.BOLD + "Refuser").addLores(ChatColor.GRAY + "Annuler et ne pas quitter").build());
                            inv.setItem(15, new ItemBuilder(Material.STAINED_CLAY, DyeColor.GREEN.getWoolData()).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Accepter").addLores(ChatColor.GRAY + "Confirmer et quitter").build());
                            player.openInventory(inv);
                        }
                        break;
                    case BOOK:
                        PlayerInteract.openTeamListMenu(player);
                        break;
                    case ARROW:
                        player.closeInventory();
                        break;
                    }
                } else if (event.getInventory().getTitle().contains("Votre équipe")) {
                    if (item.getType() == Material.GLOWSTONE_DUST) {
                        PlayerInteract.openTeamMenu(player);
                    }
                } else if (event.getInventory().getTitle().contains("Requêtes")) {
                    if (item.getType() == Material.GLOWSTONE_DUST) {
                        PlayerInteract.openTeamMenu(player);
                    } else if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        Team team = Team.getPlayerTeam(player);
                        if (team == null || !team.isAlive()) {
                            Team.allTeams.remove(team);
                            player.closeInventory();
                        } else if (team.getAlivePlayers().size() >= 3) {
                            Team.allTeams.remove(team);
                            player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous ne pouvez pas avoir plus de 3 joueurs dans votre équipe !");
                            PlayerInteract.openRequestsMenu(player, team);
                        } else {
                            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                            Player target = Bukkit.getPlayer(name);
                            if (target == null || !target.isOnline()) {
                                player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Le joueur " + name + " est déconnecté !");
                            } else {
                                Team targetTeam = Team.getPlayerTeam(target);
                                if (targetTeam != null) {
                                    player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Le joueur a déjà une équipe !");
                                    return;
                                }
                                team.addPlayer(target);
                                player.sendMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Vous avez accepté l'invitation de " + ChatColor.GREEN + name);
                                target.sendMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Vous rejoignez l'équipe " + team.getDisplayName());
                            }
                            List<String> requests = Team.playerRequests.get(team);
                            requests.remove(name);
                            if (requests.isEmpty()) {
                                Team.playerRequests.remove(team);
                                PlayerInteract.openTeamMenu(player);
                            } else {
                                PlayerInteract.openRequestsMenu(player, team);
                            }
                        }
                    }
                } else if (event.getInventory().getTitle().contains("Joueurs")) {
                    if (item.getType() == Material.GLOWSTONE_DUST) {
                        PlayerInteract.openTeamMenu(player);
                    } else if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        Team team = Team.getPlayerTeam(player);
                        if (team == null || !team.isAlive()) {
                            Team.allTeams.remove(team);
                            player.closeInventory();
                        } else {
                            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                            Player target = Bukkit.getPlayer(name);
                            if (target == null || !target.isOnline()) {
                                player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Le joueur " + name + " est déconnecté !");
                            } else {
                                Team targetTeam = Team.getPlayerTeam(target);
                                if (targetTeam != null && targetTeam.isAlive()) {
                                    player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Le joueur " + name + " est déjà dans une équipe !");
                                } else if (team.getAlivePlayers().size() >= 3) {
                                    player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous ne pouvez pas avoir plus de 3 joueurs dans votre équipe !");
                                } else {
                                    if (Team.teamInvitations.containsKey(target.getName())) {
                                        player.sendMessage(ChatColor.RED + UHCRunPlugin.prefix + ChatColor.RED + "Le joueur " + name + " a déjà une invitation en cours !");
                                    } else {
                                        Team.teamInvitations.put(target.getName(), team);
                                        player.sendMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "Vous avez invité " + ChatColor.GREEN + name + ChatColor.GRAY + " à rejoindre votre équipe !");
                                        target.sendMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "L'équipe " + team.getDisplayName() + ChatColor.GRAY + " vous invite à la rejoindre !");
                                    }
                                }
                            }
                            PlayerInteract.openPlayersMenu(player, team);
                        }
                    }
                } else if (event.getInventory().getTitle().contains("Confirmer")) {
                    if (item.getType() == Material.STAINED_CLAY) {
                        if (item.getDurability() == DyeColor.RED.getWoolData()) {
                            PlayerInteract.openTeamMenu(player);
                        } else {
                            Team team = Team.getPlayerTeam(player);
                            player.sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Vous vous êtes retiré de votre équipe.");
                            player.closeInventory();
                            if (team != null && team.isAlive()) {
                                team.removePlayer(player);
                            }
                        }
                    }
                }
            }
        }
    }
}
