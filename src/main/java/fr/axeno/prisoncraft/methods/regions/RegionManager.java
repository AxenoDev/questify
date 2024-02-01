package fr.axeno.prisoncraft.methods.regions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class RegionManager {
    
    public Location minLoc, maxLoc;
    public String name;

    public RegionManager(Location firstPoint, Location secondPoint, String name) {
        minLoc = new Location(firstPoint.getWorld(), min(firstPoint.getX(), secondPoint.getX()), min(firstPoint.getY(), secondPoint.getY()), min(firstPoint.getZ(), secondPoint.getZ()));
        maxLoc = new Location(firstPoint.getWorld(), max(firstPoint.getX(), secondPoint.getX()), max(firstPoint.getY(), secondPoint.getY()), max(firstPoint.getZ(), secondPoint.getZ()));
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double min(double a, double b) {
        return a < b ? a : b;
    }

    public double max(double a, double b) {
        return a > b ? a : b;
    }

    public List<Location> getArea() {
        List<Location> blocksLocation = new ArrayList<>();

        for(int x = minLoc.getBlockX(); x <= maxLoc.getBlockX(); x++) {
            for(int z = minLoc.getBlockZ(); z <= maxLoc.getBlockZ(); z++) {
                for(int y = minLoc.getBlockY(); y <= maxLoc.getBlockY(); y++)
                blocksLocation.add(new Location(minLoc.getWorld(), x, y, z));
            }
        }
        return blocksLocation;
    }

    @Override
    public String toString() {
        return "RegionManager{" +
                "name='" + name + '\'' +
                "}";
        
    }

}
