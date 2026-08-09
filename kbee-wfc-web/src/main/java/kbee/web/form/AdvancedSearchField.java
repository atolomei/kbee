package kbee.web.form;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidator;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;

public class AdvancedSearchField<T> extends AutoCompleteFieldV5<T> {
	private static final long serialVersionUID = 1L;
	
	
	public AdvancedSearchField(String id) {
		this(id, null, false, Width.W12);
	}
	
	public AdvancedSearchField(String id, IValidator<T> validator) {
		this(id, null, false, Width.W12);
		add(validator);
	}
	
	public AdvancedSearchField(String id, Width width) {
		this(id, null, false, width);
	}
	
	public AdvancedSearchField(String id, boolean required) {
		this(id, null, required, Width.W12);
	}
	
	public AdvancedSearchField(String id, IModel<T> model) {
		this(id, model, false, Width.W12);
	}
	
	public AdvancedSearchField(String id, IModel<T> model, boolean required) {
		this(id, model, required, Width.W12);
	}
	
	public AdvancedSearchField(String id, IModel<T> model, boolean required, Width width) {
		super(id, model, required, width);
	}
	
	@Override
	@SuppressWarnings("serial")
	public void onBeforeRender() {
		super.onBeforeRender();
		Component control = getControl(); 
		if (control!=null) {
			
			if (control.get("advanced-link")==null) {
				
				IModel<String> labelmodel = new StringResourceModel("advanced-search", AdvancedSearchField.this, null);
				
				AjaxLink<?> searchlink = new AjaxLink<Void>("advanced-link") {
					public boolean isVisible() {
						return !isOpen();
					}
					public void onClick(AjaxRequestTarget target) {
						setOpen(true);
						onOpenAdvancedSearch(target);
					}
				};
				
				searchlink.add(new Label("label", labelmodel));
				((MarkupContainer)control).add(searchlink);
				
				IModel<String> clear_labelmodel = new StringResourceModel("clear", AdvancedSearchField.this, null);
				
				AjaxLink<?> clear_link = new AjaxLink<Void>("clear-link") {
					public boolean isVisible() {
						return !isOpen();
					}
					public void onClick(AjaxRequestTarget target) {
						AdvancedSearchField.this.getModel().setObject(null);
						clearInput();
						target.add(AdvancedSearchField.this);
					}
				};
				clear_link.add(new Label("clear-label", clear_labelmodel));
				((MarkupContainer)control).add(clear_link);
			}
		}		
	}

	boolean isOpen= false;
	
	public void setOpen(boolean b) {
		this.isOpen=b;
	}
	
	public boolean isOpen() {
		return this.isOpen;
	}
	
	
	public void onOpenAdvancedSearch(AjaxRequestTarget target) {
		
	}

	@Override
	public int getMaxHistory() {
		return 12;
		
	}

	@Override
	public String getHistoryKey() {
		return null;
	}
}
 