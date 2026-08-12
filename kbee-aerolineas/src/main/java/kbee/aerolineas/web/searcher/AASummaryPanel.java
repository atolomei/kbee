package kbee.aerolineas.web.searcher;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.script.KbeeClassificableScriptWrapper;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;

import kbee.web.command.panel.CommandAttributePanelV5;

@SuppressWarnings("serial")
public class AASummaryPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private List<Panel> panels;

	/**
	 * 
	 * @param id
	 * @param model
	 * 
	 */
	public AASummaryPanel(String id, IModel<T> model, IModel<Site> siteModel, boolean  isConsole) {
		super(id, model);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		List<Panel> _list = getPanels();
		
		add(new ListView<Panel>("result",  _list) {
			protected void populateItem(ListItem<Panel> item){
				item.setOutputMarkupId(true);
				item.add(item.getModelObject());
				item.setVisible(item.getModelObject().isVisible());
			}
		});
	}
	
	public List<Panel> getPanels() {

		if (this.panels!=null)
			return this.panels;
		
		this.panels = new ArrayList<Panel>();
	 
		IModel<String> kcss = new Model<String>("col-lg-3 col-md-7 col-xs-7 keyc");
		IModel<String> vcss = new Model<String>("col-lg-9 col-md-5 col-xs-5 valuec");
		
		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("version"), 	
			getValue("numero_revision"), 
			kcss, 
			vcss));
	
		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("date"), 	
			getDate("fecha"), 
			kcss, 
			vcss));
	
		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("published-by"),
			new Model<String>(getModelObject().getLastModifiedUser().getFirstLastName() ), 
			kcss, 
			vcss));
	
		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("published-on"),
			format(getModelObject().getCheckinOffsetDateTime()), 
			kcss, 
			vcss));

		this.panels.add(new CommandAttributePanelV5("command_item", 
			getLabel("content-type"), 
			getModelObject().getContentTypeClassificationAsString(), 
			kcss, 
			vcss));

	
		return this.panels;
	}

//	protected IModel<Site> getSiteModel() {
//		return this.site_model;
//	}
//	
//	protected Page getContentPage(IModel<Content> model, int index, final boolean openandedit) {
//		return null;
//	}
//
//	protected String getContentClass(T content) {
//		return ProxyUtil.getClassName(content).toLowerCase();
//	}
//	
//	protected boolean isOpenEnabled() {
//		if (this.is_enabled==null) {
//			this.is_enabled=Boolean.valueOf(isSupportUser() || isReadable(getModel()));
//		}
//		return this.is_enabled.booleanValue();
//	}
//	
//	protected boolean isSupportUser() {
//		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
//	}
//	
//	protected boolean isReadable(IModel<T> model) {
//		return ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(model.getObject());
//	}
//	
//	protected String getSubtitle(IModel<T> model) {
//		return model.getObject().getService(ContentService.class).getConsoleSubtitleDefaultIfNull();
//	}
	
	protected String format(OffsetDateTime time) {
		return ServiceLocator.getService(DateTimeService.class).getDateDisplayString(time);
	}
	
	protected String getValue(String classifier) {
		KbeeClassificableScriptWrapper wrapper = new KbeeClassificableScriptWrapper(getModelObject());
		return wrapper.getLabel(classifier);
	}
	
	protected String getDate(String classifier) {
		KbeeClassificableScriptWrapper wrapper = new KbeeClassificableScriptWrapper(getModelObject());
		OffsetDateTime time = wrapper.getDateTime(classifier);
		return time!=null ? format(time) : "";
	}
}