package kbee.web.searcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.content.multidimensional.RelationFacet;
import com.novamens.kbee.wicket.markup.html.event.ExplorerOpenEvent;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrCube;

import kbee.web.search.SuggesterSearchField;
import kbee.web.search.service.SiteSearchSuggestionService;
import kbee.web.searcher.searchform.AdvancedSearchClickEvent;
import kbee.web.searcher.searchform.BaseSearcherForm;

@SuppressWarnings("serial")
public class SearcherForm extends BaseSearcherForm<Site> {
	private static final long serialVersionUID = 1L;

	HashMap<String, Object> parameters = new HashMap<String, Object>();

	//@SuppressWarnings("unused")
	//private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherForm.class.getName());
	
	private String text;
	boolean is_initialized = false;

	public SearcherForm(String id) {
		super(id);
	}
	
	public SearcherForm() {
		this("main-searcher", null);
	}
	
	public SearcherForm(String id, IModel<Site> model) {
		this(id, model, model.getObject().getTitle());
	}
	
	public SearcherForm(String id, IModel<Site> model, String name) {
		super(id, model, name);
		super.setAdvancedSearchLinkLabel(new StringResourceModel("advanced-search", this, null));
	}

	@Override
	public Map<String, Object> getParameters() {
		String   text = getText();
		if (text!=null) {
			text = text.replace("-", " ");
		}
		parameters.put("text", text);
		return parameters;
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (this.is_initialized)
			return;

		setName(getModel().getObject().getTitle());
		
		Form<String> form = new Form<String>("searchform");
		
		SuggesterSearchField<String> text = new SuggesterSearchField<String>("text", new PropertyModel<String>(this, "text")) {
			@Override
			public void onSearch(AjaxRequestTarget target, Suggestion suggestion) {
				SearcherForm.this.onSearch(target, suggestion);
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				return SearcherForm.this.getSuggestions(filter(pattern)); 
			}
			@Override
			protected IModel<String> getPlaceHolder() {
				return SearcherForm.this.getLabel("search", SearcherForm.this.getName());
			}
			@Override
			protected boolean includeInfo() {
				return SearcherForm.this.includeInfo(); 
			}
			@Override
			protected String getInfo(Suggestion suggestion) {
				return SearcherForm.this.getInfo(suggestion); 
			}
		};
		
		form.add(text);
		
		
		AjaxSubmitLink searchbutton = new AjaxSubmitLink("search", form) {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				onChange(target, getParameters());
			}
		};
		form.setDefaultButton(searchbutton);
		form.add(searchbutton);
		addOrReplace(form);
		
		//setAdvancedSearchLinkVisible( true );
		is_initialized = true;
		
		WebMarkupContainer as= new WebMarkupContainer("advancedSearchContainer") {
			public boolean isVisible() {
				return isAdvancedSearchLinkVisible();
			}
		};
		
		form.addOrReplace(as);
		
