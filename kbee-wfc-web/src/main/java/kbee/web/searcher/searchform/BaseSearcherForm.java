package kbee.web.searcher.searchform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.PersonMember;
import com.novamens.content.user.UserProfile;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.content.multidimensional.RelationFacet;
import com.novamens.security.User;
import com.novamens.solr.indexer.multidimensional.SolrCube;
import com.novamens.wicket.markup.html.panel.KBPanel;

public class BaseSearcherForm<T> extends KBPanel implements SearcherFormPanel<T> {
	private static final long serialVersionUID = 1L;

	private IModel<T> model;

	private String name;
	private String usage;
	private String title;
	private String text;
	private String domainName;

	private boolean advancedSearchLinkVisible = false;

	private IModel<String> label;

	public BaseSearcherForm() {
		super("main-searcher");
		label = new StringResourceModel("advanced-search", this, null);
	}

	public BaseSearcherForm(String id) {
		super(id);
	}

	public BaseSearcherForm(String id, IModel<T> model) {
		super(id, model);
		setModel(model);
		setName(model.getObject().getClass().getSimpleName());
	}

	public BaseSearcherForm(String id, IModel<T> model, String name) {
		super(id, model);
		setName(name);
		setModel(model);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (model != null)
			model.detach();
	}

	public void setAdvancedSearchLinkVisible(boolean b) {
		advancedSearchLinkVisible = b;
	}

	public boolean isAdvancedSearchLinkVisible() {
		return advancedSearchLinkVisible;
	}

	@Override
	public void setAdvancedSearchLinkLabel(IModel<String> b) {
		label = b;
	}

	@Override
	public IModel<String> getAdvancedSearchLinkLabel() {
		return label;
	}

	public String getDomainName() {
		return this.domainName;
	}

	public void setDomainName(String d) {
		this.domainName = d;
	}

	public IModel<T> getModel() {
		return model;
	}

	public void setModel(IModel<T> model) {
		this.model = model;
	}

	public T getModelObject() {
		return this.model != null ? model.getObject() : null;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	@Override
	public Map<String, Object> getParameters() {
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String s) {
		this.title = s;
	}

	@Override
	public String getUsageInfo() {
		return usage;
	}

	@Override
	public void setUsageInfo(String s) {
		this.usage = s;

	}

	protected String getPath(DataSetMember member) {
		String path = null;
		List<String> members = new ArrayList<String>();
		for (Facet facet : getFacets()) {
			if (facet instanceof ClassifierFacet) {
				ClassifierFacet classifierfacet = (ClassifierFacet) facet;
				if (((ClassifierFacet) facet).getDisplayName().equals(member.getDataSet().getName())) {
					path = ((ClassifierFacet) facet).getMember(member).getPath();
				} else {
					if (classifierfacet.getClassifier() != null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						path = classifierfacet.getMember(member).getPath();
					}
				}
			} else if (facet instanceof ClassifierHierarchicalFacet) {
				ClassifierHierarchicalFacet classifierfacet = (ClassifierHierarchicalFacet) facet;
				if (classifierfacet.getDisplayName().equals(member.getDataSet().getName())) {
					path = classifierfacet.getMember(member).getPath();
				} else {
					if (classifierfacet.getClassifier() != null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						path = classifierfacet.getMember(member).getPath();
					}
				}
			} else if (facet instanceof RelationFacet) {
				if (((RelationFacet) facet).getClassName().equals("user") && member.getDataSet().getDataSetType().equals(DataSetType.USER)) {
					Person person = ((PersonMember) member).getPerson();
					User user = person.getProfile(UserProfile.class).getUser();
					members.add(facet.getName() + "/" + user.getId());
					break;
				}
			}
		}
		return path;
	}

	protected List<Facet> getFacets() {
		List<Facet> facets = new ArrayList<Facet>();
		facets.addAll(((SolrCube) getQueryIndex().getCube()).getFacets());
		return facets;
	}

	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

//	protected User getSessionUser() {
//		return ServiceLocator.getService(SecurityService.class).getSessionUser();
//	}
//	
//	protected Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}

	protected void onChange(AjaxRequestTarget target, Map<String, Object> parameters) {
		fire(new SearcherOnChangeEvent(target, parameters));
	}
}
