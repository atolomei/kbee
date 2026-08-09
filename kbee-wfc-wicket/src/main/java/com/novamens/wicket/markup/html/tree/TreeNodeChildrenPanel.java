package com.novamens.wicket.markup.html.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.model.ListModel;


public class TreeNodeChildrenPanel<T extends TreeNode> extends ModelPanel<T> {
			
 	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TreeNodeChildrenPanel.class.getName());

	
 	IModel<T> model;

	public TreeNodeChildrenPanel(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
		setModel(model);

	}

	
	public List<TreeNode> getList() {
	
		if (getModel()==null)
			return new ArrayList<TreeNode>();
		
		if (getModel().getObject()==null)
			return new ArrayList<TreeNode>();

		
		if (getModel().getObject().children()==null)
			return new ArrayList<TreeNode>();
		
		Enumeration<? extends TreeNode> e= getModel().getObject().children();

		List<TreeNode> list = new ArrayList<TreeNode>();

		while (e.hasMoreElements())
			list.add(e.nextElement());
				
		return list;
	}
	
	@SuppressWarnings("serial")
	public void onInitialize() {
		super.onInitialize();

		ListView<T> lv = new ListView<T>("node", new ListModel<T>(new Model<Panel>(this), "list")) {
				@Override
				protected void populateItem(ListItem<T> item) {
						item.add( new TreeNodePanel<T>("treenode", item.getModel(), item.getIndex()));
				}
		};
		add(lv);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}
	
	public IModel<T> getModel() {
		return model;
	}

	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
}
