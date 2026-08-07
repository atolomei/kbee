package com.novamens.kbee.kbfs.v1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;

import com.novamens.io.FileInputStream;
import com.novamens.kbfs.v1.FSInputStream;
import com.novamens.kbfs.v1.FileServerV1;

public class EncryptedFSInputStream extends InputStream implements FSInputStream {

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	static boolean cacheenabled = true;
	
	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
	private FSInputStream filereader = null;
	private BufferedInputStream incache = null;
  	private BufferedOutputStream outcache = null;
	
    private FileRSupport fs;
    private FileServerV1 fileserver;
	
	static private final int BUFFER_SIZE 	= 8192;
    static public final int IVSIZE	 		= 16;
	
	private int bufferSize = BUFFER_SIZE;
	
	byte[] workbuffer = new byte[BUFFER_SIZE];
    private byte iv[] = new byte[IVSIZE];
	
    int total;
     
    Cipher cipher;
    SecretKeySpec secretKey;
    											
	public EncryptedFSInputStream(FileRSupport fs, FileServerV1 fileserver) throws IOException {		
		this.fs=fs;
		this.fileserver = fileserver;
		this.filereader = new SimpleFSInputStream(fs);
		open();
	}
	
	@Override
	public void setBufferSize(int size) {
		bufferSize = size;
	}

	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	@Override
	public void open() throws IOException {
		
		if (cacheenabled) {
			if (fs.cachefile !=null && fs.cachefile.exists()) {
				incache = new BufferedInputStream(new FileInputStream(fs.cachefile), getBufferSize());
				return;
			}
			else {
 				try {	
 	 				if (fs.cachetowrite!=null && !fs.cachetowrite.exists())
 						outcache = new BufferedOutputStream(new FileOutputStream(fs.cachetowrite), getBufferSize());
				
 				} catch (FileNotFoundException e) {
						logger.warn("Cache file already being written " + fs.cachetowrite);
						outcache = null;
 				}
			}
		}
		
 		filereader.open();
		filereader.read(iv,0,16);
    	
    	try {
    		
    		cipher = Cipher.getInstance("AES/CBC/NoPadding");  			// "AES/ECB/PKCS5Padding"
        	secretKey = fileserver.getKey();
        	IvParameterSpec ivParameterSpec = new IvParameterSpec(this.iv);
    		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
     		
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new IOException(e.getClass()+  " - " + e.getMessage());
			
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new IOException(e.getClass()+  " - " + e.getMessage());
		} catch (NoSuchPaddingException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new IOException(e.getClass()+  " - " + e.getMessage());
		}
	}

	@Override
	public int available() throws IOException {
 		if (incache!=null)
			return incache.available();
 		return filereader.available();
	}
 	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}
 	@Override
	public void close() throws IOException {
 		if (incache!=null)
			incache.close();
		
		if (outcache!=null) {
			outcache.close();
 			this.fileserver.addToCache(fs.url, fs.cachetowrite);
 		}
 		filereader.close();
	}

	@Override
	public int read(byte[] buffer, int offset, int len) throws IOException {
		
		if (incache!=null)
			return incache.read(buffer, offset, len); 
 		
		if (len-offset>workbuffer.length)
			len=offset+workbuffer.length;
			
		int read = filereader.read(workbuffer, 0, len);
		
		byte[] decryptbuffer;
		if (offset>0) {
				try {
					byte[] temp = cipher.doFinal(workbuffer, 0, read);
					System.arraycopy(temp, 0, buffer, offset, read);
					
					if (outcache!=null) {
						logger.info("save buffer in cache file for next time ");
						outcache.write(buffer, offset, read); 
					}
 					
				} catch (IllegalBlockSizeException | BadPaddingException e) {
					throw new IOException(e.getClass()+  " - " + e.getMessage());
				}
		}
		else {
			try {
				decryptbuffer = cipher.doFinal(workbuffer, 0, read);
				System.arraycopy(decryptbuffer, 0, buffer, 0, read);
				
				if (outcache!=null) {
					logger.info("save buffer in cache file for next time ");
					outcache.write(buffer, 0, read); 
				}
 				
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				throw new IOException(e.getClass()+  " - " + e.getMessage());
			}
		}
 		if (total+read>fs.filesize) {
			read = (int) (fs.filesize - total);
		 }
		total += read;
		return read;
 	}

	@SuppressWarnings("unused")
	private String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	@Override
	public int read() throws IOException {
		throw new IOException("Not implemented");
	}
}
