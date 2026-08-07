package com.novamens.content.web.content.markup;



import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
// import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeValueMember;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.DateField;

import kbee.web.event.wicket.EditorEvent;



/**
 * 
 * The String version of the Date will use the Wicket Locale. 
 * It should be the Session User's Locale:
 * 
 *  "d MMM yyyy" for Spa
 *  "MMM d yyyy" for Eng
 *
 * @param <T>
 */



/**
 * 
 * Date Classifiers are no longer used. 
 * Attribute Date must be used instead 

 * @param <T>
 */
@Deprecated
@SuppressWarnings("serial")
public class DateEditor<T extends Content> extends MembersEditor<T>  {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DateEditor.class.getName());
	
	
	private static final long serialVersionUID = 1L;
	
	private boolean leavevalues = false;
	
	private List<Date> dates = new ArrayList<Date>();
	private Date date;
														
	
	public class SelectorFragment extends Fragment {
		public SelectorFragment(String id) {
			super(id, "selector-fragment", DateEditor.this);
			
			add(new DateField("member", new PropertyModel<Date>(DateEditor.this, "date")) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
			
					// Dates are converted to the TimeZone of the Domain 
					Date date = getValue();
					String tz = getDomain().getTimeZone();
					ZoneId domain_zoneid;
					if (tz==null)
						tz="Z";
					try { 
						domain_zoneid=ZoneId.of(getDomain().getTimeZone());
						
					} catch (Exception e) {
						logger.error(e);
						domain_zoneid=ZoneId.of("Z");
					}
					
					Calendar cal=Calendar.getInstance();
					cal.setTime(date);
					LocalDate ldate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));
					ZonedDateTime zdt = ldate.atStartOfDay(domain_zoneid);
					
					OffsetDateTime dateTime = OffsetDateTime.ofInstant(zdt.toInstant(), domain_zoneid);
					if (dateTime.getYear()<100) {
						dateTime = OffsetDateTime.of(dateTime.getYear()+2000,
								 dateTime.getMonthValue(),
								 dateTime.getDayOfMonth(),
								 dateTime.getHour(),
								 dateTime.getMinute(),
								 dateTime.getSecond(),
								 0,
								 ZoneOffset.from(dateTime));
								 // setValue(Date.from(dateTime.plusDays(2).toInstant()));
								 setValue(Date.from(dateTime.toInstant()));
					}
					addMember(getValue());

					
					/**
					OffsetDateTime dateTime = OffsetDateTime.ofInstant(date.toInstant(), domain_zoneid);
					
					
					OffsetDateTime first_date = OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.from(dateTime));
					if (dateTime.isBefore(first_date)) {
						if (dateTime.getYear()<70) {
								OffsetDateTime correctedDateTime = OffsetDateTime.of(dateTime.getYear()+2000,
																					 dateTime.getMonthValue(),
																					 dateTime.getDayOfMonth(),
																					 dateTime.getHour(),
																					 dateTime.getMinute(),
																					 dateTime.getSecond(),
																					 0,
																					 ZoneOffset.from(dateTime));
								addMember(Date.from(correctedDateTime.plusDays(2).toInstant()));
							}
							else if (dateTime.getYear()<100) {
								OffsetDateTime correctedDateTime = OffsetDateTime.of(dateTime.getYear()+1900,
																					 dateTime.getMonthValue(),
																					 dateTime.getDayOfMonth(),
																					 dateTime.getHour(),
																					 dateTime.getMinute(),
																					 dateTime.getSecond(),
																					 0,
																					 ZoneOffset.from(dateTime));
								// date comes in the Julian Calendar and it seems that there are 2 days of difference in the conversion
								addMember(Date.from(correctedDateTime.plusDays(2).toInstant()));
								//addMember(correctedDateTime.plusDays(2));
							}
					}
					else
						addMember(getValue());
						**/
					
					setValue(null);
					fireScanAll(new EditorEvent(target));
					target.focusComponent(getInput());
					target.add(DateEditor.this);
				}	
				@Override
				public boolean isEnabled() {
					return !getTemplate().isReadOnly();
				}
			});
			
			add(new WebMarkupContainer("relation-message") {
				public boolean isVisible() {
					return false;
				}
			});
			
			WebMarkupContainer leavevalues = new WebMarkupContainer("leavevalues-container") {
				@Override
				public boolean isVisible() {
					return isBatchClassification() && !isReadOnly();
				}
			};
																	
			leavevalues.add(new AjaxCheckBox("check", new PropertyModel<Boolean>(DateEditor.this, "leaveValues")) {
				protected void onUpdate(AjaxRequestTarget target) {
					if (getLeaveValues()) removeAllMembers();
					target.add(DateEditor.this.get("container"));
				}
			});
			
			WebMarkupContainer checklabel = new WebMarkupContainer("label");
			checklabel.add(new AttributeModifier("for", new Model<String>() {
				public String getObject() {
					return leavevalues.get("check").getMarkupId();
				}
			}));
			
			leavevalues.add(checklabel);
			
			add(leavevalues);
			
			add(new WebMarkupContainer("calculation-info") {
				public boolean isVisible() {
 					return false;
				}
			});
			
			
			IModel<String> errorModel = new Model<String>() {
				public String getObject() {
					return getError();
				}
			};
			add((new Label("error-message", errorModel) {
				public boolean isVisible() {
 					return getError()!=null;
				}
			}).setEscapeModelStrings(false) );
			
			add(new AjaxLink<Void>("close-link") {
				public void onClick(AjaxRequestTarget target) {
					setEditionEnabled(false);
					target.add(DateEditor.this);
				}
				
				@Override
				public boolean isVisible() {
					return false;
				}
			});
		}
	}
	
 
	public DateEditor(String id, IModel<ClassifierTemplate> templatemodel, int index) {
		super(id, templatemodel, index);
	}
	

	@Override
	public void setLeaveValues(boolean value) {
		this.leavevalues = value;
	}
	
	@Override
	public boolean getLeaveValues() {
		return leavevalues;
	}
	

	@Override
	public void updateModel() {
		
		if (!this.isUpdated() || getLeaveValues()) 
			return;
 	
		// Convert from Date to OffsetDateTime 
		//
		
		List<OffsetDateTime> list = getOffsetDates();
		
		getEditor().getModelObject().setValues(getClassifier(), list);
		setUpdatedPart(getClassifier().getName().toLowerCase());
		
		setUpdated(false);
	
	}
	
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	
	public List<Date> getDates() {
		return this.dates;
	}
	
	public List<OffsetDateTime> getOffsetDates() {
		
		List<OffsetDateTime> list = new ArrayList<OffsetDateTime>();
		
		for (Date date: getDates()) {
			/**
			 * From Date -> OffsetDateTime 
			 */
			Calendar ca=Calendar.getInstance();
			ca.setTime(date);
			
			String tz = getDomain().getTimeZone();
			ZoneId domain_zoneid;
			if (tz==null)
				tz="Z";
			try { 
				domain_zoneid=ZoneId.of(getDomain().getTimeZone());
				
			} catch (Exception e) {
				logger.error(e);
				domain_zoneid=ZoneId.of("Z");
			}
						
			Calendar cal=Calendar.getInstance();
			cal.setTime(date);
			
			LocalDate ldate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));
			
			
			ZonedDateTime zdt = ldate.atStartOfDay(domain_zoneid);
			
			
			OffsetDateTime dateTime = OffsetDateTime.ofInstant(zdt.toInstant(), domain_zoneid);			
							
			//OffsetDateTime dt = OffsetDateTime.of( ca.get(Calendar.YEAR), 
			//		 							   ca.get(Calendar.MONTH)+1, 
			//		 							  ca.get(Calendar.DAY_OF_MONTH), 0, 0, 0, 0, ZoneOffset.UTC
			//		 							  );
			list.add(dateTime);
		}
		return list;
	}


	/** ------------------------------------------------
	 */
	@Override
	public List<DataSetMember> getMembers() {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
	
		for (Date date : getDates()) {
			
			DataSetMember member = new KbeeValueMember(getClassifier().getDataSet());
			/**
			 * From Date -> OffsetDateTime 
			 */
			Calendar cal=Calendar.getInstance();
			cal.setTime(date);
			
			
			LocalDate ldate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));
			
			
			String tz = getDomain().getTimeZone();
			ZoneId domain_zoneid;
			if (tz==null)
				tz="Z";
			try { 
				domain_zoneid=ZoneId.of(getDomain().getTimeZone());
				
			} catch (Exception e) {
				logger.error(e);
				domain_zoneid=ZoneId.of("Z");
			}
			ZonedDateTime zdt = ldate.atStartOfDay(domain_zoneid);
			
			
			OffsetDateTime dateTime = OffsetDateTime.ofInstant(zdt.toInstant(), domain_zoneid);			
			
			logger.debug(dateTime.toString());
			member.setDateValue(dateTime );
			
			members.add(member);
		}
		return members;
	}

	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (getDates().isEmpty() && !isUpdated() && !getLeaveValues()) {
			setMembers(getClassifier());
		}
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	
	@Override
	protected void addMembersView() {
		
		WebMarkupContainer elementscont = new WebMarkupContainer("elements-container");
						
		elementscont.add( new AttributeModifier("class", new Model<String>() {
			@Override
			public String getObject() {
				if (isEditable())
					return "elements-container editable";
				else
					return "elements-container readonly";
			}
			
		}) {
			
		}); 
		
		
		((WebMarkupContainer) get("container")).add(elementscont);
			
		ListView<Date> ddd = new ListView<Date>("member", new PropertyModel<List<Date>>(DateEditor.this, "dates")) {
			public void populateItem(final ListItem<Date> item) {
				item.add(new Label("member-name", new Model<String>() {
					public String getObject() {
						
						Date date = item.getModelObject();
						
						
						// we have to convert this date to the local date 
						
						
						String value;
						if (item.getModelObject()!=null) {
							
							logger.debug(date.toString());

							
							if (getLocale().getLanguage().equals("es")) {
								SimpleDateFormat sf = new SimpleDateFormat("d MMM yyyy");
								//sf.setTimeZone(TimeZone.getTimeZone(getDomain().getTimeZone()));
								value=sf.format(item.getModelObject());
								logger.debug("getDateDisplayString()->"+value);
							}
							else {
								SimpleDateFormat sf = new SimpleDateFormat("MMM d yyyy");
								// sf.setTimeZone(TimeZone.getTimeZone(getDomain().getTimeZone()));
								value=sf.format(item.getModelObject());
								logger.debug("getDateDisplayString()->"+value);
							}
									
							//value= ServiceLocator.getService(DateTimeService.class).getDateDisplayString(item.getModelObject(), getLocale());
							//value = date.toString();
							
							
							
							//logger.debug("getDateDisplayString()->"+ServiceLocator.getService(DateTimeService.class).getDateDisplayString(item.getModelObject(), getLocale()));
							
							
						}
						else
							value="";
						return value;
					}
				}));
				item.add (new AjaxLink<Void>("remove-link") {
					public void onClick(AjaxRequestTarget target) {
						removeMember(item.getModelObject());
						target.add(DateEditor.this);
					}
					public boolean isVisible() {
						return isEditionEnabled();
					}
					public boolean isEnabled() {
						return !getTemplate().isReadOnly();
					}
				});
				item.add(new WebMarkupContainer("separator") {
					public boolean isVisible() {
						return getDates().size()>1 && item.getIndex()<getDates().size()-1;
					}
				});
			}
		};
		
		elementscont.add(ddd);
		
		elementscont.add(new WebMarkupContainer("leavevalues-message") {
			public boolean isVisible() {
				return getLeaveValues();
			}
		});
		
		elementscont.add(new WebMarkupContainer("nullmember") {
			public boolean isVisible() {
				return isBatchClassification() && getDates().isEmpty() && !getLeaveValues(); 
			}
		});
		
		((WebMarkupContainer) get("container")).add(new SelectorFragment("selector") {
			public boolean isVisible() {
				return isEditionEnabled();
			}
		});
	}

	
	
	protected boolean addMember(Date member) {
		if (member==null)
			return false;
		if (getClassifier().getMultiplicity().equals(Multiplicity.M1N) || dates.isEmpty()) {
			if (!dates.contains(member))
				dates.add(member);
 		}
		else {
			dates.set(0, member);
		}
		
		setUpdated(true);
		setLeaveValues(false);
		return true;
	}


	private void removeMember(Date date) {
		setUpdated(true);
		dates.remove(date);
	}

	
	@Override
	protected void setMembers(Classifier classifier) {
		
		if (!dates.isEmpty() || isUpdated()) 
			return;
		
		Assert.isInstanceOf(Classificable.class, getEditor().getModelObject());
		
		dates = new ArrayList<Date>();
		
		 for (Classification classification : ((Classificable)getEditor().getModelObject()).getClassification()) {
			if (classification!=null && classification.getClassifier().equals(classifier)) {
				
				OffsetDateTime dt = classification.getDateValue();
				Calendar ca=Calendar.getInstance();
				ca.set(dt.getYear(), dt.getMonthValue()-1, dt.getDayOfMonth());
				// Date.from(classification.getDateValue().toInstant()		
				// Convert from OffsetDateTime to Date
				dates.add(ca.getTime());
			}
		}
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}	
}
