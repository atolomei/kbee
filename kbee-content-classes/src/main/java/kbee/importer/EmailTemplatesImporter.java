package kbee.importer;



import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.email.EmailTemplate;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.email.KbeeEmailTemplate;

import kbee.api.model.IEmailTemplate;
import kbee.api.model.IResultSet;
import kbee.api.service.ApiService;

public class EmailTemplatesImporter extends Importer {
	
	private long total = 0;
	private int updated = 0;

	public EmailTemplatesImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain); 
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		int i=0;
		try {
			IResultSet<IEmailTemplate> templates = getRemoteTemplates();
			while (templates.hasNext()) {
				IEmailTemplate remote =  templates.next();
				EmailTemplate local = getLocal(remote);
				if (local!=null && (forceUpdate() || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()))) {
					syncTemplate(remote, (KbeeEmailTemplate)local);
					update(local);
					updated++;
					logger.info("Library "+local.getDisplayName());
				}
				else {
					logger.info("Library "+ remote.getDisplayName() + " not modified");
				}
				setProgress(++i);
			}
		}
		catch (Throwable e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	@Override
	public int getTotal() {
		if (total == 0) {
			total = getRemoteTemplates().getSize();
		}
		return (int)total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" templates processed. ";
		result += String.valueOf(updated)+" templates updated</p>";
		return result;
	}
	
	private void syncTemplate(IEmailTemplate remote, KbeeEmailTemplate local) throws ContentMgmtException {
		local.setFrom(remote.getFrom());
		local.setSubject(remote.getSubject());
		local.setTitle(remote.getTitle());
		local.setStringTemplate(remote.getText());
	}
	
	private EmailTemplate getLocal(IEmailTemplate remote) {
		EmailTemplate local = getContentDao().findEmailTemplate(getSessionDomain(), remote.getLanguage(), remote.getKey());
		return local;
	}
	
	private IResultSet<IEmailTemplate> getRemoteTemplates() {
		return getServer().getEmailTemplates();
	}
}
