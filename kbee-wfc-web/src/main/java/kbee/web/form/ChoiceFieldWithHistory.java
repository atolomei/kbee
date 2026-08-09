package kbee.web.form;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidator;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class ChoiceFieldWithHistory<T> extends ChoiceField<T> {
	private static final long serialVersionUID = 1L;

	static private Logger logger =  Logger.getLogger(ChoiceFieldWithHistory.class.getName());
	
	private List<IModel<T>> history;
	//static private  final int MAX_HISTORY = 12;
	//private int max_history = MAX_HISTORY;
	private int max_history = 12;
	
	public class HistoryFragment extends Fragment {
		public HistoryFragment(String id) {
			super(id, "history-fragment", ChoiceFieldWithHistory.this);
			setOutputMarkupId(true);
		}
		@Override
		public void onInitialize() {
			super.onInitialize();
			
			
			
			
			
			add(new ListView<IModel<T>>("history", new PropertyModel<List<IModel<T>>>(ChoiceFieldWithHistory.this, "history")) {
				public void populateItem(final ListItem<IModel<T>> item) {
					AjaxLink<T> link = new AjaxLink<T>("link", item.getModelObject()) {
						public void onClick(AjaxRequestTarget target) {
							T value = item.getModelObject().getObject();
							setValue(value);
							target.add(ChoiceFieldWithHistory.this);
							onUpdate(target);
						}
					};
					try {
						link.add(new Label("name", DisplayNameExtractor.get(item.getModelObject().getObject())));
						item.add((new Label("separator", "-")).setVisible(item.getIndex()>0));
						item.add(link);
					}
					catch (Exception e) {
						logger.error(e);
					}
				}
				
			});
		}
		public boolean isVisible() {
			return ChoiceFieldWithHistory.this.isInputEnabled();
		}
	}
	
	
	/** --------------------------------------------------------------------------
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param id
	 */

	public ChoiceFieldWithHistory(String id) {
		this(id, null);
	}
	
	public ChoiceFieldWithHistory(String id, IModel<List<T>> choices) {
		this(id, null, choices, false);
	}
	
	public ChoiceFieldWithHistory(String id, IModel<List<T>> choices, boolean required) {
		this(id, null, choices, required);
	}
	
	public ChoiceFieldWithHistory(String id, IModel<T> model, IModel<List<T>> choices) {
		this(id, model, choices, false);
	}
	
	public ChoiceFieldWithHistory(String id, IModel<T> model, IModel<List<T>> choices, boolean required) {
		super(id, model, choices, required);
	}

	public ChoiceFieldWithHistory(String id, IModel<T> model, IModel<List<T>> choices, IValidator<T> validator) {
		super(id, model, choices, validator);
	}
	
	public List<IModel<T>> getHistory() {
		
		if (history!=null) 
			return history;
		
		this.history = new ArrayList<IModel<T>>();
		
		try {
			if (getHistoryKey()==null) {
				logger.debug("getHistoryKey()==null");
				return history;
			}
		} 
		catch (Exception e) {
			logger.error(e);
			return history;
		}
		
		String strvalue = getSessionUser()!=null ? 
			getSessionUser().getService(PreferencesService.class).getValue("autocomplete", getHistoryKey()) : 
			null;
		
		if (strvalue==null) 
			return history;
		
		StringTokenizer tokenizer = new StringTokenizer(strvalue, ";");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			IModel<T> valuemodel = null;
			try {
				valuemodel = deserialize(token);
				if (valuemodel!=null) {
					valuemodel.detach();
				}
			}
			catch (Exception e) {
				logger.error(e);
				valuemodel = null;
			}
			if (valuemodel!=null && isValid(valuemodel)) {
				history.add(valuemodel);
			}
		}
		
		return history;
	}
	
	public String getHistoryKey() {
		return null;
	}
	
	public int getMaxHistory() {
		//return max_history;
		return 12;
	}
	
	public boolean isNullValid() {
		return true;
	}

	@Override
	public void setValue(T value) {
		super.setValue(value);
		addHistory(value);
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("history")==null) {
			add(new HistoryFragment("history"));
		}
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		history = null;
	}

	
	protected boolean isValid(IModel<T> model) {
		return true;
	}
	

	
	protected void addHistory(T value) {
		boolean found = false;
		if (!(value instanceof Identifiable))
			return;
		
		for (IModel<T> model : getHistory()) {
			T object = model.getObject();
			if (object!=null && object.equals(value)) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			getHistory().add(0, getModel(value));
			if (getHistory().size()>getMaxHistory()) {
				getHistory().remove(getHistory().size()-1);
			}
			updateHistory();
		}
	}
	
	protected void updateHistory() {
		if (getHistoryKey()==null)
			return;
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for (IModel<T> model : getHistory()) {
			if (model.getObject()!=null) {
				if (!first) 
					buffer.append(";");
				buffer.append(serialize(model));
				first = false;
			}
		}		
		if (getSessionUser()!=null)
		getSessionUser().getService(PreferencesService.class).setValue("autocomplete", getHistoryKey(), buffer.toString());
	}
	
	
	protected String serialize(IModel<T> model) {
		String classname = model.getObject().getClass().getName();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		i = classname.indexOf("$");
		if (i>0) classname = classname.substring(0, i);
		return classname+"-"+((Identifiable)model.getObject()).getId();
	}
	
	protected IModel<T> deserialize(String token) {
		int i = token.indexOf("-");
		if (i<=0) return null;
		String classname = token.substring(0, i);
		String id = token.substring(i+1);
		IModel<T> model = getModel(classname, id);
		return model;
	}
	
	protected IModel<T> getModel(String classname, String id) {
		ObjectModel<T> model = null;
		try {
			Class<?> clazz = Class.forName(classname);
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			@SuppressWarnings("unchecked")
			Object object = (T)sf.getCurrentSession().get(clazz, Long.valueOf(id));
			if (object!=null) {
				model = new ObjectModel<T>(clazz, Long.valueOf(id));
				model.getObject();
			}
		}
		catch (Exception e) {
			model = null;
		}
		return model;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
