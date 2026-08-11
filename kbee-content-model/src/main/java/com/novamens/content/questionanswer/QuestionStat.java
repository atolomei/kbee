package com.novamens.content.questionanswer;

public interface QuestionStat {

	public void setQuestion(Question question); 
	public Question getQuestion();
	
	public int getViews();
	public int getShared();
	public int getFavorites();
	public int getVotes();
	
	public void addView();
	public void addShared();
	public void addVote();
	public void addFavorite();
	public void substractFavorite();
	
}
