package com.novamens.content.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.novamens.service.ObjectService;
import com.novamens.thumbnail.ThumbnailSize;

public interface ResourceService extends ObjectService {
	public InputStream getThumbnail(ThumbnailSize size) throws IOException;
	public File getThumbnailFile(ThumbnailSize size) throws IOException;
}