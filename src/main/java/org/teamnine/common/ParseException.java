package org.teamnine.common;

public class ParseException extends Exception{
	public ParseException(String expected, String recieved){
		super("expected '"+ expected +"' but got '"+ recieved +"'.");
	}
}