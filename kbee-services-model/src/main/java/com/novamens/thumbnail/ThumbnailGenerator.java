package com.novamens.thumbnail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface ThumbnailGenerator {
	public void generate(File file, OutputStream stream, ThumbnailSize size) throws IOException;
	public void generate(String url, OutputStream stream, ThumbnailSize size) throws IOException;
	public boolean accept(File file);
}
