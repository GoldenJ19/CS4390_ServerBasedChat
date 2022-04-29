package org.teamnine.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
	// Creates the server database at server.db if it doesn't exist
	public static Connection setupDatabase(String filename) 
	throws IOException, SQLException, ClassNotFoundException {

		File file = new File(filename);
		if (!file.exists()) {
			return createDatabase(filename);
		} else {
			System.err.println("found '"+filename+"', using that as database");
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:"+filename);
		}
	}

	private static Connection createDatabase(String filename)
	throws SQLException, ClassNotFoundException {

		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:"+filename);
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

		String createChatLogTableSQL =
    			"""
				CREATE TABLE chat_log(
					clientFrom		TEXT	NOT NULL,
					clientTo		TEXT 	NOT NULL,
					session_id  	TEXT 	NOT NULL,
					message			TEXT	NOT NULL
				);
				""";
		stmt.executeUpdate(createChatLogTableSQL);
		stmt.close();
		
		return c;
	}
}
