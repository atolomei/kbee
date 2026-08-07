package com.novamens.content.web.content.markup;



import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.DomainType;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.nav.WorkspaceBC;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.util.Property;


/**
 *  WorkspaceConsole -> Batch Delete 
 * 
 * Sirve para acciones genéricas sobre Content
 * 
 * 
 * DataSetMembersConsole. 
 */
@SuppressWarnings("serial")
public class GenericBatchActionPage extends ApplicationPage<Content> {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<Content>> selection;

	private List<BCElement> blist;
	
	
	public GenericBatchActionPage(List<IModel<Content>> selection) {
		setSelection(selection);
	}
	
	Panel bcrumb;
	
	public GenericBatchActionPage(List<IModel<Content>> selection, Panel bcrumb) {
		setSelection(selection);
		this.bcrumb=bcrumb;
	}
	
	
	public IModel<String> getReturnLabel() {
		return new StringResourceModel("back", this, null);
	}

	public String getIcon() {
		return null;
	}
	
	public String getSrcPage() {
		return "SRC";
	}

    @Override
	public String getPageHelpKey() {
		return super.getPageHelpKey()+"-"+ getSrcPage() + "-" + getTitle().getObject();
	}
	
	@Override
	public IModel<String> getPageTitle() {
		return new StringResourceModel("page-title", this, null);
	}
	
	public IModel<String> getTitle() {
		return new StringResourceModel("title", this, null);
	}
	
	public IModel<String> getType() {
		return new StringResourceModel("type", this, null);
	}

	public void onReturn() {
	}
	
	
	
	@Override 
	public void onInitialize() {
		super.onInitialize();
		
		addComponents();
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		for (IModel<Content> model : getSelection())
			model.detach();
		
		if (blist!=null)
			for (BCElement e: blist) 
				e.detach();
	}
	
	protected String executeAction(IModel<Content> model){
		return "";
	}
	
	protected Panel newActionPanel() {
		
		return new GenericBatchActionPanel("editor", getSelection()) {
			@Override
			public void onReturn() {
				GenericBatchActionPage.this.onReturn();
			}
			@Override
			public IModel<String> getReturnLabel() {
				return GenericBatchActionPage.this.getReturnLabel();
			}
			@Override
			protected String executeAction(IModel<Content> model){
				return GenericBatchActionPage.this.executeAction(model);
			}
			@Override
			protected String getExecuteButtonCss() {
				return GenericBatchActionPage.this. getExecuteButtonCss();
			}
			@Override
			protected IModel<String> getExecuteButtonLabel() {
				return GenericBatchActionPage.this.getExecuteButtonLabel();
			}
			@Override
			protected List<Property<Content>> getSelectionProperties() {
				return GenericBatchActionPage.this.getSelectionProperties();
			}
			@Override
			protected Page getPage(IModel<Content> model) {
				return GenericBatchActionPage.this.getPage(model);
			}
		};
	}
	
	
	
	protected List<Property<Content>> getSelectionProperties() {
	
		List<Property<Content>> properties = new ArrayList<Property<Content>>();
		
		properties.add(new Property<Content>() {
			public IModel<String> getLabel() {
				return new StringResourceModel("grid.title", GenericBatchActionPage.this, null);
			}
			public IModel<String> getValue(IModel<Content> model) {
				return new PropertyModel<String>(model, "title");
			}
			public String getCss() {
				return "col-lg-4";
			}
			public boolean isLink() {
				return true;
			}
		});
		
	
		if (getDomain().getDomainType()!=DomainType.EXPRESS) {
			
			properties.add(new Property<Content>() {
				public IModel<String> getLabel() {
					return new StringResourceModel("grid.task", GenericBatchActionPage.this, null);
				}
				public IModel<String> getValue(IModel<Content> model) {
					WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
					String taskname = workflowService==null || workflowService.getTask()==null ? "" : workflowService.getTask().getName();
					String procedurename;
					if (workflowService!=null && workflowService.getTask()!=null &&
							workflowService.getContext() !=null &&
							workflowService.getContext().getProcedure() !=null) {
						procedurename = workflowService.getContext().getProcedure().getCode() + ".  ";
					}
					else
						procedurename = "";
					return new Model<String>(procedurename + taskname);
				}
				public String getCss() {
					return "col-lg-3";
				}
			});
			
			
			properties.add(new Property<Content>() {
				public IModel<String> getLabel() {
					return new StringResourceModel("grid.class", GenericBatchActionPage.this, null);
				}
				public IModel<String> getValue(IModel<Content> model) {
					return new PropertyModel<String>(model, "contentTemplate.name");
				}
				public String getCss() {
					return "col-lg-2";
				}
			});
		}
		
		
		return properties;
	}
	
	protected Page getPage(IModel<Content> model) {
		return null;
	}

	protected IModel<String> getExecuteButtonLabel() {
		return new StringResourceModel("send", GenericBatchActionPage.this, null);
	}

	protected String getExecuteButtonCss() {
		return "btn btn-sm btn-primary";
	}

	protected void setSelection(List<IModel<Content>> selection) {
		this.selection = selection;
	}

	protected List<IModel<Content>> getSelection() {
		return selection;
	}
	
	
	public void setBreadCrumbPanel(Panel panel) {
		bcrumb =panel;
	}
	
	
	public void setBreadCrumb(List<BCElement>  list) {
		this.blist=list;
		
	}
	private void addComponents() {

		
		setTopNavigation(getMainTopbar()); 	
		setMenu(getMainLaternalMenu());
		
		setPageDescription(getPageTitle());
		
		PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>(null);
		panel.setTitle(getTitle());
		

		
	
		
		//bc.addElement(new TasksDropdownMenuBC());
		//bc.addElement(new WorkspaceBC());
		//bc.addElement(new BCElement(getTitle()));
		
		//panel.setBreadcrumbPanel(bc);
		
		if (this.bcrumb!=null)
			panel.setBreadcrumbPanel(bcrumb);
		else {
			MenuBreadCrumbPanel<Void>  bc = new MenuBreadCrumbPanel<Void>();
			for (BCElement b: this.blist)
				bc.addElement(b);
			bc.addElement(new BCElement(getTitle()));
			panel.setBreadcrumbPanel(bc);
		}
		
		
		setSearchPanel(false);
		setClearAllSearch(false);
		setAdvancedSearch(false);
		setSuggester(false);
		
		setPageContentHeader(panel);

		//add(new Label("action-title", getTitle()));
		//WebMarkupContainer icon = new WebMarkupContainer("icon");
		//icon.setVisible(getIcon()!=null);
		//icon.add(new AttributeModifier("class", getIcon()));
		//add(icon);
		
		add(newActionPanel());
		
		//if (blist==null)
		//	add(new InvisiblePanel("bc2"));
		//else {
		//	MenuBreadCrumbPanel<Void> panel = new MenuBreadCrumbPanel<Void> ("bc2", null, blist);
		//	add(panel);
		//}
		
	}
}

