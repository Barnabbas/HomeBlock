/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barnabbas.homeblock;

import java.util.List;
import java.util.Random;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.getspout.spoutapi.material.block.GenericCubeCustomBlock;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * A Block that will teleport you to a random home.
 *
 * @author Stef van Schuylenburg
 * @since 22-dec-2011
 */
public class RandomHomeBlock extends GenericCubeCustomBlock {
    
    /**
     * The difference that should be added to a the Lcation of a HomeBlock
     * we want to teleport to.
     * @see HomeTeleporter#TELEPORT_VECTOR
     */
    protected Vector TELEPORT_VECTOR = HomeTeleporter.TELEPORT_VECTOR;
    
    /**
     * The HomeMap used by this RandomHomeBlock to get the homes
     */
    private final HomeMap homeMap;
    
    /**
     * The Random used to choose a Home to teleport to
     */
    private final Random random;
    
    /**
     * Constructs a new RandomHomeBlock that will teleport a player to a random
     * home from {@code HomeMap}.
     * @param plugin the Plugin using this RandomHomeBlock
     * @param name the name of the item
     * @param texture the url of the texture
     * @param homeMap the HomeMap to use (currently doesnt matter which one :\)
     */
    public RandomHomeBlock(Plugin plugin, String name, String texture, HomeMap homeMap){
        super(plugin, name, texture, 16);
        
        this.homeMap = homeMap;
        random = new Random();
    }

    /**
     * Teleports {@code player to a random HomeBlock)
     * @param world
     * @param x
     * @param y
     * @param z
     * @param player
     * @return true
     */
    @Override
    public boolean onBlockInteract(World world, int x, int y, int z, SpoutPlayer player) {
        
        world.playEffect(new Location(world, x, y, z), Effect.GHAST_SHOOT, 1);
        
        List<Location> homes = homeMap.getHomes();
        
        if (homes.isEmpty()){ // there are no homes
            // teleporting atop of the block
            Location here = new Location(world, x, y, z);
            here.add(TELEPORT_VECTOR);
            player.teleport(here);
        }
        
        else { // there are homes
            assert homes.size() > 0;
            
            // chosing home
            int r = random.nextInt(homes.size());
            Location home = homes.get(r);
            
            // teleporting
            home.add(TELEPORT_VECTOR);
            player.teleport(home);
        }
        
        return true;
    }
    
    
}
