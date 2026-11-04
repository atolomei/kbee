package kbee.aerolineas.web.login;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.security.User;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.PasswordField;
import com.novamens.wicket.model.ObjectModel;

import io.odilon.log.Logger;
import kbee.util.PropertiesFactory;
import kbee.web.page.ApplicationMenuSection;

/**
 * Aerolineas Argentinas version of PasswordRecoveryPage2 (new password entry).
 * Mounted dynamically as bean "passwordrecovery-page".
 */
public class AAPasswordRecoveryPage extends AbstractKbeeWebPage {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AAPasswordRecoveryPage.class.getName());

	private static final ResourceReference CSS_AA_LOGIN = new CssResourceReference(AAPasswordRecoveryPage.class, "aa.css");
	private static final ResourceReference BOOTSTRAP_JS = new JavaScriptResourceReference(com.novamens.wicket.markup.html.form.Form.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP_JS);
	private static final ResourceReference BOOTSTRAP_CSS = new CssResourceReference(com.novamens.wicket.markup.html.form.Form.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP);
	private static final ResourceReference AW = new CssResourceReference(com.novamens.wicket.markup.html.form.Form.class, com.novamens.wicket.markup.html.form.Form.FONTAWESOME);

	private static final String XUA_Compatible = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	static final int MINIMUM_LENGTH = 8;

	private static final Locale DEFAULT_LOCALE = new Locale("es");

	private PageParameters parameters;
	private IModel<User> user_model;
	private boolean valid = false;

	public AAPasswordRecoveryPage() {
		this(null);
	}

	public AAPasswordRecoveryPage(PageParameters _parameters) {
		super();
		this.parameters = _parameters;

		// default language: Spanish, overridden by the user's locale once the user is known
		getSession().setLocale(DEFAULT_LOCALE);

		setFonts("https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&family=Noto+Sans:ital,wght@0,100..900;1,100..900&display=swap");

		if (parameters != null && parameters.get("key") != null && !parameters.get("key").isEmpty()) {
			String token = parameters.get("key").toString();
			SecurityService ss = ServiceLocator.getService(SecurityService.class);
			if (ss.isValid(token)) {
				setUser(ss.findUserById(ss.getUserId(token)));
				valid = getUser() != null;
			}
		}
		getSession().setLocale(getUserLocale());
	}

	private Locale getUserLocale() {
		User u = getUser();
		if (u instanceof KbeeUser) {
			Locale l = ((KbeeUser) u).getLocale();
			if (l != null)
				return l;
		}
		return DEFAULT_LOCALE;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		setPageFonts(getFonts());
		setPageXUACompatible(XUA_Compatible);
		setPageTitle(new Model<String>(ServiceLocator.getService(BrandingService.class).getProductTabTitle()));

		WebMarkupContainer area = new WebMarkupContainer("main-area-container");
		add(area);

		area.add(new Image("logo", new PackageResourceReference(AAPasswordRecoveryPage.class, "aa-logo.png")));

		area.add(new Label("title", new StringResourceModel("reset-password", this, null)));

		PasswordResetForm form = new PasswordResetForm("form");
		form.setVisible(valid);
		area.add(form);

		FeedbackPanel expired = new FeedbackPanel("expired");
		expired.setEscapeModelStrings(false);
		if (!valid) {
			expired.error(new StringResourceModel("expired", this, null).getString());
		}
		expired.setVisible(!valid);
		area.add(expired);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (user_model != null)
			user_model.detach();
	}

	@Override
	public String getPageHelpKey() {
		return getApplicationMenuSection().getKey() + "-" + this.getClass().getSimpleName().toLowerCase();
	}

	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.GENERAL;
	}

	@Override
	protected ResourceReference getCssResource() {
		return CSS_AA_LOGIN;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
		response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));
		response.render(CssHeaderItem.forReference(AW));
	}

	private String getUserName() {
		User u = getUser();
		return u != null ? u.getUserName() : "";
	}

	private void setUser(User user) {
		this.user_model = new ObjectModel<User>(user);
	}

	private User getUser() {
		if (this.user_model != null && this.user_model.getObject() != null)
			return this.user_model.getObject();
		return null;
	}

	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	public class PasswordResetForm extends Form<Void> {

		private static final long serialVersionUID = 1L;

		private FeedbackPanel feedback;
		private String password = "";
		private String passwordcheck = "";

		public String getPassword() 						{ return password; }
		public String getPasswordCheck() 					{ return passwordcheck; }
		public void setPassword(String password) 			{ this.password = password; }
		public void setPasswordCheck(String passwordcheck) 	{ this.passwordcheck = passwordcheck; }

		public PasswordResetForm(String id) {
			super(id);
			setOutputMarkupId(true);

			add(new Label("username", getUserName()));

			feedback = new FeedbackPanel("feedback") {
				private static final long serialVersionUID = 1L;
				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (this.anyErrorMessage())
						tag.append("class", "error", " ");
					else if (anyMessage(FeedbackMessage.SUCCESS))
						tag.append("class", "ok", " ");
				}
			};
			feedback.setVisible(false);
			feedback.setEscapeModelStrings(false);
			feedback.setOutputMarkupId(true);
			add(feedback);

			add(new PasswordField("password", new PropertyModel<String>(this, "password"), true) {
				private static final long serialVersionUID = 1L;
				public boolean isShowPasswordLink() { return true; }
				public boolean isCentered() 		{ return true; }
			});

			add(new PasswordField("password-verification", new PropertyModel<String>(this, "passwordcheck"), true) {
				private static final long serialVersionUID = 1L;
				public boolean isShowPasswordLink() { return true; }
				public boolean isCentered() 		{ return true; }
			});

			Button submit = new AjaxButton("submit", this) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target) {

					KbeeUser user = (KbeeUser) getUser();
					Locale locale = getUserLocale();
					ResourceBundle res = ResourceBundle.getBundle(AAPasswordRecoveryPage.class.getName(), locale);

					String msg = checkPasswords(res);

					if (msg != null && msg.length() > 0) {
						feedback.error(msg);
						onError(target);
						return;
					}

					ServiceLocator.getService(SecurityService.class).authenticate(user.getName());
					// only one user instance per session: the one in the user profile
					UserProfile profile = getContentDao().findUserProfileByUser(user);
					user = (KbeeUser) profile.getUser();
					user.setPassword(getPassword());
					try {
						List<String> ulist = new ArrayList<String>();
						ulist.add("Password Reset");
						ServiceLocator.getService(SecurityContentMgmtService.class).update(user, ulist);
						setPassword("");
						setPasswordCheck("");
						target.add(getForm());

						feedback.info(res.getString("success"));
						feedback.add(new AttributeModifier("class", "alert alert-info col-xs-12"));
						feedback.setVisible(true);
						target.add(feedback);
					} catch (Exception e) {
						logger.error(e);
						feedback.error(res.getString("error") + " " + e.getMessage());
						onError(target);
					}
				}

				@Override
				protected void onError(AjaxRequestTarget target) {
					feedback.setVisible(true);
					target.add(feedback.getParent());
				}
			};

			submit.add(new AttributeModifier("value", new StringResourceModel("save", this, null).getObject()));
			add(submit);
		}

		private String checkPasswords(ResourceBundle res) {
			StringBuilder str = new StringBuilder();
			String pwd = getPassword();
			String chk = getPasswordCheck();

			if (pwd == null || pwd.length() == 0)
				append(str, res.getString("complete-pwd"));

			if (chk == null || chk.length() == 0)
				append(str, res.getString("complete-verification"));

			if (pwd != null && chk != null && pwd.length() > 0) {
				if (pwd.length() < MINIMUM_LENGTH)
					append(str, res.getString("minimum-8chars"));
				if (!chk.equals(pwd))
					append(str, res.getString("password-verification-no-match"));
				if (!pwd.matches(".*[0-9].*"))
					append(str, res.getString("does-not-have-number"));
				if (!pwd.matches(".*[A-Z].*"))
					append(str, res.getString("does-not-have-capital-letter"));
			}
			return str.toString();
		}

		private void append(StringBuilder str, String s) {
			if (str.length() > 0)
				str.append("<br />");
			str.append(s);
		}
	}
}