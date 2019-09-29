package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BeaconProtect extends JavaPlugin {
    public Map<Location, Block> beacons = new HashMap<>();
    public Map<Location, BlockDurability> durabilities = new HashMap<>();
    public Map<Player, BossBar> durabilityBars = new HashMap<>();
    public Map<Material, DefaultBlockDurability> defaultBlockDurabilities = new HashMap<>();
    public ArrayList<Player> isReinforcing = new ArrayList<>();
    public DefaultBlockDurability defaultBlockDurability;//= new DefaultBlockDurability(1,1);//set to 1 in case the config can't be read
    public int[] defaultBeaconRange = new int[4];
    public int[] defaultBeaconMultiplier = new int[4];
    public DurabilityBar DurabilityBar;
    public BeaconList beaconList;
    public CustomBeacons CustomBeacons;
    public BeaconEvent beaconEvent;
    public DurabilityList durabilityList;
    public BlockDurability BlockDurability;
    public Logger logger = getLogger();
    @Override
    public void onEnable(){
        //config
        this.saveDefaultConfig();
        if(!getDataFolder().exists()){
            getDataFolder().mkdirs();
        }
        getConfig().options().copyDefaults(true);
        saveConfig();
        //defaults
        defaultBlockDurability = new DefaultBlockDurability(getConfig().getInt("default_durability"), getConfig().getInt("default_max_durability"));
        defaultBeaconRange[0] = getConfig().getInt("beacon_tiers.tier1.range");
        defaultBeaconRange[1] = getConfig().getInt("beacon_tiers.tier2.range");
        defaultBeaconRange[2] = getConfig().getInt("beacon_tiers.tier3.range");
        defaultBeaconRange[3] = getConfig().getInt("beacon_tiers.tier4.range");
        defaultBeaconMultiplier[0] = getConfig().getInt("beacon_tiers.tier1.reinforce");
        defaultBeaconMultiplier[1] = getConfig().getInt("beacon_tiers.tier2.reinforce");
        defaultBeaconMultiplier[2] = getConfig().getInt("beacon_tiers.tier3.reinforce");
        defaultBeaconMultiplier[3] = getConfig().getInt("beacon_tiers.tier4.reinforce");
        //defaultBlockDurability.setMaxBlockDurability(defaultDur);
        logger.info("Loaded global default durability of "+defaultBlockDurability.getDefaultBlockDurability()+" and max durability of "+defaultBlockDurability.getMaxBlockDurability());
        //build defaultDurabilities
        //yaml with bukkit sucks
        List<String> durs = getConfig().getStringList("block_durabilities");
        for(String line:durs){
            String[] split = line.split(":",2);
            String[] split2 = split[1].split(",",2);//can have 2 args, default and max
            String mat = split[0].replaceAll("\\s","");//material
            int dur = Integer.parseInt(split2[0].replaceAll("\\s",""));//durability

            Material material = Material.getMaterial(split[0].replaceAll("\\s",""));
            if(material==null){
                logger.warning("Could not convert "+mat+" to a Bukkit material");
            }else{
                if(split2.length==2){
                    defaultBlockDurabilities.put(material, new DefaultBlockDurability(dur, Integer.parseInt(split2[1].replaceAll("\\s",""))));//max durability
                }else{
                    defaultBlockDurabilities.put(material, new DefaultBlockDurability(dur));
                }
            }
        }
        logger.info("Loaded "+defaultBlockDurabilities.size()+" materials with a default durability");
        //initialize
        DurabilityBar = new DurabilityBar(this);
        //initialize beacons from file
        beaconList = new BeaconList(this);
        beaconList.load();
        logger.info("Loaded "+beacons.size()+" protection beacons");
        //initialize durabilities from file
        durabilityList = new DurabilityList(this);
        durabilityList.load();
        logger.info("Loaded "+durabilities.size()+" blocks with a set durability");
        //CustomBeacons event
        CustomBeacons = new CustomBeacons(this);
        CustomBeacons.startBeacons();
        logger.info("Started updating all beacons");
        //block events
        beaconEvent = new BeaconEvent(this);
        //commands
        getCommand("beacon").setExecutor(new BeaconCmd(this));

        //done
        logger.info("BeaconProtect has been enabled");
    }

    @Override
    public void onDisable(){
        CustomBeacons.stopBeacons();
        beaconList.save();
        durabilityList.save();
        logger.info("Saved beacons and blocks with a set durability to file");
        getLogger().info("BeaconProtect has been disabled");
    }

}
