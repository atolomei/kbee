package kbee.web.eform;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.RelationAdded;
import com.novamens.content.form.RelationRemoved;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
import com.novamens.kbee.content.form.KbeeERelation;
import com.novamens.kbee.content.form.KbeeERelationFieldModel;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.ProxyModel;

import kbee.web.relation.RelationSearcher;
import kbee.web.resource.ResourceLink;

@SuppressWarnings("serial")
public class ERelationPanel extends EFieldPanel<KbeeERelation> {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<Content>> values;
	
	public class ControlFragment extends Fragment {
		boolean helpvisible = false;
		public ControlFragment(String id) {
			super(id, "control-fragment", ERelationPanel.this);
			
			// TODO VER SUBTITLE
			Label s=new Label("subtitle", 
				getField().getSublabel()!=null 
					? getField().getSublabel()
					: "");
			s.setEscapeModelStrings(false);
			s.setVisible(getField().getSublabel()!=null);
			add(s);
						
			add(new ListView<IModel<Content>>("relation", () -> getValues()) {
				public void populateItem(ListItem<IModel<Content>> item) {
					if (isResourceView()) {
						item.add(new RelatedResourceView("view", item.getModelObject()));
					}
					else {
						item.add(new RelatedView("view", item.getModelObject()));
					}	
				}
			});
			
			if (!isViewer()) {
				add(new RelationSearcher("searcher", getTemplateModel()) {
					@Override
					public void onSelect(AjaxRequestTarget target, Content content) {
						addValue(content);
						target.add(getContainer());
						fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
					}
					@Override
					public boolean isVisible() {
						return !isReadOnly() && getRelation().getMultiplicity()!=null &&
							(getRelation().getMultiplicity().isMultiple() || getValues().isEmpty()) ; 
					}
				});
			}
			else {
				add(new InvisiblePanel("searcher"));
			}
			
			Label helplabel = new Label("help", () -> getHelpText()) {
				public boolean isVisible() {
					return helpvisible;
				}
			};
			helplabel.setVisible(false);
			AjaxLink<?> helplink = new AjaxLink<Void>("help-link") {
				public void onClick(AjaxRequestTarget target) {
					helpvisible = !helpvisible;
					target.add(getContainer());
				}
				public boolean isVisible() {
					return getHelpText()!=null;
				}
			};
			add(helplink);
			add(helplabel);
		}
		public boolean isResourceView() {
			int displaymode = isReverse() ?
				getRelation().getReverseDisplayMode() :
				getRelation().getTargetDisplayMode();
			return displaymode == RelationTemplate.ResourceDispalyMode;
		}
	}	
	
