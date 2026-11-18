package kbee.pjsf.web;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.query.AttributeFilter;
import com.novamens.content.query.ClassifierFilter;
import com.novamens.solr.indexer.query.SolrDateRangeFilter;
import com.novamens.wicket.markup.html.form.DateTimeField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.searcher.searchform.AdvancedSearchClickEvent;
import kbee.web.searcher.searchform.AdvancedSearchEvent;

@Getter 
@Setter
public class SumarioSearchForm extends KBPanel {
	private static final long serialVersionUID = 1L;


	private String texto;
	
	private String titulo;
	
	private OffsetDateTime desdeFecha;
	
	private OffsetDateTime hastaFecha;
	
	private String citaJurisprudencia;

	private String referenciaNormativa;
	
	private IModel<EntityMember> juez;
	
	private Form<Void> form = new Form<Void>("form");

	public SumarioSearchForm(String id) {
		super(id);
		addForm();
	}

	public SumarioSearchForm(String id, PageParameters parameters) {
		super(id);
	}

	public void onInitialize() {
		super.onInitialize();
		

	}
	
	private void addForm() {
		TextField<String> texto = new  TextField<>("texto");
		texto.setModel(new PropertyModel<String>(this, "texto"));
		form.add(texto);
		
		TextField<String> titulo = new  TextField<>("titulo");
		titulo.setModel(new PropertyModel<String>(this, "titulo"));
		form.add(titulo);
		
		
		DateTimeField desde = new DateTimeField("desdeFecha", ZoneId.systemDefault());
		desde.setModel(new PropertyModel<OffsetDateTime>(this, "desdeFecha"));
		form.add(desde);
		
		DateTimeField hasta = new DateTimeField("hastaFecha", ZoneId.systemDefault());
		hasta.setModel(new PropertyModel<OffsetDateTime>(this, "hastaFecha"));
		form.add(hasta);
		
		TextField<String> citaJurisprudencia = new  TextField<>("citaJurisprudencia");
		citaJurisprudencia.setModel(new PropertyModel<String>(this, "citaJurisprudencia"));
		form.add(citaJurisprudencia);
		
		TextField<String> referenciaNormativa = new  TextField<>("referenciaNormativa");
		referenciaNormativa.setModel(new PropertyModel<String>(this, "referenciaNormativa"));
		form.add(referenciaNormativa);

		
		form.add(new AjaxLink<Void>("simple") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire(new AdvancedSearchClickEvent(target));
			}
			public boolean isVisible() {
				return true;
			}
		});
		
		form.add(new AjaxButton("submit") {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				fire(new AdvancedSearchEvent(target, getFilters()));
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getBeforeHandler(Component component) {
						return "document.getElementById('"
								+component.getMarkupId()
								+"').innerHTML = '<span class=\"far fa-sync fa-spin fa-fw spinning\"></span> "
								+SumarioSearchForm.this.getLabel("working").getObject() +"'";
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		});
		
		add(form);
	}	
	
	private Map<String, Object> getFilters() {
		Map<String, Object> filters = new HashMap<>();
		
		Attribute attribute;

		
		if (texto!=null) {
			filters.put("text",  texto);
		}

		if (titulo!=null) {
			attribute = getAttribute("titulo");
			filters.put(attribute.getAlias(), 
				new AttributeFilter(attribute, 
				titulo));
		}

		if (desdeFecha!=null || hastaFecha!=null) {
			attribute = getAttribute("fecha");
			SolrDateRangeFilter range = new SolrDateRangeFilter(
				attribute,
				desdeFecha,
				hastaFecha
			); 
			filters.put(attribute.getAlias(),
				range);
		}
		
		if (citaJurisprudencia!=null) {
			attribute = getAttribute("citajurisprudencia");
			filters.put(attribute.getAlias(), 
				new AttributeFilter(attribute, 
						citaJurisprudencia));
		}
		
		if (referenciaNormativa!=null) {
			attribute = getAttribute("referencia");
			filters.put(attribute.getAlias(), 
				new AttributeFilter(attribute, 
				referenciaNormativa));
		}

		filters.put("tipodocumento", 
			new ClassifierFilter(
				getClassifier("tipodocumento"), 
				getTipo("Sumario")));
		
		return filters;
	}
	
	private DataSet getDataSet(String alias) {
		return  getContentDao().findDataSetByAlias(alias, getDomain().getId());
	}
	
	private DataSetMember getTipo(String value) {
		DataSetMember member =  getContentDao()
			.findMemberByValue(getDataSet("tipo"), 
				value);
		return member;
	}
	
	private Attribute getAttribute(String name) {
		List<Attribute> attributes = getContentDao().getAttributes(name, 
			(long)getDomain().getId());
		Attribute attribute = attributes.size()==1
			? attributes.get(0)
			: null;		
		return attribute;
	}
	
	private Classifier getClassifier(String name) {
		List<Classifier> classifiers = getContentDao().getClassifiers(name, 
			(long)getDomain().getId());
		Classifier classifier = classifiers.size()==1
			? classifiers.get(0)
			: null;	
		return classifier;
	}
		

	
}
