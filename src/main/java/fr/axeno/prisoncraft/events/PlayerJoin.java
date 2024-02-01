package fr.axeno.prisoncraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.axeno.prisoncraft.Main;

public class PlayerJoin implements Listener {
    
    private Main main;
    public PlayerJoin(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        main.bossBar.addPlayer(player);
    }

}
