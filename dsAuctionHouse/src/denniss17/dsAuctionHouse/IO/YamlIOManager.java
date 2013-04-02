package denniss17.dsAuctionHouse.IO;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import denniss17.dsAuctionHouse.Auction;
import denniss17.dsAuctionHouse.DS_AuctionHouse;

public class YamlIOManager implements IOManager {
	
	private FileConfiguration auctionConfig = null;
	private File auctionConfigFile = null;

	@Override
	public void setUp() {
		reloadAuctionConfig();
	}

	@Override
	public boolean updateTimesLeft(int difference) {
		return true;
	}

	@Override
	public boolean closeAuction(Auction auction) {
		// Save auction, as timeLeft is changed
		return saveAuction(auction);
	}

	@Override
	public boolean saveAuction(Auction auction) {
		String path = "auctions." + auction.getId();
		
		auctionConfig.set(path + ".id", auction.getId());
		auctionConfig.set(path + ".price", auction.getPrice());
		auctionConfig.set(path + ".seller", auction.getOfflinePlayer().getName());
		Calendar cal = Calendar.getInstance(); 
		auctionConfig.set(path + ".timeout", (cal.getTimeInMillis()/1000) + auction.getTimeLeft());
		
		for(Map.Entry<String, Object> entry : auction.getItemStack().serialize().entrySet()){
			auctionConfig.set(path + ".itemstack." + entry.getKey(), entry.getValue());
		}
		this.saveAuctionConfig();
		return true;
	}

	@Override
	public boolean removeAuction(Auction auction) {
		auctionConfig.set("auctions." + auction.getId(), null);
		this.saveAuctionConfig();
		return true;
	}

	@Override
	public Map<Integer, Auction> loadAuctions() {
		Map<Integer, Auction> result = new HashMap<Integer, Auction>();
		for(String key : auctionConfig.getConfigurationSection("auctions").getKeys(false)){
			Calendar cal = Calendar.getInstance(); 
			int timeLeft = (int)(auctionConfig.getLong("auctions." + key + ".timeout") - cal.getTimeInMillis()/1000);
			int id = auctionConfig.getInt("auctions." + key + ".id");
			Auction auction = new Auction(
					id,
					ItemStack.deserialize(auctionConfig.getConfigurationSection("auctions." + key + ".itemstack").getValues(false)),
					auctionConfig.getDouble("auctions." + key + ".price"), 
					DS_AuctionHouse.instance.getServer().getOfflinePlayer(auctionConfig.getString("auctions." + key + ".seller")),
					timeLeft
				);
			if(timeLeft<=0){
				auction.setExpired(true);
			}			
			result.put(id, auction);
						
		}
		return result;
	}
	
	protected void reloadAuctionConfig() {
		if (auctionConfigFile == null) {
			auctionConfigFile = new File(DS_AuctionHouse.instance.getDataFolder(), "auctionConfig.yml");
		}
		auctionConfig = YamlConfiguration.loadConfiguration(auctionConfigFile);
	}

	protected void saveAuctionConfig() {
		if (auctionConfig == null || auctionConfigFile == null) {
			return;
		}
		try {
			auctionConfig.save(auctionConfigFile);
		} catch (IOException ex) {
			DS_AuctionHouse.instance.getLogger().log(Level.SEVERE,
					"Could not save config to " + auctionConfigFile, ex);
		}
	}

	protected MemorySection getAuctionConfig() {
		if (auctionConfig == null) {
			reloadAuctionConfig();
		}
		return auctionConfig;
	}

}
