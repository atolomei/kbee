package com.novamens.kbee.content.service.datamanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.Classification;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.ObjectState;
import com.novamens.event.LogEvent;
import com.novamens.util.KbeeFileUtils;

/** ------------------------------------------------------------------------------------
 *  Data
 *  Titulo, Abstract, Classifiers, Tags, lista de resources 
 *  zip con los resources
 *  audit trail: exportar el audit trail
 * 
 */
public class KbeeFileSystemExporter extends KbeeBaseFileSystemExporter {
			
	static private org.apache.logging.log4j.Logger logger = LogManager.getLogger(KbeeFileSystemExporter.class.getName());

	static private final SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss z");
	
	
	
	/** 
	 * @param uid
	 */
	public KbeeFileSystemExporter(Serializable uid) {
		super(uid);
	}


	/** 
	 */
	@Override
	public void export(Content content, int index) {

		if (getGlobalLog()==null)
			throw( new RuntimeException ("Exporter is not started"));
		
		try {

			long start = System.currentTimeMillis();
			
			String content_dir = getExportDir() + File.separator + getContentHomeDir(content, index);
			
			KbeeFileUtils.forceMkdir(new File(content_dir));
			
			exportResourceList(content, index, content_dir);
			exportAttributes(content, index, content_dir);
			exportAuditTrail(content, index, content_dir);
			exportResources(content, content_dir);
			
			long end = System.currentTimeMillis();
			
			long duration = end - start;
			
			logger.info(content.getDisplayName() + " Duration: " + String.valueOf(duration) + " ms");
			
			incExported();
			
			getGlobalLog().write(String.format("%6d", getattachmentsExported()));
			getGlobalLog().write(FIELD_SEPARATOR);
			
			if (content.getTitle()!=null)
				getGlobalLog().write(content.getTitle().replace(FIELD_SEPARATOR, "_"));
			else
				getGlobalLog().write("n/a");
			getGlobalLog().write(FIELD_SEPARATOR);
			
			if (content.getId()!=null)
				getGlobalLog().write(content.getId().toString());
			else
				getGlobalLog().write("n/a");
			getGlobalLog().write(FIELD_SEPARATOR);
			
			if (content.getOId()!=null)  
				getGlobalLog().write(content.getOId().toString());
			else
				getGlobalLog().write("n/a");
			getGlobalLog().write(FIELD_SEPARATOR);
			
			if (content.getContentTemplate()!=null) 
				getGlobalLog().write(content.getContentTemplate().getName().replace(FIELD_SEPARATOR, "_"));
			else  
				getGlobalLog().write("n/a");
			getGlobalLog().write(FIELD_SEPARATOR);
			
			getGlobalLog().write(String.valueOf(duration)+ " ms");
			
			getGlobalLog().write("\n");
			
			
		} catch (IOException e) {
			logger.error(e.getStackTrace());
		}
	}
	
	/** ----------------------------------------------------------------------------
	 * @param content
	 */
	@Override
	protected void exportAttributes(Content content, int index, String home_dir) {

		String meta_file_name = content.getId().toString() + "-attributes.txt";

		BufferedWriter out = null;
		 
		try {

					File logFile = new File(home_dir + File.separator + meta_file_name);
						
					out = new BufferedWriter(new FileWriter(logFile));
					
					
					out.write( "\n\nFields and Classifiers\n");
					out.write( "----------------------\n\n");
			
					out.write("Abstract\n");
					if (content.getAbstract()!=null) {
						out.write( content.getAbstract().asString() + "\n\n");
					}
					else
						out.write( "-\n\n");
			
					if (content instanceof OrganizationalText) {
						out.write("Text\n");
						if (((OrganizationalText) content).getText()!=null)
							out.write(((OrganizationalText) content).getText().asString()+"\n\n");
						else
							out.write("-\n\n");
					}
					
					List<Classification> list = content.getClassification();
					
					for (Classification clasi: list) {
						if (clasi.getClassifier()!=null)
							out.write(clasi.getClassifier().getName()+ "\n");	
						else
							out.write("Classifier name N/A\n\n");
			
						if (clasi.getDataSetMember()!=null)
							out.write(clasi.getDataSetMember().getStrValue()+ "\n\n");
						else
							out.write("-\n\n");
					}
			
					out.write("\n");
					out.write("\n");
					out.write("---\n");
					out.write("Generated: "+ dateformat.format(new Date()) + "\n");
					out.write("\n");
					out.write("\n");
		
		} catch (IOException e) {
				logger.error(e.getStackTrace());
			
		} finally {
			if (out!=null)
				try {
					out.close();
				} catch (IOException e) {
						logger.error(e.getStackTrace());
				}
		}
	}
	
