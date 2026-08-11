package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.kbee.content.workflow.KbeeProcedurePhase;
import com.novamens.workflow.Procedure;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class PhasesEditor extends RelationEditor<Procedure, KbeeProcedurePhase> {	
	private static final long serialVersionUID = 1L;

	private IModel<Procedure> model;

 	public PhasesEditor(IModel<Procedure> model) {
		super("phases");
		this.model=model;
	}
	
	public IModel<Procedure> getModel() {
		return model;
	}
	
	@Override
	public IModel<String> getHelp() {
		return getLabel("phase-help");
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
	protected String getTitle(KbeeProcedurePhase value) {
		return value.getLabel(getLocale());
	}

	protected String getText(KbeeProcedurePhase value) {
		return null;
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
					return "name";
				}
			});
		
		properties.add(new Property<String>() {
			@Override
			public String getName() {
				return "icon";
			}
		});
			
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

		
		return properties;
	}
	
	protected Property<?> getKey() {
		return new Property<String>() {
			public String getName() {
				return "name";
			}
		};
	}
	
	@Override
	protected KbeeProcedurePhase getNewValue() {
		return new KbeeProcedurePhase();
	}


	@Override
	protected boolean creationEnabled() {
		return true;
	}
}