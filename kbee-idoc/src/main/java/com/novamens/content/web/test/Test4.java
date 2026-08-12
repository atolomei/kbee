package com.novamens.content.web.test;


import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree.State;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeNode;
import com.novamens.kbee.wicket.markup.html.tree.TreeNodeSelection;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.dataset.DataSetNode;
import kbee.web.dataset.DataSetTreeProvider;
import kbee.web.eform.EFormViewer;
import kbee.web.page.ApplicationPage;

public class Test4 extends ApplicationPage<Void> {
				
	
	private static final long serialVersionUID = 1L;


	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(Test3.class.getName());
	
	AbstractTree<TreeNode<DataSetMember>> tree;
	
	ResourceReference css  = new CssResourceReference(EFormViewer.class, "eform-viewer-v1.css");
	
	@SuppressWarnings("serial")
	public Test4(PageParameters parameters) {
		
		DataSet dataset = getDataSet(parameters);
		
	   
		ITreeProvider<TreeNode<DataSetMember>> provider = new DataSetTreeProvider(dataset);
		
	   
		tree = new DefaultNestedTree<TreeNode<DataSetMember>>("tree", provider) {
			@Override
			protected Component newContentComponent(String id, IModel<TreeNode<DataSetMember>> node)
			{
				return new Folder<>(id, this, node) {
					@Override
					protected void onClick(Optional<AjaxRequestTarget> targetOptional) {
						System.out.println("click");
					}
					protected Component newLabelComponent(String id, IModel<TreeNode<DataSetMember>> model)
					{
						return new Label(id, new Model<String>(model.getObject().getDisplayName()));
					}
					@Override
					protected boolean isClickable() {
						TreeNode<DataSetMember> t = getModelObject();
						boolean w = ((DataSetNode)t).isWriteable();
						return w;
					}
					@Override
					protected String getStyleClass() {
						String styleClass;

						TreeNode<DataSetMember> t = getModelObject();

							if (tree.getState(t) == State.EXPANDED)
							{
								styleClass = getOpenStyleClass();
							}
							else
							{
								styleClass = getClosedStyleClass();
							}

						return styleClass;
					}
				};
			}
		};
		
		add(tree);
	}
	
	public void onInitialize() {
		super.onInitialize();
	}
	
	private DataSet getDataSet(PageParameters parameters) {
		DataSet dataset = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			dataset = (DataSet) getContentDao().findModelObjectById(DataSet.class, id.toLong());
			if (dataset!=null && !dataset.getDomain().equals(getDomain())) {
				dataset = null;
			}
		}	
		return dataset;
	}
}
