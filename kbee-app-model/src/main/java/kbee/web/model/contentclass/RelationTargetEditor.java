package kbee.web.model.contentclass;

import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class RelationTargetEditor extends RelationEditor<RelationTemplate, ContentTemplate> {
	private static final long serialVersionUID = 1L;

	public RelationTargetEditor() {
		super("targetTemplates");
	}
	
	public List<ContentTemplate> getTemplates() {
		List<ContentTemplate> templates = getContentDao().getTemplates(getDomain());
		return templates;
	}
	
	@Override
	protected Property<?> getKey() {
		return new Property<ContentTemplate>() {
			public String getName() {
				return "template";
			}
			public List<ContentTemplate> getChoices() {
				return getTemplates();
			}
		};
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}