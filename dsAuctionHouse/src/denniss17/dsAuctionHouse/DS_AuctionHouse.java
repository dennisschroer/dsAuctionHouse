package denniss17.dsAuctionHouse;


import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Location;
import org.bukkit.World;

import denniss17.dsAuctionHouse.IO.IOManager;
import denniss17.dsAuctionHouse.IO.YamlIOManager;
import denniss17.dsAuctionHouse.Utils.ChatStyler;

public class DS_AuctionHouse extends JavaPlugin {
	public static DS_AuctionHouse instance;
	public static AuctionManager auctionManager;
	public static Set<AuctionZone> auctionZones;
	public static IOManager ioManager;
	public static Economy economy;
	
	@Override
	public void onEnable(){
		// Set the command executors
		AuctionHouseCommandExecutor commandExecutor = new AuctionHouseCommandExecutor(this);
		this.getCommand("auction").setExecutor(commandExecutor);
		
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		
		instance = this;
		
		ioManager = new YamlIOManager();
		ioManager.setUp();
		// ioManager before AuctionManager as the latter needs the first
		auctionManager = new AuctionManager(this);
		auctionZones = new HashSet<AuctionZone>();
		
		loadAuctionZones();
		
		if(loadEconomy()){
			getLogger().info("Vault economy found");
		}else{
			getLogger().warning("Vault not found. DISABLING plugin...");
			this.getPluginLoader().disablePlugin(this);
		}		
		
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();		
	}
	
	
	
	/** Load the economy via Vault */
	private boolean loadEconomy(){
		if(getServer().getPluginManager().getPlugin("Vault")==null){
			
			return false;
		}else{
			RegisteredServiceProvider<Economy> economyProvider = 
					getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}		
			return (economy != null);
		}
	}
	
	private void loadAuctionZones(){
		if(getConfig().contains("zones")){
			for(String name : getConfig().getConfigurationSection("zones").getKeys(false)){
				Location location1 = stringToLocation(getConfig().getString("zones." + name + ".loc1"));
				Location location2 = stringToLocation(getConfig().getString("zones." + name + ".loc2"));
				if(location1==null || location2==null){
					getLogger().warning("AuctionZone '" + name + "' is not saved correctly and will not be used.");
				}else{
					auctionZones.add(new AuctionZone(name, location1, location2));
				}
			}
		}
	}
	
	public AuctionZone getAuctionZone(Location location){
		for(AuctionZone auctionZone : auctionZones){
			if(auctionZone.isInZone(location)) return auctionZone;
		}
		return null;
	}
	
	public AuctionZone getAuctionZone(String name){
		for(AuctionZone auctionZone : auctionZones){
			if(auctionZone.getName().equalsIgnoreCase(name)) return auctionZone;
		}
		return null;
	}
	
	public void addAuctionZone(AuctionZone auctionZone){
		auctionZones.add(auctionZone);
		this.getConfig().set("zones." + auctionZone.getName() + ".loc1", locationToString(auctionZone.getFirstLocation()));
		this.getConfig().set("zones." + auctionZone.getName() + ".loc2", locationToString(auctionZone.getSecondLocation()));
		this.saveConfig();
	}
	
	public void removeAuctionZone(AuctionZone auctionZone){
		auctionZones.remove(auctionZone);
		this.getConfig().set("zones." + auctionZone.getName(), null);
		this.saveConfig();
	}
	
	public static boolean hasFreeSpace(Player player){
		ItemStack[] content = player.getInventory().getContents();
		boolean hasFreeSpace = false;
		int i = 0;
		while(i<content.length && !hasFreeSpace){
			if(content[i]==null) hasFreeSpace=true;
			i++;
		}
		return hasFreeSpace;
	}
	
	public static String secondsToString(int timeLeft) {
		String result = "";
		if((timeLeft/86400)>0){
			result += timeLeft/86400 + "d";
			timeLeft = timeLeft%86400;
		}
		if((timeLeft/3600)>0){
			result += timeLeft/3600 + "h";
			timeLeft = timeLeft%3600;
		}
		if((timeLeft/60)>0){
			result += timeLeft/60 + "m";
			timeLeft = timeLeft%60;
		}
		if(timeLeft>0){
			result += timeLeft + "s";
		}
		
		return result;
	}
	
	private String locationToString(Location location) {
		World world = location.getWorld();
		return world.getName() + ";" + location.getBlockX() + ";"
				+ location.getBlockY() + ";" + location.getBlockZ();
	}
	
	private Location stringToLocation(String safe){
		try {
			String[] vars = safe.split(";");
			if (vars.length != 4) {
				throw new NumberFormatException();
			}
			World world = getServer().getWorld(vars[0]);
			int x = Integer.parseInt(vars[1]);
			int y = Integer.parseInt(vars[2]);
			int z = Integer.parseInt(vars[3]);
			return new Location(world, x, y, z);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	/** 
	 * Send a message to a receiver.
	 * This method applies chat styles to the message
	 * @param receiver CommandSender the receiver of the message
	 * @param message The message to send
	 */
	public void sendMessage(CommandSender receiver, String message){
		receiver.sendMessage(ChatStyler.setTotalStyle(message));
	}
	
	public void broadcastMessage(String message){
		getServer().broadcastMessage(ChatStyler.setTotalStyle(message));
	}
	
	public boolean hasPermission(CommandSender user, String permission){
		return user.hasPermission("ds_auction." + permission);
	}
	
	public int getNumberOfAuctions(Player player){
		int count = 0;
		for(Auction auction : auctionManager.getAuctions().values()){
			if(auction.getOfflinePlayer().equals(player)){
				count++;
			}
		}	
		return count;
	}
	
	public int getMaxAuctions(Player player){
		int result = getConfig().getInt("general.max_auctions.default");
		for(String name : getConfig().getConfigurationSection("general.max_auctions").getKeys(false)){
			if(hasPermission(player, "max_auctions." + name)){
				if(getConfig().getInt("general.max_auctions." + name) > result)
					result = getConfig().getInt("general.max_auctions." + name);
			}
		}
		return result;
	}
	
	
}
