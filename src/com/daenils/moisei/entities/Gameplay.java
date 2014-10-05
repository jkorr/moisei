package com.daenils.moisei.entities;

import java.awt.Font;
import java.awt.Graphics;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;
import com.daenils.moisei.entities.equipments.*;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Text;
import com.daenils.moisei.graphics.GUI;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Stage;
import com.daenils.moisei.graphics.Window;

public class Gameplay {
	private final int BILLION = 1000000000;
	
	private Stage stage;
	private Game game;
	private GUI gui;
	
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
	private double nowTimeOLD;
	private double deltaGlobalCooldownTime;
	private double deltaGlobalCooldownTimeSec;
	private double globalCooldown = 400.0 / 1000.0; // modify first number only (ms)
	
	// TIMERS 'N STUFF
	private double startWaitTimer, deltaWaitTime, deltaWaitTimeSec; // MONSTER WAIT
	
	// (NEW) TIMERS 'N STUFF
	private long startTimeRunning, startTimeStage, startTimeWave, startTimeTurn, startTimeWait;
	private long deltaTimeRunning, deltaTimeStage, deltaTimeWave, deltaTimeTurn, deltaTimeWait;
	private long endTimeStage, endTimeWave, endTimeTurn, endTimeWait;
	private long nowTime;
	
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
	private boolean debugView = false;
	
	private Keyboard input;
	private Mouse inputM;
	private Text font;
	
	protected boolean[] spawnSlotFilled = new boolean[5];
	
	protected boolean playerOverride;
	
	// LEVEL-XPNEEDED MAP
	protected Map<Byte,Integer> mapLevelRanges = new HashMap<Byte,Integer>();
	
	// NOTIFS
	int notificationStartTime;
	protected boolean notificationLevelUp;
	
	// REFUGEES FROM GAMESTATS
	protected boolean monstersAllDead;
	protected int monstersAlive;
	
	// SHOP STUFF
	protected boolean shopHasOpened; // set it true when first opens so it will only open once, set it back to false at
											// the start of a new wave probably
	
	// LETTER INVENTORY STUFF
	protected boolean letterWindowHasOpened;
	
	public Gameplay(Keyboard input, Mouse inputM, Stage stage, Game game, GUI gui) {
		this.game = game;
		this.stage = stage;
		this.gui = gui;
		this.turnCount = 0;
		this.isMonsterTurn = false;
		this.isPlayerTurn = false;
		this.startGlobalCooldownTimer = System.currentTimeMillis();
		resetStageTime();
		
		// NEW TIMER STUFF
		this.startTimeRunning = System.nanoTime();
		this.startTimeStage = System.nanoTime();
		this.startTimeWave = System.nanoTime();
		this.startTimeTurn = System.nanoTime();
		
		this.waveCount = 0;
		
		this.input = input;
		this.inputM = inputM;
		font = new Text();
		
		// initialize mapLevels:
		initLevelRanges();
		
		// just so that file is initialized as well, probably a temporary measure
	//	Gamestats.submitStats_endWave();
	//	FileManager.saveStatisticsFile();
		
		// Why is it here, isn't it a duplicate?
		stage.setPlayer(new Player(input, inputM, null, stage));
	}
	
	private void initLevelRanges() {
		mapLevelRanges.put((byte) 1, 10); // FUCKING WHY?!
		mapLevelRanges.put((byte) 2, 30);
		mapLevelRanges.put((byte) 3, 80);
		mapLevelRanges.put((byte) 4, 150);
		mapLevelRanges.put((byte) 5, 350);
		mapLevelRanges.put((byte) 6, 900);
		mapLevelRanges.put((byte) 7, 2200);
		mapLevelRanges.put((byte) 8, 5000);
		mapLevelRanges.put((byte) 9, 12500);
		mapLevelRanges.put((byte) 10, 100000); // placeholder, reaching it is not intended
	}

