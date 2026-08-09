package com.novamens.wicket.util;

import java.io.File;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.request.resource.PackageResourceReference;

import kbee.util.FSUtils;
import kbee.util.NumberFormatter;

public class FileUtil {
	
	
	static private Map<String, String> icons = new HashMap<String, String>();
	
	static private Map<String, PackageResourceReference> resource_icons = new HashMap<String, PackageResourceReference>();
	static {
		
		resource_icons.put("pdf", new PackageResourceReference(NumberFormatter.class, "pdf.png"));
		resource_icons.put("word", new PackageResourceReference(NumberFormatter.class, "word.png"));
		resource_icons.put("image", new PackageResourceReference(NumberFormatter.class, "image.png"));
		resource_icons.put("excel", new PackageResourceReference(NumberFormatter.class, "excel.png"));
		resource_icons.put("powerpoint", new PackageResourceReference(NumberFormatter.class, "powerpoint.png"));
		resource_icons.put("link", new PackageResourceReference(NumberFormatter.class, "link.png"));
		resource_icons.put("zip", new PackageResourceReference(NumberFormatter.class, "zip.png"));
		resource_icons.put("video", new PackageResourceReference(NumberFormatter.class, "video.png"));
		resource_icons.put("audio", new PackageResourceReference(NumberFormatter.class, "audio.png"));
		resource_icons.put("exe", new PackageResourceReference(NumberFormatter.class, "exe.png"));		
		resource_icons.put("msg", new PackageResourceReference(NumberFormatter.class, "msg.png"));
		resource_icons.put("file", new PackageResourceReference(NumberFormatter.class, "file.png"));
		
		
	}

	
	static private Map<String, String> resource_glyphicons = new HashMap<String, String>();
	static {
		
		resource_glyphicons.put("pdf", "fal fa-file-pdf");
		resource_glyphicons.put("word", "fa-duotone  fa-file-word");
		resource_glyphicons.put("image", "fa-duotone fa-file-image");
		resource_glyphicons.put("excel", "fa-duotone fa-file-excel");
		resource_glyphicons.put("powerpoint", "fa-duotone fa-file-powerpoint");
		resource_glyphicons.put("link", "fa-duotone fa-file-text");
		resource_glyphicons.put("zip", "fa-duotone fa-file-zip");
		resource_glyphicons.put("video", "fa-duotone fa-file-video");
		resource_glyphicons.put("audio", "fa-duotone fa-file-audio");
		resource_glyphicons.put("exe", "fa-duotone fa-file-code");		
		resource_glyphicons.put("msg", "fa-duotone fa-file-text");
		resource_glyphicons.put("file", "fa-duotone fa-file");
		resource_glyphicons.put("text", "fa-duotone fa-file-text");
	}

	/** 
	 * @param key
	 * @return
	 */
	public static String getResourceGlyphIconByKey(String key) {
		return resource_glyphicons.get(key);
	}
	
	
	/**
	 * @param filename
	 * @return
	 */
	static public String getResourceGlyphIcon(String filename) {
		
		if (FSUtils.isPdf(filename))
			return resource_glyphicons.get("pdf");

		if (FSUtils.isImage(filename))
			return resource_glyphicons.get("image");

		if (FSUtils.isWord(filename))
			return resource_glyphicons.get("word");

		if (FSUtils.isExcel(filename))
			return resource_glyphicons.get("excel");
		
		if (FSUtils.isPowerpoint(filename))
			return resource_glyphicons.get("powerpoint");

		if (FSUtils.isVideo(filename))
			return resource_glyphicons.get("video");

		if (FSUtils.isAudio(filename))
			return resource_glyphicons.get("audio");
		
		if (FSUtils.isZip(filename))
			return resource_glyphicons.get("zip");

		if (FSUtils.isExe(filename))
			return resource_glyphicons.get("exe");

		if (FSUtils.isMsg(filename))
			return resource_glyphicons.get("msg");
		
		return resource_glyphicons.get("file");
	}
	
	
	
	public static PackageResourceReference getResourceIconByKey(String key) {
		return resource_icons.get(key);
	}

	/**
	 * @param filename
	 * @return
	 */
	static public PackageResourceReference getResourceIcon(String filename) {
		
		if (FSUtils.isPdf(filename))
			return resource_icons.get("pdf");

		if (FSUtils.isImage(filename))
			return resource_icons.get("image");

		if (FSUtils.isWord(filename))
			return resource_icons.get("word");

		if (FSUtils.isExcel(filename))
			return resource_icons.get("excel");
		
		if (FSUtils.isPowerpoint(filename))
			return resource_icons.get("powerpoint");

		if (FSUtils.isVideo(filename))
			return resource_icons.get("video");

		if (FSUtils.isAudio(filename))
			return resource_icons.get("audio");
		
		if (FSUtils.isZip(filename))
			return resource_icons.get("zip");

		if (FSUtils.isExe(filename))
			return resource_icons.get("exe");

		if (FSUtils.isMsg(filename))
			return resource_icons.get("msg");
		
		return resource_icons.get("file");
		
	}
	
