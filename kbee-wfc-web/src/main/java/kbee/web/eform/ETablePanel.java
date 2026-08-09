package kbee.web.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EComboField;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.ETableField;
import com.novamens.content.form.ValueUpdated;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.form.KbeeEMemberComboField;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.TextField;

 
@SuppressWarnings("serial")
public class ETablePanel extends EFieldPanel<ETableField> {
	private static final long serialVersionUID = 1L;
	
	List<EFormField<?>> columns = new ArrayList<EFormField<?>>();
	List<ETableRow> rows = new ArrayList<ETableRow>();
	int focusRow = -1;
	private boolean updated = false;
	
	public class EAjaxCellFocusEvent extends AbstractWicketAjaxEvent   {
		private int rowIndex, colIndex;
		public EAjaxCellFocusEvent(AjaxRequestTarget target, int rowIndex, int colIndex) {
			super(target);
			this.rowIndex = rowIndex;
			this.colIndex = colIndex;
		}
		public int getRowIndex() {
			return rowIndex;
		}
		public void setRowIndex(int rowIndex) {
			this.rowIndex = rowIndex;
		}
		public int getColIndex() {
			return colIndex;
		}
		public void setColIndex(int colIndex) {
			this.colIndex = colIndex;
		}
	}
	
	public class ETableRow implements Serializable {
		private Map<String, String> data;
		private int index = 0;
		public ETableRow(Map<String, String> data, int index) {
			this.data = data == null ? new HashMap<String, String>() : data;
			this.index = index; 
		}
		public String getValue(EFormField<?> field) {
			return data.get(field.getName());
		}
		public void setValue(EFormField<?> field, String value) {
			data.put(field.getName(), value); 
		}
		public Map<String, String> getData() {
			return data;
		}
		public boolean isEmpty() {
			return data.isEmpty();
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int value) {
			this.index = value;
		}
	}
	
	public class EComboCellPanel extends ETableCellPanel {
		public EComboCellPanel(String id, EFormField<?> field, IModel<String> model) {
			super(id, "string-cell-fragment", field, model);
			add(new ChoiceField<DataSetMember>("field", getMemberModel(), getChoicesModel()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					updateModel();
					EComboCellPanel.this.onUpdate(target);
				}
				@Override
				public boolean isVisible() {
					return getMode()==1;
				}
				@Override
				protected void onKey(AjaxRequestTarget target, String jsKeycode) {
					updateModel();
					EComboCellPanel.this.onUpdate(target);
				}
				@Override
				public IModel<String> getLabel() {
					return null;	
				}
				@Override
				protected void onUpdate(DataSetMember oldvalue, DataSetMember newvalue) {
					setUpdated(true);
				}
			});
		}
		public String getDisplayValue() {
			String value = getModel().getObject();
			if (value==null) return "--";
			DataSetMember member = getContentDao().findMemberById(Long.valueOf(value));
			if (member==null) return "--";
			return member.getDisplayName();
		}
		public Component getInput() {
			((ChoiceField<?>)get("field")).onBeforeRender();
			return ((ChoiceField<?>)get("field")).getInput();
		}
		protected IModel<DataSetMember> getMemberModel() {
			return new IModel<DataSetMember>() {
				public DataSetMember getObject() {
					String stringid = getModel().getObject();
					if (stringid!=null) {
						return getContentDao().findMemberById(Long.valueOf(stringid));
					}
					return null;
				}
				public void setObject(DataSetMember member) {
					getModel().setObject(member!=null?String.valueOf(member.getId()):null); 
				}
			};
		}
		protected IModel<List<DataSetMember>> getChoicesModel() {
			return new IModel<List<DataSetMember>>() {
				@SuppressWarnings("unchecked")
				public List<DataSetMember> getObject() {
					return ((EComboField<DataSetMember>)getField()).getChoicesSource(getFormObject()).getValues();
				}
			};
		}
	}	
	
	public class EStringCellPanel extends ETableCellPanel {
		public EStringCellPanel(String id, EFormField<?> field, IModel<String> model) {
			super(id, "string-cell-fragment", field, model);
			add(new TextField<String>("field", model) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					updateModel();
					EStringCellPanel.this.onUpdate(target);
				}
				@Override
				public boolean isVisible() {
					return getMode()==1;
				}
				@Override
				protected void onKey(AjaxRequestTarget target, String jsKeycode) {
					updateModel();
					EStringCellPanel.this.onUpdate(target);
				}
				@Override
				public IModel<String> getLabel() {
					return null;	
				}
				@Override
				protected void onUpdate(String oldvalue, String newvalue) {
					setUpdated(true);
				}
			});
