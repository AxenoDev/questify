package fr.axeno.prisoncraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.axeno.prisoncraft.commands.RegionsCommands;
import fr.axeno.prisoncraft.events.PlayerJoin;
import fr.axeno.prisoncraft.events.PlayerQuit;
import fr.axeno.prisoncraft.methods.regions.RegionManager;

/*
 * prisoncraft java plugin
 * by Axeno_Off
 */
public class Main extends JavaPlugin {
    private static final Logger LOGGER=Logger.getLogger("prisoncraft");


    public static Main instance;
    public static boolean activitiesEnable = true;
    public static BossBar bossBar;
    public static int countdown = 300;
    public static int currentActivityIndex = 0;
    public static int activityIndex = 0;
    public List<String> activities = new ArrayList<>();
    public List<String> getActivities = new ArrayList<>();
    public List<RegionManager> regions;


    public void onEnable() {
    
        instance = this;

        List activitiesList = getConfig().getList("activities.list");
        
        activities.addAll(activitiesList);

        new RegionsCommands(this);

        scheduleActivityTask();

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
        
        LOGGER.info("prisoncraft enabled");

        saveDefaultConfig();
    }

    public void onDisable() {
        bossBar.removeAll();
        LOGGER.info("prisoncraft disabled");
    }

    public static Main getInstance() {
        return instance;
    }

    public void scheduleActivityTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(activitiesEnable) {
                    Bukkit.broadcastMessage("Nouvelle activité disponible: " + activities.get(activityIndex));
                    bossBarFunction();
                    
                    activityIndex++;
                    
                    if(activityIndex >= activities.size()) {
                        activityIndex = 0;
                    }

                    countdown = 300;
                }
            }
        }.runTaskTimer(this, 0, 5 * 60 * 20);
        new BukkitRunnable() {
            @Override
            public void run(){
                updateBossBarProgress();
            }
        }.runTaskTimer(this, 0, 20);
    }

    private void bossBarFunction() {
        if(bossBar == null) {
            bossBar = Bukkit.createBossBar("Activité: " + activities.get(activityIndex), getBarColor(activityIndex), BarStyle.SOLID);
            for(Player player : Bukkit.getOnlinePlayers()) {
                bossBar.addPlayer(player);
            }
        } else {
            bossBar.setTitle("Activité: " + activities.get(activityIndex));
            bossBar.setColor(getBarColor(activityIndex));
        }
    }

    private BarColor getBarColor(int index) {
        switch (index) {
            case 0:
                return BarColor.PINK;
            case 1:
                return BarColor.BLUE;
            case 2:
                return BarColor.GREEN;
            case 3:
                return BarColor.RED;
            case 4:
                return BarColor.WHITE;
            default:
                return BarColor.PINK;
        }
    }

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

}
