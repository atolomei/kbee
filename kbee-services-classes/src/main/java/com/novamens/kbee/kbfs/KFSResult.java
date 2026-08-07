package com.novamens.kbee.kbfs;

import io.minio.Result;

public class KFSResult<T> extends Result<T> {

	public KFSResult(T type, Exception ex) {
		super(type, ex);
	}

}
