package kbee.api.model;

public class ISignature extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private ApiResource image;
	private IDevice device;
	private ICertificate certificate;

	public ApiResource getImage() {
		return image;
	}

	public void setImage(ApiResource image) {
		this.image = image;
	}

	public IDevice getDevice() {
		return device;
	}

	public void setDevice(IDevice device) {
		this.device = device;
	}

	public ICertificate getCertificate() {
		return certificate;
	}

	public void setCertificate(ICertificate certificate) {
		this.certificate = certificate;
	}
}