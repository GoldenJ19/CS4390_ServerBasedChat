package org.teamnine.server;

import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConnectionHandlerTest {

	@Test
	public void TestConnectionHandler() throws InterruptedException, IOException {
		try (
				Connection dbConn = DatabaseSetup.setupDatabase("testserver.db");
				ChatRoom chatRoom = new ChatRoom(dbConn);
		) {
			final int randCookie = 12;

			ConnectionHandler ch1 = new ConnectionHandler(chatRoom, 1337, randCookie, dbConn, "hello world");
			Thread chThread = new Thread(ch1);
			chThread.start();

			ConnectionHandler ch2 = new ConnectionHandler(chatRoom, 1338, 14, dbConn, "hello world");
			Thread ch2Thread = new Thread(ch2);
			ch2Thread.start();
			Thread.sleep(500);

			String resp;
			String session_id;
			try (
					var mc1 = new MockClient("localhost", 1337, "bobby", 12);
					var mc2 = new MockClient("localhost", 1338, "samir", 14)
			) {
				System.out.println("Testing for connected response for client 1...");
				mc1.connect();
				resp = mc1.recordResponse(3);

				assertEquals(
						"START\n" +
								"MSGTYPE: CONNECTED\n" +
								"END\n", resp
				);
				System.out.println("Done.");

				System.out.println("Testing for connected response for client 2...");
				mc2.connect();
				resp = mc2.recordResponse(3);

				assertEquals(
						"START\n" +
								"MSGTYPE: CONNECTED\n" +
								"END\n", resp
				);
				System.out.println("Done.");

				System.out.println("Sending chat request to client 2...");
				mc1.chatRequest("samir");
				resp = mc2.recordResponse(5);

				// Use regex to find session id.
				Pattern p = Pattern.compile("SESSION_ID: (.+)\\S*");
				Matcher m = p.matcher(resp);
				assertTrue(m.find());
				session_id = m.group(1);
				System.out.println("Done.");

				// Test for chat started from client 1.
				resp = mc1.recordResponse(5);
				assertEquals(
			"START\n" +
					"MSGTYPE: CHAT_STARTED\n" +
					"SESSION_ID: " +
					session_id +
					"\nCLIENTB: samir\n" +
					"END\n",
					resp
				);

				System.out.println("Sending chat from client 1 to client 2...");
				mc1.sendChat(session_id, "hello");
				resp = mc2.recordResponse(5);
				assertEquals(
			"START\n" +
					"MSGTYPE: CHAT\n" +
					"SESSION_ID: " + session_id + "\n" +
					"MESSAGE: hello\n" +
					"END\n", resp
				);
				System.out.println("Done.");

				System.out.println("Sending end notif from client 2 to client 1...");
				mc2.endRequest(session_id);
				resp = mc2.recordResponse(4);
				assertEquals(
			"START\n"+
					"MSGTYPE: END_NOTIF\n"+
					"SESSION_ID: "+session_id+"\n"+
					"END\n", resp
				);

				resp =  mc1.recordResponse(4);
				assertEquals(
			"START\n"+
					"MSGTYPE: END_NOTIF\n"+
					"SESSION_ID: "+session_id+"\n"+
					"END\n", resp
				);

				mc1.historyRequest("samir");
				resp = mc1.recordResponse(5);
				assertEquals(
			"START\n"+
					"MSGTYPE: HISTORY_RESP\n" +
					"SENDER: bobby\n"+
					"MESSAGE: hello\n"+
					"END\n", resp
				);
			}
			ch1.interrupt();
			ch2.interrupt();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
