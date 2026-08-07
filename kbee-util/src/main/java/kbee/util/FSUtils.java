package kbee.util;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.novamens.util.KbeeFileUtils;


public class FSUtils {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FSUtils.class.getName());
	
	static private Map<String, String> resource_glyphicons = new HashMap<String, String>();
	
	static {
		resource_glyphicons.put("pdf", "fa-duotone fa-file-pdf");
		resource_glyphicons.put("word", "fa-duotone fa-file-word");
		resource_glyphicons.put("image", "fa-duotone fa-file-image");
		resource_glyphicons.put("excel", "fa-duotone fa-file-excel");
		resource_glyphicons.put("powerpoint", "fa-duotone fa-file-powerpoint");
		resource_glyphicons.put("link", "fa-duotone fa-chain");
		resource_glyphicons.put("zip", "fa-duotone fa-file-archive");
		resource_glyphicons.put("video", "fa-duotone fa-file-video");
		resource_glyphicons.put("audio", "fa-duotone fa-file-audio");
		resource_glyphicons.put("msg", "fa-duotone fa-file-text");
		resource_glyphicons.put("file", "fa-duotone fa-file");
		resource_glyphicons.put("text", "fa-duotone fa-file-text");
		resource_glyphicons.put("exe", "fa-duotone fa-file");
		resource_glyphicons.put("java", "fa-duotone fa-file-code");
		resource_glyphicons.put("html", "fa-duotone fa-code");
		
		resource_glyphicons.put("css",  "fa-duotone  fa-css3");
		
		resource_glyphicons.put("java",  "fa-duotone fa-java");
								
		resource_glyphicons.put(".psb",  "fa-duotone fa-adobe");
		
		
									
		resource_glyphicons.put("js",   "fa-duotone fa-js");
		resource_glyphicons.put("sql",  "fa-duotone  fa-file-code");
		resource_glyphicons.put("txt", "fa-duotone fa-file-alt");
		resource_glyphicons.put("directory", "fa-duotone fa-folder");
		resource_glyphicons.put("file-not-found", "fa-duotone  file-exclamation");
	}

	/** 
	 * @param key
	 * @return
	 */
	public static String getResourceGlyphIconByKey(String key) {
		return resource_glyphicons.get(key);
	}
	
	
	public static String getGlyphIcon(File file) {
		try {
			
			if (!file.exists())
				return FSUtils.getResourceGlyphIconByKey("file-not-found");
	
			if (file.isDirectory())
				return FSUtils.getResourceGlyphIconByKey("directory");
		
			return getGlyphIcon(file.getName());
		} catch (Exception e) {
			logger.error(e);
			return FSUtils.getResourceGlyphIconByKey("file");
		}
	}
	
	
	public static String getGlyphIcon(String filename) {
		
		try {
	
			if (FSUtils.isPdf(filename))				return FSUtils.getResourceGlyphIconByKey("pdf");
			if (FSUtils.isImage(filename))				return FSUtils.getResourceGlyphIconByKey("image");
			if (FSUtils.isWord(filename))				return FSUtils.getResourceGlyphIconByKey("word");
			if (FSUtils.isExcel(filename))				return FSUtils.getResourceGlyphIconByKey("excel");
			if (FSUtils.isPowerpoint(filename))			return FSUtils.getResourceGlyphIconByKey("powerpoint");
			if (FSUtils.isVideo(filename))				return FSUtils.getResourceGlyphIconByKey("video");
			if (FSUtils.isAudio(filename))				return FSUtils.getResourceGlyphIconByKey("audio");
			if (FSUtils.isZip(filename))				return FSUtils.getResourceGlyphIconByKey("zip");
			if (FSUtils.isExe(filename))				return FSUtils.getResourceGlyphIconByKey("exe");
			if (FSUtils.isMsg(filename))				return FSUtils.getResourceGlyphIconByKey("msg");
			
			return FSUtils.getResourceGlyphIconByKey("file");	
			
		} catch (Exception e) {
			logger.error(e);
			return FSUtils.getResourceGlyphIconByKey("file");
		}
	}

	
	
	
	
	
	
	
	
	static public String getBaseName(String filename) {
		return FilenameUtils.getBaseName(filename);
	}
	
	
	static public boolean isImage(File file) {
		if (file.exists() && !file.isDirectory())
			return isImage(file.getName());
		return false;
	}

							
	static public boolean isVideo(File file) {
		if (file.exists() && !file.isDirectory())
			return isVideo(file.getName());
		return false;
	}
 	
							
	static public boolean isAudio(File file) {
		if (file.exists() && !file.isDirectory())
			return isAudio(file.getName());
		return false;
	}
	
	/**
	 * 
	 * all but webp
	 * @param string
	 * @return
	 */
	static public boolean isGeneralImage(String string) {
		return string.toLowerCase().matches("^.*\\.(png|jpg|jpeg|gif|bmp|heic)$"); 
	}
	
	static public boolean isImage(String string) {
		return isGeneralImage(string) || string.toLowerCase().matches("^.*\\.(webp)$"); 
	}
	
 	
	static public boolean isText(File file) {
		if (file!=null)
			return isText(file.getName());
		return false;
	}
	
 	static public boolean isText(String name) {
		return name.toLowerCase().matches("^.*\\.(c|cpp|json|js|net|bat|sh|ini|text|psql|plsql|java|properties|txt|xml|html|css|sql|log|err|lst|asc|me|eml|odt|tab|tex|bib|utf8|sxg|wp5|wp6|wp7|faq)$"); 
	}
 	
 	
 	static public boolean isWord(File file) {
		if (file!=null)
			return isWord(file.getName());
		return false;
	}
 	
 	static public boolean isWord(String name) {
		return name.toLowerCase().matches("^.*\\.(doc|docx|rtf)$"); 
	}
 	
 	static public boolean isPdf(String filename) {
		return filename.toLowerCase().matches("^.*\\.(pdf)$"); 
	}

 	static public boolean isPdf(File file) {
		return file.getName().toLowerCase().matches("^.*\\.(pdf)$"); 
	}

 	static public boolean isExcel(File file) {
		if (file!=null)
			return isExcel(file.getName());
		return false;
	}
 	
 	static public boolean isExcel(String name) {
		return name.toLowerCase().matches("^.*\\.(xls|xlsx|xlsm)$"); 
	}

 	static public boolean isJScript(String name) {
		return name.toLowerCase().matches("^.*\\.(js)$"); 
	}
 	
	static public boolean isCSV(File file) {
		if (file==null)
			return false;
		return file.getName().toLowerCase().matches("^.*\\.(csv)$"); 
	}
 	
 	static public boolean isCSV(String name) {
		if (name==null)
			return false;
		return name.toLowerCase().matches("^.*\\.(csv)$"); 
	}
 	
 	
 	static public boolean isPowerpoint(File file) {
		if (file!=null)
			return isPowerpoint(file.getName());
		return false;
	}

 	static public boolean isPowerpoint(String name) {
 		return name.toLowerCase().matches("^.*\\.(ppt|pptx)$"); 
	}
 	
 	
 	
	static public boolean isZip(String filename) {
		return (filename.toLowerCase().matches("^.*\\.(zip|gz|gzip|rar|bz2|lz|lzma|lzo|rz|z|arc|arj|zz|tar|par)$") ); 
	}

    static public boolean isVideo(String filename) {
		return (filename.toLowerCase().matches("^.*\\.(mp4|flv|aac|ogg|wmv|3gp|avi|swf|svi|wtv|fla|mpeg|mpg|mov|m4v)$") ); 
	}

    static public boolean isAudio(String filename) {
		return filename.toLowerCase().matches("^.*\\.(mp3|wav|ogga|ogg|aac|m4a|m4a|aif|wma)$"); 
	}
    
    static public boolean isExecutable(File srcfile) {
		return srcfile.getName().matches("^.*\\.(exe|EXE)$"); 
	}
    
    static public boolean isMsg(File srcfile) {
		return srcfile.getName().matches("^.*\\.(msg|MSG)$"); 
	}
    
 	static public boolean isMSOffice(File file) {
		return (isWord(file) ||
			   isExcel(file) ||
			   isPowerpoint(file));
  	}

    static public boolean isOCRCandidate(String filename) {
		if (filename.matches("^.*\\.(png|PNG|jpg|JPG|gif|webp|WEBP|GIF|pdf|PDF|tif|tiff|TIF|TIFF)$") ) 
			return true;
		else
			return false;
	}
    
	static public boolean isOCRCandidate(File file) {
		return isOCRCandidate(file.getName());
	}

	
    
 	static public String getExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}
	
	static public String getEncrytedFileName(String filename) {
		return filename+".aes";
	}
	
	static public void clearDirectory(File dir) {
		try {
			KbeeFileUtils.forceMkdir(dir);
		} catch ( Exception ex) {
			   logger.error(ex);
		   	}
	}
	
	public static void createDirsIfNotExist(String path) throws IOException {
		String dirs[] = path.split("\\"+File.separator); 
		int n = dirs[dirs.length-1].length();
		File directory = new File(path.substring(0, path.length()-n));
		if (!directory.exists()) {
			try {
					KbeeFileUtils.forceMkdir(directory);
			}
			catch (java.io.IOException e) {
				logger.error(e);
				throw e;
			}
		}
	}
	
	static long KB = 1024;
	static long MB = 1000 * KB;
	static long GB = 1000 * MB;
 

	/**
	static public String formatFileSize(long size) {
		
		if (size==0) return String.format("%6d ", size);
		if (size<KB) return String.format("%6d bytes", size);
		if (size<MB) return String.format("%6.0f KB", (double) size / (double) KB);

		else if (size<GB) {
			if (size<99*MB)	return String.format("%6.2f MB", (double) size / (double) MB);
			else			return String.format("%6.0f MB", (double) size / (double) MB);
		}
		else return String.format("%6.2f GB", (double) size / (double) GB);	
	}
	**/
	


	public static boolean isExe(String filename) {
			return filename.toLowerCase().matches("^.*\\.(exe)$"); 
	}

	public static boolean isMsg(String filename) {
		return filename.toLowerCase().matches("^.*\\.(msg)$"); 
}


	public static boolean isXml(String filename) {
		return filename.toLowerCase().matches("^.*\\.(xml)$");
	}
	
	
	public static boolean isHTML(String filename) {
		return filename.toLowerCase().matches("^.*\\.(html)$");
	}
								
	public static boolean isJSON(String filename) {
		return filename.toLowerCase().matches("^.*\\.(json)$");
	}
	
}
