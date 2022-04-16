package org.teamnine.server.Authenticator;
import org.teamnine.server.Authenticator.*;
public class AuthTester{

public static void main(String[] args) {
	Authenticator auth = new Authenticator(3306);
	String test1 = auth.hashMD5("test");
	System.out.println(test1);
	System.out.println(auth.matching(test1, "test"));
	}
}