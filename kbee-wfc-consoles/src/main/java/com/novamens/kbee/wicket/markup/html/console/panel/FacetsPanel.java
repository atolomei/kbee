package com.novamens.kbee.wicket.markup.html.console.panel;


import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import org.apache.wicket.request.Response;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.multidimensional.FacetService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.RangeMember;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;

import com.novamens.solr.indexer.multidimensional.SolrMember;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.wicket.markup.html.form.DateField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.ListDataProvider;
import com.novamens.wicket.markup.html.repeater.util.MemberModel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;


/**
 * Filters of panel {@link FiltersPanel}
 * 
 * @see also {@link ParametersPanel}
 *  
 */
@SuppressWarnings("serial")
public class FacetsPanel extends KBPanel {
	private static final long serialVersionUID = 1L;
													
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FacetsPanel.class.getName());


	//private static final int defaultInitialFacetsOpen = 0;
	private static final int defaultMaxMembers = 12;

	
	//private Locale locale;
	
	private List<Facet> facets = null;
	private Map<String, FacetOptions> options =  new HashMap<String, FacetOptions>();;
	private Map<String, Long> sizes =  new HashMap<String, Long>();;
	private Searcher searcher;
	private String filter;

	private List<String> names= null;
	
	private List<MemberModel> selectedmembers = new ArrayList<MemberModel>();
	
	private class CheckBoxModel extends Model<Boolean> {
		private MemberModel member;
		public CheckBoxModel(MemberModel member) {
			this.member = member;
		}
		public Boolean getObject() {
			for (MemberModel model : selectedmembers)
				if (member.getObject().getPath().equals(model.getObject().getPath()))
					return true;
			return false;
		}
	}
	
	private class ToggleLink extends WebMarkupContainer {
		public ToggleLink(String id) {
			super(id);
		}
	}
	
	private class MemberContainer extends WebMarkupContainer implements IAjaxIndicatorAware {
		private AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender() {
			@Override
			public void afterRender(final Component component)	{
				Response r = component.getResponse();
				r.write("<i class=\"fal fa-sync fa-spin fa-fw spinning\" "+
					"id=\""+getMarkupId() + "\" " +
					"style=\"margin-left:10px; font-size: 12px; display:none;\"></i>");
			}
		};
		public MemberContainer(String id) {
			super(id);
			add(indicatorAppender);
		}
		public String getAjaxIndicatorMarkupId() {
			return indicatorAppender.getMarkupId();
		}
	}
	
	private class MembersFragment extends Fragment {
		public MembersFragment(String id, final Item<String> facetitem, IDataProvider<MemberModel> membersProvider) {
			super(id, "membersFragment", FacetsPanel.this);
			
			add(new DataView<MemberModel>("members", membersProvider) {
				@Override
				protected void populateItem(final Item<MemberModel> item){
					final MemberModel member = item.getModelObject();
					Facet facet = getFacet(facetitem.getModelObject());
					final int maxMembers = getMaxVisibleMembers(facet);
					item.setOutputMarkupId(true);
					
					WebMarkupContainer membercontainer = new MemberContainer("member-container");
					
					membercontainer.add(new AttributeModifier("class", new Model<String>() {
						public String getObject() {
							return item.getIndex()<maxMembers ? "checkbox" : "value";
						}
					}));
					
					membercontainer.add(new AjaxCheckBox("check", new CheckBoxModel(member)) {
						public void onUpdate(AjaxRequestTarget target) {
							if (getModelObject()) {
								for (MemberModel model : selectedmembers)
									if (member.getObject().getPath().equals(model.getObject().getPath())) {
										selectedmembers.remove(model);
										break;
									}
							}	
							else
								selectedmembers.add(member);
							MembersFragment.this.onUpdate(target);
						}
						public boolean isVisible() {
							return item.getIndex()<maxMembers;
						}
					});
					
					item.add(membercontainer);
						
					if (item.getIndex()<maxMembers) {
						membercontainer.add(new Label("memberName", (Serializable)member.getObject().getDisplayName()));
						if (!isFilter(member.getObject())) {
							membercontainer.get("memberName").add(new AjaxEventBehavior("click") {
								@Override
								protected void onEvent(AjaxRequestTarget target) {
									Facet facet = getFacet(facetitem.getModelObject());
									setMaxMembers(facet, defaultMaxMembers);
									sizes.remove(facet.getName());
									names = null;
									setFilter(facet, null);
									onMemberSelect(target, member);
								}
							});
						}
						membercontainer.add(new Label("memberCount", (Serializable)("("+String.valueOf(member.getObject().getCount())+")")));
						item.add(new NavigationFragment(item));
					}
					else {
						membercontainer.add(new Label("memberName", getLabel("more")));
						membercontainer.get("memberName").add(new AjaxEventBehavior("click") {
							@Override
							protected void onEvent(AjaxRequestTarget target) {
								Facet facet = getFacet(facetitem.getModelObject());
								setMaxMembers(facet, maxMembers+defaultMaxMembers);
 								target.add(facetitem);
							}
						});
						
						
						
						WebMarkupContainer navigation = new WebMarkupContainer("navigation");
						navigation.setOutputMarkupId(true);
						navigation.setVisible(false);
						item.add(navigation);
						

						membercontainer.add(new AttributeModifier("class",  new Model<String>("more")));
						membercontainer.add((new Label("memberCount", (Serializable)"")).setVisible(false));
						
						WebMarkupContainer navigationLink = new WebMarkupContainer("navigationlink-container");
						navigationLink.setVisible(false);
				
						membercontainer.add(navigationLink);
					}
				}
			});
			
			add(new Label("parent-label", new Model<String>() {
				public String getObject() {
					return getRootLabel(((ListDataProvider<MemberModel>)membersProvider).getData());
				}
			}));
		}
		
		public void onUpdate(AjaxRequestTarget target) {
			
		}
	}
	
		
	private class NavigationFragment extends Fragment {
		DataView<MemberModel> members;
		public NavigationFragment(final Item<MemberModel> item) {
			
			super("navigation", "navigationFragment", FacetsPanel.this);
			
			final MemberModel member = item.getModelObject();
		
			Facet facet = getFacet(member.getFacet());
					
			IDataProvider<MemberModel> membersProvider = new ListDataProvider<MemberModel>() {
				public List<MemberModel> getList() {
					return getNavigation(member);
				}
			};
			members = new DataView<MemberModel>("members", membersProvider) {
				@Override
				protected void populateItem(final Item<MemberModel> item){
					final MemberModel member = item.getModelObject();
					AjaxLink<?> memberLink = new WorkingAjaxLink<Void>("memberLink") {
						public void onClick(AjaxRequestTarget target) {
							Facet facet = getFacet(member.getFacet());
							setMaxMembers(facet, defaultMaxMembers);
							sizes.remove(facet.getName());

							onMemberSelect(target, member);
						};
					};
					memberLink.add(new Label("memberName", member.getObject().getDisplayName()));
					item.add(memberLink);
					item.add(new Label("memberCount", "("+String.valueOf(member.getObject().getCount())+")"));
				};
			};
			
			members.setVisible(false);
			
			Label marklabel = new Label("mark", new Model<String> () {
				public String getObject() {
					return members.isVisible() ? "-" : "+";
				}
			});
			
			WebMarkupContainer linkcontainer = new WebMarkupContainer("navigationlink-container");
			AjaxLink<?> navigationLink = new IndicatingAjaxLink<Void>("navigationLink") {
				public void onClick(AjaxRequestTarget target) {
					if (!members.isVisible()) {
						members.setVisible(true);
					}
					else {
						members.setVisible(false);
					}
					target.add(item);
				};
			};
			linkcontainer.setVisible(facet.isNavigable() && member.getObject().isNavigable());
			navigationLink.add(marklabel);
			linkcontainer.add(navigationLink);
			((WebMarkupContainer)item.get("member-container")).add(linkcontainer);
			add(members);
		}
		public boolean isVisible() {
			return members.isVisible();
		}
	}
	
	public class RangeFragment extends Fragment {
		
		Date from, to;
		String facetname;
		
		public RangeFragment(Facet facet) {
			super("range", "rangeFragment", FacetsPanel.this);
			
			setOutputMarkupId(true);
			
			facetname = facet.getName();
			
			Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
			
			form.add(new DateField("from", new PropertyModel<Date>(this, "from")));
			
			form.add(new DateField("to", new PropertyModel<Date>(this, "to")));

			form.add(new AjaxLink<Void>("cancel-link") {
				public void onClick(AjaxRequestTarget target) {
					form.setVisible(false);
					target.add(RangeFragment.this);
				}
				public boolean isVisible() {
					return form.isVisible();
				}
			});
			
			form.add(new AjaxSubmitLink("apply-link", form) {
				public void onSubmit(AjaxRequestTarget target) {
					
					if (getFrom()==null) {
						setFrom(new Date(0));
					}
					
					if (getTo()==null)
						setTo(new Date());
					
					if (getFrom()!=null) {
						SolrMember member = new com.novamens.kbee.content.multidimensional.RangeMember(facetname, getFrom(), getTo());
						RangeFragment.this.onSelect(target, new MemberModel(member));
					}
				}
				public boolean isVisible() {
					return form.isVisible();
				}
			});
			
			form.setVisible(false);
			
			add(form);
			
			
			add(new AjaxLink<Void>("link") {
				public void onClick(AjaxRequestTarget target) {
					form.setVisible(true);
					target.add(RangeFragment.this);
				}
				public boolean isVisible() {
					return !form.isVisible();
				}
			});
		}
		
		public void setFrom(Date date) {
			this.from = date;
		}
		
		public Date getFrom() {
			return this.from;
		}
		
		public void setTo(Date date) {
			this.to = date;
		}
		
		public Date getTo() {
			return this.to;
		}
		
		public void onSelect(AjaxRequestTarget target, MemberModel range) {
			
		}
	}
	
	
	
	public class FilterFragment extends Fragment {
		private long elements = 0;
		private String member;
		public FilterFragment(String member) {
			super("filter", "filterFragment", FacetsPanel.this);
			setOutputMarkupId(true);
			setMember(member);
		}
		public String getMember() {
			return member;
		}
		public void setMember(String member) {
			this.member = member;
		}
		public void setElements(long elements) {
			
		}
		@Override
		public boolean isVisible() {
			Facet facet = getFacet(getMember());
			//Long size = sizes.get(facet.getName());
			//if (size==null) size = Long.valueOf(0);
			return (isExpanded(facet) && facet.isFilterable()) || (!"".equals(getFilter(facet)) && getFilter(facet)!=null);
		}
		protected void onUpdate(AjaxRequestTarget target) {
		}
		public void refresh(AjaxRequestTarget target) {
			addField();
			target.add(this);;
		}
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();

		}
		public void onInitialize() {
			super.onInitialize();
			addField();

		}
		public void addField() {
			WebMarkupContainer container = new WebMarkupContainer("container") {
				public boolean isVisible() {
					Facet facet = getFacet(getMember());
					Long size = sizes.get(facet.getName());
					return size!=null && size>10;
				}
			};
			
			//boolean v = container.isVisible();
			
			container.setOutputMarkupId(true);

			TextField<String> filter = new TextField<String>("filter-input");
			
			filter.add(new AjaxFormComponentUpdatingBehavior("keyup") {
				public void onUpdate(AjaxRequestTarget target) {
					Facet facet = getFacet(getMember());
					setFilter(facet, filter.getInput());
					FilterFragment.this.onUpdate(target);
				}
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					ThrottlingSettings settings = new org.apache.wicket.ajax.attributes.ThrottlingSettings("fid", Duration.ofMillis(400));  
					attributes.setThrottlingSettings(settings);
				}
			});
				filter.setModel(new Model<String>() {
				public String getObject() {
					Facet facet = getFacet(getMember());
					return getFilter(facet);
				}
				public void setObject(String value) {
					Facet facet = getFacet(getMember());
					setFilter(facet, value);
				}
			});
			container.addOrReplace(filter);
			addOrReplace(container);

		}
	}	
	
	public class BreadcrumbFragment extends Fragment {
		private String member;
		public BreadcrumbFragment(String member) {
			super("breadcrumb", "breadcrumbFragment", FacetsPanel.this);
			setMember(member);
		}
		public String getMember() {
			return member;
		}
		public void setMember(String member) {
			this.member = member;
		}
		@Override
		public boolean isVisible() {
			Facet facet = getFacet(getMember());
			return facet.isHierachical();
		}
		@Override
		public void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> bclink = new AjaxLink<>("root-link") {
				public void onClick(AjaxRequestTarget target) {
					onMemberRemove(target, new MemberModel(getRoot()));
				}
			};
			bclink.add(new Label("root-label", getFacet(getMember()).getDisplayName()));
			bclink.setVisible(getElements().size()>1);
			add(bclink);
			add (new ListView<Member>("breadcrumb-element", () -> getElements()) {
				public void populateItem(ListItem<Member> item) {
					AjaxLink<Void> bclink = new AjaxLink<>("bc-link") {
						public void onClick(AjaxRequestTarget target) {
							onMemberSelect(target, new MemberModel(item.getModelObject()));
						}
					};
					bclink.add(new Label("bc-item", item.getModelObject().getDisplayName()));
					item.add(bclink);
				}
			});
		}
		public Member getRoot() {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.putAll(getSearcher().getQuery().getParameters());
			
			Facet facet = getFacet(getMember());
			
			@SuppressWarnings("unchecked")
			List<String> members = (List<String>)parameters.get("members");
			
			for (String member : members) {
				if (member.startsWith(facet.getName())) {
					SolrMember b = new SolrMember(); 
					b.setPath(member);
					b.setFacet(facet.getName());
					return b;
				}
			}
			
			return null;
					
		}

		public List<Member> getElements() {
			List<Member> elements = new ArrayList<>();
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.putAll(getSearcher().getQuery().getParameters());
			if (parameters.get("members")==null) return elements;
			
			Facet facet = getFacet(getMember());
			
			@SuppressWarnings("unchecked")
			List<String> members = (List<String>)parameters.get("members");
			for (String member : members) {
				if (member.contains("|")) {
					member = member.split("\\|")[0];
				}
				if (member.startsWith(facet.getName())) {
					String path[] = member.split("/");
					String mp = path[0];
					if (path.length>2 && isDigits(path[1])) {
						for (int p=1; p<path.length; p++) {
							String memberid = path[p];
							memberid = memberid.replace("*", "");
							DataSetMember m  = getRepository(DataSetMember.class).findById(Long.valueOf(memberid) );
							SolrMember b = new SolrMember(); 
							b.setDisplayName(m.getDisplayName());
							mp += "/" + path[p];
							b.setPath(mp+ "*");
							b.setFacet(facet.getName());
							elements.add(b);
						}
					}
				}
			}
			return elements;
		}
	}


	/** ---------
	 * 
	 * 
	 * @param id
	 */
	public FacetsPanel(String id) {
		super(id);
		//this.locale=getSessionUser().getLocale();
	}
	
	public FacetsPanel(Searcher searcher) {
		this("facets", searcher);
	}
	
	public FacetsPanel(String id, Searcher searcher) {
		super(id);
		
		setOutputMarkupId(true);
		searcher.setOptions(options);
		setSearcher(searcher);
		
		IDataProvider<String> facetsProvider = new ListDataProvider<String>() {
			public List<String> getList() {
				return getFacetsNames();
			}
		};
		final DataView<String> facets = new DataView<String>("facets", facetsProvider) {
			@Override
			protected void populateItem(final Item<String> facetitem){
				Facet facet = getFacet(facetitem.getModelObject());

				facetitem.setOutputMarkupId(true);
				
				ListDataProvider<MemberModel> membersProvider = new ListDataProvider<MemberModel>() {
					public List<MemberModel> getList() {
						Facet facet = getFacet(facetitem.getModelObject());
						List<MemberModel> members = getMembers(facet, getFilter(facet));
						return members;
					}
				};
				
				
				//sizes.put(facet.getName(), membersProvider.size());


				WebMarkupContainer facetContainer = new WebMarkupContainer("facet-container");
				facetContainer.setOutputMarkupId(true);
				
				final AjaxLazyLoadPanel<Component>  memberListContainer = new AjaxLazyLoadPanel<Component>("members") {
					@Override
					public Component getLazyLoadComponent(String markupId) {
						return new MembersFragment(markupId, facetitem, membersProvider) {
							public void onUpdate(AjaxRequestTarget target) {
								target.add(facetContainer);
							}
						};
					}
					protected void onContentLoaded(Component content, Optional<AjaxRequestTarget> target){
						Facet facet = getFacet(facetitem.getModelObject());
						long size = membersProvider.size();
						sizes.put(facet.getName(), size);
						FilterFragment f = (FilterFragment)facetContainer.get("filter");
						f.refresh(target.get());
						//System.out.print("ACA");
					}
					@Override
					public Component getLoadingComponent(final String id) {
 						return new Label(id,
							"<i class=\"fal fa-sync fa-spin fa-fw spinning\" style=\"font-size:13px; margin-left: 50px; margin-top:4px;\"></i>")
							.setEscapeModelStrings(false);
 					}
					@Override
					protected void onAfterRender()		{
						super.onAfterRender();
						//sizes.remove(facetitem.getModelObject());
					}	
				};
				
				boolean expanded = isExpanded(facet);
				memberListContainer.setVisible(expanded);

				
				facetContainer.add(new FilterFragment(facetitem.getModelObject()) {
					@Override
					public boolean isVisible() {
						Facet facet = getFacet(facetitem.getModelObject());
						boolean visible = (memberListContainer.isVisible() && facet.isFilterable()) || (!"".equals(getFilter(facet)) && getFilter(facet)!=null);
//						if (visible) {
//							Long size = sizes.get(facet.getName());
//							size = membersProvider.size();
//							visible = size>10;
//						}
						return visible;
					}
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						target.add(memberListContainer);
					}
				});

				facetContainer.add(new BreadcrumbFragment(facetitem.getModelObject()));
				
				WebMarkupContainer applyOrContainer = new WebMarkupContainer("applyor-container") {
					public boolean isVisible() {
 						if (!selectedmembers.isEmpty()) {
							for (MemberModel model : selectedmembers) {
								if (model.getFacet().equals(facetitem.getModelObject())) {
 									return true;
								}
							}
							return false;
						};
						return
							false;	
 					}	
				};
				applyOrContainer.add(new WorkingIndicatorAjaxLinkV5<Void>("applyor-link") {
					public void onClick(AjaxRequestTarget target) {
 						if (selectedmembers.size()>0) {
							onMembersSelect(target, getSelectedMembers(getFacet(facetitem.getModelObject())));
							selectedmembers.clear();
						}
					}
				});
				facetContainer.add(applyOrContainer);
				
				WebMarkupContainer rangeContainer = new WebMarkupContainer("range-container") {
					public boolean isVisible() {
						Facet facet = getFacet(facetitem.getModelObject());
						boolean visible = facet.isRangeEnabled() && selectedmembers.isEmpty();
						return visible;
					}	
				};
				rangeContainer.add(new RangeFragment(facet) {
					public void onSelect(AjaxRequestTarget target, MemberModel member) {
						onMemberSelect(target, member);
					}
				});
				facetContainer.add(rangeContainer);

				facetContainer.add(new AttributeModifier("class", new Model<String>() {
					public String getObject() {
 						if (!memberListContainer.isVisible()) {
 							return "panel-collapse collapse";
						}
						else {
							return "panel-collapse collapse in";
						}
					}
				}));

				ToggleLink togglelink = new ToggleLink("accordion-toggle");
				togglelink.add(new AjaxEventBehavior("click") {
					public void onEvent(AjaxRequestTarget target) {
						if (!memberListContainer.isVisible()) {
							Facet facet = getFacet(facetitem.getModelObject());
							Long s = sizes.get(facet.getName());
							if (s==null && facet.isFilterable()) {
								long size = membersProvider.size();
								sizes.put(facet.getName(), size);
							}
							memberListContainer.setVisible(!memberListContainer.isVisible());
							target.add(facetContainer);
 						}
					}
				});
				togglelink.add(getLabel(facet));
				togglelink.add(new AttributeModifier("href", "#"+facetContainer.getMarkupId()));
				togglelink.add(new AttributeModifier("aria-expanded", expanded ? "true" :  "false") );
				togglelink.add(new AttributeModifier("class", "accordion-toggle collapsed"));
				facetitem.add(togglelink);

				facetContainer.add(memberListContainer);
				facetitem.add(facetContainer);
			}
		};
		add(facets);
	}

	public void setSearcher(Searcher searcher) {
		this.searcher = searcher;
	}
	
	public Searcher getSearcher() {
		return this.searcher;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getFilter() {
		return filter;
	}
	
	public void setFilter(Facet facet, String filter) {
		getOptions(facet.getName()).filter = filter;
	}

	public String getFilter(Facet facet) {
		return getOptions(facet.getName()).filter;
	}

	public List<MemberModel> getMembers(Facet facet, String filter) {
		List<MemberModel> members = new ArrayList<MemberModel>();
		if (facet.isFilterable() && filter!=null && !"".equals(filter))
			for(Member member :facet.getMembers(getResultSet(), filter, getMaxVisibleMembers(facet)+1)) {
				members.add(new MemberModel(member));
			}
		else
			for(Member member : facet.getMembers(getResultSet(), getMaxVisibleMembers(facet)+1)) {
				members.add(new MemberModel(member));
			}
		return members;
	}

	public List<MemberModel> getMembers(Facet facet) {
		List<MemberModel> members = new ArrayList<MemberModel>();
			for(Member member : facet.getMembers(getResultSet(), getMaxVisibleMembers(facet)+1)) {
				members.add(new MemberModel(member));
			}
		return members;
	}
	
	@SuppressWarnings("unchecked")
	public boolean isExpanded(Facet facet) {
		List<String> p = (List<String>)getSearcher().getQuery().getParameters().get("members");
		if (p!=null) {
			for (String path : p) {
				if (path.contains(facet.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public List<MemberModel> getMembers(Facet facet, ResultSet resultSet) {
		List<MemberModel> members = new ArrayList<MemberModel>();
		for(Member member : facet.getMembers(resultSet, getMaxMembers(facet)+1)) {
			members.add(new MemberModel(member));
		}
		return members;
	}
	
	public List<MemberModel> getMembers(Facet facet, ResultSet resultSet, Member rootMember) {
		List<MemberModel> members = new ArrayList<MemberModel>();
		for(Member member : facet.getMembers(resultSet, rootMember, getMaxMembers(facet)+1)) {
			members.add(new MemberModel(member));
		}
		return members;
	}
	
	public List<String> getFacetsNames() {
		if (this.names==null) {
			this.names = new ArrayList<String>();
			for (Facet facet : getFacets()) {
				try {
					if (facet.isVisible(getResultSet()) && isVisible(facet)) {
						this.names.add(facet.getName());
					}
				} 
				catch (Exception e) {
					logger.error(e);	
				}
			}
		}
		return this.names;
	}
	
	public List<Facet> getFacets() {
		if (this.facets == null) {
			this.facets = getDomain().getService(FacetService.class).getFacets(getSearcher().getQuery());
		}
		return this.facets;
	}
	
	
	public void onMemberSelect(AjaxRequestTarget target, MemberModel member) {
	}
	
	public void onMemberRemove(AjaxRequestTarget target, MemberModel member) {
	}
	
	public void onMembersSelect(AjaxRequestTarget target, List<MemberModel> member) {
	}
	
	public boolean isVisible(Facet facet) {
		return true;
	}
	
	public void reset() {
		names = null;
		facets = null;
	}
	
	public void refresh(AjaxRequestTarget target) {
		names = null;
		target.add(this);
	}

	public void onDetach() {
		super.onDetach();
		this.facets = null;
		getSearcher().detach(); 
	}
	
	private ResultSet getResultSet() {
		getSearcher().setOptions(this.options);
		return getSearcher().getResultSet();
	}
	
	private Facet getFacet(String name) {
		for (Facet facet : getFacets()) {
			if (facet.getName().equals(name))
				return facet;
		}
		return null;
	}
	
	private List<MemberModel> getSelectedMembers(Facet facet) {
		List<MemberModel> selectedfacetmembers = new ArrayList<MemberModel>();
		for (MemberModel model : selectedmembers) {
			if (model.getFacet().equals(facet.getName())) {
				selectedfacetmembers.add(model);
			}
		}
		return selectedfacetmembers;
	}

	private Label getLabel(Facet facet) {
		return new Label("facet", (Serializable)facet.getDisplayName());
	}
	
	private String getRootLabel(List<MemberModel> members) {
		String label = null;
		if (!members.isEmpty()) {
			String stringlabel = getParentLabel(members.get(0));
			label = "> "+stringlabel;
			if (stringlabel == null) label = null;
		}
		else
			label = "";
		return label;
	}
	
	private void setMaxMembers(Facet facet, int value) {
		if (getOptions(facet.getName()).maxMembers < value)
			getOptions(facet.getName()).maxMembers = value;
		getOptions(facet.getName()).maxVisibleMembers = value;
	}
	
	private int getMaxVisibleMembers(Facet facet) {
		return getOptions(facet.getName()).maxVisibleMembers;
	}
	
	private int getMaxMembers(Facet facet) {
		return getOptions(facet.getName()).maxMembers;
	}
	
	private FacetOptions getOptions(String facetName) {
		FacetOptions options = this.options.get(facetName);
		if (options == null) {
			options = new FacetOptions();
			options.maxMembers = defaultMaxMembers;
			this.options.put(facetName, options);
		}
		return options;
	}
	
	@SuppressWarnings("unchecked")
	private List<MemberModel> getNavigation(MemberModel member) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.putAll(getSearcher().getQuery().getParameters());
		parameters.putAll(((SolrQuery)getSearcher().getQuery()).getFilterParameters());
		List<String> members = new ArrayList<String>();
		
		if (parameters.get("members")!=null) 
			members.addAll((List<String>)parameters.get("members"));
		
		members.add(member.getObject().getPath().toString());
		
		parameters.put("members", members);
		Query query = getSearcher().getQuery().getBuilder().build(parameters);
		ResultSet resultSet = null;
		List<MemberModel> models = null;
		try { 
			resultSet = query.execute();
			models = getMembers(getFacet(member.getFacet()), resultSet, member.getObject());
		}
		finally {
			if (resultSet!=null) 
				resultSet.close();
		}
		return models;
	}
	
	private String getParentLabel(MemberModel model) {
		Member parent = model.getObject().getParent();
		if (parent != null) {
			String label  = parent.getDisplayName();
			while (parent != null) {
				parent = parent.getParent();
				if (parent!= null) {
					label = parent.getDisplayName() + " > " + label;
				}
			}
			return label;
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean isFilter(Member member) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.putAll(getSearcher().getQuery().getParameters());
		if (parameters.get("members")==null) return false;
		for(String path : (List<String>)parameters.get("members")) {
			if (member.getPath()!=null && member.getPath().equals(path)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isDigits(String argument) {
		for (int c = 0; c < argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}
	

}
