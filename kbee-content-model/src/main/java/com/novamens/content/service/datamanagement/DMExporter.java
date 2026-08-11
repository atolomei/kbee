package com.novamens.content.service.datamanagement;


import java.io.IOException;

import com.novamens.content.base.Content;
import com.novamens.content.model.RelationTemplate;
import com.novamens.dom.Domain;
import com.novamens.security.User;


public interface DMExporter {
	
	
	// public void export(Content content, RelationTemplate relation, String source_target, int index);
	
	public void export(Content content);
	public void export(Content content, int index);
	
	public String getExportDir();
	public String getExportLogDir();
	
	public void start() throws IOException;
	
	public void setExportDir(String home_dir);
	public void setQueryStr(String str);
	public String getQueryStr();
	public void setLogDir(String dir);
	public void close();
	public int getErrors();
	public int getExported();
	public int getattachmentsExported();
	public long getStartTime();
	
	public User getUserExport();
	public Domain getDomain();
	
	public boolean isStandAlone();
	public void setStandAlone(boolean b);
	
}
