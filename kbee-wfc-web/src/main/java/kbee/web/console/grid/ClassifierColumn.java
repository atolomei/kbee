package kbee.web.console.grid;

import java.time.OffsetDateTime;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetType;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
			
public class ClassifierColumn<T extends Classificable> extends GridColumn<SearchResult, String> {
	
	private static final long serialVersionUID = 1L;
	private IModel<Classifier> model;
	
	private String console_name;
	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ClassifierColumn.class.getName());
	
	/**
	 * 
	 * THis uses 
	 * 
	 * {@link JavaContentIndexFactory}
	 * private DocumentSchema createDataSetMemberSchema(Domain domain)
	 * 
	 * 
	 */
	public ClassifierColumn(IModel<Classifier> model, String console_name) {
		super(model.getObject().getId().toString(), new Model<String>(model.getObject().getName()));
		this.console_name = console_name;
		setModel(model);
		setPreferred(model.getObject().isDefaultGridColumn());
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	@Override
	protected IModel<String> getLabelModel(SearchResult object) {
		try {
				StringBuffer label = new StringBuffer();
				for (Classification classification : ((Classificable) object.getObject()).getClassification()) {

					Classifier classifier = classification!=null ? classification.getClassifier() : null;
					
					if (classifier!=null && getClassifier()!=null && classifier.getId().equals(getClassifier().getId())) {
						if (classifier.getDataSet().getDataSetType().equals(DataSetType.DATE)) {
							if (classification.getDateValue()!=null) {
								if (label.length()>0)
									label.append(", ");
								OffsetDateTime dt=classification.getDateValue();
								label.append(ServiceLocator.getService(DateTimeService.class).getDomainInOriginalGMTDateDisplayString(dt,getSessionUser().getLocale())); 
							}
							else {
								label.append("");
							}
						}
						else {	
							if (label.length()>0)
								label.append(", ");
							label.append(classification.getDataSetMember()!=null ? classification.getDataSetMember().getDisplayName() : "-");
						}
					}
				}
				return new Model<String>(label.toString());
				
		}catch (Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass().getName() + " | " + e.getMessage());
		}
	}
	
	
	@Override
	protected String getContextKey() {
		return this.console_name + super.getContextKey();	
	}
	

	
	@Override
	public String getSortProperty() {
		return getClassifier().isOrdered() ? getClassifier().getUniqueName()+"name_sort" : null;
	}
	
	public IModel<Classifier> getModel() {
		return model;
	}
	
	public void setModel(IModel<Classifier> model) {
		this.model = model;
	}
	
	public Classifier getClassifier() {
		return getModel().getObject();
	}
	
	@Override
	public void detach() {
		super.detach();
		getModel().detach();
	}
}
