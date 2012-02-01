/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.barnabbas.homeblock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.getspout.spoutapi.player.SpoutPlayer;

/* 
 * A CooldownMap remembers for each player how much time he has to wait until 
 * an action can be done again. To use it test if 
 * {@link #isEnabled(SpoutPlayer)} is {@code true} and call 
 * {@link #start(SpoutPlayer)} to start the cooldown period again.
 *
 * @author Stef van Schuylenburg
 * @since 31-jan-2012
 */
public abstract class CooldownMap {

    /**
     * The time in seconds until the action for {@code player} becomes enabled again.
     * @param player the Player to get the time of
     * @return the time until the action for {@code player} becomes enabled again 
     * in seconds.
     */
    public abstract int getTime(SpoutPlayer player);

    /**
     * If {@code player} can do the action again.<br>
     * Note that after doing an action {@link #start(SpoutPlayer)} have to be 
     * called again, to start the timer for {@code player} again.
     * @param player the Player to get the information of.
     * @return If {@code player} can do the action again.
     */
    public boolean isEnabled(SpoutPlayer player) {
        return getTime(player) <= 0;
    }

    /**
     * Restarts the time until {@code player} comes enabled again.<br>
     * Must be called after the action is done that needs this cooldown.
     * @param player the Player to have its timer start again.
     */
    public abstract void start(SpoutPlayer player);

    /**
     * A new cooldownMap that will enable actions {@code time} seconds after 
     * {@link #start(org.getspout.spoutapi.player.SpoutPlayer) } has been called
     * for a specific Player.<Br>
     * This CooldownMap will be enabled for all players on start. Using this 
     * factory you will get a CoolDownMap that can be used for one action (each
     * instance can be used for another action).
     * @param time in seconds
     * @return a CooldownMap waiting {@code time} seconds.
     */
    public static CooldownMap getCooldownMap(int time) {
        if (time <= 0){
            return noCooldown;
        } else {
            return new BaseCooldown(time);
        }
    }
    /**
     * A CooldownMap used for when there is no Cooldown time.<br>
     * This Map will always return {@code true} on {@code isEnabled}.
     */
    private final static CooldownMap noCooldown = new CooldownMap() {

        @Override
        public int getTime(SpoutPlayer player) {
            return 0;
        }

        @Override
        public void start(SpoutPlayer player) {
            // intentional left blank
        }
    };

    /**
     * A basic CoolDownMap implmentation
     */
    private static class BaseCooldown extends CooldownMap {

        // all times are in seconds
        
        /**
         * The Time for one time unit to happen in miliseconds.
         * This is a second, so 1000 miliseconds
         */
        private final static int TIME_UNIT = 1000;
        
        /**
         * the time needed to cooldown
         */
        private final int cooldownTime;
        /**
         * The Timer to get the time.
         */
        private final Timer timer;
        /**
         * Time since the update has been done for the last time
         */
        int lastUpdate = 0;
        /** The time left for each timer (possible needs to be updated) */
        private final Map<SpoutPlayer, Integer> timeMap =
                new HashMap<SpoutPlayer, Integer>();

        /**
         * Creates a new CooldownMap that will let the players wait for
         * {@code time} seconds until their action becomes enabled again.
         * @param time the time in seconds to cooldown
         */
        public BaseCooldown(int time) {
            this.cooldownTime = time;

            timer = new Timer(true);

            // a timer task that updates the time of {@code lastUpdate}
            TimerTask task = new TimerTask() {

                @Override
                public synchronized void run() {
                    lastUpdate++;
                }
            };

            timer.scheduleAtFixedRate(task, TIME_UNIT, TIME_UNIT);
        }

        @Override
        public int getTime(SpoutPlayer player) {
            int time;
            if (!timeMap.containsKey(player)) {
                time = 0;
            } else {
                time = timeMap.get(player) - lastUpdate;
                if (time < 0) {
                    time = 0;
                }
            }

            return time;
        }

        @Override
        public void start(SpoutPlayer player) {
            update();
            
            synchronized (timeMap) {
                timeMap.put(player, cooldownTime);
            }
        }

        /**
         * updates the time left for each player according to {@code lastUpdate}
         * value.
         */
        private synchronized void update() {

            // the players that can be removed (time <= 0)
            Set<SpoutPlayer> toRemove = new HashSet<SpoutPlayer>();

            // setting time
            for (SpoutPlayer player : timeMap.keySet()) {
                int time = timeMap.get(player);
                time -= lastUpdate;

                if (time <= 0) { // no longer needed to remember him
                    toRemove.add(player);
                } else {
                    timeMap.put(player, time);
                }
            }

            // resetting update time
            lastUpdate = 0;

            // removing the unneeded
            for (SpoutPlayer player : toRemove) {
                timeMap.remove(player);
            }
        }
    }
}
