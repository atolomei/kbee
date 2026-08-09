package kbee.web.portal.dataprovider;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.portal6.model.ViewBK;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.portal6.editor.PortalBlockEditor;

public class ViewIQLEditor extends DomainObjectEditor<ViewBK> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ViewIQLEditor.class.getName());

	
	public ViewIQLEditor(String id, IModel<ViewBK> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);

		/**
		form.add(new TextField<String>("title", true));
		form.add(new TextAreaField<String>("description", 4, 40, false));
		ChoiceField<AreaSection> cha= new ChoiceField<AreaSection>("areaSection", 
				new PropertyModel<AreaSection>(this, "areaSection"),
				new PropertyModel<List<AreaSection>>(this,"AreaSections"));
		form.add(cha);
		*/
		
	add(new EditButtonsV5<ViewBK>(this) {
		
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
public void update(AjaxRequestTarget target) {
	try {
		if (!getUpdatedParts().isEmpty()) {
			
			
			//Block block= getPortalDao().findBlockById(getModel().getObject().getId());
			//Site site=block.getSite();
			//site.getService(SiteService.class).update(getUpdatedParts());
			
			super.reset();
			//target.add( PortalBlockEditor.this);
		}
	}
	catch (Exception e) {
		logger.error(e);
		//fire(new ErrorEvent<Block>(target, getModel(),  e));

	}
}

}
