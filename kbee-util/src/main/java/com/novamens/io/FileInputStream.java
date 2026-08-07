/*
 * Created on Feb 9, 2004
 *
 * version $id$
 */
package com.novamens.io;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


/**
 * @author lleggieri
 * 
 * version $id$
 */
public class FileInputStream extends InputStream {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FileInputStream.class.getName());
	
	/**
	 * Internal file where bytes are read from.
	 */
	private RandomAccessFile mFile;

	/**
	 * current mark position.
	 */
	private long mMarkPos = 0;

	/**
	 * Constructor takes a File object. The file is opened with read permission.
	 * 
	 * @param pFile
	 *            the file to be opened for reading.
	 * @throws FileNotFoundException
	 *             if file doesn't exist.
	 * 
	 */
	public FileInputStream(final File pFile) throws FileNotFoundException {
		this.mFile = new RandomAccessFile(pFile.getAbsolutePath(), "r"); //$NON-NLS-1$
	}

	/**
	 * @return Always true: marks are supported on random access files.
	 */
	@Override
	public boolean markSupported() {
		return true;
	}

	/**
	 * Remembers the current position in the file.
	 * 
	 * @param readLimit
	 *            is not used
	 */
	@Override
	public void mark(final int readLimit) {
		try {
			this.mMarkPos = this.mFile.getFilePointer();
		} catch (final IOException e) {
			logger.error(e);
		}
	}

	/**
	 * Go back to our last marked position (or to the beginning of the file).
	 * 
	 * @throws IOException
	 *             if an I/O error occurrs.
	 */
	@Override
	public void reset() throws IOException {
		this.mFile.seek(this.mMarkPos);
	}

	/**
	 * Reads a byte of data from this input stream.
	 * 
	 * @return the next byte of data, or <code>-1</code> if the end of the
	 *         file is reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read() throws IOException {
		return this.mFile.read();
	}

	/**
	 * Reads an array of bytes from the input stream.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of the
	 *         file has been reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */

	@Override
	public int read(final byte b[]) throws IOException {
		return this.mFile.read(b, 0, b.length);
	}

	/**
	 * Reads an array of bytes from the input stream.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @param off
	 *            the start offset of the data.
	 * @param len
	 *            the maximum number of bytes read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of the
	 *         file has been reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read(final byte b[], final int off, final int len)
			throws IOException {
		return this.mFile.read(b, off, len);
	}

	/**
	 * Close the file.
	 * 
	 * @throws IOException
	 *             if an I/O error occurrs.
	 */
	@Override
	public void close() throws IOException {
		if (this.mFile != null) {
			this.mFile.close();
		}
		this.mFile = null;
	}

	/**
	 * Be sure the file is closed.
	 */
	@Override
	protected void finalize() {
		if (this.mFile != null) {
			try {
				this.close();
			} catch (final Exception e) {
			}
		}
	}

}
