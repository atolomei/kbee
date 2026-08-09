package kbee.web.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.browser.BreadcrumbToolbarItem;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.tree.TreeNodeSelection;
import com.novamens.wicket.util.AjaxBCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.dataset.DataSetNode;

@SuppressWarnings("serial")
public class TreeBreadcrumbToolbarItem extends BreadcrumbToolbarItem {
	private static final long serialVersionUID = 1L;

	DataSetNode node;
	
	public TreeBreadcrumbToolbarItem(BaseBrowser<?> browser, Align align, DataSetNode node) {
		super(browser, align);
		setOutputMarkupId(true);
		//add(new AttributeModifier("style", "font-weight: bold;color: #131414;font-size: 14px;"));
//		setBreadcrumb(new ArrayList<>());
		setNode(node);
		setBreadcrumb(getNode()!=null ? getNode().getNodesPath() : new ArrayList<>());
	}
	
	public DataSetNode getNode() {
		return node;
	}

	public void setNode(DataSetNode node) {
		this.node = node;
	}
	
	public String getRootDisplayName() {
		return getBrowser().getConsoleDisplayName().toUpperCase();
	}

	protected void addListeners() {
		add(new WicketEventListener<TreeNodeSelection<DataSetNode>>() {
			@Override
			public void onEvent(TreeNodeSelection<DataSetNode> event) {
				setNode(event.getNode());
				setBreadcrumb(getNode()!=null ? getNode().getNodesPath() : new ArrayList<>());
				if (event.getRequestTarget()!=null) {
					event.getRequestTarget().add(TreeBreadcrumbToolbarItem.this);
				}
			}
		});
	}
	
	protected void setBreadcrumb(List<DataSetNode> path) {
		MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<>("breadcrumb");
		bc.addElement(new AjaxBCElement<DataSetNode>(null, () -> getRootDisplayName()) {
			public void onClick(AjaxRequestTarget target) {
				TreeBreadcrumbToolbarItem.this.fireScanAll(new TreeNodeSelection<DataSetNode>(target, null));
			}
		});
		for (DataSetNode node : path) {
			bc.addElement(new AjaxBCElement<DataSetNode>(new Model<DataSetNode>(node), new Model<String>(node.getDisplayName())) {
				public void onClick(AjaxRequestTarget target) {
					TreeBreadcrumbToolbarItem.this.fireScanAll(new TreeNodeSelection<DataSetNode>(target, getModel()));
				}
			});
		}
		super.setPanel(bc);
	}
}
