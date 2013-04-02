package denniss17.dsAuctionHouse;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
	
	private DS_AuctionHouse plugin;
	
	public static Location loc1, loc2;
	
	public PlayerListener(DS_AuctionHouse plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		int count = 0;
		for(Auction auction : DS_AuctionHouse.auctionManager.getAuctions().values()){
			if(auction.getOfflinePlayer().equals(event.getPlayer()) && auction.isExpired()) count++;
		}
		if(count>0){
			plugin.sendMessage(event.getPlayer(), plugin.getConfig().getString("messages.you_have_expired_auctions").replace("{count}", String.valueOf(count)));
			plugin.sendMessage(event.getPlayer(), plugin.getConfig().getString("messages.after_cancel"));
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if ((player.getItemInHand().getTypeId() == plugin.getConfig().getInt("general.selectiontool")) && (player.hasPermission("lk_auction.admin"))) {
			Block b = event.getClickedBlock();
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				loc1 = b.getLocation().clone();
				plugin.sendMessage(player, ChatColor.BLUE + "First point set.");
			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				loc2 = b.getLocation().clone();
				plugin.sendMessage(player, ChatColor.BLUE + "Second point set.");
			}
			event.setCancelled(true);
		}
	}
	
}
