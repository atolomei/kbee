package com.novamens.kbee.wicket.markup.html.console.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Filter;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.service.Cube;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.HierarchicalFacet;
import com.novamens.kbee.content.multidimensional.RangeMember;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Button;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.ListDataProvider;
import com.novamens.wicket.markup.html.repeater.util.MemberModel;

/**
 * 
 * <p>Panel de parámetros del Panel de filtros 
 * del panel {@link FiltersPanelInternalContainerPanel} de la {@link Console}</p>
 * 
 * {@link ParametersPanelToolbarPanel}
 * 
 * 
 */
@SuppressWarnings("serial")
public class ParametersPanel extends KBPanel {
																									
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ParametersPanel.class.getName());

	/** Parameters de la Query */
	private Map<String, Object> parameters 		= null;
	
	private List<MemberModel> 		members 	= new ArrayList<MemberModel>();
	private List<List<MemberModel>> ormembers 	= new ArrayList<List<MemberModel>>();

	/** Si se habilitan acciones sobre los filtros aplicados */
	private boolean actionsEnabled;
	private List<Filter> filters = null;
	private String consoleDisplayName;

	/** 
	 * XFiltersPanel
	 */
	private class XFiltersPanel extends Fragment {
		
		public XFiltersPanel(String id) {
			super(id, "filtersFragment", ParametersPanel.this);
			
			setOutputMarkupId(true);
			
			IModel<List<Filter>> model = new PropertyModel<List<Filter>>(ParametersPanel.this, "filters");
			model.getObject();

			ListView<Filter> valueFiltersView = new ListView<Filter>("values", model) {
				@Override
				protected void populateItem(ListItem<Filter> item) {
					final Filter filter = item.getModelObject();
					AjaxLink<Void> link = new AjaxLink<Void>("deleteLink") {
						public void onClick(AjaxRequestTarget target) {
							removeFilter(filter.getName());
							onUpdate(target);
						}
						@Override
	 					public boolean isEnabled() {
							return ParametersPanel.this.actionsEnabled();
						}
					};
					link.add(new Label("value", filter.getDisplayValue()));
					if (item.getIndex()>0)
						item.add(new AttributeModifier("class", "filter"));
					else {
						if ((getParameter("iql")!=null) || (getParameter("text")!=null))
							item.add(new AttributeModifier("class", "filter"));
						else
							item.add(new AttributeModifier("class", "filter first"));
					}
					item.add(link);
				}
			};
			add(valueFiltersView);
			
			IDataProvider<String> facets = new ListDataProvider<String>() {
				public List<String> getList() {
					return getFacets();
				}
			};

			DataView<String> memberFiltersView = new DataView<String>("members", facets) {
				@Override
				protected void populateItem(Item<String> item){
					final String facet = item.getModelObject();
					
					if (facet!=null) {
						List<MemberModel> members = getMembers(facet);
						MemberModel leafMember = members.remove(members.size()-1);
						ListView<MemberModel> parentsMembersView = new ListView<MemberModel>("parents", members) {
							@Override
							protected void populateItem(ListItem<MemberModel> item){
								final MemberModel parent = item.getModelObject();
								AjaxLink<?> parentLink = new AjaxLink<Void>("parentLink") {
									public void onClick(AjaxRequestTarget target) {
										removeMember(parent);
										onUpdate(target);
									}
									
									@Override
									public boolean isEnabled() {
										return ParametersPanel.this.actionsEnabled();
									}
								};
								parentLink.add(new Label("value", parent.getDisplayName()));
								item.add(parentLink);
							};
						};
						item.add(parentsMembersView);
						
						WorkingIndicatorAjaxLinkV5<Void> deletelink = new WorkingIndicatorAjaxLinkV5<Void>("deleteLink") {
							public void onClick(AjaxRequestTarget target) {
								removeFacet(facet);
								onUpdate(target);
							}
							@Override
							public boolean isEnabled() {
								return ParametersPanel.this.actionsEnabled();
							}
						};
						
						deletelink.add(new Label("value", leafMember.getDisplayName()));
						
						String title = getTitle(leafMember.getObject());
						if (!"".equals(title)) {
							deletelink.add(new AttributeModifier("title", title));
						}
						
						
						item.add(deletelink);
					}
					else {
						logger.error("Facet is null");
							item.setVisible(false);
					}
					
				} 
			};
			add(memberFiltersView);
			
			IDataProvider<String> orfacets = new ListDataProvider<String>() {
				public List<String> getList() {
					return getOrFacets();
				}
			};
			
			DataView<String> orMemberFiltersView = new DataView<String>("ormembers", orfacets) {
				@Override
				protected void populateItem(Item<String> item){
					final String facet = item.getModelObject();
					List<MemberModel> members = getOrMembers(facet);
					final int memberscount = members.size();
						
					ListView<MemberModel> membersView = new ListView<MemberModel>("members", members) {
						@Override
						protected void populateItem(ListItem<MemberModel> item){
							MemberModel member = item.getModelObject();
							item.add(new Label("value", member.getDisplayName()));
							item.add((new Label("or", "|")).setVisible(item.getIndex()<memberscount-1));
						};
					};
					
					WorkingIndicatorAjaxLinkV5<Void> deletelink = new WorkingIndicatorAjaxLinkV5<Void>("deleteLink") {
						public void onClick(AjaxRequestTarget target) {
							removeFacet(facet);
							onUpdate(target);
						}
						@Override
						public boolean isEnabled() {
							return ParametersPanel.this.actionsEnabled();
						}
					};
					deletelink.add(membersView);
					item.add(deletelink);
				}
			};
			add(orMemberFiltersView);
		}
		
		@Override
		public boolean isVisible() {
			return !getFilters().isEmpty() || !getFacets().isEmpty() ||	!getOrFacets().isEmpty();
		}
	};

	
	/**
	 * 
	 * 
	 * 
	 * @param parameters
	 * @param consoleDName
	 * 
	 * 
	 * 
	 */
	public ParametersPanel(Map<String, Object> parameters, String consoleDName) {
		this("parameters", parameters, true, consoleDName);
		
	}

	public ParametersPanel(String id, Map<String, Object> parameters, String consoleDName) {
		this(id, parameters, true, consoleDName);
	}

	
	/** 
	 * @param id
	 * 
	 * @param parameters
	 * 
	 * @param actionsEnabled Permitir quitar filtros o no. Nota: 
	 * desde el panel de visualizacion que se muestra cuando
	 * el panel de filtros esta oculto, no se puede quitar filtros por ahora.
	 * 
	 */
	public ParametersPanel(String id, Map<String, Object> parameters, boolean actionsEnabled, String consoleDName) {
		super(id);
		setParameters(parameters);
		setOutputMarkupId(true);
		setConsoleDisplayName(consoleDName);
		this.actionsEnabled = actionsEnabled;
	}

	 

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer parameters_container = new WebMarkupContainer("parameters-container") {
			@Override
			public boolean isVisible() {
				return !isEmpty();
			}
		};

		parameters_container.setOutputMarkupId(true);
		add(parameters_container);
		parameters_container.add(getToolbar());

		
		/** 
		 * Texto 
		 * 
		 */
		WebMarkupContainer textPanel = new WebMarkupContainer("text-panel") {
			@Override
			public boolean isVisible() {
				return getParameter("text")!=null && getParameter("text") instanceof String;
			}
		};

		AjaxLink<?> textlink = new AjaxLink<Void>("text-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setParameter("text", null);
				onUpdate(target);
			}
		};
		
		textPanel.add(textlink);
		
		textlink.add(new Label("text", new Model<String>() {
			@Override
			public String getObject() {
				return (String)getParameter("text");
			}
		}));
		parameters_container.add(textPanel);
		

		/**
		 * IQL 
		 */
		WebMarkupContainer iqlPanel = new WebMarkupContainer("iqlPanel") {
			@Override
			public boolean isVisible() {
				return getParameter("iql")!=null && isIQLVisible();
			}
		};
		
		iqlPanel.add( new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				if (getParameter("iql")!=null)										
					return "first";
				else
					return "";
			}
		}));
		
		
		AjaxLink<Void> iqllink = new AjaxLink<Void>("deleteLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setParameter("iql", null);
				onUpdate(target);
			}
			@Override
			public boolean isEnabled() {
				return ParametersPanel.this.actionsEnabled();
			}
		};
		
		iqllink.add(new Label("iql", new Model<String>() {
			@Override
			public String getObject() {
				return (String)getParameter("iql");
			}
		}));
		
		iqlPanel.add(iqllink);
		parameters_container.add(iqlPanel);


		parameters_container.add(new XFiltersPanel("ParmametersPanelAppliedFiltersPanel"));
		
	}
	
	public String getConsoleDisplayName() {
		return this.consoleDisplayName;
	}

	public void setConsoleDisplayName(String consoleName) {
		this.consoleDisplayName=consoleName;
	}

	@SuppressWarnings("unchecked")
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
		this.members.removeAll(this.members);
		this.ormembers.clear();
		setMembers((List<String>)parameters.get("members"));
	}

	public void setParameter(String key, Object value) {
		this.parameters.put(key, value);
	}

	public Object getParameter(String name) {
		return parameters==null ? null : parameters.get(name);
	}
	
	public String getTitle() {
		StringBuilder  title = new StringBuilder();
		
		for (String facet : getFacets()) {
			for (MemberModel member : getMembers(facet)) {
				if (title.length()>0)
					title.append(", ");
				title.append(member.getDisplayName());
			}
		}
		
		for (String facet : getOrFacets()) {
			if (title.length()>0)
				title.append(", ");
			for (MemberModel member : getOrMembers(facet)) {
				if (title.length()>0)
					title.append(" or ");
				title.append(member.getDisplayName());
			}
		}
		return title.toString();
	}


	public void onSave(AjaxRequestTarget targt, String title) {
	}

	
	public Map<String, Object> getParameters() {
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.putAll(this.parameters);
		parameters.put("members", getMembers());
		return parameters;
	}

	public void setMember(MemberModel member) {
		boolean found = true;
		while (found) {
			found = false;
			for (MemberModel m : members) {
				if (m.getObject()!=null)
				if (isDescendant(m.getObject(), member.getObject()) || isDescendant(member.getObject(), m.getObject())) {
					members.remove(m);
					found = true;
					break;
				}
			}
		}
		found = true;
		while (found) {
			found = false;
			for (List<MemberModel> list : ormembers) {
				for (MemberModel m : list) {
					if (m.getObject()!=null)
					if (isDescendant(m.getObject(), member.getObject()) || isDescendant(member.getObject(), m.getObject())) {
						list.remove(m);
						if (list.isEmpty()) ormembers.remove(list);
						found = true;
						break;
					}
				}
				if (found) break;
			}
		}	
		members.add(member);
	}

	public void setOrMembers(List<MemberModel> members) {
		for (MemberModel model : members) {
			boolean found = true;
			while (found) {
				found = false;
				for (MemberModel m : this.members) {
					if (m.getObject()!=null)
					if (isDescendant(m.getObject(), model.getObject()) || isDescendant(model.getObject(), m.getObject())) {
						this.members.remove(m);
						found = true;
						break;
					}
				}
			}	
		}
 		List<MemberModel> members2 = new ArrayList<MemberModel>();
		members2.addAll(members);
		ormembers.add(members2);
	}
	
	public List<String> getMembers() {
		List<String> memberspaths = new ArrayList<String>();
		for (MemberModel member : this.members) {
			if (member.getObject()!=null)
			memberspaths.add(member.getObject().getPath());
		} 
		for (List<MemberModel> members : this.ormembers) {
			StringBuilder path  = new StringBuilder();
			for (MemberModel member : members) {
				if (path.length()>0) 
					path.append("|");
				path.append(member.getObject().getPath());
			}
			memberspaths.add(path.toString());
		}
		return memberspaths;
	}

	public boolean contains(MemberModel member) {
		for (MemberModel m : this.members) {
			if (m.getObject().getPath().equals(member.getObject().getPath())) {
				return true;
			}
		}	
		return false;
	}

	public void onUpdate(AjaxRequestTarget target) {
		
	}
	
	public void onMemberRemove(MemberModel member) {
		
	}
	
	public void onFilterRemove(String filter, String value)	{
		
	}
	
	public void onParametersEmpty(AjaxRequestTarget target)	{
		
	}
	
	public void clearAll(AjaxRequestTarget target) {
		this.filters = null;
		this.members.clear(); 
		this.ormembers.clear();
		this.parameters.remove("iql");
		this.parameters.remove("text");
		Map<String, Object> remove_parameters = new HashMap<String, Object>(); 
		/** it is not possible to remove value while iterating the list */
		for (String name : parameters.keySet()) {
			Object value = parameters.get(name);
			if (value instanceof Filter) 
				remove_parameters.put(name, value);
		}
		for (String key: remove_parameters.keySet()) 
			parameters.remove(key);
		onUpdate(target);
		fireScanAll(new FilterSelectorClearAllEvent(target));
	}
	
	public void clearAll() {
		this.filters = null;
		this.members.clear(); 
		this.ormembers.clear();
		this.parameters.remove("iql");
		this.parameters.remove("text");
		Map<String, Object> remove_parameters = new HashMap<String, Object>(); 
		/** it is not possible to remove value while iterating the list */
		for (String name : parameters.keySet()) {
			Object value = parameters.get(name);
			if (value instanceof Filter) 
				remove_parameters.put(name, value);
		}
		for (String key: remove_parameters.keySet()) 
			parameters.remove(key);
	}
	
	public void refresh(AjaxRequestTarget target) {
		this.filters = null;
		getFilters();
	}
	
	public boolean isEmpty() {
		//getFilters();
		return ((this.members==null || this.members.size()==0) 	&& 
			(this.ormembers==null || this.ormembers.size()==0) 	&&
			(this.parameters.get("iql")==null) 					&&
			(this.parameters.get("text")==null) 				&&
			(this.filters==null || this.filters.isEmpty()));
	}
	

	protected void setMembers(List<String> members) {
		if (members == null) 
			return;
		for(String path : members) {
			if (path.contains("|")) {
				String orpaths[] = path.split("\\|");
				List<MemberModel> ormember = new ArrayList<MemberModel>();
				for (String orpath : orpaths) {
					MemberModel member = new MemberModel(getCube().getMember(orpath));
					ormember.add(member);
				}
				this.ormembers.add(ormember);
			}
			else {
				this.members.add(new MemberModel(getCube().getMember(path)));
			}
		}
	}

	protected List<MemberModel> getMembers(String facet) {
		List<MemberModel> members = new ArrayList<MemberModel>();
		
		if (facet==null) {
			logger.debug("Strange Facet is null");;
			return members;
		}
		
		for (MemberModel member : this.members) {
			try {
				if (facet.equals(member.getFacet())) {
					members.add(member);
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return members;
	}

	protected List<MemberModel> getOrMembers(String facet) {
		for (List<MemberModel> members : this.ormembers) {
			MemberModel member = members.get(0);
			if (facet.equals(member.getFacet())) {
				return members;
			}
		}
		return members;
	}
	
	protected List<String> getFacets() {
		List<String> facets = new ArrayList<String>();
		for (MemberModel member : this.members) {
			if (member.getObject()!=null && (facets.indexOf(member.getFacet())<0) && isVisibleFilterByName(member.getFacet()))
				facets.add(member.getFacet());
		}
		return facets;
	}
	
	protected boolean isVisibleFilterByName(String facetname) {
		return true;
	}

	protected List<String> getOrFacets() {
		List<String> facets = new ArrayList<String>();
		for (List<MemberModel> members : this.ormembers) {
			for (MemberModel member : members) {
				if (facets.indexOf(member.getFacet())<0)
					facets.add(member.getFacet());
			}
		}
		return facets;
	}

	protected void removeFacet(String facet) {
		boolean remove = true;
		while (remove) {
			remove = false;
			for (MemberModel member : this.members) {
				if (facet.equals(member.getFacet())) {
					this.members.remove(member);
					onMemberRemove(member);
					remove = true;
					break;
				}
			}
			for (List<MemberModel> members : this.ormembers) {
				for (MemberModel member : members) {
					if (facet.equals(member.getFacet())) {
						this.ormembers.remove(members);
						onMemberRemove(member);
						remove = true;
						break;
					}
				}
				if (remove)
					break;
			}

		}
	}
	
	protected boolean actionsEnabled() {
		return actionsEnabled;
	}

	protected void removeMember(MemberModel memberToDelete) {
		for (MemberModel member : this.members) {
			if (memberToDelete.getObject().getPath().equals(member.getObject().getPath())) {
				this.members.remove(member);
				onMemberRemove(member);
				break;
			}
		}
	}
	
	public List<Filter> getFilters() {
		
		if (filters!=null)
			return filters;
		
		filters = new ArrayList<Filter>();
		
		if (parameters!=null)
			for (Object value: parameters.values()) {
				if (value instanceof Filter && isVisibleFilterByName(((Filter)value).getName())) 
					filters.add((Filter)value);
			}
		return filters;
	}
	
	public Cube getCube() {
		return getQueryIndex().getCube();
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	public boolean isIQLVisible() {
		return true;
	}

	protected void removeFilter(String name) {
		for (String filtername : this.parameters.keySet()) {
			if (parameters.get(filtername) instanceof Filter && name.equals(((Filter)parameters.get(filtername)).getName())) {
				Filter filter = (Filter)parameters.get(filtername);
				this.parameters.remove(filtername);
				filters=null;
				getFilters();
				onFilterRemove(filtername, filter.getDisplayValue());
				break;
			}
		}
	}

	protected ParametersPanelToolbarPanel getToolbar() {
		ParametersPanelToolbarPanel toolbar = new ParametersPanelToolbarPanel("toolbar") {
			@Override
			public boolean isSaveQuerySupported() {
				return ParametersPanel.this.isSaveQuerySupported();
			}
			@Override
			protected boolean isSaveQueryEnabled() {
				return !isEmpty();
			}
			@Override
			protected boolean isClearEnabled() {
				return !isEmpty();
			}
			@Override
			protected void onSaveQuery(AjaxRequestTarget target) {
				ParametersPanel.this.saveQuery(target, getTitle(), getParameters()); 
			}
			@Override
			protected void onSaveDashboardQuery(AjaxRequestTarget target) {
				ParametersPanel.this.saveDashboardQuery(target, getTitle(), getParameters()); 
			}
			@Override
			protected void onClearAll(AjaxRequestTarget target) {
				clearAll(target);				
			}
		};
		return toolbar;
	}

	protected boolean isSaveQuerySupported() {
		return true;
	}

	protected void saveQuery			(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {}
	protected void saveDashboardQuery	(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {}

	public void onSaveQuery(AjaxRequestTarget target) {
		((SaveQueryModal) ParametersPanel.this.get("parameters-container:modal")).open(target,  getTitle(), false, getParameters(), new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {}
		});
	}
	

	public void onSaveDashboardQuery(AjaxRequestTarget target) {
		((SaveQueryModal) ParametersPanel.this.get("parameters-container:modal")).open(target,  getTitle(), true,  getParameters(), new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {}
		});
	}

	protected String getTitle(Member member) {
		String title = "";
		if (member instanceof com.novamens.indexer.query.RangeMember) {
			title = member.getDisplayName();
		}
		else {
			String path[] = member.getPath().split("/");
			if (path.length>2) {
				for (int p=1;p<path.length-1;p++) {
					DataSetMember m  = getRepository(DataSetMember.class).findById(Long.valueOf(path[p]) );
					if (!"".equals(title)) title+= "/";
					title += m.getDisplayName();
				}
			}
		}
		return title;
	}
	
	protected Facet getFacet(String name) {
		for (Facet facet : getCube().getFacets()) {
			if (facet.getName().equals(name))
				return facet;
		}
		return null;
	}
	
	protected boolean isDescendant(Member m1, Member m2) {
		String p1 = m1.getPath().replace("*", "");
		String p2 = m2.getPath().replace("*", "");
		return p2.startsWith(p1);
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
