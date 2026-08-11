package com.novamens.content.resource;

import java.time.OffsetDateTime;

import com.novamens.content.user.UserSignature;

public interface SignedFile {
	public UserSignature getSignature();
	public OffsetDateTime getDate();
}
