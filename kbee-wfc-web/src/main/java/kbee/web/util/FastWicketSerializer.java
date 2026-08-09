package kbee.web.util;

import org.apache.wicket.serialize.ISerializer;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;
import org.wicketstuff.pageserializer.common.listener.ISerializationListener;

import com.novamens.kbee.content.multidimensional.ClassificationDisplayNameExtractor;




public class FastWicketSerializer implements ISerializer {


	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FastWicketSerializer.class.getName());

	
	private final FSTConfiguration fastSerializationConfig;

	private ISerializationListener listener;

	/**
	 * Build a Fast serializer with a default sensible configuration.
	 */
	public FastWicketSerializer()
	{
		this(getDefaultFSTConfiguration());
	}

	/**
	 * Build a Fast serializer with a custom configuration.
	 */
	public FastWicketSerializer(FSTConfiguration config)
	{
		fastSerializationConfig = config;
	}

	public static final FSTConfiguration getDefaultFSTConfiguration()
	{
		FSTConfiguration config = FSTConfiguration.createDefaultConfiguration();
		//config.setIgnoreSerialInterfaces(false);
		return config;
	}

	@Override
	public byte[] serialize(Object object)
	{
		Exception exception = null;

		try
		{
			FSTObjectOutput out = fastSerializationConfig.getObjectOutput();

//			if (listener != null)
//			{
//				out.setListener(new ListenerAdapter(listener));
//				listener.begin(object);
//			}
			out.writeObject(object);
			out.setListener(null);

			return out.getCopyOfWrittenBuffer();
		}
		catch (Exception e)
		{
			exception = e;
			logger.error(e);
			throw new RuntimeException();
//			throw new FastWicketSerialException(String.format(
//					"Unable to serialize the object %1$s", object), e);
		}
		finally
		{
			if (listener != null)
			{
				listener.end(object, exception);
			}
		}
	}

	/**
	 * Define a listener to inspect the serialization process.
	 * 
	 * @param listener
	 * @return the Serializer for chaining.
	 */
	public FastWicketSerializer setListener(ISerializationListener listener)
	{
		this.listener = listener;
		return this;
	}

	/**
	 * @return the listener used to inspect the serialization process.
	 */
	public ISerializationListener getListener()
	{
		return listener;
	}

	@Override
	public Object deserialize(byte[] data)
	{
		try
		{
			return fastSerializationConfig.getObjectInput(data).readObject();
		}
		catch (Exception e)
		{
			throw new RuntimeException();
//			throw new FastWicketSerialException("Unable to deserialize the data", e);
		}
	}

//	static class ListenerAdapter implements FSTSerialisationListener
//	{
//		private final ISerializationListener listener;
//
//		public ListenerAdapter(ISerializationListener listener)
//		{
//			this.listener = listener;
//		}
//
//		@Override
//		public void objectWillBeWritten(Object obj, int streamPosition)
//		{
//			listener.before(streamPosition, obj);
//		}
//
//		@Override
//		public void objectHasBeenWritten(Object obj, int oldStreamPosition, int streamPosition)
//		{
//			listener.after(streamPosition, obj);
//		}
//	}
}