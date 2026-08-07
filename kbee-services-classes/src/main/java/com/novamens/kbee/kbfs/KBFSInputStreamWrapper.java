package com.novamens.kbee.kbfs;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import com.novamens.kbfs.FileServerException;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.SimpleImageInfo;

/**
 * 
 * {@link Minio} does not implement S3 method {@link getObjectMetadata}
 * The goal of this class is  to be able to 
 * return the number of bytes uploaded into Minio Server.
 * 
 * It also calculates SHA256 of the stream 
 * and W x H px for images.
 * 
 * <b>IMPORTANT</b>. This class does not close the InputStream 
 * received as parameter it is the caller who must close it.
 * 
 */
public class KBFSInputStreamWrapper extends InputStream implements LengthAwareInputStream {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KBFSInputStreamWrapper.class.getName());
	
 	private DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMdd");
 	
 	static final int BUFFER_SIZE = 16384;
	
 	
 	private byte[] buffer= new byte[BUFFER_SIZE];
 	
	InputStream is;
	OutputStream out;
	
	long fileSize=0;
	long bytesread=0;

	//String crc32 = null;
	String sha256 = null;
	
	int width=0;
	int height=0;
	
	long length = -1;
	
	private boolean isCalculated = false;
	private boolean closed = false;
	
	private String tempFilename;
	private String filename;
	
	
	public KBFSInputStreamWrapper(InputStream is, String filename) throws FileServerException, IOException {
		this.is=is;
		this.filename=filename;
		init();
	}
	
	/**
	 * 
	 * 10 temp. sub directories x day
	 * 
	 * @throws FileServerException
	 * @throws FileNotFoundException
	 */
	private void init() throws FileServerException, FileNotFoundException {
		if (is instanceof LengthAwareInputStream) {
			length = ((LengthAwareInputStream)is).getLength();
		}
		String destdir=getWorkDir(); 		
 		this.tempFilename=destdir + File.separator + this.filename;
 		if ((new File(tempFilename)).exists()) 
			this.tempFilename = destdir + File.separator + String.valueOf(Double.valueOf(Math.random()*100000000).intValue())+"-"+filename;
 		logger.debug("Temp File: " + tempFilename);
		this.out = new BufferedOutputStream(new FileOutputStream(tempFilename), BUFFER_SIZE);
	}

	@Override
	public int read() throws IOException {
		int b =is.read();
		try {
			out.write(b);
			bytesread++;
		} catch (IOException e) {
			logger.error(e);
			throw(e);
		} catch (Exception e) {
			logger.error(e);
			throw(new IOException(e));
		}
		return b;
	}
	
	@Override
	public int available() throws IOException {
		return is.available();
	}
	

	public long getLength() {
		return length;
	}
	
	
	@Override
	public void	mark(int readlimit) {
		is.mark(readlimit);
	}
	
	
	@Override
	public boolean	markSupported() {
		return is.markSupported();
				
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int n=0;
		try {
			n=is.read(b);
			if (n>0)
				out.write(b,0,n);
			return n;
		} catch (IOException e) {
			logger.error(e);
			throw(e);
		} catch (Exception e) {
			logger.error(e);
			throw(new IOException(e));

		} finally {
 			if (n>0)
				bytesread+=n;
		}
	}

	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int n=0;
		try {
			n=is.read(b, off, len);
			if (n>0)
				out.write(b, off, n);
			return n;
		} catch (IOException e) {
			logger.error(e);
			throw(e);
		} catch (Exception e) {
			logger.error(e);
			throw(new IOException(e));
		} finally {
 			if (n>0)
				bytesread+=n;
		} 
	}
	
	@Override
	public void	reset() throws IOException  {
		is.reset();
	}
	
	@Override
	public long	skip(long n) throws IOException  {
		logger.debug("skip " + String.valueOf(n) + " we should add to bytesread");
 		long s= is.skip(n);
 		if (s>0)
 			this.bytesread=+s;
		return s;
	}
	
	
 	public long getFileSize() {
		return this.fileSize;
	}

 	
 												
 	
 	/**
	 * This method does NOT close is
	 * 
	 */
	@Override
	public void	close() throws IOException {

		try {
			if (out!=null && !closed) {
				out.close();
				closed=true;
 				if ((new File(this.tempFilename)).exists()) 
					this.fileSize=new File(this.tempFilename).length();

 				//calculateSHA256();
				calculateImageDimensions();
				
			}
		} catch (IOException e) {
			logger.error(e);
			throw(e);
		} catch (Exception e) {
			logger.error(e);
			throw(new IOException(e));
		} 
		finally {
			// remove CRC32 File
			try {
				if ((new File(this.tempFilename)).exists()) {
					long a =System.currentTimeMillis();	
					logger.debug("Deleting " + this.tempFilename + " ("+ String.valueOf(System.currentTimeMillis()-a)+" ms)");
					KbeeFileUtils.forceDelete(new File(this.tempFilename));
				}
			} catch (Exception e) {
				logger.error(e);
			}
  		}
		super.close();
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getWidth() {
		return this.width;
	}


	public String getSHA256() {
		return this.sha256;
	}

	
	//public String getCRC32() {
	//	return this.crc32;
	//}
	
	public long getBytesRead() {
		return  bytesread; 
	}
	

	/**
	private void calculateCRC32() {
		
		if (this.crc32!=null || !closed)
			return;
		
		try {
			long xcrc32 = org.apache.commons.io.FileUtils.checksumCRC32(new File(tempFilename));
			this.crc32=Long.toHexString(xcrc32);
		} 
		catch (IOException e1) {
			logger.error(e1,"crc32 ");
			this.crc32="err";
		}
	}
**/

	
	private void calculateSHA256() {
	
		if (this.sha256!=null || !closed)
			return;
		
		long start=System.currentTimeMillis();
		
	    int count;
	    MessageDigest digest;
	    
		try {

			digest = MessageDigest.getInstance("SHA-256");
			
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
			return;
		}
	    
		BufferedInputStream bis = null;
		try {

			bis = new BufferedInputStream(new FileInputStream(tempFilename));
		    
			while ((count = bis.read(buffer)) > 0)
		        digest.update(buffer, 0, count);

		    this.sha256=Base64.getEncoder().encodeToString(digest.digest());
		    
		    logger.debug( tempFilename +" -> " + this.sha256 + " | size->" + this.sha256.length() + " | " + String.valueOf(System.currentTimeMillis()-start)+" ms");
		    
		    
		} catch (FileNotFoundException e) {
				logger.error(e);
				return;
		} catch (IOException e) {
				logger.error(e);
				return;
		}
		finally {
			if (bis!=null) {
				try {
					bis.close();
					
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}
	
	
	/**
	 * 
	 */
	private void calculateImageDimensions()
	 {
		if (this.isCalculated || !closed)
			return;
		try {
			if (kbee.util.FSUtils.isGeneralImage(filename)) {
				 	SimpleImageInfo imageInfo;
					int nw, nh;
					try {
						imageInfo = new SimpleImageInfo(new File(tempFilename));
						nw  = imageInfo.getWidth();
						nh = imageInfo.getHeight();
					}
					catch (IOException e) {
						logger.warn(e);
						nw = 0;
						nh = 0;
					}
				this.width=nw;
				this.height=nh;
			}
		}
		catch (Exception e) {
			logger.error(e);
 		} finally {
			this.isCalculated=true;
		}
	}

	private String getWorkDir() {
		String dir = ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + "sha256" + File.separator + workdf.format(OffsetDateTime.now());
		File file = new File(dir);
 		if (!file.exists())  {
			try {
				KbeeFileUtils.forceMkdir(file);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return dir;
	}

}
