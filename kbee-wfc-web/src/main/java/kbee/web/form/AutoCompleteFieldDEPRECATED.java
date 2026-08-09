package kbee.web.form;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

@Deprecated
@SuppressWarnings("serial")
public class AutoCompleteFieldDEPRECATED<T> extends TextField<T> {
	private static final long serialVersionUID = 1L;
	private Suggestion suggestion;
	private List<IModel<T>> history;
	
	public class HistoryFragment extends Fragment {
		public HistoryFragment(String id) {
			super(id, "history-fragment", AutoCompleteFieldDEPRECATED.this);
			add(new ListView<IModel<T>>("history", new PropertyModel<List<IModel<T>>>(AutoCompleteFieldDEPRECATED.this, "history")) {
				public void populateItem(final ListItem<IModel<T>> item) {
					AjaxLink<T> link = new AjaxLink<T>("link", item.getModelObject()) {
						public void onClick(AjaxRequestTarget target) {
							setValue(item.getModelObject().getObject());
							setSuggestion(null);
							onUpdate(target);
						}
					};
					link.add(new Label("name", DisplayNameExtractor.get(item.getModelObject().getObject())));
					item.add((new Label("separator", "-")).setVisible(item.getIndex()>0));
					item.add(link);
				}
			});
		}
	}
	
	public AutoCompleteFieldDEPRECATED(String id) {
		this(id, null, false, Width.W10);
	}
	
	public AutoCompleteFieldDEPRECATED(String id, Width width) {
		this(id, null, false, width);
	}
	
	public AutoCompleteFieldDEPRECATED(String id, boolean required) {
		this(id, null, required, Width.W10);
	}
	
	public AutoCompleteFieldDEPRECATED(String id, IModel<T> model) {
		this(id, model, false, Width.W10);
	}
	
	public AutoCompleteFieldDEPRECATED(String id, IModel<T> model, boolean required, Width width) {
		super(id, model, required, width, null);
	}
	
	public Suggestion getSuggestion() {
		return suggestion;
	}
	
	public void setSuggestion(Suggestion suggestion) {
		this.suggestion = suggestion;
	}
	
	protected AutoCompleteSettings getSettings() {
		AutoCompleteSettings settings = new AutoCompleteSettings();
		settings.setThrottleDelay(700);  
		settings.setAdjustInputWidth(false);
//		settings.setMinInputLength(0);
		//settings.setShowListOnEmptyInput(true);
		settings.setCssClassName("suggestions");
		return settings;
	}
	
	protected org.apache.wicket.markup.html.form.TextField<?> newTextField() {
		
//		AutoCompleteTextField input = new AutoCompleteTextField("input", new PropertyModel<Suggestion>(this, "suggestion"), new SuggestionRender(), getSettings()) {
//			@Override
//			@SuppressWarnings("unchecked")
//			public void onChange(AjaxRequestTarget target, Suggestion suggestion) {
//				if (suggestion!=null && suggestion.getObject() instanceof IModel) {
//					setValue(((IModel<T>)suggestion.getObject()).getObject());
//					addHistory(AutoCompleteField.this.getValue());
//					setSuggestion(null);
//				}
//				else {
//					if (suggestion!=null && !(suggestion.getObject() instanceof String)) {
//						setValue((T)suggestion.getObject());
//						addHistory(AutoCompleteField.this.getValue());
//						setSuggestion(null);
//					}
//				}
//			};
//			@Override
//			public void validate() {
//				super.validate();
//				AutoCompleteField.this.validate();
//			}
//			@Override
//			public boolean isEnabled() {
//				return getEditor()!=null ? getEditor().isEditionEnabled() : true;
//			}
//			@Override
//			public List<Suggestion> getSuggestions(String pattern) {
//				return AutoCompleteField.this.getSuggestions(pattern);
//			}
//		};
//		
//		return input;
		return null;
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		
	}
	
	public List<Suggestion> getSuggestions(String pattern) {
		return null;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		WebMarkupContainer searcher = new WebMarkupContainer("searcher"){
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		searcher.add(new AttributeModifier("onclick", "top."+getInput().getMarkupId()+".show();"));
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			if (get("horizontal-layout:control:searcher")==null) {
				((MarkupContainer)get("horizontal-layout:control")).add(searcher);
				((MarkupContainer)get("horizontal-layout:control")).add(new HistoryFragment("history"));
			}
		}
		else {
			if (get("control:searcher")==null) {
				((MarkupContainer)get("control")).add(searcher);
				((MarkupContainer)get("control")).add(new HistoryFragment("history"));
			}
		}
	}
	
	@Override
	public void onDetach() {
		history = null;
		super.onDetach();
	}
	
	protected String getHistoryKey() {
		return null;
	}
	
	protected IModel<T> getModel(String classname, String id) {
		ObjectModel<T> model = null;
		try {
			Class<?> clazz = Class.forName(classname);
			model = new ObjectModel<T>(clazz, Long.valueOf(id));
			model.getObject();
		}
		catch (Exception e) {
			model = null;
		}
		return model;
	}
	
	protected void addHistory(T value) {
		boolean found = false;
		if (!(value instanceof Identifiable))
			return;
		
		for (IModel<T> model : getHistory()) {
			if (model.getObject().equals(value)) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			getHistory().add(0, getModel(value));
			if (getHistory().size()>3) {
				getHistory().remove(getHistory().size()-1);
			}
			updateHistory();
		}
	}
	
	public List<IModel<T>> getHistory() {
		if (history!=null) 
			return history;
		
		history = new ArrayList<IModel<T>>();
		
		if (getHistoryKey()==null)
			return history;
		
		String strvalue = getUser().getService(PreferencesService.class).getValue("autocomplete", getHistoryKey());
		
		if (strvalue==null) 
			return history;
		
		StringTokenizer tokenizer = new StringTokenizer(strvalue, ";");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int i = token.indexOf("-");
			if (i>0) {
				String classname = token.substring(0, i);
				String id = token.substring(i+1);
				IModel<T> valuemodel = getModel(classname, id);
				if (valuemodel!=null) {
					history.add(valuemodel);
				}
			}
		}
		
		return history;
	}
	
	/**
	 * [VER AT]
	 */
	protected void updateHistory() {
		if (getHistoryKey()==null)
			return;
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for (IModel<T> model : getHistory()) {
			if (!first) 
				buffer.append(";");
			String classname = model.getObject().getClass().getName();
			int i = classname.indexOf("_");
			if (i>0) classname = classname.substring(0, i);
			buffer.append(classname+"-"+((Identifiable)model.getObject()).getId());
			first = false;
		}						
		getUser().getService(PreferencesService.class).setValue("autocomplete", getHistoryKey(), buffer.toString());
	}
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
 