package com.novamens.kbee.content.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EDisposition;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EFormAwareModel;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EIdentifiableForm;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;

@Entity
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_Form")
@Proxy(lazy=false)
public class KbeeEForm extends AbstractObject implements EIdentifiableForm {

	@Id 
	@SequenceGenerator(name = "form_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "form_sequencer")
	@Column(name = "ID")
	private Long id;

	@Column(name = "name")
	private String name;
	
	@Column(name = "display_name")
	private String displayName;
	
	@Column(name = "cssclass")
	private String cssClass;
	
	@Column(name = "components")
	private String jsoncomponents;
	
	@Column(name = "disposition")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.content.form.EDispositionUserType")
	private EDisposition disposition = EDisposition.VERTICAL;
	
	@Column(name = "display_level")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.content.form.EFormAccessLevelUserType")
	private EFormAccessLevel accessLevel = EFormAccessLevel.GENERAL;
	
	@Column(name = "use_inline")
	private boolean useInline;
	
	@Column(name = "file_container")
	private boolean fileContainer;
	
	@Column(name = "behaviors")
	private String behaviors;
	
	@Column(name = "visibility")
	private String visibilityCondition;
	
	@Column(name = "viewer")
	private String viewer;
	
	transient private List<EFormComponent> components;

	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public int getVersion()	{
		return 0;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String name) {
		this.displayName = name;
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public EDisposition getDisposition() {
		return disposition;
	}
	
	@Override
	public EFormAccessLevel getFormAccessLevel() {
		return accessLevel!=null?accessLevel:EFormAccessLevel.GENERAL;
	}
	
	public void setFormAccessLevel(EFormAccessLevel level) {
		this.accessLevel = level;;
	}
	
	@Override
	public boolean isUseInline() {
		return useInline;
	}

	@Override
	public boolean isFileContainer() {
		return fileContainer;
	}

	public void setUseInline(boolean useInline) {
		this.useInline = useInline;
	}

	public void setFileContainer(boolean fileContainer) {
		this.fileContainer = fileContainer;
	}

	public void setBehaviors(String behaviors) {
		this.behaviors = behaviors;
	}
	
	public void setBehaviors(List<String> behaviors) {
		String tokens = "";
		for (String behavior: behaviors) {
			if (!"".equals(tokens)) tokens += ", ";
			tokens += behavior.trim();
		}
		this.behaviors = tokens;
	}
	
	public String getViewer() {
		return viewer;
	}

	public void setViewer(String viewer) {
		this.viewer = viewer;
	}

	public List<String> getBehaviors() {
		List<String> behaviors = new ArrayList<String>();
		if (this.behaviors!=null) {
			String tokens[] = this.behaviors.split(",");
			for (int t=0; t<tokens.length; t++) {
				String token = tokens[t];
				token = token.trim();
				if (!"".equals(token)) {
					behaviors.add(token);
				}
			}
		}
		return behaviors;
	}
	
	public List<EFormComponent> getComponents() {
		if (components==null) {
			if (jsoncomponents!=null) {
				components = parseComponents(jsoncomponents);
				setForm(components);
			}
			else {
				return  new ArrayList<EFormComponent>();
			}
		}
		
		return components;
	}
	
	public void setComponents(List<EFormComponent> components) {
		this.jsoncomponents = getJson(components);
	}
	
	public List<EFormField<?>> getFields() {
		return getFields(getComponents());
	}
	
	@Override
	public EFormField<?> getField(String name) {
		for (EFormField<?> field : getFields(getComponents())) {
			if (name.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public String getVisibilityCondition() {
		return visibilityCondition;
	}

	public void setVisibilityCondition(String visibilityCondition) {
		this.visibilityCondition = visibilityCondition;
	}
	
	@Override
	public boolean isVisible(EFormData data) {
		if (visibilityCondition!=null && !"".equals(visibilityCondition)) {
			return Boolean.TRUE.equals(evaluate(visibilityCondition, data));
		}
		return true;
	}
	
	@Override
	public boolean hasToolbar() {
		return getViewer()!=null;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeEForm)) return false;
		return ((KbeeEForm)object).getId().equals(getId());
	}
	
	public AuditSet getAuditSet() {
		return AuditSet.MODEL;
	}
	
	private List<EFormField<?>> getFields(List<EFormComponent> components) {
		List<EFormField<?>> fields = new ArrayList<EFormField<?>>();
		for (EFormComponent component : components) {
			if (component instanceof EFormField) {
				fields.add((EFormField<?>)component);
			}
			if (component instanceof EFormContainer) {
				fields.addAll(getFields(((EFormContainer)component).getComponents()));
			}
		}
		return fields;
	}
	
	private void setForm(List<EFormComponent> components) {
		if (components!=null)
		for (EFormComponent component : components) {
			if (component instanceof EFormField) {
				EFieldModel<?> model = ((EFormField<?>)component).getModel();
				if (model!=null && model instanceof EFormAwareModel) {
					((EFormAwareModel)model).setForm(this);
				}
			}
			if (component instanceof EFormContainer) {
				setForm(((EFormContainer)component).getComponents());
			}
		}
	}
	
	private String getJson(List<EFormComponent> components) {
		return EFormParser.Get().getJson(components);
	}
	
	private List<EFormComponent> parseComponents(String json) {
		return EFormParser.Get().getComponents(json);
	}
	
	private Object evaluate(String condition, EFormData data) {
		return (new ScriptEvaluator()).evaluate(condition, data);
	}
}