package com.novamens.kbee.kbfs.v1;


import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;

import com.novamens.kbfs.v1.FSOutputStream;

public class EncryptedFSOutputWriter extends OutputStream implements FSOutputStream {

	static boolean cacheenabled = true;	

	static private final int BUFFER_SIZE 	= 8192;
    static private final int PADDING_BLOCK 	= 16;
    static public final int IVSIZE	 		= 16;
    static private SecureRandom random = new SecureRandom();

	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
	private SimpleFSOutputStream filewriter;

	private BufferedOutputStream outcache = null;

    private Cipher cipher;
    private SecretKeySpec secretKey;
    
    private KbeeFileServer fileserver; 
    private FileWSupport fs;
    
    private byte[] iv	 		= new byte[IVSIZE];
    private byte[] bufferin	 	= new byte[BUFFER_SIZE + PADDING_BLOCK];
    private byte[] workbuffer;    
    private byte[] bufaux		= new byte[BUFFER_SIZE];
    
    private int bufused	= 0;
    private int padding = 0;
    		
    private int bufferSize = BUFFER_SIZE;
    
    public byte[] getIV()				{return cipher.getIV();}
    public String  getEncryptedName() 	{return filewriter.getName();}

    public int  getBufferSize()			{return bufferSize;}
    
    public void setBufferSize(int size) {
    	bufferSize=size;
    	bufferin = null;
    	bufferin = new byte[size + PADDING_BLOCK];
    	filewriter.setBufferSize(size);
    }

