package com.novamens.kbee.kbfs.v1;

import java.io.IOException;

public interface SubDirectoryGenerationStrategy {
	public String generateRelativePath(SubDirGenerationStrategyContext context) throws IOException;
	
	public String getName();
	public void setName(String name);
	
	
}