	static final private String PATH = "/images/icons"; 

	static {
		// file types
		//
		icons.put("pdf",		PATH + "/pdf.png");
		icons.put("word",		PATH + "/word.png");
		icons.put("doc",		PATH + "/word.png");
		icons.put("docx",		PATH + "/word.png");
		icons.put("dot",		PATH + "/word.png");
		icons.put("excel",		PATH + "/excel.png");
		icons.put("xls",		PATH + "/excel.png");
		icons.put("xlsx",		PATH + "/excel.png");
		icons.put("lock",		PATH + "/lock.png");
		icons.put("jpg",		PATH + "/jpg.png");
		icons.put("png",		PATH + "/png.png");
		icons.put("tif",		PATH + "/tif.png");
		icons.put("ppt",		PATH + "/ppt.png");
		icons.put("pptx", 		PATH + "/ppt.png");    	
		icons.put("zip", 		PATH + "/zip.png");
		icons.put("rar", 		PATH + "/zip.png");
    	icons.put("gzip", 		PATH + "/zip.png");    	
    	icons.put("mp3", 	    PATH + "/mp3.png");
    	icons.put("m4a", 	    PATH + "/mp3.png");
    	icons.put("html", 	    PATH + "/html.png");
    	icons.put("htm", 	    PATH + "/html.png");
    	icons.put("flv", 	    PATH + "/flv.png");
    	icons.put("mp4", 	    PATH + "/video.png");
    	icons.put("mov", 	    PATH + "/video.png");
    	icons.put("wmv", 	    PATH + "/video.png");
    	icons.put("txt", 	    PATH + "/txt.png");
    	icons.put("mpp", 	    PATH + "/mpp.png");
    	icons.put("msg", 	    PATH + "/msg.png");    	
    	icons.put("rtf", 	    PATH + "/rtf.png");
    	icons.put("gif", 	    PATH + "/gif.png");    	    	
    	icons.put("exe", 	    PATH + "/exe.png");
    	icons.put("indd", 		PATH + "/indd.png");    	
    	icons.put("general",    PATH + "/file.png");
    	icons.put("youtube",    PATH + "/youtube.png");
    	icons.put("xml",  		PATH + "/xml.png");
    	icons.put("link", 		PATH + "/link.png");
    };
    
	static long KB = 1024;
	static long MB = 1000 * KB;
	static long GB = 1000 * MB;
 
	
	static private NumberFormat eng_integer_nf;
	static private NumberFormat eng_float_nf;

	static private NumberFormat es_integer_nf;
	static private NumberFormat es_float_nf;
	
