/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barnabbas.homeblock;

import com.avaje.ebean.EbeanServer;
import java.util.ArrayList;
import java.util.LinkedList;
import me.barnabbas.homeblock.persistence.HomeLocation;
import me.barnabbas.homeblock.persistence.PlayersHome;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * A Map to get the location of the Homes of the players.
 *
 * @author Stef van Schuylenburg
 * @since 19-dec-2011
 */
public class HomeMap {
    
    /**
     * name used to identify this HomeMap
     */
    private final String homeMapName;
    
    private final EbeanServer database;
    
    /**
     * The Plugin using this HomeMap
     */
    private final Plugin plugin;
    
    /**
     * Location that is used when no Home is set.<br>
     * Or {@code null} when there is no default.
     */
    private Location defaultLocation;
    
    
    /**
     * Constructs a new HomeMap with name as identifieer.
     * @param name an unique name to identify this HomeMap
     * @param plugin the Plugin using this HomeMap
     */
    public HomeMap(String name, Plugin plugin){
        homeMapName = name;
        this.database = plugin.getDatabase();
        this.plugin = plugin;
    }
    
    /**
     * Gets the Home location of {@code player} according to this Map.<br>
     * Returns {@code null} when there is no Home set for {@code player} and 
     * when there is no default set.
     * @param player the Player to get the HomeLocation of
     * @return the Home location of {@code player} or {@code null} when there is
     * no Home set yet.
     * 
     * @throws IllegalArgumentException if the Home of {@code player} is in an 
     * unknown World
     */
    public Location getHome(SpoutPlayer player) throws IllegalArgumentException{
        String name = player.getName();
        
        // getting his Home
        HomeLocation home = findHomeOf(name);
        
        if (home == null){ // no Home set
            return defaultLocation;
        } else { // there is a home
            World world = plugin.getServer().getWorld(home.getWorld());
            
            //when world is not found by the Server
            if (world == null){
                throw new IllegalArgumentException("Your Home is an unknown "
                        + "world, namely " + home.getWorld());
            }
            
            return new Location(world, home.getX(), home.getY(), home.getZ());
        }
    }
    
    /**
     * Sets the Home of {@code player} to {@code location}.<br>
     * Old Home will be removed.
     * @param player the Player to set the home of
     * @param location the new location of Players Home
     * @throws NullPointerException if {@code location == null || player == null}
     */
    public void setHome(SpoutPlayer player, Location location) 
                    throws NullPointerException {
        
        if (location == null){
            throw new NullPointerException("Can not set a home to location null!");
        }
        
        String name = player.getName();
        
        // checking if there is already such a location
        HomeLocation hl = findHomeLocation(location);
        if (hl == null){ // no home yet here
            hl = createHomeLocation(location);
        }
        
        assert hl != null;
        
        // setting the players home
        setPlayersHome(name, hl);
    }
    
    /**
     * Sets the Home location of the Players that have not set a home yet.<Br>
     * Players will be teleported to {@code location} when they have not set a 
     * Home, or when their home is removed.<br>
     * When {@code location == null} the default location will be removed.
     * @param location the new Location to use as default.
     * @throws UnsupportedOperationException if default is not supported by this
     * implementation.
     */
    public void setDefault(Location location) throws UnsupportedOperationException {
        
        // take clone to create immutability
        if (location != null){
            location = location.clone();
        }
        
        this.defaultLocation = location;
    }
    
    /**
     * Will set the Home of all players with their Home at {@code location} to 
     * unset again.
     * @param location the Location to remove as Home.
     */
    public void removeHome(Location location) {
        HomeLocation hl = findHomeLocation(location);
        
        if (hl == null){ // there is no home set at location
            // do nothing :)
        } else { // there are one or more homes set to location
            List<PlayersHome> homes = database.find(PlayersHome.class).
                    where().eq("locationId", hl.getId()).findList();
            
            // delete all entries
            database.delete(homes);
            database.delete(hl);
        }
    }
    
    /**
     * All the Locations of the Homes set to this HomeMap.
     * @return All the Locations of the Homes set to this HomeMap.
     */
    public List<Location> getHomes(){
        
        // getting HomeLocations
        List<PlayersHome> phs = database.find(PlayersHome.class).
                where().ieq("homeMap", homeMapName).findList();
        List<Integer> locationIds = new LinkedList<Integer>();
        for (PlayersHome ph: phs){
            locationIds.add(ph.getLocationId());
        }
        List<HomeLocation> homeLocations = database.find(HomeLocation.class).
                where().in("id", locationIds).findList();
        
        // getting them as Locations
        List<Location> locations = new ArrayList<Location>();
        for (HomeLocation hl: homeLocations){
            World world = plugin.getServer().getWorld(hl.getWorld());
            
            if (world != null){
                locations.add(new Location(world, hl.getX(), hl.getY(), hl.getZ()));
            }
        }
        
        return locations;
    }
    
    
    /* auxilary methods */
    
    
    /**
     * Will add a new HomeLocation to the database based on Location 
     * {@code loc}.
     * @param loc the Location containing the values of the new HomeLocation
     * @return the HomeLocation that has been added to the database.
     */
    private HomeLocation createHomeLocation(Location loc){
        HomeLocation home = new HomeLocation();
        
        home.setX(loc.getBlockX());
        home.setY(loc.getBlockY());
        home.setZ(loc.getBlockZ());
        home.setWorld(loc.getWorld().getName());
        
        database.save(home);
        return home;
    }    
    
    /**
     * search through the database to find a HomeLocation with attributes 
     * {@code location}. If there is none it will return {@code null}.
     * @param location the Location to search for
     * @return a HomeLocation with the same attributes as {@code location} or 
     * {@code null} when it couldnt find one.
     */
    private HomeLocation findHomeLocation(Location location){
        return database.find(HomeLocation.class).
                where().eq("x", location.getX()).
                        eq("y", location.getY()).
                        eq("z", location.getZ()).
                        eq("world", location.getWorld().getName()).findUnique();
    }
    
    /**
     * Gives the HomeLocation of players home according to this Map.<br>
     * If he has no Home {@code null} will be returned.
     * @param player the name of the Player to find the home of
     * @return the HomeLocation of {@code player} or {@code null} when he has 
     * none.
     */
    private HomeLocation findHomeOf(String player){
        // getting his Home
        PlayersHome playersHome = database.find(PlayersHome.class).
                where().ieq("player", player).
                        ieq("homeMap", homeMapName).findUnique();
        
        if (playersHome == null){ // player has no Home
            return null;
        } else { // player has a home
            return database.find(HomeLocation.class).
                    where().idEq(playersHome.getLocationId()).findUnique();
        }
    }
    
    /**
     * Will set into the database that {@code player} has a Home at {@code home}.
     * @param player the name of the player
     * @param home the new home of the player
     */
    private void setPlayersHome(String player, HomeLocation home){
        PlayersHome ph = database.find(PlayersHome.class).
                where().ieq("player", player).
                        ieq("homeMap", homeMapName).findUnique();
        
        if (ph == null){ // no home for player yet
            // creating new PlayersHome
            ph = new PlayersHome();
            
            ph.setHomeMap(homeMapName);
            ph.setLocationId(home.getId());
            ph.setPlayer(player);
            
            database.save(ph);
        } 
        else { // there is already a home
            ph.setLocationId(home.getId());
            
            database.save(ph);
        }
    }
}
