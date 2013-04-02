package denniss17.dsAuctionHouse;

import java.util.Map;

import org.bukkit.scheduler.BukkitTask;

public class AuctionManager implements Runnable{
	private DS_AuctionHouse plugin;
	private BukkitTask bukkitTask;
	private Map<Integer, Auction> auctions;
	private int firstFree;
	
	public AuctionManager(DS_AuctionHouse plugin) {
		this.plugin = plugin;
		this.firstFree = 1;
		this.auctions = DS_AuctionHouse.ioManager.loadAuctions();
		
		int expiredCount = 0;
		for(Auction auction : auctions.values()){
			if(auction.isExpired()) expiredCount++;
		}
		plugin.getLogger().info(auctions.size() + " auctions loaded (" + expiredCount + " expired)");
	}
	
	private int findFreeId(){
		int result = firstFree;
		while(auctions.containsKey(result)){
			result++;
		}
		firstFree = result+1;
		return result;
	}
	
	public void addAuction(Auction auction){
		int id = findFreeId();
		auctions.put(id, auction);
		auction.setId(id);
		runTimer();
		// Save to harddisk
		DS_AuctionHouse.ioManager.saveAuction(auction);
	}
	
	public void removeAuction(Auction auction){
		// remove from mapping
		auctions.remove(auction.getId());
		firstFree = auction.getId()<firstFree ? auction.getId() : firstFree;
		// remove from harddisk
		DS_AuctionHouse.ioManager.removeAuction(auction);
	}
	
	public void removeAuction(int auctionId){
		Auction auction = getAuctionById(auctionId);
		if(auction!=null) removeAuction(auction);
	}
	
	private void runTimer(){
		if(bukkitTask==null){
			bukkitTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 20L, 20L);
		}
	}
	
	private void stopTimer(){
		if(bukkitTask!=null){ bukkitTask.cancel(); bukkitTask = null; }
	}

	@Override
	public void run() {
		updateAuctions();		
	}

	public void updateAuctions() {
		for(Auction auction: auctions.values()){
			auction.tick();
		}
		if(auctions.size()==0){
			stopTimer();
			bukkitTask = null;
		}
	}

	public Auction getAuctionById(int id) {
		return auctions.get(id);
	}

	public Map<Integer, Auction> getAuctions() {
		return auctions;
	}

}
