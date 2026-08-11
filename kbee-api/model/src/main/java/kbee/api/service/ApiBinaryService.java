package kbee.api.service;

import java.io.InputStream;

import kbee.api.model.ApiFile;
import kbee.api.model.ITransaction;

public interface ApiBinaryService {
	public ITransaction update(ApiFile file);
	public ITransaction update1(ApiFile file, InputStream resource);
	public ITransaction zipupdate(ApiFile file, InputStream resource);
}
