package com.novamens.kbee.content.questionanswer;

import com.novamens.content.questionanswer.Question;
import com.novamens.content.questionanswer.QuestionStat;

/**
 * 
 * 
 */
public class KBeeQuestionStat implements QuestionStat {

	Question question;
	
	int views;
	int shared;
	int favorites;
	int votes;
	
	@Override
	public void setQuestion(Question question) {
		this.question=question;
	}

	@Override
	public Question getQuestion() {
		return question;
	}

	@Override
	public int getViews() {
		return views;
	}

	@Override
	public int getShared() {
		return shared;
	}

	@Override
	public int getFavorites() {
		return favorites;
	}

	@Override
	public int getVotes() {
		return question.getVotes();
	}

	@Override
	public void addView() {
		views++;
	}

	@Override
	public void addShared() {
		shared++;
	}

	@Override
	public void addVote() {
		question.addVote();
	}

	@Override
	public void addFavorite() {
		favorites++;
	}

	@Override
	public void substractFavorite() {
		favorites--;
	}

}