	public void update() {
		if (!letterWindowHasOpened) {
			openLetterWindow();
			letterWindowHasOpened = true;
		}
		
		if (shopHasOpened) shop();
		if (shopHasOpened && turnCount > 2) shopHasOpened = false;
		
		// GAMESTATS REFUGEES
		monstersAllDead = stage.checkIfAllDead();
		monstersAlive = checkMonstersAliveCount();

		nowTimeOLD = nowTime();
		nowTime = System.nanoTime();
		gameFlow();
		
		if (monstersAllDead || stage.getPlayer().getHealth() <= 0) setContinueGame(false);
		if (monstersAlive > 0 && !forcedPause && stage.getPlayer().getHealth() > 0) setContinueGame(true);
		
		// TODO: temporary solution to the monster spawn before all removed issue
		if (!continueGame && monstersAllDead) removeDead();
		
		if (notificationStartTime > 0 && (deltaTimeStage / BILLION) - notificationStartTime >= 2) {
			notificationLevelUp = false;
		}
		
		getIsMonsterTurn();
		getIsPlayerTurn();
		
		if (turnCount < 1) {
			turnCount++;
			CombatLog.println("New turn begins.");
		//	System.out.print("\n+T" + turnCount + " | ");
			}
		
		handleTimers();
		handlePause();
		
		if (input.debugLockAbility && !onGlobalCooldown) {
			stage.getPlayer().removeLastAbility();
			enableGlobalCooldown();
		}
		
		if (input.debugUnlockAbility && !onGlobalCooldown) {
			Random rand = new Random();
			int r = rand.nextInt((Ability.getAbilityCount() - 0) + 0) + 0;
			stage.getPlayer().unlockAbility(stage.getPlayer(), r);
			enableGlobalCooldown();
		}
		
		
		
		if (isPlayerTurn) showActionsLeft = stage.getPlayer().actionPoints;
		if (isMonsterTurn) {
			int actionSum = 0;
			for (int i = 0; i < stage.getMonsters().size(); i++) {
				actionSum += stage.getMonsters().get(i).actionPoints;
			}
			showActionsLeft = actionSum;
		}
		
		if (stage.getPlayer().getWeapon() != null) weaponString = "\n\n" + stage.getPlayer().getWeapon().getName() + " (" + stage.getPlayer().getWeapon().getWeaponTypeString() +")"
				+ "\ndmg: " + stage.getPlayer().getWeapon().getDmgRange() + "|hit chance: " + stage.getPlayer().getWeapon().getHitChance() + "%" + "|mDMG: " + stage.getPlayer().getWeapon().getDamageValue() + "|heal: " + stage.getPlayer().getWeapon().getHealValue()
				+ "\n\nCHARGES: " + stage.getPlayer().getWeapon().getWeaponCharges() + "|HoT: " + stage.getPlayer().getWeapon().getHotValue() + "|DoT: " + stage.getPlayer().getWeapon().getDotValue() + "|MoT: " + stage.getPlayer().getWeapon().getMotValue();
		else if (stage.getPlayer().damage[0] > 0) {
			weaponString = "\n\n" + "Bare hands" + " (null)"
					+ "\nphDMG: " + stage.getPlayer().damage[0] + "-" + stage.getPlayer().damage[1];
		}
		else weaponString = "N / A";
		
		if ((deltaTimeWave / BILLION) < 2.0) newWave = true;
		else newWave = false;
		
		dealOTValues(stage.getPlayer());
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			dealOTValues(stage.getMonsters().get(i));
		}
		
