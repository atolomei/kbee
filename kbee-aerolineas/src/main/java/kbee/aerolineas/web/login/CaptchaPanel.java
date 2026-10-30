package kbee.aerolineas.web.login;

import java.security.SecureRandom;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;
import com.novamens.wicket.markup.html.form.TextField;

import lombok.Getter;

public class CaptchaPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static final String CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Model<String> challenge = Model.of();
    private final Model<String> answer = Model.of();

    private final CaptchaImageResource captchaResource;
    private final TextField<String> captchaInput;
    
    private boolean placeholderLabel = false;
    
    @Getter
    private String input;
    
    private final Image captchaImage;

    private boolean verified;

    public CaptchaPanel(String id) {
        super(id);

        challenge.setObject(generateCode());

        // Recurso CAPTCHA oficial de Wicket.
        captchaResource = new CaptchaImageResource(challenge);

        // Imagen.
        captchaImage = new Image("captchaImage", captchaResource);
        captchaImage.setOutputMarkupId(true);
        add(captchaImage);
        
        add(new AjaxLink<Void>("reloadCaptcha") {
              @Override
            public void onClick(AjaxRequestTarget target) {

                refresh();

                target.add(captchaImage);
                target.add(captchaInput);
            }
        });
        
        captchaInput = new  TextField<String>("captchaInput");
        captchaInput.setModel(new PropertyModel<String>(this, "input"));
        captchaInput.add(new CaptchaValidator());
        add(captchaInput);
        
        setOutputMarkupId(true);

    }
    
    
	public void setPlaceholderLabel(boolean placeholderLabel) {
        captchaInput.setPlaceholderLabel(placeholderLabel);
		this.placeholderLabel = placeholderLabel;
	}

    /**
     * Valida el código ingresado.
     */
    public boolean validateCaptcha() {

        String expected = challenge.getObject();
        String entered = input;

        verified = expected != null
                && entered != null
                && expected.equalsIgnoreCase(entered.trim());

        return verified;
    }

    /**
     * Genera un nuevo desafío y limpia la entrada.
     */
    public void refresh() {
        verified = false;

        challenge.setObject(generateCode());
        answer.setObject(null);
        //captchaInput.clearInput();

        captchaResource.invalidate();
    }

    public boolean isVerified() {
        return verified;
    }
    
    public void setInput(String input) {
    	this.input = input;
    }

    private String generateCode() {

        StringBuilder result = new StringBuilder(5);

        for (int i = 0; i < 5; i++) {
            result.append(
                    CHARACTERS.charAt(
                            RANDOM.nextInt(CHARACTERS.length())
                    )
            );
        }

        return result.toString();
    }
    
    public class CaptchaValidator implements IValidator<String> {
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
    	public void validate(IValidatable<String> validatable)
    	{
    		
            String expected = challenge.getObject();
            String entered = validatable.getValue();

            verified = expected != null
                    && entered != null
                    && expected.equalsIgnoreCase(entered.trim());

    		// Check value against pattern
    		if (!verified)
    		{
    			ValidationError error = new ValidationError(this);
    			validatable.error(error);
    		}
    	}

    }
}