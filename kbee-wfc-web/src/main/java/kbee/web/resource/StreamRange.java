package kbee.web.resource;

import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class StreamRange extends InputStream {
	private InputStream stream;
	//private int readed = 0;
	private Integer from, to;

	public StreamRange(InputStream stream, String range) throws IOException {
		this.stream = stream;
		
		String[] ranges = range.split("-");
		String fromstr = ranges[0];
		fromstr = fromstr.replace("bytes","");
		from = Integer.parseInt(fromstr.trim());
		if (ranges.length == 2) {
			String tostr = ranges[1];
			int s = tostr.indexOf("/");
			tostr = tostr.substring(0,s);
			to = Integer.parseInt(tostr);
		}
		
		open();
	 }
 		 
	public void open() throws IOException  {
		if (from>0 && (to==null || from<to)) {
			stream.skip(from);
			//readed = from;
		}	
	}
	
	public int available() throws IOException {
		return stream.available();
	}

	public int read(byte[] buffer, int offset, int len) throws IOException 	{
		int r = stream.read(buffer, offset, len);
		//if (r>0) readed += r; 
		return r;
	}
	
	public int read(byte[] buffer) throws IOException 	{
		return read(buffer, 0, buffer.length);
	}
	
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public int read() throws IOException {
		throw new IOException("Not implemented");
	}

}
