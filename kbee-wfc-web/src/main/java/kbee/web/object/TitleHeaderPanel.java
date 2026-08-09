package kbee.web.object;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;

/**
 * 
 * $attribute:Unit$ - $attribute:Resident (Last, First) Name$ - $classifier:Document Type$ - $attribute:Effective date:MM/dd/yy$ - $classifier:Site Name$
 *
 * @param <T>
 */
public abstract class TitleHeaderPanel<T> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	/** 
	 * 
	 * <p>Title Panel for Object Pages that use a {@link Breadcrumb} under the title.
	 * The Title is taken by default from the object displayName if available.</p>
	 * 
	 * <p>Used by: {@link Domain}, {@link Group}, {@link WorkflowRule}, {@link DataSet}, {@link Classifier}, and others.</p>
	 * 
	 * @param id
	 * @param model
	 * 
	 */
		
	public TitleHeaderPanel(String id, IModel<T> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);
	}

	public void onUpdate(AjaxRequestTarget target) {
		target.add(this);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
	
		add(new Label("title", getTitle()) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return (getTitle()!=null && getTitle().getObject()!=null);
			}
		});
		
		
		if (get("breadcrumb")==null)
			add(new InvisiblePanel("breadcrumb"));
		
		WebMarkupContainer ic = new WebMarkupContainer("icon-container");
		add(ic);
		
		WebMarkupContainer i = new WebMarkupContainer("icon");
		ic.add(i);

		if (getGlyphicon()!=null) 
			i.add(new  AttributeModifier("class",getGlyphicon()));
		
		else 
			ic.setVisible(false);
	}

	protected IModel<String> getTitle() {
		if (getModel().getObject() instanceof Identifiable) {
			return new PropertyModel<String>(getModel(), "displayName");	
		}
		return new Model<String>("-");
	}

	protected IModel<String> getGlyphicon() {
			return null;
	}

	protected Image getPhoto() {
		return null;
	}
	
	/** 
	 * @param panel must have id "breadcrumb"
	 */
	protected  void setBreadCrumbPanel(Panel panel) {
		addOrReplace(panel);
	}
	
	//protected boolean isFreeVersion() {
	//	return getDomain().getDomainType()==DomainType.FREE;
	//}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
}
