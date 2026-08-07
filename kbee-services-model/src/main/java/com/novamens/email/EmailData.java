package com.novamens.email;




import java.io.File;
import java.io.Serializable;


public class EmailData implements Serializable {
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailData.class.getName());

	private static final long serialVersionUID = 2949289465267171132L;
	
	
	
	public String from;     // email
	public String to;       // email
	public String subject; 
	
	public String msg;
	
	public Serializable audit_resource_id = null; // id of KBFile to add to the Event
	
	
	public String resources[] = null; // absolute path of files to send
	public String local_file = null;  // absolute path of files to send
	
	public String context_info; 		//
	
	public String user_id;	    		// User that Sends the email
	public String user_receiver_id;	    // User that Receives the email (if applicable)
	public String object_id;    		// Content Id
	
	

	public String getUserReceiverId() 			{return user_receiver_id;}
	public void setUserReceiverId(String uid) 	{this.user_receiver_id=uid;}
	
	public String getUserId() 				{return user_id;}
	public String getObjectId() 			{return object_id;}

	public void setUserId(String uid) 		{this.user_id=uid;}
	public void setObjectId(String oid) 	{this.object_id=oid;}
	
	public String getFrom() 				{return from;}
	public String getTo() 					{return to;}
	public String getSubject() 				{return subject;}
	public String getMsg() 					{return msg;}
	public String[] getResources() 			{return resources;}
	
	public String getLocalFileToSend() 			{return this.local_file;}
	
	
	public String getContextInfo() 			{return context_info;}
	public void   setContextInfo(String s)	{context_info=s;}
	
	public EmailData(String from, String to, String subject, String msg, String context_info) {
		this(from, to, subject, msg, null,  context_info);
	}

	public EmailData(String from, String to, String subject, String msg, String resources[]) {
		this(from, to, subject, msg, resources, null);
	}
	public EmailData(String from, String to, String subject, String msg, String resources[], String context_info) {
			this(from, to, subject, msg, resources, context_info, null, null);
	}
	
	public EmailData(String from, String to, String subject, String msg, String resources[], String context_info, String local_file)  {
		this(from, to, subject, msg, resources, context_info,  local_file, null);
	}
	
	
	public EmailData(String from, String to, String subject, String msg, String resources[], String context_info, String local_file, Serializable kb_file_audit) {
		this.from = from;
		this.to = to;
		this.msg =  msg;
		this.subject = subject;
		this.context_info=context_info;
		this.resources=resources;
		this.local_file=local_file;
		this.audit_resource_id=kb_file_audit;
	}

	@Override
	public int hashCode() {
		try {
			String st= (from!=null?from:"")+
						(to!=null?to:"")+
						(subject!=null?subject:"")+
						(user_id!=null?user_id:"")+
						(msg!=null?msg:"")+
						(object_id!=null?object_id:"")+
						(user_receiver_id!=null?user_receiver_id:"");
					
		return st.hashCode();
		
		} catch (Exception e) {
			return super.hashCode();
		}
	}
	
 	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[from: " + from + " | ");
		str.append(" to: " + to + " | ");
		str.append(" subject: " + subject + " | ");
		str.append(" msg ------------------: \n" + msg + " \n ------------------ | ");

		if (this.local_file!=null) {
			str.append(" local file attachment: " + this.local_file + " | ");
		}

		if (resources!=null) {
			str.append(" resources: ");
			int n=0;
			for (String resource: resources) {
				if(n++>0)
					str.append(", ");
				str.append(getResourceName(resource));
			}
		}
		
		
		if (context_info!=null)
			str.append(" |  context_info: " + this.context_info);

		str.append("] \n");
		
		return str.toString();
	}
	
	private String getResourceName(String url) {
		File file;
		try {
			file = getFile(url);
			if (file!=null)
				return file.getName();
		} catch (Exception e) {
			logger.error(e);
		}
		return "n/a";
	}

	
	public void setAuditKBFileId(Serializable id) {
		 this.audit_resource_id=id;
	}
	
	
	public Serializable getAuditKBFileId() {
		return this.audit_resource_id;
	}
	
	private File getFile(String path) {
		if (path==null || path.equalsIgnoreCase("null"))
			return null;
		try {
			return new File(path);

		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
}
