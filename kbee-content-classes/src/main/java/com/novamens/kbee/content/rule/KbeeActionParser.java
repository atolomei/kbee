 package com.novamens.kbee.content.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.rule.Action;
import com.novamens.content.rule.ArchiveAction;
import com.novamens.content.rule.ClassificationAction;
import com.novamens.content.rule.DeleteAction;
import com.novamens.content.rule.LaunchAction;
import com.novamens.content.rule.RemoveClassificationAction;
import com.novamens.content.rule.SendNotificationAction;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.dom.Json;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;

public class KbeeActionParser extends ActionParser {
	
	// classification action { classifier: id, values : [{ value }] }
	// multiple action [ {}, {} ]

	@SuppressWarnings("unchecked")
	public Json getJson(Action action) {
		KbeeJson json = new KbeeJson();
		if (action instanceof KbeeMultipleAction) {
			List<Map<String, String>> jsonactions = new ArrayList<Map<String, String>>();
			for (Action singleaction : ((KbeeMultipleAction)action).getActions()) {
				jsonactions.add(getMap(singleaction));
			}
			json.put("actions", jsonactions);
		}
		else {
			if (action!=null) {
				json.put("action", getMap(action));
			}
			else {
				json = null;
			}
		}
		return json;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Action getAction(Json json) {
 		Action action = null;
		if (json.get("action")!=null) {
			action = getAction((Map)json.get("action"));
		}
		else {
			if (json.get("actions")!=null) {
				List<Action> actions = new ArrayList<Action>();
				List<Map> maps = (List<Map>)json.get("actions");
				for (Map map : maps) {	
					actions.add(getAction(map));
				}
				action = new KbeeMultipleAction(actions);
			}
		}
		return action;	
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Action getAction(Map map) {
		String type = (String)map.get("type");
		if ("sendnotification".equals(type)) {
			KbeeSendNotificationAction action = new KbeeSendNotificationAction();
			
			Object roleidvalue = map.get("role");
			if (roleidvalue!=null) {
				Long roleid = roleidvalue instanceof String ?  Long.valueOf((String)roleidvalue) : Long.valueOf((Integer)roleidvalue); 
				KbeeAbstractRole role = (KbeeAbstractRole)getSecurityDao().findRoleById(roleid);
				if (role!=null)
				action.setRole(role);
			}
			
			Object personIds = map.get("personIds");
			if (personIds!=null)
				action.setNotifyPersonListString( (String) personIds);
			
			action.setSubtitle(unescape((String)map.get("subject")));
			action.setText(unescape((String)map.get("text")));
			
			return action;
		}
		if ("classification".equals(type) || "removeclassification".equals(type)) {
			KbeeClassificationAction action = "classification".equals(type) ? new KbeeClassificationAction() : new KbeeRemoveClassificationAction();
			String classifierid = (String)map.get("classifier");
			if (classifierid!=null) {
				try {
					KbeeClassifier classifier = (KbeeClassifier) getContentDao().findModelObjectById(Classifier.class, Long.valueOf(classifierid));
					if (classifier!=null)
						action.setClassifier(classifier);
				}
				catch (NumberFormatException e) {
				}
			}
			List<Map> valuesmaps = (List<Map>)map.get("values");
			List<DataSetMember> values = new ArrayList<DataSetMember>();
			if (valuesmaps!=null) {
				for (Map valuemap : valuesmaps) {
					if (valuemap.get("value")!=null) {
						try {
							Long valueid = Long.valueOf((String)valuemap.get("value"));
							KbeeDataSetMember value = (KbeeDataSetMember) getContentDao().findMemberById(valueid);
							if (value!=null) values.add(value);
						}
						catch (NumberFormatException e) {
						}
					}
				}
			}
			action.setValues(values);
			
			return action;
		}
		if ("archive".equals(type)) {
			KbeeArchiveAction action = new KbeeArchiveAction();
			return action;
		}
		if ("delete".equals(type)) {
			KbeeDeleteAction action = new KbeeDeleteAction();
			return action;
		}
		if ("launch".equals(type)) {
			KbeeLaunchAction action = new KbeeLaunchAction();
			try {
				Integer launcherid = (Integer)map.get("launcher");
				if (launcherid!=null) {
					KbeeProcessLauncher launcher = (KbeeProcessLauncher) getWorkflowDao().getProcessLauncher(Long.valueOf(launcherid));
					if (launcher!=null) {
						action.setLauncher(launcher);
						action.setNote(unescape((String)map.get("note")));
					}	
				}	
			}
			catch (Exception e) {
			}
			return action;
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private Map getMap(Action action) {
		if (action==null)
			return null;
		Map map = new HashMap();
		if (action instanceof SendNotificationAction) {
			map.put("type", "sendnotification");

			if (((KbeeSendNotificationAction)action).getNotifyPersonListString()!=null) {
				map.put("personIds", ((KbeeSendNotificationAction)action).getNotifyPersonListString());
			}
			if (((KbeeSendNotificationAction)action).getRole()!=null)
				map.put("role", ((KbeeSendNotificationAction)action).getRole().getId());
			
			if (((KbeeSendNotificationAction)action).getText()!=null && !"".equals(((KbeeSendNotificationAction)action).getText()))
				map.put("text", escape(((KbeeSendNotificationAction)action).getText()));
			
			if (((KbeeSendNotificationAction)action).getSubtitle()!=null && !"".equals(((KbeeSendNotificationAction)action).getSubtitle()))
				map.put("subject", escape(((KbeeSendNotificationAction)action).getSubtitle()));
		}
		else 
		if (action instanceof ClassificationAction) {
				map.put("type", action instanceof RemoveClassificationAction ? "removeclassification" : "classification") ;
			if (((KbeeClassificationAction)action).getClassifier()!=null)
				map.put("classifier", ((KbeeClassificationAction)action).getClassifier().getId());
			
			List<Map> values = new ArrayList<Map>();
			for (DataSetMember value : (((KbeeClassificationAction)action).getValues())) {
				Map valuemap = new HashMap();
				valuemap.put("value", value.getId());
				values.add(valuemap);
			}
			if (!values.isEmpty()) {
				map.put("values", values);
			}
		}
		else 
		if (action instanceof DeleteAction) {
			map.put("type", "delete");
		}
		else
		if (action instanceof ArchiveAction) {
			map.put("type", "archive");
		}
		else 
		if (action instanceof LaunchAction) {
			map.put("type", "launch");
			if (((KbeeLaunchAction)action).getLauncher()!=null)
			map.put("launcher", ((KbeeLaunchAction)action).getLauncher().getId());
			map.put("note", escape(((KbeeLaunchAction)action).getNote()));
		}
		else {
			Assert.isTrue(false, "invalid action");
		}
		return map;
	}	
	
	private String escape(String value) {
		if (value!=null) {
			value = value.replace("\\", "");
			value = value.replace("\"", "\\'");
		}
		return value;
	}
	
	private String unescape(String value) {
		if (value!=null) {
			value = value.replace("\\'", "\"");
		}
		return value;
	}
	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
}
