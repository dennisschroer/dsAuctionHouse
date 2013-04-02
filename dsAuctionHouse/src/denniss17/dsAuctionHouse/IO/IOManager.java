package denniss17.dsAuctionHouse.IO;

import java.util.Map;

import denniss17.dsAuctionHouse.Auction;

public interface IOManager {
	/**
	 * Setup the IOManager. Called when loaded, used for prepairing database/config.
	 */
	public void setUp();
	
	/**
	 * Decrease the times left of all open auctions.
	 * Best to call this async.
	 * @param difference The amount of seconds to decrease it with.
	 * @return true on success
	 */
	public boolean updateTimesLeft(int difference);
	
	/**
	 * Close the given auction, so that the items can be claimed back
	 * @param auction The auction to be closed
	 * @return true on success
	 */
	public boolean closeAuction(Auction auction);
	
	/**
	 * Save the given Auction
	 * @param auction The Auction to save
	 * @return true on success
	 */
	public boolean saveAuction(Auction auction);
	
	/**
	 * Remove the given Auction from the harddisk.
	 * Used if auction is closed and claimed.
	 * @param auction The Auction to remove
	 * @return true on success
	 */
	public boolean removeAuction(Auction auction);
	
	/**
	 * Load all Auctions from disk, return it as a mapping from auction_id to Auction
	 * @return A mapping containing all auctions
	 */
	public Map<Integer, Auction> loadAuctions();
	
}
