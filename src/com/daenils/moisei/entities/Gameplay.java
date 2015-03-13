package com.daenils.moisei.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;
import com.daenils.moisei.entities.equipments.*;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Notification;
import com.daenils.moisei.graphics.Text;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Stage;

public class Gameplay {
	private final int BILLION = 1000000000;
	
	private List<Notification> notifications = new ArrayList<Notification>();
	
	private Stage stage;
	
	private long turnCount;
	private boolean isPlayerTurn;
	private boolean isMonsterTurn;
	
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

	// GAME PAUSE
	public boolean continueGame = true;
	private boolean isPaused;
	private boolean forcedPause;
	
	// SWITCH BETWEEN DIFFERENT UI VIEWS / TOGGLE UI STUFF
	private boolean debugView = false;
	

	private Text font;
	
	protected boolean playerOverride;
	
	// LEVEL-XPNEEDED MAP
	protected Map<Byte,Integer> mapLevelRanges = new HashMap<Byte,Integer>();
	
	// NOTIFS
	int notificationStartTime;
	protected boolean notificationLevelUp;
	
	// REFUGEES FROM GAMESTATS
	protected boolean monstersAllDead;
	protected int monstersAlive;
	
	// GAMESTATS
	// SAVED STUFF
	public static long savedTurnCount;
	public static int savedWaveCount;
	public static double savedDeltaGameTime;
	public static int savedMonsterDeathCount;
	
	public static int savedPlayerLevel;
	public static int savedPlayerTotalXP;
	public static int savedPlayerMaxHP;
	public static int savedPlayerMaxMana;
	public static int savedPlayerMaxAP;
	
	public static int monsterDeathCount;
	
	// SAVED STUFF -- BUT NOT YET IMPLEMENTED
	public static int savedMonsterCount;
	public static int savedPlayerDamageDealt; // needs a playerDamageDealt as well?
	public static int savedMonsterDamageDealt; // -- " --

	// SHOP STUFF
	protected boolean shopHasOpened; // set it true when first opens so it will only open once, set it back to false at
											// the start of a new wave probably
	

	private int[] elementPercentage = new int[4];
	