		checkForCooldowns(stage.getPlayer());
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			checkForCooldowns(stage.getMonsters().get(i));
		}
	}

	

	private void checkForCooldowns(Entity e) {
		// universal method:
		for (int i = 0; i < e.abilities.size(); i++) {
			if (e.abilities.get(i).getLastUsed() > 0 && e.abilities.get(i).getCooldown() > 0) {
				if (e.abilities.get(i).getLastUsed() + (e.abilities.get(i).getCooldown() * 2) + 1 > turnCount) {
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
				turnCount && !e.abilities.get(i).isAppliedOT() && 
				(turnCount > e.abilities.get(i).getLastUsed())) {
					e.applyOTs(e.abilities.get(i));
					e.abilities.get(i).setAppliedOT(true);
				} else e.tick = 0;
			}
		}
		
		// universal method (weapons):
		if (e.weapon != null) {
			if (e.weapon.getLastUsed() > 0 && e.weapon.getIsOT()) {
				if (e.weapon.getLastUsed() + e.weapon.getTurnCount() + 1 >
				turnCount && !e.weapon.isAppliedOT() &&
				(turnCount > e.weapon.getLastUsed())) {
					e.applyOTs(e.weapon);
					e.weapon.setAppliedOT(true);
				} else e.tick = 0;
			}
		}
	}
	
	public void handleTimers() {
		if (continueGame) {
		isPaused = false;
		
		deltaTimeRunning = nowTime - startTimeRunning;
		deltaTimeStage = (nowTime - startTimeStage) + endTimeStage;
		deltaTimeWave = (nowTime - startTimeWave) + endTimeWave;
		deltaTimeTurn = (nowTime - startTimeTurn) + endTimeTurn;
		
		// temporary solution to the pause issues:
		deltaWaitTime = nowTimeOLD - startWaitTimer;
		deltaWaitTimeSec = (double) ((int) (deltaWaitTime / 100)) / 10;
		}
		
		deltaGlobalCooldownTime = nowTimeOLD - startGlobalCooldownTimer;
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
				endTimeStage = deltaTimeStage;
				endTimeWave = deltaTimeWave;
				endTimeTurn = deltaTimeTurn;
			}
			isPaused = true;
			
			startTimeStage = nowTime;
			startTimeWave = nowTime;
			startTimeTurn = nowTime;
		}
	}
	
	public String newLnLeftPad(int n) {
		String returnString = "\n";
		for (int i = 0; i < n; i++) returnString = returnString.concat("\t");
		return returnString;  
	}
	
	// rendering the GUI text here is probably temporary
	public void render(Screen screen) {
		// render hiteffect (player)
		/*
		if (stage.getPlayer().getHealth() < stage.getPlayer().lastHealth && TIMESTAMP + DISPLAY_TIME < NOW) {
			screen.renderBgFill(0xffff0000);
		}
		
		*/
		
		// TEST STRING
//		font.renderNew("Hi!", 50, 50, 0, Font.SANS_SERIF, 12, g);
		
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
		
		// WEAPON INFO
		// TODO: make it work similarly to the ability resource info text rendering
		if (debugView)
		renderWeaponInfoWindow(screen);
		
		// ABILITY texts
		for (int i = 0; i < stage.getPlayer().abilities.size(); i++)	 {
			renderAbilityHelperText(screen, i);
			if (stage.getPlayer().abilities.get(i).showTooltip) renderAbilityText(screen, i);
		}
		
		// HITCHANCE
		for (int i = 0; i < stage.getMonsters().size(); i++) {
		if (isPlayerTurn && stage.getPlayer().lastActionPoints > stage.getPlayer().actionPoints && deltaGlobalCooldownTimeSec < 1.5
				|| 
				isMonsterTurn && stage.getMonsters().get(i).lastActionPoints > stage.getMonsters().get(i).actionPoints && stage.getMonsters().get(i).lastHitChance > 0
				) {			
			renderHitChanceBox(screen);
		}
		}
		
		// FLOATING PLAYERDAMAGE
//		if (Gamestats.playerLastActionPoints > Gamestats.playerActionPoints && deltaGlobalCooldownTimeSec < 0.9 && !Gamestats.monstersAllDead)
//			font.render(Gamestats.player.currentTarget.x + 10, Gamestats.player.currentTarget.y - 20, 10, 0xffff1100, Font.font_kubastaBig, 2, "" + Gamestats.playerHitDamage*-1, screen);
		
		// TEMP: GAMESTATS
	//	font.render(1110, 420, -4, 0xffffff00, Font.font_default, 1, Gamestats.readGameStats(), screen);
		
		// STRICTLY DEBUG
		if (debugView) renderDebugInfo(screen);
		
		
		//	font.renderXCentered(-1, 199, 12, 0xff444444, Font.font_kubastaBig,1, "Sample String", screen);
		//	font.renderXCentered(2, 202, 12, 0xff444444, Font.font_kubastaBig,1, "Sample String", screen);
		//	font.renderXCentered(200, 12, 0xffddbb00, Font.font_kubastaBig,1, "Sample String", screen);
	}

	private void renderHitChanceBox(Screen screen) {
		int renderedHitChance = 0;
		if (isPlayerTurn) renderedHitChance = stage.getPlayer().getLastHitChance();
		else if (isMonsterTurn) {
			for (int i = 0; i < stage.getMonsters().size(); i++) {
				if (stage.getMonsters().get(i).lastActionPoints > stage.getMonsters().get(i).actionPoints) {
					renderedHitChance = stage.getMonsters().get(i).getLastHitChance();
				}
			}
		}
		font.render(580, 275, -2, 0, Text.font_kubastaBig, 2, "" + renderedHitChance, screen);
	}

	private void renderMonsterInfo(Screen screen) {
		int n = 0;
//		if (Gamestats.playerTargetCycled < Gamestats.monsterCount && Gamestats.playerTargetCycled > 0) n = Gamestats.playerTargetCycled - 1;
//		else if (Gamestats.playerNeverCycled) n = Gamestats.monsterCount - 1;
		
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			
			// first if line is the new code, second one is the old code which only works with cycling
			
			 
			if (stage.getMonsters().get(i).isAlive && stage.getPlayer().currentTarget == stage.getMonsters().get(i)) {
//			if (Gamestats.monsterIsAlive[i] && (n) == i && (Gamestats.playerNeverCycled) ) {
				renderMonsterBarBg(screen, i, stage.getMonsters().get(i).x + 12, stage.getMonsters().get(i).y + 7);
				renderMonsterHealthBar(screen, i, stage.getMonsters().get(i).x + 12, stage.getMonsters().get(i).y + 7);
				
				renderMonsterBarBg(screen, i, stage.getMonsters().get(i).x + 12, stage.getMonsters().get(i).y + 10);
				renderMonsterManaBar(screen, i, stage.getMonsters().get(i).x + 12, stage.getMonsters().get(i).y + 10);
				font.render(stage.getMonsters().get(i).x + 5, stage.getMonsters().get(i).y + 15, -7, 0xffdd0010, "" + stage.getMonsters().get(i).name
						, screen);
					if (stage.getMonsters().get(i).showDetails) {
						font.render(stage.getMonsters().get(i).x + 5, stage.getMonsters().get(i).y - 0, -7, 0xffbb0000, 
							"" + stage.getMonsters().get(i).getHealth() + "/" + stage.getMonsters().get(i).maxHealth
							, screen);
						if (stage.getMonsters().get(i).getMana() > 0) font.render(stage.getMonsters().get(i).x + 42, stage.getMonsters().get(i).y - 0, -7, 0xff0000bb, 
								"" + stage.getMonsters().get(i).getMana() + "/" + stage.getMonsters().get(i).maxMana
								, screen);
						if (stage.getMonsters().get(i).getShield() > 0) font.render(stage.getMonsters().get(i).x + 42, stage.getMonsters().get(i).y - 8, -7, 0xff00bbbb, 
								"" + stage.getMonsters().get(i).getShield()
								, screen);
					}
				}
		}
	}
	
	private void renderMonsterBarBg(Screen screen, int monster, int x, int y) {
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < 50; i++) {
				screen.renderPixel(x + i, y + k, 0xffbbbbbb);
			}			
		}
	}
	
	private void renderMonsterManaBar(Screen screen, int monster, int x, int y) {
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < 50 * (stage.getMonsters().get(monster).pMana / 100.0); i++) {
				screen.renderPixel(x + i, y + k, 0xff0000bb);
			}			
		}
	}
	
	private void renderMonsterHealthBar(Screen screen, int monster, int x, int y) {
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < 50 * (stage.getMonsters().get(monster).pHealth / 100.0); i++) {
				screen.renderPixel(x + i, y + k, 0xffbb0000);
			}			
		}
	}
	
	private void renderMonsterInfoPercentages(Screen screen) {
		int n = 0;
//		if (Gamestats.playerTargetCycled < Gamestats.monsterCount && Gamestats.playerTargetCycled > 0) n = Gamestats.playerTargetCycled - 1;
//		else if (Gamestats.playerNeverCycled) n = Gamestats.monsterCount - 1;
		
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			
			// first if line is the new code, second one is the old code which only works with cycling
			
			if (stage.getMonsters().get(i).isAlive && stage.getPlayer().currentTarget == stage.getMonsters().get(i)) {
//			if (Gamestats.monsterIsAlive[i] && (n) == i && (Gamestats.playerNeverCycled) ) {
				font.render(stage.getMonsters().get(i).x + 5, stage.getMonsters().get(i).y + 15, -7, 0xffdd0010, "" + stage.getMonsters().get(i).id
						+ "\nH:" + stage.getMonsters().get(i).pHealth + "%"
						+ "|M:" + stage.getMonsters().get(i).pMana + "%"
						+ "\nS:" + stage.getMonsters().get(i).getShield() + ""
//						+ " (" + Gamestats.playerHitDamage*-1 + ")"
							
						, screen);
				}
		}
	}

	private void renderCombatLog(Screen screen) {
		if (CombatLog.getLogLength() > 4) {
			for (int i = 0; i < 5; i++) {
				font.render(0, 250 + (i*10), -8, 0xffe5e5e5, Text.font_default, 1.0, CombatLog.getLastLines(4 - i), screen);
			}
		}
	}

	private void renderTurnInfoBox(Screen screen, String timeString) {
//			font.render(645/2, 572/2, -6, 0xffffff00, Text.font_default, 1, "- TURN INFO -", screen);  
		font.render(GUI.screenTurninfoPos - 35, 303, -8, 0xffdddddd, Text.font_default, 1,
				"  T\n " + turnCount + "\n\n\n\n" +  (int) (deltaTimeTurn / BILLION)
				, screen);
		
		font.render(GUI.screenTurninfoPos + 65, 303, -8, 0xffdddddd, Text.font_default, 1,
				"W\n " + waveCount + "\n\n\n\n" + (int) (deltaTimeWave / BILLION)
				, screen);
		
		// ACTIONS BAR
		font.render(GUI.screenTurninfoPos - 6, 340, -8, 0xffffcc00, Text.font_default, 1,
		"" + getActionsLeftBar() + ""
				, screen);
		
		// TEMPORARY END TURN "BUTTON"
		font.render(192, 305, -8, 0xff252575, Text.font_default, 1, "[END TURN]", screen);
	}
	
	private void renderTurnInfoBoxPercentages(Screen screen, String timeString) {
		font.render(645/2, 572/2, -6, 0xffffff00, "- TURN INFO -", screen);  
		font.render(GUI.screenTurninfoPos, 572/2, -7, 0xffffff00,
				"\n\n-> " + printWhosTurn() +
				"\nACTIONS LEFT: " + getActionsLeftBar() + "" +
				"\n\nTURN " + turnCount + " - " + (int) (deltaTimeTurn / BILLION) +
				"\nWAVE " + waveCount + " - " + (int) (deltaTimeWave / BILLION) +
				"\nGAME TIME: " + (int) (deltaTimeStage / BILLION)
		//		"\nGAME TIME: " + timeString
				, screen);
	}

	private void renderVersionInfo(Screen screen) {
		font.render(525, 2, -8, 0xff000000, Text.font_default, 1, Game.getTitle() + " " + Game.getVersion()
				+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.getProjectStage().length() + 1) + Game.getProjectStage().toUpperCase()
				+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.isFpsLockedString().length() + 1) + Game.isFpsLockedString(), screen);
	}

	private void renderPlayerInfoBox(Screen screen) {
		// BARS
		renderPlayerHealthBar(screen);
		renderPlayerManaBar(screen);
		renderPlayerXPBar(screen);
		
		// HP TEXT
		font.render(GUI.screenPlayerinfoPos-153, 303, -8, 0xffffffff, Text.font_default, 1, ""
				+ "" + stage.getPlayer().getHealth() + "/" + stage.getPlayer().maxHealth
				+ " (" + stage.getPlayer().getShield() + ")"
				, screen);
		
		// MANA TEXT
		font.render(GUI.screenPlayerinfoPos-153, 314, -8, 0xffffffff, Text.font_default, 1, ""
				+ "" + stage.getPlayer().getMana() + "/" + stage.getPlayer().maxMana
				, screen);
		
		// XP TEXT
		font.render(GUI.screenPlayerinfoPos-153, 325, -8, 0xffffffff, Text.font_default, 1, ""
				+ "" + "" + stage.getPlayer().getXp() + "/" + stage.getPlayer().getXpNeeded() 
				, screen);
		
		// LEVEL
		font.render(GUI.screenPlayerinfoPos-266, 308, 10, 0, Text.font_kubastaBig, 1, ""
				+ "" + stage.getPlayer().level				 
				, screen);
		font.render(GUI.screenPlayerinfoPos-263, 308, 10, 0xffffffff, Text.font_kubastaBig, 1, ""
				+ "" + stage.getPlayer().level				 
				, screen);
		
		// GOLD
		font.render(GUI.screenPlayerinfoPos-145, 350, -7, 0xffffffff, Text.font_default, 1, 
				"$" +
				stage.getPlayer().getGoldAmount()
				, screen);
	}
	
	private void renderPlayerHealthBar(Screen screen) {
		for (int k = 0; k < 8; k++) {
			for (int i = 0; i < 76 * (stage.getPlayer().pHealth / 100.0); i++) {
				screen.renderPixel(282 + i, 302 + k, 0xffbb0000);
			}			
		}
	}
	
	
	private void renderPlayerManaBar(Screen screen) {
		for (int k = 0; k < 8; k++) {
			for (int i = 0; i < 76 * (stage.getPlayer().pMana / 100.0); i++) {
				screen.renderPixel(282 + i, 313 + k, 0xff0000bb);
			}			
		}
	}
	
	private void renderPlayerXPBar(Screen screen) {
		for (int k = 0; k < 8; k++) {
			for (int i = 0; i < 76 * (stage.getPlayer().pXP / 100.0); i++) {
				screen.renderPixel(282 + i, 324 + k, 0xffbbbb00);
			}			
		}
	}
	
	
	
	private void renderWeaponInfoWindow(Screen screen) {
		font.render(380, 264, -8, 0xff00ff00, Text.font_default, 1, "" + weaponString, screen);
	}
	
	private void renderPlayerInfoBoxPercentages(Screen screen) {
		font.render(GUI.screenPlayerinfoPos+135/2, 572/2, -8, 0xffffff00, "- PLAYER INFO -"
				, screen);
		font.render(GUI.screenPlayerinfoPos, 572/2, -7, 0xffffff00, ""
				+ "\n\nHEALTH: " + stage.getPlayer().pHealth + "%"
				+ " (" + stage.getPlayer().getLastHitReceived() + ")"
				+ " | SHIELD: " + stage.getPlayer().getShield()
				+ "\nMANA: " + stage.getPlayer().pMana + "%"
				+ "\nLEVEL: " + stage.getPlayer().level + " | XP: " + stage.getPlayer().pXP + "%"
				+ weaponString
				, screen);
	}
	
	protected void renderDebugInfo(Screen screen) {
		font.render(-4, 2, -8, 0xffeeaa00, Text.font_default, 1, "DEBUG STUFF\n"
				//			+ "\nRandom Wait: " + Gamestats.monsterRW
							+ "\nMonsters spawned: " + stage.getMonsters().size()
							+ "\nMonsters alive: " + monstersAlive
							+ "\n\n" + individualMonsterDetails() + "\n"
							+ "\nTotal TurnCount: " + Gamestats.getTotalTurnCount()
							+ "\nTotal RunTime: " + (int) (deltaTimeRunning / BILLION)
							+ "\nTotal GameTime: " + (int) (deltaTimeStage / BILLION)
							+ "\nMonster deathcount: " + Monster.getDeathCount()
							+ "\nTotal Monster deathcount: " + Gamestats.getTotalMonsterDeathCount()
			//				+ "\nPl Ability 1 last used: " + Gamestats.player.abilities.get(0).getLastUsed() // these 3 lines are poorly written
			//				+ "\nPl Ability 2 last used: " + Gamestats.player.abilities.get(1).getLastUsed() // they should be removed/changed
			//				+ "\nPl Ability 4 last used: " + Gamestats.player.abilities.get(3).getLastUsed() // as soon as possible
			//				+ "\nPl Ability 4 onCooldown: " + Gamestats.player.abilities.get(3).isOnCooldown()
				//			+ "\nCurrent wave: " + waveCount
							+ "\nGlobalCooldown: " + deltaGlobalCooldownTimeSec 
				//			+ "\nTargetCycled: " + stage.getPlayer().targetCycled
							+ "\nCombat Log length: " + CombatLog.getSize()
							+ "\nGame is paused: " + !continueGame
							+ "\nPause forced: " + getForcedPause()
							+ "\nPlayer's spellpower: " + stage.getPlayer().spellPower
							+ "\n\nmX: " + Mouse.getX() + " mY: " + Mouse.getY() + " mB: " + Mouse.getB() 
							
	
							
				//			+ "\nMONSTER ATTACKED: " + Gamestats.monstersAttacked
							, screen);
	}

	private String individualMonsterDetails() {
		String string = "";
		string = string.concat("Monster HP:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).getHealth());
		}
		string = string.concat("\nMonster AP:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).actionPoints);
		}
		string = string.concat("\nMonster MP:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).getMana());
		}
		string = string.concat("\nMonster isAlive:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).getIsAlive());
		}
		string = string.concat("\nMonster isWaiting:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).isWaiting);
		}
		string = string.concat("\nMonster LVL:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).level);
		}
		string = string.concat("\nMonster DMG:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).damage[0] + "-" + stage.getMonsters().get(i).damage[1]);
		}
		string = string.concat("\nMonster HITDMG:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).hitDamage);
		}
		string = string.concat("\nMonster randomWait:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).getRandomWait());
		}
		return string;
	}
	
	protected void renderNotifications(Screen screen) {
		int deltaTimeTurnSeconds = (int) (deltaTimeTurn / BILLION);
		if (turnCount > 1 && deltaTimeTurnSeconds < 1.2) {
			font.renderXCentered(2, 62, 12, 0xff9a9a9a, Text.font_kubastaBig, 1.5, printWhosTurnTop(), screen);
			font.renderXCentered(60, 12, 0xff4d4d4d, Text.font_kubastaBig, 1.5, printWhosTurnTop(), screen);
		}
		
		if (!continueGame) {
			font.renderXCentered(-3, 20, 12, 0xff050505, Text.font_kubastaBig, 1.1, "GAME PAUSED", screen);
			font.renderXCentered(20, 12, 0xff8d2d8d, Text.font_kubastaBig, 1.1, "GAME PAUSED", screen);
		}
		
		if (notificationLevelUp) {
			font.renderXCentered(2, 82, 12, 0xff050505, Text.font_kubastaBig, 1.1, "LEVEL UP!", screen);
			font.renderXCentered(80, 12, 0xffdfbf00, Text.font_kubastaBig, 1.1, "LEVEL UP!", screen);
		}
			
			if (turnCount < 2 && deltaTimeTurnSeconds < 2 && waveCount < 2)
				font.renderXCentered(60, 2, 0xff61118e, Text.font_kubastaBig, 2.5, "Press SPACE to hit your enemy", screen);
			if (turnCount < 2 && deltaTimeTurnSeconds > 3 && deltaTimeTurnSeconds < 5 && waveCount < 2)
				font.renderXCentered(60, 2, 0xff61118e, Text.font_kubastaBig, 2.5, "Press ENTER to end your turn", screen);
			if (turnCount < 2 && deltaTimeTurnSeconds > 6 && deltaTimeTurnSeconds < 8 && waveCount < 2)
				font.renderXCentered(60, 2, 0xff61118e, Text.font_kubastaBig, 2.5, "Press Q,W,E,R to use your abilities", screen);
			if (turnCount < 2 && deltaTimeTurnSeconds > 9 && deltaTimeTurnSeconds < 11 && waveCount < 2)
				font.renderXCentered(60, 2, 0xff61118e, Text.font_kubastaBig, 2.5, "Press G to switch between weapons", screen);
			
			if (stage.getPlayer().getHealth() <= 0) {
				font.renderXCentered(-4, 150, 10, 0xff4d0d0d, Text.font_kubastaBig, 1, "YOU HAVE DIED", screen);
				font.renderXCentered(150, 10, 0xffad0d0d, Text.font_kubastaBig, 1, "YOU HAVE DIED", screen);
			}
			
		//	if (Gamestats.monsterHP[5] < 1)
		//		font.render(580 - 10, 160, -8, 0xffa30300, "Monster DIED", screen);
	}
	
	protected void renderAbilityText(Screen screen, int n) {
		// MANA COSTS
		font.render(27 + n * 35, GUI.screenBottomElements + 3, -9, 0xff252525, "" + stage.getPlayer().abilities.get(n).getMPcost() + "", screen);
		font.render(28 + n * 35, GUI.screenBottomElements + 3, -9, 0xff00ccff, "" + stage.getPlayer().abilities.get(n).getMPcost() + "", screen);
		
		// COOLDOWNS
		font.render(4 + n * 35, GUI.screenBottomElements + 43, -9, 0xff252525, "" + stage.getPlayer().abilities.get(n).getCooldown() + "", screen);
		font.render(5 + n * 35, GUI.screenBottomElements + 43, -9, 0xffccff00, "" + stage.getPlayer().abilities.get(n).getCooldown() + "", screen);
		
		// ONCOOLDOWN WARNING (temporary)
		font.render(13 + n * 35, GUI.screenBottomElements + 22, -8, 0xff252525, Text.font_default, 2, "" + stage.getPlayer().abilities.get(n).isOnCooldownText(), screen);
		font.render(14 + n * 35, GUI.screenBottomElements + 22, -8, 0xffccff00, Text.font_default, 1, "" + stage.getPlayer().abilities.get(n).isOnCooldownText(), screen);
	}

	protected void renderAbilityHelperText(Screen screen, int n) {
		char[] qwer = {'Q', 'W', 'E', 'R'};
		// QWER HELP KEYBIND
		font.render(31 + n * 35, GUI.screenBottomElements + 38, -8, 0xff252525, "" + qwer[n], screen);
		font.render(32 + n * 35, GUI.screenBottomElements + 38, -8, 0xffffffff, "" + qwer[n], screen);
	}
	
	protected void newTurn() {
		startTimeTurn = nowTime;
		endTimeTurn = 0;
		// delta = null?
		CombatLog.printnt("New turn begins.");
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
		for (int i = 0; i < stage.getPlayer().abilities.size(); i++) {
			stage.getPlayer().abilities.get(i).setAppliedOT(false);
			if (stage.getPlayer().weapon != null) stage.getPlayer().weapon.setAppliedOT(false);
		}
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			for (int l = 0; l < stage.getMonsters().get(i).abilities.size(); l++) {
				stage.getMonsters().get(i).abilities.get(l).setAppliedOT(false);
				if (stage.getMonsters().get(i).weapon != null) stage.getMonsters().get(i).weapon.setAppliedOT(false);
			}
		}
		
		// reset isStunned
		stage.getPlayer().isStunned = false;
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			stage.getMonsters().get(i).isStunned = false;
		}
	}
	
	protected void endTurn() {
		// THIS METHOD IS SPECIFICALLY TAILORED FOR THE MONSTER
		// USE THE OTHER ONE WITH THE PLAYER
	//	removeDead();
		CombatLog.printet("Turn " + turnCount + " has ended in " + (int) (deltaTimeTurn / BILLION) + " seconds" + " (Game time: " + (int) (deltaTimeStage / BILLION) + "s)");
		newTurn();
		
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			stage.getMonsters().get(i).resetActionPoints(stage.getMonsters().get(i));
		}
		Monster.resetMonstersAttacked();

	}
	
	protected void endTurn(Entity e) {
	//	removeDead();
		CombatLog.printet("Turn " + turnCount + " has ended in " + (int) (deltaTimeTurn / BILLION) + " seconds" + " (Game time: " + (int) (deltaTimeStage / BILLION) + "s)");
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
			for (int i = 0; i < stage.getMonsters().size(); i++) {
				stage.getMonsters().get(i).resetActionPoints(stage.getMonsters().get(i));
			}
			Monster.resetMonstersAttacked();
		}
		
	}
	
	public void newMonsterWave() {
		newMonsterWave(1);
	}

	public void newMonsterWave(int n) {
		if (!(turnCount == 0)) buffPlayer();
			
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
		int n = 2; // failsafe (temporary?)
		if (Monster.monstersLoaded > 1) n = Monster.monstersLoaded;
		Random rand = new Random();
		int r = rand.nextInt(((n - 1) - 1) + 1) + 1;
		Monster emma = new Monster(r, slot, stage.getPlayer(), stage);
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
		stage.getPlayer().resetActionPoints(stage.getPlayer()); // this is not really a nice solution
		stage.getPlayer().currentTarget = null;
		// reset cooldowns:
		stage.getPlayer().resetCooldowns(stage.getPlayer());
		for (int i = 0; i < stage.getMonsters().size(); i++)
			stage.getMonsters().get(i).resetCooldowns(stage.getMonsters().get(i));
		
		setContinueGame(true);
	}
	
	protected void buffPlayer() {
	/*
	 * 	int buffHp = 10;
	 *	int buffMp = 10;
	 *	Gamestats.player.addHealth(buffHp);
	 *	Gamestats.player.addMana(buffMp);
	 *	CombatLog.println("Player buffed for +" + buffHp + " health and +" + buffMp +" mana" );
	 *	
	 *	if (Gamestats.waveCount % 4 == 0) {
	 *		Gamestats.player.addMaxActionPoints(1);
	 *		Gamestats.player.actionPoints++;
	 *		CombatLog.println("Player received an additional action point! (" + Gamestats.playerMaxActionPoints + " total)");
	 *	}
	 *	
	 */
		
		// TEMP: MAGIC FOUNTAIN
		if (waveCount % 8 == 0) {
			stage.getPlayer().health = stage.getPlayer().maxHealth; // ugly, don't do it pls
			stage.getPlayer().mana = stage.getPlayer().maxMana; // ugly, don't do it pls
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
	
	public double getDeltaTimeStage() {
		return this.deltaTimeStage;
	}
	
	public double getDeltaTimeTurn() {
		return deltaTimeTurn;
	}
	
	public double getTurnTimeStart() {
		return startTimeTurn;
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
	
	public double getStartTimeWave() {
		return startTimeWave;
	}
	
	public double getDeltaTimeWave() {
		return deltaTimeWave;
	}
	
	public long getTurnCount() {
		return turnCount;
	}
	
	public int getWaveCount() {
		return waveCount;
	}
	
	public boolean getNewWave() {
		return newWave;
	}
	
	public Stage getStage() {
		return stage;
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
		if (showActionsLeft <= 10) {
			for (int i = 0; i < showActionsLeft; i++) {
				s = s.concat("#");
			}			
		}
		else {
			s = "    ";
			s = s.concat(Integer.toString(showActionsLeft));
			s = s.concat("#");
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
	
	public void resetStageTime() {
		this.startTimeStage = nowTime;
		this.endTimeStage = 0;
	}
	
	public void resetTurnTime() {
		this.startTimeTurn = nowTime;
		this.endTimeTurn = 0;
	}
	
	public void resetWaveTime() {
		this.startTimeWave = nowTime;
		this.endTimeWave = 0;
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
	
	public void setMonsterTurn(boolean b) {
		this.isMonsterTurn = b;
	}
	
	public void setPlayerTurn(boolean b) {
		this.isPlayerTurn = b;
	}
	
	public void setNotificationLevelUp() {
		notificationStartTime = (int) (deltaTimeStage / BILLION);
		notificationLevelUp = true;
	}
	
	// GAMEFLOW CONTROL
	public void gameFlow() {
		// add player here : might be a duplicate, player is also added in the constructor !
		if ( stage.getPlayer() == null) stage.setPlayer(new Player(input, inputM, null, stage));
		
		// check for shop popup
		if (waveCount %  3 == 0 && waveCount > 0 && turnCount == 1 && !shopHasOpened) {
			openShop();
		}
		
		// earliest version of "game progession"
		if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().level, 0, 3) && (continueGame || turnCount == 0)) {newMonsterWave(1);}
		else if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().level, 3, 5) && continueGame) newMonsterWave(2);
		else if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().level, 5, 7) && continueGame) newMonsterWave(3);
		else if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().level, 7, 9) && continueGame) newMonsterWave(4);
		else if (stage.getMonsters().size() < 1 && stage.getPlayer().level >= 9 && continueGame) newMonsterWave(5);
	}
	
	public boolean isBetween(int comparedNum, int min, int max) {
		return (comparedNum >= min && comparedNum < max);
	}
	
	// MECHANIC: LETTER WINDOW
	private void openLetterWindow() {
		gui.createWindow(240, 200, 164, 120, 0xff555555, "LETTERS");
		gui.getWindow("letters").add(5,3);
		gui.getWindow("letters").add(Letter.testletter);
	}
	
	// MECHANIC: SHOP
	private void openShop() {
		shopHasOpened = true;
		gui.createWindow(200, 120, 240, 120, 0xff4444ee, "SHOP");
		gui.getWindow("shop").add(7,2);
		gui.getWindow("shop").add(1, Window.BUTTON_CLOSE);
		gui.getWindow("shop").add(new Weapon(null, 1));
		gui.getWindow("shop").add(new Weapon(null, 2));
		gui.getWindow("shop").add(new Weapon(null, 4));
	}
	
	private void shop() {
		if (Mouse.getB() == 1
				&& gui.getWindow("shop") != null
				&& gui.getWindow("shop").getRequestedItem() != null
				&& gui.getWindow("shop").getRequestedItem().getShowTooltip()
				&& !onGlobalCooldown) {
			buyItem(gui.getWindow("shop").getRequestedItem());
			enableGlobalCooldown();
		}
	}
	
	
	private void buyItem(Equipment item) {
		boolean enablePurchase = false;
		if (stage.getPlayer().getGoldAmount() >= item.getVendorPrice()) {
			if (item.isUnique()) {
				if (item instanceof Weapon && stage.getPlayer().hasWeaponID(item.getID())) enablePurchase = false;
				else enablePurchase = true;
			} else if (!item.isUnique()) enablePurchase = true;
			
		}
		else {
			enablePurchase = false;
			CombatLog.println("You cannot afford that.");
		}
		
		if (enablePurchase) {
			stage.getPlayer().unlockWeapon(stage.getPlayer(), item.getID());
			stage.getPlayer().removeGold(item.getVendorPrice());
			System.out.print("\nBuying " + item.getName());
		}
	}

	// GAMESTATS REFUGEES
	private int checkMonstersAliveCount() {
		int n = 0;
		for (int i =0; i < stage.getMonsters().size(); i++) {
			if (stage.getMonsters().get(i).isAlive) n++;
		}
		return n;
	}

}
