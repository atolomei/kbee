package com.novamens.kbee.content.form;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.SignedData;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EListField;
import com.novamens.content.model.DataSetMember;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Json;
import com.novamens.kbee.content.base.KbeeSignedData;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.resource.AbstractResource;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "WF_Form_Data")
@Proxy(lazy=false)
public class KbeeEFormActivityData implements EFormContentData, Identifiable  {

	@Id
	@SequenceGenerator(name = "data_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_sequencer")
	@Column(name = "ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeWorkflowActivity.class)
	@JoinColumn(name="activity_id")
	private Activity activity;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeEForm.class)
	@JoinColumn(name="form_id")
	private EForm form;	
	
	@Column(name = "capture")
	private String capture;
	
	@Column(name = "data")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json jsondata;
	
	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = KbeeSignedData.class)
	@JoinTable(name = "WF_SIGNEDFORM_DATA", 
	joinColumns = {	@JoinColumn(name = "DATA_ID", nullable = false, updatable = false) }, 
		inverseJoinColumns = { @JoinColumn(name = "SIGNED_ID", nullable = false, updatable = false) })
	@Fetch(FetchMode.SELECT)
	private List<SignedData> signatures = new ArrayList<SignedData>();
	
	public KbeeEFormActivityData() {
		
	}

	public KbeeEFormActivityData(Activity activity, EForm form) {
		this.activity = activity;
		this.form = form;
	}
		
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public String getDisplayName()	{
		return "-";
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	@Override
	public Object getData(String name) {
		return null;
	}
	
	@Override
	public Object getObject(String name) {
		return null;
	}
	
	@Override
	public Content getContent() {
		return getActivity()!=null ? ((KbeeWorkflowActivity)getActivity()).getContent() : null;
	}
	
	public String getCapture() {
		return capture;
	}
	
	public void setCapture(String capture) {
		this.capture = capture;
	}
	
	@Override
	public <T> List<T> getValues(EListField<T> field) {
		return new ArrayList<T>();
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getData(EFormField<?> field) {
		if (jsondata==null) {
			return null;
		}
		Object value = jsondata.get(field.getName());
		
		if (value instanceof List) {
			List values = new ArrayList();
			for (Object element : (List<?>)value) {
				if (element instanceof Map) {
					values.add(getValue((Map)element));
				}
			}
			return values;
		}
		
		if (value instanceof Map) {
			return getValue((Map)value);
		}
		
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public void setData(String name, Object value) {
		if (jsondata==null) {
			jsondata = new KbeeJson();
		}
		if (value!=null) {
			if (value instanceof List<?>) {
				if (!((List<?>)value).isEmpty()) {
					((KbeeJson)jsondata).put(name, getList(value));
				}
				else {
					jsondata.remove(name);
				}
			}
			else {
				((KbeeJson)jsondata).put(name, getMap(value));
			}	
		}	
		else {
			jsondata.remove(name);
		}
	}
	
	public void setData(EFormField<?> field, Object value) {
		setData(field.getName(), value);
	}
	
	@Override
	public EForm getForm() {
		return form;
	}
	
	public void setForm(EForm form) {
		this.form = form;
	}
	
	@Override
	public boolean isEmpty() {
		return jsondata!=null ? jsondata.isEmpty() : true;
	}
	
	public boolean isSigned() {
		return getSignatures()!=null && !getSignatures().isEmpty();
	}
	
	@Override
	public List<SignedData> getSignatures() {
		return signatures;
	}
	
	@Override
	public void setSignature(SignedData signature) {
		this.signatures.add(signature);
	}
	
	@Override
	public void setSignatures(List<SignedData> signatures) {
		this.signatures.clear();
		this.signatures.addAll(signatures);
	}
	
	@Override
	public void clearSignatures() {
		this.signatures.clear();
	}
	
	@Override
	public EFormData clone() {
		return null;
	}
	
	@Override
	public String getObjectTitle() {
		return getContent()!=null ? getContent().getTitle() : null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getList(Object value) {
		if (value==null)
			return null;
		
		if (value instanceof List<?>) {
			List values = new ArrayList();
			for (Object element : (List<?>)value) {
				values.add(getMap(element));
			}
			return values;
		}
		
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getMap(Object value) {
		if (value==null)
			return null;
		
		if (value instanceof DataSetMember) {
			Map map = new HashMap();
			map.put("type","member");
			map.put("label", ((DataSetMember)value).getDisplayName());
			map.put("id", (Long)((DataSetMember)value).getId());
			return map;
		}
		
		if (value instanceof Resource) {
			Map map = new HashMap();
			map.put("type","resource");
			map.put("id", (Long)((Resource)value).getId());
			return map;
		}
		
		if (value instanceof OffsetDateTime) {
			Map map = new HashMap();
			map.put("type","date");
			map.put("value", value.toString());
			return map;        
		}
		
		if (value instanceof Content) {
			Map map = new HashMap();
			map.put("type","content");
			map.put("id", (Long)((Content)value).getId());
			return map;
		}
		
		if (value instanceof Serializable) {
			Map map = new HashMap();
			map.put("type","value");
			map.put("value", value);
			return map;        
		}
		
		return null;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private Object getValue(Map map) {
		String type = (String)map.get("type");
		
		if ("value".equals(type)) {
			Object value = map.get("value");
			return value;
		}
		
		if ("date".equals(type)) {
			Object value = map.get("value");
			OffsetDateTime date = ServiceLocator.getService(DateTimeService.class).parseStrDate((String)value);
			return date;
		}
		
		if ("member".equals(type)) {
			Long id = Long.valueOf(String.valueOf(map.get("id")));
			DataSetMember member = getMember(id);
			return member;
		}

		if ("resource".equals(type)) {
			Long id = Long.valueOf(String.valueOf(map.get("id")));
			Resource resource = getResource(id);
			return resource;
		}
		
		return null;
	}
	
	private DataSetMember getMember(Long id) {
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		DataSetMember member = (DataSetMember)sf.getCurrentSession().load(KbeeDataSetMember.class, id);
		return member;		
	}
	
	private Resource getResource(Long id) {
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		Resource resource = (Resource)sf.getCurrentSession().load(AbstractResource.class, id);
		return resource;		
	}
}
