package kbee.web.workflow.task; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.dom.Json;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeValueMember;
import com.novamens.kbee.content.workflow.KbeeAttributeRule;
import com.novamens.kbee.content.workflow.KbeeClassificationRule;
import com.novamens.kbee.content.workflow.KbeeLetterRule;
import com.novamens.kbee.content.workflow.KbeeNotificationRule;
import com.novamens.kbee.content.workflow.KbeeScriptRule;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.kbee.content.workflow.RuleParser;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;

public class KbeeRuleParser extends RuleParser {
	
	static Logger logger = LogManager.getLogger(KbeeRuleParser.class.getName());

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Json getJson(WorkflowRule rule) {
		KbeeJson json = new KbeeJson();
		Map map = getMap(rule);
		if (map!=null)
			json.put("rule", map);
		else
			return null;
		return json;
	};
	
	@SuppressWarnings("rawtypes")
	public WorkflowRule getRule(Json json) {
		if (json==null) return null;
		Map rulemap = (Map)json.get("rule");
		WorkflowRule rule = getRule(rulemap);
		return rule;
	}	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getMap(WorkflowRule rule) {
		Map map = new HashMap();
		if (rule instanceof KbeeClassificationRule) {
			KbeeClassificationRule classificationrule = (KbeeClassificationRule)rule;
			map.put("classifier", classificationrule.getClassifier()!=null ? String.valueOf(classificationrule.getClassifier().getId()) : "-");
			if (classificationrule.getValue()!=null) {
				map.put("value", String.valueOf(classificationrule.getValue().getId()));
			}
		}
		else 
		if (rule instanceof KbeeAttributeRule) {
			KbeeAttributeRule attributerule = (KbeeAttributeRule)rule;
			map.put("attribute", attributerule.getAttribute()!=null ? String.valueOf(attributerule.getAttribute().getId()) : "-");
			if (attributerule.getValue()!=null) {
				map.put("value", attributerule.getValue());
			}
		}
		else
		if (rule instanceof KbeeNotificationRule) {
			KbeeNotificationRule notificationrule = (KbeeNotificationRule)rule;
			if (notificationrule.getText()!=null && !notificationrule.getReceivers().isEmpty()) {
				map.put("text", notificationrule.getText());
				List<Map<String, String>> receiversmap = new ArrayList<Map<String, String>>();
				for (Role role : notificationrule.getReceivers()) {
					Map receivermap = new HashMap();
					receivermap.put("role", String.valueOf(role.getId()));
					receiversmap.add(receivermap);
				}
				map.put("receivers", receiversmap);
			}
		}
		else
		if (rule instanceof KbeeScriptRule) {
			KbeeScriptRule scriptrule = (KbeeScriptRule)rule;
			if (scriptrule.getScript()!=null) {
				map.put("script", scriptrule.getScript());
			}
		}
		else
		if (rule instanceof KbeeLetterRule) {
			KbeeLetterRule letterrule = (KbeeLetterRule)rule;
			if (letterrule.getTemplate()!=null) {
				map.put("template", letterrule.getTemplate());
			}
		}
		else {
			Assert.isInstanceOf(MultipleRule.class, rule);
			MultipleRule multiplerule = (MultipleRule)rule;
			List<Map<String, String>> jsonrules = new ArrayList<Map<String, String>>();
			if (multiplerule.getRules().isEmpty()) return null;
			for (WorkflowRule singlerule : multiplerule.getRules()) {
				jsonrules.add(getMap(singlerule));
			}
			map.put("rules", jsonrules);
		}
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private WorkflowRule getRule(Map rulemap) {
		WorkflowRule rule = null;
		try {
			if (rulemap==null)
				return null;
			String classifierid = (String)rulemap.get("classifier");
			if (classifierid!=null && !classifierid.equals("-")) {
				KbeeClassifier classifier = (KbeeClassifier) getContentDao().findModelObjectById(Classifier.class, Long.valueOf(classifierid));
				String valueid = (String)rulemap.get("value");
				if (valueid != null) {
					KbeeValueMember value = (KbeeValueMember) getContentDao().findMemberById(Long.valueOf(valueid));
					if (classifier!=null && value!=null) {
						rule = new KbeeClassificationRule(classifier, value);
					}	
				}	
			}
			else { 
				String attributeid = (String)rulemap.get("attribute");
				if (attributeid!=null && !attributeid.equals("-")) {
					KbeeAttribute attribute = (KbeeAttribute) getContentDao().findModelObjectById(Attribute.class, Long.valueOf(attributeid));
					String value = (String)rulemap.get("value");
					if (value!=null && attribute!=null) {
						rule = new KbeeAttributeRule(attribute, value);
					}	
				}
				else {
					String text = (String)rulemap.get("text");
					if (text!=null) {
						rule = new KbeeNotificationRule();
						((KbeeNotificationRule)rule).setText(text);
						List<Role> roles = new ArrayList<Role>();
						for (Map rolemap : (List<Map>)rulemap.get("receivers")) {
							try {
								Role role = getContentSecurityDao().findRoleById(Long.valueOf((String)rolemap.get("role")));
								role = (Role)getContentDao().reload(role);
								if (role!=null )
									roles.add(role);
							}
							catch(Exception e) {
								logger.error(e);
							}
						}
						if (!roles.isEmpty()) {
							((KbeeNotificationRule)rule).setReceivers(roles);
						}
						else {
							rule = null;
						}
					}
					else {
						String template = (String)rulemap.get("template");
						if (template!=null) {
							rule = new KbeeLetterRule();
							((KbeeLetterRule)rule).setTemplate(template);
						}
						else {
							String script = (String)rulemap.get("script");
							if (script!=null) {
								rule = new KbeeScriptRule();
								((KbeeScriptRule)rule).setScript(script);
							}
							else {
								List<Map> rulesmaps = (List<Map>)rulemap.get("rules");
								List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
								if (rulesmaps!=null)
								for (Map singlerulemap : rulesmaps) {
									WorkflowRule singlerule = getRule(singlerulemap);
									if (singlerule!=null)
										rules.add(singlerule);
								}
								if (!rules.isEmpty()) {
									rule = new MultipleRule(rules);
								}
							}	
						}
					}	
				}
			}
		}
		catch (Exception e) {
			rule = null;
		}
		return rule;
	}
	
	private ContentSecurityDao  getContentSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
