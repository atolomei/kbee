package kbee.web.notes;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.notes.Billboard;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceThumbnailImage;

public class WorkNoteColumnPanel extends ModelPanel<Billboard> {

	private static final long serialVersionUID = 1L;
	
	static PackageResourceReference MENU_ICON = new PackageResourceReference(AbstractKbeeWebPage.class, "menu-red.png");
	
	
	Label l_name;
	
	public WorkNoteColumnPanel(String id, IModel<Billboard> model, String date_format, boolean isExpanded) {
		super(id, model);
		
		Person person = getContentDao().findUserProfileByUser(model.getObject().getUser()).getPerson();

		if (person.getPhoto()!=null) {
			@SuppressWarnings("unchecked")
			Image image = new ResourceThumbnailImage("photo", new ObjectModel<Resource>((Resource) person.getPhoto()), ThumbnailSize.MINI);
			add(image);
		}
		else {
			Image image = ServiceLocator.getService(BrandingWebService.class).getUserAvatarPhoto("photo", person);;
			add(image);
		}
		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = model.getObject().getUser();
		
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null) 
			zid=ZoneId.systemDefault().getId();
		
		
		l_name=new Label("name", getStringResourceModelName());
		
		add(l_name);
		add(new Label("user", person.getFirstLastName()));
		
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
		
		if (getCss()!=null) {
			l_name.add(new AttributeModifier("class", getCss()));
		}
			
		
		String text =null; 
		
		if (isExpanded)
			text=model.getObject().getText();
		else {
			if (model.getObject().getText()!=null) {
				Document doc = Jsoup.parse(model.getObject().getText());
				text = doc.text();
				}
		}
			
		if (text==null)
			text="";
		
		else if (!isExpanded && text.length()>420)
			text=text.substring(0, 420)+"...";
		
		add((new Label("text", text)).setEscapeModelStrings(false));
		
		l_name.add(new AjaxEventBehavior("click") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				fire(new ClickEvent<Billboard>(target, WorkNoteColumnPanel.this.getModel(), 0));
			}
		});
		
	 
	}

	
	
	protected IModel<String> getStringResourceModelName() {
		 return new PropertyModel<String>(getModel(), getDisplayProperty());
	}

	
	
	protected String getDisplayProperty() {
		return "displayName";
	}
	
	protected String getCss() {
		return null;
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
	
	

}
