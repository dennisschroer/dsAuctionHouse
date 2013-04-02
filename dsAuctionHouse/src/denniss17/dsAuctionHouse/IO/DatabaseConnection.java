package denniss17.dsAuctionHouse.IO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.java.JavaPlugin;


public class DatabaseConnection{
	private Connection connection;
	
	private String url;
	private String database;
	private String user;
	private String password;
	
	// CONSTRUCTORS
	public DatabaseConnection(JavaPlugin plugin){
		this.url = plugin.getConfig().getString("database.url");
		this.database = plugin.getConfig().getString("database.database");
		this.user = plugin.getConfig().getString("database.user");
		this.password = plugin.getConfig().getString("database.password");
		this.connection = null;
	}
	
	public String getDatabase(){
		return this.database;
	}
	
	public Connection getConnection(){
		return this.connection;
	}
	
	public void connect() throws SQLException{		
		if(url==null||user==null||password==null||database==null){
			throw new SQLException("One of the settings is not set!");
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException("Driver not found");
		}
		this.connection = DriverManager.getConnection(url + database, user, password);
	}
	
	public void close() throws SQLException{
		if(!this.connection.isClosed()){
			this.connection.close();
		}
	}
	
	public boolean tableExists(String table, String column) throws SQLException{
		boolean exists = false;
		
		this.connect();

        try {
            this.executeQuery("SELECT " + column + " FROM " + table);
            exists = true;
        } catch (SQLException e) {
            exists = false;
        }
        
        this.close();

        return exists;
	}
	
	/**
	 * Execute a query like UPDATE/INSERT/DELETE which returns a counter or nothing
	 * @param query The query to execute
	 * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
	 * @throws SQLException (Could also be SQLTimeoutException)
	 */
	public int executeUpdate(String query) throws SQLException{		
		this.connect();
		
		Statement statement = this.connection.createStatement();
		int result = statement.executeUpdate(query);
		
		this.close();
		
		return result;
	}
	
	/**
	 * Execute a query like SELECT which returns a result.<br>
	 * DatabaseConnection.close() MUST BE CALLED afterwarts
	 * @param query The query to execute
	 * @return A ResultSet Object with the result in it.
	 * @throws SQLException (Could also be SQLTimeoutException)
	 */
	public ResultSet executeQuery(String query) throws SQLException{
		
		this.connect();
		
		Statement statement = this.connection.createStatement();
		ResultSet rs = statement.executeQuery(query);
		
		// Close can't be called here, this would make the ResultSet empty
		
		return rs;
	}
}
