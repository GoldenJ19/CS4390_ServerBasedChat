package org.teamnine.server;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.sqlite.JDBC;

public class DatabaseSetup {
	// Creates the server database at server.db if it doesn't exist
	public static Connection setupDatabase(String filename) 
	throws IOException, SQLException, ClassNotFoundException {

		File file = new File(filename);
		if (!file.exists()) {
			return createDatabase();
		} else {
			System.err.println("found 'server.db', using that as database");
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:server.db");
		}
	}

	private static Connection createDatabase() 
	throws SQLException, ClassNotFoundException {

		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:server.db");
		System.out.println("Opened database successfully");
		Statement stmt = c.createStatement();

		// Create User table
		String sql = "CREATE TABLE USERS (" +
					 "id	INT PRIMARY KEY NOT NULL," +
					 "name	TEXT			NOT NULL," +
					 "skey	TEXT			NOT NULL" +
					 ")";
		stmt.executeUpdate(sql);
		stmt.close();
		
		return c;
	}
}
