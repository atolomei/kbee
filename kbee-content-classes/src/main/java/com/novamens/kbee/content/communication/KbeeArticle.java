package com.novamens.kbee.content.communication;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.communication.Article;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.ContentTemplate;

@Entity
@DiscriminatorValue(OrganizationalText.ARTICLE_TYPE)
public class KbeeArticle extends KbeeOrganizationalText implements Article, Serializable {
	private static final long serialVersionUID = 655385935068989114L;
	
	public KbeeArticle() {
		super();
	}
	
	public KbeeArticle(ContentTemplate ct) {
		super(ct);
	}
}
