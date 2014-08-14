package com.daenils.moisei.entities;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.files.FileManager;
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
	private double deltaGlobalCooldownTimeSec;
	private double globalCooldown = 300.0 / 1000.0; // modify first number only (ms)
	
	// TIMERS 'N STUFF
	private double startTurnTimer, deltaTurnTime, deltaTurnTimeSec, pauseTurnTime; // TURN
	private double startGameTimer, deltaGameTime, deltaGameTimeSec, pauseGameTime; // GAME
	private double startWaveTimer, deltaWaveTime, deltaWaveTimeSec, pauseWaveTime; // WAVE
	private double startPauseTimer, deltaPauseTime, deltaPauseTimeSec, turnPauseTime, wavePauseTime, totalPauseTime; // PAUSE
	private double startWaitTimer, deltaWaitTime, deltaWaitTimeSec; // MONSTER WAIT
	
	// MONSTER WAIT
	private boolean isWaitingOn;
	
	private Date d;

	// GAME PAUSE
	public boolean continueGame = true;
	private boolean isPaused;
	private boolean forcedPause;
	private boolean totalUpdated;
	
	// SWITCH BETWEEN DIFFERENT UI VIEWS / TOGGLE UI STUFF
	private boolean percentageView;
	private boolean debugView = true;
	
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
		
		// just so that file is initialized as well, probably a temporary measure
		Gamestats.submitStats_endWave();
		FileManager.saveStatisticsFile();
	}
	
	public void update() {
		if (deltaTurnTime < 0) deltaPauseTime = 0;

		nowTime = nowTime();
		gameFlow();
		
		if (Gamestats.monstersAllDead || Gamestats.playerHP <= 0) setContinueGame(false);
		if (Gamestats.monstersAlive > 0 && !forcedPause && Gamestats.playerHP > 0) setContinueGame(true);
		
		// TODO: temporary solution to the monster spawn before all removed issue
		if (!continueGame && Gamestats.monstersAllDead) removeDead();
		
		
//		System.out.println("WaitingOn? " + isWaitingOn);
		
		getIsMonsterTurn();
		getIsPlayerTurn();
		
		if (turnCount < 1) {
			turnCount++;
			CombatLog.printnt("New turn begins.");
		//	System.out.print("\n+T" + turnCount + " | ");
			}
		
		handleTimers();
		handlePause();
		
		if (input.debugLockAbility && !onGlobalCooldown) {
			Gamestats.player.removeLastAbility();
			enableGlobalCooldown();
		}
		
		if (input.debugUnlockAbility && !onGlobalCooldown) {
			Random rand = new Random();
			int r = rand.nextInt((Ability.getAbilityCount() - 0) + 0) + 0;
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
		
		if (Gamestats.player.weapon != null) weaponString = "\n\nWEAPON: " + Gamestats.player.getWeapon().getName() + " (" + Gamestats.player.getWeapon().getWeaponTypeString() +")"
				+ "\nphDMG: " + Gamestats.player.getWeapon().getDmgRange() + " | HIT CHANCE: " + Gamestats.player.getWeapon().getHitChance() + "%" + " | mDMG: " + Gamestats.player.getWeapon().getDamageValue() + " | heal: " + Gamestats.player.getWeapon().getHealValue()
				+ "\nCHARGES: " + Gamestats.player.getWeapon().getWeaponCharges() + " | HoT: " + Gamestats.player.getWeapon().getHotValue() + " | DoT: " + Gamestats.player.getWeapon().getDotValue() + " | MoT: " + Gamestats.player.getWeapon().getMotValue();
		else if (Gamestats.playerDamage[0] > 0) {
			weaponString = "\n\nWEAPON: " + "Bare hands" + " (null)"
					+ "\nphDMG: " + Gamestats.playerDamage[0] + "-" + Gamestats.playerDamage[1];
		}
		else weaponString = "N / A";
		
		if (deltaWaveTime < 2.0) newWave = true;
		else newWave = false;
		
		dealOTValues(Gamestats.player);
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			dealOTValues(stage.getMonsters().get(i));
		}
		
		checkForCooldowns(Gamestats.player);
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			checkForCooldowns(stage.getMonsters().get(i));
		}
		
		if (deltaGameTime % 1 == 0) { 
			d = new Date((long) nowTime - (long) startGameTimer - 3600000);
		//	System.out.println(d.toString().split(" ")[3].split(":")[2]);
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
		int tick;
		// universal method (abilities):
		for (int i = 0; i < e.abilities.size(); i++) {
			if (e.abilities.get(i).getLastUsed() > 0 && e.abilities.get(i).getIsOT()) {
				if (e.abilities.get(i).getLastUsed() + e.abilities.get(i).getTurnCount() + 1 >
				Gamestats.turnCount && !e.abilities.get(i).isAppliedOT() && 
				(Gamestats.turnCount > e.abilities.get(i).getLastUsed())) {
					tick = (int) (e.abilities.get(i).getLastUsed() + Gamestats.turnCount - 2);
					e.applyOTs(e.abilities.get(i), tick);
					e.abilities.get(i).setAppliedOT(true);
				}
			}
		}
		
		// universal method (weapons):
		if (e.weapon != null) {
			if (e.weapon.getLastUsed() > 0 && e.weapon.getIsOT()) {
				if (e.weapon.getLastUsed() + e.weapon.getTurnCount() + 1 >
				Gamestats.turnCount && !e.weapon.isAppliedOT() &&
				(Gamestats.turnCount > e.weapon.getLastUsed())) {
					tick = (int) (e.weapon.getLastUsed() + Gamestats.turnCount - 2);
					e.applyOTs(e.weapon, tick);
					e.weapon.setAppliedOT(true);
				}
			}
		}
	}
	
	public void handleTimers() {
		if (continueGame) {
		isPaused = false;
		
		// temporary solution to the pause issues:
		deltaTurnTime = nowTime - startTurnTimer;
		if (turnCount > 1) deltaTurnTime -= turnPauseTime;
		deltaTurnTimeSec = (double) ((int) (deltaTurnTime / 100)) / 10;
	
		deltaGameTime = nowTime - startGameTimer - totalPauseTime;
		deltaGameTimeSec = (double) ((int) (deltaGameTime / 100)) / 10;
		
		deltaWaitTime = nowTime - startWaitTimer;
		deltaWaitTimeSec = (double) ((int) (deltaWaitTime / 100)) / 10;
//		System.out.println(deltaWaitTime);
		
		// temporary solution to the pause issues:
		deltaWaveTime = nowTime - startWaveTimer;
		if (turnCount > 1) deltaWaveTime -= wavePauseTime;
		deltaWaveTimeSec = (double) ((int) (deltaWaveTime / 100)) / 10;
		}
		
		deltaGlobalCooldownTime = nowTime - startGlobalCooldownTimer;
		deltaGlobalCooldownTimeSec = (double) ((int) (deltaGlobalCooldownTime / 100)) / 10; // not sure why I can't make it work in the previous line though
		
		
		
		if (deltaGlobalCooldownTimeSec > 0 && deltaGlobalCooldownTimeSec % globalCooldown == 0) {
			onGlobalCooldown = false;
//			System.out.println("GCD TICK: " + deltaGlobalCooldownTimeSec);
		}
	}

	private long nowTime() {
		return System.currentTimeMillis();
	}
	
	public void handlePause() {
		if (!continueGame) {
			if (!isPaused) {
				startPauseTimer = System.currentTimeMillis();
			}
			isPaused = true;
			
			deltaPauseTime = (nowTime - startPauseTimer);
			deltaPauseTimeSec = (double) ((int) (deltaPauseTime / 100)) / 10;
			
			totalUpdated = false;
		}
		else {
			if (!totalUpdated) {
				totalPauseTime += deltaPauseTime;
				turnPauseTime += deltaPauseTime;
				wavePauseTime += deltaPauseTime;
				totalUpdated = true;
			}
		}
	}
	
	public String newLnLeftPad(int n) {
		String returnString = "\n";
		for (int i = 0; i < n; i++) returnString = returnString.concat("\t");
		return returnString;  
	}
	
	// rendering the GUI text here is probably temporary
	public void render(Screen screen) {
		// TODO: move it to the hell outta here and make it nicer and make it work with all the timers
		String timeString = "00:00";
		if (d != null) timeString = d.toString().split(" ")[3].split(":")[1] + ":" + d.toString().split(" ")[3].split(":")[2];
		
		// CURRENT VERSION
		renderVersionInfo(screen);
		
		// TURN INFO BOX
		if (percentageView) renderTurnInfoBoxPercentages(screen, timeString);
		else renderTurnInfoBox(screen, timeString);
		
		// PLAYER INFO BOX
		if (percentageView) renderPlayerInfoBoxPercentages(screen);
		else renderPlayerInfoBox(screen);
		
		// COMBAT LOG
		renderCombatLog(screen);
		
		// MONSTER INFO #3
		if (percentageView) renderMonsterInfoPercentages(screen);
		else renderMonsterInfo(screen);
		
		// NOTIFICATIONS
		renderNotifications(screen);
		
		// ABILITY texts
		for (int i = 0; i < Gamestats.player.abilities.size(); i++)	renderAbilityText(screen, i);
		
		// FLOATING PLAYERDAMAGE
//		if (Gamestats.playerLastActionPoints > Gamestats.playerActionPoints && deltaGlobalCooldownTimeSec < 0.9 && !Gamestats.monstersAllDead)
//			font.render(Gamestats.player.currentTarget.x + 10, Gamestats.player.currentTarget.y - 20, 10, 0xffff1100, Font.font_kubastaBig, 2, "" + Gamestats.playerHitDamage*-1, screen);
		
		// TEMP: GAMESTATS
		font.render(1110, 420, -4, 0xffffff00, Font.font_default, 1, Gamestats.readGameStats(), screen);
		
		// STRICTLY DEBUG
		if (debugView) renderDebugInfo(screen);
		
		//	font.renderXCentered(-1, 199, 12, 0xff444444, Font.font_kubastaBig,1, "Sample String", screen);
		//	font.renderXCentered(2, 202, 12, 0xff444444, Font.font_kubastaBig,1, "Sample String", screen);
		//	font.renderXCentered(200, 12, 0xffddbb00, Font.font_kubastaBig,1, "Sample String", screen);
	}

	private void renderMonsterInfo(Screen screen) {
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
	}
	
	private void renderMonsterInfoPercentages(Screen screen) {
		int n = 0;
//		if (Gamestats.playerTargetCycled < Gamestats.monsterCount && Gamestats.playerTargetCycled > 0) n = Gamestats.playerTargetCycled - 1;
//		else if (Gamestats.playerNeverCycled) n = Gamestats.monsterCount - 1;
		
		for (int i = 0; i < Gamestats.monsterCount; i++) {
			
			// first if line is the new code, second one is the old code which only works with cycling
			
			if (Gamestats.monsterIsAlive[i] && Gamestats.player.currentTarget == stage.getMonsters().get(i)) {
//			if (Gamestats.monsterIsAlive[i] && (n) == i && (Gamestats.playerNeverCycled) ) {
				font.render(stage.getMonsters().get(i).x + 5, stage.getMonsters().get(i).y + 15, -7, 0xffdd0010, "" + Gamestats.monsterId[i]
						+ "\nH:" + stage.getMonsters().get(i).pHealth + "%"
						+ "|M:" + stage.getMonsters().get(i).pMana + "%"
						+ "\nS:" + Gamestats.monsterShield[i] + ""
//						+ " (" + Gamestats.playerHitDamage*-1 + ")"
							
						, screen);
				}
		}
	}

	private void renderCombatLog(Screen screen) {
		if (CombatLog.getLogLength() > 6) {
			for (int i = 0; i < 7; i++) {
				font.render(0, 460 + (i*10), -8, 0xffe5e5e5, CombatLog.getLastLines(6 - i), screen);
			}
		}
	}

	private void renderTurnInfoBox(Screen screen, String timeString) {
		font.render(645, 572, -6, 0xffffff00, "- TURN INFO -", screen);  
		font.render(GUI.screenTurninfoPos, 572, -7, 0xffffff00,
				"\n\n-> " + printWhosTurn() +
				"\nACTIONS LEFT: " + showActionsLeft + "" +
				"\n\nTURN " + turnCount + " - " + deltaTurnTimeSec +
				"\nWAVE " + waveCount + " - " + deltaWaveTimeSec +
				"\nGAME TIME: " + deltaGameTimeSec
		//		"\nGAME TIME: " + timeString
				, screen);
	}
	
	private void renderTurnInfoBoxPercentages(Screen screen, String timeString) {
		font.render(645, 572, -6, 0xffffff00, "- TURN INFO -", screen);  
		font.render(GUI.screenTurninfoPos, 572, -7, 0xffffff00,
				"\n\n-> " + printWhosTurn() +
				"\nACTIONS LEFT: " + getActionsLeftBar() + "" +
				"\n\nTURN " + turnCount + " - " + deltaTurnTimeSec +
				"\nWAVE " + waveCount + " - " + deltaWaveTimeSec +
				"\nGAME TIME: " + deltaGameTimeSec
		//		"\nGAME TIME: " + timeString
				, screen);
	}

	private void renderVersionInfo(Screen screen) {
		font.render(1147, 0, -8, 0, Game.getTitle() + " " + Game.getVersion()
				+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.getProjectStage().length() + 1) + Game.getProjectStage().toUpperCase()
				+ newLnLeftPad(Game.getProjectStage().length() - Game.isFpsLockedString().length() + 4) + Game.isFpsLockedString(), screen);
	}

	private void renderPlayerInfoBox(Screen screen) {
		font.render(GUI.screenPlayerinfoPos+135, 572, -8, 0xffffff00, "- PLAYER INFO -"
				, screen);
		font.render(GUI.screenPlayerinfoPos, 572, -7, 0xffffff00, ""
				+ "\n\nHEALTH: " + Gamestats.playerHP + "/" + Gamestats.playerMaxHP
				+ " (" + Gamestats.playerLastHitReceived + ")"
				+ " | SHIELD: " + Gamestats.playerShield
				+ "\nMANA: " + Gamestats.playerMana + "/" + Gamestats.playerMaxMana
				+ "\nLEVEL: " + Gamestats.playerLevel + " | XP: " + Gamestats.playerXP
				+ weaponString
				, screen);
	}
	
	private void renderPlayerInfoBoxPercentages(Screen screen) {
		font.render(GUI.screenPlayerinfoPos+135, 572, -8, 0xffffff00, "- PLAYER INFO -"
				, screen);
		font.render(GUI.screenPlayerinfoPos, 572, -7, 0xffffff00, ""
				+ "\n\nHEALTH: " + Gamestats.player.pHealth + "%"
				+ " (" + Gamestats.playerLastHitReceived + ")"
				+ " | SHIELD: " + Gamestats.playerShield
				+ "\nMANA: " + Gamestats.player.pMana + "%"
				+ "\nLEVEL: " + Gamestats.playerLevel + " | XP: " + Gamestats.player.pXP + "%"
				+ weaponString
				, screen);
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
							+ "\nTotal GameTime: " + deltaGameTimeSec
							+ "\nMonster deathcount: " + Gamestats.monsterDeathCount
							+ "\nTotal Monster deathcount: " + Gamestats.getTotalMonsterDeathCount()
							+ "\nPl Ability 1 last used: " + Gamestats.player.abilities.get(0).getLastUsed() // these 3 lines are poorly written
			//				+ "\nPl Ability 2 last used: " + Gamestats.player.abilities.get(1).getLastUsed() // they should be removed/changed
			//				+ "\nPl Ability 4 last used: " + Gamestats.player.abilities.get(3).getLastUsed() // as soon as possible
							+ "\nCurrent wave: " + Gamestats.waveCount
							+ "\nGlobalCooldown: " + deltaGlobalCooldownTimeSec 
							+ "\nTargetCycled: " + Gamestats.player.targetCycled
							+ "\nCombat Log length: " + CombatLog.getSize()
							+ "\nGame is paused: " + !continueGame
							+ "\nPause force: " + getForcedPause()
							+ "\nTotal pause time: " + totalPauseTime
	
							
				//			+ "\nMONSTER ATTACKED: " + Gamestats.monstersAttacked
							, screen);
	}
	
	protected void renderNotifications(Screen screen) {
		if (Gamestats.turnCount > 1 && Gamestats.deltaTurnTime < 1.2) {
			font.renderXCentered(2, 132, 12, 0xff9a9a9a, Font.font_kubastaBig, 1, printWhosTurnTop(), screen);
			font.renderXCentered(130, 12, 0xff4d4d4d, Font.font_kubastaBig, 1, printWhosTurnTop(), screen);
		}
		
		if (!continueGame) {
			font.renderXCentered(2, 52, 12, 0xff050505, Font.font_kubastaBig, 1, "GAME PAUSED", screen);
			font.renderXCentered(50, 12, 0xff8d2d8d, Font.font_kubastaBig, 1, "GAME PAUSED", screen);
		}
		
			
			if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime < 2 && Gamestats.waveCount < 2)
				font.renderXCentered(160, 2, 0xff61118e, Font.font_kubastaBig, 2, "Press SPACE to hit your enemy", screen);
			if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime > 3 && Gamestats.deltaTurnTime < 5 && Gamestats.waveCount < 2)
				font.renderXCentered(160, 2, 0xff61118e, Font.font_kubastaBig, 2, "Press ENTER to end your turn", screen);
			if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime > 6 && Gamestats.deltaTurnTime < 8 && Gamestats.waveCount < 2)
				font.renderXCentered(160, 2, 0xff61118e, Font.font_kubastaBig, 2, "Press Q,W,E,R to use your abilities", screen);
			if (Gamestats.turnCount < 2 && Gamestats.deltaTurnTime > 9 && Gamestats.deltaTurnTime < 11 && Gamestats.waveCount < 2)
				font.renderXCentered(160, 2, 0xff61118e, Font.font_kubastaBig, 2, "Press G to switch between weapons", screen);
			
			if (Gamestats.playerHP <= 0) {
				font.renderXCentered(-4, 150, 10, 0xff4d0d0d, Font.font_kubastaBig, 1, "YOU HAVE DIED", screen);
				font.renderXCentered(150, 10, 0xffad0d0d, Font.font_kubastaBig, 1, "YOU HAVE DIED", screen);
			}
			
		//	if (Gamestats.monsterHP[5] < 1)
		//		font.render(580 - 10, 160, -8, 0xffa30300, "Monster DIED", screen);
	}
	
	protected void renderAbilityText(Screen screen, int n) {
		font.render(20 + n * 130, GUI.screenBottomElements + 97, -8, 0, "[#" + Gamestats.player.abilities.get(n).getID() + "] " + Gamestats.player.abilities.get(n).getName(), screen);
		font.render(22 + n * 130, GUI.screenBottomElements + 77, -8, 0xff2020cc, "(" + Gamestats.player.abilities.get(n).getMPcost() + "mp)", screen);
		font.render(110 + n * 130, GUI.screenBottomElements - 30, -8, 0xff20cc20, "(" + Gamestats.player.abilities.get(n).getCooldown() + "cd)", screen);
	}
	
	protected void newTurn() {
		turnPauseTime = 0;
		deltaTurnTime = 0;	
		
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
		
		// reset dot flags
		for (int i = 0; i < Gamestats.player.abilities.size(); i++) {
			Gamestats.player.abilities.get(i).setAppliedOT(false);
			if (Gamestats.player.weapon != null) Gamestats.player.weapon.setAppliedOT(false);
		}
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			for (int l = 0; l < stage.getMonsters().get(i).abilities.size(); l++) {
				stage.getMonsters().get(i).abilities.get(l).setAppliedOT(false);
				if (stage.getMonsters().get(i).weapon != null) stage.getMonsters().get(i).weapon.setAppliedOT(false);
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
		CombatLog.printet("Turn " + Gamestats.turnCount + " has ended in " + deltaTurnTimeSec + " seconds" + " (Game time: " + deltaGameTimeSec + "s)");
		newTurn();
		
		for (int i = 0; i < Gamestats.monsterCount; i++) {
			stage.getMonsters().get(i).resetActionPoints(stage.getMonsters().get(i));
		}
		Monster.resetMonstersAttacked();

	}
	
	protected void endTurn(Entity e) {
	//	removeDead();
		CombatLog.printet("Turn " + Gamestats.turnCount + " has ended in " + deltaTurnTimeSec + " seconds" + " (Game time: " + deltaGameTimeSec + "s)");
		FileManager.saveCombatLogFile();
 
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
		if (!(Gamestats.turnCount == 0)) buffPlayer();
			
		if (stage.checkIfAllDead()) {
			resetGame();
		}
			
		spawnMonster(n);
	
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
		CombatLog.println("" + emma.name + " spawned.");
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
	//	System.out.println("RESTART TIME");
	}

	
	protected void resetGame() {
		Gamestats.submitStats_endWave();
		FileManager.saveStatisticsFile();
	//	resetGameplayTime();
		resetTurnTime();
		resetWaveTime();
		resetTurnCount();
		Gamestats.player.resetActionPoints(Gamestats.player); // this is not really a nice solution
		Gamestats.player.currentTarget = null;
		// reset cooldowns:
		Gamestats.player.resetCooldowns(Gamestats.player);
		for (int i = 0; i < stage.getMonsters().size(); i++)
			stage.getMonsters().get(i).resetCooldowns(stage.getMonsters().get(i));
		
		setContinueGame(true);
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
			CombatLog.println("Player received an additional action point! (" + Gamestats.playerMaxActionPoints + " total)");
		}
		
		// TEMP: MAGIC FOUNTAIN
		if (Gamestats.waveCount % 8 == 0) {
			Gamestats.player.health = Gamestats.player.maxHealth; // ugly, don't do it pls
			Gamestats.player.mana = Gamestats.player.maxMana; // ugly, don't do it pls
			CombatLog.println("Player received a blessing (Health and Mana restored).");
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
		return this.deltaGameTimeSec;
	}
	
	
	public double getDeltaTurnTime() {
		return deltaTurnTimeSec;
	}
	
	public double getTurnStartTime() {
		return startTurnTimer;
	}
	
	public double getStartWaitTimer() {
		return startWaitTimer;
	}
	
	public double getDeltaWaitTime() {
		return deltaWaitTimeSec;
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
		return deltaWaveTimeSec;
	}
	
	public int getWaveCount() {
		return waveCount;
	}
	
	public boolean getNewWave() {
		return newWave;
	}
	
	public boolean getContinueGame() {
		return continueGame;
	}
	
	public boolean getPercentageView() {
		return percentageView;
	}
	
	public boolean getDebugView() {
		return debugView;
	}
	
	public boolean getForcedPause() {
		return forcedPause;
	}
	
	private String getActionsLeftBar() {
		String s = "";
		if (showActionsLeft <= 18) {
			for (int i = 0; i < showActionsLeft; i++) {
				s = s.concat("#");
			}			
		}
		else {
			s = Integer.toString(showActionsLeft);
		}
		
		return s;
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
		deltaWaitTimeSec = n;
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
	
	public void setPercentageView(boolean b) {
		percentageView = b;
	}
	
	public void setDebugView(boolean b) {
		debugView = b;
	}
	
	public void setForcedPause(boolean b) {
		forcedPause = b;
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
