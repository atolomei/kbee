package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.AreaType;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

public class PortalAreaEditor extends DomainObjectEditor<Area> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalAreaEditor.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	
	public PortalAreaEditor(String id, IModel<Area> model) {
		super(id, model);
		setOutputMarkupId(true);
	}


	public void onDetach() {
		super.onDetach();
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		
		
	

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
		
		form.add(new TextField<String>("title", true));
		form.add(new TextField<String>("key"));
		form.add(new TextAreaField<String>("description", 4, 40));
		
		
		ChoiceField<AreaType> cha= new ChoiceField<AreaType>("type", 
				new PropertyModel<AreaType>(this, "areaType"),
				new PropertyModel<List<AreaType>>(this,"areaTypes"));
		form.add(cha);

		
		BooleanField he = new BooleanField("header", 	new PropertyModel<Boolean>(this, "header"));
		form.add(he);

		
		// PageSectionType
		// PageSectionDisposition
		
		//if (getModel()!=null && getModel().getObject()!=null)
		//	form.add((new StaticField<String>("id", new Model<String>(getModel().getObject().getId().toString()))).setVisible(isAdminSessionUser()));
		//else
		//	form.add(new InvisiblePanel("id"));
		
		add(new EditButtonsV5<Area>(this) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-xs"; 
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-xs";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-xs";
			}
			
			@Override
			public boolean isVisible() {
				return !isReadOnly();
			}
			
			@Override
			public boolean isEnabled()  {
				return true;
			}
		});		
	
	

	}
	

	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
		target.add( PortalAreaEditor.this);
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add( PortalAreaEditor.this);
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				Area area = getPortalDao().findAreaById(getModel().getObject().getId());
				Site site=area.getSite();
				site.getService(SiteService.class).update(getUpdatedParts());

				super.reset();
				target.add( PortalAreaEditor.this);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Area>(target, getModel(), e));

		}
	}

	public Boolean getHeader() {
		return Boolean.valueOf(getModel().getObject().isHeader());
		
	}
	
	
	public void setHeader(Boolean b) {
		getModel().getObject().setHeader(b);
	}
	
	public void setAreaType(AreaType  a) {
		getModel().getObject().setAreaType(a);
	}
	
	public AreaType getAreaType() {
		return getModel().getObject().getAreaType();
	}
	
	
	public List<AreaType> getAreaTypes() {
		List<AreaType> types= new ArrayList<AreaType>();
		for (AreaType a:AreaType.ALL)
			types.add(a);
		return types;
		
	}


}
