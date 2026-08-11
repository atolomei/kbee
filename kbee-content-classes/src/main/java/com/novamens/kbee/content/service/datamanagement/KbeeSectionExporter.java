package com.novamens.kbee.content.service.datamanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.base.Content;

public class KbeeSectionExporter {
		
	private BufferedWriter section_writer = null;
	private final String section_name;
	private final String export_dir;
																						
	static private org.apache.logging.log4j.Logger logger = LogManager.getLogger(KbeeSectionExporter.class.getName());

	/** ---------------------------------------------------------------------------------------------------
	 */

	public KbeeSectionExporter(String export_dir, String section) {
		this.section_name=section;
		this.export_dir=export_dir;
    }
	
	/** ---------------------------------------------------------------------------------------------------
	 */
	public void export(Content content, int index) {
    	try {
    		
    		if (getSectionWriter()==null) {
    			try {
	    			File file = new File( getExportDir() + File.separator + getSectionName() + File.separator + getSectionName() + ".html");
	    			section_writer = new BufferedWriter(new FileWriter(file));
	    			exportPageHeader();
	    		} catch (RuntimeException e) {
    				logger.error(e.getStackTrace());
    				return;
    			}
    		}
    		
    		String meta_file_name = "info-"+content.getId().toString() + ".html";
    		String dir = content.getId().toString() + "-" + content.getOId().toString() + "-" + content.getContentTemplate().getName().toLowerCase();
    															
    		getSectionWriter().write("<li class= \"list-group-item-heading\"> <span class=\"metadata\">" + String.valueOf(index+1) + ". </span>" + "<a class=\"btn-link\" href=\""+ dir + File.separator + meta_file_name+"\"> "+  content.getTitle() +"</a></li>");
    		
		} catch (IOException e) {
			logger.error(e.getStackTrace());
		}
    }
    
	/** ---------------------------------------------------------------------------------------------------
	 */
    public void exportPageHeader() {
		
		try {
				BufferedWriter out = getSectionWriter();
				
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
				out.write(	"<title> " + this.getSectionName() + "</title>\n");
										
				out.write(  "<link rel=\"stylesheet\" href=\"../css/idoc.css\">");
				
				out.write(  "<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Roboto:400,400i,500,700\">\n");
				out.write(  "</head>\n\n");
				out.write(  "<body class=\"kbee\">\n");
				
				out.write(  "<div class=\"header\">\n");
					out.write(  "<div class=\"logo\">\n");
						out.write(  "<img src =\"../img/logo.png\">\n");
					out.write(  "</div>\n");
				out.write(  "</div>\n");

				out.write("<div class=\"container-fluid\">\n");

				out.write(  "<div class=\"breadcrumb\">\n");
				out.write(  "<span><a href=\"../index.html\"> Home </a></span><span><span class=\"separator\"> / </span> Section " + getSectionName() +  "</span>\n" );
				out.write(  "</div>\n");

				out.write("<div class=\"title-panel\">");
				out.write("<h1> Section " + getSectionName() + "</h1>");
				out.write(  "</div>\n");
				
				out.write("<ul class=\"list-group col-lg-12\">");
				
		} catch (IOException e) {
			logger.error(e.getStackTrace());
		}
	}
    
	/** ---------------------------------------------------------------------------------------------------
	 */

    public void exportPageFooter() {
    	
		try {

			BufferedWriter out = getSectionWriter();
			
			getSectionWriter().write("</ul>"); // list group
			out.write(  "</div>\n");  // container-fluid
			out.write("</body>\n");
			out.write("</html>\n");
			
		} catch (IOException e) {
			logger.error(e.getStackTrace());
		}
    }

	/** ---------------------------------------------------------------------------------------------------
	 */

    public void close() {
    	
    	if (getSectionWriter()!=null)
    		
			try {
				exportPageFooter();
			}
    		finally {
    			if (getSectionWriter()!=null) {
    				try {
    					getSectionWriter().close();
    				}
    				catch (IOException e) {
    					logger.error(e);
    				}
    			}
    		}
    }
 
	public String getSectionName() {
		return this.section_name;
	}
							
	 
	public BufferedWriter getSectionWriter() {
		return this.section_writer;
	}

	 

	public String getExportDir() {
		return this.export_dir;
	}

	 

	public String getUrl() {
		return getExportDir() + File.separator + getSectionName() + File.separator +  getSectionName() + ".html";
	}
}
