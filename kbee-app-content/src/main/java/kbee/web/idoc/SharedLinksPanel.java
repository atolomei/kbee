package kbee.web.idoc;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.service.TokenService;
import com.novamens.content.service.UrlService;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;

@SuppressWarnings("serial")
public class SharedLinksPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;

	IModel<Person> personmodel;
	
	public SharedLinksPanel(String id, IModel<T> contentmodel, IModel<Person> personmodel) {
		super(id, contentmodel);
		setPerson(personmodel);
	}
	
	public void setPerson(IModel<Person> model) {
		this.personmodel = model;
	}
	
	public Person getPerson() {
		return this.personmodel!=null ? personmodel.getObject() : null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("home-link") {
			public void onClick() {
				setResponsePage(new RedirectPage(getContent().getService(UrlService.class).getServerUrl()));
			}
		});
		
		add(new Link<Void>("registration-link") {
			public void onClick() {
				setResponsePage(new RedirectPage(getRegistrationUrl(getPerson())));
			}
			public boolean isVisible() {
				return getPerson()!=null;
			}
		});
	}
	
	public Content getContent() {
		return getModelObject();
	}
	
	private String getRegistrationUrl(Person person) {
		KbeeJson data = new KbeeJson();
		if (person==null) return null;
		data.put("id", String.valueOf(person.getId()));
		data.put("date", person.getCreationOffsetDateTime().toString());
		data.put("domain", String.valueOf(person.getDomain().getId()));
		return person.getService(UrlService.class).getServerUrl() + "/registrationinit/" + ServiceLocator.getService(TokenService.class).getToken(data);
	}
}