package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.graphics.Font;
import com.daenils.moisei.graphics.GUI;
import com.daenils.moisei.graphics.Screen;

public class Gameplay {
	private long turnCount;
	private boolean isPlayerTurn;
	private boolean isMonsterTurn;
	private int showActionsLeft;
	private String weaponString; // needed because otherwise nullpointexception (@ gui text rendering)
	
	// GLOBAL COOLDOWN STUFF
	protected boolean onGlobalCooldown = false;
	private double startGlobalCooldownTimer;
	private double nowTime;
	private double deltaGlobalCooldownTime;
	private double globalCooldown = 0.4; // 400ms
	
	// TURN TIMER STUFF
	private double startTurnTimer;
	private double deltaTurnTime;
	
	// GAME TIMER STUFF
	private double startGameTimer;
	private double deltaGameTime;
	
	// MONSTER WAIT TIMER STUFF
	private double startWaitTimer;
	private double deltaWaitTime;
	private boolean isWaitingOn;
	
	private Keyboard input;
	private Font font;
	
	public Gameplay(Keyboard input) {
		this.turnCount = 0;
		this.isMonsterTurn = false;
		this.isPlayerTurn = false;
		this.startGlobalCooldownTimer = System.currentTimeMillis();
		this.startTurnTimer = System.currentTimeMillis();
		this.startGameTimer = System.currentTimeMillis();
		this.startWaitTimer = System.currentTimeMillis();
		
		this.input = input;
		font = new Font();
	}
	
	public void update() {
//		System.out.println("WaitingOn? " + isWaitingOn);
		
		getIsMonsterTurn();
		getIsPlayerTurn();
		
		if (turnCount < 1) {
			turnCount++;
			System.out.print("\n+T" + turnCount + " | ");
			}
		
		nowTime = System.currentTimeMillis();

		deltaTurnTime = (int) (nowTime - startTurnTimer) / 100;
		deltaTurnTime = deltaTurnTime / 10;
		
		deltaGameTime = (int) (nowTime - startGameTimer) / 100;
		deltaGameTime = deltaGameTime / 10;		
		
		deltaGlobalCooldownTime = (int) (nowTime - startGlobalCooldownTimer) / 100;
		deltaGlobalCooldownTime = deltaGlobalCooldownTime / 10; // not sure why I can't make it work in the previous line though
		
		deltaWaitTime = (int) (nowTime - startWaitTimer) / 100;
		deltaWaitTime = deltaWaitTime / 10;
//		System.out.println(deltaWaitTime);
		
		if (deltaGlobalCooldownTime > 0 && deltaGlobalCooldownTime % globalCooldown == 0) {
			onGlobalCooldown = false;
			// this whole stuff needs revision, it becomes imprecise very quickly
			// (but it's good enough for now) | test line:
			// 2014-07-27: actually it's not like it's imprecise, but for some weird
			// reason the value gets double such as: 0.4, 0.8, 1.6, 3.2, 6.4, ...
//			 System.out.println(deltaGameplayTime);
		}
		
		
		if (input.debugToggleMonsterTurn && !onGlobalCooldown) {
			setMonsterTurn();
			enableGlobalCooldown(); 

		}
		
		if (input.debugTogglePlayerTurn && ! onGlobalCooldown) {
			setPlayerTurn();
			enableGlobalCooldown();
		}
		
		if (isPlayerTurn) showActionsLeft = Gamestats.playerActionPoints;
		if (isMonsterTurn) showActionsLeft = Gamestats.monsterActionPoints;
		
		if (Gamestats.playerDamage[0] > 0) weaponString = "\nWeapon damage: " + Gamestats.playerDamage[0] + "-" + Gamestats.playerDamage[1];
		else weaponString = "N / A";
		
	}
	
