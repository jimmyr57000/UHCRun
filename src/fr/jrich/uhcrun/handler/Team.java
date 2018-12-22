package fr.jrich.uhcrun.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Scoreboard;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.util.ItemBuilder;
import fr.jrich.uhcrun.util.ScoreboardBuilder;

@Data
public class Team {
    public static List<Team> allTeams = new ArrayList<>();
    public static Map<String, Team> teamInvitations = new HashMap<>();
    public static Map<Team, List<String>> playerRequests = new HashMap<>();

    public static Team getPlayerTeam(Player player) {
        if (player.hasMetadata("PLAYER_TEAM")) { return (Team) player.getMetadata("PLAYER_TEAM").get(0).value(); }
        return null;
    }

    public static Team getTeam(String name) {
        for (Team team : Team.allTeams) {
            if (team.getCraftTeam() != null && team.getName().equalsIgnoreCase(name)) { return team; }
        }
        return null;
    }

    private static List<Material> teamMaterials = new ArrayList<Material>(Arrays.asList(Material.DIAMOND, Material.REDSTONE, Material.GOLD_INGOT, Material.NETHER_STAR, Material.SPONGE, Material.RED_ROSE, Material.BEACON, Material.COAL, Material.QUARTZ, Material.LAPIS_BLOCK, Material.GOLDEN_APPLE));
    private static Map<Material, ChatColor> teamColors = new HashMap<Material, ChatColor>() {
        {
            this.put(Material.DIAMOND, ChatColor.AQUA);
            this.put(Material.REDSTONE, ChatColor.DARK_RED);
            this.put(Material.GOLD_INGOT, ChatColor.GOLD);
            this.put(Material.NETHER_STAR, ChatColor.DARK_PURPLE);
            this.put(Material.SPONGE, ChatColor.YELLOW);
            this.put(Material.RED_ROSE, ChatColor.RED);
            this.put(Material.BEACON, ChatColor.GREEN);
            this.put(Material.COAL, ChatColor.BLACK);
            this.put(Material.QUARTZ, ChatColor.WHITE);
            this.put(Material.LAPIS_BLOCK, ChatColor.DARK_BLUE);
            this.put(Material.GOLDEN_APPLE, ChatColor.LIGHT_PURPLE);
        }
    };
    private static Map<Material, String> teamNames = new HashMap<Material, String>() {
        {
            this.put(Material.DIAMOND, "Diamant");
            this.put(Material.REDSTONE, "Redstone");
            this.put(Material.GOLD_INGOT, "Or");
            this.put(Material.NETHER_STAR, "Nether star");
            this.put(Material.SPONGE, "Eponge");
            this.put(Material.RED_ROSE, "Rose");
            this.put(Material.BEACON, "Balise");
            this.put(Material.COAL, "Charbon");
            this.put(Material.QUARTZ, "Quartz");
            this.put(Material.LAPIS_BLOCK, "Lapis");
            this.put(Material.GOLDEN_APPLE, "Pomme de notch");
        }
    };

    public static Team createRandomTeam(Player player) {
        Map<Material, Integer> usedMaterials = new HashMap<>();
        List<Material> materials = new ArrayList<>(Team.teamMaterials);
        for (Team team : Team.allTeams) {
            if (!usedMaterials.containsKey(team.getMaterial())) {
                usedMaterials.put(team.getMaterial(), 1);
            } else {
                usedMaterials.put(team.getMaterial(), usedMaterials.get(team.getMaterial()) + 1);
            }
            materials.remove(team.getMaterial());
        }
        if (materials.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Toutes les équipes ont déjà été crées !");
            return null;
        }
        Material teamMaterial = materials.get(0);
        ChatColor teamColor = Team.teamColors.get(teamMaterial);
        String teamName = Team.teamNames.get(teamMaterial);
        Team team = new Team(teamMaterial, teamMaterial.name().toLowerCase(), teamColor + teamName, teamColor);
        team.addPlayer(player);
        return team;
    }

    private Material material;
    private String name;
    private String displayName;
    private ChatColor color;
    private Location location;
    private org.bukkit.scoreboard.Team craftTeam;

    public Team(Material material, String name, String displayName, ChatColor color) {
        this.material = material;
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.createTeam(Bukkit.getScoreboardManager().getMainScoreboard());
        Team.allTeams.add(this);
    }

    public boolean isAlive() {
        return !this.getAlivePlayers().isEmpty();
    }

    public ItemStack getIcon() {
        int index = 0;
        Set<Player> alivePlayers = this.getAlivePlayers();
        String[] lore = new String[alivePlayers.size()];
        for (Player player : alivePlayers) {
            lore[index] = ChatColor.AQUA + player.getName();
            index++;
        }
        ItemStack item = new ItemBuilder(material, 1).setTitle(displayName).addLores(lore).build();
        return item;
    }

    public Set<Player> getAlivePlayers() {
        Set<Player> players = new HashSet<>();
        for (OfflinePlayer offline : craftTeam.getPlayers()) {
            if (offline instanceof Player && !((Player) offline).isDead()) {
                players.add((Player) offline);
            }
        }
        return players;
    }

    public void removePlayer(Player player) {
        player.removeMetadata("PLAYER_TEAM", UHCRunPlugin.instance);
        player.setPlayerListName(player.getName());
        craftTeam.removePlayer(player);
        if (!this.isAlive()) {
            Team.allTeams.remove(this);
        } else {
            this.updateScoreboard();
        }
        PlayerData data = UHCRunPlugin.instance.getData(player);
        if (data != null) {
            ScoreboardBuilder sbBuilder = data.getSbBuilder();
            sbBuilder.removeLines(15, 14, 13, 12);
            if (!Step.isStep(Step.LOBBY)) {
                sbBuilder.setLine(15, "Team " + ChatColor.GRAY + "Spectateur");
            }
        }
    }

    public void addPlayer(Player player) {
        player.setMetadata("PLAYER_TEAM", new FixedMetadataValue(UHCRunPlugin.instance, this));
        player.setPlayerListName(color + (player.getName().length() <= 14 ? player.getName() : player.getName().substring(0, 14)));
        craftTeam.addPlayer(player);
        this.updateScoreboard();
    }

    private void updateScoreboard() {
        Set<Player> alivePlayers = this.getAlivePlayers();
        for (Player teamPlayer : alivePlayers) {
            ScoreboardBuilder builder = UHCRunPlugin.instance.getData(teamPlayer).getSbBuilder();
            builder.setLine(15, "Team " + color + displayName);
            builder.removeLines(14, 13, 12);
            int score = 14;
            for (Player alive : alivePlayers) {
                builder.setLine(score--, " " + color + alive.getName());
            }
        }
    }

    public void createTeam(Scoreboard scoreboard) {
        craftTeam = scoreboard.getTeam(name);
        if (craftTeam == null) {
            craftTeam = scoreboard.registerNewTeam(name);
        }
        craftTeam.setPrefix(color.toString());
        craftTeam.setDisplayName(displayName);
        craftTeam.setAllowFriendlyFire(false);
        craftTeam.setSuffix(ChatColor.RESET.toString());
    }
}
