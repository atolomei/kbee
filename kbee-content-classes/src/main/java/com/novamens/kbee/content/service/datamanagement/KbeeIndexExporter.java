package com.novamens.kbee.content.service.datamanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;

import com.novamens.content.service.datamanagement.DMExporter;
import com.novamens.datetime.DateTimeService;
import com.novamens.service.ServiceLocator;
	

/** 
 * 
 */
public class KbeeIndexExporter {
		
	BufferedWriter writer = null;
	private final String export_dir;
	
	private final DMExporter dm_exporter;
																						
	static private org.apache.logging.log4j.Logger logger = LogManager.getLogger(KbeeIndexExporter.class.getName());

	static public final SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss z");
	static public final SimpleDateFormat shortdf = new SimpleDateFormat("dd MMM yyyy");
	

	public KbeeIndexExporter(String export_dir, DMExporter exporter) {
		this.export_dir=export_dir;
		this.dm_exporter=exporter;
    }
	
	public void generate() {
	
		try {
			exportPageHeader();
			
			final File[] dirContents = new File(getExportDir()).listFiles();
			if (dirContents!=null) {
				for (File file : dirContents) {
					if (file.isDirectory()) {
						if (!(file.getName().startsWith("css") || file.getName().startsWith("log"))) {
							File [] htmls = file.listFiles();
							if (htmls!=null) {
								for (File ht : htmls) {
									if (!ht.isDirectory()) {
										String ext = FilenameUtils.getExtension(ht.getName());
										if (ext!=null) {
											if (ext.toLowerCase().equals("html")) {
												String url = file.getName() + File.separator + ht.getName();
												getWriter().write("<li class=\"list-group-item-heading\"> <a class=\"btn-link\" href=\""+  url +  "\"> Section " + FilenameUtils.getBaseName(ht.getName()) + "</a></li>\n");
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			logger.error(e);
			
		} finally {
			if (getWriter()!=null)
				close();
		}
    }
	
	
	/** ---------------------------------------------------------------------------------------------------
	 */
    public void exportPageHeader() {
		
		try {
				BufferedWriter out = getWriter();
				
				out.write(  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				out.write(  "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
				out.write(  "<head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\">\n");
				out.write(  "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
				out.write(	"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, user-scalable=yes\">\n");
				out.write(	"<link rel=\"icon\" type=\"image/x-icon\" href=\"/images/idoc-favicon.png\">\n");
				out.write(	"<meta name=\"language\" language=\"English\">\n");
				out.write(	"<meta name=\"robots\" content=\"NOINDEX, NOFOLLOW\">\n");
				out.write(	"<meta name=\"rating\" content=\"General\">\n");
				out.write(	"<meta name=\"keywords\" content=\"kbee\">\n");
				out.write(	"<title> Export </title>\n");
										
				out.write(  "<link rel=\"stylesheet\" href=\"./css/idoc.css\">");
				
				out.write(  "<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Roboto:400,400i,500,700\">\n");
				out.write(  "</head>\n\n");
				out.write(  "<body class=\"kbee\">\n");
				
				out.write(  "<div class=\"header\">\n");
				out.write(  "<div class=\"logo\">\n");
				out.write(  "<img src =\"img/logo.png\">\n");
				out.write(  "</div>\n");
				out.write(  "</div>\n");
				
				out.write("<div class=\"container-fluid\">\n");

				out.write(  "<div class=\"breadcrumb\">\n");
				out.write("<span>Home</span>\n" );
				out.write(  "</div>\n");

				out.write("<div class=\"title-panel\">");
				out.write("<h1>RPDD Export</h1>");
				out.write(  "</div>\n");

				out.write("<h2 class=\"first\">About</h2>");

				try {
					String edate = shortdf.format(getExporter().getStartTime());
					String euser = getExporter().getUserExport().getFirstLastName();
					String edomain = (getExporter().getDomain().getOrganization()!=null?getExporter().getDomain().getOrganization():getExporter().getDomain().getName());
					out.write("<div><p>This data was exported on " + edate +" by "+ euser + " from " + edomain + ".<br />Each section contains up to "+ String.valueOf( KbeeBaseFileSystemExporter.getSectionSize()) +" items.</p>" + "</div>");
				
				} catch (NullPointerException e) {
					logger.error(e.getStackTrace());
				}
				
				out.write("<h2>Contents</h2>");
				out.write("<ul class=\"list-group\">");
				
		} catch (IOException e) {
			logger.error(e);
			logger.error(e.getStackTrace());
		}
	}
    
    
    


	private void exportPageFooter() {
    	
		try {

			BufferedWriter out = getWriter();
			
			getWriter().write("</ul>"); // list group

			out.write("<h2>Stats</h2>");
			
			// out.write("<div class=\"form-group\">" + "<div class=\"label\">#Query</div><div> " + getExporter().getQueryStr() + "</div></div>");
		
			out.write("<div class=\"form-group\">" + "<div class=\"label\">Contents Exported</div><div> "  + String.valueOf(getExporter().getExported()+ "</div></div>"));
			out.write("<div class=\"form-group\">" + "<div class=\"label\">Attachments Exported</div><div> "  + String.valueOf(getExporter().getattachmentsExported()+ "</div></div>"));
			long start_time = getExporter().getStartTime();
			long end_time = System.currentTimeMillis();
																																						
			out.write("<div class=\"form-group\">" + "<div class=\"label\">Duration</div><div> "+  
					ServiceLocator.getService(DateTimeService.class).formatLapseSeconds(end_time-start_time, Locale.getDefault()) + "</div></div>");
			
			out.write("<div class=\"panel loginfo\">\n");
				out.write("<div class=\"col-lg-12\">---</div>\n");
				out.write("<div class=\"col-lg-12\">\n");
					out.write("<span>"+ dateformat.format(new Date()) + "</span>\n");
				out.write("</div>\n");
			out.write("</div>\n");
			
			
			out.write(  "</div>\n");  // container-fluid
			out.write("</body>\n");
			out.write("</html>\n");
			
			
		} catch (IOException e) {
			logger.error(e.getClass().getName(), e);
		}
    }
    

    public void close() {
    	
    	if (getWriter()!=null) {
			try {
		
				exportPageFooter();
			}
    		finally {
    			if (getWriter()!=null) {
    				try {
    					getWriter().close();
    				}
    				catch (IOException e) {
    					logger.error(e.getClass().getName(), e);
    				}
    			}
    		}
    	}
    }
							
	public BufferedWriter getWriter() {
		if (this.writer==null)
			try {
				this.writer = new BufferedWriter(new FileWriter(getExportDir()+ File.separator + "index.html"));
			} catch (IOException e) {
				logger.error(e.getClass().getName(), e);
			}
		return this.writer;
	}
	
	public String getExportDir() {
		return this.export_dir;
	}
	
	private DMExporter getExporter() {
		return this.dm_exporter;
	}

}
