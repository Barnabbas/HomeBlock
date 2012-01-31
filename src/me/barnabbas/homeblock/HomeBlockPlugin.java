/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barnabbas.homeblock;

import me.barnabbas.homeblock.persistence.HomeLocation;
import me.barnabbas.homeblock.persistence.PlayersHome;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.persistence.PersistenceException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.inventory.SpoutItemStack;
import org.getspout.spoutapi.inventory.SpoutShapedRecipe;
import org.getspout.spoutapi.material.Material;
import org.getspout.spoutapi.material.MaterialData;
import org.omg.IOP.CodecPackage.FormatMismatch;

/**
 * The Main plugin that will be run.
 *
 * @author Stef van Schuylenburg
 */
public class HomeBlockPlugin extends JavaPlugin {

    private static final Logger console = Logger.getLogger("Minecraft");
    /**
     * The Plugin name, should be used as a prefix in messages to 
     * {@code console}.
     */
    static final String PLUGIN_NAME = "[HomeSystem] ";

    @Override
    public void onEnable() {

        // loading/setting configh
        setupConfig();
        setupDatabase();

        // creating home blocks
        loadBlocks();

        console.info(PLUGIN_NAME + "loaded");
    }

    @Override
    public void onDisable() {
        console.info(PLUGIN_NAME + "disabled");
    }

    /**
     * The classes used as Database tables
     * @return The classes used as Database tables
     */
    @Override
    public List<Class<?>> getDatabaseClasses() {
        return Arrays.asList(HomeLocation.class, PlayersHome.class);
    }

    /* Database stuff */
    
    /**
     * Sets the database up and creates one if it is not set up yet
     */
    private void setupDatabase() {
        try {
            super.getDatabase().find(HomeLocation.class).findRowCount();
            super.getDatabase().find(PlayersHome.class).findRowCount();
        } catch (PersistenceException e) {
            console.info(PLUGIN_NAME + "setting up database for Home locations");
            super.installDDL();
        }
    }

    /**
     * Creates a new config file when there is none yet.<br>
     * Will fill uncomplete config files with the default values.
     */
    private void setupConfig() {
        FileConfiguration config = super.getConfig();

        if (!config.isSet("HomeSystems")) { // config is not set yet
            config.options().copyDefaults(true);
            super.saveConfig();
            console.info(PLUGIN_NAME + "config file created.");
        }
    }
    
    /* Block stuff */

    /**
     * Load the blocks that are chosen by the config
     */
    private void loadBlocks() {
        // getting the HomeSystem names
        List<String> enabled = super.getConfig().getStringList("enabled");

        if (enabled == null) { // there is no enabled
            console.info(PLUGIN_NAME + "No enabled HomeSystems found.");
        } else {
            ConfigurationSection homeSystems =
                    super.getConfig().getConfigurationSection("HomeSystems");

            // loading HomeSystems
            for (String homeSysName : enabled) {
                ConfigurationSection homeSystem =
                        homeSystems.getConfigurationSection(homeSysName);
                if (homeSystem == null) { // could not find it
                    console.info(PLUGIN_NAME + "No section found for "
                            + homeSysName + " in the HomeSystems section");
                } else {
                    // adding the home system
                    try {
                        addHomeSystem(homeSystem);
                        console.info(PLUGIN_NAME + homeSysName + " HomeSystem loaded.");
                    } catch (FormatMismatch ex) {
                        console.info(PLUGIN_NAME + "Could not load "
                                + homeSysName + ". Reason: "
                                + ex.getMessage());
                    }
                }

            }
        }
    }

