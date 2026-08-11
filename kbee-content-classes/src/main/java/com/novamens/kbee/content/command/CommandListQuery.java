package com.novamens.kbee.content.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;

import com.novamens.indexer.query.PhoneticTextFilter;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.command.CommandService;
import com.novamens.service.ServiceLocator;

public class CommandListQuery implements Query {

	private static final long serialVersionUID = 1L;

	private Map<String, Object> parameters;

	
	public CommandListQuery() {
	}

	
	public CommandListQuery(Domain domain) {
			getParameters().put("domain", domain.getId());
	}


	
	@Override
	public ResultSet execute() {
		
		List<Command> xlist;
		if (getParameters().containsKey("domain"))
				xlist=((CommandService) ServiceLocator.getService(CommandService.class)).getCommandsAsList((Serializable) getParameters().get("domain"));
		else
			xlist=((CommandService) ServiceLocator.getService(CommandService.class)).getCommandsAsList();
		
		List<Command> list;
		
		list = xlist;
		
		String asc = (String) getParameters().get("ascending");
		
		if (asc==null)
				asc="true";
			
		if (getParameters().containsKey("sort")) {
				String sort = (String) getParameters().get("sort");
				if (sort.equals("name")) {
					sortName(list, asc);
				}
				else if (sort.equals("status")) {
					sortStatus(list, asc);
				}
				else  {
					sortStart(list, asc);
				}
		}
		else
			sortStatus(list, asc);
		
		List<CommandProxy> plist = new ArrayList<CommandProxy>();
		
		String text = null;
		
		Object o = getParameters().get("text");
		
		if (o instanceof String) {
			text = (String) getParameters().get("text");
		}
		else if (o instanceof PhoneticTextFilter && ((PhoneticTextFilter) o).getValue()!=null) {
			text = ((PhoneticTextFilter) o).getValue().toString();
		}
		
		boolean is_text = (text!=null);

		for (Command c: xlist) {
			if (!is_text || (c.getDisplayName()!=null && c.getDisplayName().toLowerCase().trim().contains(text.toLowerCase().trim()))) {
				plist.add(new CommandProxy(c));
			}
				
		}
		return new CommandListResultSet(plist);
	}

	
	@Override
	public QueryBuilder getBuilder() {
		return null;
	}

	
	@Override
	public Map<String, Object> getParameters() {
		if (parameters==null) 
			parameters = new HashMap<String, Object>();
		return parameters;
	}


	@Override
	public void setParameters(Map<String, Object> parameters) {
		this.parameters=parameters;
	}
	

	public void setParameter(String name, Object value) {
	}

	@Override
	public void setOptions(Map<String, FacetOptions> options) {
	}

	@Override
	public String getTitle() {
		return null;
	}
	
	@Override
	public List<Facet> getFacets() {
		return new ArrayList<Facet>();
	}

	
	
	private void sortStatus(List<Command> list, final String order) {
		Collections.sort(list, new	 Comparator<Command>() {
			@Override
			public int compare(Command a, Command b) {
				try {
						if (a.getState() == CommandState.RUNNING && b.getState() != CommandState.RUNNING)
							return (order.equals("true"))?-1:1;
						
						if (b.getState() == CommandState.RUNNING && a.getState() != CommandState.RUNNING)
							return (order.equals("true"))?1:-1;
		
						if (a.getState() == b.getState()) {
						
							if (order.equals("true"))
								return b.getDateStarted().compareTo(a.getDateStarted());
							else
								return a.getDateStarted().compareTo(b.getDateStarted());
						}
		
						if (order.equals("true"))
							return a.getState().getLabel().compareToIgnoreCase(b.getState().getLabel());
						else
							return b.getState().getLabel().compareToIgnoreCase(a.getState().getLabel());
						
				} catch (RuntimeException e) {

					return 0;
				}
			}
		});
		
	}
	

	private void sortStart(List<Command> list, final String order) {
		
		
		Collections.sort(list, new	 Comparator<Command>() {
			@Override
			public int compare(Command a, Command b) {
				try {
						if (order.equals("true"))
								return a.getDateStarted().compareTo(b.getDateStarted());
						else
							return b.getDateStarted().compareTo(a.getDateStarted());
								
						
				} catch (RuntimeException e) {
					return 0;
				}
			}
		});
		
	}

	
	private void sortName(List<Command> list, final String order) {
		
			Collections.sort(list, new	 Comparator<Command>() {
				@Override
				public int compare(Command a, Command b) {
					try {
							if (a.getName()==null && b.getName()==null)
								return 0;
					
							else if (a.getName()==null && b.getName()!=null)
								return (order.equals("true"))?-1:1;
							
							else if (a.getName()!=null && b.getName()==null)
								return (order.equals("true"))?1:-1;
							
							if (order.equals("true"))
								return a.getName().compareToIgnoreCase(b.getName());
							else
								return b.getName().compareToIgnoreCase(a.getName());
								
							
					} catch (RuntimeException e) {
						return 0;
					}
				}
			});
			
	}
	

}
