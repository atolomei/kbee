package kbee.web.eform;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.form.KbeeEResources;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field.Width;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceModel;
import kbee.web.resource.ResourceViewPanel;

@SuppressWarnings("serial")
public class EResourcesStatelessViewer extends EFieldPanel<KbeeEResources>  {
	private static final long serialVersionUID = 1L;

//	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EResourcesStatelessViewer.class.getName());
	
	private List<IModel<Resource>> resources = new ArrayList<IModel<Resource>>();
	private ViewMode viewmode = ViewMode.ICON;
	private IModel<Content> contentModel;
	
	public class ControlFragment extends Fragment {
		public ControlFragment(String id) {
			super(id, "control-fragment", EResourcesStatelessViewer.this);
			setOutputMarkupId(true);
			addView();
		}
		private void addView() {
			
			Label s=new Label("subtitle", new Model<String>(getField().getSublabel()));
			s.setEscapeModelStrings(false);
			s.setVisible(getField().getSublabel()!=null);
			add(s);
			
			WebMarkupContainer view = new WebMarkupContainer("resources-view");
			view.setOutputMarkupId(true);
			view.add(new ListView<IModel<Resource>>("resources-list", new PropertyModel<List<IModel<Resource>>>(EResourcesStatelessViewer.this, "resources")) {
				protected void populateItem(ListItem<IModel<Resource>> item) {
					item.add(new ResourceView(item.getModelObject()));
					item.add(new AttributeModifier("class", getViewMode().getElementCss())); // grid2,3,4
				}
			});
			view.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return getViewMode().getListCss();
				}
			})); 
			addOrReplace(view);
		}
	}
	
	public class ResourceView extends Fragment {
		public ResourceView(IModel<Resource> model) {
			super("resource-view", "resource-view-fragment", EResourcesStatelessViewer.this);
			add(new ResourceViewPanel<Content>("resource-view", model, getContentModel()) {
				@Override
				public ViewMode getViewMode() {
					return EResourcesStatelessViewer.this.getViewMode();
				}
				@Override
				public boolean isStateLess() {
					return true;
				}
				@Override
				protected String format(OffsetDateTime time) {
					Locale locale = getSessionUser()!=null ? getSessionUser().getLocale() : Locale.getDefault();
					return ServiceLocator.getService(DateTimeService.class).getDateDisplayString(time, locale);
				}
			});
		}
	}	
	
	public EResourcesStatelessViewer(String id, KbeeEResources field, IModel<EFormData> data) {
		super(id, field, data);
		setOutputMarkupId(true);
	}
	
	public ViewMode getViewMode() {
		return this.viewmode;
	}

	public void setViewMode(ViewMode mode) {
		this.viewmode = mode;
	}
	
	public Disposition getDisposition() {
		return Disposition.VERTICAL;
	}
	
	public void setContent(IModel<Content> model) {
		this.contentModel = model;
	}
	
	public void setContent(Content content) {
		this.contentModel = new ObjectModel<Content>(content);
	}
	
	public IModel<Content> getContentModel() {
		return contentModel;
	}
	
	public List<IModel<Resource>> getResources() {
		return resources;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setResources();
		
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
		
		getContainer().get("label").add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-1 control-label" : "control-label";
			}
		}));
		
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			layout.add(new AttributeModifier("class", Width.W10.getCss()));
			getContainer().get("control").setVisible(false);
		}
		else {
			layout.setVisible(false);
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (contentModel!=null)
			contentModel.detach();
	}
	
	private void setResources() {
		List<?> resources = (List<?>)getData().getData(getField());
		setContent(((EFormContentData)getData()).getContent());
		this.resources.clear();
		if (resources!=null) {
			for (Object resource : resources) {
				if (resource instanceof Resource) {
					this.resources.add(new ResourceModel((Resource)resource));
				}
			}
		}
	}
}