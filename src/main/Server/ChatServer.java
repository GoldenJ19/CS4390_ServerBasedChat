import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ChatServer {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private PrintWriter out;
	private Scanner in;
	private ParseBuilder pb;	

	private String[] subbedUsers = {"AHAD", "GRANT"};

	public void start(int port) throws Exception {
		serverSocket = new ServerSocket(port);
		clientSocket = serverSocket.accept();
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new Scanner(clientSocket.getInputStream());
		pb = new ParseBuilder(in);

		String msgType;
		msgType = pb.pass("START").pass("MSGTYPE:").extract();
		switch(msgType) {
			case "CONNECT":
				connectHandler();
				break;
			default:
				throw new Exception("invalid msgType");
		}
	}

	/*private String parseMsgType() throws Exception {
		String msgType = "";
		
		if (in.nextLine().equals("START")) {
			if (in.next().equals("MSGTYPE:")) {
				msgType = in.next();
				return msgType;
			} else {
				throw new Exception("couldn't find MSGTYPE after START line");
			}
		} else {
			throw new Exception("message didn't start with START");
		}
	}*/

	public void stop() throws Exception {
		in.close();
		out.close();
		clientSocket.close();
		serverSocket.close();
	}
	
	private void connectHandler() throws Exception {
		ParseBuilder pb = new ParseBuilder(in);
		String username = pb.pass("USERNAME:").extract();
		String randCookieStr = pb.pass("RAND_COOKIE:").extract();

		int rand_cookie = Integer.parseInt(randCookieStr);
		System.out.println("rand_cookie = " + rand_cookie);

		boolean isSubbed = false;
		for (String user : subbedUsers) {
			if (username.equals(user)) {
				isSubbed = true;
				break;
			}
		}
		
		if (isSubbed) {
			System.out.println("User " + username + " is valid.");
			connectedResponse();
		} else {
			System.out.println("User " + username + " is NOT valid.");
			stop();
		}
	}

	private void connectedResponse() {
		out.printf("START\nMSGTYPE: CONNECTED\nEND\n");
	}

	public static void main(String[] args) throws Exception {
		ChatServer server = new ChatServer();
		try {
			server.start(6666);
		} finally {
			server.stop();
		}
	}
	
	
}
