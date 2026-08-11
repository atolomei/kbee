package kbee.web.notes;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.notes.Billboard;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.wicket.markup.html.console.browser.HitExpandedPanel;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceThumbnailImage;


public class WorkNoteHitExpandedPanel extends Panel implements HitExpandedPanel {
	
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(WorkNoteHitExpandedPanel.class.getName());

	static PackageResourceReference MENU_ICON = new PackageResourceReference(AbstractKbeeWebPage.class, "menu-red.png");

	private static final long serialVersionUID = 1L;
	
	IModel<Billboard> model;
	String date_format=null;
	
	public WorkNoteHitExpandedPanel(String id, IModel<Billboard> model) {
			this(id, model, null);
	}
	
	public WorkNoteHitExpandedPanel(String id, IModel<Billboard> model, String date_format) {
		super(id);
		setModel(model);
		this.date_format=date_format;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Person person = getContentDao().findUserProfileByUser(model.getObject().getUser()).getPerson();

		if (person.getPhoto()!=null) {
			Image image = new ResourceThumbnailImage("photo", new ObjectModel<Resource>((Resource) person.getPhoto()), ThumbnailSize.MINI);
			add(image);
		}
		else {
			//Image image = ServiceLocator.getService(BrandingWebService.class).getUserAvatarPhoto("photo", person);;
			//add(image);
			add( new Image("photo", ServiceLocator.getService(BrandingWebService.class).getUserAvatarResourceReference(person)));

		}
		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = model.getObject().getUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null) {
			zid=ZoneId.systemDefault().getId();
		}
		
		add(new Label("name", getStringResourceModelName()));
		add(new Label("user", person.getDisplayName()));
		
		
		
		OffsetDateTime xd = model.getObject().getCreationOffsetDateTime();
		
		if (xd!=null) {
				String tst;
				ZonedDateTime zd = ZonedDateTime.ofInstant(xd.toInstant(), ZoneId.of(zid));
				if (date_format==null)
					tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				else if (date_format.equals(DateTimeService.COLlOQUIAL_AGO_LABEL))
					tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				else if (date_format.equals(DateTimeService.COLlOQUIAL_LABEL))
					tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL, null);
				else if (date_format.equals(DateTimeService.MONTH_DAY_YEAR_LABEL))
					tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year);
				else if (date_format.equals(DateTimeService.TIMESTAMP_LABEL))
					tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm_ss_zzz);
				else
					tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm);
				add((new Label("date", tst)).setEscapeModelStrings(false));
		}
		else
			add(new Label("date","na"));
		
		String text=model.getObject().getText();
		
		if (text==null)
			text="";
		
		add( (new Label("text", text)).setEscapeModelStrings(false));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		getModel().detach();
	}

	protected void setModel(IModel<Billboard> model) {
		this.model=model;
	}

	
	protected IModel<Billboard> getModel() {
		return model;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected IModel<String> getStringResourceModelName() {
		 return new PropertyModel<String>(getModel(), getDisplayProperty());
	}

	protected String getDisplayProperty() {
		return "displayName";
	}


}
