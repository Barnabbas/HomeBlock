/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barnabbas.homeblock.material;

import me.barnabbas.homeblock.CooldownMap;
import me.barnabbas.homeblock.HomeBlockPlugin;
import me.barnabbas.homeblock.HomeMap;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.getspout.spoutapi.material.block.GenericCubeCustomBlock;
import org.getspout.spoutapi.player.SpoutPlayer;


/**
 * A Block that let you teleport to your Home by rightClicking.
 *
 * @author Stef van Schuylenburg
 * @since 19-dec-2011
 */
public class HomeTeleporter extends GenericCubeCustomBlock {
    
    /**
     * The difference that should be added to a the Lcation of a HomeBlock
     * we want to teleport to.
     */
    final static Vector TELEPORT_VECTOR = new Vector(.5, 1, .5);
    
    /**
     * The HomeMap to use to retrieve the Home Locations from Players.
     */
    private final HomeMap homeMap;
    
    /**
     * The CooldownMap used for cooldown times
     */
    private final CooldownMap cooldownMap;
    
    /**
     * Creates a new HomeTeleporter that will teleport a Player to his Home
     * according to the HomeMap.
     * 
     * @param plugin the Plugin that is using this Block
     * @param name the Name of the block
     * @param texture the texture for this Block
     * @param homeMap the HomeMap to retrieve Home Locations from.
     * @param cooldownMap the CooldownMap for the cooldown effect
     */
    public HomeTeleporter(Plugin plugin, String name, String texture,
            HomeMap homeMap, CooldownMap cooldownMap){
        super(plugin, name, texture, 16);
        
        this.homeMap = homeMap;
        this.cooldownMap = cooldownMap;
        
        this.setLightLevel(5);
    }

    /**
     * Teleports {@code player} to his Home. <br>
     * When Home is not set, will teleport to a random close by location.
     * @param world
     * @param x
     * @param y
     * @param z
     * @param player the Player to teleport.
     * @return {@code true}
     */
    @Override
    public boolean onBlockInteract(World world, int x, int y, int z,
            SpoutPlayer player) {
        
        // checking for cool down
        if (!cooldownMap.isEnabled(player)){
            int time = cooldownMap.getTime(player);
            player.sendMessage(HomeBlockPlugin.PLUGIN_NAME + " action ready in "
                    + time + " seconds.");
            return false;
        }
        
        // cooldownMap is enabled for player
        
        world.playEffect(new Location(world, x, y, z), Effect.GHAST_SHOOT, 1);
        
        // players home
        Location home;
        try {
            home = homeMap.getHome(player);
        } catch (IllegalArgumentException illegalArgumentException) {
            // home is in another world :\
            player.sendMessage(illegalArgumentException.getMessage());
            home = null;
        }
        
        
        if (home != null){ // home is set
            home.add(TELEPORT_VECTOR); // setting above HomeBlock
            player.teleport(home);
            
            // starting cooldown timer
            cooldownMap.start(player);
            
        } else { // home is not set
            
            // teleporting right atop of this Block
            Location here = new Location(world, x, y, z);
            here.add(TELEPORT_VECTOR);
            player.teleport(here);
        }
        
        return true;
    }
    
    
    
}
