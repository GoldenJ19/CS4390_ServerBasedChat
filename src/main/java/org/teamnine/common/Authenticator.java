package org.teamnine.common;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Authenticator{
	private int[] K_Alist;
	private int[] randCookieList;
	
	//Returns T/F depending on if a randCookie is in the list of known
	//randCookies
	public boolean validateRandCookie(int randCookie) { 
		for(int i : randCookieList) {
			if(randCookie == i)
			{
				return true;
			}	
		}
		return false;
		}
	
	//Returns a MD5 hashed string, where the input string is 
	//a randCookie and client K_A concatenated
	public static String A3(int rand, String secretKey) {
		try {  
			String input = String.valueOf(rand) + secretKey;
			
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
  
            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());
  
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
  
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } 
	
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }	
	}
	
	
	//Returns a SHA1 hashed string, where the input string is 
	//a randCookie and client K_A concatenated
	public static String A8(int rand, String secretKey) {
		try {
			String input = String.valueOf(rand) + secretKey;
			
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
	        digest.reset();
	        digest.update(input.getBytes());
	        String hashtext = String.format("%040x", new BigInteger(1, digest.digest()));
	        
	        return hashtext;
		} 
		
		catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }	
		
	}
	
	
	//Boolean function that compares a MD5 hash to some randCookie and K_A
	public boolean A3match(String orig, int rand, String secretKey){
	String newComp = A3(rand, secretKey);
	if(orig.equals(newComp)) {return true;}
	else {return false;}
	}
	
	//Boolean function that compares a SHA1 hash to some randCookie and K_A
	public boolean A8match(String orig, int rand, String secretKey){
		String newComp = A8(rand, secretKey);
		if(orig.equals(newComp)) {return true;}
		else {return false;}
		}
}
