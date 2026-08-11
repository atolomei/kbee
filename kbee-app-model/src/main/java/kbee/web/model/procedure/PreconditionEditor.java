package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.workflow.KbeeValidator;
import com.novamens.service.ServiceLocator;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class PreconditionEditor<T> extends RelationEditor<T, KbeeValidator> {
	private static final long serialVersionUID = 1L;
	
	class IqlValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String statement = validatable.getValue();
			try {
				IqlService iqlservice = getDomain().getService(IqlService.class);
				ResultSet set = iqlservice.execute(statement);
				set.hasNext();
			} 
			catch (RuntimeException e) {
				validatable.error(new ValidationError(this));
			}
		}
	}

	public PreconditionEditor(String id) {
		super(id);
	}
	
	public PreconditionEditor() {
		super("rules");
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
				return "condition";
			}
			@Override
			public IValidator<String> getValidator() {
				return new IqlValidator();
			}
		});
		
		properties.add(new Property<String>() {
			@Override
			public String getName() {
				return "message";
			}
		});
		
		return properties;
	}
	
	@Override
	protected KbeeValidator getNewValue() {
		KbeeValidator validator = new KbeeValidator();
		return validator;
	}
	
	@Override
	protected String getTitle(KbeeValidator value) {
		return value.getCondition()!=null && !"".equals(value.getCondition()) ? value.getCondition() : "null condition";
	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
