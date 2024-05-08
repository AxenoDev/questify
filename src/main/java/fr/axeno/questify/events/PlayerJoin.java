package fr.axeno.questify.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.axeno.questify.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(player.getWorld());
        Location playerLocation = player.getLocation();
        BlockVector3 playerVector3 = BlockVector3.at(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        ApplicableRegionSet regions = container.get(world).getApplicableRegions(playerVector3);

        if(Main.isOnRegion(player, regions, Main.getInstance().getConfig().getString("regions.name")) || !Main.REGION_ENABLE) Main.bossBar.addPlayer(player);


    }

}
