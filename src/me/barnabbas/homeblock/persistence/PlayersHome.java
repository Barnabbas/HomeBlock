/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barnabbas.homeblock.persistence;

import com.avaje.ebean.validation.NotEmpty;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A Relation between the players and their Home.<br>
 * Also uses the kind of Home as attribute.<br>
 * This class is used to save stuff in databases.
 *
 * @author Stef van Schuylenburg
 * @since 20-dec-2011
 */
@Entity
@Table(name="HomeBlock_hasHome")
public class PlayersHome {
    
    @Id
    Integer id;
    
    @NotEmpty
    String player;
    @NotEmpty
    String homeMap;
    
    Integer locationId;

    public String getHomeMap() {
        return homeMap;
    }

    public void setHomeMap(String homeMap) {
        this.homeMap = homeMap;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }
    
    
    
}
