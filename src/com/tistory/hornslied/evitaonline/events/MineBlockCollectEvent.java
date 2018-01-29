package com.tistory.hornslied.evitaonline.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.palmergames.bukkit.towny.object.Town;

public class MineBlockCollectEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private Town town;
	private Player player;
	private Location location;
	
	public MineBlockCollectEvent(Town town, Player player, Location location) {
		this.town = town;
		this.player = player;
		this.location = location;
	}
	
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public Town getTown() {
    	return town;
    }
    
    public Player getPlayer() {
    	return player;
    }
    
    public Location getLocation() {
    	return location;
    }
}
