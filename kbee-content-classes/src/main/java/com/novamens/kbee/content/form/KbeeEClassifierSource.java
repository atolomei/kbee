package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.novamens.content.form.EFormDataSource;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.service.DataAccessService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.dom.KbeeUrl;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public abstract class KbeeEClassifierSource implements EFormDataSource<DataSetMember> {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeEClassifierSource.class.getName());

	
	private Classificable object;

	
	public class DataSourceUrl implements Url {
		private static final long serialVersionUID = 1L;
		
		String label;
		com.novamens.dom.Url url;
		public DataSourceUrl(String label, com.novamens.dom.Url url) {
			setLabel(label);
			setUrl(url);
			
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public com.novamens.dom.Url getUrl() {
			return url;
		}
		public void setUrl(com.novamens.dom.Url url) {
			this.url = url;
		}
	}
	
	public KbeeEClassifierSource(Classificable object) {
		this.object = object;
	}
	
	public Classificable getObject() {
		return object;
	}
	
	@Override
	public List<DataSetMember> getValues() {
		return getRelation().getService(DataAccessService.class).getAll(getObject());
	}
	
	@Override
	public List<Suggestion> getValues(String pattern) {
		return getRelation().getService(DataAccessService.class).getSuggestions(pattern, getObject());
	}
	
	@Override
	public List<Suggestion> getValues(String pattern, Map<String, Object> parameters) {
		return getRelation().getService(DataAccessService.class).getSuggestions(pattern, getObject(), parameters);
	}
	
	public abstract ClassifierTemplate getRelation();
	
	public List<Url> getUrls() {
		List<Url> urls = new ArrayList<Url>();
		try {
			urls.add(getUrl(getRelation().getClassifier().getDataSet()));
			if (getRelation().getClassifier().getDataSet2()!=null) {
				urls.add(getUrl(getRelation().getClassifier().getDataSet2()));
			}
		} catch (Exception e) {
			logger.error(e);
			logger.error("User Classifier ?");
		}
		
		return urls;
	}
	
	public boolean isReadable() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()) || 
				ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId()) ||
				ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private DataSourceUrl getUrl(DataSet dataset) {
		
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("id", String.valueOf(dataset.getId()));
		
		String label;
		try {
			label = getLabel("values", 
				getSessionUser().getLocale()
				, dataset.getDisplayName());
			
		} catch (Exception e) {
			label=e.getClass().getName();
					
			
		}
		
		String path = dataset instanceof UserSet ?	"security-users-page" :	"settings-dataset-members-page";
		
		if (dataset instanceof UserSet) parameters = null;
		
		
		com.novamens.dom.Url domurl = new KbeeUrl(path, parameters);
		DataSourceUrl url = new DataSourceUrl(label, domurl);
		return url;
	}
	
	private String getLabel(String key, Locale locale, String... parameter) {
		ResourceBundle resources = ResourceBundle.getBundle( KbeeEClassifierSource.class.getName(), locale);
		String label = resources.getString(key);
		for (int p=0; p<parameter.length; p++) {
			label = label.replace("{"+String.valueOf(p)+"}", parameter[p]);
		}
		return label;
	}
}