	/** ----------------------------------------------------------------------------
	 * @param content
	 */
	protected void exportResourceList(Content content, int index, String home_dir) {

		String meta_file_name = content.getId().toString() + "-info.txt";

		BufferedWriter out = null;
		 
		try {

			File logFile = new File(home_dir + File.separator + meta_file_name);
			
			out = new BufferedWriter(new FileWriter(logFile));
			
			out.write("Title\n");
			out.write(content.getDisplayName() + "\n\n");

			out.write("Content Class\n");
			if (content.getContentTemplate()!=null)
				out.write(content.getContentTemplate().getName() + "\n\n");
			else
				out.write( "-\n\n");
			
			out.write("Id\n");
			
			if (content.getId()!=null)
				out.write( content.getId().toString() + "\n\n");
			else
				out.write( "-\n\n");

			out.write("OId\n");
			if (content.getOId()!=null)
				out.write( content.getOId().toString() + "\n\n");
			else
				out.write( "-\n\n");
			
			out.write( "\n\nFields and Classifiers\n");
			out.write( "----------------------\n\n");

			out.write("Abstract\n");
			if (content.getAbstract()!=null) {
				out.write( content.getAbstract().asString() + "\n\n");
			}
			else
				out.write( "-\n\n");

			if (content instanceof OrganizationalText) {
				out.write("Text\n");
				if (((OrganizationalText) content).getText()!=null)
					out.write(((OrganizationalText) content).getText().asString()+"\n\n");
				else
					out.write("-\n\n");
			}
			
			List<Classification> list = content.getClassification();
			
			for (Classification clasi: list) {
				if (clasi.getClassifier()!=null)
					out.write(clasi.getClassifier().getName()+ "\n");	
				else
					out.write("Classifier name N/A\n\n");
	
				if (clasi.getDataSetMember()!=null)
					out.write(clasi.getDataSetMember().getStrValue()+ "\n\n");
				else
					out.write("-\n\n");
			}

			if (content instanceof ResourceContainer) {
									

				out.write( "\n\nResources\n");
				out.write( "---------\n\n");

				List<Resource> resources = ((ResourceContainer) content).getResources();

				for (Resource resource: resources) {

					if (resource.getState()==ObjectState.ENABLED) {
						
							if (resource.getName()!=null)
								out.write("name: " + resource.getName() + "\n");
							else
								out.write("name: n/a\n");

							if (resource.getMetadataAsString()!=null)
								out.write("Metadata: " + resource.getMetadataAsString() + "\n");
							else
								out.write("Metadata: n/a\n");

							if (resource.getTitle()!=null)
								out.write("title: " + resource.getTitle() + "\n");
							else
								out.write("title: -\n");

							if (resource.getDescription()!=null)
								out.write("Description: " + resource.getDescription() + "\n");
			
							if (resource instanceof ExternalResource) {
								if (((ExternalResource) resource).getUrl()!=null)
									out.write("url: " + ((ExternalResource) resource).getUrl() + "\n");
								else
									out.write("url: n/a\n");
							}
							else if (resource instanceof KBFile)  {

									KBFile rf = (KBFile) resource;

									if (rf.getSize()>0)
										out.write("Size: " + formatFileSize(rf.getSize()) + "\n");
									else
										out.write("Size: n/a\n");
									
									if (rf.getCRC32()!=null)
										out.write("CRC32: " + rf.getCRC32() + "\n");
									
									if (rf.getSHA256()!=null)
										out.write("SHA 256: " + rf.getSHA256() + "\n");
									
									if (rf.getSubTitle()!=null)
										out.write("Subtitle: " + rf.getSubTitle() + "\n");
									
									if (rf.getGroup()!=null)
										out.write("Group: " + rf.getGroup() + "\n");
							}
					}
					else {
						if (resource.getTitle()!=null)
							out.write(resource.getTitle() + " [ State:" + resource.getState().getLabel() + "]\n");
						else
							out.write(" [ State:" + resource.getState().getLabel() +" n/a ]\n");
					}

					out.write("\n");
				}
			}
			
			out.write("\n");
			out.write("\n");
			out.write("---\n");
			out.write("Generated: "+ dateformat.format(new Date()) + "\n");
			out.write("\n");
			out.write("\n");
			
		} catch (IOException e) {
				logger.error(e.getStackTrace());
			
		} finally {
			if (out!=null)
				try {
					out.close();
				} catch (IOException e) {
						logger.error(e.getStackTrace());
				}
		}
	}
	

