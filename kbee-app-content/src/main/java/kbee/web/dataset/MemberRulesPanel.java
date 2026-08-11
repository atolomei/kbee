package kbee.web.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.IQLRule;
import com.novamens.content.web.security.markup.RuleStandAlonePage;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.security.PredicatesIqlEvaluator;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

@SuppressWarnings("serial")
public class MemberRulesPanel extends ModelPanel<DataSetMember> {
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(MemberRulesPanel.class.getName());
	
	private Classifier classifier;
	
	public MemberRulesPanel(String id, IModel<DataSetMember> model) {
		super(id, model);
		
		add(new ListView<SecurityRule>("rules", new PropertyModel<List<SecurityRule>>(this, "rules")) {
			public void populateItem(final ListItem<SecurityRule> item) {
				Link<Void> rulelink = new Link<Void>("rule-link") {
					public void onClick() {
						setResponsePage(new RuleStandAlonePage(new ObjectModel<IQLRule>((IQLRule)item.getModelObject())));
					}
				};
				rulelink.add(new Label("rule-label", item.getModelObject().getName()));
				item.add(rulelink);
			}
		});
		
		add(new WebMarkupContainer("emptypanel") {
			public boolean isVisible() {
				return getRules().isEmpty();
			}
		});
	}
	
	public void onDetach() {
		this.classifier = null;
		super.onDetach();
	}
	
	public List<SecurityRule> getRules() {
		List<SecurityRule> rules = new ArrayList<SecurityRule>();
		if (getClassifier()!=null)
		for (SecurityRule rule : getSecurityDao().getRules(getDomain())) {
			if (rule.getCondition()!=null && getMembers(rule).contains(String.valueOf(getModel().getObject().getId()))) {
				rules.add(rule);
			}
		}
		return rules;
	}
	
	private List<String> getMembers(SecurityRule rule) {
		
		try {
			Expression iqlexpression = getClassifier().getDomain().getService(IqlService.class).getExpression(rule.getCondition());
			PredicatesIqlEvaluator evaluator = new PredicatesIqlEvaluator(iqlexpression);
			Map<String, List<String>> predicates = evaluator.evaluate();
			List<String> membersids = predicates.get(getClassifier().getPredicate());
			
			if (membersids==null) 
				membersids = new ArrayList<String>();

			return membersids;
		} 
		catch (Exception e) {
			List<String> li =  new ArrayList<String>();
			li.add("Error: " + e.getClass().getName());
			return li;
		}
	}
	
	private Classifier getClassifier() {
		
		if (this.classifier!=null)
			return this.classifier;
		
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			try {
				if (classifier.getDataSet().equals(getModel().getObject().getDataSet())) {
					this.classifier = classifier;
					break;
				}
			} catch (RuntimeException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
		return this.classifier;
	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}

}
