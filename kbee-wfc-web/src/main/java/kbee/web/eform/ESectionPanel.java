package kbee.web.eform;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormSection;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import kbee.web.error.ErrorPanel;

@SuppressWarnings("serial")
public class ESectionPanel extends EComponentPanel<EFormSection> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ESectionPanel.class.getName());

	public ESectionPanel(String id, EFormSection section, IModel<EFormData> data) {
		super(id, section, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addComponents();
	}
	
	protected void addComponents() {
		
		getContainer().add(new Label("label", getComponent().getLabel()));
		
		WebMarkupContainer sc=new WebMarkupContainer("subtitle-container");
		sc.setVisible(getComponent().getSublabel()!=null);
		Label la=new Label("subtitle", getComponent().getSublabel());
		la.setEscapeModelStrings(false);
		sc.add(la);
		getContainer().add(sc);
		
		getContainer().add(new ListView<EFormComponent>("component", getComponent().getComponents()) {
			public void populateItem(ListItem<EFormComponent> item) {
				try {
					Panel panel = getPanel("panel", item.getModelObject());
					if (panel!=null) {
						item.add(panel);
					}
					else {
						logger.debug("panel is null");
						item.add(new InvisiblePanel("panel"));
					}
				} catch (Exception e) {
					logger.error(e);
					item.add(new ErrorPanel("panel", e));
				}
			}
		});
	}
}