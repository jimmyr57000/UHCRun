package fr.jrich.uhcrun;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import lombok.Getter;
import lombok.SneakyThrows;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.event.block.BlockBreak;
import fr.jrich.uhcrun.event.block.BlockPlace;
import fr.jrich.uhcrun.event.block.LeavesDecay;
import fr.jrich.uhcrun.event.entity.CreatureSpawn;
import fr.jrich.uhcrun.event.entity.EntityDamage;
import fr.jrich.uhcrun.event.entity.EntityDamageByPlayer;
import fr.jrich.uhcrun.event.entity.EntityDeath;
import fr.jrich.uhcrun.event.entity.EntityExplode;
import fr.jrich.uhcrun.event.entity.EntityRegainHealth;
import fr.jrich.uhcrun.event.entity.EntityTarget;
import fr.jrich.uhcrun.event.entity.FoodLevelChange;
import fr.jrich.uhcrun.event.inventory.BrewCraft;
import fr.jrich.uhcrun.event.inventory.CraftItem;
import fr.jrich.uhcrun.event.inventory.InventoryClick;
import fr.jrich.uhcrun.event.player.AsyncPlayerChat;
import fr.jrich.uhcrun.event.player.PlayerCommandPreprocess;
import fr.jrich.uhcrun.event.player.PlayerDamage;
import fr.jrich.uhcrun.event.player.PlayerDamageByPlayer;
import fr.jrich.uhcrun.event.player.PlayerDeath;
import fr.jrich.uhcrun.event.player.PlayerDropItem;
import fr.jrich.uhcrun.event.player.PlayerInteract;
import fr.jrich.uhcrun.event.player.PlayerItemConsume;
import fr.jrich.uhcrun.event.player.PlayerJoin;
import fr.jrich.uhcrun.event.player.PlayerKick;
import fr.jrich.uhcrun.event.player.PlayerLogin;
import fr.jrich.uhcrun.event.player.PlayerMove;
import fr.jrich.uhcrun.event.player.PlayerPickupItem;
import fr.jrich.uhcrun.event.player.PlayerPortal;
import fr.jrich.uhcrun.event.player.PlayerQuit;
import fr.jrich.uhcrun.event.player.PlayerRespawn;
import fr.jrich.uhcrun.event.server.ServerListPing;
import fr.jrich.uhcrun.event.weather.ThunderChange;
import fr.jrich.uhcrun.event.weather.WeatherChange;
import fr.jrich.uhcrun.event.world.ChunkUnload;
import fr.jrich.uhcrun.handler.MySQL;
import fr.jrich.uhcrun.handler.PlayerData;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.handler.Team;
import fr.jrich.uhcrun.scheduler.BeginCountdown;
import fr.jrich.uhcrun.scheduler.GameRunnable;
import fr.jrich.uhcrun.util.BiomeChanger;
import fr.jrich.uhcrun.util.FileUtils;
import fr.jrich.uhcrun.util.ReflectionHandler;
import fr.jrich.uhcrun.util.ScoreboardBuilder;

public class UHCRunPlugin extends JavaPlugin {
    public static UHCRunPlugin instance;
    public static String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "UHCRun" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " ";

    private List<Location> spawnLocations;
    private MySQL database;
    @Getter
    private Random random = new Random();
    private Map<UUID, PlayerData> data = new HashMap<>();
    private Map<Material, Material> craftUpgrades = new HashMap<Material, Material>() {
        {
            this.put(Material.WOOD_AXE, Material.STONE_AXE);
            this.put(Material.WOOD_HOE, Material.STONE_HOE);
            this.put(Material.WOOD_PICKAXE, Material.STONE_PICKAXE);
            this.put(Material.WOOD_SPADE, Material.STONE_SPADE);
            this.put(Material.WOOD_SWORD, Material.STONE_SWORD);
        }
    };

