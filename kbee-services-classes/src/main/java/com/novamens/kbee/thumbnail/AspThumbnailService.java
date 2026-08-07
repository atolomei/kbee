package com.novamens.kbee.thumbnail;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.novamens.kbfs.KBFSService;
import com.novamens.kbfs.v1.FSInputStream;
import com.novamens.kbfs.v1.FSOutputStream;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailGenerator;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.FSUtils;
import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;

/**
 * Thumbnails Server
 */
@SuppressWarnings("unused")
public class AspThumbnailService implements ThumbnailService {
	
	 private static Logger logger = Logger.getLogger(AspThumbnailService.class.getName());
	
	 
	 static private final int BUFFER_SIZE = 8192;

	 private KbeeFileServer fileServer;
	 
	 private class Url {
		 private String name, path;
		 public Url(String name, String path) {
			 setName(name);
			 setPath(path);
		 }
		 public String getName() {
			 return name;
		 }
		 public void setName(String name) {
			 this.name = name;
		 }
		 public String getPath() {
			return path;
		 }
		 public void setPath(String path) {
			this.path = path;
		 }	
	 }
	 
	 private class Cache {
		 private Counter metric_cache_hits;
		 private Counter metric_cache_miss;				
		 public File get(Url url) throws IOException {
			File file = ((KbeeFileServer)getFileServer()).getFileToRead(url.getPath());
			logger.debug("getThumbnailFile -> " + url.getPath());
			if (file!=null) 
				getCounterCacheHits().inc();
			else
				getCounterCacheMiss().inc();
			return file;
		 }
		 public FSOutputStream getWriter(Url url, String id, String domain) throws IOException {
			FSOutputStream writer = getFileServer().getFSOutputStream(url.getName(), id, domain);
			return writer;
		 }
		 private Counter getCounterCacheHits() {
			if (metric_cache_hits==null) {
				KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
				metric_cache_hits = mt.getMetrics().counter(MetricRegistry.name(AspThumbnailService.class, "cache-hits"));
			}
			return metric_cache_hits;
		 }
		 private Counter getCounterCacheMiss() {
			if (metric_cache_miss==null) {
				KbeeSystemMetricsService mt = ServiceLocator.getService(KbeeSystemMetricsService.class);
				metric_cache_miss = mt.getMetrics().counter(MetricRegistry.name(AspThumbnailService.class, "cache-miss"));
			}
			return metric_cache_miss;
		}
	};
	
	private Cache cache = new Cache();
	private List<ThumbnailGenerator> generators;
	
	static private final String idocSet	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.iconset", "version2").trim();
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
	
	
	public AspThumbnailService() {
	}
	
