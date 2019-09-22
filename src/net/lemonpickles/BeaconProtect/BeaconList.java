package net.lemonpickles.BeaconProtect;

import net.lemonpickles.util.FileMgmt;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class BeaconList extends FileMgmt {
    private BeaconProtect plugin;
    public BeaconList(BeaconProtect plugin){
        super(plugin, "beacons.yml");
        this.plugin = plugin;
        FileConfiguration beacons = super.getConfig();
        if(super.getConfig().getList("beacons")==null){
            beacons.createSection("beacons");
        }
        super.save();
    }
    public void save(){
        ArrayList<String> locs = new ArrayList<>();
        for(Map.Entry<Location, Block> entry:plugin.beacons.entrySet()){
            Location location = entry.getKey();
            locs.add("["+location.getBlockX()+", "+location.getBlockY()+", "+location.getBlockZ()+"]");
        }
        super.getConfig().set("beacons",locs);
        super.save();
    }
    public void load(){
        super.load();
        plugin.beacons.clear();
        //Convert each string [0,0,0] to a Location
        for(String rawData:super.getConfig().getStringList("beacons")){
            rawData = rawData.substring(1, rawData.length() - 1);
            int[] loc = new int[3];
            int i = 0;
            for(String coord:rawData.split(",")) {
                loc[i] = Integer.parseInt(coord.trim());
                i++;
            }
            Location location = new Location(getServer().getWorld("world"), loc[0], loc[1], loc[2]);
            plugin.beacons.put(location,location.getBlock());
        }

    }
}
