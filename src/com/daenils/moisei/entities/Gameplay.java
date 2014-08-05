package com.daenils.moisei.entities;

import java.util.Random;

import com.daenils.moisei.Game;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.graphics.Font;
import com.daenils.moisei.graphics.GUI;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Stage;

public class Gameplay {
	private Stage stage;
	
	private long turnCount;
	private boolean isPlayerTurn;
	private boolean isMonsterTurn;
	private int showActionsLeft;
	private String weaponString; // needed because otherwise nullpointexception (@ gui text rendering)
	
	private int waveCount;
	private boolean newWave;
	
	// GLOBAL COOLDOWN STUFF
	protected boolean onGlobalCooldown = false;
	private double startGlobalCooldownTimer;
	private double nowTime;
	private double deltaGlobalCooldownTime;
	private double globalCooldown = 0.4; // 400ms
	
	// TURN TIMER STUFF
	private double startTurnTimer;
	private double deltaTurnTime;
	private double pauseTurnTime;
	
	// GAME TIMER STUFF
	private double startGameTimer;
	private double deltaGameTime;
	private double pauseGameTime;
	
	
	// MONSTER WAIT TIMER STUFF
	private double startWaitTimer;
	private double deltaWaitTime;
	private boolean isWaitingOn;
	
	// GAME CONTINUATION AFTER MONSTERS DIED
	private boolean continueGame;
	
	private Keyboard input;
	private Font font;
	
	protected boolean[] spawnSlotFilled = new boolean[5];
	
	public Gameplay(Keyboard input, Stage stage) {
		this.stage = stage;
		this.turnCount = 0;
		this.isMonsterTurn = false;
		this.isPlayerTurn = false;
		this.startGlobalCooldownTimer = System.currentTimeMillis();
		this.startTurnTimer = System.currentTimeMillis();
		resetGameplayTime();
		this.startWaitTimer = System.currentTimeMillis();
		
		this.waveCount = 0;
		
		this.input = input;
		font = new Font();
	}
	
	public void update() {
//		System.out.println(newWave);
		
		if (!Gamestats.monstersAllDead) continueGame = true;
		if (Gamestats.monstersAllDead) continueGame = false;
		
		if (Gamestats.monsterCount == 0 && (continueGame || turnCount == 0)) newMonsterWave();
		
//		System.out.println("WaitingOn? " + isWaitingOn);
		
		getIsMonsterTurn();
		getIsPlayerTurn();
		
		if (turnCount < 1) {
			turnCount++;
			System.out.print("\n+T" + turnCount + " | ");
			}
		
		nowTime = System.currentTimeMillis();

		if (continueGame) {
		
		deltaTurnTime = (int) (nowTime - startTurnTimer) / 100;
		deltaTurnTime = deltaTurnTime / 10;
		
		deltaGameTime = (int) (nowTime - startGameTimer) / 100;
		deltaGameTime = deltaGameTime / 10;		
		
		deltaWaitTime = (int) (nowTime - startWaitTimer) / 100;
		deltaWaitTime = deltaWaitTime / 10;
//		System.out.println(deltaWaitTime);
		}
		
		deltaGlobalCooldownTime = (int) (nowTime - startGlobalCooldownTimer) / 100;
		deltaGlobalCooldownTime = deltaGlobalCooldownTime / 10; // not sure why I can't make it work in the previous line though
		
		
		if (deltaGlobalCooldownTime > 0 && deltaGlobalCooldownTime % globalCooldown == 0) {
			onGlobalCooldown = false;
			// this whole stuff needs revision, it becomes imprecise very quickly
			// (but it's good enough for now) | test line:
			// 2014-07-27: actually it's not like it's imprecise, but for some weird
			// reason the value gets double such as: 0.4, 0.8, 1.6, 3.2, 6.4, ...
//			 System.out.println(deltaGameplayTime);
		}
		
		if (input.debugLockAbility && !onGlobalCooldown) {
			Gamestats.player.removeLastAbility();
			enableGlobalCooldown();
		}
		
		if (input.debugUnlockAbility && !onGlobalCooldown) {
			Random rand = new Random();
			int r = rand.nextInt((5 - 1) + 1) + 1;
			Gamestats.player.unlockAbility(Gamestats.player, r);
			enableGlobalCooldown();
		}
		
		
		
		if (isPlayerTurn) showActionsLeft = Gamestats.playerActionPoints;
		if (isMonsterTurn) {
			int actionSum = 0;
			for (int i = 0; i < Gamestats.monsterCount; i++) {
				actionSum += Gamestats.monsterActionPoints[i];
			}
			showActionsLeft = actionSum;
		}
		
		if (Gamestats.playerDamage[0] > 0) weaponString = "\nWeapon damage: " + Gamestats.playerDamage[0] + "-" + Gamestats.playerDamage[1];
		else weaponString = "N / A";
		
		if (deltaGameTime < 2.0) newWave = true;
		else newWave = false;
		
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
				+ " (" + (Gamestats.playerLastHealth - Gamestats.playerHP) *-1 + ")"
				+ "\nMana: " + Gamestats.playerMana
				+ "\nXP: " + Gamestats.playerXP
				+ "\nWeapon name: " + "Default Dagger"
				+ weaponString
				+ " (" + Gamestats.playerHitDamage + ")"
				, screen);

		
		// MONSTER INFO #3
		int n = 0;
		if (Gamestats.playerTargetCycled < Gamestats.monsterCount && Gamestats.playerTargetCycled > 0) n = Gamestats.playerTargetCycled - 1;
		else if (Gamestats.playerNeverCycled) n = Gamestats.monsterCount - 1;
		
