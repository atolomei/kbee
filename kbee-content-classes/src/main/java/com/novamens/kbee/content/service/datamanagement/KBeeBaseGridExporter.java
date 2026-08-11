package com.novamens.kbee.content.service.datamanagement;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import com.novamens.content.base.Content;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.service.datamanagement.DMExporter;
import com.novamens.dom.Domain;
import com.novamens.security.User;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;


/**
 * 
 * Crea el archivo de exportacion
 * 
 * 
 *
 */				
public class KBeeBaseGridExporter implements DMExporter {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KBeeBaseGridExporter.class.getName());
	
	static public final String FIELD_SEPARATOR  = " ; ";	
	static public final String NA 				= " N/A ";
	
	static long KB = 1024;
	static long MB = 1000 * KB;
	static long GB = 1000 * MB;

	private String working_dir;
	private String export_dir;
	private String export_log_dir;
	
	private Serializable uid;
	
	private String query_str;
	private int exported = 0;
	private int attachments_exported = 0;
	private int errors = 0;

	private BufferedWriter global_log = null;
	
	private long start_time;
	private long end_time;
	
	private User user;

	private boolean isInitialized  =false;


	/**
	 * Inicia el archivo de Exportacion
	 * Por cada SearchResult. agrega 1 fila al archivo de Exportacion
	 * 
	 * @param uid
	 * 
	 */
	
	public KBeeBaseGridExporter(Serializable uid) {
	
		this.uid=uid;
		this.working_dir = getWorkDir()  + File.separator + "xp" + uid.toString() + "-" + String.valueOf(System.currentTimeMillis());

		// Export dir es absoluto
		//
		setExportDir(this.working_dir + File.separator + "export");

		// Log dir es absoluto
		//
		//setLogDir(this.working_dir + File.separator + "export" + File.separator + "log");

		
	}
	
	@Override
	public void export(Content content) {
		// TODO Auto-generated method stub
	}

	@Override
	public void export(Content content, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getExportDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExportLogDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() throws IOException {
		//
		//
		//
	}

	@Override
	public void setExportDir(String home_dir) {
	}

	@Override
	public void setQueryStr(String str) {
	}

	@Override
	public String getQueryStr() {

		return null;
	}

	@Override
	public void setLogDir(String dir) {


	}

	@Override
	public void close() {


	}

	@Override
	public int getErrors() {
		return 0;
	}

	@Override
	public int getExported() {
		return 0;
	}

	@Override
	public int getattachmentsExported() {
		return 0;
	}

	@Override
	public long getStartTime() {
		return 0;
	}

	@Override
	public User getUserExport() {
		return null;
	}

	@Override
	public Domain getDomain() {
		return null;
	}

	@Override
	public boolean isStandAlone() {
		return false;
	}

	@Override
	public void setStandAlone(boolean b) {
	}
	
	protected String getDataExportDir() {
		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + "dataexport";
	}
	
	protected String getWorkDir() {
		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath();
	}

	


}
