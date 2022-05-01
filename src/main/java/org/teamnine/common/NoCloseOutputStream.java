package org.teamnine.common;

import java.io.IOException;
import java.io.OutputStream;

public class NoCloseOutputStream extends OutputStream {
	OutputStream os;

	public NoCloseOutputStream(OutputStream os) {
		this.os = os;
	}
	public void close() {
		// no-op
	}

	public void flush() throws IOException {
		os.flush();
	}

	public void write(byte[] b) throws IOException {
		os.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b, off, len);
	}

	public void write(int b) throws IOException {
		os.write(b);
	}
}
