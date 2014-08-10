package com.daenils.moisei.entities;

import java.util.Random;

import com.daenils.moisei.CombatLog;
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
	
	private boolean readyToSpawn;
	
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
	
	// WAVE TIMER STUFF
	private double startWaveTimer;
	private double deltaWaveTime;
	private double pauseWaveTime;
	
	
	// MONSTER WAIT TIMER STUFF
	private double startWaitTimer;
	private double deltaWaitTime;
	private boolean isWaitingOn;
	
	// GAME CONTINUATION AFTER MONSTERS DIED
	public boolean continueGame;
	
	private Keyboard input;
	private Font font;
	
	protected boolean[] spawnSlotFilled = new boolean[5];
	
	protected boolean playerOverride;
	
	public Gameplay(Keyboard input, Stage stage) {
		this.stage = stage;
		this.turnCount = 0;
		this.isMonsterTurn = false;
		this.isPlayerTurn = false;
		this.startGlobalCooldownTimer = System.currentTimeMillis();
		this.startTurnTimer = System.currentTimeMillis();
		resetGameplayTime();
		this.startWaitTimer = System.currentTimeMillis();
		this.startWaveTimer = System.currentTimeMillis();
		
		this.waveCount = 0;
		
		this.input = input;
		font = new Font();
	}
	
	public void update() {
//		System.out.println(newWave);
		gameFlow();
		
		if (!Gamestats.monstersAllDead) continueGame = true;
		if (Gamestats.monstersAllDead) continueGame = false;
		
		// TODO: temporary solution to the monster spawn before all removed issue
		if (!continueGame) removeDead();
		
		
//		System.out.println("WaitingOn? " + isWaitingOn);
		
		getIsMonsterTurn();
		getIsPlayerTurn();
		
		if (turnCount < 1) {
			turnCount++;
			CombatLog.printnt("New turn begins.");
		//	System.out.print("\n+T" + turnCount + " | ");
			}
		
		handleTimers();
		
		if (input.debugLockAbility && !onGlobalCooldown) {
			Gamestats.player.removeLastAbility();
			enableGlobalCooldown();
		}
		
		if (input.debugUnlockAbility && !onGlobalCooldown) {
			Random rand = new Random();
			int r = rand.nextInt((8 - 0) + 0) + 0;
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
		
		if (Gamestats.playerDamage[0] > 0) weaponString = "\nWEAPON DAMAGE: " + Gamestats.playerDamage[0] + "-" + Gamestats.playerDamage[1];
		else weaponString = "N / A";
		
		if (deltaGameTime < 2.0) newWave = true;
		else newWave = false;
		
		dealOTValues(Gamestats.player);
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			dealOTValues(stage.getMonsters().get(i));
		}
		
		checkForCooldowns(Gamestats.player);
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			checkForCooldowns(stage.getMonsters().get(i));
		}
	}

	

	private void checkForCooldowns(Entity e) {
		// universal method:
		for (int i = 0; i < e.abilities.size(); i++) {
			if (e.abilities.get(i).getLastUsed() > 0 && e.abilities.get(i).getCooldown() > 0) {
				if (e.abilities.get(i).getLastUsed() + (e.abilities.get(i).getCooldown() * 2) + 1 > Gamestats.turnCount) {
					e.abilities.get(i).setOnCooldown(true);
				}
				else e.abilities.get(i).setOnCooldown(false);
			}
		}
	}
	
	private void dealOTValues(Entity e) {
		for (int i = 0; i < e.abilities.size(); i++) {
			if (e.abilities.get(i).getLastUsed() > 0 && e.abilities.get(i).getIsOT()) {
				if (e.abilities.get(i).getLastUsed() + e.abilities.get(i).getTurnCount() + 1 >
				Gamestats.turnCount && !e.abilities.get(i).isAppliedOT() && 
				(Gamestats.turnCount > e.abilities.get(i).getLastUsed())) {
					e.applyOTs(e.abilities.get(i));
					e.abilities.get(i).setAppliedOT(true);
				}
			}
		}
	}

	public void handleTimers() {
		nowTime = System.currentTimeMillis();

		if (continueGame) {
		
		deltaTurnTime = (int) (nowTime - startTurnTimer) / 100;
		deltaTurnTime = deltaTurnTime / 10;
		
		deltaGameTime = (int) (nowTime - startGameTimer) / 100;
		deltaGameTime = deltaGameTime / 10;		
		
		deltaWaitTime = (int) (nowTime - startWaitTimer) / 100;
		deltaWaitTime = deltaWaitTime / 10;
//		System.out.println(deltaWaitTime);
		
		deltaWaveTime = (int) (nowTime - startWaveTimer) / 100;
		deltaWaveTime = deltaWaveTime / 10;
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
	}
	
	public String newLnLeftPad(int n) {
		String returnString = "\n";
		for (int i = 0; i < n; i++) returnString = returnString.concat("\t");
		return returnString;  
	}
	
	// rendering the GUI text is probably temporary
	public void render(Screen screen) {
		// CURRENT VERSION
		font.render(1147, 0, -8, 0, Game.getTitle() + " " + Game.getVersion()
				+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.getProjectStage().length() + 1) + Game.getProjectStage().toUpperCase(), screen);
		// TURN INFO BOX
		font.render(645, 572, -6, 0xffffff00, "- TURN INFO -", screen);  
		font.render(GUI.screenTurninfoPos, 572, -7, 0xffffff00,
				"\n\n-> " + printWhosTurn() +
				"\nACTIONS LEFT: " + showActionsLeft + "" +
				"\n\nTURN " + turnCount + " - " + getDeltaTurnTime() +
				"\nWAVE " + waveCount + " - " + getDeltaWaveTime() +
				"\nGAME TIME: " + getDeltaGameTime()
				, screen);
		
		// PLAYER INFO BOX
		font.render(GUI.screenPlayerinfoPos+135, 572, -8, 0xffffff00, "- PLAYER INFO -"
				, screen);
		font.render(GUI.screenPlayerinfoPos, 572, -7, 0xffffff00, ""
				+ "\n\nHEALTH: " + Gamestats.playerHP + "/" + Gamestats.playerMaxHP
				+ " (" + Gamestats.playerLastHitReceived + ")"
				+ " | SHIELD: " + Gamestats.playerShield
				+ "\nMANA: " + Gamestats.playerMana + "/" + Gamestats.playerMaxMana
				+ "\nXP: " + Gamestats.playerXP
				+ "\n\nWEAPON NAME: " + "Default Dagger"
				+ weaponString
				+ " (" + Gamestats.playerHitDamage + ")"
				, screen);

		// COMBAT LOG
		if (CombatLog.getLogLength() > 6) {
			for (int i = 0; i < 7; i++) {
				font.render(0, 460 + (i*10), -8, 0xffe5e5e5, CombatLog.getLastLines(6 - i), screen);
			}
		}
		
		// MONSTER INFO #3
		int n = 0;
//		if (Gamestats.playerTargetCycled < Gamestats.monsterCount && Gamestats.playerTargetCycled > 0) n = Gamestats.playerTargetCycled - 1;
//		else if (Gamestats.playerNeverCycled) n = Gamestats.monsterCount - 1;
		
		for (int i = 0; i < Gamestats.monsterCount; i++) {
			
			// first if line is the new code, second one is the old code which only works with cycling
			
			if (Gamestats.monsterIsAlive[i] && Gamestats.player.currentTarget == stage.getMonsters().get(i)) {
//			if (Gamestats.monsterIsAlive[i] && (n) == i && (Gamestats.playerNeverCycled) ) {
				font.render(stage.getMonsters().get(i).x + 5, stage.getMonsters().get(i).y + 15, -7, 0xffdd0010, "" + Gamestats.monsterId[i]
						+ "\nH:" + Gamestats.monsterHP[i] + "/" + Gamestats.monsterMaxHP[i]
						+ "|M:" + Gamestats.monsterMana[i] + "/" + Gamestats.monsterMaxMana[i]
						+ "\nS:" + Gamestats.monsterShield[i] + ""
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
			font.render(580 + 40, 290 - 10, -8, 0xffff1100, "\n\n" + Gamestats.playerHitDamage*-1, screen);
		
		
		// STRICTLY DEBUG
		renderDebugInfo(screen);
	}
	
	protected void renderDebugInfo(Screen screen) {
		font.render(25, 25, -8, 0xff00deff, "DEBUG STUFF\n"
				//			+ "\nRandom Wait: " + Gamestats.monsterRW
							+ "\nMonsters spawned: " + Gamestats.monsterCount
							+ "\nMonsters alive: " + Gamestats.monstersAlive
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
							+ "\nPl Ability 1 last used: " + Gamestats.player.abilities.get(0).getLastUsed() // these 3 lines are poorly written
			//				+ "\nPl Ability 2 last used: " + Gamestats.player.abilities.get(1).getLastUsed() // they should be removed/changed
			//				+ "\nPl Ability 4 last used: " + Gamestats.player.abilities.get(3).getLastUsed() // as soon as possible
							+ "\nCurrent wave: " + Gamestats.waveCount
							+ "\nGlobalCooldown: " + deltaGlobalCooldownTime 
							+ "\nTargetCycled: " + Gamestats.player.targetCycled
							+ "\nCombat Log length: " + CombatLog.getSize()
	
							
				//			+ "\nMONSTER ATTACKED: " + Gamestats.monstersAttacked
							, screen);
	}
	
	protected void renderNotifications(Screen screen) {
		if (Gamestats.turnCount > 1 && Gamestats.deltaTurnTime < 1.2)
			font.render(590, 160, -4, 0xffff6a05, printWhosTurnTop(), screen);
			
			if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime < 2)
				font.render(495, 160, -4, 0xffff6a05, "Press SPACE to hit your enemy", screen);
			if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime > 3 && Gamestats.deltaTurnTime < 5)
				font.render(495, 160, -4, 0xffff6a05, "Press ENTER to end your turn", screen);
			if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime > 6 && Gamestats.deltaTurnTime < 8)
				font.render(470, 160, -4, 0xffff6a05, "Press Q,W,E,R to use your abilities", screen);
			
		//	if (Gamestats.monsterHP[5] < 1)
		//		font.render(580 - 10, 160, -8, 0xffa30300, "Monster DIED", screen);
	}
	
	protected void renderAbilityText(Screen screen, int n) {
		font.render(20 + n * 130, GUI.screenBottomElements + 97, -8, 0, "[#" + Gamestats.player.abilities.get(n).getID() + "] " + Gamestats.player.abilities.get(n).getName(), screen);
	}
	
	protected void newTurn() {
		CombatLog.printnt("New turn begins.");
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
	//	System.out.print("\n+T" + turnCount + " | ");
//		System.out.println("\nA new turn has began! (Turn " + turnCount + ")");
		
		// reset dot flags new
		for (int i = 0; i < Gamestats.player.abilities.size(); i++) {
			Gamestats.player.abilities.get(i).setAppliedOT(false);
		}
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			for (int l = 0; l < stage.getMonsters().get(i).abilities.size(); l++) {
				stage.getMonsters().get(i).abilities.get(l).setAppliedOT(false);
			}
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
	//	removeDead();
		CombatLog.printet("Turn " + Gamestats.turnCount + " has ended in " + deltaTurnTime + " seconds" + " (Game time: " + deltaGameTime + "s)");
		deltaTurnTime = 0;
		newTurn();
		
		for (int i = 0; i < Gamestats.monsterCount; i++) {
			stage.getMonsters().get(i).resetActionPoints(stage.getMonsters().get(i));
		}
		Monster.resetMonstersAttacked();

	}
	
	protected void endTurn(Entity e) {
	//	removeDead();
		CombatLog.printet("Turn " + Gamestats.turnCount + " has ended in " + deltaTurnTime + " seconds" + " (Game time: " + deltaGameTime + "s)");
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
	
	public void newMonsterWave() {
		newMonsterWave(1);
	}

	public void newMonsterWave(int n) {
		if (stage.checkIfAllDead()) {
			resetGame();
		}
			spawnMonster(n);
			
		if (!(Gamestats.turnCount == 0)) buffPlayer();
		waveCount++;
		

	}

	private void removeDead() {
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			if ((!stage.getMonsters().get(i).isAlive) || stage.getMonsters().get(i).forceRemoved) stage.getMonsters().get(i).needsRemove = true;
			else stage.getMonsters().get(i).needsRemove = false;
		}
	}
	
	private void addMonster(int slot) {
		Random rand = new Random();
		int r = rand.nextInt((4 - 1) + 1) + 1;
		Monster emma = new Monster(r, slot, Gamestats.player);
		stage.add(emma);
		CombatLog.println("" + emma.name + "spawned.");
		Game.getGameplay().enableGlobalCooldown(); 


	}
	
	protected void spawnMonster() {
		spawnMonster(1);
	}
	
	protected void spawnMonster(int n) {
			if (getSpawnSlotFilled(5)) {
	/*			System.out.println("All done here: " + 
			Gamestats.spawnSlotFilled[0] + Gamestats.spawnSlotFilled[1] + Gamestats.spawnSlotFilled[2]
			+ Gamestats.spawnSlotFilled[3] + Gamestats.spawnSlotFilled[4]); */
				Game.getGameplay().enableGlobalCooldown(); 
				}
			
		for (int i = 0; i < n; i++) {
		//	System.out.println("SPAWNING...");
			onGlobalCooldown = false;
			if (!getSpawnSlotFilled(1) && !onGlobalCooldown) {
				addMonster(1);
				enableGlobalCooldown();
			}
			else if (!getSpawnSlotFilled(2) && getSpawnSlotFilled(1)
					&& !onGlobalCooldown) {
				addMonster(2);
				enableGlobalCooldown();
			}
			else if (!getSpawnSlotFilled(3) && getSpawnSlotFilled(2)
					&& !onGlobalCooldown) {
				addMonster(3);
				enableGlobalCooldown();
			}
			else if (!getSpawnSlotFilled(4) && getSpawnSlotFilled(3)
					&& !onGlobalCooldown) {
				addMonster(4);
				enableGlobalCooldown();
			}
			else if (!getSpawnSlotFilled(5) && getSpawnSlotFilled(4)
					&& !onGlobalCooldown) {
				addMonster(5);
				enableGlobalCooldown();
			}
		}
	}

	
	protected void resetGame() {
		Gamestats.submitGameStats();
	//	resetGameplayTime();
		resetTurnTime();
		resetWaveTime();
		Gamestats.player.resetActionPoints(Gamestats.player); // this is not really a nice solution
		Gamestats.player.currentTarget = null;
		resetTurnCount();
		// reset cooldowns:
		Gamestats.player.resetCooldowns(Gamestats.player);
		for (int i = 0; i < stage.getMonsters().size(); i++)
			stage.getMonsters().get(i).resetCooldowns(stage.getMonsters().get(i));
	}
	
	protected void buffPlayer() {
		int buffHp = 10;
		int buffMp = 10;
		Gamestats.player.addHealth(buffHp);
		Gamestats.player.addMana(buffMp);
		CombatLog.println("Player buffed for +" + buffHp + " health and +" + buffMp +" mana" );
		
		if (Gamestats.waveCount % 4 == 0) {
			Gamestats.player.addMaxActionPoints(1);
			Gamestats.player.actionPoints++;
		}
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
		if (getIsMonsterTurn()) whosTurn = "AI TURN";
		if (getIsPlayerTurn()) whosTurn = "YOUR TURN";
		return whosTurn;
	}
	
	public String printWhosTurnTop() {
		String whosTurn = "N / A";
		if (getIsMonsterTurn()) whosTurn = " AI TURN";
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
	
	public double getStartWaveTimer() {
		return startWaveTimer;
	}
	
	public double getDeltaWaveTime() {
		return deltaWaveTime;
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
	
	public void resetWaveTime() {
		this.startWaveTimer = System.currentTimeMillis();
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
	
	public void setContinueGame(boolean b) {
		continueGame = b;
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
	
	// GAMEFLOW CONTROL
	public void gameFlow() {
		if (Gamestats.monsterCount < 1 && isBetween(Gamestats.playerXP, 0, 30) && (continueGame || Gamestats.turnCount == 0)) {newMonsterWave(1);}
		else if (Gamestats.monsterCount < 1 && isBetween(Gamestats.playerXP, 30, 110) && continueGame) newMonsterWave(2);
		else if (Gamestats.monsterCount < 1 && isBetween(Gamestats.playerXP, 110, 230) && continueGame) newMonsterWave(3);
		else if (Gamestats.monsterCount < 1 && isBetween(Gamestats.playerXP, 230, 350) && continueGame) newMonsterWave(4);
		else if (Gamestats.monsterCount < 1 && Gamestats.playerXP >= 350 && continueGame) newMonsterWave(5);
	}
	
	public boolean isBetween(int comparedNum, int min, int max) {
		return (comparedNum >= min && comparedNum < max);
	}

}
