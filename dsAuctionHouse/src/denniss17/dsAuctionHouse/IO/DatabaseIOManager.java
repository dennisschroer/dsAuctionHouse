package denniss17.dsAuctionHouse.IO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import denniss17.dsAuctionHouse.Auction;
import denniss17.dsAuctionHouse.DS_AuctionHouse;

public class DatabaseIOManager implements IOManager {
	private DatabaseConnection databaseConnection;
	private DS_AuctionHouse plugin;
	
	public DatabaseIOManager(){
		this.plugin = DS_AuctionHouse.instance;
		this.databaseConnection = new DatabaseConnection(plugin);
	}

	@Override
	public void setUp() {
		try {
			if(this.databaseConnection.tableExists(plugin.getConfig().getString("database.table_auctions"), "id")){
				
			}else{
				plugin.getPluginLoader().disablePlugin(plugin);
				throw new SQLException("TEMPORARY: CREATE TABLE YOURSELF");			
			}
		} catch (SQLException e) {
			this.handleSQLException(e);
		}
	}
	
	private void handleSQLException(SQLException e){
		plugin.getLogger().severe("===== SQL Exception =====");
		plugin.getLogger().severe("Message: " + e.getMessage());
		plugin.getLogger().severe("SQLState: " + e.getSQLState());
		plugin.getLogger().severe("ErrorCode: " + e.getErrorCode());
	}

	@Override
	public boolean updateTimesLeft(int difference) {
		String query = "UPDATE `" + plugin.getConfig().getString("database.table_auctions") + "` " +
				"SET `seconds_left`=`seconds_left`-" + difference + " WHERE `open`=1;";
				
		try {
			this.databaseConnection.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			this.plugin.getLogger().severe("Updating the time left failed. This can result in closed auctions being reopened on reload.");
			this.handleSQLException(e);
			return false;
		}
	}

	@Override
	public boolean closeAuction(Auction auction) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private String enchantmentsToString(Map<Enchantment, Integer> enchantments){
		String result = "";
		for(Enchantment enchantment : enchantments.keySet()){
			result += enchantment.getName() + "," + enchantments.get(enchantment) + ";";
		}
		return result;
	}

	@Override
	public boolean saveAuction(Auction auction) {
		try {
			this.databaseConnection.connect();
			PreparedStatement statement = this.databaseConnection.getConnection().prepareStatement(
					"INSERT INTO `" + plugin.getConfig().getString("database.table_auctions") + "`" +
							"(id, seconds_left, seller, amount, price, item_id, item_data, item_durability, item_enchantments, item_displayname)" +
							"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
					);
			ItemStack itemStack = auction.getItemStack();
			statement.setInt(1, auction.getId());
			statement.setInt(2, auction.getTimeLeft());
			statement.setString(3, auction.getOfflinePlayer().getName());
			statement.setInt(4, itemStack.getAmount());
			statement.setDouble(5, auction.getPrice());
			statement.setInt(6, itemStack.getTypeId());
			statement.setByte(7, itemStack.getData().getData());
			statement.setInt(8, itemStack.getDurability());
			statement.setString(9, enchantmentsToString(itemStack.getEnchantments()));
			statement.setString(10, itemStack.getItemMeta().getDisplayName());
			statement.executeUpdate();
			this.databaseConnection.close();
			return true;
		} catch (SQLException e) {
			this.handleSQLException(e);
			return false;
		}
	}

	@Override
	public Map<Integer, Auction> loadAuctions() {
		String query = "SELECT * FROM `" + plugin.getConfig().getString("database.table_auctions") + "`;";
		Map<Integer, Auction> result = new HashMap<Integer, Auction>();
		try {
			ResultSet resultSet = this.databaseConnection.executeQuery(query);
			while(resultSet.next()){
				//ItemStack itemStack = ItemStack.
			}
			this.databaseConnection.close();
		} catch (SQLException e) {
			this.handleSQLException(e);
		}
		
		return result;
	}

	@Override
	public boolean removeAuction(Auction auction) {
		// TODO Auto-generated method stub
		return false;
	}

}
