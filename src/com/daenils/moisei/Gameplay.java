package com.daenils.moisei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;
import com.daenils.moisei.entities.Entity;
import com.daenils.moisei.entities.Monster;
import com.daenils.moisei.entities.Player;
import com.daenils.moisei.entities.Letter.Element;
import com.daenils.moisei.entities.equipments.*;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Notification;
import com.daenils.moisei.graphics.Sprite;
import com.daenils.moisei.graphics.Spritesheet;
import com.daenils.moisei.graphics.Text;
import com.daenils.moisei.graphics.Screen;

public class Gameplay {
	public final static String QUIT_BY_PLAYER = "QUIT_BY_PLAYER";
	public final static String QUIT_BY_VICTORY = "QUIT_BY_VICTORY";
	public final static String QUIT_BY_DEFEAT = "QUIT_BY_DEFEAT";
	
	private final int BILLION = 1000000000;
	public static final int DEBUG_VIEWS = 5;
	
	private List<Notification> notifications = new ArrayList<Notification>();
	
	private Stage stage;
	private boolean askForQuit = false;
	
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
	private int debugView = 0;

	private Text font;
	
	protected boolean playerOverride;
	
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
	
	// REQUEST QUIT
	private long requestDuration;
	private long requestStartTime;
	private boolean isQuitAlreadyRequested;
	
	// SAVED STUFF -- BUT NOT YET IMPLEMENTED
	public static int savedMonsterCount;
	public static int savedPlayerDamageDealt; // needs a playerDamageDealt as well?
	public static int savedMonsterDamageDealt; // -- " --

	// SHOP STUFF
	protected boolean shopHasOpened; // set it true when first opens so it will only open once, set it back to false at
											// the start of a new wave probably
	
	protected String monstersLeftGraphic;
	
	// COMBATLOG DISPLAY
	protected int curLine = (CombatLog.getSize() -1);
	private int[] linesDisplayed = new int[5];
	private final int LINES_TO_DISPLAY = 5;

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
		
		// just so that file is initialized as well, probably a temporary measure
	//	Gamestats.submitStats_endWave();
	//	FileManager.saveStatisticsFile();
		
