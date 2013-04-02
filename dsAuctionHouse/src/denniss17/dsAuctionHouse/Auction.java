package denniss17.dsAuctionHouse;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

public class Auction {
	private int timeLeft;
	private double price;
	private int id;
	private ItemStack itemStack;
	private OfflinePlayer seller;
	/** 
	 * Is this auction expired? 
	 * If so, the auction is closed but the items have not been claimed back.
	 */
	private boolean isExpired;
	
	/**
	 * Create a new auction
	 * @param itemStack The ItemStack that is being sold
	 * @param price The price that it is being sold for
	 * @param seller The player who sells it
	 * @param lifeTime The number of seconds the auction exists
	 */
	public Auction(ItemStack itemStack, double price, OfflinePlayer seller, int lifeTime){
		this.price = price;
		this.itemStack = itemStack;
		this.seller = seller;
		this.timeLeft = lifeTime;
		DS_AuctionHouse.auctionManager.addAuction(this);
	}
	
	/**
	 * Create a new standalone auction which doesn't register itself 
	 * with the auctionManager. Used in the IOManagers
	 * @param id The id of this auction
	 * @param itemStack The ItemStack that is being sold
	 * @param price The price that it is being sold for
	 * @param seller The player who sells it
	 * @param lifeTime The number of seconds the auction exists
	 */
	public Auction(int id, ItemStack itemStack, double price, OfflinePlayer seller, int lifeTime){
		this.id = id;
		this.price = price;
		this.itemStack = itemStack;
		this.seller = seller;
		this.timeLeft = lifeTime;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	/** 
	 * One second has expired. Check if the auction should be closed
	 */
	public void tick(){
		if(!isExpired) {
			this.timeLeft--;
			if(this.timeLeft==0) cancel(false);
		}		
	}
	
	public void cancel(boolean initiatedByPlayer){
		if(seller.isOnline()){
			if(initiatedByPlayer){
				DS_AuctionHouse.instance.sendMessage(seller.getPlayer(), DS_AuctionHouse.instance.getConfig().getString("messages.sale_canceled"));
			}else{
				DS_AuctionHouse.instance.sendMessage(seller.getPlayer(), DS_AuctionHouse.instance.getConfig().getString("messages.not_sold"));
			}
			DS_AuctionHouse.instance.sendMessage(seller.getPlayer(), DS_AuctionHouse.instance.getConfig().getString("messages.after_cancel"));
		}
		this.setExpired(true);
		this.timeLeft=0;
	}

	public OfflinePlayer getOfflinePlayer() {
		return seller;
	}
	
	public void setExpired(boolean expired){
		this.isExpired = expired;
	}
	
	public boolean isExpired(){
		return isExpired;
	}

	public double getPrice() {
		return price;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public String getItemName(){
		if(itemStack.getType().equals(Material.POTION)){
			Potion potion = Potion.fromItemStack(itemStack);
			String name = potion.isSplash() ? "SPLASH " : "";
			return name + "POTION OF " + potion.getType().toString();
		}else{
			String name = itemStack.getType().toString();
			if(itemStack.getDurability()!=0) name += "-" + itemStack.getDurability();
			return name;
		}
	}
	
	@Override
	public String toString(){
		String item = getItemName();
		if(itemStack.getEnchantments().size()>0) item = "enchanted " + item;
		
		return DS_AuctionHouse.instance.getConfig().getString("messages.auction_list_line")
				.replace("{player}", seller.getName())
				.replace("{id}", String.valueOf(getId()))
				.replace("{item}", item)
				.replace("{amount}" , String.valueOf(itemStack.getAmount()))
				.replace("{price}", String.valueOf(price))
				.replace("{timeleft}", DS_AuctionHouse.secondsToString(timeLeft))
				;
	}

	public int getTimeLeft() {
		return timeLeft;
	}
}
