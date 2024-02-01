package fr.axeno.prisoncraft.methods.regions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.axeno.prisoncraft.Main;
import fr.axeno.prisoncraft.methods.players.GamePlayer;

public class ClaimManager {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new GamePlayer(event.getPlayer().getName());
    }
    
    public void updateRegionList(RegionManager oldRegion, RegionManager newRegion) {
        Main.getInstance().regions.remove(oldRegion);
        Main.getInstance().regions.add(newRegion);
    }

}
