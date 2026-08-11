package com.novamens.content.base;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.user.UserSignature;

public interface SignedData {
	
	public class Resource {
		private String id, digest;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getDigest() {
			return digest;
		}
		public void setDigest(String digest) {
			this.digest = digest;
		}
	}
	
	public OffsetDateTime getDate();
	public UserSignature getSignature();
	public String getSnapshot();
	public String getDigest();
	public String getSignedData();
	public List<Resource> getResources();
}