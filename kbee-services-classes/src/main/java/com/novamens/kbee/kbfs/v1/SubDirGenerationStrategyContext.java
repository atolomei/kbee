package com.novamens.kbee.kbfs.v1;

public class SubDirGenerationStrategyContext {

	public String filename;
	public String id; 		
	public String domain;
	public String repo_type;
	
	public SubDirGenerationStrategyContext(String filename, String id, String domain, String repo_type) {
		this.filename=filename;
		this.id=id; 		
		this.domain=domain;
		this.repo_type=repo_type;
			
	}
}