	public Gameplay(Stage stage) {
		this.stage = stage;
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
		
		font = new Text();
		
		// initialize mapLevels:
		initLevelRanges();
		
		// just so that file is initialized as well, probably a temporary measure
	//	Gamestats.submitStats_endWave();
	//	FileManager.saveStatisticsFile();
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
		// GAMESTATS REFUGEES
		monstersAllDead = stage.checkIfAllDead();
		monstersAlive = checkMonstersAliveCount();
		if (stage.getMonsters().size() > 0 && !stage.getMonsters().get(0).isAlive) {
			monsterDeathCount++;
		}

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
		
		// NOTIFICATIONS
		for (int i = 0; i < notifications.size(); i++) {
			notifications.get(i).update();
		}
		
		remove();
		
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
		
		// NOTIFICATIONS
		for (int i = 0; i < notifications.size(); i++) {
			notifications.get(i).render(screen);
		}
		
		// render hiteffect (player)
		/*
		if (stage.getPlayer().getHealth() < stage.getPlayer().lastHealth && TIMESTAMP + DISPLAY_TIME < NOW) {
			screen.renderBgFill(0xffff0000);
		}
		
		*/
		
		// TEST STRING
//		font.renderNew("Hi!", 50, 50, 0, Font.SANS_SERIF, 12, g);
				
		// CURRENT VERSION
		renderVersionInfo(screen);
		if (!debugView) renderDebugInfoLetterDroptable(screen);
		
		// TURN INFO BOX
//		if (percentageView) renderTurnInfoBoxPercentages(screen, timeString);
//		else renderTurnInfoBox(screen, timeString);
		
		// PLAYER INFO BOX
//		if (percentageView) renderPlayerInfoBoxPercentages(screen);
//		else renderPlayerInfoBox(screen);
		
		renderPlayerInfo(screen);
		renderMonsterInfo(screen);
		
		// COMBAT LOG
		renderCombatLog(screen);
		
		renderTurnInfo(screen);
		
		renderElementalBars(screen);
		
		// MONSTER INFO #3
//		if (percentageView) renderMonsterInfoPercentages(screen);
//		else renderMonsterInfo(screen);
		
		// NOTIFICATIONS
		renderNotifications(screen);
		
		// WEAPON INFO
		// TODO: make it work similarly to the ability resource info text rendering
//		if (debugView)
//		renderWeaponInfoWindow(screen);
		
		// ABILITY texts
//		for (int i = 0; i < stage.getPlayer().abilities.size(); i++)	 {
//			renderAbilityHelperText(screen, i);
//			if (stage.getPlayer().abilities.get(i).showTooltip) renderAbilityText(screen, i);
//		}
		
		// HITCHANCE
//		for (int i = 0; i < stage.getMonsters().size(); i++) {
//		if (isPlayerTurn && stage.getPlayer().lastActionPoints > stage.getPlayer().actionPoints && deltaGlobalCooldownTimeSec < 1.5
//				|| 
//				isMonsterTurn && stage.getMonsters().get(i).lastActionPoints > stage.getMonsters().get(i).actionPoints && stage.getMonsters().get(i).lastHitChance > 0
//				) {			
//			renderHitChanceBox(screen);
//		}
//		}
		
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

	private void renderElementalBars(Screen screen) {
		// DEBUG MODE: VALUES
		renderElementalValues(screen);
		
		// REAL DEAL
		int x = 450, y = 300;
		int w = 90, h = 10;
		int[] segmentMark = {9, 9+18, 9+18+27, 9+18+27+36};
		 
		
		for (int i = 0; i < elementPercentage.length; i++)
			elementPercentage[i] = getStage().getPlayer().getElementalPower(i) * 3; // WIDTH DIVIDED BY CAPSUM
		
		// 1 FIRE
		renderElementalBar(screen, x, y, w, h, 0xff800000);
		renderElementalBar(screen, x, y, w, h, 0xffff0000, elementPercentage[0]);
		renderSegmentMarks(screen, x, y, h, segmentMark);
		
		// 2 WATER
		renderElementalBar(screen, x, y+h+2, w, h, 0xff000080);
		renderElementalBar(screen, x, y+h+2, w, h, 0xff0000ff, elementPercentage[1]);
		renderSegmentMarks(screen, x, y+h+2, h, segmentMark);
	
		// 3 EARTH
		renderElementalBar(screen, x+w+5, y, w, h, 0xff008000);
		renderElementalBar(screen, x+w+5, y, w, h, 0xff00ff00, elementPercentage[2]);
		renderSegmentMarks(screen, x+w+5, y, h, segmentMark);
		
		// 4 WIND
		renderElementalBar(screen, x+w+5, y+h+2, w, h, 0xff808080);
		renderElementalBar(screen, x+w+5, y+h+2, w, h, 0xffaaaaaa, elementPercentage[3]);
		renderSegmentMarks(screen, x+w+5, y+h+2, h, segmentMark);
		
		// SEGMENT MARKERS (TEMP CODE PROBABLY WILL BE FIXED IN ART)
		// 3 - 6 - 9 - 12 = 9 - 18 - 27 - 36
	
		
	}

	private void renderSegmentMarks(Screen screen, int x, int y, int h, int[] segmentMark) {
		for (int l = 0; l < 4; l++)
			for (int k = 0; k < h; k++)
				for (int i = 0; i < 1; i++)
					screen.renderPixel(x + segmentMark[l] + i, y + k, 0xffffffff);
	}
	
	private void renderElementalBar(Screen screen, int x, int y, int w, int h, int col) {
		for (int k = 0; k < h; k++)
			for (int i = 0; i < w; i++) 
				screen.renderPixel(x + i, y + k, col);
	}
	
	private void renderElementalBar(Screen screen, int x, int y, int w, int h, int col, int value) {
		for (int k = 0; k < h; k++)
			for (int i = 0; i < value; i++) 
				screen.renderPixel(x + i, y + k, col);
	}
	
	private void renderElementalValues(Screen screen) {
		font.render(5+12, 350, -8, 0xffffff, Text.font_default, 1, ""+getStage().getPlayer().getElementalPower(0), screen);
		font.render(5+42, 350, -8, 0xffffff, Text.font_default, 1,  ""+getStage().getPlayer().getElementalPower(1), screen);
		font.render(5+72, 350, -8, 0xffffff, Text.font_default, 1,  ""+getStage().getPlayer().getElementalPower(2), screen);
		font.render(5+102, 350, -8, 0xffffff, Text.font_default, 1,  ""+getStage().getPlayer().getElementalPower(3), screen);
		font.renderColored(5, 350, -8, -1, Text.font_default, 1, "2F:  4WA:  3EA:  5WI:", screen);
	}

	private void renderCombatLog(Screen screen) {
		if (CombatLog.getLogLength() > 4) {
			for (int i = 0; i < 5; i++) {
				font.render(0, 250 + (i*10), -8, 0xffe5e5e5, Text.font_default, 1.0, CombatLog.getLastLines(4 - i), screen);
			}
		}
	}
	
	private void renderTurnInfo(Screen screen) {
//		font.render(645/2, 572/2, -6, 0xffffff00, Text.font_default, 1, "- TURN INFO -", screen); 
		// TURNCOUNT
		char[] turnCountRender = new char[2];
		if (turnCount < 10) {
			turnCountRender[0] = (char) 48;
			turnCountRender[1] = (turnCount + "").charAt(0);
		} else {
			turnCountRender[0] = (turnCount + "").charAt(0);
			turnCountRender[1] = (turnCount + "").charAt(1);
		}
		
		font.render(259, 7, 10, 0, Text.font_kubastaBig, 1, ""
				+ turnCountRender[0] + turnCountRender[1]				 
				, screen);
		font.render(262, 7, 10, 0xfffefefe, Text.font_kubastaBig, 1, ""
				+ turnCountRender[0] + turnCountRender[1]				 
				, screen);
		
		// GAMETIME
		int gtSec, gtMin;
		gtMin = (int) ((deltaTimeStage / BILLION) / 60);
		gtSec = (int) ((deltaTimeStage / BILLION) % 60);
		
		String gtMinString, gtSecString;
		
		if (gtMin < 10) {
			gtMinString = "0" + gtMin;
		} else gtMinString = "" + gtMin;
		
		if (gtSec < 10) {
			gtSecString = "0" + gtSec;
		} else gtSecString = "" + gtSec;
		
		String gtString = gtMinString + ":" + gtSecString;
		font.render(295, 54, -8, 0, Text.font_default, 1, gtString, screen);
		font.render(295, 55, -8, 0xffffffff, Text.font_default, 1, gtString, screen);
		
/*	font.render(GUI.screenTurninfoPos - 35, 303, -8, 0xffdddddd, Text.font_default, 1,
			"  T\n " + turnCount + "\n\n\n\n" +  (int) (deltaTimeTurn / BILLION)
			, screen);
	
	font.render(GUI.screenTurninfoPos + 65, 303, -8, 0xffdddddd, Text.font_default, 1,
			"W\n " + waveCount + "\n\n\n\n" + (int) (deltaTimeWave / BILLION)
			, screen);
	*/
	
	// ACTIONS BAR
//	font.render(GUI.screenTurninfoPos - 6, 340, -8, 0xffffcc00, Text.font_default, 1,
//	"" + getActionsLeftBar() + ""
//			, screen);
	
//	// TEMPORARY END TURN "BUTTON"
//	font.render(192, 305, -8, 0xff252575, Text.font_default, 1, "[END TURN]", screen);
}
	
	private void renderVersionInfo(Screen screen) {
		font.render(525, 2, -8, 0xff000000, Text.font_default, 1, Game.getTitle() + " " + Game.getVersion()
				+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.getProjectStage().length() + 1) + Game.getProjectStage().toUpperCase()
				+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.isFpsLockedString().length() + 1) + Game.isFpsLockedString(), screen);
	}
	
	private void renderPlayerInfo(Screen screen) {
		// BARS
		renderPlayerHealthBar(screen);
		renderPlayerXPBar(screen);
		
		// HP TEXT
		font.render(202, 31, -8, 0xffffffff, Text.font_default, 1, ""
				+ "" + stage.getPlayer().pHealth + "%"
		//		+ "" + stage.getPlayer().getHealth() + "/" + stage.getPlayer().maxHealth
		//		+ " (" + stage.getPlayer().getShield() + ")"
				, screen);
		
		// LEVEL
	//	font.render(163, 50, 10, 0, Text.font_kubastaBig, 1, ""
	//			+ "" + stage.getPlayer().level				 
	//			, screen);
	//	font.render(160, 50, 10, 0xffffffff, Text.font_kubastaBig, 1, ""
	//			+ "" + stage.getPlayer().level				 
	//			, screen);
		
		// GOLD
	//	font.render(GUI.screenPlayerinfoPos-145, 350, -7, 0xffffffff, Text.font_default, 1, 
	//			"$" +
	//			stage.getPlayer().getGoldAmount()
	//			, screen);
		
		// WINDBUFF
		if (getStage().getPlayer().damageReduction > 0) {
			font.render(156, 46, -8, 0xffa4a4a4, Text.font_default, 1, 
					"DMGR " + getStage().getPlayer().damageReduction + "%"
					, screen);
			font.render(155, 46, -8, 0xffbbbbbb, Text.font_default, 1, 
					"DMGR " + getStage().getPlayer().damageReduction + "%"
					, screen);
		}
		
	//	if ((deltaTimeTurn / BILLION) < 2 && isMonsterTurn) {
	//		for (int i = 0; i < getStage().getPlayer().currentWordLength; i++) {
	//			int x = 198, y = 96;
	//			font.render(x + (i*6), y, -8, 0xff555555, Text.font_default, 1, getStage().getPlayer().currentWord[i] + ""
	//					, screen);
	//			font.render((x-1) + (i*6), y, -8, getStage().getPlayer().currentWordColors[i], Text.font_default, 1, getStage().getPlayer().currentWord[i]+ ""
	//					, screen);
	//		}			
	//	}
		
		// PLAYER WORD (TYPING)
		for (int i = 0; i < getStage().getPlayer().letterBar.size(); i++) {
			int x = 198, y = 116;
			font.render(x + (i*6), y, -8, 0xff555555, Text.font_default, 1, getStage().getPlayer().letterBar.get(i).value + ""
						, screen);
			font.render((x) + (i*6), y-1, -8, getStage().getPlayer().letterBar.get(i).frameColor, Text.font_default, 1, getStage().getPlayer().letterBar.get(i).value+ ""
					, screen);
		}
		
/*			if ((deltaTimeTurn / BILLION) < 2 && isMonsterTurn) {
				for (int i = 0; i < getStage().getPlayer().currentWordLength; i++) {
					int x = 198, y = 116;
					font.render(x + (i*6), y, -8, 0xff555555, Text.font_default, 1, getStage().getPlayer().currentWord[i] + ""
							, screen);
					font.render((x) + (i*6), y-1, -8, getStage().getPlayer().currentWordColors[i], Text.font_default, 1, getStage().getPlayer().currentWord[i]+ ""
							, screen);
				}
			} */
		
	
		// MONSTER WORD
		/*
		if (getStage().getMonsters().size() > 0 && (deltaTimeTurn / BILLION) < 2 && isPlayerTurn) {
			for (int i = 0; i < getStage().getMonsters().get(0).currentWordLength; i++) {
				int x = 380, y = 96;
					font.render(x + (i*6), y, -8, 0xff555555, Text.font_default, 1, getStage().getMonsters().get(0).currentWord[i] + ""
						, screen);
					font.render((x-1) + (i*6), y, -8, 0xffffffff, Text.font_default, 1, getStage().getMonsters().get(0).currentWord[i] + ""
							, screen);
			}
		}
		*/
		
	}
	
	private void renderMonsterInfo(Screen screen) {
		renderEnemyHealthBar(screen);
		renderEnemyXPBar(screen);
		
		// HP TEXT
		if (stage.getMonsters().size() > 0) {
			font.render(402, 31, -8, 0xffffffff, Text.font_default, 1, ""
					+ "" + stage.getMonsters().get(0).pHealth + "%"
		//			+ "" + stage.getPlayer().getHealth() + "/" + stage.getPlayer().maxHealth
		//			+ " (" + stage.getPlayer().getShield() + ")"
					, screen);
		}
	}
	
	private void renderPlayerHealthBar(Screen screen) {
		for (int k = 0; k < 20; k++) {
			for (int i = 0; i < 120 * (100.0 / 100.0); i++) {
				screen.renderPixel(160 + i, 25 + k, 0xff660000);
			}	
			for (int i = 0; i < 120 * (stage.getPlayer().pHealth / 100.0); i++) {
				screen.renderPixel(160 + i, 25 + k, 0xffbb0000);
			}			
		}
	}
	
	private void renderEnemyHealthBar(Screen screen) {
		for (int k = 0; k < 20; k++) {
			for (int i = 0; i < 120 * (100.0 / 100.0); i++) {
				screen.renderPixel(360 + i, 25 + k, 0xff660000);
			}	
			if (stage.getMonsters().size() > 0) {
				for (int i = 0; i < 120 * (stage.getMonsters().get(0).pHealth / 100.0); i++) {
					screen.renderPixel(360 + i, 25 + k, 0xffbb0000);
				}	
			}
		}
	}
	
	
