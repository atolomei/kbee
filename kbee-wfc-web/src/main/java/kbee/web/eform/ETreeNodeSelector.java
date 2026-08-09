package kbee.web.eform;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeNode;
import com.novamens.security.acl.Permission;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.dataset.DataSetNode;
import kbee.web.dataset.WriteableTreeProvider;

@SuppressWarnings("serial")
public class ETreeNodeSelector extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private AbstractTree<TreeNode<DataSetMember>> tree;
	private IModel<DataSet> model;
	private AccessStrategy strategy;

	public ETreeNodeSelector(String id, IModel<DataSet> model, AccessStrategy strategy) {
		super(id, model);
		this.model = model;
		this.strategy = strategy;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setVisible(false);
		
		Permission permission = getPermission(strategy);
		
		ITreeProvider<TreeNode<DataSetMember>> provider = new WriteableTreeProvider(
				model.getObject(), 
				getPermission(strategy));
		   
		tree = new DefaultNestedTree<TreeNode<DataSetMember>>("tree", provider) {
			@Override
			protected Component newContentComponent(String id, IModel<TreeNode<DataSetMember>> node)	{
				return new Folder<>(id, this, node) {
					protected Component newLabelComponent(String id, IModel<TreeNode<DataSetMember>> model)	{
						return new Label(id, new Model<String>(model.getObject().getDisplayName()));
					}
					protected boolean isClickable() {
						TreeNode<DataSetMember> t = getModelObject();
						boolean w = ((DataSetNode)t).isWriteable();
						w = ((DataSetNode)t).hasPermission(permission);
						return w;
					}
					@Override
					protected void onClick(Optional<AjaxRequestTarget> targetOptional) {
						TreeNode<DataSetMember> node = getModelObject();
						onSelect(targetOptional.get(), node.getObject());
					}
					@Override
					protected String getStyleClass() {
						String styleClass;
						TreeNode<DataSetMember> node = getModelObject();
						if (tree.getState(node) == State.EXPANDED)					{
							styleClass = getOpenStyleClass();
						}
						else{
							styleClass = getClosedStyleClass();
						}
						return styleClass;
					}
				};
			}
		};
			
		add(new AjaxLink<Void>("close") {
			public void onClick(AjaxRequestTarget target) {
				ETreeNodeSelector.this.setVisible(false);
				onClose(target);
			}
		});
			
		add(tree);
	}
	
	protected void onSelect(AjaxRequestTarget target, DataSetMember member) {
		
	}
	
	protected void onClose(AjaxRequestTarget target) {
		
	}
	
	protected Permission getPermission(AccessStrategy stragey) {
		if (AccessStrategy.Writeables.equals(stragey)) {
			return KbeePermission.WRITE;
		}
		else {
			if (AccessStrategy.ChildsEnabled.equals(stragey)) {
				return KbeePermission.CHILDS;
			}
		}
		return null;
	}
}