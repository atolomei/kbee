package com.novamens.kbee.content.service.datamanagement;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.base.CustomAttribute;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.TextContainer;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.ObjectState;
import com.novamens.event.LogEvent;
import com.novamens.security.User;
import com.novamens.service.BrandingService;
import com.novamens.service.ContentExportService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;
	

/** 
 *  Export content (section is managed by Exporter)
 *  
 *  Export ./css/idoc.css
 *  Export ./index.html
 *  
 *  Export section home:
 *  
 *  ./1/section-1.html
 *  Export element 1..1000
 *  
 *  ./2/section-2.html
 *  ./2/33333-4445
 *  ./2/33333-4445/info-33333-4445.html
 *  ./2/33333-4445/audit-33333-4445.html

 *  ./3/section-3.html
 *  
 */
public class KbeeHTMLExporter extends KbeeBaseFileSystemExporter {
															
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeHTMLExporter.class.getName());
											
	// Move to BrandingService
	//

	static private final String LOGO_KBEE 	= "kbee.png";
	
	static private final int BUFFER_SIZE = 4096;
	
	static public final DateTimeFormatter dateformat = DateTimeFormatter.RFC_1123_DATE_TIME;
	
	static final int TEXT	 		= 1; // ok
	static final int RESOURCES 		= 2; // ok
	static final int ATTRIBUTES 	= 3; // ok
	static final int NOTES 			= 4; // ok
	static final int PRIVATE_NOTES 	= 5; // Secured
	static final int CUSTOM 		= 6; // ok
	static final int AUDIT   		= 7; // Secured
	
	
	private String export_mode = ContentExportService.ALL;

			
	private KbeeSectionExporter section_exporter;


	public KbeeHTMLExporter(Serializable uid) {
		super(uid);
	}

	

	public String getExportMode() {
		return this.export_mode;
	}
	
	
	public void setExportMode(String mode) {
		this.export_mode = mode;
	}

	
	@Override
	public void close() {
		try {
			super.close();
		} finally  {
			if( getSectionExporter()!=null)
				getSectionExporter().close();
		}
	}
	

	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	@Override
	protected void exportResourceList(Content content, int index, String home_dir) {

		
		String meta_file_name;
		
		if (isStandAlone())
			meta_file_name = "index.html";
		else
			meta_file_name = "info-"+content.getId().toString() + ".html";

		BufferedWriter out = null;
		 
		try {

			File logFile = new File(home_dir + File.separator + meta_file_name);
			
			out = new BufferedWriter(new FileWriter(logFile));

			exportPageHeader(out, content, index);
			exportTitlePanel(out, content);
			exportNavPanel(out, content, RESOURCES);

			out.write("<div class=\"tab-internal\">\n");

			exportResourcesPanel(out, content, true);
			
			out.write("\n");
			out.write("\n");
			out.write("<div class=\"panel loginfo\">\n");
				out.write("<div class=\"col-lg-12\">---</div>\n");
				out.write("<div class=\"col-lg-12\">\n");
				out.write("<span>"+ dateformat.format(OffsetDateTime.now()) + "</span>\n");
				out.write("</div>\n");
			out.write("</div>\n");
			
			out.write("</div>\n"); // tab-internal
			out.write("\n");
			out.write("\n");
			
		} catch (IOException e) {
			logger.error(e);
		} finally {
			if (out!=null)
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}
		}
	}

	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void exportAuditTrail(Content content, int index, String home_dir) {

		 BufferedWriter out = null;
		 
		try {
									
			String audit_file_name = "audit-"+ content.getId().toString() + ".html";
			
			
			File logFile = new File(home_dir + File.separator + audit_file_name);
			
			out = new BufferedWriter(new FileWriter(logFile));
			
			exportPageHeader(out, content, index);
			exportTitlePanel(out, content);
			exportNavPanel(out, content, AUDIT);
			
			out.write("<div class=\"tab-internal\">\n");
			List<LogEvent> list = getAuditTrail(content);
			Iterator<LogEvent> iterator = list.listIterator();
			
			out.write("<div class=\"row header\"\">");
				out.write("<span class=\"title-col col-lg-2\">Date</span>");
				out.write("<span class=\"title-col col-lg-2\">User</span>");			
				out.write("<span class=\"title-col col-lg-2\">Action</span>");
				out.write("<span class=\"title-col col-lg-1\">Event Type</span>");
				out.write("<span class=\"title-col col-lg-2\">Target</span>");
				out.write("<span class=\"title-col col-lg-3\">Description</span>");
			out.write("</div>");

	
			out.write("<ul class=\"table-body\"\">");
			
			
			// if the Session User has Write permission on this file
			// 
			//if (ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(content)) {
				
				while (iterator.hasNext()) {
						LogEvent event = iterator.next();
						out.write("<li class=\"row\">");
							out.write("<span class=\"col-lg-2\">" + dateformat.format(event.getTime()) + "</span>");
							if (event.getEventUser()!=null)
								out.write("<span class=\"col-lg-2\">" +event.getEventUser().getFirstLastName() + "</span>");			
							else
								out.write("<span>" +"N/A" + "</span>");
							
							if (event.getAction()!=null)
								out.write("<span class=\"col-lg-2\">"+ event.getAction()+"</span>");
							else
								out.write("class=\"col-lg-2\""+NA+"</span>");
							
							out.write("<span class=\"col-lg-1\">"+event.getEventType()+"</span>");
							out.write("<span class=\"col-lg-2\">"+event.getTarget()+"</span>");
							
							if (event.getDescription()!=null)
								out.write("<span class=\"col-lg-3\">"+event.getDescription()+"</span>");
							else
								out.write("<span class=\"col-lg-3\">"+NA+"</span>");
						
						out.write("</li>"); // row
							
						out.write("\n");
				}
			
			out.write("</ul>"); // table-body
			
			out.write("\n");
			out.write("\n");
			
			out.write("<div class=\"panel loginfo\">\n");
			out.write("<div class=\"col-lg-12\">---</div>\n");
			out.write("<div class=\"col-lg-12\">\n");
			out.write("<span>"+ dateformat.format(OffsetDateTime.now()) + "</span>\n");
			out.write("</div>\n");
			out.write("</div>\n");
			out.write("</div>\n"); // tab-internal
			
		} catch (IOException e) {
			logger.error(e);
		} finally {
			if (out!=null)
				try {
					exportPageFooter(out);
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}
		}
	}

		
	 /**
	  * 
	  */
	@Override
	protected void exportCustomTags(Content content, int index, String home_dir) {
			
		BufferedWriter out = null;
		try {
										
			String audit_file_name = "custom-"+ content.getId().toString() + ".html";
			File logFile = new File(home_dir + File.separator + audit_file_name);
			out = new BufferedWriter(new FileWriter(logFile));
				
			exportPageHeader(out, content, index);
			exportTitlePanel(out, content);
			exportNavPanel(out, content,  CUSTOM);
				
			out.write("<div class=\"tab-internal\">\n");
				
			// User Defined Values
			//
			List<CustomAttribute> attributes = content.getUserDefinedAttributes();
				
			for (CustomAttribute attribute : attributes) {
				out.write("<div class=\"form-group\">\n");
				out.write("<div class=\"label\">" + attribute.getName() + "</div>\n");	
				out.write("<div class=\"lvalue\">" + attribute.getValue() + "</div>\n");
				out.write("</div>\n");
			}

			out.write("\n");
			out.write("\n");
			out.write("<div class=\"panel loginfo\">\n");
			out.write("<div class=\"col-lg-12\">---</div>\n");
			out.write("<div class=\"col-lg-12\">\n");
			out.write("<span>"+ dateformat.format(OffsetDateTime.now()) + "</span>\n");
			out.write("</div>\n");
			out.write("</div>\n");
				
			out.write("</div>\n"); // tab-internal
			out.write("\n");
			out.write("\n");
				
		} 
		catch (IOException e) {
			logger.error(e);
		} 
		finally {
			if (out!=null)
			try {
				exportPageFooter(out);
				out.close();
			}
			catch (IOException e) {
				logger.error(e);
			}
		}
	}

	
	
	
	/***
	 * 
	 * 
	 * 
	 */
	@Override
	protected  void exportNotes(Content content, int index, String home_dir){
		
		 BufferedWriter out = null;
		 
		try {
									
			String audit_file_name = "notes-"+ content.getId().toString() + ".html";
			File logFile = new File(home_dir + File.separator + audit_file_name);
			out = new BufferedWriter(new FileWriter(logFile));
			
			exportPageHeader(out, content, index);
			exportTitlePanel(out, content);
			exportNavPanel(out, content,  NOTES);
			
			out.write("<div class=\"tab-internal\">\n");

			if (content.getAbstract()!=null) {
				out.write("<div class=\"form-group\">\n");
					if (content.getAbstract()!=null) 
						out.write( "<div>"+content.getAbstract().asString() + "</div>\n\n");
				out.write("</div>\n");
			}

			out.write("\n");
			out.write("\n");
			out.write("<div class=\"panel loginfo\">\n");
				out.write("<div class=\"col-lg-12\">---</div>\n");
				out.write("<div class=\"col-lg-12\">\n");
				out.write("<span>"+ dateformat.format(OffsetDateTime.now()) + "</span>\n");
				out.write("</div>\n");
			out.write("</div>\n");
			
			out.write("</div>\n"); // tab-internal
			out.write("\n");
			out.write("\n");
			
		} catch (IOException e) {


		} finally {
			if (out!=null)
				try {
					exportPageFooter(out);
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}
		}
	}

	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	protected  void exportPrivateNotes(Content content, int index, String home_dir){
							
		 BufferedWriter out = null;
		 
		try {
									
			String audit_file_name = "private-notes-"+ content.getId().toString() + ".html";
			File logFile = new File(home_dir + File.separator + audit_file_name);
			out = new BufferedWriter(new FileWriter(logFile));
			
			exportPageHeader(out, content, index);
			exportTitlePanel(out, content);
			exportNavPanel(out, content,  PRIVATE_NOTES);
			
			out.write("<div class=\"tab-internal\">\n");

			if (content.getPrivateNotes()!=null) {
				out.write("<div class=\"form-group\">\n");
					if (content.getPrivateNotes()!=null) 
						out.write( "<div style=\"max-width:980px;margin-top: -24px;  margin-bottom: 32px;\">"+content.getPrivateNotes().asString() + "</div>\n\n");
				out.write("</div>\n");
			}

			out.write("\n");
			out.write("\n");
			
			exportResourcesPanel(out, content, false);
			
			out.write("<div class=\"panel loginfo\">\n");
				out.write("<div class=\"col-lg-12\">---</div>\n");
				out.write("<div class=\"col-lg-12\">\n");
				out.write("<span>"+ dateformat.format(OffsetDateTime.now()) + "</span>\n");
				out.write("</div>\n");
			out.write("</div>\n");
			
			out.write("</div>\n"); // tab-internal
			out.write("\n");
			out.write("\n");
			
			
			
		} catch (IOException e) {
			logger.error(e);
			
		} finally {
			if (out!=null)
				try {
					exportPageFooter(out);
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}
		}
	}

	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	protected void exportAttributes(Content content, int index, String home_dir) {

		 BufferedWriter out = null;
		 
		try {
												
			String audit_file_name = "attributes-"+ content.getId().toString() + ".html";
			File logFile = new File(home_dir + File.separator + audit_file_name);
			out = new BufferedWriter(new FileWriter(logFile));
			
			exportPageHeader(out, content, index);
			exportTitlePanel(out, content);
			exportNavPanel(out, content, ATTRIBUTES);
			
			out.write("<div class=\"tab-internal\">\n");
						
			/*if (content instanceof OrganizationalText) {
				out.write("<div class=\"form-group\">\n");
				out.write("<div class=\"label\">Text</div>\n");
					if (((OrganizationalText) content).getText()!=null)
						out.write("<div class=\"value\">" + ((OrganizationalText) content).getText().asString()+"</div>\n\n");
					else
						out.write("<div class=\"value\">-</div>\n\n");
				out.write("</div>\n");
			}*/
			
			List<Classification> list = content.getClassification();
			
			for (Classification clasi: list) {
				out.write("<div class=\"form-group\">\n");
					if (clasi.getClassifier()!=null)
						out.write("<div class=\"label\">" + clasi.getClassifier().getName()+ "</div>\n");	
					else
						out.write("<div class=\"label\">Classifier name N/A</div>\n\n");
		
					if (clasi.getDataSetMember()!=null)
						out.write("<div class=\"value\">" + clasi.getDataSetMember().getStrValue()+ "</div>\n\n");
					else
						out.write("<div class=\"value\">-</div>\n\n");
				out.write("</div>\n");
			}

			
			List<AttributeTemplate> atlist = content.getContentTemplate().getAttributes();
			for (AttributeTemplate atemplate: atlist) {
				List<String> values = content.getAttributeValues(atemplate.getAttribute());
				if (values!=null && !values.isEmpty()) {
					StringBuilder str = new StringBuilder();
					for (String s: values) {
						if (str.length()>0)
							str.append(" | ");
						str.append(s);
					}
					out.write("<div class=\"form-group\">\n");
					out.write("<div class=\"label\">" + atemplate.getAttribute().getName()+ "</div>\n");
					out.write("<div class=\"value\">" + str.toString()+ "</div>\n\n");
					out.write("</div>\n");
				}
			}

			// User Defined Values
			//
			List<CustomAttribute> attributes = content.getUserDefinedAttributes();
			
			for (CustomAttribute attribute : attributes) {
				out.write("<div class=\"form-group\">\n");
						out.write("<div class=\"label\">" + attribute.getName() + "</div>\n");	
						out.write("<div class=\"lvalue\">" + attribute.getValue() + "</div>\n");
				out.write("</div>\n");
			}
			
				out.write("<div class=\"form-group\">\n");
				out.write("<div class=\"label\"> Content Class</div>\n");
				out.write("<div>" + content.getContentTemplate().getName()+ "</div>\n");
				out.write("</div>\n");
				
				out.write("<div class=\"form-group\">\n");
				out.write("<div class=\"label\">OId - Version - Id</div>\n");
				out.write("<div class=\"value\">" + content.getOId().toString() +  " - " + String.valueOf(content.getVersion())  +  " - "  + content.getId().toString()+ "</div>\n");
				out.write("</div>\n");
				
				if (content.isExternal()) {
					out.write("<div class=\"form-group\">\n");
					out.write("<div class=\"label\">External Id</div>\n");
					out.write("<div class=\"value\">" + content.getExternalId() + "</div>\n");
					out.write("</div>\n");
					
					out.write("<div class=\"form-group\">\n");
					out.write("<div class=\"label\">External Last Modified Date</div>\n");
					out.write("<div class=\"value\">" + (content.getExternalTime()!=null?dateformat.format(content.getExternalTime()):"-") + "</div>\n");
					out.write("</div>\n");
				}
				
			out.write("\n");
			out.write("\n");
			out.write("<div class=\"panel loginfo\">\n");
				out.write("<div class=\"col-lg-12\">---</div>\n");
				out.write("<div class=\"col-lg-12\">\n");
				out.write("<span>"+ dateformat.format(OffsetDateTime.now()) + "</span>\n");
				out.write("</div>\n");
			out.write("</div>\n");
			
			out.write("</div>\n"); // tab-internal
			out.write("\n");
			out.write("\n");
			
		} catch (IOException e) {
			logger.error(e);
			
		} finally {
			if (out!=null)
				try {
					exportPageFooter(out);
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}
		}
	}

	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	protected void exportText(Content content, int index, String home_dir) {

		 BufferedWriter out = null;
		 
		try {
								
			String audit_file_name = "text-"+ content.getId().toString() + ".html";
			File logFile = new File(home_dir + File.separator + audit_file_name);
			out = new BufferedWriter(new FileWriter(logFile));
			
			exportPageHeader(out, content, index);
			exportTitlePanel(out, content);
			exportNavPanel(out, content, TEXT);
			
			out.write("<div class=\"tab-internal text\">\n");
			
			if (content instanceof OrganizationalText) {
				out.write("<div class=\"form-group\">\n");
					if (((OrganizationalText) content).getText()!=null) {
						out.write("<div class=\"value text-editor\"> <div class=\"text-viewer\">" + ((OrganizationalText) content).getText().asString()+"</div></div>\n\n");
					}
				out.write("</div>\n");
			}
			
			out.write("\n");
			out.write("\n");
			out.write("<div class=\"panel loginfo\">\n");
				out.write("<div class=\"col-lg-12\">---</div>\n");
				out.write("<div class=\"col-lg-12\">\n");
				out.write("<span>"+ dateformat.format(OffsetDateTime.now()) + "</span>\n");
				out.write("</div>\n");
			out.write("</div>\n");
			
			out.write("</div>\n"); // tab-internal
			out.write("\n");
			out.write("\n");
			
		} catch (IOException e) {
			logger.error(e);
			
		} finally {
			if (out!=null)
				try {
					exportPageFooter(out);
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}
		}
	}

	
	/**
	 * Despues de exportar cada elemento, se agrega una fila en el section exporter.
	 * Si es una section nueva o no existe, se crea el section exporter.
	 */
	@Override
	protected void onAfterExportElement(Content content, int index) {
		if (this.isStandAlone())
			return;
		String section = getSection(content, index);
		if (section!=null) {
			if (getSectionExporter()==null) 
				setSectionExporter(new KbeeSectionExporter(getExportDir(), section));
			if (!section.equals(getSectionExporter().getSectionName())) {
				getSectionExporter().close();
				setSectionExporter(new KbeeSectionExporter(getExportDir(), section));
			}
			getSectionExporter().export(content, index);
		}
	}

	
	/** 
	 * 
	 * copia el idoc.css
	 * genera el index.html
	 * 
	 */
	@Override
	protected void onAfterExport(String home_dir)	{

		
		if (!isStandAlone()) {
			KbeeIndexExporter index  = new KbeeIndexExporter(getExportDir(), this); 
			try {
				index.generate();
			} catch (Exception e) {
				logger.error(e);
			}
			finally {
				index.close();
			}
		}
		
		// -----------------
		// Css  
		//
		String css_dir  = getExportDir() + File.separator + "css";
		logger.debug("Export css: " + css_dir);
		
		try {
			
			URL defaulturl = this.getClass().getResource("idoc.css");
			
			if (defaulturl!=null) {

				BufferedInputStream in = null;
				BufferedOutputStream out = null;
				
				KbeeFileUtils.forceMkdir(new File(css_dir));		
				
				byte buffer[] = new byte[BUFFER_SIZE];
				
				in = new BufferedInputStream(getClass().getResourceAsStream("idoc.css"), BUFFER_SIZE);
				out = new BufferedOutputStream( new FileOutputStream(new File(css_dir + File.separator + "idoc.css")), BUFFER_SIZE);
				int bread;
				bread=in.read(buffer, 0, BUFFER_SIZE);
				while (bread>0) {
					out.write(buffer, 0, bread);
					bread=in.read(buffer, 0, BUFFER_SIZE);
				}
				in.close();
				out.close();
			}
			
		} catch (IOException e) {
			logger.error(e);
		}


		// -----------------
		// Logo 
		//
		try {
			
			String logo_dir  = getExportDir() + File.separator + "img";
			
			
			URL url = null;
			String logo_str;
			// String brand=ServiceLocator.getService(BrandingService.class).getProductKey();

			logo_str =  LOGO_KBEE; 

			if (logo_str!=null)
				url = this.getClass().getResource(logo_str);
			
			
			if (url!=null) {

				BufferedInputStream in = null;
				BufferedOutputStream out = null;
				KbeeFileUtils.forceMkdir(new File(logo_dir));		
				byte buffer[] = new byte[BUFFER_SIZE];
				in = new BufferedInputStream(getClass().getResourceAsStream(logo_str), BUFFER_SIZE);
				out = new BufferedOutputStream( new FileOutputStream(new File(logo_dir + File.separator + "logo.png")), BUFFER_SIZE);
				int bread;
				bread=in.read(buffer, 0, BUFFER_SIZE);
				while (bread>0) {
					out.write(buffer, 0, bread);
					bread=in.read(buffer, 0, BUFFER_SIZE);
				}
				in.close();
				out.close();

			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	

	private void exportTitlePanel(BufferedWriter out, Content content) {
		try {
			
		out.write("<div class=\"title-panel-container\">");
			out.write("<div class=\"main-title-panel\">");
				out.write("<div class=\"title-container\"><h1>"+content.getTitle()+ "</h1></div>");
				out.write("<div class=\"metadata\">");
				out.write("</div>");
			out.write("</div>");
			
			// ---------------------------------------------------------------
			// Section, Version FOR Contents [Library] ver TASKS
			//
			out.write("<div style=\"padding:0; float:right;display:block;\">");
						out.write("<div style=\"float:right; margin-top:0px;\">");
								out.write("<div style=\"padding-right:16px; padding-left:0px; float:left;\">");
										out.write("<div style=\"text-transform: uppercase; font-weight: bold; line-height: 1.8em; font-size:14px;\">Section");
										out.write("</div>");

										String section;
										
										if (content.getWorkspace()!=null && content.getWorkspace()>0 && content.getState()==ObjectState.ENABLED)
											section="Workspace";
										
										else if (content.getState()==ObjectState.ENABLED && content.getContentTemplate().isKnowledgeBaseCabinet())
											section="Knowledge Base";
										
										else if (content.getState()==ObjectState.ENABLED && content.getContentTemplate().isTemplatesCabinet())
											section="Templates";
										
										else if (content.getExternalId()!=null && content.getState()==ObjectState.ENABLED)
											section="External";
										
										else if (content.getState()==ObjectState.ARCHIVED)
											section="Archive";
										
										else if (content.getState()==ObjectState.DELETED)
											section="Recycle Bin";
										
										else if (content.getState()==ObjectState.ENABLED)
											section="Library";
										else
											section="N/A";
										out.write("<div style=\"text-align: center;\" >" + section + "</div>");
								out.write("</div>");
								out.write("<div style=\"padding-right:0px; padding-left:16px; float:left;\">");
										out.write("<div style=\"text-transform: uppercase;font-weight: bold; line-height: 1.8em; font-size:14px;\">Version"); 
										out.write("</div>");
										out.write("<div style=\"text-align: center;\">");
											out.write("<span>"+ String.valueOf(content.getVersion())+ "</span>");
											out.write("<span style=\"margin-left: 6px;    font-size: 11px;    font-weight: 500;    color: #6CC79F;    text-transform: uppercase;\">");
											if (content.isHeadVersion())
												out.write("Head</span>");
											else
												out.write("</span>");
										out.write("</div>");	
								out.write("</div>");
						out.write("</div>");
			out.write("</div>");
		out.write("</div>");
			
				
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	
	
	/**
	 * 
	 * 
	 * @param out
	 * @param content
	 * @param tab
	 */
	private void exportNavPanel(BufferedWriter out,  Content content, int tab) {
		try {
			out.write("<nav class=\"nav\">\n");

				if (isStandAlone())
					out.write("<div class=\"nav-element\"><a class=\"btn-link" + (tab==RESOURCES   	?  " selected" : ""  ) + "\" href=\"index.html\">Resources</a></div>\n");
				else
					out.write("<div class=\"nav-element\"><a class=\"btn-link" + (tab==RESOURCES   	?  " selected" : ""  ) + "\" href=\"info-" + content.getId().toString()  + ".html\">Resources</a></div>\n");
				
				out.write("<div class=\"nav-element\"><a class=\"btn-link" + (tab==ATTRIBUTES  	?  " selected" : ""  ) + "\" href=\"attributes-" + content.getId().toString()  + ".html\">Attributes</a></div>\n");

				if (content instanceof TextContainer) 
					out.write("<div class=\"nav-element\"><a class=\"btn-link" + (tab==TEXT  ?  " selected" : ""  ) + "\" href=\"text-" + content.getId().toString()  + ".html\">"+(content.getContentTemplate().getText_label()!=null?content.getContentTemplate().getText_label():"Private Area")+"</a></div>\n");	
				
				if (content.getContentTemplate().isAbstract())
					out.write("<div class=\"nav-element\"><a class=\"btn-link" + (tab==NOTES  		?  " selected" : ""  ) + "\" href=\"notes-" + content.getId().toString()  + ".html\">"+(content.getContentTemplate().getAbstract_label()!=null?content.getContentTemplate().getAbstract_label():"Notes") +"</a></div>\n");
						
				if (content.getContentTemplate().isPrivateNotes()) {
					if (hasPermissionsPrivateNotes(content))
						out.write("<div class=\"nav-element\"><a class=\"btn-link" + (tab==PRIVATE_NOTES  		?  " selected" : ""  ) + "\" href=\"private-notes-" + content.getId().toString()  + ".html\"> "+(content.getContentTemplate().getPrivate_notes_label()!=null?content.getContentTemplate().getPrivate_notes_label():"Private Area")+"</a></div>\n");
				}
						
				if (content.getContentTemplate().isCustomAttributes()) {
					if (hasPermissionsCustomAttributes(content))
						out.write("<div class=\"nav-element\"><a class=\"btn-link" + (tab==CUSTOM  		?  " selected" : ""  ) + "\" href=\"custom-" + content.getId().toString()  + ".html\">Custom Tags</a></div>\n");
				}
				
				if (hasPermissionsAuditTrail(content))
					out.write("<div class=\"nav-element\"><a class=\"btn-link" + (tab==AUDIT 		?  " selected" : "" ) + "\" href=\"audit-" + content.getId().toString() + ".html\">Audit</a></div>\n");
			
			out.write("</nav>\n");
				
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	private void exportPageFooter(BufferedWriter out) {
		try {
			out.write(  "</div>\n");  // container-fluid
			out.write("</body>\n");
			out.write("</html>\n");
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	
	private void exportPageHeader(BufferedWriter out, Content content, int index) {
		
		try {
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
				out.write(	"<title> " + content.getDisplayName() + "</title>\n");
				
				if (isStandAlone())
					out.write(  "<link rel=\"stylesheet\" href=\"css/idoc.css\">");
				else
					out.write(  "<link rel=\"stylesheet\" href=\"../../css/idoc.css\">");
				
				out.write(  "<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Roboto:400,400i,500,700\">\n");

				// FA 4.7
				out.write(  "<script src=\"https://use.fontawesome.com/aa9c4b97d1.js\"></script>\n");
				
				out.write(  "</head>\n\n");
				out.write(  "<body class=\"kbee\">\n");
				
				out.write(  "<div class=\"header\">\n");
					out.write(  "<div class=\"logo\">\n");
				
				if (isStandAlone())
					out.write(  "<img src =\"img/logo.png\">\n");
				else
					out.write(  "<img src =\"../../img/logo.png\">\n");
					
				out.write(  "</div>\n");
				out.write(  "</div>\n");
				out.write(  "<div class=\"container-fluid\">\n");


				String section = getSection(content, index);
				if (section!=null) {
					
					if (getSectionExporter()==null) 
						setSectionExporter(new KbeeSectionExporter(getExportDir(), section));
					
					if (!section.equals(getSectionExporter().getSectionName())) {
						getSectionExporter().close();
						setSectionExporter(new KbeeSectionExporter(getExportDir(), section));
					} 
				}
				
				if (!isStandAlone()) {
					out.write("<div class=\"breadcrumb\">\n");
						out.write(    "<span><a href=\"../../index.html\">Home</a></span>"
									+ "<span class=\"separator\"> / </span>"
								    + "<span><a href=\"../" + getSectionExporter().getSectionName()+ ".html\"> Section " + getSectionExporter().getSectionName() +  "</a></span>"
									+ "<span class=\"separator\"> / </span>"
									+ "<span>" + content.getTitle() + "</span>\n" );
					out.write(  "</div>\n");
				}
				
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	
	private KbeeSectionExporter getSectionExporter() {
		return section_exporter;
	}

	
	
	
	private void setSectionExporter(KbeeSectionExporter section_exporter) {
		this.section_exporter=section_exporter;
	}

	
												
	private void exportResourcesPanel(BufferedWriter out, Content content, boolean is_public) {
					
		if (content instanceof ResourceContainer) {

			List<Resource> resources = getExportMode().equals( ContentExportService.SEARCHER ) ?
					((ResourceContainer) content).getPortalEnabledResources() :
					((ResourceContainer) content).getResources();

			boolean is_ul = false;
			
			try  {
						for (Resource resource: resources) {
							
									if (resource.getState()==ObjectState.ENABLED && ( (!is_public && !resource.isPublicArea()) || (is_public && resource.isPublicArea()))) {

										if (!is_ul) {
											out.write("<ul class=\"media\">\n");
											is_ul=true;
										}
										
										out.write("<li class=\"media-element\">\n");
										
												String link;
												String title;
					
												if (resource.getTitle()!=null)
													title=resource.getTitle();
												else if ((resource.getName()!=null))
													title=resource.getName();
												else
													title=resource.getId().toString();
												
												if (resource instanceof KBFile) {
													
													try {
														link = "files" + File.separator + urlEscape(((KBFile) resource).getFileName());
													} catch (Exception e) {
														logger.error(e);
														link = "#";
														title = title + " <em>[not found]</em> ";
													}
												}	
												else
													link= "#";
												
												try {
												
													out.write("<div class=\"col-lg-12\">");
													   // icon
													   //
														out.write("<div class=\"icon-container\">");
															String gy = resource.getFontAwesomeFreeIcon();
															out.write("<i class=\""+ gy +"\"><a class=\"btn-link\" target=\"_blank\" href=\" "+ link +"\"></a></i>\n");
														out.write("</div>\n");  

														// title
														//
														out.write("<div class=\"title\"><a class=\"btn-link\" target=\"_blank\" href=\" "+ link +"\">" + title + "</a></div>\n");
														
													out.write("</div>\n"); 
													
												} catch (Exception e) {
													logger.error(e);
													out.write("<div class=\"col-lg-12\"><a class=\"btn-link\" href=\" "+ link +"\">err</a></div>\n");
												}
												
												out.write("<div class=\"meta-data col-lg-12\">\n");
												
												if (resource.getMetadataAsString(dateformat)!=null)
															out.write("<span>" + resource.getMetadataAsString(dateformat) + ". </span>\n");
												else
															out.write("<span>n/a.</span>\n");
							
														try {
															if (resource.getDescription()!=null)
																out.write("<span>Description: </span><span>" + resource.getDescription() + ". </span>\n");
														} catch (Exception e) {
															logger.error(e);
														}
														
														try {
															if (resource instanceof ExternalResource) {
																if (((ExternalResource) resource).getUrl()!=null)
																	out.write("<span>url: </span> <span>" + ((ExternalResource) resource).getUrl() + "</span>\n");
																else
																	out.write("<span> url:</span> n/a</span>\n");
															}
															else if (resource instanceof KBFile) {
																	KBFile rf = (KBFile) resource;
																	if (rf.getSize()>0)
																		out.write("<span>" + formatFileSize(rf.getSize()) + ". </span>\n");
																	else
																		out.write("<span>Size:</span> <span>n/a.</span>\n");
																	
																	if (rf.getSubTitle()!=null)
																		out.write("<span>Subtitle:</span> <span>" + rf.getSubTitle() + ".</span>\n");
																	
																	if (rf.getGroup()!=null)
																		out.write("<span>Group:</span> <span>" + rf.getGroup() + ".</span>\n");
															}
														} catch (Exception e) {
															logger.error(e);
														}
														
												out.write("</div>\n"); // metadata
					
										out.write("</li>");
										out.write("\n");
										
								}

						}  // for
						
						if (is_ul)
							out.write("</ul>");
			
			
			} catch (Exception e) {
					logger.error(e);
			}
		}
	}
	
	private String urlEscape(String fileName) {
		if (fileName==null)
			return "#";
		return fileName.replace("#", "%23")
				.replace("!", "%21")
				.replace("\"", "%22")
				.replace("$", "%24")
				.replace("'", "%26")
				.replace("'", "%27")
				.replace("/", "%2F")
				.replace("@", "%40")
				.replace(",", "%2C");
	}


	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}

