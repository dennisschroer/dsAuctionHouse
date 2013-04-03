package denniss17.dsAuctionHouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionHouseCommandExecutor implements CommandExecutor {
	private DS_AuctionHouse plugin;

	public AuctionHouseCommandExecutor(DS_AuctionHouse plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(cmd.getName().equals("auction")){
			return cmdAuction(sender, cmd, commandlabel, args);
		}
		return false;		
	}

	private boolean cmdAuction(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(args.length==0){
			return cmdAuctionMenu(sender, cmd, commandlabel, args);
		}else{
			if(args[0].equals("sell")){
				return cmdAuctionSell(sender, cmd, commandlabel, args);
			}else if(args[0].equals("info")){
				return cmdAuctionInfo(sender, cmd, commandlabel, args);
			}else if(args[0].equals("buy")){
				return cmdAuctionBuy(sender, cmd, commandlabel, args);
			}else if(args[0].equals("cancel")){
				return cmdAuctionCancel(sender, cmd, commandlabel, args);
			}else if(args[0].equals("list")){
				return cmdAuctionList(sender, cmd, commandlabel, args);
			}else if(args[0].equals("mine")){
				return cmdAuctionMine(sender, cmd, commandlabel, args);
			}else if(args[0].equals("search")){
				return cmdAuctionSearch(sender, cmd, commandlabel, args);
			}else if(args[0].equals("zone")){
				return cmdAuctionZone(sender, cmd, commandlabel, args);
			}else if(args[0].equals("claim")){
				return cmdAuctionClaim(sender, cmd, commandlabel, args);
			}
		}
		return false;
	}

	private boolean cmdAuctionSell(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(args.length>=3){
			if(sender instanceof Player){
				Player player = (Player)sender;
				AuctionZone currentZone = plugin.getAuctionZone(player.getLocation());
				if(plugin.getConfig().getBoolean("general.restrict_to_auctionzones") && currentZone==null){
					plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_in_auction_zone"));
					return true;
				}
				try{
					ItemStack itemStack = player.getInventory().getItemInHand();
					double price = Double.parseDouble(args[2]);
					int amount = Integer.parseInt(args[1]);
					double tax = price * plugin.getConfig().getDouble("general.tax_percentage");
					if(price<0){
						plugin.sendMessage(player, plugin.getConfig().getString("messages.error_price_not_correct"));
						return true;
					}
					if(!DS_AuctionHouse.economy.has(player.getName(), tax)){
						plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_enough_money"));
						return true;
					}
					if (itemStack.getAmount() >= amount && amount > 0 && amount <=64) {
						DS_AuctionHouse.economy.withdrawPlayer(player.getName(), tax);
						plugin.sendMessage(player, plugin.getConfig().getString("messages.you_started_auction").replace("{tax}", String.valueOf(tax)));
						// Create clones with different amounts
						ItemStack clone = itemStack.clone();
						ItemStack clone2 = itemStack.clone();
						// clone = ItemStack of Auction
						clone.setAmount(amount);
						// clone2 = ItemStack to give back to player
						clone2.setAmount(itemStack.getAmount() - amount);
						
						player.getInventory().removeItem(itemStack);
						player.getInventory().addItem(clone2);
						Auction auction = new Auction(clone, price, player, plugin.getConfig().getInt("general.auction_lifetime"));
						// auction adds itself to auctionhandler
						
						
						String item = auction.getItemName();
						if(itemStack.getEnchantments().size()>0) item = "enchanted " + item;
						
						plugin.broadcastMessage(plugin.getConfig().getString("messages.item_for_sale")
								.replace("{player}", player.getName())
								.replace("{id}", String.valueOf(auction.getId()))
								.replace("{item}", item)
								.replace("{amount}" , String.valueOf(clone.getAmount()))
								.replace("{price}", String.valueOf(price))
								.replace("{timeleft}", DS_AuctionHouse.secondsToString(auction.getTimeLeft()))
							);
						return true;
					}else{
						plugin.sendMessage(player, plugin.getConfig().getString("messages.error_amount_not_correct"));
						return true;
					}
				}catch(NumberFormatException e){
					plugin.sendMessage(player, plugin.getConfig().getString("messages.error_amount_not_correct"));
					return true;
				}				
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
				return true;
			}
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_sell"));
			return true;
		}
	}
	
	private boolean cmdAuctionInfo(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(args.length>=2){
			if(sender instanceof Player){
				Player player = (Player)sender;
				AuctionZone currentZone = plugin.getAuctionZone(player.getLocation());
				if(plugin.getConfig().getBoolean("general.restrict_to_auctionzones") && currentZone==null){
					plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_in_auction_zone"));
					return true;
				}
				try{
					int id = Integer.parseInt(args[1]);
					
					Auction auction = DS_AuctionHouse.auctionManager.getAuctionById(id);
					// Auction doesn't exist
					if(auction==null || auction.isExpired()){
						plugin.sendMessage(player, plugin.getConfig().getString("messages.error_auction_not_exists"));
						return true;
					}
					
					String enchantments = "";
					String durability = null;
					for(Entry<Enchantment, Integer> entry : auction.getItemStack().getEnchantments().entrySet()){
						enchantments += entry.getKey().getName() + ' ' + entry.getValue() + ", ";
					}
					if(auction.getItemStack().getType().getMaxDurability()!=0){
						durability = String.valueOf(
								(100*(auction.getItemStack().getType().getMaxDurability()-auction.getItemStack().getDurability()))
								/auction.getItemStack().getType().getMaxDurability()) + '%';
					}
					
					// Send first lines
					for(String msg : plugin.getConfig().getStringList("messages.auction_info")){
						plugin.sendMessage(player, msg
								.replace("{player}", player.getName())
								.replace("{id}", String.valueOf(auction.getId()))
								.replace("{item}", auction.getItemName())
								.replace("{material}", auction.getItemStack().getType().toString())
								.replace("{amount}" , String.valueOf(auction.getItemStack().getAmount()))
								.replace("{price}", String.valueOf(auction.getPrice()))
								.replace("{timeleft}", DS_AuctionHouse.secondsToString(auction.getTimeLeft()))
								);
					}
					// Send enchantments
					if(!enchantments.equals("")){
						plugin.sendMessage(player, plugin.getConfig().getString("messages.auction_info_enchantments")
								.replace("{enchantments}", enchantments));
					}
					// Send durability
					if(durability!=null){
						plugin.sendMessage(player, plugin.getConfig().getString("messages.auction_info_durability")
								.replace("{durability}", durability));
					}
					// Send bottom line
					plugin.sendMessage(player, plugin.getConfig().getString("messages.auction_info_bottom")
							.replace("{id}", String.valueOf(auction.getId())));
					
					
					return true;				
				}catch(NumberFormatException e){
					plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_not_a_number"));
					return true;
				}
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
				return true;
			}
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_info"));
			return true;
		}
	}
	
	private boolean cmdAuctionBuy(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(args.length>=2){
			if(sender instanceof Player){
				Player buyer = (Player)sender;
				AuctionZone currentZone = plugin.getAuctionZone(buyer.getLocation());
				if(plugin.getConfig().getBoolean("general.restrict_to_auctionzones") && currentZone==null){
					plugin.sendMessage(buyer, plugin.getConfig().getString("messages.error_not_in_auction_zone"));
					return true;
				}
				try{
					int id = Integer.parseInt(args[1]);
					
					Auction auction = DS_AuctionHouse.auctionManager.getAuctionById(id);
					// Auction doesn't exist
					if(auction==null || auction.isExpired()){
						plugin.sendMessage(buyer, plugin.getConfig().getString("messages.error_auction_not_exists"));
						return true;
					}
					// Buyer hasn't enough money
					if(!DS_AuctionHouse.economy.has(buyer.getName(), auction.getPrice())){
						plugin.sendMessage(buyer, plugin.getConfig().getString("messages.error_not_enough_money"));
						return true;
					}
					// Check for free space
					if(!DS_AuctionHouse.hasFreeSpace(buyer)){
						plugin.sendMessage(buyer, plugin.getConfig().getString("messages.error_not_enough_space"));
						return true;
					}		
					
					// Everything checked
					// Handle money
					DS_AuctionHouse.economy.withdrawPlayer(buyer.getName(), auction.getPrice());
					DS_AuctionHouse.economy.depositPlayer(auction.getOfflinePlayer().getName(), auction.getPrice());
					
					// Handle item
					buyer.getInventory().addItem(auction.getItemStack());
					
					// Send messages
					plugin.sendMessage(buyer, plugin.getConfig().getString("messages.item_bought")
							.replace("{player}", auction.getOfflinePlayer().getName())
							.replace("{id}", String.valueOf(auction.getId()))
							.replace("{item}", auction.getItemName())
							.replace("{amount}" , String.valueOf(auction.getItemStack().getAmount()))
							.replace("{price}", String.valueOf(auction.getPrice()))
						);
					if(auction.getOfflinePlayer().isOnline()){
						plugin.sendMessage(auction.getOfflinePlayer().getPlayer(), plugin.getConfig().getString("messages.item_sold")
								.replace("{player}", buyer.getName())
								.replace("{id}", String.valueOf(auction.getId()))
								.replace("{item}", auction.getItemName())
								.replace("{amount}" , String.valueOf(auction.getItemStack().getAmount()))
								.replace("{price}", String.valueOf(auction.getPrice()))
							);
					}
					
					// Remove auction
					DS_AuctionHouse.auctionManager.removeAuction(auction);					
					
					return true;				
				}catch(NumberFormatException e){
					plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_not_a_number"));
					return true;
				}
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
				return true;
			}
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_buy"));
			return true;
		}
	}
	
	private boolean cmdAuctionCancel(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(args.length>=2){
			if(sender instanceof Player){
				Player player = (Player)sender;
				AuctionZone currentZone = plugin.getAuctionZone(player.getLocation());
				if(plugin.getConfig().getBoolean("general.restrict_to_auctionzones") && currentZone==null){
					plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_in_auction_zone"));
					return true;
				}
				try{
					int id = Integer.parseInt(args[1]);
					
					Auction auction = DS_AuctionHouse.auctionManager.getAuctionById(id);
					if(auction==null || auction.isExpired()){
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_auction_not_exists"));
						return true;
					}
					if(!auction.getOfflinePlayer().equals((OfflinePlayer)player)){
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_not_your_auction"));
						return true;
					}
					
					auction.cancel(true);				
					return true;
				}catch(NumberFormatException e){
					plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_not_a_number"));
					return true;
				}
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
				return true;
			}
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_cancel"));
			return true;
		}
	}
	
	private boolean cmdAuctionList(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(sender instanceof Player){
			Player player = (Player)sender;
			// Check auctionZone of Player
			AuctionZone currentZone = plugin.getAuctionZone(player.getLocation());
			if(plugin.getConfig().getBoolean("general.restrict_to_auctionzones") && currentZone==null){
				plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_in_auction_zone"));
				return true;
			}
			
			// Pages
			int page = 1;
			if(args.length>=2){
				try{
					page = Integer.parseInt(args[1]);
				}catch(NumberFormatException e){
					// Nothing, show page 1
				}
				if(page<1) page=1;
			}
			int pagestart = (page-1) * 10;
			
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.auction_list_header"));
			
			int count = 0;
			for(Auction auction : DS_AuctionHouse.auctionManager.getAuctions().values()){
				if(!auction.isExpired() && !auction.getOfflinePlayer().equals(player)){
					if(count>=pagestart && count<(pagestart+10)) plugin.sendMessage(sender, auction.toString());
					count++;
				}
			}	
			if(count<=pagestart) plugin.sendMessage(sender, plugin.getConfig().getString("messages.no_open_auctions"));
			if(count>pagestart+10) plugin.sendMessage(sender, plugin.getConfig().getString("messages.auction_list_more").replace("{page}", String.valueOf(page+1)));
			
			return true;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
			return true;
		}
	}
	
	private boolean cmdAuctionMine(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(sender instanceof Player){
			Player player = (Player)sender;
			AuctionZone currentZone = plugin.getAuctionZone(player.getLocation());
			if(plugin.getConfig().getBoolean("general.restrict_to_auctionzones") && currentZone==null){
				plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_in_auction_zone"));
				return true;
			}
			
			// Pages
			int page = 1;
			if(args.length>=2){
				try{
					page = Integer.parseInt(args[1]);
				}catch(NumberFormatException e){
					// Nothing, show page 1
				}
				if(page<1) page=1;
			}
			int pagestart = (page-1) * 10;
			
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.auction_list_header"));
			
			int count = 0;
			for(Auction auction : DS_AuctionHouse.auctionManager.getAuctions().values()){
				if(!auction.isExpired() && auction.getOfflinePlayer().equals(player)){
					if(count>=pagestart && count<(pagestart+10)) plugin.sendMessage(sender, auction.toString());
					count++;
				}
			}	
			if(count<=pagestart) plugin.sendMessage(sender, plugin.getConfig().getString("messages.no_open_auctions"));
			if(count>pagestart+10) plugin.sendMessage(sender, plugin.getConfig().getString("messages.auction_mine_more").replace("{page}", String.valueOf(page+1)));
			
			return true;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
			return true;
		}
	}
	
	private boolean cmdAuctionClaim(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(sender instanceof Player){
			Player player = (Player)sender;
			AuctionZone currentZone = plugin.getAuctionZone(player.getLocation());
			if(plugin.getConfig().getBoolean("general.restrict_to_auctionzones") && currentZone==null){
				plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_in_auction_zone"));
				return true;
			}
		
		
			if(args.length==1){
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.auction_list_header"));
				
				int count = 0;
				for(Auction auction : DS_AuctionHouse.auctionManager.getAuctions().values()){
					if(auction.isExpired() && auction.getOfflinePlayer().equals(player)){
						plugin.sendMessage(sender, auction.toString());
						count++;
					}
				}	
				if(count==0) plugin.sendMessage(sender, plugin.getConfig().getString("messages.no_expired_auctions"));
				
				return true;				
			// /claim all or /claim <id>
			}else{
				if(args[1].equals("all")){
					// List is needed to prevent java.util.ConcurrentModificationException
					List<Integer> toTrash = new ArrayList<Integer>();
					for(Auction auction : DS_AuctionHouse.auctionManager.getAuctions().values()){
						if(auction.isExpired() && auction.getOfflinePlayer().equals(player)){
							if(!DS_AuctionHouse.hasFreeSpace(player)){
								plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_enough_space"));
								return true;
							}							
							player.getInventory().addItem(auction.getItemStack());
							toTrash.add(auction.getId());
						}
					}
					for(int id : toTrash){
						DS_AuctionHouse.auctionManager.removeAuction(id);
					}
					return true;
				}else{
					// Id given
					try{
						int id = Integer.parseInt(args[1]);
						
						Auction auction = DS_AuctionHouse.auctionManager.getAuctionById(id);
						if(auction==null){
							plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_auction_not_exists"));
							return true;
						}
						if(!auction.getOfflinePlayer().equals(player)){
							plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_not_your_auction"));
							return true;
						}
						if(!auction.isExpired()){
							plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_auction_still_open"));
							return true;
						}
						if(!DS_AuctionHouse.hasFreeSpace(player)){
							plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_enough_space"));
							return true;
						}	
						
						// Everything checked -> give item to player
						player.getInventory().addItem(auction.getItemStack());
						
						DS_AuctionHouse.auctionManager.removeAuction(auction);
						return true;
					}catch(NumberFormatException e){
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_not_a_number"));
						return true;
					}
				}
			}
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
			return true;
		}
	}
	
	private boolean cmdAuctionSearch(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(args.length>=2){
			if(sender instanceof Player){
				Player player = (Player)sender;
				AuctionZone currentZone = plugin.getAuctionZone(player.getLocation());
				if(plugin.getConfig().getBoolean("general.restrict_to_auctionzones") && currentZone==null){
					plugin.sendMessage(player, plugin.getConfig().getString("messages.error_not_in_auction_zone"));
					return true;
				}
				int count = 0;
				try{
					int id = Integer.parseInt(args[1]);
					
					for(Auction auction : DS_AuctionHouse.auctionManager.getAuctions().values()){
						if(!auction.isExpired() && auction.getItemStack().getTypeId()==id){
							plugin.sendMessage(sender, auction.toString());
							count++;
						}
					}	
					if(count==0) plugin.sendMessage(sender, plugin.getConfig().getString("messages.no_open_auctions"));
				}catch(NumberFormatException e){
					String name = args[1];
					
					for(Auction auction : DS_AuctionHouse.auctionManager.getAuctions().values()){
						if(!auction.isExpired() && auction.getItemName().contains(name.toUpperCase())){
							plugin.sendMessage(sender, auction.toString());
							count++;
						}
					}	
					if(count==0) plugin.sendMessage(sender, plugin.getConfig().getString("messages.no_open_auctions"));
				}
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
			}
			return true;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_search"));
			return true;
		}
	}
	
	private boolean cmdAuctionZone(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(!sender.hasPermission("ds_auction.admin")){
			return false;
		}	
		
		if(args.length>=2){
			if(args[1].equals("save")){
				if(PlayerListener.loc1!=null && PlayerListener.loc2!=null){
					if(args.length>=3){
						Location loc1 = PlayerListener.loc1;
						Location loc2 = PlayerListener.loc2;
						Location locmin = new Location(
								PlayerListener.loc1.getWorld(),
								Math.min(loc1.getBlockX(), loc2.getBlockX()),
								Math.min(loc1.getBlockY(), loc2.getBlockY()),
								Math.min(loc1.getBlockZ(), loc2.getBlockZ())
								);
						Location locmax = new Location(
								PlayerListener.loc1.getWorld(),
								Math.max(loc1.getBlockX(), loc2.getBlockX()),
								Math.max(loc1.getBlockY(), loc2.getBlockY()),
								Math.max(loc1.getBlockZ(), loc2.getBlockZ())
								);
						
						AuctionZone zone = new AuctionZone(args[2], locmin, locmax);
						plugin.addAuctionZone(zone);
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.zone_saved"));
					}else{
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_zone"));
					}
					return true;
				}else{
					plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_set_locations_first"));
					return true;
					// location not set
				}
			}else if(args[1].equals("remove")){
				if(args.length>=3){
					AuctionZone zone = plugin.getAuctionZone(args[2]);
					
					if(zone!=null){
						plugin.removeAuctionZone(zone);
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.zone_removed"));
					}else{
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.zone_not_found"));
					}
					return true;
					
				}else{
					plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_zone"));
					return true;
				}
			}else if(args[1].equals("list")){
				String list = "";
				for(AuctionZone zone : DS_AuctionHouse.auctionZones){
					list += zone.getName() + ", ";
				}
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.zone_list").replace("{list}", list));
				return true;
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_zone"));
				return true;
			}
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_zone"));
			return true;
		}
	}

	private boolean cmdAuctionMenu(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_header"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_sell"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_info"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_buy"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_cancel"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_list"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_mine"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_claim"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_claim2"));
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_search"));
		if(sender.hasPermission("ds_auction.admin")){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_zone"));
		}
		
		
		return true;
	}

}