	/** ----------------------------------------------------------------------------
	 * @param content
	 * 
	 */
	protected void exportAuditTrail(Content content, int index, String home_dir) {
		
		 BufferedWriter out = null;
		 
		try {

			String audit_file_name = content.getId().toString() + "-audit.txt";

			File logFile = new File(home_dir + File.separator + audit_file_name);
								
			out = new BufferedWriter(new FileWriter(logFile));
			
			List<LogEvent> list = getAuditTrail(content);
			
			Iterator<LogEvent> iterator = list.listIterator();

			out.write("Title: " + content.getDisplayName() + "\n");
			out.write("id: " + content.getId().toString() + "\noid: " + content.getOId().toString() + "\n\n");

			int counter = 0;

			out.write("     #");
			out.write(FIELD_SEPARATOR);
			
			out.write("Date ");
			out.write(FIELD_SEPARATOR);

			out.write("User ");			
			out.write(FIELD_SEPARATOR);
			
			out.write("Action ");
			out.write(FIELD_SEPARATOR);

			out.write("Event Type ");
			out.write(FIELD_SEPARATOR);
			
			out.write("Target ");
			out.write(FIELD_SEPARATOR);
					
			out.write("Description ");
			out.write(FIELD_SEPARATOR);
			
			out.write("\n");

			int total = list.size();
			
			while (iterator.hasNext()) {

					LogEvent event = iterator.next();

					out.write(String.format("%4d", (total-counter))+". ");
					out.write(FIELD_SEPARATOR);
					
					out.write(dateformat.format(event.getTime()));
					out.write(FIELD_SEPARATOR);

					if (event.getEventUser()!=null)
						out.write(event.getEventUser().getFirstLastName().replace(FIELD_SEPARATOR, "_"));			
					else
						out.write("N/A");
					out.write(FIELD_SEPARATOR);

					if (event.getAction()!=null)
						out.write(event.getAction().replace(FIELD_SEPARATOR, "_"));
					else
						out.write(NA);
					out.write(FIELD_SEPARATOR);

					out.write(event.getEventType());
					out.write(FIELD_SEPARATOR);
					
					out.write(event.getTarget());
					out.write(FIELD_SEPARATOR);
					
					if (event.getDescription()!=null)
						out.write(event.getDescription().replace(FIELD_SEPARATOR, "_"));
					else
						out.write(NA);
					
					out.write("\n");
					counter++;
			}

			out.write("\n");
			out.write("---\n");
			out.write("Generated: "+ dateformat.format(new Date()) + "\n");
			out.write("\n");
			out.write("\n");
			
		} catch (IOException e) {
				logger.error(e.getStackTrace());
			
		} finally {
			if (out!=null)
				try {
					out.close();
				} catch (IOException e) {
						logger.error(e.getStackTrace());
				}
		}
	}

	/** -------------------------------------------------------------------------
	 */
	protected void exportResources(Content content, String home_dir) {
		
		if (content instanceof ResourceContainer) {
			List<Resource> list = ((ResourceContainer) content).getResources();
			for (Resource resource: list) {
				
				if (resource instanceof KBFile) {
						try {
							File file = ((KBFile) resource).getFile();
							if (file!=null) {
								File dest_file = new File( home_dir + File.separator + file.getName());
								logger.info("Copying: " + dest_file.getAbsolutePath());
								FileUtils.copyFile(file, dest_file);
								incAttachmentsExported();
							}
							
						} catch (IOException e) {
							logger.error(e.getStackTrace());
						}
				}
				logger.info(resource.getTitle() + "  "  + resource.getDescription());
				
			}
		}
		
	}


	@Override
	protected void exportCustomTags(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub
		
		throw new RuntimeException("  pending");
		
		
	}


	@Override
	protected void exportNotes(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub
		
		throw new RuntimeException( "pending");
		
	}


	@Override
	protected void exportPrivateNotes(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub
		
		throw new RuntimeException( "pending");
	}
	
	@Override
	protected void exportText(Content content, int index, String content_dir) {

		throw new RuntimeException( "pending");
		
	}
	
}