    /**
     * FileServer constructs if with 
     * 
     * @param fileserver
     * @param fs
     * @throws IOException
     * 
     */
    protected EncryptedFSOutputWriter(KbeeFileServer fileserver, FileWSupport fs) throws IOException {
    	
    	this.fileserver = fileserver;
    	this.fs=fs;
    	this.filewriter = new SimpleFSOutputStream(this.fileserver, fs);
    	this.filewriter.setBufferSize(getBufferSize());
     	
    	if (random==null)
    		random = new SecureRandom();
    	
    	try {			
    		cipher = Cipher.getInstance("AES/CBC/NoPadding");
    		random.nextBytes(iv);
    		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
    		secretKey = this.fileserver.getKey();
    		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

    		open();
			
    	} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
    		logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
    		throw new IOException(e.getClass() + "  " + e.getMessage());
    	
    	} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
    		logger.error(e1);
    		throw new IOException(e1.getClass() + "  " + e1.getMessage());
		}
	}
    
   
	public void open() throws IOException {
 		filewriter.open();
 		if (cacheenabled) 
			  outcache = new BufferedOutputStream(new FileOutputStream(fs.cachefile), getBufferSize());
 		if (bytesWritten()==0) 
			filewriter.write(this.iv,0,16);
	}	
	/**
	 * 
	 * (len - offset) % PADDING_BLOCK must be zero 
	 * 
	 * @param buffer
	 * @param offset
	 * @param len
	 */
	private void encryptWorkbuffer(byte[] buffer, int offset, int len) {
		try {
			workbuffer = cipher.doFinal(buffer, offset, len);
			filewriter.write(workbuffer, 0, len);
 
		} catch (FileNotFoundException e) {
			 logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		} catch (IOException e1) {
			 logger.error(e1);
		} catch (IllegalBlockSizeException | BadPaddingException e1) {
			 logger.error(e1);
		}
	}
	
	public void writeAux() throws IOException {
		encryptWorkbuffer(bufaux, 0, getBufferSize());
	}
	
	@Override
	public void write(byte[] buffer, int off, int len) throws IOException {
			
		if (cacheenabled) {
			outcache.write(buffer, off, len);
		}
		
		// if aux buffer does not fill up, write to aux buffer and return
		if ((bufused + len)<getBufferSize()) {
			System.arraycopy(buffer, off, bufaux, bufused, len);
			bufused += len;
			return;
		} 
		
		// aux buffer does fill up
		else if (bufused>0) {
			
			int written = 0;
			
			// complete aux buffer and write
			int delta = bufused + len - getBufferSize();
			int avail = len - delta;  

			System.arraycopy(buffer, off, bufaux, bufused, avail);
			writeAux();
			bufused = 0;
			written += avail;
			
			// if there is more to write
			if (delta>0) {
			
				// first byte not written
				int start = off + avail;
				
				// write n buffers, the remaining bytes do not complete a whole aux buffer
				for(int n=0; n<delta/getBufferSize();n++) {
					encryptWorkbuffer(buffer, start + n * getBufferSize(), getBufferSize());
					written += getBufferSize();
				}
				
				// there are some bytes left, that can not make a whole buffer
				if (delta % getBufferSize()>0) {
					start = off + written;
					int leg  =  len - written;
					System.arraycopy(buffer, start, bufaux, bufused, leg);
					bufused  += leg;
				}
			}
			return;
		}

		/*
		 * 
		if(bufused>0) {			
			System.arraycopy(bufaux, 0, bufferin, 0, bufaux.length);
			int i=0;
			int res = getBufferSize() - bufused;
			if ((len-off) - res >0) {	
				for(int n=bufused; n<bufused + res; n++) {
						bufferin[n]=buffer[i];
						i++;
				}

				logger.debug("writing enc1: " + getBufferSize());
				encryptWorkbuffer(bufferin, 0, getBufferSize());
				
				if (cacheenabled) {
					logger.debug("writing plain1: " + len);
					outcache.write(buffer, off, len);
				}
				
				off = off + res;
				len = len - res; 
			}
			else {
				for(int n=bufused; n<off+len; n++) {
					bufferin[n]=buffer[i];
					i++;
				}
				return;
			}
		}

		int size = len-off;
		
		for(int n=0; n<size/getBufferSize();n++) {
			logger.debug("writing enc2: " + getBufferSize());
			encryptWorkbuffer(buffer, off + n * getBufferSize(), getBufferSize());

			if (cacheenabled) {
				logger.debug("writing plain2: " + getBufferSize());
				outcache.write(buffer, off + n * getBufferSize(), getBufferSize());
			}
		}
		
		if (size % getBufferSize()>0) {
			int start, end, blocklength;	
			blocklength = size % getBufferSize();
			start    	= off + len - blocklength;
			end	     	= off + len;
			bufaux      = java.util.Arrays.copyOfRange(buffer, start, end);
			bufused     = bufaux.length; 
		}
		*/
		
	}
	
	public void write(byte[] buffer) throws IOException {
		write(buffer, 0, buffer.length);
	}
	
	public OutputStream getStream() {
		return filewriter.getStream();
	}
	
	public void close() throws IOException {

		if (bufused>0) {
			padding =  (bufused % PADDING_BLOCK>0 ? PADDING_BLOCK - bufused % PADDING_BLOCK:0);
				System.arraycopy(bufaux, 0, bufferin , 0, bufused);
				int end = bufused;
				for(int n=0; n<padding;n++) {
					bufferin[end] = 0x20;
					end++;
				}

				encryptWorkbuffer(bufferin, 0, end);
		
				if (cacheenabled) {
					outcache.close();
					this.fileserver.addToCache(this.filewriter.getFileWSupport().url, fs.cachefile);
				}
		}
		filewriter.close();
	}
	
	@Override
	public String getRelativeUrl() {
		return filewriter.getRelativeUrl();
	}

	@Override
	public String getName() {
		return filewriter.getName();
	}
	
	@Override
	public int bytesWritten() {
		return filewriter.bytesWritten();
	}
	@Override
	public void write(int b) throws IOException {
		throw new IOException("Not implemented");
	}
	@Override
	public String getId() {
		return getRelativeUrl();
	}
	@Override
	public String getAbsolutePath() {
		return filewriter.getAbsolutePath();
	}
	
}
