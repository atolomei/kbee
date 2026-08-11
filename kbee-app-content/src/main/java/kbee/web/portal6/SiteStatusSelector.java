package kbee.web.portal6;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;

import kbee.web.console.BaseBrowser;

public class SiteStatusSelector extends ToolbarItem {

	private static final long serialVersionUID = 1L;

	private static String all = "status.all";
	private static String draft = "status.draft";
	private static String enabled = "status.enabled";
	private static String archived = "status.archived";


	private String source;

	public SiteStatusSelector(BaseBrowser<?> browser, Align align) {
		this(browser, align, ToolbarItem.JUSTIFY_LEFT);
	}
	
	
	public SiteStatusSelector(BaseBrowser<?> browser, Align align, int justify) {
		super(browser, align);

		setOutputMarkupId(true);
																			
		if 		(getBrowser().getQuery().getParameters().get("status") == null)													this.source = all;
		else if (getBrowser().getQuery().getParameters().get("status") == String.valueOf(ObjectState.ENABLED.getId()))			this.source = enabled;
		else if (getBrowser().getQuery().getParameters().get("status") == String.valueOf(ObjectState.ARCHIVED.getId()))			this.source = archived;
		else if (getBrowser().getQuery().getParameters().get("status") == String.valueOf(ObjectState.DRAFT.getId()))			this.source = draft;
		
		
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		String status = (String) getBrowser().getQuery().getParameters().get("status");

		if (status == null)
			setSource(all);

		else if ("enabled".equals(status))
			setSource(enabled);

		else if ("all".equals(status))
			setSource(all);

		else if ("draft".equals(status))
			setSource(draft);

		else if ("archived".equals(status))
			setSource(archived);

		if (get("source") == null)
			addChoices();

	}

	public List<String> getSources() {
		List<String> sources = new ArrayList<String>();
		sources.add(enabled);
		sources.add(archived);
		sources.add(draft);
		sources.add(all);
		
		return sources;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	protected void addChoices() {

		/**
		<wicket:head>
		<style> 
			.kbee .grid-toolbar .bootstrap-select:not([class*="col-"]):not([class*="form-control"]):not(.input-group-btn) {width: 92px;}
		</style>
		</wicket:head>
		**/

		
		add(new ExtendedChoiceField<String>("source", new PropertyModel<String>(this, "source"),
				new PropertyModel<List<String>>(this, "sources")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onUpdate(AjaxRequestTarget target) {

				String source = getValue();

				if (source.equals(all))
					getBrowser().getQuery().getParameters().remove("status");

				else if (source.equals(draft))
					getBrowser().getQuery().getParameters().put("status", String.valueOf(ObjectState.DRAFT.getId()));

				else if (source.equals(enabled))
					getBrowser().getQuery().getParameters().put("status", String.valueOf(ObjectState.ENABLED.getId()));

				else if (source.equals(archived))
					getBrowser().getQuery().getParameters().put("status", String.valueOf(ObjectState.ARCHIVED.getId()));

				getBrowser().resetSelection();
				getBrowser().refresh(target);

			}

			@Override
			public String getIdValue(String value) {
				return value.toLowerCase();
			}

			@Override
			public String getDisplayValue(String value) {
				return (new StringResourceModel(value, SiteStatusSelector.this, null)).getObject();
			}
			
			@Override
			public String getCss() {
				return "browser-select";
			}
		});
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}
