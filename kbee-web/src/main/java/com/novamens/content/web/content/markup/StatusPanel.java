package com.novamens.content.web.content.markup;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.kbee.wicket.model.ModelPanel;

/**
 *  
 *  Title right panel 
 *  in Library
 *  <b>Section</b> and <b>Version</b>
 *  
 * @param <T>
 */

@Deprecated
@SuppressWarnings("serial")
public class StatusPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
			
	private static final Logger logger = LogManager.getLogger(StatusPanel.class.getName());

	public StatusPanel(IModel<T> model) {
		super("status", model);
		
		add(new Label("status", new Model<String>() {
			
			public String getObject() {

				try {
					Content content = StatusPanel.this.getModelObject();
	
					if (content.isEnabled()) {
						List<Library> libraries = content.getDomain().getService(LibraryService.class).getLibraries(content);
						if (!libraries.isEmpty()) {
							return libraries.get(0).getDisplayName();
						}
						
//						CabinetStatus cs= content.getDomain().getService(DomainService.class).getCabinetStatus();
//						if (content.isExternal()) 										return cs.external_name;
//						else if (content.getContentTemplate().isKnowledgeBaseCabinet())	return cs.kbase_name;
//						else if (content.getContentTemplate().isTemplatesCabinet())		return cs.template_name;
//						else 
//							return cs.standard_name;
						
					}
	
					if (content.isArchived())				return getLabel("content.status.archived").getObject();
					if (content.isRecycled())				return getLabel("content.status.recycled").getObject();
					if (!content.isHeadVersion())			return getLabel("content.status.versioned").getObject();
					
					return "N/A";
				} 
				catch (Exception e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
						logger.error(e.getMessage());
						return "err: " + e.getClass().getSimpleName();
				}
			}
		}));
					
		WebMarkupContainer versioncontainer = new WebMarkupContainer("version-container");
		versioncontainer.add(new Label("version", new Model<String>() {
			public String getObject() {
				return String.valueOf(StatusPanel.this.getModelObject().getVersion());
			}
		})); 
		
		versioncontainer.add(new Label("head", new 	StringResourceModel ((StatusPanel.this.getModelObject().isHeadVersion() ? "headversion":"previousversion"), StatusPanel.this, null)));
			
		add(versioncontainer);
	}	
	
//	@Override
//	public boolean isVisible() {
//		return true;
//		
//	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
}
