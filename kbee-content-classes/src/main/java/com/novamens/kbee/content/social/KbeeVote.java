package com.novamens.kbee.content.social;

import java.time.OffsetDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;

import com.novamens.content.social.Vote;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;

/**
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_vote")
@DynamicInsert
public class KbeeVote implements Vote {
	
	@Id 
	@SequenceGenerator(name = "portal_sequencer", sequenceName = "portalid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "portal_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "vote")
	private int vote;
 	
	@Column(name = "votedate")
	private OffsetDateTime date;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id")
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "content_id")
	private Content content;

	
	
	public KbeeVote() {
	}
	
	
	public KbeeVote(Content content, User user, int vote) {
		setUser(user);
		setOffsetDateTime(OffsetDateTime.now());
		setContent(content);
		setVote(vote);
	}


	/**
	 * Should always be 1
	 */
	@Override
	public void setVote(int vote) {
		this.vote=vote;
	}

	@Override
 	public int getVote() {
		return vote;
	}
	
	@Override
	public OffsetDateTime getOffsetDateTime() {
		return date;
	}
	
	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Content getContent() {
		return content;
	}

	@Override
	public void setOffsetDateTime(OffsetDateTime date) {
		this.date=date;
	}
	
	@Override
	public void setUser(User user) {
		this.user=user;
	}

	@Override
	public void setContent(Content content) {
		this.content=content;
	}
	
	@Override
	public int hashCode() {
		int uh, ch;
		if (getUser()!=null) 
			uh = getUser().getId().hashCode();
		else 
			uh = 0;
		if (getContent()!=null) 
			ch = getContent().getId().hashCode();
		else 
			ch = 0;
		return super.hashCode() + uh + ch;
	}
		
	@Override
	public boolean equals(Object object) {
		if (object instanceof Vote) {
			User ouser = ((Vote) object).getUser();
			Content ocontent =  ((Vote) object).getContent();
			if (ouser!=null && ocontent!=null && getUser()!=null && getContent()!=null) {
				return ouser.getId().equals(getUser().getId()) && ocontent.getId().equals(getContent().getId());	
			}
		}
		return false;
	}
	
}
