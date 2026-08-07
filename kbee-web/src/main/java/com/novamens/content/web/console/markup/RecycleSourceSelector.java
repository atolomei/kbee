package com.novamens.content.web.console.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class RecycleSourceSelector extends ToolbarItem {
	private static final long serialVersionUID = 1L;
	
	private static String AllSources = "recyle.source.all";
	private static String WorkspaceSource = "recyle.source.workspace";
	private static String LibrarySource = "recyle.source.library";
	private static String TemplatesSource = "recyle.source.templates";
	
	private String source;

	public RecycleSourceSelector(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		setOutputMarkupId(true);
	}


	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		String head = (String)getBrowser().getQuery().getParameters().get("head");
		String template = (String)getBrowser().getQuery().getParameters().get("istemplate");
		
		if (head==null && template==null) {
			setSource(AllSources);
		}
		else {
			if (head!=null && "true".equals(head)) {
				setSource(LibrarySource);
			}
			else {
				if (head!=null && "false".equals(head)) {
					setSource(WorkspaceSource);
				}
			}
		}
		if (get("source")==null) {
			addChoices();
		}
	}
	
	/**-----------------------------------------------------------------------------
	 * @return
	 */
	public List<String> getSources() {
		List<String> sources = new ArrayList<String>();
		sources.add(AllSources);
		sources.add(WorkspaceSource);
		sources.add(LibrarySource);
		if (getDomain().getDomainType()!=DomainType.EXPRESS)
			sources.add(TemplatesSource);
		return sources;
	}
	
	/**-----------------------------------------------------------------------------
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**-----------------------------------------------------------------------------
	 */

	public String getSource() {
		return source;
	}

	/**-----------------------------------------------------------------------------
	 */
	
	protected void addChoices() {
		
		add(new ExtendedChoiceField<String>("source", new PropertyModel<String>(this, "source"), new PropertyModel<List<String>>(this, "sources")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				String source = getValue();
				if (source.equals(AllSources)) {
					getBrowser().getQuery().getParameters().remove("istemplate");
					getBrowser().getQuery().getParameters().remove("-istemplate");
					getBrowser().getQuery().getParameters().remove("head");
				}
				if (source.equals(WorkspaceSource)) {
					getBrowser().getQuery().getParameters().put("head", "false");
				}
				if (source.equals(LibrarySource)) {
					getBrowser().getQuery().getParameters().remove("istemplate");
					getBrowser().getQuery().getParameters().put("-istemplate", "true");
					getBrowser().getQuery().getParameters().put("head", "true");
				}
				if (source.equals(TemplatesSource)) {
					getBrowser().getQuery().getParameters().remove("head");
					getBrowser().getQuery().getParameters().remove("-istemplate");
					getBrowser().getQuery().getParameters().put("istemplate", "true");
				}
				getBrowser().resetSelection();
				getBrowser().refresh(target);
			}
			@Override
			public String getIdValue(String value) {
				return value.toLowerCase();
			}
			@Override
			public String getDisplayValue(String value) {
				return (new StringResourceModel(value, RecycleSourceSelector.this, null)).getObject();
			}
		});
	}
	
	/**-----------------------------------------------------------------------------
	 */

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}