		// init combatlog display
		for (int i = 0; i < 5; i++)
			linesDisplayed[i] = i;
	}
	


	public void update() {
		// GAMESTATS REFUGEES
		monstersAllDead = stage.checkIfAllDead();
		monstersAlive = checkMonstersAliveCount();
		if (stage.getMonsters().size() > 0 && !stage.getMonsters().get(0).getIsAlive()) {
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
		for (int i = 0; i < e.getAbilitiesSize(); i++) {
			if (e.getAbility(i).getLastUsed() > 0 && e.getAbility(i).getCooldown() > 0) {
				if (e.getAbility(i).getLastUsed() + (e.getAbility(i).getCooldown() * 2) + 1 > turnCount) {
					e.getAbility(i).setOnCooldown(true);
				}
				else e.getAbility(i).setOnCooldown(false);
			}
		}
	}
	
	private void dealOTValues(Entity e) {
		// universal method (abilities):
		for (int i = 0; i < e.getAbilitiesSize(); i++) {
			if (e.getAbility(i).getLastUsed() > 0 && e.getAbility(i).getIsOT()) {
				if (e.getAbility(i).getLastUsed() + e.getAbility(i).getTurnCount() + 1 >
				turnCount && !e.getAbility(i).isAppliedOT() && 
				(turnCount > e.getAbility(i).getLastUsed())) {
					e.applyOTs(e.getAbility(i));
					e.getAbility(i).setAppliedOT(true);
				} else e.setTick(0);
			}
		}
		
		// universal method (weapons):
		if (e.getWeapon() != null) {
			if (e.getWeapon().getLastUsed() > 0 && e.getWeapon().getIsOT()) {
				if (e.getWeapon().getLastUsed() + e.getWeapon().getTurnCount() + 1 >
				turnCount && !e.getWeapon().isAppliedOT() &&
				(turnCount > e.getWeapon().getLastUsed())) {
					e.applyOTs(e.getWeapon());
					e.getWeapon().setAppliedOT(true);
				} else e.setTick(0);
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
		
		// RENDER DEBUG VIEW
		if (debugView == 0) renderStageInfo(screen);
		if (debugView == 1) renderDebugInfoLetterDroptable(screen);
		if (debugView == 2) renderDebugInfoElementalDroptable(screen);
		if (debugView == 3) renderDebugInfo(screen);
		if (debugView == 4) renderDebugPlayerInfo(screen);
		if (debugView == 5) renderDebugBuffInfo(screen);
		
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
//		for (int i = 0; i < stage.getPlayer().getAbilitiesSize(); i++)	 {
//			renderAbilityHelperText(screen, i);
//			if (stage.getPlayer().getAbility(i).showTooltip) renderAbilityText(screen, i);
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
		
		
		//	font.renderXCentered(-1, 199, 12, 0xff444444, Font.font_kubastaBig,1, "Sample String", screen);
		//	font.renderXCentered(2, 202, 12, 0xff444444, Font.font_kubastaBig,1, "Sample String", screen);
		//	font.renderXCentered(200, 12, 0xffddbb00, Font.font_kubastaBig,1, "Sample String", screen);
	}
	
	

	private void renderElementalBars(Screen screen) {
		// DEBUG MODE: VALUES
		if (debugView > 0) renderElementalValues(screen);
		
		// REAL DEAL
		int x = 359, y = 268;
		int xB = 450+2, yB = 300+2;
		int w = 20, h = 20;
		int wB = 16, hB = 16;
		int[] segmentMark = {stage.getPlayer().getElementalPowerCap(0), 
				stage.getPlayer().getElementalPowerCap(0)+stage.getPlayer().getElementalPowerCap(1),
				stage.getPlayer().getElementalPowerCap(0)+stage.getPlayer().getElementalPowerCap(1)+stage.getPlayer().getElementalPowerCap(2),
				stage.getPlayer().getElementalPowerCap(0)+stage.getPlayer().getElementalPowerCap(1)+stage.getPlayer().getElementalPowerCap(2)+stage.getPlayer().getElementalPowerCap(3)};
		int[] subtractValue = {stage.getPlayer().getElementalPowerCap(0), 
				stage.getPlayer().getElementalPowerCap(0)+stage.getPlayer().getElementalPowerCap(1), 
				stage.getPlayer().getElementalPowerCap(0)+stage.getPlayer().getElementalPowerCap(1)+stage.getPlayer().getElementalPowerCap(2)};
		int[] elementSegments = new int[4];
		

		for (int i = 0; i < elementPercentage.length; i++) {
			if ( getStage().getPlayer().getElementalPower(i) >= segmentMark[2]) {
				elementPercentage[i] = (int) (16 * ((double) ( getStage().getPlayer().getElementalPower(i) - subtractValue[2]) / (double) getStage().getPlayer().getElementalPowerCap(3)));
				if (getStage().getPlayer().getElementalPower(i) < getStage().getPlayer().getElementalPowerCapSum()) 
					elementSegments[i] = 3;
				else elementSegments[i] = 4;
			}
			else if ( getStage().getPlayer().getElementalPower(i) >= segmentMark[1]){
				elementPercentage[i] = (int) (16 * ((double) ( getStage().getPlayer().getElementalPower(i) - subtractValue[1]) / (double) getStage().getPlayer().getElementalPowerCap(2)));
				elementSegments[i] = 2; 
			}
			else if ( getStage().getPlayer().getElementalPower(i) >= segmentMark[0]) {
				elementPercentage[i] = (int) (16 * ((double) ( getStage().getPlayer().getElementalPower(i) - subtractValue[0]) / (double) getStage().getPlayer().getElementalPowerCap(1)));
				elementSegments[i] = 1;
			}
			else {
				elementPercentage[i] = (int) (16 * ((double)  getStage().getPlayer().getElementalPower(i) / (double) getStage().getPlayer().getElementalPowerCap(0)));
				elementSegments[i] = 0;
			}
		}
		
		// 1 FIRE
		renderElementalBar(Sprite.elementalCircle[0], screen, x, y, wB, hB, Screen.PALETTE_DARK[0], hB);
		renderElementalBar(Sprite.elementalCircle[0], screen, x, y, wB, hB, Screen.PALETTE_BASE[0], elementPercentage[0]);
		screen.renderElementalCircle(x, y, Element.FIRE, elementSegments[0]);

		// 2 WATER
		renderElementalBar(Sprite.elementalCircle[0], screen, x+w+2, y, wB, hB, Screen.PALETTE_DARK[1], hB);
		renderElementalBar(Sprite.elementalCircle[0], screen, x+w+2, y, wB, hB, Screen.PALETTE_BASE[1], elementPercentage[1]);
		screen.renderElementalCircle(x+w+2, y, Element.WATER, elementSegments[1]);
		
		// 3 EARTH
		renderElementalBar(Sprite.elementalCircle[0], screen, x+w+2+w+2, y, wB, hB, Screen.PALETTE_DARK[2], hB);
		renderElementalBar(Sprite.elementalCircle[0], screen, x+w+2+w+2, y, wB, hB, Screen.PALETTE_BASE[2], elementPercentage[2]);
		screen.renderElementalCircle(x+w+2+w+2, y, Element.EARTH, elementSegments[2]);
		
		// 4 WIND
		renderElementalBar(Sprite.elementalCircle[0], screen, x+w+2+w+2+w+2, y, wB, hB, Screen.PALETTE_DARK[3], hB);
		renderElementalBar(Sprite.elementalCircle[0], screen, x+w+2+w+2+w+2, y, wB, hB, Screen.PALETTE_BASE[3], elementPercentage[3]);
		screen.renderElementalCircle(x+w+2+w+2+w+2, y, Element.WIND, elementSegments[3]);
		
		// SEGMENT MARKERS (TEMP CODE PROBABLY WILL BE FIXED IN ART)
		// 3 - 6 - 9 - 12 = 9 - 18 - 27 - 36
		
		// RENDER ELEMENTAL CIRCLES
	//	screen.renderSprite(x, y, Sprite.elementalCircle[0], 1);
		
		
	}
	
	private void renderElementalBar(Screen screen, int x, int y, int w, int h, int col, int value) {
		for (int i = 0; i < h; i++)
			for (int k = 0; k < value; k++)
				screen.renderPixel(x+i, y - k + h, col);
		
		//		for (int k = 0; k < h; k++)
//			for (int i = 0; i < value; i++) 
//				screen.renderPixel(x + i, y + k, col);
	}
	
	private void renderElementalBar(Sprite sprite, Screen screen, int x, int y, int w, int h, int col, int value) {
		sprite = new Sprite(Sprite.elementalCircle[0]);
		int xp = 2, yp = 2;
		
		for (int i = 0; i < h; i++)
			for (int k = 0; k < value; k++)
				sprite.renderPixel(xp + i, yp + (h) - k, col);
		
		screen.renderSprite(x, y, sprite, 1);
	}
	
	private void renderElementalValues(Screen screen) {
		font.render(5+12, 350, -8, 0xffffff, Text.font_default, 1, ""+getStage().getPlayer().getElementalPower(0), screen);
		font.render(5+42, 350, -8, 0xffffff, Text.font_default, 1,  ""+getStage().getPlayer().getElementalPower(1), screen);
		font.render(5+72, 350, -8, 0xffffff, Text.font_default, 1,  ""+getStage().getPlayer().getElementalPower(2), screen);
		font.render(5+102, 350, -8, 0xffffff, Text.font_default, 1,  ""+getStage().getPlayer().getElementalPower(3), screen);
		font.renderColored(5, 350, -8, -1, Text.font_default, 1, "2F:  4WA:  3EA:  5WI:", screen);
	}

	private void renderCombatLog(Screen screen) {
		setLinesDisplayedByEndPoint();
		if (CombatLog.getLogLength() > (LINES_TO_DISPLAY - 1)) {
			for (int i = 0; i < LINES_TO_DISPLAY; i++) {
				font.render(0, 250 + (i*10), -8, 0xffe5e5e5, Text.font_default, 1.0, CombatLog.getLine(linesDisplayed[i]), screen);
			}
		}
	}
	
	// TODO: move these methods to the end
	public void setLinesDisplayedByEndPoint() {
		for (int i = 0; i < LINES_TO_DISPLAY; i++) {
			linesDisplayed[(LINES_TO_DISPLAY - 1) - i] = curLine - i;
		}
	}
	
	public void setLinesDisplayedScroll(boolean b) {		// TODO: set it to +11 later
		if (curLine - 1 > (LINES_TO_DISPLAY - 2) && b)				// -2 for all log | +11 for only log (without header)
			curLine--;
		if (curLine -1 < ((CombatLog.getSize() -1)-1) && !b)
			curLine++;
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
	//	font.render(525, 2, -8, 0xff000000, Text.font_default, 1, Game.getTitle() + " " + Game.getVersion()
	//			+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.getProjectStage().length() + 1) + Game.getProjectStage().toUpperCase()
	//			+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.isFpsLockedString().length() + 1) + Game.isFpsLockedString(), screen);
		
		font.render(557, 2+7+7+1, -8, 0xff555555, Text.font_default, 1, newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.isFpsLockedString().length() + 1) + Game.isFpsLockedString(), screen);
		font.render(557, 2+7+7, -8, 0xffffffff, Text.font_default, 1, newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.isFpsLockedString().length() + 1) + Game.isFpsLockedString(), screen);
		
		font.render(551, 2+7+9+7+1, -8, 0xff555555, Text.font_default, 1, newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - stage.getTitle().length() + 1) + stage.getTitle(), screen);
		font.render(551, 2+7+9+7, -8, 0xffffffff, Text.font_default, 1, newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - stage.getTitle().length() + 1) + stage.getTitle(), screen);
		
		
	}
	
	private void renderPlayerInfo(Screen screen) {
		// BARS
		renderPlayerHealthBar(screen);
		renderPlayerXPBar(screen);
		
		// HP TEXT
		font.render(202, 31, -8, 0xffffffff, Text.font_default, 1, ""
				+ "" + stage.getPlayer().getPHealth() + "%"
				+ " (" + stage.getPlayer().getHealth() + ")" 
		//		+ "" + stage.getPlayer().getHealth() + "/" + stage.getPlayer().maxHealth
		//		+ " (" + stage.getPlayer().getShield() + ")"
				, screen);
		
		// LEVEL
	//	font.render(163, 50, 10, 0, Text.font_kubastaBig, 1, ""
	//			+ "" + stage.getPlayer().getLevel()				 
	//			, screen);
	//	font.render(160, 50, 10, 0xffffffff, Text.font_kubastaBig, 1, ""
	//			+ "" + stage.getPlayer().getLevel()				 
	//			, screen);
		
		// GOLD
	//	font.render(GUI.screenPlayerinfoPos-145, 350, -7, 0xffffffff, Text.font_default, 1, 
	//			"$" +
	//			stage.getPlayer().getGoldAmount()
	//			, screen);
		
		// WINDBUFF
		if (getStage().getPlayer().getDamageReduction() > 0) {
			font.render(156, 46, -8, 0xffa4a4a4, Text.font_default, 1, 
					"DMGR " + getStage().getPlayer().getDamageReduction() + "%"
					, screen);
			font.render(155, 46, -8, 0xffbbbbbb, Text.font_default, 1, 
					"DMGR " + getStage().getPlayer().getDamageReduction() + "%"
					, screen);
		}
		
	//	if ((deltaTimeTurn / BILLION) < 2 && isMonsterTurn) {
	//		for (int i = 0; i < getStage().getPlayer().currentWordLength; i++) {
	//			int x = 198, y = 96;
	//			font.render(x + (i*6), y, -8, 0xff555555, Text.font_default, 1, getStage().getPlayer().getCurrentWord(i) + ""
	//					, screen);
	//			font.render((x-1) + (i*6), y, -8, getStage().getPlayer().currentWordColors[i], Text.font_default, 1, getStage().getPlayer().getCurrentWord(i)+ ""
	//					, screen);
	//		}			
	//	}
		
		// PLAYER WORD (TYPING)
		for (int i = 0; i < getStage().getPlayer().getLetterBarSize(); i++) {
			int x = 198, y = 116;
			font.render(x + (i*6), y, -8, 0xff555555, Text.font_default, 1, getStage().getPlayer().getLetterBar(i).getValue() + ""
						, screen);
			font.render((x) + (i*6), y-1, -8, getStage().getPlayer().getLetterBar(i).getFrame(), Text.font_default, 1, getStage().getPlayer().getLetterBar(i).getValue()+ ""
					, screen);
		}
		
/*			if ((deltaTimeTurn / BILLION) < 2 && isMonsterTurn) {
				for (int i = 0; i < getStage().getPlayer().currentWordLength; i++) {
					int x = 198, y = 116;
					font.render(x + (i*6), y, -8, 0xff555555, Text.font_default, 1, getStage().getPlayer().getCurrentWord(i) + ""
							, screen);
					font.render((x) + (i*6), y-1, -8, getStage().getPlayer().currentWordColors[i], Text.font_default, 1, getStage().getPlayer().getCurrentWord(i)+ ""
							, screen);
				}
			} */
		
	
		// MONSTER WORD
		/*
		if (getStage().getMonsters().size() > 0 && (deltaTimeTurn / BILLION) < 2 && isPlayerTurn) {
			for (int i = 0; i < getStage().getMonsters().get(0).currentWordLength; i++) {
				int x = 380, y = 96;
					font.render(x + (i*6), y, -8, 0xff555555, Text.font_default, 1, getStage().getMonsters().get(0).getCurrentWord(i) + ""
						, screen);
					font.render((x-1) + (i*6), y, -8, 0xffffffff, Text.font_default, 1, getStage().getMonsters().get(0).getCurrentWord(i) + ""
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
					+ "" + stage.getMonsters().get(0).getPHealth() + "%"
					+ " (" + stage.getMonsters().get(0).getHealth() + ")" 
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
			for (int i = 0; i < 120 * (stage.getPlayer().getPHealth() / 100.0); i++) {
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
				for (int i = 0; i < 120 * (stage.getMonsters().get(0).getPHealth() / 100.0); i++) {
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
			for (int i = 0; i < 120 * (stage.getPlayer().getPXP() / 100.0); i++) {
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
	
	protected void renderStageInfo(Screen screen) {
		// RENDER ALL (BG)
		for (int i = 0; i < stage.getMonstersTotal(); i++) {
			font.render(-24+(i*24), -10, 0, 0xff404040, Text.font_kubastaBig, 1, "#", screen);
	//		font.render(-3+(i*8), 2, -8, 0xff757575, Text.font_default, 1, "$", screen);
		}

		// RENDER ALIVE
		if (stage.getMonstersAlive() > 0) {
			for (int i = 0; i < stage.getMonstersAlive(); i++) {
			//	font.render(-3+(i*8), 2, -8, 0xffffffff, Text.font_default, 1, "$", screen);
				font.render(-22+(i*24), -10, 0, 0xffC24040, Text.font_kubastaBig, 1, "#", screen);
			}
		}
	}
 	
	protected void renderDebugInfo(Screen screen) {
		font.render(-4, 2, -8, 0xff00aa55, Text.font_default, 1, "DEBUG STUFF\n"
				+ "\nMonsters on stage: " + stage.getMonstersTotal()
				+ "\nMonsters alive: " + stage.getMonstersAlive()
				+ "\nMonsters spawned: " + stage.getMonstersSpawned()
				+ "\n"
				+ "\nVwls: " + getStage().getPlayer().getVowelCount()
				+ " Conss: " + getStage().getPlayer().getConsonantCount()
				+ " (T: " + getStage().getPlayer().getLetterInventorySize() +")"
				+ "\n"
				+ "\nTotal TurnCount: " + getTotalTurnCount()
				+ "\nTotal RunTime: " + (int) (deltaTimeRunning / BILLION)
				+ "\nTotal GameTime: " + (int) (deltaTimeStage / BILLION)
				+ "\nMonster deathcount: " + Monster.getDeathCount()
				+ "\n"
				+ "\nGlobalCooldown: " + deltaGlobalCooldownTimeSec
				+ "\nGlobalCooldown: " + onGlobalCooldown
				+ "\nGame is paused: " + !continueGame
				+ "\nPause forced: " + getForcedPause()
				, screen);
	}
	
	protected void renderDebugPlayerInfo(Screen screen) {
		font.render(-4, 2, -8, 0xff0055aa, Text.font_default, 1, "PLAYER DEBUG\n"
				+ "\nlevel: " + stage.getPlayer().getLevel()
				+ "\nXP: " + stage.getPlayer().getXp() + "(" + stage.getPlayer().getPXP() + ")"
				+ "\nHP: " + stage.getPlayer().getHealth() + "(" + stage.getPlayer().getPHealth()+ ")"
				+ "\nMP: " + stage.getPlayer().getMana()+ "(" + stage.getPlayer().getPMana() + ")"
				+ "\n"
				+ "\nGold: " + stage.getPlayer().getGoldAmount()
				+ "\n"
				+ "\nFixateElement: " + stage.getPlayer().getFixElement()
				+ "\nRepl. elem: " + stage.getPlayer().getReplaceElementsNow()
				+ "\nWord DMG mod: " + stage.getPlayer().getWordDamageModifier()
				+ "\nhas reflective mit: " + stage.getPlayer().hasReflectiveMitigation()
	//			+ "Target Level:" + stage.getMonsters().get(0).getLevel()
				, screen);
	}
	
	protected void renderDebugBuffInfo(Screen screen) {
		String monsterInfoString;
		if (stage.getMonsters().size() > 0) {
			monsterInfoString = "\nMonster buffs: " 
					+ "\n" + stage.getMonsters().get(0).getBuffName(0) + " " + stage.getMonsters().get(0).getBuffTicksLeft(0) + "\n"
					+ stage.getMonsters().get(0).getBuffName(1) + " " + stage.getMonsters().get(0).getBuffTicksLeft(1) + "\n"
					+ stage.getMonsters().get(0).getBuffName(2) + " " + stage.getMonsters().get(0).getBuffTicksLeft(2) + "\n"
					+ stage.getMonsters().get(0).getBuffName(3) + " " + stage.getMonsters().get(0).getBuffTicksLeft(3) + "\n"
					+ stage.getMonsters().get(0).getBuffName(4) + " " + stage.getMonsters().get(0).getBuffTicksLeft(4) + "\n";
		} else monsterInfoString = "";
		
		font.render(-4, 2, -8, 0xff0055aa, Text.font_default, 1, "BUFF-DEBUFF DEBUG\n"
				+ "\nPlayer buffs: "
				+ "\n" + stage.getPlayer().getBuffName(0) + " " + stage.getPlayer().getBuffTicksLeft(0) + "\n"
				+ stage.getPlayer().getBuffName(1) + " " + stage.getPlayer().getBuffTicksLeft(1) + "\n"
				+ stage.getPlayer().getBuffName(2) + " " + stage.getPlayer().getBuffTicksLeft(2) + "\n"
				+ stage.getPlayer().getBuffName(3) + " " + stage.getPlayer().getBuffTicksLeft(3) + "\n"
				+ stage.getPlayer().getBuffName(4) + " " + stage.getPlayer().getBuffTicksLeft(4) + "\n"
				+ monsterInfoString
				, screen);
	}	
	
	protected void renderDebugInfoOLD(Screen screen) {
		font.render(-4, 2, -8, 0xffeeaa00, Text.font_default, 1, "DEBUG STUFF\n"
				//			+ "\nRandom Wait: " + Gamestats.monsterRW
							+ "\nMonsters spawned: " + stage.getMonsters().size()
							+ "\nMonsters alive: " + monstersAlive
							+ "\n\n" + individualMonsterDetails() + "\n"
							+ "\n" + getStage().getPlayer().getLetterCountString()
							+ "\nVowels: " + getStage().getPlayer().getVowelCount()
							+ "/ Consonants: " + getStage().getPlayer().getConsonantCount()
							+ "/ TOTAL: " + getStage().getPlayer().getLetterInventorySize()
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
							+ "\nPlayer's spellpower: " + stage.getPlayer().getSpellPower()
							+ "\n\nmX: " + Mouse.getX() + " mY: " + Mouse.getY() + " mB: " + Mouse.getB()
							
							
	
							
				//			+ "\nMONSTER ATTACKED: " + Gamestats.monstersAttacked
							, screen);
	}
	
	protected void renderDebugInfoLetterDroptable(Screen screen) {
		String droptable = "DROPTABLE:" + "[" + getStage().getPlayer().getVowelCount() + ":" + getStage().getPlayer().getConsonantCount() + "]";
		for (int i = 0; i < 26; i++) {
			droptable += "\n" + (char) (i+65) + ":" + getStage().getPlayer().getLetterDroprate(i) + " | " + getStage().getPlayer().getLetterDroprateBracket(i);
		}
		
		font.render(-4, 2, -8, 0xffaa0055, Text.font_default, 1, "DEBUG STUFF" + " [" + getStage().getPlayer().getLetterCountString() + "]" + "\n"
				+ droptable
				+ "\nTOTAL: " + getStage().getPlayer().getRollMax()
				, screen);
		
	}
	
	protected void renderDebugInfoElementalDroptable(Screen screen) {
		String droptable = "E-DROPTABLE:" + "[" + getStage().getPlayer().getVowelCount() + ":" + getStage().getPlayer().getConsonantCount() + "]";
		for (int i = 0; i < 5; i++) {
			droptable += "\n" + Player.ELEMENTS_ORDERED[i].toUpperCase().substring(0, 2) + ":" + getStage().getPlayer().getElementDroprate(i) + " | " + getStage().getPlayer().getElementDroprateBracket(i);
		}
		
		font.render(-4, 2, -8, 0xffaa0055, Text.font_default, 1, "DEBUG STUFF" + " [" + getStage().getPlayer().getLetterCountString() + "]" + "\n"
				+ droptable
				+ "\nTOTAL: " + getStage().getPlayer().getERollMax()
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
			string = string.concat(" " + stage.getMonsters().get(i).getActionPoints());
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
			string = string.concat(" " + stage.getMonsters().get(i).isWaiting());
		}
		string = string.concat("\nMonster LVL:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).getLevel());
		}
		string = string.concat("\nMonster DMG:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).getDamage(0) + "-" + stage.getMonsters().get(i).getDamage(1));
		}
		string = string.concat("\nMonster HITDMG:");
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			string = string.concat(" " + stage.getMonsters().get(i).getHitDamage());
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
		
		// PAUSE: PAUSED BY PLAYER
		if (!continueGame && forcedPause) {
			font.renderXCentered(-3, 20, 12, 0xff050505, Text.font_kubastaBig, 1.1, "GAME PAUSED", screen);
			font.renderXCentered(20, 12, 0xff8d2d8d, Text.font_kubastaBig, 1.1, "GAME PAUSED", screen);
		}
		
		// PAUSE: STAGE COMPLETED
		if (!continueGame && !forcedPause && stage.getPlayer().getIsAlive()) {
			font.renderXCentered(-3, 20, 12, 0xff050505, Text.font_kubastaBig, 1.1, "STAGE COMPLETED", screen);
			font.renderXCentered(20, 12, 0xff8d2d8d, Text.font_kubastaBig, 1.1, "STAGE COMPLETED", screen);
		}
		
		// PAUSE: PLAYER DIED
		if (stage.getPlayer().getHealth() <= 0) {
			font.renderXCentered(-4, 80, 10, 0xff4d0d0d, Text.font_kubastaBig, 1, "YOU HAVE DIED", screen);
			font.renderXCentered(80, 10, 0xffad0d0d, Text.font_kubastaBig, 1, "YOU HAVE DIED", screen);
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
			
			
		//	if (Gamestats.monsterHP[5] < 1)
		//		font.render(580 - 10, 160, -8, 0xffa30300, "Monster DIED", screen);
	}
	
	public void newTurn() {
		submitStats_endWave();
		FileManager.saveStatisticsFile();
		
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
	//	if (this.isPlayerTurn) getStage().getPlayer().setDamageReduction(0);
		
		// RESET TICKDONE FOR BUFFS/DEBUFSS
		// PLAYER
		for (int i = 0; i < stage.getPlayer().getBuffsSize(); i++) {
			stage.getPlayer().getBuff(i).setTickDone(false);
		}
		
		// MONSTER
		if (stage.getMonsters().size() > 0) {
			for (int i = 0; i < stage.getMonsters().get(0).getBuffsSize(); i++)
				stage.getMonsters().get(0).getBuff(i).setTickDone(false);			
		}
		
	//	stage.getPlayer().resetWordDamageModifier();
		
		// LETTER STUFF
		if (getStage().getPlayer().getLetterInventory().size() < getStage().getPlayer().getInitialLetterSpawn()) {
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
			
			getStage().getPlayer().spawnLettersNEW(getStage().getPlayer().getInitialLetterSpawn() - getStage().getPlayer().getLetterInventory().size());
			
	//		getStage().getPlayer().spawnVowels(15 - getStage().getPlayer().getLetterInventory().size());
	//		while (getStage().getPlayer().getLetterInventory().size() < 10) getStage().getPlayer().spawnLetter();
			
			// PRINT LETTERCOUNT
	//		getStage().getPlayer().printLetterCount();
		}
		
		// reset dot flags
		for (int i = 0; i < stage.getPlayer().getAbilitiesSize(); i++) {
			stage.getPlayer().getAbility(i).setAppliedOT(false);
			if (stage.getPlayer().getWeapon() != null) stage.getPlayer().getWeapon().setAppliedOT(false);
		}
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			for (int l = 0; l < stage.getMonsters().get(i).getAbilitiesSize(); l++) {
				stage.getMonsters().get(i).getAbility(l).setAppliedOT(false);
				if (stage.getMonsters().get(i).getWeapon() != null) stage.getMonsters().get(i).getWeapon().setAppliedOT(false);
			}
		}
		
		// reset isStunned
		stage.getPlayer().setIsStunned(false);
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			stage.getMonsters().get(i).setIsStunned(false);;
		}
	}
	
	public void endTurn() {
		if (!getStage().getMonsters().get(0).isStunned()) displayMonsterLastWord();
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
		for (int i = 0; i < getStage().getMonsters().get(0).getCurrentWordLength(); i++)
			monsterWord += getStage().getMonsters().get(0).getCurrentWord(i);

		Notification currentWord = new Notification(monsterWord, 0, 2, Text.font_default, 0xffffffff, true, 380, 116, false);
		notifications.add(currentWord);
	}
	
	private void displayPlayerLastWord() {
		String playerWord = "";
		for (int i = 0; i < getStage().getPlayer().getCurrentWordLength(); i++)
			playerWord += getStage().getPlayer().getCurrentWord(i);
		
		System.out.println(getStage().getPlayer().getCurrentWord());

		Notification currentWord = new Notification(playerWord, 0, 2, Text.font_default, -1, true, 198, 116, false);
		notifications.add(currentWord);
	}
	
	public void endTurn(Entity e) {
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
			if ((!stage.getMonsters().get(i).getIsAlive()) || stage.getMonsters().get(i).getForceRemoved()) stage.getMonsters().get(i).setRemove(true);
			else stage.getMonsters().get(i).setRemove(false);
		}
	}
	
	private void addMonster(int slot, int id) {
			Monster emma = new Monster(id, slot, stage.getPlayer(), stage); // TODO: replace 1 with r for random
			stage.add(emma);
			CombatLog.println("" + emma.getName() + " spawned.");
			enableGlobalCooldown();			
	}
	
	protected void spawnMonster() {
		spawnMonster(1);
	}
	
	protected void spawnMonster(int n) {
		addMonster(1, 1);
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
		stage.getPlayer().setTarget(null);
		// reset cooldowns:
		stage.getPlayer().resetCooldowns(stage.getPlayer());
		for (int i = 0; i < stage.getMonsters().size(); i++)
			stage.getMonsters().get(i).resetCooldowns(stage.getMonsters().get(i));
		
		setContinueGame(true);
	}
	
	protected void testNotif() {
	Notification test0 = new Notification("1NE 2FI 3EA 4WA 5WI", 0, 5 , Text.font_default, -1, true, 150, 50, false);
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
			stage.getPlayer().setHealth(stage.getPlayer().getMaxHealth());
			stage.getPlayer().setMana(stage.getPlayer().getMaxMana());
			CombatLog.println("Player received a blessing (Health and Mana restored).");
		}
	}
	
	// currently public, because Game.java should be able to handle it for now
	public void setFirst() {
		// temporary code: always the player starts
		this.isPlayerTurn = true;
	}
	
	public void resetCombatLogScroll() {
		curLine = CombatLog.getSize() - 1;
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
	
	public int getDebugView() {
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
	
	public void setDebugView(int i) {
		debugView = i;
	}
	
	public void incrementDebugView() {
		debugView++;
	}
	
	public void setForcedPause(boolean b) {
		forcedPause = b;
	}
	
	// GAMESTATS
	public void submitStats_endWave() {
		savedTurnCount = turnCount;
		savedWaveCount = waveCount;
		savedDeltaGameTime = getDeltaTimeStage() / BILLION;
		savedMonsterDeathCount = monsterDeathCount;
		savedPlayerLevel = stage.getPlayer().getLevel();
		savedPlayerTotalXP = stage.getPlayer().getTotalXp();
		savedPlayerMaxHP = stage.getPlayer().getMaxHealth();
		savedPlayerMaxMana = stage.getPlayer().getMaxMana();
		savedPlayerMaxAP = stage.getPlayer().getMaxActionPoints();
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
		// TODO: maybe invoke DIE/WIN notifications from here (when they are converted to notifs)
		
		if (!continueGame && !forcedPause && stage.getPlayer().getIsAlive() && stage.getMonstersAlive() > 0) {
			System.out.println("!%");
			stage.updateMonsterFlags();
		}
		// CONTINUE ON PLAYER DEATH (LOSE)
		if (!continueGame && !forcedPause && stage.getInputPlayerEndTurn() && !stage.getPlayer().getIsAlive()) {
			CombatLog.printFooter();					// print the footer with the list of submitted words
			FileManager.saveCombatLogFile();		// save the combatlog one last time	
			setAskingForQuit(true, QUIT_BY_DEFEAT);			
			Game.showMessage("Game ended on condition = " + QUIT_BY_DEFEAT +".", 0, 2, Game.TOPLEFT, false);
		}
		
		// CONTINUE ON MONSTERS DEAD (WIN)
		if (!continueGame && !forcedPause && stage.getInputPlayerEndTurn() && stage.getPlayer().getIsAlive() && !onGlobalCooldown) {
			savePlayerToProfile();
			CombatLog.printFooter();					// print the footer with the list of submitted words
			FileManager.saveCombatLogFile();		// save the combatlog one last time	
			setAskingForQuit(true, QUIT_BY_VICTORY);
			Game.showMessage("Game ended on condition = " + QUIT_BY_VICTORY +".", 0, 2, Game.TOPLEFT, false);
		}
		
		// SET FLAG FOR QUIT ALREADY REQUESTED
		if (requestStartTime > 0 || requestDuration > 0)
			if (System.nanoTime() >= (requestStartTime + (requestDuration * 1000000000L))) {
				isQuitAlreadyRequested = false;
				requestDuration = 0;
				requestStartTime = 0;
				System.out.println("REQUEST-EXIT VALUES HAS BEEN RESET.");
			}
		
		// REQUEST MENU
		if (stage.getInputPlayerExitToMenu() && !onGlobalCooldown && !isQuitAlreadyRequested) {
			enableGlobalCooldown();
			requestDuration = 2;
			requestStartTime = System.nanoTime();
			isQuitAlreadyRequested = true;
			Game.showMessage("Press 'ESC' again to confirm exit.", 0, (int) requestDuration, Game.TOPLEFT, false);
		}
		
		// CONFIRMED QUIT
		if (!onGlobalCooldown && isQuitAlreadyRequested && stage.getInputPlayerExitToMenu() && (System.nanoTime() <= (requestStartTime + (requestDuration * 1000000000L)))) {
			CombatLog.printFooter();					// print the footer with the list of submitted words
			FileManager.saveCombatLogFile();		// save the combatlog one last time	
			setAskingForQuit(true, QUIT_BY_PLAYER);
			Game.showMessage("Game ended on condition = " + QUIT_BY_PLAYER +".", 0, 2, Game.TOPLEFT, false);
		}
		
		// check for shop popup
	//	if (waveCount %  3 == 0 && waveCount > 0 && turnCount == 1 && !shopHasOpened) {
	//		openShop();
	//	}

		// game progression for the new stuff
//		if ((stage.getMonsters().size() < 1) && (continueGame || turnCount == 0)) newMonsterWave(1);
		
		
		
		/*
		// earliest version of "game progession"
		if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().getLevel(), 0, 3) && (continueGame || turnCount == 0)) {newMonsterWave(1);}
		else if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().getLevel(), 3, 5) && continueGame) newMonsterWave(2);
		else if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().getLevel(), .getLevel()&& continueGame) newMonsterWave(3);
		else if (stage.getMonsters().size() < 1 && isBetween(stage.getPlayer().getLevel(), 7, 9) && continueGame) newMonsterWave(4);
		else if (stage.getMonsters().size() < 1 && stage.getPlayer().getLevel() >= 9 && continueGame) newMonsterWave(5);
		*/
	}
	
	public boolean isBetween(int comparedNum, int min, int max) {
		return (comparedNum >= min && comparedNum < max);
	}
	
	public void savePlayerToProfile() {
		FileManager.setProfileData("level", stage.getPlayer().getLevel(), false);
		FileManager.setProfileData("xp", stage.getPlayer().getXp(), false);
		FileManager.setProfileData("gold", stage.getPlayer().getGoldAmount(), false);
		if ((stage.getId() + 1) <= Stage.getMaxStage()) {
			// unlock next level
			FileManager.setProfileData("stagesunlocked", true, stage.getId() + 1);
			Game.updateUnlockedStages();
			
			// set continuestage if first completion
			if (FileManager.getProfileDataAsInt("continuestage") < (stage.getId() + 1))
				FileManager.setProfileData("continuestage", (stage.getId() + 1)+"");
		}
		// mark current as completed
		FileManager.setProfileData("stagescompleted", true, stage.getId());
	}

	// GAMESTATS REFUGEES
	private int checkMonstersAliveCount() {
		int n = 0;
		for (int i =0; i < stage.getMonsters().size(); i++) {
			if (stage.getMonsters().get(i).getIsAlive()) n++;
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
		
		Notification n = new Notification("-"+value, 0, 1, Text.font_kubastaBig, 0xffff0000, false, x, y, false);
		addNotification(n);
	}
	
	public void displayHealing(int value, boolean isPlayer) {
		int x = 360, y = 60;
		if (isPlayer) x = 160;
		
		Notification n = new Notification("+ "+value+"", 0, 1, Text.font_kubastaBig, 0xff00ff00, false, x, y, false);
		addNotification(n);
	}
	
	public boolean isAskingForQuit() {
		return askForQuit;
	}
	
	public void setAskingForQuit(boolean b, String condition) {
		askForQuit = b;
		CombatLog.println("REQUESTING MAIN MENU. CONDITION: " + condition);
	}
	
	// GETTERS
	public boolean isOnGlobalCooldown() {
		return onGlobalCooldown;
	}

}
