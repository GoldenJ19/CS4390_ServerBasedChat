package org.teamnine.common;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
			//Concat the rand cookie and secretkey
			String input = String.valueOf(rand) + secretKey;
			
            // getInstance using MD5
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
	public static byte[] A8(int rand, String secretKey) {
		try {
			//Concat the rand cookie and secretkey
			String concat = rand + secretKey;
			
			//Convert the concatenated key to a byte array
			byte[] key = concat.getBytes(StandardCharsets.UTF_8);
			
			//getInstance using SHA-256
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			
			//Reset digest, then update using the new byte array and SHA-256
	        digest.reset();
	        digest.update(key);

	        return digest.digest();
		} 
		
		catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
		}
	}

	// opmode should be Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	public static Cipher getCipher(int opmode, int rand, String secretKey) throws Exception {
		final String algorithm = "AES/ECB/PKCS5Padding";
		Cipher cipher = Cipher.getInstance(algorithm);
		byte[] secret = A8(rand, secretKey);
		SecretKey skey = new SecretKeySpec(secret, "AES");
		cipher.init(opmode, skey);

		return cipher;
	}
	
	
	//Boolean function that compares a MD5 hash to some randCookie and K_A
	public boolean A3match(String orig, int rand, String secretKey){
	String newComp = A3(rand, secretKey);
	if(orig.equals(newComp)) {return true;}
	else {return false;}
	}
}
