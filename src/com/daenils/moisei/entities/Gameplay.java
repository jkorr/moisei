package com.daenils.moisei.entities;

import com.daenils.moisei.input.Keyboard;

public class Gameplay {
	private long turnCount;
	private boolean isPlayerTurn;
	private boolean isMonsterTurn;
	
	// GLOBAL COOLDOWN STUFF
	protected boolean onGlobalCooldown = false;
	private double startGameplayTime;
	private double nowGameplayTime;
	private double deltaGameplayTime;
	private double globalCooldown = 0.4; // 400ms
	
	private Keyboard input;
	
	public Gameplay(Keyboard input) {
		this.turnCount = 0;
		this.isMonsterTurn = false;
		this.isPlayerTurn = false;
		this.startGameplayTime = System.currentTimeMillis();
		
		this.input = input;
	}
	
	public void update() {
		getIsMonsterTurn();
		getIsPlayerTurn();
		
		if (turnCount < 1) {
			turnCount++;
		}
		
		nowGameplayTime = System.currentTimeMillis();
		deltaGameplayTime = (int) (nowGameplayTime - startGameplayTime) / 100;
		deltaGameplayTime = deltaGameplayTime / 10; // not sure why I can't make it work in the previous line though
//		System.out.println(deltaGameplayTime);
		
		if (deltaGameplayTime > 0 && deltaGameplayTime % globalCooldown == 0) {
			onGlobalCooldown = false;
			// this whole stuff needs revision, it becomes imprecise very quickly
			// (but it's good enough for now) | test line:
			// System.out.println(deltaGameplayTime);
		}
		
		
		if (input.debugToggleMonsterTurn && !onGlobalCooldown) {
			setMonsterTurn();
			enableGlobalCooldown(); 

		}
		
		if (input.debugTogglePlayerTurn && ! onGlobalCooldown) {
			setPlayerTurn();
			enableGlobalCooldown();
		}
		
	}
	
	public void render() {
	}
	
	protected void newTurn() {
		if ((!this.isMonsterTurn) && (this.isPlayerTurn)) {
			this.isMonsterTurn = true;
			this.isPlayerTurn = false;
		}
		else if ((this.isMonsterTurn) && (!this.isPlayerTurn)) {
			this.isMonsterTurn = false;
			this.isPlayerTurn = true;
		}
		this.turnCount++;
		System.out.println("\nA new turn has began! (Turn " + turnCount + ")");
	}
	
	protected void endTurn(Entity e) {
		if(turnCount > 0) {
		System.out.println("Turn " + turnCount + " has ended.");
		}
		
		newTurn();
		e.resetActionPoints(e);
	}
	
	// currently public, because Game.java should be able to handle it for now
	public void setFirst() {
		// temporary code: always the player starts
		this.isPlayerTurn = true;
	}
	
	public void enableGlobalCooldown() {
		resetStartGameplayTime();
		onGlobalCooldown = true;
	}
	
	// GETTERS
	
	public long getCurrentTurn() {
		return this.turnCount;
	}
	
	public boolean getIsMonsterTurn() {
		return this.isMonsterTurn;
	}
	
	public boolean getIsPlayerTurn() {
		return this.isPlayerTurn;
	}
	
	// SETTERS
	
	public void resetStartGameplayTime() {
		this.startGameplayTime = System.currentTimeMillis();
	}
	
	// DEBUG STUFF
	
	public void setMonsterTurn() {
		if (!this.isMonsterTurn) this.isMonsterTurn = true;
		else if (this.isMonsterTurn) this.isMonsterTurn = false;
	}
	
	public void setPlayerTurn() {
		if (!this.isPlayerTurn) this.isPlayerTurn = true;
		else if (this.isPlayerTurn) this.isPlayerTurn = false;
	}
}