	// rendering the GUI text is probably temporary
	public void render(Screen screen) {
		// TURN INFO BOX

		font.render(645, 572, -6, 0xffffff00, "- Turn Info -", screen);  
		font.render(GUI.screenTurninfoPos, 572, -6, 0xffffff00,
				"\nTurn " + turnCount + " - " + getDeltaTurnTime() +
				"\n" + printWhosTurn() +
				"\nEnergy left: " + showActionsLeft + "" +
				"\nGame time: " + getDeltaGameTime()
				, screen);
		
		// PLAYER INFO BOX
		font.render(GUI.screenPlayerinfoPos+110, 572, -6, 0xffffff00, "- Player Info -"
				, screen);
		font.render(GUI.screenPlayerinfoPos, 572, -6, 0xffffff00, ""
				+ "\nHealth: " + Gamestats.playerHP
				+ " (" + Gamestats.monsterHitDamage*-1 + ")"
				+ "\nMana: " + Gamestats.playerMana
				+ "\nXP: " + Gamestats.playerXP
				+ "\nWeapon name: " + "Default Dagger"
				+ weaponString
				+ " (" + Gamestats.playerHitDamage + ")"
				, screen);
		
		// MONSTER INFO
		font.render(580 + 15, 290 + 210, -6, 0xffff0000, "Monster 1"
				+ "\n HP: " + Gamestats.monsterHP
				+ " (" + Gamestats.playerHitDamage*-1 + ")"
					
				, screen);
		
		// NOTIFICATIONS
		if (Gamestats.turnCount > 1 && Gamestats.deltaTurnTime < 1.2)
		font.render(560, 160, +5, 0xffff6a05, printWhosTurnTop(), screen);
		
		if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime < 2)
			font.render(500, 160, -4, 0xffff6a05, "Press SPACE to hit your enemy", screen);
		if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime > 3 && Gamestats.deltaTurnTime < 5)
			font.render(500, 160, -4, 0xffff6a05, "Press ENTER to end your turn", screen);
		
		if (Gamestats.monsterHP < 1)
			font.render(580 - 10, 160, -4, 0xffa30300, "Monster DIED", screen);
		
		// FLOATING PLAYERDAMAGE
		if (Gamestats.playerLastActionPoints > Gamestats.playerActionPoints)
			font.render(580 + 40, 290 - 10, -6, 0xffff1100, "\n\n" + Gamestats.playerHitDamage*-1, screen);
		
		
		// STRICTLY DEBUG
		font.render(25, 25, -6, 0xffffa500, "DEBUG STUFF" + 
				"\nRandom Wait: " + Gamestats.monsterRW
				, screen);
	}
	
	protected void newTurn() {
		
		startTurnTimer = System.currentTimeMillis();
		if ((!this.isMonsterTurn) && (this.isPlayerTurn)) {
			this.isMonsterTurn = true;
			this.isPlayerTurn = false;
		}
		else if ((this.isMonsterTurn) && (!this.isPlayerTurn)) {
			this.isMonsterTurn = false;
			this.isPlayerTurn = true;
		}
		this.turnCount++;
		System.out.print("\n+T" + turnCount + " | ");
//		System.out.println("\nA new turn has began! (Turn " + turnCount + ")");
	}
	
	protected void endTurn(Entity e) {
		
		System.out.print("-T" + turnCount + " [" + deltaTurnTime + "s]" + " [" + deltaGameTime + "s]");
		deltaTurnTime = 0;
		

		if(turnCount > 0) {
			
//		System.out.println("Turn " + turnCount + " has ended.");
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
	
	public String printWhosTurn() {
		String whosTurn = "N / A";
		if (getIsMonsterTurn()) whosTurn = "Monster turn";
		if (getIsPlayerTurn()) whosTurn = "Player turn";
		return whosTurn;
	}
	
	public String printWhosTurnTop() {
		String whosTurn = "N / A";
		if (getIsMonsterTurn()) whosTurn = "AI TURN";
		if (getIsPlayerTurn()) whosTurn = "YOUR TURN";
		return whosTurn;
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
	
	public double getDeltaGameTime() {
		return this.deltaGameTime;
	}
	
	
	public double getDeltaTurnTime() {
		return deltaTurnTime;
	}
	
	public double getTurnStartTime() {
		return startTurnTimer;
	}
	
	public double getStartWaitTimer() {
		return startWaitTimer;
	}
	
	public double getDeltaWaitTime() {
		return deltaWaitTime;
	}
	
	public boolean getIsWaitingOn() {
		return isWaitingOn;
	}
	
	// SETTERS
	
	public void setIsWaitingOn(boolean b) {
		isWaitingOn = b;
	}
	
	public void resetStartGameplayTime() {
		this.startGlobalCooldownTimer = System.currentTimeMillis();
	}
	
	public void setStartWaitTimer(double n) {
		startWaitTimer = n;
	}
	
	public void setDeltaWaitTime( double n) {
		deltaWaitTime = n;
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