    /**
     * Adds a new HomeSection to this Plugin based on configuration sections.<br>
     * It should be formatted like:<Br>
     * <ul><li> the name of your HomeSystem in the name of the section,
     * must be unique to avoid collisions!</li>
     * <li>a subsection named "HomeBlock" containing the following<br>
     * <ul><li>"name: " + a name for the HomeBlock (this will be the ingame name)</li>
     *     <li>"texture: " + a url for the texture of the HomeBlock</li>
     *     <li>a subsection named "recipe" containing:<br>
     *     <ul><li>"shape: " + a shape made with characters, like abb, b </li>
     *         <li>"set: " + a list containing for each element:
     *                      char used in shape + ", " + the (notchian) name of the material</li>
     *         <li>"setId: " + a list containing for each element:
     *                      char used in shape + ", " + the material id of the material</li>
     * </ul></ul>
     * <li>a subsection named "HomeTeleporter" which is formatted as above</li>
     * </ul>
     * @param section the Section to contain the information for the HomeSystem
     * @throws FormatMismatch if {@code section} is not well formatted to create a
     * HomeSystem of.
     */
    private void addHomeSystem(ConfigurationSection section) throws FormatMismatch {

        // creating HomeMap
        String title = section.getName();
        if (title == null) {
            throw new FormatMismatch("No name for the home system found.");
        }
        HomeMap homeMap = new HomeMap(title, this);

        // getting blocks
        
        // homeblock;
        ConfigurationSection hbSection = 
                section.getConfigurationSection("HomeBlock");
        if (hbSection == null){ // not found!
            throw new FormatMismatch("Could not find the HomeBlock section");
        } else {
            BlockBuilder builder = createBlockBuilder(hbSection);
            HomeBlock homeBlock = new HomeBlock(this, builder.name,
                    builder.texture, homeMap);
            SpoutItemStack result = new SpoutItemStack(homeBlock);
            SpoutShapedRecipe recipe = getRecipe(builder, result);
            SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
        }
        
        // hometeleporter
        ConfigurationSection htSection = 
                section.getConfigurationSection("HomeTeleporter");
        if (htSection == null){
            throw new FormatMismatch("Could not find the HomeTeleporter section");
        } else {
            BlockBuilder builder = createBlockBuilder(htSection);
            HomeTeleporter homeTele = new HomeTeleporter(this, builder.name,
                    builder.texture, homeMap);
            SpoutItemStack result = new SpoutItemStack(homeTele);
            SpoutShapedRecipe recipe = getRecipe(builder, result);
            SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
        }
        
        // randomhome (optional)
        if (section.isSet("RandomHome")){
            ConfigurationSection rhSection =
                    section.getConfigurationSection("RandomHome");
            BlockBuilder builder = createBlockBuilder(rhSection);
            RandomHomeBlock randomHome = new RandomHomeBlock(this, builder.name,
                    builder.texture, homeMap);
            SpoutItemStack result = new SpoutItemStack(randomHome);
            SpoutShapedRecipe recipe = getRecipe(builder, result);
            SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
        }
        
        // homescroll (optional)
        if (section.isSet("HomeScroll")){
            ConfigurationSection hsSection =
                    section.getConfigurationSection("HomeScroll");
            BlockBuilder builder = createBlockBuilder(hsSection);
            HomeScroll homeScroll = new HomeScroll(this, builder.name,
                    builder.texture, homeMap);
            SpoutItemStack result = new SpoutItemStack(homeScroll, 8);
            SpoutShapedRecipe recipe = getRecipe(builder, result);
            SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
        }
    }
    
    /**
     * Creates a Builder from the information in {@code confi}.<br>
     * {@code config} must be formatted like specified in 
     * {@link #addHomeSystem(org.bukkit.configuration.ConfigurationSection)}
     *  HomeBlock is specified, otherwise an FormatMismatch is trown.
     * @param config the Configurations to get the data from.
     * @throws FormatMismatch if {@code config} is not formatted correctly.
     * 
     * @return a BlockBuilder to create a block.
     */
    private BlockBuilder createBlockBuilder(ConfigurationSection config)
            throws FormatMismatch{
        
        BlockBuilder builder = new BlockBuilder();
        
        // getting name and texture
        String name = config.getString("name");
        if (name == null) {
            throw new FormatMismatch("No name found for " + config.getName());
        }
        String texture = config.getString("texture");
        if (texture == null) {
            throw new FormatMismatch("No texture found for " + config.getName());
        }
        
        // setting block data
        builder.name = name;
        builder.texture = texture;
        
        // setting recipe
        ConfigurationSection recipeConfig = 
                config.getConfigurationSection("recipe");
        if (recipeConfig == null) {
            throw new FormatMismatch("No recipe found for " + config.getName());
        }
        try {
            getRecipeData(recipeConfig, builder);
        } catch (FormatMismatch exception) {
            throw new FormatMismatch("Could not create a recipe for " +
                    config.getName() + ". Reason: " + exception.getMessage());
        }
        
        return builder;
    }

