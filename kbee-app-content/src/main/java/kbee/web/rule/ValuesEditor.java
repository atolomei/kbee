package kbee.web.rule;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.rule.ClassificationAction;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.service.MemberSuggestionService;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class ValuesEditor extends RelationEditor<ClassificationAction, DataSetMember> {
			
	//private static Logger logger = Logger.getLogger(ValuesEditor.class.getName());
	
	private static final long serialVersionUID = 1L;

	public ValuesEditor(IModel<Collection<DataSetMember>> model) {
		super("values");
		setPropertyModel(model);
	}
	
	public List<Suggestion> getSuggestions(String pattern) {
		if (getClassifier()!=null) {
			return getClassifier().getDataSet().getService(MemberSuggestionService.class).getSuggestions(pattern); 
		}
		return null; 
	}
	
	@Override
	public boolean ordered() {
		return true;
	}

	@Override
	protected Property<?> getKey() {
		return new Property<DataSetMember>() {
			public String getName() {
				return "values";
			}
			public boolean isAutocomplete() {
				return true;
			}
			public List<Suggestion> getSuggestions(String pattern) {
				return ValuesEditor.this.getSuggestions(pattern);
			}
		};
	}
	
	protected Classifier getClassifier() {
		return null;
	}

//	@Override
//	protected int compare(IModel<Group> a, IModel<Group> b) {
//		try {
//		if (a.getObject().getName()==null)
//			return (b.getObject().getName()!=null?1:0);
//		else if(b.getObject().getName()==null)
//			return -1;
//		return a.getObject().getName().compareToIgnoreCase(b.getObject().getName());
//		} catch (Exception e) {
//			logger.error(e);
//			return 0;
//		}
//	}
//	
//	@Override
//	protected void onValueClick(IModel<Group> model) {
//	
//		final boolean role_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
//		final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
//		
//		if (role_security)
//			setResponsePage(new GroupStandAlonePage(model));
//		else  {
//			setResponsePage(new ErrorPage<Object>( 
//				new Model<String>("Your user account doesn't have rights to read Group " + model.getObject().getName()), 
//				new Model<String>("Groups")));
//			// TODO Alert Window
//			//
//		}
//	}
//	
//	@Override
//	protected boolean deleteEnabled(Group value) {
//		return !value.isDerived();
//	}
}