		for (int i = 0; i < Gamestats.monsterCount; i++) {
			
			if (Gamestats.monsterIsAlive[i] && (n) == i && (Gamestats.playerNeverCycled) ) {
				font.render(stage.getMonsters().get(i).x + 15, 290 + 210, -6, 0xffff0000, "" + Gamestats.monsterId[i]
						+ "\n HP: " + Gamestats.monsterHP[i]
						+ " MP: " + Gamestats.monsterMana[i]
//						+ " (" + Gamestats.playerHitDamage*-1 + ")"
							
						, screen);
				}
		}

		/*
		if (Gamestats.monsterIsAlive[0]) {
		font.render(580 + 15, 290 + 210, -6, 0xffff0000, "" + Gamestats.monsterId[0]
				+ "\n HP: " + Gamestats.monsterHP[0]
//				+ " (" + Gamestats.playerHitDamage*-1 + ")"
					
				, screen);
		}
		*/
		
		// NOTIFICATIONS
		renderNotifications(screen);
		
		// ABILITY texts
		for (int i = 0; i < Gamestats.player.abilities.size(); i++)	renderAbilityText(screen, i);
		
		// FLOATING PLAYERDAMAGE
		if (Gamestats.playerLastActionPoints > Gamestats.playerActionPoints && deltaGlobalCooldownTime < 0.9)
			font.render(580 + 40, 290 - 10, -6, 0xffff1100, "\n\n" + Gamestats.playerHitDamage*-1, screen);
		
		
		// STRICTLY DEBUG
		renderDebugInfo(screen);
	}
	
	protected void renderDebugInfo(Screen screen) {
		font.render(25, 25, -6, 0xff00ff00, "DEBUG STUFF"
				//			+ "\nRandom Wait: " + Gamestats.monsterRW
							+ "\nMonsters spawned: " + Gamestats.monsterCount
							+ "\nMonster HP: " + Gamestats.monsterHP[0] + "-" + Gamestats.monsterHP[1] + "-" + Gamestats.monsterHP[2]
									+ "-" + Gamestats.monsterHP[3] + "-" + Gamestats.monsterHP[4]
							+ "\nMonster Mana: " + Gamestats.monsterMana[0] + "-" + Gamestats.monsterMana[1] + "-" + Gamestats.monsterMana[2]
									+ "-" + Gamestats.monsterMana[3] + "-" + Gamestats.monsterMana[4]
							+ "\nMonster isAlive: " + Gamestats.monsterIsAlive[0] + "-" + Gamestats.monsterIsAlive[1] + "-" + Gamestats.monsterIsAlive[2]
									+ "-" + Gamestats.monsterIsAlive[3] + "-" + Gamestats.monsterIsAlive[4]
							+ "\nMonster APs: " + Gamestats.monsterActionPoints[0] + "-" + Gamestats.monsterActionPoints[1] + "-" + Gamestats.monsterActionPoints[2]
									+ "-" + Gamestats.monsterActionPoints[3] + "-" + Gamestats.monsterActionPoints[4]		
							+ "\nMonster Levels: " + Gamestats.monsterLevel[0] + "-" + Gamestats.monsterLevel[1] + "-" + Gamestats.monsterLevel[2]
									+ "-" + Gamestats.monsterLevel[3] + "-" + Gamestats.monsterLevel[4]
							+ "\nMonster Damage: " + Gamestats.monsterDamage[0][0] + "-" + Gamestats.monsterDamage[0][1] + "-" + Gamestats.monsterDamage[1][0] + "-" + Gamestats.monsterDamage[1][1] + "-" + Gamestats.monsterDamage[2][0] + "-" + Gamestats.monsterDamage[2][1]
									+ "-" + Gamestats.monsterDamage[3][0] + "-" + Gamestats.monsterDamage[3][1] + "-" + Gamestats.monsterDamage[4][0] + "-" + Gamestats.monsterDamage[4][1]	
							+ "\nMonster HitDMG: " + Gamestats.monsterHitDamage[0] + "-" + Gamestats.monsterHitDamage[1] + "-" + Gamestats.monsterHitDamage[2]
									+ "-" + Gamestats.monsterHitDamage[3] + "-" + Gamestats.monsterHitDamage[4]
							+ "\nMonster isWaiting: " + Gamestats.monsterIsWaiting[0] + "-" + Gamestats.monsterIsWaiting[1] + "-" + Gamestats.monsterIsWaiting[2]
									+ "-" + Gamestats.monsterIsWaiting[3] + "-" + Gamestats.monsterIsWaiting[4]	
							+ "\nMonster Random Wait: " + Gamestats.monsterRW[0] + "-" + Gamestats.monsterRW[1] + "-" + Gamestats.monsterRW[2]
									+ "-" + Gamestats.monsterRW[3] + "-" + Gamestats.monsterRW[4]	
							+ "\nTotal TurnCount: " + Gamestats.getTotalTurnCount()
							+ "\nTotal GameTime: " + Gamestats.getTotalGameTime()
							+ "\nMonster deathcount: " + Gamestats.monsterDeathCount
							+ "\nTotal Monster deathcount: " + Gamestats.getTotalMonsterDeathCount()
		//					+ "\nPl Ability 1 last used: " + Gamestats.player.abilities.get(0).getLastUsed() // these 3 lines are poorly written
			//				+ "\nPl Ability 2 last used: " + Gamestats.player.abilities.get(1).getLastUsed() // they should be removed/changed
				//			+ "\nPl Ability 3 last used: " + Gamestats.player.abilities.get(2).getLastUsed() // as soon as possible
							+ "\nCurrent wave: " + Gamestats.waveCount
							+ "\nGlobalCooldown: " + deltaGlobalCooldownTime 
							
				//			+ "\nMONSTER ATTACKED: " + Gamestats.monstersAttacked
							, screen);
	}
	
	protected void renderNotifications(Screen screen) {
		if (Gamestats.turnCount > 1 && Gamestats.deltaTurnTime < 1.2)
			font.render(560, 160, +5, 0xffff6a05, printWhosTurnTop(), screen);
			
			if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime < 2)
				font.render(500, 160, -4, 0xffff6a05, "Press SPACE to hit your enemy", screen);
			if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime > 3 && Gamestats.deltaTurnTime < 5)
				font.render(500, 160, -4, 0xffff6a05, "Press ENTER to end your turn", screen);
			
			if (Gamestats.monsterHP[5] < 1)
				font.render(580 - 10, 160, -4, 0xffa30300, "Monster DIED", screen);
	}
	
	protected void renderAbilityText(Screen screen, int n) {
		font.render(20 + n * 130, GUI.screenBottomElements + 97, -4, 0, Gamestats.player.abilities.get(n).getName(), screen);
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
		
		// reset dot flags
		Gamestats.player.oneDotPerTurn = false;
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			stage.getMonsters().get(i).oneDotPerTurn = false;
		}
		
		// reset isStunned
		Gamestats.player.isStunned = false;
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			stage.getMonsters().get(i).isStunned = false;
		}
	}
	
	protected void endTurn() {
		// THIS METHOD IS SPECIFICALLY TAILORED FOR THE MONSTER
		// USE THE OTHER ONE WITH THE PLAYER
		
		System.out.print("-T" + turnCount + " [" + deltaTurnTime + "s]" + " [" + deltaGameTime + "s]" + " [" + Gamestats.monstersAttacked + " atks]");
		deltaTurnTime = 0;
		newTurn();
		
		for (int i = 0; i < Gamestats.monsterCount; i++) {
			stage.getMonsters().get(i).resetActionPoints(stage.getMonsters().get(i));
		}
		Monster.resetMonstersAttacked();

	}
	
	protected void endTurn(Entity e) {
		
		System.out.print("-T" + turnCount + " [" + deltaTurnTime + "s]" + " [" + deltaGameTime + "s]" + " [" + Gamestats.monstersAttacked + " atks]");
		deltaTurnTime = 0;
		

		if(turnCount > 0) {
			
//		System.out.println("Turn " + turnCount + " has ended.");
		}
		
		
		newTurn();
		
		if (e instanceof Player) e.resetActionPoints(e);
		
		// TODO: THIS BELOW IS A TEMPORARY FIX
		// IT SHOULD BE CHANGED LATER ON OR
		// THE VERY LEAST MOVED FROM HERE
		
		if (e instanceof Monster) {
			for (int i = 0; i < Gamestats.monsterCount; i++) {
				stage.getMonsters().get(i).resetActionPoints(stage.getMonsters().get(i));
			}
			Monster.resetMonstersAttacked();
		}
		
	}
	
	protected void newMonsterWave() {
		if (Gamestats.monstersAllDead) {
			resetGame();
		}
		if (!(Gamestats.turnCount == 0)) buffPlayer();
		waveCount++;
		spawnMonster();
	}
	
	private void addMonster(int slot) {
		Monster emma = new Monster(slot, Gamestats.player);
		stage.add(emma);
		System.out.print("" + emma.name + "spawned.");
		Game.getGameplay().enableGlobalCooldown(); 


	}
	
	protected void spawnMonster() {
		if (getSpawnSlotFilled(5)) {
			System.out.println("All done here: " + 
		Gamestats.spawnSlotFilled1 + Gamestats.spawnSlotFilled2 + Gamestats.spawnSlotFilled3
		+ Gamestats.spawnSlotFilled4 + Gamestats.spawnSlotFilled5);
			Game.getGameplay().enableGlobalCooldown(); 
			}
		
		
		if (!getSpawnSlotFilled(3) && !onGlobalCooldown) {
			addMonster(3);
			enableGlobalCooldown();
		}
		if (!getSpawnSlotFilled(2) && getSpawnSlotFilled(3) && !onGlobalCooldown) {
			addMonster(2);
			enableGlobalCooldown();
		}		
		if (!getSpawnSlotFilled(4) && getSpawnSlotFilled(2) && !onGlobalCooldown) {
			addMonster(4);
			enableGlobalCooldown();
		}
		if (!getSpawnSlotFilled(1) && getSpawnSlotFilled(4) && !onGlobalCooldown) {
			addMonster(1);
			enableGlobalCooldown();
		}			
		if (!getSpawnSlotFilled(5) && getSpawnSlotFilled(1) && !onGlobalCooldown) {
			addMonster(5);
			enableGlobalCooldown();
		}
		
	}

	
	protected void resetGame() {
		Gamestats.submitGameStats();
	//	resetGameplayTime();
		resetTurnTime();
		Gamestats.player.resetActionPoints(Gamestats.player); // this is not really a nice solution
		Gamestats.player.defaultTarget = null;
		resetTurnCount();
	}
	
	protected void buffPlayer() {
		System.out.println("Player buffed!");
		Gamestats.player.addHealth(10);
		Gamestats.player.addMana(10);
		
		if (Gamestats.waveCount % 3 == 0)
		Gamestats.player.addDefaultActionPoints(1);
	}
	
	// currently public, because Game.java should be able to handle it for now
	public void setFirst() {
		// temporary code: always the player starts
		this.isPlayerTurn = true;
	}
	
	public void enableGlobalCooldown() {
		resetStartGCDTimer();
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
	
	public boolean getSpawnSlotFilled(int n) {
		return spawnSlotFilled[n - 1];
	}
	
	public int getWaveCount() {
		return waveCount;
	}
	
	public boolean getNewWave() {
		return newWave;
	}
	

	
	// SETTERS
	
	public void setIsWaitingOn(boolean b) {
		isWaitingOn = b;
	}
	
	public void resetStartGCDTimer() {
		this.startGlobalCooldownTimer = System.currentTimeMillis();
	}
	
	public void resetGameplayTime() {
		this.startGameTimer = System.currentTimeMillis();
	}
	
	public void resetTurnTime() {
		this.startTurnTimer = System.currentTimeMillis();
	}
	
	public void setStartWaitTimer(double n) {
		startWaitTimer = n;
	}
	
	public void setDeltaWaitTime( double n) {
		deltaWaitTime = n;
	}
	
	public void setSpawnSlotFilled(int n, boolean b) {
		spawnSlotFilled[n - 1] = b;
	}
	
	public void resetTurnCount() {
		turnCount = 0;
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
