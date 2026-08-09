package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Suggestion;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.SelectorPanel;
import com.novamens.wicket.markup.html.repeater.util.OnClickListener;

import kbee.web.form.AdvancedSearchField;
import kbee.web.search.service.ParametricSearchSuggestionService;

/**
 * 
 * TODO VER QE EXISTE EN KBEE WEB
 *
 */
public class DocumentSearcherBean extends Panel implements SelectorPanel<Content>  {
	private static final long serialVersionUID = 1L;
	
	private IModel<RelationTemplate> model;
	private List<OnClickListener<Content>> listeners = new ArrayList<OnClickListener<Content>>();
	
	@SuppressWarnings("serial")
	public DocumentSearcherBean(String id, IModel<RelationTemplate> model) {
		super(id);
		setTemplateModel(model);
		setOutputMarkupId(true);
		
		Panel searcher = new SearcherAdvancedPanel("searcher") {
			@Override
			public void onSelect(AjaxRequestTarget target, Content content) {
				DocumentSearcherBean.this.onSelect(target, content);
			}
			@Override
			@SuppressWarnings("unchecked")
			protected void onClose(AjaxRequestTarget target) {
				setVisible(false);
				((AdvancedSearchField<Content>) DocumentSearcherBean.this.get("content")).setOpen(false);
				target.add(DocumentSearcherBean.this);
			}
		};
		
		add(searcher);
		
		
		add(new AdvancedSearchField<Content>("content",  new PropertyModel<Content>(this, "content")) {
			/**
			 * advanced search 
			 */
			@Override
			public void onOpenAdvancedSearch(AjaxRequestTarget target) {
				searcher.setVisible(!searcher.isVisible());
				target.add(DocumentSearcherBean.this);
			}
			
			public void onUpdate(AjaxRequestTarget target) {
				DocumentSearcherBean.this.onSelect(target, getValue());
			}
			@Override
			public IModel<String> getLabel() {
				return new Model<String>(getTemplateModel().getObject().getTargetLabel());
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				if (!getTemplateModel().getObject().getTargetTemplates().isEmpty()) {
					String templates = "";
					for (ContentTemplate template : getTemplateModel().getObject().getTargetTemplates()) {
						if (!templates.equals("")) templates +=", ";
						templates += template.getId();
					}
					templates = "[" + templates + "]";
					parameters.put("template", templates);
				}
				parameters.put("type", "[idoc, text]");
				List<Suggestion> suggestions = getDomain().getService(ParametricSearchSuggestionService.class)
						.getSuggestions(pattern, parameters);
				return suggestions;
			}
		}); 
	}
	
	public void onSelect(AjaxRequestTarget target, Content content) {
		for (OnClickListener<Content> listener : listeners) {
			listener.onClick(target, content);
		}
	}
	
	public void setTemplateModel(IModel<RelationTemplate> model) {
		this.model = model;
	}
	
	public IModel<RelationTemplate>  getTemplateModel() {
		return model;
	}
	public void setContent(Content content) {
		
	}
	
	public Content getContent() {
		return null;
	}

	protected void onClose(AjaxRequestTarget target) {
	}

	public void addListener(OnClickListener<Content> listener) {
		this.listeners.add(listener);
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
