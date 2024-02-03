package fr.axeno.prisoncraft;

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
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import fr.axeno.prisoncraft.events.PlayerQuit;

/*
 * prisoncraft java plugin
 * by Axeno_Off
*/
public class Main extends JavaPlugin {
    
    private static final Logger LOGGER = Logger.getLogger("prisoncraft");

    public static Main instance;
    public static boolean activitiesEnable = true;
    public static BossBar bossBar;
    public static int countdown = 300;
    public static int currentActivityIndex = 0;
    public static int activityIndex = 0;
    public static List<String> activities = new ArrayList<>();
    public static List<String> getActivities = new ArrayList<>();
    public static ConfigRegions configMsg;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onEnable() {

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            getLogger().warning("WorldGuard n'est pas installé !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    
        instance = this;
        this.configMsg = new ConfigRegions(this, "messages.yml");
        saveDefaultConfig();        
        

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

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new MainListener(), this);
        
        LOGGER.info("prisoncraft enabled");

    }

    public void onDisable() {
        bossBar.removeAll();
        LOGGER.info("prisoncraft disabled");
    }

    public static Main getInstance() {
        return instance;
    }

    private class MainListener implements org.bukkit.event.Listener {
        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();

            World world = player.getWorld();
            com.sk89q.worldedit.Vector playerVector = BukkitUtil.toVector(player.getLocation());
            ApplicableRegionSet regions = WGBukkit.getRegionManager(world).getApplicableRegions(playerVector);

            if(isOnRegion(player, regions, Main.getInstance().getConfig().getString("regions.name"))) {
                bossBar.addPlayer(player);
            } else {
                bossBar.removePlayer(player);
            }
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();

            World world = player.getWorld();
            Vector playerVector = BukkitUtil.toVector(player.getLocation());
            ApplicableRegionSet regions = WGBukkit.getRegionManager(world).getApplicableRegions(playerVector);

            if(isOnRegion(player, regions, Main.getInstance().getConfig().getString("regions.name"))) {
                bossBar.addPlayer(player);
            }
            
        }
    }

    public boolean isOnRegion(Player player, ApplicableRegionSet regions, String nomRegion) {
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
                        Vector playerVector = BukkitUtil.toVector(player.getLocation());
                        ApplicableRegionSet regions = WGBukkit.getRegionManager(player.getWorld()).getApplicableRegions(playerVector);

                        if(isOnRegion(player, regions, getConfig().getString("regions.name"))) {
                            player.sendMessage(configMsg.getMessage("newactivity").replace("{activity}", activities.get(activityIndex)));
                        }

                    }
                    bossBarFunction();
                    
                    activityIndex++;
                    
                    if(activityIndex >= activities.size()) {
                        activityIndex = 0;
                    }

                    countdown = 300;
                }
            }
        }.runTaskTimer(this, 0, getConfig().getInt("activities.time") * 60 * 20);
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
                        getLogger().warning("Couleur invalide pour l'activité: " + activityName);
                        return BarColor.PINK;
                    }
                }
            }
        }

        return BarColor.PINK;
    }

    // private BarColor getBarColor(int index) {
    //     switch (index) {
    //         case 0:
    //             return BarColor.PINK;
    //         case 1:
    //             return BarColor.BLUE;
    //         case 2:
    //             return BarColor.GREEN;
    //         case 3:
    //             return BarColor.RED;
    //         case 4:
    //             return BarColor.WHITE;
    //         default:
    //             return BarColor.PINK;
    //     }
    // }

    private void updateBossBarProgress() {
        if(bossBar != null) {
            double progress = (double) countdown / 300;
            bossBar.setProgress(progress);

            countdown--;

            if(countdown <= 0) {
                bossBar.removeAll();
                bossBar = null;
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
            String messages = configMsg.get().getString(value).replace("&", "§");
            return messages;
        }
    }
}