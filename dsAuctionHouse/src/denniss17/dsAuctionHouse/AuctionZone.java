package denniss17.dsAuctionHouse;

import org.bukkit.Location;

public class AuctionZone {
	private Location location1;
	private Location location2;
	private String name;
	
	public AuctionZone(String name, Location location1, Location location2){
		this.name = name;
		this.location1=location1;
		this.location2=location2;
	}
	
	public boolean isInZone(Location location){
		if(location.getWorld()==location1.getWorld()){
			if(location1.getBlockX() <= location.getBlockX() && location2.getBlockX() >= location.getBlockX()){
				if(location1.getBlockY() <= location.getBlockY() && location2.getBlockY() >= location.getBlockY()){
					if(location1.getBlockZ() <= location.getBlockZ() && location2.getBlockZ() >= location.getBlockZ()){
						return true;
					}	
				}
			}
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public Location getFirstLocation() {
		return location1;
	}
	
	public Location getSecondLocation() {
		return location2;
	}
	
}
