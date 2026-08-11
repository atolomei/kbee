package com.novamens.content.web.sql.markup;

import java.util.Iterator;


import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import com.novamens.content.entity.Person;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.event.Event;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

@Deprecated
public class SQLNavigationBar extends GlobalNavigationBar<Person> {

	private static final long serialVersionUID = 1L;

	public SQLNavigationBar(String id) {
		super(id);
		setOutputMarkupId(true);
		super.setIsAlerts(false);
		
	}
	
	
	@Override
	protected void addComponents() {
		super.addComponents();
		add(newInfoPanel());
	}
	
 	protected Component newInfoPanel()  {
 		return new AjaxLink<Object>("info-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire(new InfoClickEvent(target));
			}
		};
 	}


	@Override
	public void navigate() {
		
	}


	@Override
	public void onStartWorkflow() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setEditor(Editor<?> editor) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isFromContentBase() {
		// TODO Auto-generated method stub
		return false;
	}
	
	 
   



}