//			((TextField<?>)get("field")).addInputBehavior(new AjaxEventBehavior("blur") {
//				@Override
//				protected void onEvent(AjaxRequestTarget target) {
//					EStringCellPanel.this.onBlur(target);
//					
//				}
//				@Override
//				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
//					attributes.setEventPropagation(EventPropagation.BUBBLE); 
//					super.updateAjaxAttributes(attributes);
//				}	
//			});
		}
		public Component getInput() {
			((TextField<?>)get("field")).onBeforeRender();
			return ((TextField<?>)get("field")).getInput();
		}
	}
	
	public class ETableCellPanel extends Fragment {
		private EFormField<?> field;
		private IModel<String> model;
		protected int mode = 0;
		public ETableCellPanel(String id, String markupid, EFormField<?> field, IModel<String> model) {
			super(id, markupid, ETablePanel.this);
			setOutputMarkupId(true);
			this.model = model;
			this.field = field;
			add(new Label("value", () -> getDisplayValue()) {
				public boolean isVisible() {
					return getMode()==0;
				}
			});
			get("value").add(new AjaxEventBehavior("click") {
				protected void onEvent(AjaxRequestTarget target) {
					onFocus(target);
				}
			});
		}
		public EFormField<?> getField() {
			return field;
		}
		public IModel<String> getModel() {
			return model;
		}
		public String getDisplayValue() {
			String value = getModel().getObject();
			if (value==null) return "--";
			return value;
		}
		public String getValue() {
			return null;
		}
		public int getMode() {
			return mode;
		}
		public void setMode(int mode) {
			this.mode = mode;
		}
		public Component getInput() {
			return null;
		}
		public void onUpdate(AjaxRequestTarget target) {
		}
		public void onFocus(AjaxRequestTarget target) {
		}
		public void onBlur(AjaxRequestTarget target) {
		}
	}	
	
	public class ETableRowPanel extends Fragment {
		private ETableRow row;
		private List<ETableCellPanel> cells = new ArrayList<ETableCellPanel>();
		public class CellModel implements IModel<String> {
			EFormField<?> field;
			public CellModel(EFormField<?> field) {
				this.field = field;
			}
			public String getObject() {
				return getRow().getValue(getField());
			}
			public void setObject(String value) {
				getRow().setValue(getField(), value);
			}
			public EFormField<?> getField() {
				return field;
			}
		}
		public ETableRowPanel(String id, ETableRow row) {
			super(id, "row-fragment", ETablePanel.this);
			setOutputMarkupId(true);
			this.row = row;
			add(new ListView<EFormField<?>>("cell", getColumns()) {
				public void populateItem(ListItem<EFormField<?>> item) {
					ETableCellPanel cellpanel;
					if (item.getIndex()+1>cells.size()) {
						cellpanel = getCellPanel(item.getIndex(), item.getModelObject());
						cells.add(cellpanel);
					}
					else {
						cellpanel = cells.get(item.getIndex());
					}
					if (item.getModelObject().getCssClass()!=null) {
						item.add(new AttributeModifier("class", item.getModelObject().getCssClass()));
					}
					item.add(cellpanel);
					if (focusRow==getRow().getIndex()&&item.getIndex()==0) {
						cellpanel.setMode(1);
						RequestCycle requestCycle = RequestCycle.get();
						Optional<AjaxRequestTarget> target = requestCycle.find(AjaxRequestTarget.class);
						if (target.isPresent()) {
							target.get().focusComponent(cellpanel.getInput());
						}
					}
				}
			});
			WebMarkupContainer menu = new WebMarkupContainer("menu-container");
			menu.setOutputMarkupId(true);
			menu.add(getMenu());
			add(menu);
		}
		public ETableRow getRow() {
			return row;
		}
		public ETableCellPanel getCellPanel(int columnIndex, EFormField<?> field) {
			ETableCellPanel panel = null;
			if (field instanceof KbeeEStringField) {
				panel =  new EStringCellPanel("panel", field, new CellModel(field)) {
					public void onUpdate(AjaxRequestTarget target) {
						setMode(0);
						setFocus(columnIndex+1, target);
						ETableRowPanel.this.onUpdate(target);
					}
					public void onFocus(AjaxRequestTarget target) {
						setFocus(columnIndex, target);
					}
					public void onBlur(AjaxRequestTarget target) {
						setFocus(cells.size(), target);
					}
				};
			}
			if (field instanceof KbeeEMemberComboField) {
				panel =  new EComboCellPanel("panel", field, new CellModel(field)) {
					public void onUpdate(AjaxRequestTarget target) {
						setMode(0);
						setFocus(columnIndex+1, target);
						ETableRowPanel.this.onUpdate(target);
					}
					public void onFocus(AjaxRequestTarget target) {
						setFocus(columnIndex, target);
					}
					public void onBlur(AjaxRequestTarget target) {
						setFocus(cells.size(), target);
					}
				};
			}
			return panel;
		}
		protected void setFocus(int colIndex, AjaxRequestTarget target) {
			int rowIndex = getRow().getIndex();
			if (colIndex>=cells.size()) {
				colIndex = 0;
				rowIndex++;
			}
			fireScanAll(new EAjaxCellFocusEvent(target, rowIndex, colIndex));
		}
		@Override
		public void onInitialize() {
			super.onInitialize();
			add(new WicketEventListener<EAjaxCellFocusEvent>() {
				@Override
				public void onEvent(EAjaxCellFocusEvent event) {
					int colIndex=0;
					for (ETableCellPanel panel : cells) {
						if (event.getRowIndex()==getRow().getIndex() && colIndex++==event.getColIndex()) {
							panel.setMode(1);
							event.getRequestTarget().focusComponent(panel.getInput());
						}	
						else
							panel.setMode(0);
					}
					event.getRequestTarget().add(ETableRowPanel.this.getParent());
				}
			});
		}
		
		protected void onUpdate(AjaxRequestTarget target) {
		}
		protected Panel getMenu() {
			ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						getRows().remove(row);
						refresh(target);
					}	
					@Override
					public String getLabel() {	
						return ETablePanel.this.getLabel("menu.delete").getObject();
					}
			});
			return menu;
		}
	}

	public ETablePanel(String id, ETableField table, IModel<EFormData> data) {
		super(id, table, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addTable();
	}
	
	public void updateModel() {
		getData().setData(getField(), getRowsData());
		updated = false;
	}
	
	public void onDetach() {
		super.onDetach();
		focusRow = -1;
	}
	
	protected void setUpdated(boolean value) {
		if (!updated && value) {
			updated = true;
			setUpdatedField(new ValueUpdated(getData().getForm(), getField().getLabel(), null, null));
		}
	}
	
	protected void addTable() {
		
		setRows();
		
		addLabel();
		
		addHeader();
		
		addBody();
		
		add(new WicketEventListener<EAjaxCellFocusEvent>() {
			@Override
			public void onEvent(EAjaxCellFocusEvent event) {
				if (event.getRowIndex()==getRows().size()) {
					getRows().add(new ETableRow(new HashMap<String, String>(), getRows().size()));
					focusRow = getRows().size()-1;
					event.getRequestTarget().add(getContainer());
				}	
			}
		});
	}
	
	protected void addLabel() {
		getContainer().add(new Label("label", getComponent().getLabel()));
	}
	
	protected void addHeader() {
		getContainer().add(new ListView<EFormField<?>>("column", getColumns()) {
			public void populateItem(ListItem<EFormField<?>> item) {
				item.add(new Label("title", item.getModelObject().getLabel()));
				if (item.getModelObject().getCssClass()!=null) {
					item.add(new AttributeModifier("class", item.getModelObject().getCssClass()));
				}
			}
		});
	}
	
	protected void addBody() {
		getContainer().add(new ListView<ETableRow>("row", getRows()) {
			public void populateItem(ListItem<ETableRow> item) {
				item.setOutputMarkupId(true);
				item.add(new ETableRowPanel("panel", item.getModelObject()) {
					public void onUpdate(AjaxRequestTarget target) {
						getData().setData(getField(), getRowsData());
					}
				});
			}
		});
		getContainer().add(new AjaxLink<Void>("plus-link") {
			public void onClick(AjaxRequestTarget target) {
				getRows().add(new ETableRow(new HashMap<String, String>(), getRows().size()));
				focusRow = getRows().size()-1;
				target.add(getContainer());
			}
		});
	}
	
	protected List<ETableRow> getRows() {
		return rows;
	}
	
	protected List<EFormField<?>> getColumns() {
		if (this.columns.isEmpty())
			for (EFormComponent component : getField().getComponents()) {
				if (component instanceof EFormField<?>) {
					columns.add((EFormField<?>)component);
				}
			}
		return columns;
	}
	
	protected List<?> getRowsData() {
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		for (ETableRow row : rows) {
			if (!row.isEmpty()) {
				data.add(row.getData());
			}
		}
		return data;
	}
	
	@SuppressWarnings("unchecked")
	protected void setRows() {
		List<?> rows = (List<?>)getData().getData(getField());
		int index=0;
		if (rows!=null) {
			for (Object rowdata : rows) {
				this.rows.add(new ETableRow((Map<String, String>)rowdata, index++));
			}
		}
	}
	
	@Override
	protected String getCssClass() {
		String css = "row";
		if (getComponent().getCssClass()!=null) {
			css = getComponent().getCssClass();
		}
		return "".equals(css.trim()) ? null : css.trim();
	}
	
//	
//	private ContentDao getContentDao() {
//		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}