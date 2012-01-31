/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barnabbas.homeblock.persistence;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * A Location class used to save stuff in databases.
 *
 * @author Stef van Schuylenburg
 * @since 20-dec-2011
 */

@Entity
@Table(name="HomeBlock_location")
public class HomeLocation {
    
    @Id
    Integer id;
    
    int x, y, z;
    
    String world;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }
    
}
