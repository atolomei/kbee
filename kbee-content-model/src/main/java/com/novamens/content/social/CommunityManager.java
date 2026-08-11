package com.novamens.content.social;

import com.novamens.content.base.Content;

public interface CommunityManager {

	public double getProbabiltyNotAppropiate(Content content);
	public boolean checkAndRemoveContent(Content content);
	
}
