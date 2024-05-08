package fr.axeno.questify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.axeno.questify.events.PlayerJoin;
import fr.axeno.questify.events.PlayerMove;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import fr.axeno.questify.events.PlayerQuit;

/*
 * prisoncraft java plugin
 * by axenq
*/
public class Main extends JavaPlugin {
    
    private static final Logger LOGGER = Logger.getLogger("prisoncraft");

    public static Main instance;
    public static boolean activitiesEnable = true;
    public static BossBar bossBar;
    public static int countdown;
    public static int currentActivityIndex = 0;
    public static int activityIndex = 0;
    public static List<String> activities = new ArrayList<>();
    public static List<String> getActivities = new ArrayList<>();
    public static ConfigRegions configMsg;
    public static boolean REGION_ENABLE;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onEnable() {

        REGION_ENABLE = getConfig().getBoolean("regions.enable");

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") == null || Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
            getLogger().warning("WorldGuard or WorldEdit isn't installed");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;
        this.configMsg = new ConfigRegions(this, "messages.yml");
        saveDefaultConfig();

        countdown = (getConfig().getInt("activities.time") * 60);

        ConfigurationSection activitiesSection = getConfig().getConfigurationSection("activities.list");
        if (activitiesSection != null) {
            for (String activityKey : activitiesSection.getKeys(false)) {
                String activityName = activitiesSection.getString(activityKey + ".name");
                activities.add(activityName);
            }
        }

        getLogger().info("Activities list size: " + activities.size());
        getLogger().info("Activities list content: " + activities);

        scheduleActivityTask();

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerMove(), this);

        LOGGER.info("Questify is enabled");

    }

    public void onDisable() {
        bossBar.removeAll();
        LOGGER.info("Questify is disabled");
    }

    public static Main getInstance() {
        return instance;
    }

    public static boolean isOnRegion(Player player, ApplicableRegionSet regions, String nomRegion) {
        for(ProtectedRegion region : regions) {
            if(region.getId().equalsIgnoreCase(nomRegion)) {
                return true;
            }
        }

        return false;
    }

    public void scheduleActivityTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(activitiesEnable) {
                    for(Player player : Bukkit.getOnlinePlayers()) {

                        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(player.getWorld());
                        Location playerLocation = player.getLocation();
                        BlockVector3 playerVector3 = BlockVector3.at(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
                        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                        ApplicableRegionSet regions = container.get(world).getApplicableRegions(playerVector3);

                        if(isOnRegion(player, regions, getConfig().getString("regions.name"))) {
                            player.sendMessage(configMsg.getMessage("newactivity").replace("{activity}", activities.get(activityIndex)));
                        }

                    }
                    bossBarFunction();
                    
                    activityIndex++;
                    
                    if(activityIndex >= activities.size()) {
                        activityIndex = 0;
                    }

                    countdown = (getConfig().getInt("activities.time") * 60);
                }
            }
        }.runTaskTimer(this, 0, (getConfig().getInt("activities.time") * 60) * 20);
        new BukkitRunnable() {
            @Override
            public void run(){
                updateBossBarProgress();
            }
        }.runTaskTimer(this, 0, 20);
    }

    private void bossBarFunction() {
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(configMsg.getMessage("bossbarname").replace("{activity}", activities.get(activityIndex)), getActivityColor(activities.get(activityIndex)), BarStyle.SOLID);
            for(Player player : Bukkit.getOnlinePlayers()) {
                bossBar.addPlayer(player);
            }
        } else {
            bossBar.setTitle(configMsg.getMessage("bossbarname").replace("{activity}", activities.get(activityIndex)));
            bossBar.setColor(getActivityColor(activities.get(activityIndex)));
        }
    }

    private BarColor getActivityColor(String activityName) {
        ConfigurationSection activitiesSection = getConfig().getConfigurationSection("activities.list");

        if (activitiesSection != null) {
            for (String activityKey : activitiesSection.getKeys(false)) {
                String name = getConfig().getString("activities.list." + activityKey + ".name");

                if (name != null && name.equalsIgnoreCase(activityName)) {
                    String colorStr = getConfig().getString("activities.list." + activityKey + ".color");
                    try {
                        return BarColor.valueOf(colorStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("Invalid color for activity " + activityName);
                        return BarColor.PINK;
                    }
                }
            }
        }

        return BarColor.PINK;
    }

    private void updateBossBarProgress() {
        if(bossBar != null) {
            double progress = (double) countdown / (getConfig().getInt("activities.time") * 60);
            bossBar.setProgress(progress);

            countdown--;

            if(countdown <= 0) {
                bossBar.removeAll();
            }
        }
    }

    public class ConfigRegions {
        private File file;
        private YamlConfiguration conf;
        public ConfigRegions(JavaPlugin plugin, String fileName) {
            file = new File(Main.getInstance().getDataFolder(), fileName);
            if(!file.exists())
                try {
                    if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
                    InputStream in = plugin.getResource(fileName);
                    if(in != null) {
                        OutputStream out = new FileOutputStream(file);
                        
                        byte[] buf = new byte[1024 * 4];
                        int len = in.read(buf);
                        while (len != -1) {
                            out.write(buf, 0, len);
                            len = in.read(buf);
                        }
                        out.close();
                        in.close();
                    } else file.createNewFile();
                } catch (Exception e) { e.printStackTrace(); }
            reload();
        }

        public void reload() {
            try {
                conf = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            } catch (UnsupportedEncodingException | FileNotFoundException e) { e.printStackTrace(); }
        }

        public YamlConfiguration get() {
            return conf;
        }

        public void save() {
            try {
                conf.save(file);
            } catch (IOException e) { e.printStackTrace(); }
        }

        public String getMessage(String value) {
            return configMsg.get().getString(value).replace("&", "§");
        }
    }
}