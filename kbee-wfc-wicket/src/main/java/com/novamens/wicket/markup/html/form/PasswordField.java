package com.novamens.wicket.markup.html.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;

@SuppressWarnings("serial")
public class PasswordField extends TextField<String> {
	private static final long serialVersionUID = 1L;

	private org.apache.wicket.markup.html.form.PasswordTextField _input = null;
	boolean showpwdlink = false;

	public class PasswordControlFragment extends ControlFragment {
		private boolean showPassword = false;

		public PasswordControlFragment(String id, MarkupContainer markupProvider) {
			super(id, markupProvider);

			// Preview Ajax Link
			AjaxLink<Void> preview = new AjaxLink<Void>("preview") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					org.apache.wicket.markup.html.form.TextField<?> input = newTextField();
					showPassword = !showPassword;
					// HTML -> <i wicket:id="preview-icon"></i>
					WebMarkupContainer ni = new WebMarkupContainer("preview-icon");
					ni.add(new AttributeModifier("class", (showPassword ? "far fa-eye-slash" : "far fa-eye")));
					addOrReplace(ni);
					target.add(PasswordField.this);
					input.add(AttributeModifier.replace("type", showPassword ? "text" : "password"));
				}
			};

			WebMarkupContainer ni = new WebMarkupContainer("preview-icon");
			ni.add(new AttributeModifier("class", (showPassword ? "far fa-eye-slash" : "far fa-eye")));
			preview.add(ni);

			preview.setVisible(!isShowPasswordLink());
			add(preview);

			if (PasswordField.this.isHelpVisible())
				preview.add(new AttributeAppender("style", "margin-right: 40px;"));

			WebMarkupContainer spc = new WebMarkupContainer("show-password-container");
			add(spc);
			spc.setVisible(isShowPasswordLink());

			Label spc_label = new Label("show-password-label", (showPassword ? new StringResourceModel("hide-password", PasswordField.this, null) : new StringResourceModel("show-password", PasswordField.this, null)));

			AjaxLink<Void> spc_link = new AjaxLink<Void>("show-password-link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					org.apache.wicket.markup.html.form.TextField<?> input = newTextField();
					showPassword = !showPassword;
					// WebMarkupContainer ni= new WebMarkupContainer("preview-icon");
					// ni.add(new AttributeModifier("class", (showPassword ? "far fa-eye-slash" :
					// "far fa-eye")));
					// addOrReplace(ni);
					input.add(AttributeModifier.replace("type", showPassword ? "text" : "password"));

					Label spc_label = new Label("show-password-label", (showPassword ? "hide password" : "show password"));
					((AjaxLink<Void>) PasswordControlFragment.this.get("show-password-container:show-password-link")).addOrReplace(spc_label);

					target.add(PasswordField.this);
				}
			};

			spc_link.add(spc_label);
			spc.add(spc_link);

		}
	}

	@Override
	protected Fragment newControlFragment() {
		PasswordControlFragment fr = new PasswordControlFragment("control", PasswordField.this);

		StringBuilder str = new StringBuilder();

		if (isCentered())
			str.append("container-panel-centered ");
		else
			str.append("container-panel ");

		if (isHelpVisible())
			str.append("helpvisible");

		fr.add(new AttributeModifier("class", str.toString()));
		return fr;
	}

	public PasswordField(String id) {
		this(id, null, false, Width.W12);
	}

	public PasswordField(String id, Width width) {
		this(id, null, false, width);
	}

	public PasswordField(String id, boolean required) {
		this(id, null, required, Width.W12);
	}

	public PasswordField(String id, IModel<String> model) {
		this(id, model, false, Width.W12);
	}

	public PasswordField(String id, IModel<String> model, boolean required) {
		this(id, model, required, Width.W12);
	}

	public PasswordField(String id, IModel<String> model, boolean required, Width width) {
		super(id, model, required, width, null);
	}

	public boolean isShowPasswordLink() {
		return showpwdlink;
	}

	@Override
	protected org.apache.wicket.markup.html.form.TextField<?> newTextField() {

		if (_input != null)
			return _input;

		_input = new org.apache.wicket.markup.html.form.PasswordTextField("input", new PropertyModel<String>(this, "value")) {

			@Override
			public void validate() {
				super.validate();
				PasswordField.this.validate();
			}

			@Override
			public boolean isEnabled() {
				return getEditor() != null ? getEditor().isEditionEnabled() : true;
			}

			protected void onComponentTag(final ComponentTag tag) {
				IValueMap attributes = tag.getAttributes();
				attributes.put("type", "password");
				super.onComponentTag(tag);
			}

			@Override
			public String getInputName() {
				String overridedName = PasswordField.this.getInputName();
				if (overridedName != null)
					return overridedName;

				return super.getInputName();
			}

			@Override
			protected String[] getInputTypes() {
				return new String[] { "password", "text" };
			}
		};

		_input.add(new AttributeModifier("placeholder", PasswordField.this.getPlaceHolderLabel()));

		if (this.isShowPasswordLink())
			_input.add(new AttributeModifier("style", "width:100%; padding-left:0;"));

		_input.setResetPassword(false);
		return _input;
	}
}