	static {
		
		eng_integer_nf = NumberFormat.getInstance(Locale.ENGLISH);
		eng_integer_nf.setMinimumFractionDigits(0);
		eng_integer_nf.setMaximumFractionDigits(0);
		eng_integer_nf.setRoundingMode(RoundingMode.HALF_UP);
		
		eng_float_nf = NumberFormat.getInstance(Locale.ENGLISH);
		eng_float_nf.setMinimumFractionDigits(2);
		eng_float_nf.setMaximumFractionDigits(2);
		eng_float_nf.setRoundingMode(RoundingMode.HALF_UP);

		
		
		es_integer_nf = NumberFormat.getInstance(Locale.ENGLISH);
		es_integer_nf.setMinimumFractionDigits(0);
		es_integer_nf.setMaximumFractionDigits(0);
		es_integer_nf.setRoundingMode(RoundingMode.HALF_UP);
		
		es_float_nf = NumberFormat.getInstance(Locale.ENGLISH);
		es_float_nf.setMinimumFractionDigits(2);
		es_float_nf.setMaximumFractionDigits(2);
		es_float_nf.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	static public String formatFileSize(long size) {
		return formatFileSize(size, Locale.getDefault());
	}
	
	static public String formatFileSize(long size, String css) {
		return  formatFileSize(size, Locale.getDefault(), css); 
	}
	
	static public String formatFileSize(long size, Locale locale) {
				return  formatFileSize(size, locale, null); 
	}
	

	

	
	static public String formatNumber(long number) {
			return formatNumber( number, Locale.getDefault());
	}
	
	static public String formatNumber(double number) {
		return formatNumber( number, Locale.getDefault());
	}
	

	static public String formatNumber(int number) {
		return formatNumber( number, Locale.getDefault());
	}


	static public String formatNumber(float number) {
		return formatNumber( number, Locale.getDefault());
	}

	
	static public String formatNumber(long number,  Locale locale) {
		NumberFormat integer_nf;
		if (locale.equals(Locale.forLanguageTag("es")))
			integer_nf = es_integer_nf;
		else 
			integer_nf = eng_integer_nf;
		return  integer_nf.format(number);
	}
	
	static public String formatNumber(int number,  Locale locale) {
		NumberFormat integer_nf;
		if (locale.equals(Locale.forLanguageTag("es")))
			integer_nf = es_integer_nf;
		else 
			integer_nf = eng_integer_nf;
		return  integer_nf.format(number);

	}
	
	static public String formatNumber(float number,  Locale locale) {
		NumberFormat float_nf;
		if (locale.equals(Locale.forLanguageTag("es")))
			float_nf = es_integer_nf;
		else 
			float_nf = eng_integer_nf;
		return float_nf.format(number);
	}
	
	static public String formatNumber(double number,  Locale locale) {
		NumberFormat float_nf;
		
		if (locale.equals(Locale.forLanguageTag("es"))) {
			float_nf = es_float_nf;
		}
		else {
			float_nf = eng_float_nf;
		}
		return  float_nf.format(number);
	}
	
	
	/**
	 * 
	 * @param size
	 * @return
	 */
	static public String formatFileSize(long size, Locale locale, String css) {
	
		NumberFormat integer_nf;
		NumberFormat float_nf;
		
		if (locale.equals(Locale.forLanguageTag("es"))) {
			integer_nf = es_integer_nf;
			float_nf = es_float_nf;
		}
		else {
			integer_nf = eng_integer_nf;
			float_nf = eng_float_nf;
		}

		String css_open =  css!=null? "<span class= \""+css+"\">":"";
		String css_close =   css!=null? "</span>" :"";
		
		if (size==0) return integer_nf.format(size).trim();
		if (size<0) return "n/a";
		if (size<KB) return integer_nf.format(size)+" bytes";
		if (size<MB) return integer_nf.format((double) size / (double) KB)+ css_open + " KB" + css_close;
		else if (size<GB) {
			if (size<99*MB) return   float_nf.format((double) size / (double) MB) + css_open + " MB" + css_close;   // 		return String.format("%6.2f MB", (double) size / (double) MB).trim();
			else			return integer_nf.format((double) size / (double) MB)+ css_open + " MB" + css_close;    //         return String.format("%6.0f MB", (double) size / (double) MB).trim();
		}
		else 
			return float_nf.format((double) size / (double) GB)+ css_open + " GB" + css_close;	//  return String.format("%6.2f GB", (double) size / (double) GB).trim();	
	}
	
	
	static public String getIconFromName(String name) {
				String[] ext=name.split("\\.");
				if (ext.length>1) 
					return getIcon(ext[ext.length-1].toLowerCase());
				else
					return getIcon("general");
	}
	
	
	static public String getIconType(String name) { 
	
		if (isPdf(name))
			return "pdf";
		
		if (isImage(name))
			return "image";
		
		if (isWord(name))
			return "word";

		if (isExcel(name))
			return "excel";

		if (isPowerpoint(name))
			return "powerpoint";

		if (isZip(name))
			return "zip";

		if (isVideo(name))
			return "video";

		if (isXml(name))
			return "xml";

		return "file";
		
	}
	
	static public String getIcon(String type) {
		String icon = icons.get(type);
		return icon!=null?icon:icons.get("general");
	}
	
	static public boolean isWord(String filename) {
		return FSUtils.isWord(filename); 
	}

	static public boolean isExcel(String filename) {
		return FSUtils.isExcel(filename); 
 	}

	static public boolean isPowerpoint(String filename) {
		return FSUtils.isPowerpoint(filename); 
 	}
								
	static public boolean isZip(String filename) {
		return FSUtils.isZip(filename);
	}

	static public boolean isPdf(File file) {
		return FSUtils.isPdf(file);
	}

							
	static public boolean isXml(String filename) {
		return FSUtils.isXml(filename);
	}

	static public boolean isPdf(String filename) {
    		return FSUtils.isPdf(filename);
	}
    

	
	static public boolean isImage(File file) {
		if (file.exists() && !file.isDirectory())
			return FSUtils.isImage(file.getName());
		return false;
	}
	
	
    static public boolean isImage(String filename) {
    	return FSUtils.isImage(filename);
	}
    							
	static public boolean isVideo(File file) {
		if (file.exists() && !file.isDirectory())
			return FSUtils.isVideo(file.getName());
		return false;
	}

    
    static public boolean isVideo(String filename) {
    	return FSUtils.isVideo(filename);
 	}
    
    static public boolean isAudio(String filename) {
    	return FSUtils.isAudio(filename);
 	}
    
    static public boolean isOCRCandidate(String filename) {
    	if (filename.matches("^.*\\.(png|PNG|jpg|jpeg|webp|WEBP|JPEG|JPG|gif|GIF|pdf|PDF|tif|tiff|TIF|TIFF|HEIC|heic)$") ) 
			return true;
		else
			return false;
	}

	static public boolean isOCRCandidate(File file) {
		return isOCRCandidate(file.getName());
	}




}
