package org.teamnine.common;

import java.util.Scanner;

public class ParseBuilder {
	Scanner scanner;

	public ParseBuilder(Scanner theScanner) {
		scanner = theScanner;
	}

	// pass checks for symbol s without reading it.
	public ParseBuilder pass(String s) throws Exception {
		String next = scanner.next().trim();
		if (!next.equals(s)) {
			throw new Exception("expected '"+s+"' but got '"+next+"'.");
		}	

		return this;
	}

	// extract returns the next string.
	public String extract() throws Exception {
		return scanner.next().trim();	
	}
}
