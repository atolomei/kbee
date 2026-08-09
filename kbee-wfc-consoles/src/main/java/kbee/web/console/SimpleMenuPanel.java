package kbee.web.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Site;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.panel.KBPanel;

public class SimpleMenuPanel<K,T> extends KBPanel {

	
	IModel<K> model;
	
	 List <MenuItemFactory<T>>  list;
	 IModel<String> title;
	 
	 public SimpleMenuPanel(String id, IModel<K> model) {
		 		this(id, model, null);
		}
	 
	 public SimpleMenuPanel(String id, IModel<K> model, List <MenuItemFactory<T>> list) {
		super(id);
		this.model=model;
		this.list=list;
	}
	
	
	 public void setTitle(IModel<String> title) {
		 this.title=title;
		 
	 }
	public void onInitialize() {
		super.onInitialize();

		if (list==null)
			list=new ArrayList<MenuItemFactory<T>>();
		
		ContextMenuPanel<T> menu=new ContextMenuPanel<T>("menu", null);
		for (MenuItemFactory<T> m:list)
			menu.addItem(m);
		add(menu);
		
		Label t=new Label("title", getTitle());
		t.setEscapeModelStrings(false);
		t.setVisible(getTitle()!=null);
		add(t);
		
	}

	

	public IModel<String> getTitle() {
		return this.title;
	}

	public void onDetach() {
		super.onDetach();
		
		if(model!=null) 
			model.detach();
		
		if (list!=null) {
			for (MenuItemFactory<T> m: list) {
				m.detach();
			}
		}
		
		
	}
	
	//private IModel<K> getModel() {
	//	return model;
	//}

	
	 public List <MenuItemFactory<T>> getItems() {
		 return list;
	 }
	 
	
	
}
