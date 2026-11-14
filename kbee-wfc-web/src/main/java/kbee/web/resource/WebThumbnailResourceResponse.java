package kbee.web.resource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.novamens.content.base.Resource;
import com.novamens.content.service.ResourceService;
import com.novamens.thumbnail.ThumbnailSize;

public class WebThumbnailResourceResponse extends AbstractResourceResponse {

	private static final byte[] PLACEHOLDER = createPlaceholder();

	private boolean missing = false;

	public WebThumbnailResourceResponse(URI uri) {
		super(uri);
	}

	private static byte[] createPlaceholder() {
		try {
			BufferedImage img = new BufferedImage(160, 160, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			g.setColor(new Color(0xE0E0E0));
			g.fillRect(0, 0, 160, 160);
			g.dispose();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(img, "png", out);
			return out.toByteArray();
		} catch (IOException e) {
			return new byte[0];
		}
	}

	public long getContentLength() {
		if (missing) {
			return PLACEHOLDER.length;
		}
		try {
			if (super.getContentLength()>0) {
				return super.getContentLength();
			}	
			File thumbnailfile = getResource().getService(ResourceService.class).getThumbnailFile(getSize());
			long length = thumbnailfile!=null ? thumbnailfile.length() : 0;
			setContentLength(length);
			return length;
		}
		catch (Exception e) {
			logger.warn("Resource content not available, serving placeholder. URI -> " + getURI() + " | " + e.getMessage());
			missing = true;
			setContentLength(PLACEHOLDER.length);
			return PLACEHOLDER.length;
		}
	}

	public ThumbnailSize getSize() {
		String name = getURI().getName();
		String segments[] = name.split("/");
		String sizesegments[] = segments[0].split("-");
		ThumbnailSize size = ThumbnailSize.valueOf(sizesegments[1].toUpperCase());
		return size;
	}
	
	public String getResourceId() {
		String name = getURI().getName();
		String segments[] = name.split("/");
		String id = null;
		if (segments.length>0 && segments[0]!=null && segments[0].toLowerCase().contains("thumbnail")) {
			id = segments[1];
		}
		return id;
	}
	
	@Override
	public InputStream openStream() throws IOException {
		if (!missing) {
			try {
				return getResource().getService(ResourceService.class).getThumbnail(getSize());
			}
			catch (Exception e) {
				logger.warn("Resource content not available, serving placeholder. URI -> " + getURI() + " | " + e.getMessage());
				missing = true;
			}
		}
		return new ByteArrayInputStream(PLACEHOLDER);
	}
	
	public String getContentType() {
		return missing ? "image/png" : "image/jpg";
	}

	@Override
	protected Resource loadResource() throws IOException {
		return getResource(getResourceId());
	}
}