    /**
     * Puts the data of the recipe information in {@code builder}.
     * The information is gotten from {@code section}.<br>
     * {@code section must be formatted like specified in 
     * {@link #addHomeSystem(ConfigurationSection)}.
     * @param section the COnfigurationSection to get the information of.
     * @param builder the BlockBuilder to put the recipe data in.
     * @throws FormatMismatch if {@code section} is not formatted like specified.
     */
    private void getRecipeData(ConfigurationSection section,
            BlockBuilder builder) throws FormatMismatch {

        // getting the shape
        String shapeString = section.getString("shape");
        if (shapeString == null) {
            throw new FormatMismatch("Could not find a shape");
        }
        String[] shape = shapeString.split(",");

        builder.shape = shape;

        // getting ids and names for the items
        List<String> setters = section.getStringList("set");
        builder.setters = new HashMap<Character, String>();
        if (setters != null) {
            for (String setter : setters) {
                String[] s = setter.split("=");
                
                if (s.length != 2 || s[0].length() != 1) {
                    throw new FormatMismatch("Invalid set element: " + setter);
                }
                
                builder.setters.put(s[0].charAt(0), s[1].trim());
            }
        }

        setters = section.getStringList("setId");
        builder.idSetters = new HashMap<Character, Integer>();
        if (setters != null) {
            for (String setter : setters) {
                String[] s = setter.split("=");

                if (s.length != 2 || s[0].length() != 1) {
                    throw new FormatMismatch("Invalid set element: " + setter);
                }
                
                builder.idSetters.put(s[0].charAt(0), Integer.valueOf(s[1].trim()));
            }
        }
    }
    
    /**
     * Creates a Recipe build of the information in {@code builder} which will
     * create {@code result}.
     * The returned Recipe is not registered yet.
     * @param builder the BlockBuilder containing the information for the recipe.
     * @param result The ItemStack that will be created.
     * @return a Recipe based on information in {@code builder} that will create
     * {@code result}.
     * @throws IllegalArgumentException if there is a value of a setter in 
     * {@code builder} that is not a material name.
     */
    private SpoutShapedRecipe getRecipe(BlockBuilder builder, SpoutItemStack result)
                throws IllegalArgumentException{
        SpoutShapedRecipe recipe = new SpoutShapedRecipe(result);
        
        // shape
        recipe.shape(builder.shape);
        
        // strings
        for (Entry<Character, String> entry: builder.setters.entrySet()){
            Material mat = MaterialData.getMaterial(entry.getValue());
            
            if (mat == null){
                throw new IllegalArgumentException(entry.getValue() +
                        " is not a Material.");
            }
            
            recipe.setIngredient(entry.getKey(), mat);
        }
        // ids
        for (Entry<Character, Integer> entry: builder.idSetters.entrySet()){
            Material mat = MaterialData.getMaterial(entry.getValue());
            
            if (mat == null){
                throw new IllegalArgumentException(entry.getValue() +
                        " is not a Material.");
            }
            
            recipe.setIngredient(entry.getKey(), mat);
        }
        
        return recipe;
    }
    
    /**
     * A class that will contain the data to be able to fully create a Block.
     */
    private static class BlockBuilder{
        
        /**
         * The name for the block.
         */
        String name;
        
        /**
         * The url of a texture for the block.
         */
        String texture;
        
        /**
         * The shape for the block
         */
        String[] shape;
        
        /**
         * the setters for the recipe materials,
         * using names.
         */
        Map<Character, String> setters;
        /**
         * the setters for the recipe materials,
         * using ids.
         */
        Map<Character, Integer> idSetters;
    }
}