	public class RelatedView extends Fragment {
		private IModel<Content> model;
		private boolean isexpanded = false;
		public RelatedView(String id, IModel<Content> model) {
			this(id, "related-view-fragment", model);
		}
		protected RelatedView(String id, String markupid, IModel<Content> model) {
			super(id, markupid, ERelationPanel.this);
			setOutputMarkupId(true);
			this.model = model;
			Link<?> link = new Link<Void>("link")  {
				public void onClick() {
					setResponsePage(new RedirectPage(getUrl(getContent())));
				}
			};
			link.add(new Label("title", getContent().getDisplayName()));
			add(new Label("subtitle", getSubtitle()));
			((Label)get("subtitle")).setEscapeModelStrings(false);
			add(link);
			AjaxLink<?> removeLink = new AjaxLink<Void>("remove-link")  {
				@Override
				public void onClick(AjaxRequestTarget target) {
					removeValue(model.getObject());
					target.add(getContainer());
					fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
				}
				@Override
				public boolean isVisible() {
					return !isReadOnly(); 
				}
			};
			add(removeLink);
			add(new AjaxLink<Void>("expander") {
				public void onClick(AjaxRequestTarget target) {
					expand(target);
				}
			});
			((MarkupContainer)get("expander")).add(new WebMarkupContainer("icon"));
			get("expander:icon").add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return isexpanded ? "far fa-angle-down" : "far fa-angle-up";
				}
			}));
			WebMarkupContainer expanded = new WebMarkupContainer("expanded-view") {
				public boolean isVisible() {
					return isexpanded;
				}
			};
			if (isShared()) {
				expanded.add(new EFormSharedViewer("view", getFormData(getInlineForm())));
			}
			else {
				expanded.add(new EFormViewer("view", getFormData(getInlineForm())));
			}
			add(expanded);
		}
		
		public void expand(AjaxRequestTarget target) {
			isexpanded = !isexpanded;
			target.add(this);
		}
		public Content getContent() {
			return model.getObject();
		}
		public IModel<Content> getModel() {
			return model;
		}
		public String getSubtitle() {
			return getContent().getService(ContentService.class).getPortalSubtitle();
		}
		private IModel<EFormData> getFormData(EForm eform) {
			return new EFormDataModel(getContent().getFormData(eform));
		}
		
		private EForm getInlineForm() {
			for (EForm form : getContent().getContentTemplate().getForms()) {
				if (form.isUseInline()) {
					KbeeTaskForm wrapper = new KbeeTaskForm();
					wrapper.setForm(form);
					wrapper.setReadOnly(true);
					wrapper.setEnabled(false);
					return wrapper;
				}
			}
			KbeeTaskForm wrapper = new KbeeTaskForm();
			wrapper.setForm(getDefaultForm(getContent()));
			wrapper.setReadOnly(true);
			wrapper.setEnabled(false);
			return wrapper;
		}
	}
	
	public class RelatedResourceView extends RelatedView {
		public RelatedResourceView(String id, IModel<Content> model) {
			super(id, "related-resource-view-fragment", model);
			add(new AttributeModifier("class", "media icon"));
			ResourceLink<Content> link = new ResourceLink<Content>("resource-link", getResourceModel(), getModel()) {
				@Override
				public boolean isVisible() {
					return getResource()!=null;
				}
			};
			link.add(getIcon());
			add(link);
		}	
		public IModel<Resource> getResourceModel() {
			Resource resource = getResource();
			if (resource!=null) {
				return new ObjectModel<Resource>(resource);
			}
			return null;
		}
		public Resource getResource() {
			if (getContent()!=null) {
				Content content = (new ProxyModel<Content>(getContent())).getObject();
				if (content instanceof ResourceContainer) {
					List<Resource> resources = ((ResourceContainer)content).getResources();
					if (!resources.isEmpty()) {
						return resources.get(0);
					}
				}
			}
			return null;
		}
		protected WebMarkupContainer getIcon() {
			WebMarkupContainer icon = new WebMarkupContainer("glyphicon");
			Resource resource = getResource();
			if (resource!=null) {
				icon.add(new AttributeModifier("class", resource.getGlyphIcon()));
			}
			else {
				icon.setVisible(false);
			}
			return icon;
		}
	}
	
	public ERelationPanel(KbeeERelation field, IModel<EFormData> data) {
		this("component", field, data);
	}
	
	public ERelationPanel(String id, KbeeERelation field, IModel<EFormData> data) {
		super(id, field, data);
	}  
	
	public List<IModel<Content>> getValues() {
		return values;
	}
	
	public boolean isReadOnly() {
		return getField().isReadOnly() || 
			!getData().getForm().isEnabled() || 
			isViewer() || 
			isReverse();
	}
	
	public boolean isViewer() {
		return false;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setValues();
		
		WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
		layout.add(new ControlFragment("control"));
		getContainer().add(new ControlFragment("control"));
		getContainer().add(layout);
		
		getContainer().add(new Label("label", new Model<String>() {
			public String getObject() {
				return getField().getLabel()!=null ?
					getField().getLabel() :
					"";	
			}
		}));
		
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			getContainer().get("control").setVisible(false);
		}
		else {
			layout.setVisible(false);
		}
	} 
	
	public IModel<RelationTemplate> getTemplateModel() {
		RelationTemplate template = ((KbeeERelationFieldModel)(getField().getModel())).getRelation();
		return new ObjectModel<RelationTemplate>(template);
	}
	
	public Content getContent() {
		return ((EFormContentData)getData()).getContent();
	}
	
	public RelationTemplate getRelation() {
		return getTemplateModel().getObject();
	}
	
	public boolean isReverse() {
		return !getRelation().getSourceTemplate().equals(getContent().getContentTemplate());
	}
	
	public String getLabel() {
		return getField().getLabel()!=null ?
			getField().getLabel() :
			"";	
	}
	
	public boolean contains(Content content) {
		return false;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (values!=null)
		for (IModel<Content> model : values) {
			model.detach();
		}
	}
	
	protected void setValues(List<?> values) {
		if (this.values==null)
		this.values = new ArrayList<IModel<Content>>();
		this.values.clear();
		if (this.values!=null && values!=null) {
			for (Object content : values) {
				if (content instanceof Content) {
					this.values.add(new ObjectModel<Content>((Content)content));
				}
			}
		}		
		getData().setData(getField(), getValues());
	}

	
	
	
	
	protected boolean isShared() {
		return false;
	}
	
	protected String getUrl(Content content) {
		return content.getService(UrlService.class).getUrl();
	}
	
	protected EForm getDefaultForm(Content content) {
		return new KbeeInlineForm(content);
	}
	
	protected void addValue(Content content) {
		if (values==null)
		values = new ArrayList<IModel<Content>>();
		if (content!=null && !contains(content)) {
			IModel<Content> model = new ObjectModel<Content>(content);
			setUpdatedField(new RelationAdded(getData().getForm(), getLabel(), content));
			values.add(model);
		}
		getData().setData(getField(), getValues());
	}
	
	protected void removeValue(Content content) {
		if (values==null)
		values = new ArrayList<IModel<Content>>();
		int index = 0;
		for (IModel<Content> model : values) {
			if (model.getObject().equals(content)) {
				values.remove(index);
				getData().setData(getField(), getValues());
				setUpdatedField(new RelationRemoved(getData().getForm(), getLabel(), content));
				break;
			}
			else {
				index++;
			}
		}
	}
	
	protected void setValues() {
		List<?> contents = (List<?>)getData().getData(getField());
		if (values==null)
		values = new ArrayList<>();
		this.values.clear();
		if (values!=null && contents!=null) {
			for (Object object : contents) {
				if (object instanceof Content) {
					Content content = (Content)object;
					if (!isReverse() || content.isHeadVersion()) {
						this.values.add(new ObjectModel<Content>(content));
					}
				}
			}
		}
	}
}