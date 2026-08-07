package com.novamens.kbee.thumbnail;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import net.coobird.thumbnailator.filters.Watermark;
import net.coobird.thumbnailator.geometry.Positions;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.novamens.io.FileInputStream;
import com.novamens.kbee.kbfs.v1.IdBasedDirectoryStrategy;
import com.novamens.kbee.kbfs.v1.KbeeFileServer;
import com.novamens.kbee.kbfs.v1.SubDirGenerationStrategyContext;
import com.novamens.kbee.kbfs.v1.SubDirectoryGenerationStrategy;
import com.novamens.kbee.metrics.KbeeSystemMetricsService;
import com.novamens.kbfs.v1.FSInputStream;
import com.novamens.kbfs.v1.FSOutputStream;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.FSUtils;
import kbee.util.PropertiesFactory;


/**
 * Thumbnails Server
 */
@Deprecated
public class KbeeThumbnailService implements ThumbnailService {

	static private final int BUFFER_SIZE = 8192;
	
	 private String rootdir;
	
	static private final String VIDEO_EXECUTABLE 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.thumbnailserver.video.executable", "ffmpeg").trim();
	static private final String idocSet	 			= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.iconset", "version2").trim();

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeThumbnailService.class.getName());

	static private Map<String, String> thumbnails = new HashMap<String, String>();
	
	static {						

		 thumbnails.put("pdf-sm", 			 idocSet+"/pdfsm.png"); 
		 thumbnails.put("msexcel-sm", 		 idocSet+"/excelsm.png"); 
		 thumbnails.put("mspowerpoint-sm", 	 idocSet+"/powerpointsm.png");
		 thumbnails.put("msword-sm", 		 idocSet+"/wordsm.png");
		 thumbnails.put("audio-sm",	 		 idocSet+"/audiosm.png");
		 							
		 thumbnails.put("pdf", 				 idocSet+"/pdf.png"); 
		 thumbnails.put("msexcel", 			 idocSet+"/excel.png"); 
		 thumbnails.put("mspowerpoint", 	 idocSet+"/powerpoint.png");
		 thumbnails.put("msword", 			 idocSet+"/word.png");
		 thumbnails.put("audio",			 idocSet+"/audio.png");

		 thumbnails.put("zip", 				 idocSet+"/zip.png");
		 thumbnails.put("zip-sm", 			 idocSet+"/zipsm.png");
		 
		 thumbnails.put("file", 			 idocSet+"/file.png");
		 thumbnails.put("file-sm", 			 idocSet+"/filesm.png");
		 
		 thumbnails.put("text", 			 idocSet+"/file.png");
		 thumbnails.put("text-sm", 			 idocSet+"/filesm.png");
		 
		 thumbnails.put("link", 			 idocSet+"/link.png");
		 thumbnails.put("link-sm", 			 idocSet+"/linksm.png");
		 					
		 thumbnails.put("image", 			 idocSet+"/image.png");
		 
		 thumbnails.put("msg", 				 idocSet+"/msg.png");
		 thumbnails.put("msg-sm",			 idocSet+"/msgsm.png");
		 
		 thumbnails.put("video",			 idocSet+"/video.png");
		 
		 thumbnails.put("xml", 				 idocSet+"/xml.png");
		 thumbnails.put("xml-sm",			 idocSet+"/filesm.png");
		 
		 thumbnails.put("html", 			 idocSet+"/html.png");
		 thumbnails.put("html-sm", 			 idocSet+"/htmlsm.png");
		 
		 thumbnails.put("exe", 				 idocSet+"/exe.png");
		 thumbnails.put("exe-sm", 			 idocSet+"/exesm.png");
		 
		 thumbnails.put("video-watermark",	 "defaults/video-watermark.png");
	}
	
	private class Thp {
		public int th_w;
		public int th_h;
		public int img_w;
		public int img_h;
		public int wo;
		public int ho;
		public boolean use_image =false;
	}
	
	
	
	private KbeeFileServer file_server;
	
	private SubDirectoryGenerationStrategy dirgenerator;
	private PDFThumbnailGenerator pdfgen;
	private ImageThumbnailGenerator imagegen;
	// private VideoThumbnailGenerator videogen;
	
	private boolean isEncrypted = false;
	private boolean usesCache = false;

	private Map<String, String> defaultFormats = thumbnails ; // new HashMap<String, String>();
	
	private byte buffer[] = new byte[ BUFFER_SIZE ];

	private     Counter  metric_cache_hits;
	private     Counter  metric_cache_miss;				
	

	
	private boolean isStarted = false;
	
	public KbeeThumbnailService() {
	}

	
	
	public void start() {
		
		synchronized (this) {
			try {
				if (!this.isStarted) {
					
					logger.debug("Starting ThumbnailService");
					
					dirgenerator 	= new IdBasedDirectoryStrategy();
					imagegen 		= new ImageThumbnailGenerator();
					pdfgen 			= new PDFThumbnailGenerator();
				}
			}
			catch (Exception e) {
				logger.error(e);
				throw new KbeeRuntimeException(e);
			}
			finally {
				this.isStarted = true;
			}
		}
	}
	
	
	/**
	 * pngs -> png
	 * pdf  -> jpg
	 *
	 */
	@Override	
	public File getThumbnailFile(String id, String domain, File srcfile, ThumbnailSize size) throws IOException {
		
		if (!isStarted)
			start();
		
		String url;
		String ext = null;  
		
		if (srcfile!=null && srcfile.getName()!=null) {
		
			String ex=FilenameUtils.getExtension(srcfile.getName()).toLowerCase();
			if (ex.equals("png"))
				ext="png";
			else if (ex.equals("webp"))
				ext="webp";
			else
				ext="jpg";
		}
		else {
			ext="jpg";
		}
		

		String thumbnailName = getThumbnailNameFromId(id, ext, size);

		url = getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext(thumbnailName, id, domain, FileServerV1.FAST));

		
		logger.debug("getThumbnailFile -> " + url);
		// Si no esta en el File Server -> lo genera
		//
		File file = ((KbeeFileServer) getFileServer()).getFileToRead(url);
		if (file==null) {
			getCounterCacheMiss().inc();
			url = add(id, domain, srcfile, thumbnailName, size);
			
			file= ((KbeeFileServer) getFileServer()).getFileToRead(url);
			
			if (logger.isDebugEnabled()) {
				if (file.exists()) 
					logger.debug("File ok " + url);
				else
					logger.debug("File doesnt exist " + url);
			}

		} else {
			getCounterCacheHits().inc();
		}
		return file;
		
	}

	
	
	public void removeAll() throws IOException {					
		try {
			
			logger.debug("Closing Server");
			close();
			
			logger.debug("Removing dir: "+ getRoot()); 
			KbeeFileUtils.forceDelete(new File(getRoot()));
			
			start();
			
			}
		catch (IOException e) {
			logger.error(e);
			throw(e);
		}	
		catch (Exception e) {
			logger.error(e);
				throw(e);
			}
		}

	
	
	
	/** 
	 * Thumbnail de resources Externos con link a YouTube (obtiene el thumbnail de Youtube)
	 */
	@Override
	public File getThumbnailFile(String id, String domain, String urlYT, ThumbnailSize size) throws IOException {

		if (!isStarted)
			start();

		
		String thumbnailName =  getThumbnailNameFromId(id, "jpg", size);
		String url = getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext(thumbnailName, id, domain, FileServerV1.FAST));
		
		File file = ((KbeeFileServer) getFileServer()).getFileToRead(url);
		
		if (file==null) 
		{
			getCounterCacheMiss().inc();
			url = getFileYT(id, domain, urlYT);
			file= ((KbeeFileServer) getFileServer()).getFileToRead(url);
			addWatermark(file, file);
		}
		else
			getCounterCacheHits().inc();
		return file;
	}
	
	
	
	@Override
	public String getDefaultThumbnail(String key) {
		return defaultFormats.get(key);
	}

	
	/**
	 * @param objectid
	 * @param file
	 * @param domainname
	 * @return
	 * @throws IOException
	 */
		
	@Override
	public void evict(String id, String domain) throws IOException {
		
		if (!isStarted)
			start();

		String url;
		
		ThumbnailSize arr_size [] = {ThumbnailSize.LARGE,
									 ThumbnailSize.MEDIUM,
									 ThumbnailSize.MINI,
									 ThumbnailSize.SMALL,									 
								     ThumbnailSize.W980,
									 ThumbnailSize.AVATAR_STATUS};
		String thumbnailName;
		
		for (ThumbnailSize size: arr_size) {
			thumbnailName =  getThumbnailNameFromId(id, "jpg", size);
			url = getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext(thumbnailName, id, domain, FileServerV1.FAST));
			getFileServer().remove(url);
			
			thumbnailName =  getThumbnailNameFromId(id, "png", size);
			url = getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext(thumbnailName, id, domain, FileServerV1.FAST));
			getFileServer().remove(url);
		}
	}

	
	public void close() {
		
		logger.debug("closing Thumbnail Server.");
		
		if (getFileServer()!=null)
			getFileServer().close();		
		
		file_server=null;
		dirgenerator = null;
		imagegen = null;
		pdfgen = null;
		
		this.isStarted = false;
 	}

	
  	public boolean isEncrypted() 	{
  		return isEncrypted;
  	}

	
  	
  	public boolean usesCache() 		{
		return usesCache;
	}

  	
	/**
	 * @param url
	 * @throws IOException
	 */
	public void remove(String url) throws IOException {
		this.getFileServer().remove(url); 
	}


	private String generateThFromPdf(String id, String domain, File srcfile, String thumbnailName, ThumbnailSize size) throws IOException {

		FSOutputStream writer = null;
		
		try {
			
			writer = getFileServer().getFSOutputStream(thumbnailName, id, domain);
			
			if (this.pdfgen.generateThumbnailToOutputStream(srcfile, (OutputStream) writer, size.getWidth(), size.getHeight())) 
				return writer.getRelativeUrl();
			else {	
				
				writer.close();

				logger.debug(writer.getAbsolutePath());
				KbeeFileUtils.forceDelete(new File(writer.getAbsolutePath()));
				
				String th_src_url;
				
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					th_src_url = getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("pdfsm.png", "pdf-sm", "kbee", FileServerV1.FAST));
				else
					th_src_url = getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("pdf.png", "pdf", "kbee", FileServerV1.FAST));
				
				return putInFS(id, domain, srcfile, size, th_src_url);
			}
		}
		finally {
			if (writer!=null)
				writer.close();
		}
	}
	
	/** -------------------------------------------------------------------------------------------
	 */
	private String generateThFromVideo(String id, String domain, File srcfile, String thumbnailName, ThumbnailSize size) throws IOException {
		try {

			String rel_dest = getFileServer().getRelativeURLForFile(thumbnailName, id, domain);
			String dest 	= getFileServer().getRootDirectory() +File.separator + rel_dest;

			File fullpath = new File(FilenameUtils.getFullPathNoEndSeparator(dest));
			
			if (!fullpath.exists()) 
				KbeeFileUtils.forceMkdir(fullpath);
			
			@SuppressWarnings("unused")
			Process process = new ProcessBuilder(VIDEO_EXECUTABLE, "-i",srcfile.getAbsolutePath(), "-ss","00:00:1.435", "-f","image2", "-vframes","1", dest).start();
			try {
				
				Thread.sleep(900);
				
			} catch (InterruptedException e) 
			{
				
			}
			
			File destfile = new File(dest);

			long destsize = FileUtils.sizeOf(destfile);
			getFileServer().addSize(destsize);
			 
			return  rel_dest;


		} catch (Throwable e) {
			logger.error(e);
			return putInFS(id, domain, srcfile, size, getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("video.png", "video", "kbee", FileServerV1.FAST)));
		}
	}
	

	/** -------------------------------------------------------------------------------------------
	 * Thumbnai de resources Externos con link a YouTube (obtiene el thumbnail de Youtube)
	 */
	private String getFileYT(String id, String domain, String urlStr) throws IOException {
	
		if (!isStarted)
			start();

		BufferedInputStream reader=null;
		String[] partes = urlStr.split("v=");
		if(partes!=null&&partes.length==2){
			String urlThumnbail = "http://img.youtube.com/vi/"+partes[1].trim()+"/default.jpg";
			URL url = new URL(urlThumnbail);
			URLConnection conn = url.openConnection();
			reader = new BufferedInputStream( conn.getInputStream());
			FSOutputStream out = getFileServer().getFSOutputStream( getThumbnailNameFromId(id, "jpg", ThumbnailSize.LARGE), id, domain);
			int read = 0;
			byte [] bufferin = new byte[BUFFER_SIZE];
			try {
		        while ((read = reader.read(bufferin)) >= 0) {
		        	out.write(bufferin, 0, read); 
		        }
			}finally{
				out.close();
				reader.close();
			}
			return out.getRelativeUrl();
		}
		return null;
	}

	/** 
	 */
	private void addWatermark(File inputFile, File outputFile) throws IOException {

		if (!isStarted)
			start();

		
		BufferedImage originalImage = ImageIO.read(inputFile);
		File watermarkImage = ((KbeeFileServer) getFileServer()).getFileToRead(getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("video-watermark.png", "video-watermark", "kbee", FileServerV1.FAST)));
		BufferedImage watermarkBufferedImage = ImageIO.read(watermarkImage);
		Watermark filter = new Watermark(Positions.BOTTOM_RIGHT, watermarkBufferedImage, 1f);
		BufferedImage captionedImage = filter.apply(originalImage);
		ImageIO.write(captionedImage, "jpg", outputFile);
		
	}


	/** 
	 */
	private Thp getThp(File srcfile, ThumbnailSize size) throws IOException {
		
		if (!isStarted)
			start();

		
		Thp ret = new Thp();


		
		try {
		
			String ex=FilenameUtils.getExtension(srcfile.getName()).toLowerCase();
			if (ex.equals("webp")) {
				ret.use_image=true;
				return ret;
			}
			
			
			BufferedImage bimg = ImageIO.read(srcfile);
			
			if (bimg==null)
				return null;
				
			ret.th_w = size.getWidth(); 
			ret.th_h = size.getHeight();
			ret.img_w = bimg.getWidth();
			ret.img_h = bimg.getHeight();
	
			
			// if image is smaller that Thumbnail -> use actual image
			if (ret.img_w <= ret.th_w  && 
				ret.img_h <= ret.th_h) {
				ret.use_image=true;
				return ret;
			}
				
			
			if (ret.th_w!=0 && ret.th_h!=0) {
				
				// Si la imagen es más chica que el thumbnail requerido -> devuelve la imagen
				//
				if (ret.img_w <= ret.th_w &&  ret.th_h <= ret.img_h) {
					ret.wo=ret.img_w;
					ret.ho=ret.img_h;
					return ret;
				}
				
				// Si la imagen es más grande que el thumbnail requerido, recorta
				//
				
				int delta_w = (ret.img_w-ret.th_w);
				int delta_h = (ret.img_h-ret.th_h);
	
				if (delta_h<delta_w) {
					ret.ho 			   = ret.th_h;
					ret.wo 			   =  (ret.img_h>0) ? ret.img_w * ret.ho / ret.img_h : (int) (ret.th_w);
				} 
				else {
					ret.wo 			   = ret.th_w;
					ret.ho 			   =  (ret.img_w>0) ? ret.img_h * ret.wo / ret.img_w : (int) (ret.th_h);
				}
				
				return ret;
			}
	
			// Si h es 0
			//	
			else if (ret.th_h==0 && ret.th_w!=0) {
				
				int delta_w = (ret.img_w-ret.th_w);
				
				// Si el ancho del th es mas grande que el ancho de la imagen devuelve la imagen
				if (delta_w<0) {
					ret.ho=ret.img_h;
					ret.wo=ret.img_w;
					return ret;
				}
				else {
					
					// Si el ancho del th es mas chico que el ancho de la imagen resizea proporcional
					
					ret.wo 			   =  ret.th_w;
					ret.ho 			   =  (ret.img_w>0) ? ret.img_h * ret.wo / ret.img_w : (int) (ret.img_h);
					if (ret.ho<=0)
						ret.ho=ret.img_h;
	
					return ret;
				}
			}
			// Si w es 0
			//	
			else {
							
				int delta_h = (ret.img_h-ret.th_h);
				
				// Si el alto del th es mas grande que el alto de la imagen devuelve la imagen
				//
				if (delta_h<0) {
					ret.ho=ret.img_h;
					ret.wo=ret.img_w;
					return ret;
				}
				else {
					
					// Si el alto del th es mas chico que el alto de la imagen resizea proporcional
					//
					ret.ho 			   = ret.th_h;
					ret.wo 			   =  (ret.img_h>0) ? ret.img_w * ret.ho / ret.img_h : (int) (ret.img_w);
					if (ret.wo<=0)
						ret.wo=ret.img_w;
	
					return ret;
				}
			}
		
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
		
	}
	
	
	
	
	private String generateThFromImage(String id, String domain, File srcfile, String thumbnailName, ThumbnailSize size) throws IOException {

		if (!isStarted)
			start();

		
		FSOutputStream writer = null;

		try { 
			
			Thp thp = getThp(srcfile, size); 
			writer = getFileServer().getFSOutputStream(thumbnailName, id, domain);
			
			if (thp.use_image) {
				copy(srcfile,writer);	
				return writer.getRelativeUrl();
			}
			else if (getImageGenerator().generateThumbnailToOutputStream(srcfile, (OutputStream) writer, thp.wo, thp.ho)) 
				return writer.getRelativeUrl();
			else
				return putInFS(id, domain, srcfile, size, getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("image.png", "file", "kbee", FileServerV1.FAST)));
 		
		} finally {
			if (writer!=null)
				writer.close();
		}
	}

	private ImageThumbnailGenerator getImageGenerator() {
		return imagegen;
	}

	
	/**
	 * @param id
	 * @param domain
	 * @param srcfile
	 * @param thumbnailName
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	private String add(String id, String domain, File srcfile, String thumbnailName, ThumbnailSize size) throws IOException {
		
		if (!isStarted)
			start();

		
		if (srcfile==null) {
			if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
				return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("filesm.png", "file-sm", "kbee", FileServerV1.FAST)));
			return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("file.png", "file", "kbee", FileServerV1.FAST)));
		}

		if (isImage(srcfile)) 
			return generateThFromImage(id, domain, srcfile, thumbnailName, size);
		
		if (kbee.util.FSUtils.isPdf(srcfile))
			return generateThFromPdf(id, domain, srcfile, thumbnailName, size);

		try {
	
			if (kbee.util.FSUtils.isExcel(srcfile)) {
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("excelsm.png", "msexcel-sm", "kbee", FileServerV1.FAST)));
				else
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("excel.png", "msexcel", "kbee", FileServerV1.FAST)));
			}
			
			
			if (kbee.util.FSUtils.isCSV(srcfile)) {
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("excelsm.png", "msexcel-sm", "kbee", FileServerV1.FAST)));
				else
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("excel.png", "msexcel", "kbee", FileServerV1.FAST)));
			}

						
			if (kbee.util.FSUtils.isWord(srcfile)) { 
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("wordsm.png", "msword-sm", "kbee", FileServerV1.FAST)));
				else
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("word.png", "msword", "kbee", FileServerV1.FAST)));
			}
			
			if (kbee.util.FSUtils.isPowerpoint(srcfile)) {
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("powerpointsm.png", "mspowerpoint-sm", "kbee", FileServerV1.FAST)));
				else
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("powerpoint.png", "mspowerpoint", "kbee", FileServerV1.FAST)));
			}

			if (isCompressed(srcfile)) {
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("zipsm.png", "zip-sm", "kbee", FileServerV1.FAST)));
				
				return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("zip.png", "zip", "kbee", FileServerV1.FAST)));
			}
		
			if (isAudio(srcfile)) {
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("audiosm.png", "audio-sm", "kbee", FileServerV1.FAST)));
				else
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("audio.png", "audio", "kbee", FileServerV1.FAST)));
			}
			
			if (isVideo(srcfile)) {
				return generateThFromVideo(id, domain, srcfile, thumbnailName, size);
			}
			
			if (kbee.util.FSUtils.isText(srcfile))  {
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("textsm.png", "text-sm", "kbee", FileServerV1.FAST)));
				return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("text.png", "text", "kbee", FileServerV1.FAST)));
			}

			if (isExecutable(srcfile)) {
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("exesm.png", "exe-sm", "kbee", FileServerV1.FAST)));
				return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("exe.png", "exe", "kbee", FileServerV1.FAST)));
			}
			
			if (isMsg(srcfile)) {
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("msgsm.png", "msg-sm", "kbee", FileServerV1.FAST)));
				return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("msg.png", "msg", "kbee", FileServerV1.FAST)));
			}

			if (isXml(srcfile)) {															 
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("xmlsm.png", "xml-sm", "kbee", FileServerV1.FAST)));
				return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("xml.png", "xml", "kbee", FileServerV1.FAST)));
			}
				
			if (isHtml(srcfile)) {
				if (size==ThumbnailSize.MINI || size==ThumbnailSize.AVATAR_STATUS)
					return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("htmlsm.png", "html-sm", "kbee", FileServerV1.FAST)));
				
				return putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("html.png", "html", "kbee", FileServerV1.FAST)));
			}
			
		}
			catch(java.io.IOException e1) {
				try {
					
					String info = id +" | " + domain  +" | " + srcfile.getName()  +" | " + thumbnailName  +" | " + size.getLabel();
					logger.error(e1);
					logger.error("Object; " + info);
					String ret = putInFS(id, domain, srcfile, size,  getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("file.png", "file", "kbee", FileServerV1.FAST)));
					return ret;
				}
				catch(java.io.IOException e2) {
					String info = id +" | " + domain  +" | " + srcfile.getName()  +" | " + thumbnailName  +" | " + size.getLabel();
					logger.error("Object; " + info);
					logger.error(e2);
					return null;
				}
		}
		
		try { 
			return putInFS(id, domain, srcfile, size, getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext("file.png", "file", "kbee", FileServerV1.FAST)));
		} catch(java.io.IOException e3) {
			String info = id +" | " + domain  +" | " + srcfile.getName()  +" | " + thumbnailName  +" | " + size.getLabel();
			logger.error("Object; " + info);
			logger.error(e3);
			return null;
		}
	}
	
	
	
	private String getThumbnailNameFromId(String id, String extension, ThumbnailSize size) {
		return id+"-"+size.getLabel()+"."+extension;
	 }
	/**
	 * @param id
	 * @param domain
	 * @param srcfile
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	private boolean isCompressed(File srcfile) {
		return FSUtils.isZip(srcfile.getName()); 
	}

	private boolean isVideo(File srcfile) {
		return FSUtils.isVideo(srcfile.getName()); 
	}

	private boolean isAudio(File srcfile) {
		return FSUtils.isAudio(srcfile.getName()); 
	}
								
	private boolean isExecutable(File srcfile) {
		return srcfile.getName().matches("^.*\\.(exe|EXE)$"); 
	}

	private boolean isMsg(File srcfile) {
		return srcfile.getName().matches("^.*\\.(msg|MSG)$"); 
	}

	private boolean isXml(File srcfile) {
		return FSUtils.isXml(srcfile.getName()); 
	}

	private boolean isHtml(File srcfile) {
		return FSUtils.isHTML(srcfile.getName()); 
	}

	
	private boolean isImage(File srcfile) {
		return FSUtils.isImage(srcfile.getName()); 
	}

	
	/** 
	 * @param fileServer 
	 */
	private void addDefaultImages(KbeeFileServer fileServer) {
		
		byte buffer[] = null;
		Iterator<Entry<String, String>> it = thumbnails.entrySet().iterator();

		logger.debug("add Default Images");
		
		while (it.hasNext()) {

			Entry<String, String> entry = it.next();
			
			BufferedInputStream in = null;
			FSOutputStream out = null;

			this.defaultFormats.put(entry.getKey(), entry.getValue());

			try {
				
				String url = getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext(FilenameUtils.getName(entry.getValue()), 
						                                                                                                      entry.getKey(), 
						                                                                                                      "kbee", 
						                                                                                                      FileServerV1.FAST));
				if (fileServer.getFileToRead(url)==null) {
					
					if (buffer==null)
						buffer= new byte[BUFFER_SIZE];	
					URL defaulturl = this.getClass().getResource(entry.getValue());
					if (defaulturl!=null) {
						in = new BufferedInputStream(getClass().getResourceAsStream(entry.getValue()), BUFFER_SIZE);
						logger.debug("Adding  ->" + url);
						out = fileServer.getFSOutputStream(FilenameUtils.getName(entry.getValue()), entry.getKey(), "kbee");
						int bread;
						bread=in.read(buffer, 0, BUFFER_SIZE);
						while (bread>0) {
							out.write(buffer, 0, bread);
							bread=in.read(buffer, 0, BUFFER_SIZE);
						}
						in.close();
						out.close();
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
			finally {
				if (in!=null)
					try {
						in.close();
					} catch (IOException e) {
						logger.error(e);
					}
				
				if (out!=null)
					try {
						out.close();
					} catch (IOException e) {
						logger.error(e);
					}
			}
		}
	}


	@Override
	public long getSize() {
		
		if (getFileServer()!=null)
			return getFileServer().getSize();
		return 0;
	};

	@Override
	public long getCacheMiss() {
		return getCounterCacheMiss().getCount();	
	}
	
    @Override
	public long getCacheHits() {
		return getCounterCacheHits().getCount();	
	}

    
    private void copy(File file, FSOutputStream out) throws IOException {
    	BufferedInputStream in = null;
    	try {
    			in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
				int read;
				read=in.read(buffer, 0, BUFFER_SIZE);
				while (read>0) {
					out.write(buffer, 0, read);
					read=in.read(buffer, 0, BUFFER_SIZE);
				}
		
		} finally {
			if (in!=null)
				in.close();
		}
    }
    
    
	/**
	 * Agrega un Thumbnail generado en otro lado al FS del ThumbnailServer
	 * 
	 * @param th_name
	 * @param id
	 * @param domain
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String putInFS(String id, String domain, File file, ThumbnailSize size, String th_src_url) throws IOException {

		FSInputStream in = null;
		FSOutputStream out = null;
		String ext;
		try {
				in = getFileServer().getFSInputStream(th_src_url, 0);
				if (file!=null && file.getName()!=null)
				 ext  = FilenameUtils.getExtension(file.getName()).toLowerCase().equals("png") ?"png":"jpg";
				else
					ext="jpg";
				out = getFileServer().getFSOutputStream(getThumbnailNameFromId(id, ext, size), id, domain);
				int read;
				while (in.available()>0) {
					read=in.read(buffer, 0, BUFFER_SIZE);
					out.write(buffer, 0, read);
				}
				return out.getRelativeUrl();
		
		} finally {
			if (in!=null)
				in.close();
			if (out!=null)
				out.close();
		}
		
		
	}
	
	
	private FileServerV1 getFileServer() {
		
		if (!isStarted)
			start();
		
		if (file_server==null)
			try {
				file_server	= new KbeeFileServer("Thumbnail FileServer", getRoot(), getDirGenerator());
				
				addDefaultImages(file_server);
				
			} catch (IOException e) {
				throw new KbeeRuntimeException(e);
			}
		return file_server;
	}

	
	private SubDirectoryGenerationStrategy getDirGenerator() {
		return this.dirgenerator;
	}
	

	private String getRoot() {
		if (this.rootdir!=null)
			return this.rootdir;
		this.rootdir =  ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + "thumbnail-server" + File.separator + "db";  
		return this.rootdir;
		
	}
	
	
	private Counter getCounterCacheHits() {
		if (metric_cache_hits==null) {
			KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
			metric_cache_hits = mt.getMetrics().counter(MetricRegistry.name(KbeeThumbnailService.class, "cache-hits"));
		}
		return metric_cache_hits;
	}
	
	
	private Counter getCounterCacheMiss() {
		if (metric_cache_miss==null) {
			KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
			metric_cache_miss = mt.getMetrics().counter(MetricRegistry.name(KbeeThumbnailService.class, "cache-miss"));
		}
		return metric_cache_miss;
	}
	
	
}
