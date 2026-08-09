package kbee.web.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.kbee.template.KbeeTemplateModelInfo;
import com.novamens.text.TemplateModelInfo;
import com.novamens.text.TemplateModelInfo.ModelType;

@SuppressWarnings("serial")
public class ModelHelpPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private boolean globals = false;
	private TemplateModelInfo model;
	private List<TemplateModelInfo> path = new ArrayList<TemplateModelInfo>();

	public class PathFragment extends Fragment {
		public PathFragment() {
			super("path", "path-fragment", ModelHelpPanel.this);
			
			add(new ListView<TemplateModelInfo>("path", () -> getBreadCrumb()) {
				public void populateItem(ListItem<TemplateModelInfo> item) {
					TemplateModelInfo model = item.getModelObject();
					AjaxLink<Void> link = new AjaxLink<Void>("link") {
						public void onClick(AjaxRequestTarget target) {
							showPath(target, item.getIndex());
						}
					};
					link.add(new Label("model", model.getName()));
					item.add(link);
				}
			});
		}	
	}
	
	public class ContentStructureFragment extends Fragment {
		private List<TemplateModelInfo> models;
		public ContentStructureFragment(TemplateModelInfo model) {
			super("model-info", "content-info-fragment", ModelHelpPanel.this);
			models = KbeeTemplateModelInfo.getContentModels(model.getTemplate());
			add(new ListView<TemplateModelInfo>("model", () -> getModels()) {
				public void populateItem(ListItem<TemplateModelInfo> item) {
					item.add(new Label("template", item.getModelObject().getName()));
					item.addOrReplace(new StructureFragment(item.getModelObject()));
				}
			});
		}
		public List<TemplateModelInfo> getModels() {
			return models;
		}
	}
	
	public class StructureFragment extends Fragment {
		private TemplateModelInfo model;
		public StructureFragment(TemplateModelInfo model) {
			this("model-info", model);
		}	
		public StructureFragment(String id, TemplateModelInfo model) {
			super(id, "model-info-fragment", ModelHelpPanel.this);
			
			this.model = model;
			
			add(new ListView<TemplateModelInfo>("element", () -> getElements()) {
				public void populateItem(ListItem<TemplateModelInfo> item) {
					TemplateModelInfo model = item.getModelObject();
					AjaxLink<Void> link = new AjaxLink<Void>("link") {
						@Override
						public void onClick(AjaxRequestTarget target) {
							showModel(target, KbeeTemplateModelInfo.CreateFrom(model));
						}
						@Override
						public boolean isEnabled() {
							return !model.getType().isCanonical();
						}
					};
					link.add(new Label("name", () -> model.getName()));
					item.add(link);
					item.add(new Label("type", () -> getType(model)));
					item.add(new Label("path", () -> getMacro(model)));
					item.add(new Label("description", () -> model.getDescription()) );
					item.add(new ExternalLink("modifiers", model.getType().getModifiers()) {
						public boolean isVisible() {
							return model.getType().getModifiers()!=null;
						}
					});
				}
			});
		}	
		public TemplateModelInfo getModel() {
			return model;
		}
		public List<TemplateModelInfo> getElements() {
			List<TemplateModelInfo> elements = new ArrayList<TemplateModelInfo>();
			elements.addAll(getModel().getElements());
			Collections.sort(elements, new Comparator<TemplateModelInfo>() {
				@Override
				public int compare(TemplateModelInfo a, TemplateModelInfo b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});	
			return elements;
		}
		public String getType(TemplateModelInfo model) {
			String type = model.getType().getLabel();
			if (ModelType.VALUE.equals(model.getType()))
				type = model.getDataSet();
			if (model.getMultiplicity()!=null && model.getMultiplicity().isMultiple())
				type += "[]";
			return type;	
		}
		public String getMacro(TemplateModelInfo model) {
			String macro = "", path="";
			if (isRelative())
			for (int i=1; i<getBreadCrumb().size(); i++) {
				if (!"".equals(path)) path+=".";
				path += getBreadCrumb().get(i).getName();
			}
			if (!"".equals(path)) path+=".";
			path += model.getName();
			if (model.getMultiplicity()!=null &&model.getMultiplicity().isMultiple()) {
				macro =" <#list " + path + " as item>-${item}</#list>";
			}
			else {
				macro += "${"+ path+"}";
			}
			return macro;
		}
		protected boolean isRelative() {
			return true;
		}
	}
	
	public ModelHelpPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	public TemplateModelInfo getModel() {
		return model;
	}

	public void setModel(TemplateModelInfo model) {
		this.model = model;
		path.clear();
		path.add(model);
	}
	
	public List<TemplateModelInfo> getBreadCrumb() {
		return path;
	}
	
	public void showModel(AjaxRequestTarget target, TemplateModelInfo model) {
		path.add(model);
		addOrReplace(getPanel(model));
		target.add(this);
	}
	
	public void showPath(AjaxRequestTarget target, int level) {
		List<TemplateModelInfo> newpath = new ArrayList<TemplateModelInfo>();
		for (int i=0; i<=level; i++) {
			newpath.add(path.get(i));
		}
		path = newpath;
		addOrReplace(new PathFragment());
		addOrReplace(getPanel(path.get(level)));
		target.add(this);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add (new AjaxLink<Void>("globals-link") {
			public void onClick(AjaxRequestTarget target) {
				globals = !globals;
				target.add(ModelHelpPanel.this);
			};
		});
		addOrReplace(getGlobalsPanel());
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		addOrReplace(new Label("description", () -> model.getDescription()));
		if (get("model-info")==null || path.size()==1) {
			addOrReplace(new PathFragment());
			addOrReplace(new StructureFragment(getModel()));
		}
	}
	
	private WebMarkupContainer getGlobalsPanel() {
		return new StructureFragment("global-info", KbeeTemplateModelInfo.GetGlobals()) {
			@Override
			public boolean isVisible() {
				return globals;
			}
			@Override
			public boolean isRelative() {
				return false;
			}
		};
	}
	
	private WebMarkupContainer getPanel(TemplateModelInfo model) {
		if (ModelType.CONTENT.equals(model.getType()))
			return new ContentStructureFragment(model);
		else
			return new StructureFragment(model);
	}
}