    @Override
    public void onEnable() {
        UHCRunPlugin.instance = this;
        this.saveDefaultConfig();
        database = new MySQL(this, this.getConfig().getString("mysql.host"), this.getConfig().getString("mysql.port"), this.getConfig().getString("mysql.database"), this.getConfig().getString("mysql.user"), this.getConfig().getString("mysql.pass"));
        try {
            database.openConnection();
            database.updateSQL("CREATE TABLE IF NOT EXISTS `players` ( `id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(30) NOT NULL, `uuid` varbinary(16) NOT NULL, `coins` double NOT NULL, `fk_miner` int(11) DEFAULT '0' NOT NULL, `fk_better_bow` int(11) DEFAULT '0' NOT NULL, `fk_better_sword` int(11) DEFAULT '0' NOT NULL, `fk_better_armor` int(11) DEFAULT '0' NOT NULL, `fk_merlin` int(11) DEFAULT '0' NOT NULL, `created_at` datetime NOT NULL, `updated_at` datetime NOT NULL, PRIMARY KEY (`id`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;");
        } catch (ClassNotFoundException | SQLException e) {
            this.getLogger().severe("Impossible de se connecter à la base de données :");
            e.printStackTrace();
            this.getLogger().severe("Arrêt du serveur...");
            Bukkit.shutdown();
            return;
        }
        this.registerRecipes();
        Step.setCurrentStep(Step.LOBBY);
        World world = Bukkit.getWorlds().get(0);
        Location spawnLoc = new Location(world, 0, 0, 0);
        world.getChunkAt(spawnLoc).load(true);
        world.setDifficulty(Difficulty.NORMAL);
        world.getPopulators().add(new UHCOrePopulator());
        spawnLoc.setY(world.getHighestBlockYAt(0, 0) + 36);
        for (int x = -9; x <= 9; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -9; z <= 9; z++) {
                    boolean side = z == 9 || z == -9 || x == 9 || x == -9;
                    if (y == -1) {
                        Block block = spawnLoc.clone().add(x, y, z).getBlock();
                        block.setType(Material.STAINED_GLASS);
                        block.setData(side ? DyeColor.WHITE.getWoolData() : DyeColor.values()[random.nextInt(DyeColor.values().length)].getWoolData());
                    } else if (side) {
                        Block block = spawnLoc.clone().add(x, y, z).getBlock();
                        block.setType(Material.STAINED_GLASS_PANE);
                        block.setData(random.nextBoolean() ? DyeColor.WHITE.getWoolData() : DyeColor.BLACK.getWoolData());
                    }
                }
            }
        }
        try {
            CuboidClipboard clipBoard = SchematicFormat.MCEDIT.load(new File(this.getDataFolder(), "nether.schematic"));
            for (int i = 0; i < 20; i++) {
                Location location = BeginCountdown.getRandomLocation(world);
                location.setY(39);
                int blockX = location.getBlockX();
                int blockZ = location.getBlockZ();
                clipBoard.paste(new EditSession(BukkitUtil.getLocalWorld(world), Integer.MAX_VALUE), BukkitUtil.toVector(location), false);
                this.getLogger().info("Structure générée en X : " + blockX + " | Z : " + blockZ);
                for (int x = 0; x < clipBoard.getWidth(); x += 16) {
                    for (int z = 0; z < clipBoard.getLength(); z += 16) {
                        Chunk chunk = new Location(world, blockX - x, 32, blockZ + z).getChunk();
                        for (BlockState blockState : chunk.getTileEntities()) {
                            if (blockState instanceof Chest) {
                                Chest chest = (Chest) blockState;
                                Inventory inv = chest.getInventory();
                                inv.addItem(new ItemStack(Material.NETHER_STALK, random.nextInt(2) + 1));
                                Random random = UHCRunPlugin.instance.random;
                                if (random.nextBoolean()) {
                                    inv.addItem(new ItemStack(Material.IRON_INGOT, random.nextInt(7) + 1));
                                }
                                if (random.nextBoolean()) {
                                    inv.addItem(new ItemStack(Material.GOLD_INGOT, random.nextInt(7) + 1));
                                }
                                if (random.nextInt(4) == 1) {
                                    inv.addItem(new ItemStack(Material.DIAMOND, random.nextInt(3) + 1));
                                }
                                chest.update();
                            }
                        }
                    }
                }
            }
        } catch (IOException | DataException | MaxChangedBlocksException e) {
            e.printStackTrace();
        }
        spawnLocations = new ArrayList<>();
        for (int i = 0; i < Bukkit.getMaxPlayers(); i++) {
            Location location = BeginCountdown.getRandomLocation(world).add(0, 40, 0);
            for (int x = 1; x <= 10; x++) {
                for (int z = 0; z <= 10; z++) {
                    location.clone().add(x, 0, z).getChunk().load(true);
                }
            }
            spawnLocations.add(location);
        }
        world.setSpawnLocation(0, spawnLoc.getBlockY(), 0);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setTime(6000);
        this.clearCommands("kill", "me", "tell");
        this.register(BlockBreak.class, BlockPlace.class, LeavesDecay.class, CreatureSpawn.class, EntityDamage.class, EntityDamageByPlayer.class, EntityDeath.class, EntityExplode.class, EntityRegainHealth.class, EntityTarget.class, FoodLevelChange.class, BrewCraft.class, CraftItem.class, InventoryClick.class, AsyncPlayerChat.class, PlayerCommandPreprocess.class, PlayerDamage.class, PlayerDamageByPlayer.class, PlayerDeath.class, PlayerDropItem.class, PlayerInteract.class, PlayerItemConsume.class, PlayerJoin.class, PlayerKick.class, PlayerLogin.class, PlayerMove.class, PlayerPickupItem.class, PlayerPortal.class, PlayerQuit.class, PlayerRespawn.class, ServerListPing.class, ThunderChange.class, WeatherChange.class, ChunkUnload.class);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @SneakyThrows
    @Override
    public void onLoad() {
        File worldContainer = this.getServer().getWorldContainer();
        FileUtils.delete(new File(worldContainer, "world"));
        FileUtils.delete(new File(worldContainer, "world_nether"));
        FileUtils.delete(new File(worldContainer, "world_the_end"));
        List<Integer> excludes = Arrays.asList(0, 7, 8, 9, 11, 24);
        BiomeChanger.changeBiome(0, this.getRandomWithExcludes(excludes));
        BiomeChanger.changeBiome(7, this.getRandomWithExcludes(excludes));
        BiomeChanger.changeBiome(11, this.getRandomWithExcludes(excludes));
        BiomeChanger.changeBiome(24, this.getRandomWithExcludes(excludes));
    }

    private void registerRecipes() {
        ShapedRecipe speckledMelon = new ShapedRecipe(new ItemStack(Material.SPECKLED_MELON));
        speckledMelon.shape(new String[] { "GGG", "GMG", "GGG" });
        speckledMelon.setIngredient('G', Material.GOLD_INGOT);
        speckledMelon.setIngredient('M', Material.MELON);
        Bukkit.addRecipe(speckledMelon);
        ShapedRecipe compass = new ShapedRecipe(new ItemStack(Material.COMPASS));
        compass.shape(new String[] { "CIE", "IRI", "BIF" });
        compass.setIngredient('C', Material.SULPHUR);
        compass.setIngredient('I', Material.IRON_INGOT);
        compass.setIngredient('E', Material.SPIDER_EYE);
        compass.setIngredient('R', Material.REDSTONE);
        compass.setIngredient('B', Material.BONE);
        compass.setIngredient('F', Material.ROTTEN_FLESH);
        Bukkit.addRecipe(compass);
        List<Recipe> toAdd = new ArrayList<>();
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while (recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if (recipe instanceof ShapedRecipe && craftUpgrades.containsKey(recipe.getResult().getType())) {
                recipes.remove();
                ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
                ShapedRecipe newRecipe = new ShapedRecipe(new ItemStack(craftUpgrades.get(recipe.getResult().getType())));
                newRecipe.shape(shapedRecipe.getShape());
                for (Entry<Character, ItemStack> ingredient : shapedRecipe.getIngredientMap().entrySet()) {
                    if (ingredient.getValue() != null) {
                        newRecipe.setIngredient(ingredient.getKey(), ingredient.getValue().getData());
                    }
                }
                toAdd.add(newRecipe);
            }
        }
        for (Recipe recipe : toAdd) {
            Bukkit.addRecipe(recipe);
        }
    }

    public Location getRandomSpawn() {
        return spawnLocations.remove(random.nextInt(spawnLocations.size()));
    }

    private int getRandomWithExcludes(List<Integer> excludes) {
        int n = random.nextInt(40);
        while (excludes.contains(n)) {
            n = random.nextInt(40);
        }
        return n;
    }

    public boolean isSpectator(Player player) {
        return player.hasMetadata("SPECTATOR") || Team.getPlayerTeam(player) == null;
    }

    public boolean isInBorder(Location origin, Location loc) {
        double x = loc.getX(), z = loc.getZ();
        double halfMapSize = GameRunnable.mapSize / 2;
        return !(x < origin.clone().add(-halfMapSize, 0.0D, 0.0D).getBlockX() || x > origin.clone().add(halfMapSize, 0.0D, 0.0D).getBlockX() || z < origin.clone().add(0.0D, 0.0D, -halfMapSize).getBlockZ() || z > origin.clone().add(0.0D, 0.0D, halfMapSize).getBlockZ());
    }

    public void setSpectator(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setMetadata("SPECTATOR", new FixedMetadataValue(this, true));
        player.setAllowFlight(true);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (player != online) {
                player.showPlayer(online);
                if (!this.isSpectator(online)) {
                    online.hidePlayer(player);
                }
            }
        }
    }

    public void loadData(Player player) {
        ScoreboardBuilder builder = ScoreboardBuilder.builder("uhc", ChatColor.DARK_GRAY + "[-" + ChatColor.YELLOW + "UHCRun" + ChatColor.DARK_GRAY + "-]", DisplaySlot.SIDEBAR);
        if (Step.isStep(Step.LOBBY)) {
            builder.setLine(6, ChatColor.GRAY + "--- Timers ---");
            if (!BeginCountdown.started) {
                builder.setLine(5, ChatColor.GREEN + "Attente");
            } else {
                int remainingMins = BeginCountdown.timeUntilStart / 60 % 60;
                int remainingSecs = BeginCountdown.timeUntilStart % 60;
                builder.setLine(5, ChatColor.RED + "Début du jeu");
                builder.setLine(4, ChatColor.RED + "dans " + ChatColor.GRAY + (remainingMins > 0 ? remainingMins + "min" : "") + (remainingSecs > 0 ? (remainingMins > 0 ? " " : "") + remainingSecs + "s" : ""));
            }
            Objective health = builder.getScoreboard().registerNewObjective("health", "health");
            health.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            player.setScoreboard(builder.getScoreboard());
        }
        UHCRunPlugin.this.data.put(player.getUniqueId(), new PlayerData(player.getUniqueId(), player.getName(), 0, builder));
    }

    public PlayerData getData(Player player) {
        PlayerData data = this.data.get(player.getUniqueId());
        if (data == null) {
            player.kickPlayer(ChatColor.RED + "Erreur");
            return null;
        }
        return data;
    }

    @SneakyThrows
    private void clearCommands(final String... command) {
        final CommandMap commandMap = (CommandMap) ReflectionHandler.getField(Bukkit.getServer().getClass(), true, "commandMap").get(Bukkit.getServer());
        final Field knownCommands = ReflectionHandler.getField(commandMap.getClass(), true, "knownCommands");
        final Map<String, Command> commands = (Map<String, Command>) knownCommands.get(commandMap);
        new BukkitRunnable() {

            @Override
            @SneakyThrows
            public void run() {
                List<String> commandNames = Arrays.asList(command);
                for (Entry<String, Command> entry : new HashMap<>(commands).entrySet()) {
                    if (commandNames.contains(entry.getValue().getName())) {
                        commands.remove(entry.getKey());
                    }
                }
                knownCommands.set(commandMap, commands);
            }
        }.runTaskLater(this, 1);
    }

    public void onPlayerLoose(Player player) {
        if (Step.isStep(Step.LOBBY)) {
            data.remove(player.getUniqueId());
        }
        final Team team = Team.getPlayerTeam(player);
        if (team != null) {
            team.removePlayer(player);
            if ((Step.isStep(Step.TELEPORTATION) || Step.isStep(Step.IN_GAME)) && !Team.allTeams.contains(team)) {
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.GRAY + "L'équipe " + team.getDisplayName() + ChatColor.GRAY + " a perdue !");
                    }
                }.runTaskLater(this, 1);
                if (Team.allTeams.size() == 1) {
                    final Team winnerTeam = Team.allTeams.get(0);
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(UHCRunPlugin.prefix + ChatColor.GOLD + ChatColor.BOLD + "Victoire de l'équipe " + ChatColor.BOLD + winnerTeam.getDisplayName() + " " + ChatColor.YELLOW + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|" + ChatColor.YELLOW + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.BOLD + " Félicitations " + ChatColor.YELLOW + ChatColor.MAGIC + " |" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|" + ChatColor.YELLOW + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|");
                        }
                    }.runTaskLater(this, 1);
                    for (Entry<UUID, PlayerData> entry : data.entrySet()) {
                        PlayerData data = entry.getValue();
                        if (winnerTeam != null) {
                            Player online = Bukkit.getPlayer(entry.getKey());
                            if (online != null && online.isOnline()) {
                                if (Team.getPlayerTeam(online) == winnerTeam) {
                                    data.addCoins(35);
                                } else {
                                    data.addCoins(2);
                                }
                            }
                        }
                    }
                    this.stopGame();
                }
            }
        }
    }

    public void stopGame() {
        Step.setCurrentStep(Step.POST_GAME);
        for (Entry<UUID, PlayerData> entry : data.entrySet()) {
            final String uuid = entry.getKey().toString().replaceAll("-", "");
            final PlayerData data = entry.getValue();
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        ResultSet res = database.querySQL("SELECT name FROM players WHERE uuid=UNHEX('" + uuid + "')");
                        if (res.first()) {
                            database.updateSQL("UPDATE players SET name='" + data.getName() + "', coins=coins+" + data.getCoins() + ", updated_at=NOW() WHERE uuid=UNHEX('" + uuid + "')");
                        } else {
                            database.updateSQL("INSERT INTO players(name, uuid, coins, created_at, updated_at) VALUES('" + data.getName() + "', UNHEX('" + uuid + "'), " + data.getCoins() + ", NOW(), NOW())");
                        }
                    } catch (ClassNotFoundException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(this);
        }
        new BukkitRunnable() {

            @Override
            public void run() {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    UHCRunPlugin.this.teleportToLobby(online);
                }
            }
        }.runTaskLater(UHCRunPlugin.this, 300);
        new BukkitRunnable() {

            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }.runTaskLater(UHCRunPlugin.this, 400);
    }

    public void teleportToLobby(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("lobby");
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    @SneakyThrows
    private void register(Class<? extends UHCRunListener>... classes) {
        for (Class<? extends UHCRunListener> clazz : classes) {
            Bukkit.getPluginManager().registerEvents((Listener) ReflectionHandler.getConstructor(clazz, UHCRunPlugin.class).newInstance(this), this);
        }
    }
}