/*	private void renderPlayerManaBar(Screen screen) {
		for (int k = 0; k < 8; k++) {
			for (int i = 0; i < 76 * (stage.getPlayer().pMana / 100.0); i++) {
				screen.renderPixel(282 + i, 313 + k, 0xff0000bb);
			}			
		}
	} */
	
	private void renderPlayerXPBar(Screen screen) {
		for (int k = 0; k < 2; k++) {
			// bg
			for (int i = 0; i < 120 * (100.0 / 100.0); i++) {
				screen.renderPixel(160 + i, 23 + k, 0xff666600);
			}	
			// value
			for (int i = 0; i < 120 * (stage.getPlayer().pXP / 100.0); i++) {
				screen.renderPixel(160 + i, 23 + k, 0xffbbbb00);
			}			
		}
	}
	
	private void renderEnemyXPBar(Screen screen) {
		for (int k = 0; k < 2; k++) {
			// bg
			for (int i = 0; i < 120 * (100.0 / 100.0); i++) {
				screen.renderPixel(360 + i, 23 + k, 0xff666666);
			}	
			// value
			if (stage.getMonsters().size() > 0) {
				for (int i = 0; i < 120 * (100 / 100.0); i++) {
					screen.renderPixel(360 + i, 23 + k, 0xffffffff); // TODO: make this a "class" / "type" color thingy
				}		
			}
		}
	}
	
	protected void renderDebugInfo(Screen screen) {
		font.render(-4, 2, -8, 0xffeeaa00, Text.font_default, 1, "DEBUG STUFF\n"
				//			+ "\nRandom Wait: " + Gamestats.monsterRW
							+ "\nMonsters spawned: " + stage.getMonsters().size()
							+ "\nMonsters alive: " + monstersAlive
							+ "\n\n" + individualMonsterDetails() + "\n"
							+ "\n" + getStage().getPlayer().letterCountString
							+ "\nVowels: " + getStage().getPlayer().vowelCount
							+ "/ Consonants: " + getStage().getPlayer().consonantCount
							+ "/ TOTAL: " + getStage().getPlayer().letterInventory.size()
							+ "\nTotal TurnCount: " + getTotalTurnCount()
							+ "\nTotal RunTime: " + (int) (deltaTimeRunning / BILLION)
							+ "\nTotal GameTime: " + (int) (deltaTimeStage / BILLION)
							+ "\nMonster deathcount: " + Monster.getDeathCount()
							+ "\nTotal Monster deathcount: " + getTotalMonsterDeathCount()
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
	
	protected void renderDebugInfoLetterDroptable(Screen screen) {
		String droptable = "DROPTABLE:" + "[" + getStage().getPlayer().vowelCount + ":" + getStage().getPlayer().consonantCount + "]";
		for (int i = 0; i < 26; i++) {
			droptable += "\n" + (char) (i+65) + ":" + getStage().getPlayer().letterDroprate[i] + " | " + getStage().getPlayer().letterDroprateBracket[i];
		}
		
		font.render(-4, 2, -8, 0xffaa0055, Text.font_default, 1, "DEBUG STUFF" + " [" + getStage().getPlayer().letterCountString + "]" + "\n"
				+ droptable
				+ "\nTOTAL: " + getStage().getPlayer().rollMax
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
		
		// RESET DMG REDUCTION ON PLAYER
		if (this.isPlayerTurn) getStage().getPlayer().damageReduction = 0;
		
		// LETTER STUFF
		if (getStage().getPlayer().getLetterInventory().size() < getStage().getPlayer().initialLetterSpawn) {
	/*		// check vowels --> THIS IS THE OLD MAKESHIFT SYSTEM
			int vowelCounter = 0;
			for (int i = 0; i < getStage().getPlayer().getLetterInventory().size(); i++) {
				if (getStage().getPlayer().isVowel(getStage().getPlayer().getLetterInventory().get(i).value))
					vowelCount++;
			}
			CombatLog.printet("Vowelcount:" + vowelCount);
			
			// the number 5 below is fixed for 40% value
			if (vowelCount < 6) getStage().getPlayer().spawnVowels(6 - vowelCount);
			
			getStage().getPlayer().spawnConsonants(15 - getStage().getPlayer().getLetterInventory().size());
			
			*/
			
			getStage().getPlayer().spawnLettersNEW(getStage().getPlayer().initialLetterSpawn - getStage().getPlayer().getLetterInventory().size());
			
	//		getStage().getPlayer().spawnVowels(15 - getStage().getPlayer().getLetterInventory().size());
	//		while (getStage().getPlayer().getLetterInventory().size() < 10) getStage().getPlayer().spawnLetter();
			
			// PRINT LETTERCOUNT
	//		getStage().getPlayer().printLetterCount();
		}
		
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
		if (!getStage().getMonsters().get(0).isStunned) displayMonsterLastWord();
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

	private void displayMonsterLastWord() {
		String monsterWord = "";
		for (int i = 0; i < getStage().getMonsters().get(0).currentWordLength; i++)
			monsterWord += getStage().getMonsters().get(0).currentWord[i];

		Notification currentWord = new Notification(monsterWord, 2, Text.font_default, 0xffffffff, true, 380, 116);
		notifications.add(currentWord);
	}
	
	private void displayPlayerLastWord() {
		String playerWord = "";
		for (int i = 0; i < getStage().getPlayer().currentWordLength; i++)
			playerWord += getStage().getPlayer().currentWord[i];
		
		System.out.println(getStage().getPlayer().currentWord);

		Notification currentWord = new Notification(playerWord, 2, Text.font_default, -1, true, 198, 116);
		notifications.add(currentWord);
	}
	
	protected void endTurn(Entity e) {
	//	removeDead();
		CombatLog.printet("Turn " + turnCount + " has ended in " + (int) (deltaTimeTurn / BILLION) + " seconds" + " (Game time: " + (int) (deltaTimeStage / BILLION) + "s)");
		FileManager.saveCombatLogFile();
		FileManager.saveStatisticsFile();
 
		if(turnCount > 0) {
			
//		System.out.println("Turn " + turnCount + " has ended.");
		}
		 
		
		newTurn();
		
		if (e instanceof Player) {
			e.resetActionPoints(e);
			displayPlayerLastWord();
		}
		
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
			Monster emma = new Monster(1, slot, stage.getPlayer(), stage); // TODO: replace 1 with r for random
			stage.add(emma);
			CombatLog.println("" + emma.name + " spawned.");
			enableGlobalCooldown();			
	}
	
	protected void spawnMonster() {
		spawnMonster(1);
	}
	
	protected void spawnMonster(int n) {
		addMonster(1);
		enableGlobalCooldown();
	}

	
	protected void resetGame() {
		submitStats_endWave();
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
	
	protected void testNotif() {
	Notification test0 = new Notification("1NE 2FI 3EA 4WA 5WI", 5 , Text.font_default, -1, true, 150, 50);
	notifications.add(test0);
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
	
	public boolean getDebugView() {
		return debugView;
	}
	
	public boolean getForcedPause() {
		return forcedPause;
	}
	
	public int getMonstersAlive() {
		return monstersAlive;
	}
	
	public void monsterEndTurn() {
		System.out.println("Monster turn ended.");
		endTurn();
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
	
	public void resetTurnCount() {
		turnCount = 0;
	}
	
	public void setContinueGame(boolean b) {
		continueGame = b;
	}
	
	public void setDebugView(boolean b) {
		debugView = b;
	}
	
	public void setForcedPause(boolean b) {
		forcedPause = b;
	}
	
	// GAMESTATS
	public void submitStats_endWave() {
		savedTurnCount += turnCount;
		savedWaveCount = waveCount;
		savedDeltaGameTime = getDeltaTimeStage() / BILLION;
		savedMonsterDeathCount = monsterDeathCount;
		savedPlayerLevel = stage.getPlayer().level;
		savedPlayerTotalXP = stage.getPlayer().getTotalXp();
		savedPlayerMaxHP = stage.getPlayer().maxHealth;
		savedPlayerMaxMana = stage.getPlayer().maxMana;
		savedPlayerMaxAP = stage.getPlayer().maxActionPoints;
	}
	
	public static void submitStats_endStage() {
	}
	
	public static String readGameStats() {
		String s = "Turn count: " + savedTurnCount
				+ "\nWave count: " + savedWaveCount
				+ "\nPlaytime: " + savedDeltaGameTime
				+ "\nKills: " + savedMonsterDeathCount
				+ "\nPlayer level: " + savedPlayerLevel
				+ "\nTotal XP: " + savedPlayerTotalXP
				+ "\nMax HP: " + savedPlayerMaxHP
				+ "\nMax Mana: " + savedPlayerMaxMana
				+ "\nMax AP: " + savedPlayerMaxAP;
		return s;
	}
	
	public int getTotalTurnCount() {
		return (int) (savedTurnCount + getCurrentTurn());
	}
	
	// TODO: maybe change this one to double for precision
	public int getTotalGameTime() {
		return (int) (savedDeltaGameTime + (getDeltaTimeStage() / BILLION));
	}
	
	public static int getTotalMonsterDeathCount() {
		return savedMonsterDeathCount + monsterDeathCount;
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
		
		// check for shop popup
	//	if (waveCount %  3 == 0 && waveCount > 0 && turnCount == 1 && !shopHasOpened) {
	//		openShop();
	//	}

		// game progression for the new stuff
		if ((stage.getMonsters().size() < 1) && (continueGame || turnCount == 0)) newMonsterWave(1);
		
		/*
		// earliest version of "game progession"
		if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().level, 0, 3) && (continueGame || turnCount == 0)) {newMonsterWave(1);}
		else if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().level, 3, 5) && continueGame) newMonsterWave(2);
		else if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().level, 5, 7) && continueGame) newMonsterWave(3);
		else if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().level, 7, 9) && continueGame) newMonsterWave(4);
		else if (stage.getMonsters().size() < 1 && stage.getPlayer().level >= 9 && continueGame) newMonsterWave(5);
		*/
	}
	
	public boolean isBetween(int comparedNum, int min, int max) {
		return (comparedNum >= min && comparedNum < max);
	}

	// GAMESTATS REFUGEES
	private int checkMonstersAliveCount() {
		int n = 0;
		for (int i =0; i < stage.getMonsters().size(); i++) {
			if (stage.getMonsters().get(i).isAlive) n++;
		}
		return n;
	}
	
	// NOTIFICATIONS
	
	public void add(Notification n) {
		notifications.add(n);
	}
	
	private void remove() {
			for (int i = 0; i < notifications.size(); i++) {
				if (notifications.get(i).getNeedsRemoved()) {
					notifications.remove(i);
					System.out.println("Notification removed");
				}
			}
	}
	
	public List<Notification> getNotifications() {
		return notifications;
	}

	public void addNotification(Notification n) {
		notifications.add(n);
	}
	
	// FLOATING DMG NUMBER
	public void displayDamage(int value, boolean isPlayer) {
		int x = 160, y = 60;
		if (isPlayer) x = 360;
		
		Notification n = new Notification("-"+value, 1, Text.font_kubastaBig, 0xffff0000, false, x, y);
		addNotification(n);
	}
	
	public void displayHealing(int value, boolean isPlayer) {
		int x = 360, y = 60;
		if (isPlayer) x = 160;
		
		Notification n = new Notification("+ "+value+"", 1, Text.font_kubastaBig, 0xff00ff00, false, x, y);
		addNotification(n);
	}
	
}
