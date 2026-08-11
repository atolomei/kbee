 package kbee.api.model;

import java.io.Serializable;
import java.util.List;

public class IFormData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private ApiProxy form;
	private ApiProxy file;
	private ApiProxy value;
	private String url;
	private boolean signed;
	private List<IFieldData> data;
	private boolean fileContainer;
	private String layout;
	private ISignedData signedData;
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public ApiProxy getForm() {
		return form;
	}

	public void setForm(ApiProxy form) {
		this.form = form;
	}

	public boolean isSigned() {
		return signed;
	}

	public void setSigned(boolean signed) {
		this.signed = signed;
	}

	public List<IFieldData> getData() {
		return data;
	}

	public void setData(List<IFieldData> data) {
		this.data = data;
	}
	
	public boolean isFileContainer() {
		return fileContainer;
	}

	public void setFileContainer(boolean fileContainer) {
		this.fileContainer = fileContainer;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public ApiProxy getFile() {
		return file;
	}

	public void setFile(ApiProxy file) {
		this.file = file;
	}

	public ApiProxy getValue() {
		return value;
	}

	public void setValue(ApiProxy value) {
		this.value = value;
	}

	public ISignedData getSignedData() {
		return signedData;
	}

	public void setSignedData(ISignedData signedData) {
		this.signedData = signedData;
	}

}
