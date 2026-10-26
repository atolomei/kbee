package kbee.aerolineas.web.login;

import java.security.SecureRandom;

import org.apache.wicket.extensions.markup.html.captcha.CaptchaImageResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

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

        // Campo de entrada.
//        captchaInput = new TextField<>("captchaInput", answer);
//        captchaInput.setRequired(true);
//        captchaInput.setLabel(Model.of("Código de verificación"));
//        add(captchaInput);
//        
//        captchaInput
        
        captchaInput = new  TextField<String>("captchaInput");
        captchaInput.setModel(new PropertyModel<String>(this, "input"));
        captchaInput.add(new CaptchaValidator());
        add(captchaInput);
        
        setOutputMarkupId(true);

        // Mensajes de error exclusivos del CAPTCHA.
//        FeedbackPanel feedback = new FeedbackPanel(
//                "captchaFeedback",
//                new ComponentFeedbackMessageFilter(captchaInput)
//        );
//
//        feedback.setOutputMarkupId(true);
//        add(feedback);
    }

//    public FormComponent<String> getCaptchaInput() {
//        return captchaInput;
//    }

//    /**
//     * Validador para registrar en el formulario.
//     */
//    public IFormValidator createValidator() {
//
//        return new IFormValidator() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public FormComponent<?>[] getDependentFormComponents() {
//                return new FormComponent<?>[] {
//                        captchaInput
//                };
//            }
//
//            @Override
//            public void validate(Form<?> form) {
//
//                if (!validateCaptcha()) {
//                    captchaInput.error(
//                            "El código de verificación es incorrecto."
//                    );
//                }
//            }
//        };
//    }

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