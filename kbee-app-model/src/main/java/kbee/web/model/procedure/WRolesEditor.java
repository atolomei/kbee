package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.content.workflow.KbeeWRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class WRolesEditor extends RelationEditor<Procedure, KbeeWRole> {	
	private static final long serialVersionUID = 1L;

	private IModel<Procedure> model;

 	public WRolesEditor(IModel<Procedure> model) {
		super("roles");
		this.model=model;
	}
	
 	public IModel<Procedure> getModel() {
		return model;
	}
	
 	
 	@Override
 	public IModel<String> getHelp() {
 		return new StringResourceModel("wrole-help", this, null);
 	}
 	
 	
 	@Override
 	public boolean isItemTextVisible() {
		return true;
	}
 	
 	public void onDetach() {
		if (model!=null)
			model.detach();
		super.onDetach();
	}
	
 	
 	@Override
 	public boolean isItemLink() {
 			return false;
 	}
 	
 	@Override
	protected List<Property<?>> getProperties() {
		
		List<Property<?>> properties = new ArrayList<Property<?>>();

		properties.add(new Property<String>() {
			@Override
 			public String getName() {
				return "label";
			}
			@Override
			public boolean getTitle() {
				return true;
			}
		});


		if (isRoot()) {
			properties.add(new Property<String>() {
				@Override
	 			public String getName() {
					return "name";
				}
			});
		}
		
		
		return properties;
	}
	
	protected Property<?> getKey() {
		if (isRoot()) {
			return new Property<String>() {
				public String getName() {
					return "name";
				}
			};
		}
		else {
			return null;
		}
	}
	
	@Override
	protected KbeeWRole getNewValue() {
		KbeeWRole role = new KbeeWRole();
		return role;
	}

	@Override
	protected String getText(KbeeWRole value) {
		return null;
		//return value.getLabel();
	}

	@Override
	protected boolean creationEnabled() {
		return true;
	}
	
	private boolean isRoot() {
		return ServiceLocator.getService(SecurityService.class).isRoot();
	}
}
