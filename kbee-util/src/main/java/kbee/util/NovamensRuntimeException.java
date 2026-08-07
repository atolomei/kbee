// $Id: NovamensRuntimeException.java,v 1.4 2007/03/08 00:46:47 alexis Exp $

package kbee.util;

/**
 * The class <code>NovamensRuntimeException</code> and its subclasses are a
 * form of <code>RuntimeException</code> that indicates conditions that a
 * reasonable application built by Novamens S.A. might want to catch. A method
 * is not required to declare in its <code>throws</code> clause any subclasses
 * of <code>NovamensRuntimeException</code> that might be thrown during the
 * execution of the method but not caught.
 */
public class NovamensRuntimeException extends RuntimeException {

	/**
	 * Serialization identifying number.
	 */
	private static final long serialVersionUID = 7769751218169968667L;

	/**
	 * Constructs a new runtime exception with the specified cause and a detail
	 * message of <tt>(cause==null ? null : cause.toString())</tt> (which
	 * typically contains the class and detail message of <tt>cause</tt>).
	 * This constructor is useful for runtime exceptions that are little more
	 * than wrappers for other throwables.
	 * 
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public NovamensRuntimeException(final Throwable cause) {
		super(cause.getMessage() != null ? cause.getMessage() : cause.toString(), cause);
	}

	/**
	 * Constructs a new runtime exception with the specified detail message and
	 * cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is
	 * <i>not</i> automatically incorporated in this runtime exception's detail
	 * message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public NovamensRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new runtime exception with the specified detail message. The
	 * cause is not initialized, and may subsequently be initialized by a call
	 * to {@link #initCause}.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the {@link #getMessage()} method.
	 */
	public NovamensRuntimeException(final String message) {
		super(message);
	}
}
