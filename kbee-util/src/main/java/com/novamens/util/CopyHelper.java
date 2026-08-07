package com.novamens.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * @author alexis
 * @version $Id: CopyHelper.java,v 1.5 2007/03/08 00:46:47 alexis Exp $
 */
public final class CopyHelper {

	/**
	 * Constante con el tama#o predeterminado del buffer utilizado para realizar
	 * copias.
	 */
	private static final int bufferSize = 8 * 1024;

	/**
	 * Constructor privado de CopyHelper.
	 */
	private CopyHelper() {
		super();
	}

	/**
	 * Copia todo el contenido del InputStream al OutputStream.
	 * 
	 * @param in
	 *            el InputStream donde obtener los datos.
	 * @param out
	 *            el OutputStream donde copiar los datos.
	 * @throws IOException
	 *             si ocurre alg#n error de I/O.
	 */
	public static void copy(final InputStream in, final OutputStream out)
			throws IOException {
		copy(in, out, 0, -1);
	}

	/**
	 * Copia todo el contenido del Reader al writer.
	 * 
	 * @param reader
	 *            el Reader donde obtener los datos.
	 * @param writer
	 *            el Writer donde copia los datos.
	 * @throws IOException
	 *             si ocurre alg#n error de I/O.
	 */
	public static void copy(final Reader reader, final Writer writer)
			throws IOException {
		copy(reader, writer, 0, -1);
	}

	/**
	 * Copia todo el contenido especificado del OutputStream al InputStream.
	 * 
	 * @param in
	 *            el InputStream donde obtener los datos.
	 * @param out
	 *            el OutputStream donde copiar los datos.
	 * @param offset
	 *            la posici#n desde donde empezar a leer.
	 * @param byteCount
	 *            la cantidad de bytes a copiar.
	 * @throws IOException
	 *             si ocurre alg#n error de I/O.
	 */
	public static void copy(final InputStream in, final OutputStream out,
			final long offset, long byteCount) throws IOException {
		final byte buffer[] = new byte[bufferSize];
		int len = bufferSize;

		long skipped = 0;
		while (skipped < offset) {
			final long count = in.skip(offset);
			if (count < 0) {
				throw new IOException("Failed to skip " + offset
						+ " bytes from stream");
			}
			skipped += count;
		}

		if (byteCount >= 0) {
			while (byteCount > 0) {
				if (byteCount < bufferSize) {
					len = in.read(buffer, 0, (int) byteCount);
				} else {
					len = in.read(buffer, 0, bufferSize);
				}

				if (len == -1) {
					break;
				}

				out.write(buffer, 0, len);
				byteCount -= len;
			}
		} else {
			while (true) {
				len = in.read(buffer, 0, bufferSize);
				if (len < 0) {
					break;
				}
				out.write(buffer, 0, len);
			}
		}
	}

	/**
	 * Copia el contenido especificado del Reader al writer.
	 * 
	 * @param in
	 *            el Reader donde obtener los datos.
	 * @param out
	 *            el Writer donde copia los datos.
	 * @param offset
	 *            la posici#n desde donde empezar a leer.
	 * @param byteCount
	 *            la cantidad de bytes a copiar.
	 * @throws IOException
	 *             si ocurre alg#n error de I/O.
	 */
	public static void copy(final Reader in, final Writer out,
			final int offset, long byteCount) throws IOException {
		final char buffer[] = new char[bufferSize];
		int len = bufferSize;

		long skipped = 0;
		while (skipped < offset) {
			final long count = in.skip(offset);
			if (count < 0) {
				throw new IOException("Failed to skip " + offset
						+ " bytes from stream");
			}
			skipped += count;
		}

		if (byteCount >= 0) {
			while (byteCount > 0) {
				if (byteCount < bufferSize) {
					len = in.read(buffer, 0, (int) byteCount);
				} else {
					len = in.read(buffer, 0, bufferSize);
				}

				if (len == -1) {
					break;
				}

				out.write(buffer, 0, len);
				byteCount -= len;
			}
		} else {
			while (true) {
				len = in.read(buffer, 0, bufferSize);
				if (len == -1) {
					break;
				}
				out.write(buffer, 0, len);
			}
		}
	}

}
