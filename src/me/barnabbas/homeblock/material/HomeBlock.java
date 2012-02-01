/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barnabbas.homeblock.material;

import me.barnabbas.homeblock.HomeMap;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.material.block.GenericCubeCustomBlock;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * A Block that will let you set your home using right-click.
 * 
 * @author Stef van Schuylenburg
 * @since 1-dec-2011
 */
public class HomeBlock extends GenericCubeCustomBlock {
    
    /**
     * The HomeMap to set the HomeLocations in.
     */
    private final HomeMap homeMap;
    
    /**
     * Creates a new HomeBlock that will set the Homes of players in 
     * {@code homeMap}.
     * 
     * @param plugin the Plugin that is using this Block
     * @param name the Name of the block
     * @param texture the texture for this Block
     * @param homeMap the HomeMap to save the Home Locations in.
     */
    public HomeBlock(Plugin plugin, String name, String texture, HomeMap homeMap){
        super(plugin, name, texture, 16);
        
        this.homeMap = homeMap;
    }
    
    /**
     * Removes this Block location from the HomeMap.
     * @param world
     * @param x
     * @param y
     * @param z 
     */
    @Override
    public void onBlockDestroyed(World world, int x, int y, int z) {
        homeMap.removeHome(new Location(world, x, y, z));
    }

    /**
     * Sets the Home of {@code player}
     * @param world
     * @param x
     * @param y
     * @param z
     * @param player the Player to set Home of
     * @return {@code true}
     */
    @Override
    public boolean onBlockInteract(World world, int x, int y, int z, SpoutPlayer player) {
        Location location = new Location(world, x, y, z);
        
        setHome(player, location);
        world.playEffect(location, Effect.DOOR_TOGGLE, 1);
        
        return true;
    }

    /**
     * 
     * @param world
     * @param x
     * @param y
     * @param z
     * @param living 
     */
    @Override
    public void onBlockPlace(World world, int x, int y, int z, LivingEntity living) {
        int entityId = living.getEntityId();
        SpoutPlayer player = SpoutManager.getPlayerFromId(entityId);
        
        // setting home when it is a player
        if (player != null){
            setHome(player, new Location(world, x, y, z));
        }
    }
    
    /**
     * Sets the home Location of {@code player} to {@code location}.
     * @param player the Player to set the Home of
     * @param location the new Location for Players Home
     */
    private void setHome(SpoutPlayer player, Location location){
        player.sendMessage("[HomeBlock]HomeBlock set");
        
        homeMap.setHome(player, location);
    }
    
}
