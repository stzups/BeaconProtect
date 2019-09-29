package net.lemonpickles.BeaconProtect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BeaconEvent implements Listener{
    private BeaconProtect plugin;
    public BeaconEvent(BeaconProtect plugin){
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }



    @EventHandler
    public void blockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if(plugin.CustomBeacons.checkForBlocks(block).size()==0){
            if(block.getType()==Material.BEACON){
                Location location = block.getLocation();
                if(!plugin.beacons.containsKey(location)){
                    plugin.beacons.put(location, block);
                    String msg = "The beacon at " +block.getX()+", "+block.getY()+", "+block.getZ() + " has been registered";
                    player.sendMessage(msg);
                    plugin.logger.info(msg);
                }
            }
        }else {
            event.setCancelled(true);
            player.sendMessage("You cannot place here! This area is protected by a beacon");
        }
    }
    @EventHandler
    public void playerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack stack = player.getInventory().getItemInMainHand();
        boolean reinforce = player.isSneaking()&&stack.getType().isBlock();
        if(plugin.isReinforcing.contains(player)&&!reinforce){
            plugin.isReinforcing.remove(player);
            player.sendMessage("Left block reinforce mode.");
            event.setCancelled(true);
        }else if(event.getHand()== EquipmentSlot.HAND&&event.getAction()==Action.RIGHT_CLICK_BLOCK){
            if(block!=null) {
                if (block.getType() == Material.CHEST) {
                    if (plugin.CustomBeacons.checkForBlocks(block).size()!=0) {
                        event.setCancelled(true);
                        player.sendMessage("You cannot interact here! This area is protected by a beacon");
                    }else if(plugin.durabilities.containsKey(block.getLocation())){
                        int dur = plugin.durabilities.get(block.getLocation()).getDurability();
                        if(dur==1){
                            event.setCancelled(true);
                        }else{player.sendMessage("You cannot interact here! This chest has "+dur+" remaining!");}
                    }
                }
            }
        }else if(event.getAction()==Action.LEFT_CLICK_BLOCK){
            if(reinforce){
                if(plugin.isReinforcing.contains(player)){//in reinforce mode
                    if(stack.getType()==block.getType()){
                        BlockDurability blockDur;
                        if(!plugin.durabilities.containsKey(block.getLocation())){
                            blockDur = new BlockDurability(plugin, block, player, 0);
                        }else{blockDur = plugin.durabilities.get(block.getLocation());}
                        if(blockDur.changeDurability(plugin, player, 1, true)){
                            stack.setAmount(stack.getAmount()-1);
                        }else{
                            player.sendMessage("This block cannot be reinforced anymore!");
                        }
                    }else{
                        player.sendMessage("You must use "+block.getType()+" to reinforce this block");
                    }

                }else{//set to reinforce mode
                    plugin.isReinforcing.add(player);
                    player.sendMessage("Entered block reinforce mode. Shift+Punch a block to reinforce.");
                }
            }else if(plugin.isReinforcing.contains(player)){
                plugin.isReinforcing.remove(player);
                player.sendMessage("Left block reinforce mode.");
                event.setCancelled(true);
                return;
            }else if(block!=null) {//info click
                if(!plugin.durabilities.containsKey(block.getLocation())){
                    new BlockDurability(plugin, block, player,0);
                }else{plugin.durabilities.get(block.getLocation()).changeDurability(plugin, player,0, false);}
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (!plugin.durabilities.containsKey(block.getLocation())) {//block durability hasn't been set yet
            new BlockDurability(plugin, block, player,-1);
        } else {//block has a durability, take away from it
            plugin.durabilities.get(block.getLocation()).changeDurability(plugin, player, -1, false);
        }
        if (plugin.durabilities.get(block.getLocation()).getDurability() > 0) {
            event.setCancelled(true);
            return;
        }else{
            plugin.durabilityBars.get(player).removeAll();
            plugin.durabilities.remove(block.getLocation());
        }

        //normal block breakage
        if (block.getType() == Material.BEACON) {
            Location location = block.getLocation();
            if (plugin.beacons.containsKey(location)) {
                plugin.beacons.remove(location);
                String msg = "The beacon at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " has been removed";
                player.sendMessage(msg);
                plugin.logger.info(msg);
            }
        }
    }
}
