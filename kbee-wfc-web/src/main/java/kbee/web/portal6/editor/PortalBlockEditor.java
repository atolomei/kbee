package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

public class PortalBlockEditor extends DomainObjectEditor<Block> {

	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalBlockEditor.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	
	
	public PortalBlockEditor(String id, IModel<Block> model) {
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
		form.add(new TextField<String>("key", true));
		
		// form.add(new TextField<String>("css", false));
		
		form.add(new TextAreaField<String>("description", 4, 40, false));

		
		ChoiceField<AreaSection> cha= new ChoiceField<AreaSection>("areaSection", 
				new PropertyModel<AreaSection>(this, "areaSection"),
				new PropertyModel<List<AreaSection>>(this,"AreaSections"));
		form.add(cha);
		
		
		
		
		//if (getModel()!=null && getModel().getObject()!=null)
		//	form.add((new StaticField<String>("id", new Model<String>(getModel().getObject().getId().toString()))).setVisible(isAdminSessionUser()));
		//else
		//	form.add(new InvisiblePanel("id"));
		
		add(new EditButtonsV5<Block>(this) {
			
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
		target.add( PortalBlockEditor.this.getParent());
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add( PortalBlockEditor.this.getParent());
	}

	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				
				Block block= getPortalDao().findBlockById(getModel().getObject().getId());
				Site site=block.getSite();
				site.getService(SiteService.class).update(getUpdatedParts());
				
				super.reset();
				target.add( PortalBlockEditor.this);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Block>(target, getModel(),  e));

		}
	}

	
	public void setAreaSection(AreaSection  a) {
		getModel().getObject().setAreaSection(a);
	}
	
	public AreaSection getAreaSection() {
		return getModel().getObject().getAreaSection();
	}
	
	
	public List<AreaSection> getAreaSections() {
		List<AreaSection> types= new ArrayList<AreaSection>();
		Area area = (Area) getModel().getObject().getParent();
		
		if (area==null || area.getAreaSections()==null) {
			logger.error("area or areaSections is null");
			return types;
		}
		
		for (AreaSection s: area.getAreaSections()) 
			types.add(s);
		
		return types;
		
	}

	

}
