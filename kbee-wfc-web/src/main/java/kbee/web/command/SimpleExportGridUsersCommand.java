package kbee.web.command;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.command.CommandState;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.user.UserProfile;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.ResultSet;
import com.novamens.security.acl.Group;

import kbee.web.security.UsersQuery;


public class SimpleExportGridUsersCommand extends SimpleBaseExportGridCommand {

	static Logger logger = LogManager.getLogger(SimpleExportGridUsersCommand.class.getName());
	
	static public final String USEPARATOR = ", ";
	
	private int total = 0;

	public SimpleExportGridUsersCommand(UsersQuery query) {		
		super.setQuery(query);
	}
	
	
	@Override
	protected void executeExport() {

		BufferedWriter out = null;
		setState(CommandState.RUNNING);
		
		this.total = 0;
		
		try {
				if (getQuery()==null) {
					logger.error("query is null.");
					this.setState(CommandState.ERROR);
					this.setResultComments("query is null.");
					return;
				}
				
				
				if (! (getQuery() instanceof UsersQuery)) {
					logger.error("query must be a UsersQuery.");
					this.setState(CommandState.ERROR);
					this.setResultComments("query must be a UsersQuery.");
					return;
				}
				
				
				long start = System.currentTimeMillis();
				
				String name = "users-" + getSessionUser().getId().toString() + "-" + String.valueOf(start);
				
				File file = new File(getWorkingDir() + File.separator + name + ".csv");
				
				out = new BufferedWriter(new FileWriter(file));
		
				ResultSet results = getQuery().execute();
				
				total = results.size();
				
				if (total==0) {
					this.setState(CommandState.COMPLETED);
					this.setProgress(100);
					return;
				}
					
				int progress = 0;
				int counter  = 0;
				
				logger.info("Processing: " + String.valueOf(total));
				
				Map<String, Classifier> 	classifiers 		= new HashMap<String, Classifier>();
				List<Classifier> 			list_classifiers 	= new ArrayList<Classifier>();
				
				int n = 0;
				
				while (results.hasNext() && n<400) {
					try {
							DataSetMember person = (DataSetMember) results.next().getObject();
							for (Classification clasi: person.getClassification() ) {
								Classifier c = clasi.getClassifier();
								if (c.getState()==ObjectState.ENABLED) {
									if (!classifiers.containsKey(String.valueOf(c.getId()))) {
										classifiers.put(String.valueOf(c.getId()), c);
										list_classifiers.add(c);
									}
								}
							}
							
					} catch (RuntimeException  e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					}
					n++;
				}
	
				// init export file
				//
				
				StringBuilder header = new StringBuilder();

				header.append("Lastname"); // 1 
				header.append(USEPARATOR);

				header.append("Firstname"); // 2 
				header.append(USEPARATOR);

				header.append("username"); // 3 
				header.append(USEPARATOR); 

				header.append("Modified");  // 5 
				header.append(USEPARATOR);
				
				header.append("Modified by"); // 6 
				header.append(USEPARATOR);
				
				header.append("Status"); // 7 
				header.append(USEPARATOR);

				header.append("email"); // 8
				header.append(USEPARATOR);

				header.append("phone"); // 9 
				header.append(USEPARATOR);
				
				header.append("timezone"); // 10 
				header.append(USEPARATOR);
				
				header.append("General Permissions"); // 11 
				header.append(USEPARATOR);
				
				header.append("Groups"); // 12 
				header.append(USEPARATOR);
				
				header.append("Id"); // 13 
				header.append(USEPARATOR);

				header.append("Photo"); 	// 4 
				header.append(USEPARATOR);    



				// ------------------
				// Classifiers
				int m=0;
				for (Classifier cl: list_classifiers) {
					if (m>0)
						header.append(USEPARATOR);
					header.append(cl.getName());
					m++;
				}
				
				logger.info(header.toString());
				out.write(header.toString()+"\n");
				
				results = getQuery().execute();
	
				
				// Attributes are not exported
				//
				while (results.hasNext()) {
					
					try {
						
						PersonMember person = (PersonMember) results.next().getObject();
						
						StringBuilder str = new StringBuilder();
						
						// Lastname 1 
						//
						str.append(escape(person.getLastName()));
						str.append(USEPARATOR);
				
						// Firstname 2
						//
						str.append(escape(person.getFirstName()));
						str.append(USEPARATOR);
						
						// Username 3
						//
						try {
							str.append(escape(person.getProfile(UserProfile.class).getUser().getUserName()));
						}
						catch (Exception e) {
							str.append(escape(e.getClass().getSimpleName()));
						}
						str.append(USEPARATOR);
										
						// Modified 5 
						//
						OffsetDateTime modi = person.getLastModifiedOffsetDateTime();
						if (modi!=null)
							str.append(escape(dateformat.format(modi)));
						else
							str.append("");
						str.append(USEPARATOR);
						
						// Modified User 6 
						//
						if (person.getLastModifiedUser()!=null) 
							str.append(person.getLastModifiedUser().getFirstLastName());
						else
							str.append("");
						str.append(USEPARATOR);
	
						// Status 7
						//
						str.append(escape(person.getState().getLabel()));
						str.append(USEPARATOR);

						// Email 8
						//
						str.append(escape(person.getEmail()));
						str.append(USEPARATOR);

						// Phone 9
						//
						str.append(escape(person.getPhone()));
						str.append(USEPARATOR);

						// TimeZone 10
						// 
						try {
							str.append(escape(person.getProfile(UserProfile.class).getUser().getTimeZone()));
						}
						catch (Exception e) {
							str.append(escape(e.getClass().getSimpleName()));
						}
						str.append(USEPARATOR);
						
						
						
						// General Permissions 11						
						try {
							Set<Group> set = person.getProfile(UserProfile.class).getUser().getGroups();
							StringBuilder gp = new StringBuilder();
							for (Group g: set) {
								if (g.isCanonical()) {
									if (gp.length()>0) 
										gp.append(" | ");
									gp.append(g.getDisplayName());
								}
							}
							str.append(escape(gp.toString()));
						} catch (Exception e) {
								logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
								str.append(escape(e.getClass().getSimpleName()));
						}
						str.append(USEPARATOR);

						

						// Groups 12
						try {
							Set<Group> set = person.getProfile(UserProfile.class).getUser().getGroups();
							StringBuilder gp = new StringBuilder();
							for (Group g: set) {
								if (!g.isCanonical()) {
									if (gp.length()>0) 
										gp.append(" | ");
									gp.append(g.getDisplayName());
								}
							}
							str.append(escape(gp.toString()));
						} catch (Exception e) {
								logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
								str.append(escape(e.getClass().getSimpleName()));
						}
						str.append(USEPARATOR);

						
						// Id 13
						//
						try {
							str.append(escape(person.getProfile(UserProfile.class).getUser().getId().toString()));
						} catch (Exception e) {
							logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
							str.append(escape(e.getClass().getSimpleName()));
						}
						str.append(USEPARATOR);
				
						
						// Photo 4
						//
						str.append(escape(person.getPhoto()!=null?person.getPhoto().getName():""));
						str.append(USEPARATOR);
				
						// Classifiers 
						//
						int r=0;
						
						for (Classifier cl: list_classifiers) {
							String s = getClassification(person, cl);
							if (r>0)
								str.append(USEPARATOR);
							str.append(s);
							r++;
						}
						
						logger.info(str.toString());
						out.write(str.toString()+"\n");
						
						
					} catch (Exception  e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					}
					finally {
						counter++;
						if (total>0) 
							progress = 100 * counter/total;
						this.setProgress(progress);
					}
				}
				
				if (out!=null)
					out.close();
	
				sendEmail(file);
				
				setState(CommandState.COMPLETED);
				setProgress(100);
	
		}
		catch (Throwable e) {
				logger.error(e.getStackTrace());
				this.setResult(e.getClass().getSimpleName());
				this.setResultDetails(e.getMessage());
				setState(CommandState.ERROR);
				
				return;
		
		} finally {
			if (out!=null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e.getStackTrace());
				}
				
			}
			setDateTerminated(OffsetDateTime.now());
		}
	}

	@Override
	public String getTitle() {
		return this.getClass().getSimpleName();
	}
	
	
	protected String escape(String str) {
		if (str==null)
			return "";
		return str.replace(",", "");
	}

	
}
