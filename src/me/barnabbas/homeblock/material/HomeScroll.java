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
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.getspout.spoutapi.block.SpoutBlock;
import org.getspout.spoutapi.inventory.SpoutItemStack;
import org.getspout.spoutapi.material.Material;
import org.getspout.spoutapi.material.item.GenericCustomItem;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * A HomeScroll is an Item that will teleport you to your Home on using.<br>
 * After using it will dissapear.
 *
 * @author Stef van Schuylenburg
 * @since 27-dec-2011
 */
public class HomeScroll extends GenericCustomItem {

    /**
     * The HomeMap to get the Home from the players of.
     */
    private final HomeMap homeMap;
    
    /**
     * The CooldownMap used for cooldown times
     */
    private final CooldownMap cooldownMap;
    
    /**
     * The Vector that should be added to the home location to teleport atop of
     * the block.
     */
    private static final Vector TELEPORT_VECTOR = HomeTeleporter.TELEPORT_VECTOR;

    /**
     * Creates a new HomeScroll that will teleport you to your Home according
     * to {@code HomeMap}.
     * @param plugin the Plugin using this HomeScroll.
     * @param name the name of the HomeScroll
     * @param texture the url for the texture of this Item
     * @param homeMap the HomeMap to get the Home from.
     * @param cooldownMap the CooldownMap for the cooldown effect
     */
    public HomeScroll(Plugin plugin, String name, String texture,
            HomeMap homeMap, CooldownMap cooldownMap) {
        super(plugin, name, texture);
        this.homeMap = homeMap;
        this.cooldownMap = cooldownMap;
    }

    /**
     * Teleports {@code player} to his home, if he has any.
     * @param player
     * @param block
     * @param face
     * @return if {@code player} can teleport to his home.
     */
    @Override
    public boolean onItemInteract(SpoutPlayer player, SpoutBlock block, BlockFace face) {

        // getting players home
        Location home = homeMap.getHome(player);

        if (home == null) { // no home is found
            return false;
        } else if (cooldownMap.isEnabled(player)) { // player is going home...
            // taking item away
            player.setItemInHand(consume(player.getItemInHand(), 1, this));

            // playing sound
            player.getWorld().playEffect(player.getLocation(),
                    Effect.GHAST_SHOOT, 1);
            
            // teleporting
            home.add(TELEPORT_VECTOR);
            player.teleport(home);
            
            // starting cooldown timer
            cooldownMap.start(player);
            
            return true;
        } else { // not cooled down yet
            int time = cooldownMap.getTime(player);
            player.sendMessage(HomeBlockPlugin.PLUGIN_NAME + " action ready in "
                    + time + " seconds.");
            return false;
        }

    }
    
    /**
     * Will consume {@code n} of {@code material} Items from {@code stack}.
     * So, it will lower {@code n} of {@code material} in {@code stack} and
     * if the amount reaches 0 or lower, it will remove the material from te stack.
     * @param stack the ItemStack to remove the items from.
     * @param n the amount of items you want to remove.
     * @param material the Material you want to remove from the stack.
     */
    private static SpoutItemStack consume(ItemStack stack, int n, Material material){
        int amount = stack.getAmount();
        amount -= n;
        if (amount <= 0){ // no more items
            return null;
        } else {
            return new SpoutItemStack(material, amount);
        }
    }
}
