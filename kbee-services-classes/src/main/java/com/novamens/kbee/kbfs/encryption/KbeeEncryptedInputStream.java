package com.novamens.kbee.kbfs.encryption;

import com.novamens.kbee.kbfs.encryption.interfaces.StreamEncryptorInfo;

import java.io.FilterInputStream;
import java.io.InputStream;

public class KbeeEncryptedInputStream extends FilterInputStream {

    private StreamEncryptorInfo streamEncryptorInfo;

    protected KbeeEncryptedInputStream(InputStream in, StreamEncryptorInfo streamEncryptorInfo) {
        super(in);
        setStreamEncryptorInfo(streamEncryptorInfo);
    }

    public StreamEncryptorInfo getStreamEncryptorInfo() {
        return streamEncryptorInfo;
    }

    public void setStreamEncryptorInfo(StreamEncryptorInfo streamEncryptorInfo) {
        this.streamEncryptorInfo = streamEncryptorInfo;
    }
}
