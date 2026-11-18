package kbee.pjsf.web;

import com.novamens.wicket.markup.html.form.TextField;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class AllSearchForm extends Panel {
	private static final long serialVersionUID = 1L;


	@Getter
	@Setter
	private String caratula;
	
	private Form<Void> form = new Form<Void>("form");


	public AllSearchForm(String id) {
		super(id);
		addForm();
	}

	public AllSearchForm(String id, PageParameters parameters) {
		super(id);
	}

	public void onInitialize() {
		super.onInitialize();
		

	}
	
	private void addForm() {


		TextField<String> caratula = new  TextField<String>("caratula");
		caratula.setModel(new PropertyModel<String>(this, "caratula"));

		form.add(caratula);
		
		
		add(form);
	}	
		

	
}
