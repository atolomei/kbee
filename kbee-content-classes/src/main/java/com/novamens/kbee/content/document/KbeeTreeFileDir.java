package com.novamens.kbee.content.document;

 
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Resource;
import com.novamens.content.document.TreeFileDir;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.security.User;

 
/**
 * TreeFile that represents a Directory.
 */
@Entity
@DiscriminatorValue(value=TreeFileDir.DISCRIMNATOR_CODE)
public class KbeeTreeFileDir extends KbeeTreeFile implements TreeFileDir {
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger kblogger = kbee.util.logging.Logger.getLogger(KbeeTreeFileDir.class.getName());
	
	
	// redundant col
 	@Column(name = "isdirectory")
 	private boolean isdirectory = true;

	@Column(name = "dir_name")
 	private String dir_name;

	/** this field is used by the Searcher Library. It should be replaced by 
	 *  the KbeePortal tools 
	 * */

	@Override
	public String getType() {
		return TreeFileDir.DISCRIMNATOR_CODE;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o.getClass().equals(KbeeTreeFileDir.class))
			return ((Long)((KbeeTreeFileDir) o).getId()).equals((Long) getId());
		return false;	
	}
	
	@Override
	public String getTitle() {
	
		if (super.getTitle()!=null)
			return super.getTitle();
		
		if (dir_name!=null)
			return dir_name;
		
		if (getId()!=null)
			return this.getClass().getSimpleName()+" "+String.valueOf(getId());

		return this.getClass().getSimpleName();
	}
	
	
	@Override
    public void setDirectoryName(String directory_name) {
    	this.dir_name=directory_name;
	}

    @Override
    public String getDirectoryName() {
		return dir_name;
	}
    					
    public static TreeFileDir createRoot() {
        return new KbeeTreeFileDir();
        
    }
    
    
    @Override
    public String getName() {
    	return getDirectoryName();
    }

	@Override
	public boolean isDirectory() {
		return true;
	}
	
	public String toHTMLString() {
		StringBuilder str = new StringBuilder();
		str.append("<div class=\"dirname_container\">\n");
		String css= "tree_file_directory " +  ((getDirectoryName()!=null) ? "" : " rootdir" );
		str.append("<span class=\""+css+"\">" +  ((getDirectoryName()!=null) ? (getDirectoryName()): "/" ) +"</span>\n");
		str.append("</div>\n"); 
		return str.toString();
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}
	
	public Resource getPreviousVersion() {
		return null;
	}
	
	public int getVersion() {
		return 0;
	}

	@Override
	public OffsetDateTime getUploadOffsetDateTime() {
		return super.getLastModifiedOffsetDateTime();
	}

	@Override
	public User getUploadUser() {
		return super.getLastModifiedUser();	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}


	

}
