package org.teamnine.common;

import java.io.IOException;
import java.util.Scanner;

public class ParseBuilder {
	Scanner scanner;

	public ParseBuilder(Scanner theScanner) {
		scanner = theScanner;
	}

	// pass checks for symbol s without reading it.
	public ParseBuilder pass(String s) throws ParseException {
		String next = scanner.next().trim();
		if (!next.equals(s)) {
			throw new ParseException(s, next);
		}	

		return this;
	}

	// extract returns the next string.
	public String extract() throws IOException {
		return scanner.next().trim();	
	}
}
