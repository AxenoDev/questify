package fr.axeno.prisoncraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.axeno.prisoncraft.Main;

public class PlayerQuit implements Listener {

    private Main main;
    public PlayerQuit(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        main.bossBar.removePlayer(player);
    }
    
}
