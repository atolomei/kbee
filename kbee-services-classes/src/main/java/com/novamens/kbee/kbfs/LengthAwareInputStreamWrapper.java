package com.novamens.kbee.kbfs;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LengthAwareInputStreamWrapper extends FilterInputStream implements LengthAwareInputStream {

    private final long length;
    private long bytesRead = 0;

    public LengthAwareInputStreamWrapper(InputStream in, long length) {
        super(in);
        if (length <= 0) {
            throw new IllegalArgumentException("Content-Length must be > 0");
        }
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            bytesRead++;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
            bytesRead += n;
        }
        return n;
    }
}