		AjaxLink<Void> asl=new AjaxLink<Void>("advanced") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire(new AdvancedSearchClickEvent(target));
			}
			
			public boolean isVisible() {
				return isAdvancedSearchLinkVisible();
			}
			
		};
		as.add(asl);
		
		
		Link<Void> av=new Link<Void>("explorer") {
			@Override
			public void onClick() {
				// fire(new AdvancedSearchClickEvent(target));
				fire(new ExplorerOpenEvent<Site>(SearcherForm.this.getModel()));
			
			}
			
			public boolean isVisible() {
				return isAdvancedSearchLinkVisible();
			}
			
		};
		as.add(av);
		
		
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
	
	@SuppressWarnings("unchecked")
	protected void onSearch(AjaxRequestTarget target, Suggestion suggestion) {
		Object object = suggestion.getObject();
		if (object instanceof IModel && ((IModel<?>)object).getObject() instanceof DataSetMember) {
			getParameters().remove("iql");
			setAsParameter(((IModel<DataSetMember>)object).getObject(), suggestion.getText());
			getParameters().put("sort", "relevance");
			onChange(target, getParameters());
		}
		else
			if (object instanceof IModel && ((IModel<?>)object).getObject() instanceof Content) {
				String url = getUrl((IModel<Content>)object);
				target.appendJavaScript("var win = window.open('"+url+"', '_blank');");
			}
	}
	
	protected List<Suggestion> getSuggestions(String pattern) {
		return getModelObject().getService(SiteSearchSuggestionService.class).getSuggestions(pattern); 
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected String filter(String text) {
		if (text!=null) {
			text = text.replace("[", "");
			text = text.replace("{", "");
			text = text.replace("]", "");
			text = text.replace("}", "");
			//text = text.replace("-", " ");
			text = text.replace("/", "");
			if ("".equals(text.trim()))
				text = null;
		}
		return text;
	}
	
	protected void setAsParameter(DataSetMember member, String text) {
		List<String> members = new ArrayList<String>();
		String facetname=null;
		boolean descendants = true;
		for (Facet facet : getFacets()) {
			if (facet instanceof ClassifierFacet) {
				ClassifierFacet  classifierfacet = (ClassifierFacet)facet; 
				if (((ClassifierFacet)facet).getDisplayName().equals(member.getDataSet().getName()) ||
						(facetname!=null && facetname.equals(facet.getDisplayName()))) {
					members.add(((ClassifierFacet)facet).getMember(member).getPath());
				}
				else {
					if (facetname==null && classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						members.add(classifierfacet.getMember(member).getPath());
					}
				}
			}
			else
			if (facet instanceof ClassifierHierarchicalFacet) {
				ClassifierHierarchicalFacet  classifierfacet = (ClassifierHierarchicalFacet)facet; 
				if ((facetname==null && classifierfacet.getDisplayName().equals(member.getDataSet().getName())) || 
					(facetname!=null && facetname.equals(facet.getDisplayName()))) {
					String path = member.getDataSet().isHierachical() && descendants
						? classifierfacet.getMember(member).getPath()+"*"
						: classifierfacet.getMember(member).getPath();		
					members.add(path);
				}
				else {
					if (facetname==null && classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						members.add(classifierfacet.getMember(member).getPath());
					}
				}
			}
			else
			if (facet instanceof RelationFacet) {
				if (((RelationFacet)facet).getClassName().equals("user") && member.getDataSet().getDataSetType().equals(DataSetType.USER)) {
					Person person = ((PersonMember)member).getPerson();
					User user = person.getProfile(UserProfile.class).getUser();
					members.add(facet.getName() + "/" + user.getId());
					break;
				}
			}
		}
		getParameters().put("members", members);
	}
	
	protected String getUrl(IModel<Content> model) {
		
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		
		String url= ServiceLocator.getService(PortalUrlService.class).getRelativeDetailUrl(model.getObject(), getModel().getObject());
		
		url = protocol +"://" + host + port + "/" + url;
		
		
		return url;
	}
	
	protected List<Facet> getFacets() {
		List<Facet> facets = new ArrayList<Facet>();
		facets.addAll(((SolrCube)getIndex().getCube()).getFacets());
		return facets;
	}
	
	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected boolean includeInfo() {
		return true;
	}
	
	protected String getInfo(Suggestion suggestion) {
		String info = "";
		IModel<?> model = (IModel<?>)suggestion.getObject();
		Object object = model.getObject();
		if (object instanceof DataSetMember) {
			DataSetMember member = (DataSetMember)object;
			ExtractionRule rule = member.getDataSet().getSublineRule();
			if (rule!=null) {
				info = (String)rule.extract((DataSetMember)object);
			}
		}
		else {
			if (object instanceof Content) {
				Content content = (Content)object;
				info = content.getService(ContentService.class).getConsoleSubtitle();
			}	
		}
		return info; 
	}
}
