package org.teamnine.common;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.OutputStream;

public class FixedCipherOutputStream extends OutputStream {
	CipherOutputStream os;
	// Should be the inner stream in the CipherOutputStream
	OutputStream inner;
	// Should be the same cipher in CipherOutputStream
	Cipher c;
	public FixedCipherOutputStream(CipherOutputStream os, OutputStream inner, Cipher c) {
			this.os = os;
			this.inner = inner;
			this.c = c;
	}

	public void close() throws IOException {
		os.close();
	}

	// Closes the output stream to force the encryption to finish
	public void flush() throws IOException {
		try {
			inner.write(c.doFinal());
			inner.write(c.doFinal());
			inner.flush();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		}
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
