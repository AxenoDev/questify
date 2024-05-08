package fr.axeno.questify.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.axeno.questify.Main;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        Main.bossBar.removePlayer(player);
    }
    
}
