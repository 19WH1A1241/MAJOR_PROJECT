package com.app;

// interface listener to send data from libGDX to native environment
public interface InterfaceListener {
	void saveScore(int score);

	void signIn();

	void signOut();

	void rate();

	void showLeaders();

	void admobInterstitial();

	void move();



}