package com.novamens.content.web.security.markup;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.security.IQLRule;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.console.grid.DatePropertyColumn;

import com.novamens.wicket.markup.html.form.StaticField;

@SuppressWarnings("serial")
public class RuleStatusPanel extends ModelPanel<IQLRule> {
	private static final long serialVersionUID = 1L;
	
	public class CommandModel implements IModel<Command> {
		private Command command;
		private Long commandId;
		public CommandModel(Command command) {
			setObject(command);
		}
		public void setObject(Command command) {
			this.commandId = (Long)command.getId();
			this.command = command;
		}
		public Command getObject() {
			if (command==null) {
				command = ServiceLocator.getService(CommandService.class).getCommand(commandId);
			}
			return command;
		}
		public void detach() {
			this.command = null;
		}
	}
	
	public class CommandsProvider extends SortableDataProvider<Command, String> {
		public Iterator<Command> iterator(long first, long count) {
			ArrayList<Command> iteration = new ArrayList<Command>();
			Iterator<Command> iterator = getCommands().listIterator((int)first);
			int i = 0;
			while (i++<count) {
				iteration.add(iterator.next());
			}
			return iteration.iterator();
		}	
		public IModel<Command> model(Command object) {
			return new CommandModel(object);
		}
		public long size() {
			return getCommands().size();
		}
	}

	public RuleStatusPanel(String id, IModel<IQLRule> model) {
		super(id, model);
	}
	
	

	public void onDetach() {
		super.onDetach();
		this.commands=null;
	}

	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("commands-container")==null) {
			addForm();
			addTable();
		}
	}
	
	List<Command> commands;
	
	private List<Command> getCommands() {
		
		if (commands!=null)
			return commands;
		
		commands = new ArrayList<Command>();
		for (Command command : ServiceLocator.getService(CommandService.class).getCommands().values()) {
			if (CommandState.RUNNING.equals(command.getState()) && 
				command.getParameter("rule")!=null && 
				getModelObject().getId().equals(command.getParameter("rule"))) {
				commands.add(command);
			}
		}
		return commands;
	}
	
	protected List<IColumn<Command, String>> getColumns() {
		
		List<IColumn<Command, String>> columns = new ArrayList<IColumn<Command, String>>();
		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null) zid=ZoneId.systemDefault().getId();

		columns.add(new PropertyColumn<Command, String>(new StringResourceModel("name", this, null), "name") {
			@Override
			public String getCssClass() {
				return "col-xs-2";
			}
		});
		
		columns.add(new DatePropertyColumn<Command, String>(new StringResourceModel("started", this, null), "dateStarted", ZoneId.of(zid), user.getLocale(), false) {
			@Override
			public String getCssClass() {
				return "col-xs-2";
			}
		});
		
		columns.add(new PropertyColumn<Command, String>(new StringResourceModel("progress", this, null), "progress") {
			@Override
			public String getCssClass() {
				return "col-xs-2";
			}
		});
		
		return columns;
	}	
		
	private void addForm() {
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL) {
			@Override
			public boolean isVisible() {
				return getCommands().isEmpty();
			}
		};
		
		form.add(new StaticField<String>("status", new Model<String>("Enabled")));
				
		add(form);
	}
	
	private void addTable() {
		
		DataTable<Command, String> table = new DataTable<Command, String>("commands", getColumns(), new CommandsProvider(), 40);
		
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (CommandsProvider)table.getDataProvider()));
		
		WebMarkupContainer container = new WebMarkupContainer("commands-container") {
			@Override
			public boolean isVisible() {
				return !getCommands().isEmpty();
			}
		};
		
		WebMarkupContainer tablecontainer = new WebMarkupContainer("table-container");
		
//		if (this.adjust_height) {
//			container.add(new AjustableHeightBehavior(180));
//			tablecontainer.add(new AjustableHeightBehavior(170));
//		}
		
		tablecontainer.add(table);
		container.add(tablecontainer);
		
//		container.add(new com.novamens.wicket.markup.html.repeater.util.NavigationToolbar("navigation", table, true) {
//			protected String getDownloadFilename() {
//				return getDownloadFileName();
//			}
//			@Override
//			protected File getFile() {
//				try {
//					return getDownloadFile();
//				} catch (Exception e) {
//					logger.error(e.getClass().getName(), e);
//					return null;
//				}
//			};
//		});
		
		add(container);
	}
	
	private KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