	@Override	
	public File getThumbnailFile(String id, String domain, File srcfile, ThumbnailSize size) throws IOException {
		
		if (srcfile==null || srcfile.getName()==null) {
			logger.error("src is null in thumbnail server");
			return null;
		}	
		
		Url url = getUrl(id, domain, srcfile, size);

		File file = getCache().get(url);
		
		if (file==null) {
			ThumbnailGenerator generator = getGenerator(srcfile);
			if (generator!=null) {
				generator.generate(srcfile, (OutputStream)getCache().getWriter(url, id, domain), size);
				file = getCache().get(url);
			}
		}
		
		if (file==null) {
			file = getDefaultThumbnail(srcfile, size);
		}

		return file;
	}

//	/** 
//	 * Thumbnail de resources Externos con link a YouTube (obtiene el thumbnail de Youtube)
//	 */
	@Override	
	public File getThumbnailFile(String id, String domain, String url, ThumbnailSize size) throws IOException {
		
		if (url ==null) {
			return getDefaultThumbnail("html", size);
		}
		
		try {
			URL neturl = new URL(url);
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		Url fileurl = getUrl(String.valueOf(url.hashCode()), domain, id+".html", size);

		File file = getCache().get(fileurl);
		
		boolean validfile = true;
		if (file!=null) {
			if (file.length()==0) {
				validfile = false;
			}
		}
		
		if (file==null || !validfile) {
			ThumbnailGenerator generator = getGenerator(url);
			if (generator!=null) {
				generator.generate(url, (OutputStream)getCache().getWriter(fileurl, String.valueOf(url.hashCode()), domain), size);
				file = getCache().get(fileurl);
			}
		}
		
		if (file==null || file.length()==0) {
			file = getDefaultThumbnail("html", size);
		}
		
		return file;
	}

	
	public void removeAll() throws IOException {					
		try {
//			logger.debug("Closing Server");
//			close();
//			logger.debug("Removing dir: "+ getRoot());
			
			KbeeFileUtils.forceDelete(new File(getRoot()));
//			start();
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
	
	@Override
	public String getDefaultThumbnail(String key) {
		return null;
//		return defaultFormats.get(key);
	}
//
//	
//	/**
//	 * @param objectid
//	 * @param file
//	 * @param domainname
//	 * @return
//	 * @throws IOException
//	 */
//		
	@Override
	public void evict(String id, String domain) throws IOException {
//		
//		if (!isStarted)
//			start();
//
//		String url;
//		
//		ThumbnailSize arr_size [] = {ThumbnailSize.LARGE,
//									 ThumbnailSize.MEDIUM,
//									 ThumbnailSize.MINI,
//									 ThumbnailSize.SMALL,									 
//								     ThumbnailSize.W980,
//									 ThumbnailSize.AVATAR_STATUS};
//		String thumbnailName;
//		
//		for (ThumbnailSize size: arr_size) {
//			thumbnailName =  getThumbnailNameFromId(id, "jpg", size);
//			url = getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext(thumbnailName, id, domain, FileServerV1.FAST));
//			getFileServer().remove(url);
//			
//			thumbnailName =  getThumbnailNameFromId(id, "png", size);
//			url = getDirGenerator().generateRelativePath(new SubDirGenerationStrategyContext(thumbnailName, id, domain, FileServerV1.FAST));
//			getFileServer().remove(url);
//		}
	}
//
//	
	@Override
	public void close() {
//		
//		logger.debug("closing Thumbnail Server.");
//		
//		if (getFileServer()!=null)
//			getFileServer().close();		
//		
//		file_server=null;
//		dirgenerator = null;
//		imagegen = null;
//		pdfgen = null;
//		
//		this.isStarted = false;
 	}
	
	@Override
  	public boolean isEncrypted() 	{
  		return false;
  	}
	
	@Override
  	public boolean usesCache() 		{
		return false;
	}
	
	public void remove(String url) throws IOException {
		this.getFileServer().remove(url); 
	}
	
	@Override
	public long getSize() {
		if (getFileServer()!=null)
			return getFileServer().getSize();
		return 0;
	};
	
	@Override
	public long getCacheMiss() {
		return getCache().getCounterCacheMiss().getCount();	
	}
	
    @Override
	public long getCacheHits() {
		return getCache().getCounterCacheHits().getCount();	
	}
    
    @Override
	public void start() {
	}
	
	private ThumbnailGenerator getGenerator(File srcfile) {
		for (ThumbnailGenerator generator : getGenerators()) {
			if (generator.accept(srcfile)) {
				return generator;
			}
		}
		return null;
	}
	
	private ThumbnailGenerator getGenerator(String url) {
		// es youtube??
		if (url!=null && url.contains("youtube"))
			return new YoutubeThumbnailGenerator();
		return new WebSiteThumbnailGenerator();
	}
	
	private List<ThumbnailGenerator> getGenerators() {
		if (generators==null) {
			generators = new ArrayList<ThumbnailGenerator>();
			generators.add(new AspPdfThumbnailGenerator());
			generators.add(new AspWordThumbnailGenerator());
			generators.add(new ImageThumbnailGenerator2());
		}
		return generators;
	}
	
	private Url getUrl(String id, String domain, File srcfile, ThumbnailSize size) throws IOException {
		String srcextension = FilenameUtils.getExtension(srcfile.getName()).toLowerCase();
		String extension = !srcextension.equals("png") && !srcextension.equals("webp") ? "jpg" : srcextension;
		String thumbnailName = getThumbnailName(id, extension, size);
		String url = getFileServer().getUrl(thumbnailName, id, domain);
		return new Url(thumbnailName, url);
	}
	
	private Url getUrl(String id, String domain, String srcname, ThumbnailSize size) throws IOException {
		String srcextension = FilenameUtils.getExtension(srcname).toLowerCase();
		String extension = !srcextension.equals("png") && !srcextension.equals("webp") ? "jpg" : srcextension;
		String thumbnailName = getThumbnailName(id, extension, size);
		String url = getFileServer().getUrl(thumbnailName, id, domain);
		return new Url(thumbnailName, url);
	}
	
	private String getThumbnailName(String id, String extension, ThumbnailSize size) {
		return id+"-"+size.getLabel()+"."+extension;
	}
	
	private File getDefaultThumbnail(File srcfile, ThumbnailSize size) {
		try {
			String filetype = getType(srcfile);
			String thumbnailname = thumbnails.get(filetype);
			thumbnailname = thumbnailname.replace("/", File.separator);
			String path = getFileServer().getUrl(thumbnailname, "000000", "kbee");
			Url url = new Url(thumbnailname, path);
			File file = getCache().get(url);
			if (file ==null) {
				FSOutputStream out = getCache().getWriter(url, "000000", "kbee");
				BufferedInputStream in = new BufferedInputStream(getClass().getResourceAsStream(thumbnails.get(filetype)), BUFFER_SIZE);
				byte buffer[] = new byte[BUFFER_SIZE];	
				int i;
				i=in.read(buffer, 0, BUFFER_SIZE);
				while (i>0) {
					out.write(buffer, 0, i);
					i=in.read(buffer, 0, BUFFER_SIZE);
				}
				in.close();
				out.close();
				file = getCache().get(url);
			}
			return file;
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	private File getDefaultThumbnail(String filetype, ThumbnailSize size) {
		try {
			String thumbnailname = thumbnails.get(filetype);
			if (thumbnailname==null) thumbnailname = thumbnails.get("file");
			thumbnailname = thumbnailname.replace("/", File.separator);
			String path = getFileServer().getUrl(thumbnailname, "000000", "kbee");
			Url url = new Url(thumbnailname, path);
			File file = getCache().get(url);
			if (file ==null) {
				FSOutputStream out = getCache().getWriter(url, "000000", "kbee");
				BufferedInputStream in = new BufferedInputStream(getClass().getResourceAsStream(thumbnails.get(filetype)), BUFFER_SIZE);
				byte buffer[] = new byte[BUFFER_SIZE];	
				int i;
				i=in.read(buffer, 0, BUFFER_SIZE);
				while (i>0) {
					out.write(buffer, 0, i);
					i=in.read(buffer, 0, BUFFER_SIZE);
				}
				in.close();
				out.close();
				file = getCache().get(url);
			}
			return file;
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	private String getType(File file) {
		String type = "file";
		if (FSUtils.isExcel(file)) {
			type = "msexcel";
		}
		if (FSUtils.isCSV(file)) {
			type = "msexcel";
		}
		if (FSUtils.isWord(file)) { 
			type = "msword";
		}
		if (FSUtils.isPowerpoint(file)) {
			type = "mspowerpoint";
		}
		if (FSUtils.isZip(file.getName())) {
			type = "zip";
		}
		if (FSUtils.isAudio(file.getName())) {
			type = "audio";
		}
		if (FSUtils.isVideo(file)) {
			type = "video";
		}
		if (FSUtils.isText(file))  {
			type = "text";
		}
		if (FSUtils.isExecutable(file)) {
			type = "exe";
		}
		if (FSUtils.isMsg(file)) {
			type = "msg";
		}
		if (FSUtils.isXml(file.getName())) {															 
			type = "xml";
		}
		if (FSUtils.isHTML(file.getName())) {
			type = "html";
		}
		return type;
	}
	
	private Cache getCache() {
		return cache;
	}
	
 	private KbeeFileServer getFileServer() {
//		if (!isStarted)
//			start();
		if (fileServer==null)
			try {
				fileServer	= new KbeeFileServer("Thumbnail FileServer", getRoot(), new IdBasedDirectoryStrategy());
			} 
			catch (IOException e) {
				throw new KbeeRuntimeException(e);
			}
		return fileServer;
	}
	
	private String getRoot() {
		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + "thumbnail-server" + File.separator + "db";  
	}
}
