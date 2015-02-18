package com.daenils.moisei.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.GUI;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;
import com.daenils.moisei.graphics.Stage;
import com.daenils.moisei.graphics.Window;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;
import com.daenils.moisei.entities.Letter.Element;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.entities.equipments.Weapon;

public class Player extends Entity {
	private Keyboard input;
	private Mouse inputM;
	Random rand = new Random(System.nanoTime());
	private boolean canUseSkills;
//	private Ability[] playerAbility = new Ability[4];
	
	protected boolean neverCycled;
	protected boolean isRadialMenuUp = false;
	
	protected int weaponSwitcher = 0;
	
	protected int goldAmount;
	
	// LETTER INVENTORY STUFF
	protected boolean letterWindowHasOpened;
	
	// LETTER STUFF
	protected List<Letter> letterInventory = new ArrayList<Letter>();
	protected List<Letter> letterBar = new ArrayList<Letter>();
	protected List<Letter> radialMenu = new ArrayList<Letter>();
	protected int letterAmount, maxLetterAmount;
	
	protected int initialLetterSpawn = 2 * 8;
	protected int[] letterCount = new int[26];
	protected String letterCountString = "";
	protected int vowelCount, consonantCount;
	
	// LETTER GEN STUFF
	private List<Character> letterlistValue;
	private int[] letterlistAdjustedValue = new int[26];
	protected int[] letterlistUpperBr = new int[26];
	private char lastLetter = 'A';
	
	private int[] letterBaseDroprate = {81, 15, 27, 43, 120, 23, 
			20, 59, 73, 1, 7, 40,
				26, 70, 77, 18, 1, 60,
					63, 91, 29, 11, 21, 2,
						21, 1};
	
	protected int[] letterDroprate = new int[26];
	protected int[] letterDroprateBracket = new int[26];
	protected int rollMax = -1;
	protected int genVowelCount = 0;

	public Player(Keyboard input, Mouse inputM, Entity defaultTarget, Stage stage) {
		this.name = "Player";
		this.type = "player";
		this.id = -1;
		
		this.stage = stage;
		
		this.x = 0;
		this.y = 0;
		
		this.input = input;
		this.inputM = inputM;
		this.canUseSkills = false;	
		this.abilityCount = 4;
		this.weaponCount = 10;
		
		
		
//		this.playerAbility[0] = new Ability(1, this);
//		this.playerAbility[1] = new Ability(2, this);
//		this.playerAbility[2] = new Ability(3, this);
//		this.playerAbility[3] = new Ability(1, this);
		
		initAbilities();
		initWeapons();
		
		this.baseHealth = 100;
		this.baseMana = 25;
		this.maxActionPoints = 1;
		this.spellPower = 1;
		this.baseDamage = new int[] {4, 7};
		this.shield = 0;
		this.isAlive = true;
		this.xp = 0;
		this.level = 1;
		this.setGoldAmount(0);
		
		this.maxHealth = baseHealth;
		this.maxMana = baseMana;
		this.maxLetterAmount = 20;
		
		this.damage = baseDamage;
		this.damageDbl[0] = baseDamage[0];
		this.damageDbl[1] = baseDamage[1];
		
		this.levelUp((byte) 1);
		if (this.level < 2) {
			this.xpGained = 10; // initialized here for now
			this.xpNeeded = 10; // initialized here for now
		} else {
			setXpGained();
			setXpNeeded();
		}
		
		this.actionPoints = maxActionPoints;
		this.health = maxHealth;
		this.mana = maxMana;
		
		this.currentTarget = null;
		setPercentageValues();
		
		// TODO: TEST LINE, PLS REMOVE
		initLetters();
		
/*		spawnLetter('C', Element.NEUTRAL);
		spawnLetter('K', Element.FIRE);
		spawnLetter('A', Element.NEUTRAL);
		spawnLetter('X', Element.WATER);
		spawnLetter('E', Element.FIRE);
		spawnLetter('B', Element.EARTH);
		spawnLetter('A', Element.WIND); */
	
	}
	
	private void initLetters() {
	//	for (int i = 0; i < 10; i++) {
	//		spawnLetter();
	//	}
		spawnLettersNEW(initialLetterSpawn);
		
	}

	public void initAbilities() {
		unlockAbility(this, 1);
		unlockAbility(this, 2);
		unlockAbility(this, 3);
		unlockAbility(this, 0);
	}
	
	public void initWeapons() {
//		unlockWeapon(this, 1);
//		unlockWeapon(this, 2);
//		unlockWeapon(this, 4);
		if (this.weapons.size() > 0) this.weapon = weapons.get(0);
	}
	
	public void update() {

	//	System.out.println(Letter.mapLetterDroptable);
	//	System.out.println(Letter.mapLetterDroptableSorted);
		if (actionPoints == 0) Game.getGameplay().endTurn(this);

		if (!letterWindowHasOpened) {
			openLetterWindow();
			openLetterBar();
			letterWindowHasOpened = true;
		}
		
		updateLetterContents();
		letterWindow();
		letterBar();
		
		setPercentageValues();
		updateAbilities();
	//	updateLetterlist();
	//	applyDots();
		
//		System.out.println("A3 LAST: " + abilities.get(3).getLastUsed());
//		System.out.println("A3 CD: " + abilities.get(3).isOnCooldown());
		
		// set a default target
		if (currentTarget == null && Game.getGameplay().monstersAlive > 0) {
	//		System.out.println("new target...");
			newCycledTarget();
		}
		

		
		// Check if it's the player turn and no cooldown and alive:
		if (Game.getGameplay().getIsPlayerTurn() && !Game.getGameplay().onGlobalCooldown && actionPoints > 0 && isAlive == true && Game.getGameplay().getContinueGame())
			canUseSkills = true;
		else canUseSkills = false;
		
		// KEY BINDINGS
		// BASIC ATTACK
		if (input.playerBasicAttack && canUseSkills) {
			basicAttack(this, currentTarget, weapon);
			Game.getGameplay().enableGlobalCooldown();
		}
		
		// ALPHABET
		for (int i = 0; i < 26; i++) {
			if (input.alphabet[i] && canUseSkills && !isRadialMenuUp) {
				CombatLog.println("Letter " + (char) (i+65) + ".");
				if (letterCount[i] == 1) {
					selectLetterByValue(i);
				}
				else if (letterCount[i] > 1) {
					createRadialMenu(i);
				}
				Game.getGameplay().enableGlobalCooldown();
			}
		}
		
		// REMOVE LAST
		if (input.removeLast && canUseSkills && letterBar.size() > 0) {
			deselectLast();
			Game.getGameplay().enableGlobalCooldown();
		}
		
		// RADIAL MENU
		for (int i = 0; i < radialMenu.size(); i++) {
			if (input.radialChoice[i] && canUseSkills && isRadialMenuUp) {
				selectLetterFromRadialMenuByOrder(i);
				isRadialMenuUp = false;
				}
		}
		
		// Q ABILITY
		if (input.playerQ && canUseSkills && (abilities.size() > 0)) {
		//	useAbility(this, abilities.get(0));
		//	getUnstuck();
			Game.getGameplay().endTurn(this);
			Game.getGameplay().enableGlobalCooldown();
		}
		
		// W ABILITY
		if (input.playerW && canUseSkills && (abilities.size() > 1)) {
		//	useAbility(this, abilities.get(1));
			spawnLettersNEW(1);
			Game.getGameplay().enableGlobalCooldown();
		}
		
		// E ABILITY
		if (input.playerE && canUseSkills && (abilities.size() > 2)) {
		//	useAbility(this, abilities.get(2));
			Game.getGameplay().enableGlobalCooldown();
		}
		
		// R ABILITY
		if (input.playerR && canUseSkills && (abilities.size() > 3)) {
			useAbility(this, abilities.get(3));
			Game.getGameplay().enableGlobalCooldown();
		}
		
		// END TURN
		if (input.playerEndTurn && !Game.getGameplay().onGlobalCooldown && Game.getGameplay().getIsPlayerTurn() && !Game.getGameplay().getForcedPause()) {
			// System.out.println("!!!");
			if (Game.getGameplay().monstersAlive > 0) {
				submitWord();
			//	Game.getGameplay().endTurn(this);
			}
			else {
				Game.getGameplay().setContinueGame(true);
			//	Game.getGameplay().gameFlow();
			}
			// every keybind available to the player has to contain this line:
			Game.getGameplay().enableGlobalCooldown(); 
		}
		
		// SWITCH WEAPONS
		if (input.playerSwitchWeapon && !Game.getGameplay().onGlobalCooldown && Game.getGameplay().getIsPlayerTurn()) {
			if (this.weapons.size() > 0) {
				if (weaponSwitcher == weapons.size() - 1) {
					this.weapon = null;
					weaponSwitcher = -1;
					}
				else {
					weaponSwitcher++;
					this.weapon = weapons.get(weaponSwitcher);
				}
				if (this.weapon != null) CombatLog.println(this.name + " equips " + this.weapon.getName());
				else if (this.weapon == null) CombatLog.println(this.name + " sheaths his/her weapon.");
				Game.getGameplay().enableGlobalCooldown();
			}
				
		}
		
		// SWITCH GUI VIEW
		if (input.playerSwitchGUIView && !Game.getGameplay().onGlobalCooldown) {
			if (!Game.getGameplay().getPercentageView()) Game.getGameplay().setPercentageView(true);
			else if (Game.getGameplay().getPercentageView()) Game.getGameplay().setPercentageView(false);
			Game.getGameplay().enableGlobalCooldown(); 
		}
		
		// PAUSE GAME
		if (input.playerPauseGame && !Game.getGameplay().onGlobalCooldown) {
			if (Game.getGameplay().getContinueGame()) {
				Game.getGameplay().setContinueGame(false);
				Game.getGameplay().setForcedPause(true);
			}
			else if (!Game.getGameplay().getContinueGame()) {
				Game.getGameplay().setContinueGame(true);
				Game.getGameplay().setForcedPause(false);
			}
			Game.getGameplay().enableGlobalCooldown(); 
		}
		
		// DEBUG: ADD MONSTER
		if (input.debugAddMonster && !Game.getGameplay().onGlobalCooldown) {
			if (Game.getGameplay().monstersAlive > 0) Game.getGameplay().spawnMonster();
			else Game.getGameplay().newMonsterWave();
		}
		
		// DEBUG: FORCE NEW WAVE
		if (input.debugForceNewWave && !Game.getGameplay().onGlobalCooldown) {
		//	Game.getGameplay().newMonsterWave();
		//	((Monster) currentTarget).isAlive = false;
		//	Game.getGameplay().playerOverride = true;
		//	stage.setRandomStage();
	//		Game.getGameplay().enableGlobalCooldown(); 
		}
		
		// DEBUG: TOGGLE FPS LOCK
		if (input.debugToggleFpsLock && !Game.getGameplay().onGlobalCooldown) {
			// DEBUG FUNCTION TO TOGGLE FPS LOCK ON/OFF
				Game.toggleFpsLock();
				System.err.print("\n" + Game.isFpsLockedString());
				Game.getGameplay().enableGlobalCooldown(); 
		}
		
		// DEBUG: SHOW DEBUG INFO
		if (input.debugShowDebugInfo && !Game.getGameplay().onGlobalCooldown) {
			if (!Game.getGameplay().getDebugView()) Game.getGameplay().setDebugView(true);
			else if (Game.getGameplay().getDebugView()) Game.getGameplay().setDebugView(false);
			Game.getGameplay().enableGlobalCooldown();
		}
		

		inputTargeting();
		if (GUI.getNoWindows()) mouseInput();

		
		// THE FOLLOWING TWO PROBABLY DO NOT NEED TO BE CONSTANTLY UPDATED (CAN BE MOVED LATER ON)
		updateVowelCount();
		updateConsonantCount();
		
		
		// LETTERCOUNT TO STRING
		letterCountString = "";
		for (int i = 0; i < letterCount.length; i++) {
			letterCountString += ((char) (int) (i+65)) + "" + letterCount[i] + " ";
		}
		
	//	System.out.println(vowelCount + consonantCount);
		
		cleanRadialMenu();
		
	}
	
	protected void updateVowelCount() {
		int vowelCounter = 0;
		for (int i = 0; i < getLetterInventory().size(); i++) {
			if (isVowel(getLetterInventory().get(i).value))
				vowelCounter++;
		}
		vowelCount = vowelCounter;
		genVowelCount = vowelCount;
	}
	
	// USED IN GENERATING ONLY
	protected void updateVowelCounter(char c) {
		if (isVowel(c)) genVowelCount++;
	}
	
	protected void updateConsonantCount() {
		consonantCount = (getLetterInventory().size() - vowelCount);
	}
	
	protected void updateAbilities() {
//		for (int i = 0; i < abilities.size(); i++) abilities.get(i).update();
//		removeAbility();
	}

	private void inputTargeting() {
			// OLD
			for (int i = 0; i < stage.getMonsters().size(); i++) {
				if (input.playerTarget[i] && !Game.getGameplay().onGlobalCooldown) {
					setTarget(stage.getMonsters().get(i));
					targetCycled = i + 1;
					Game.getGameplay().enableGlobalCooldown();
				}
			}
		
		
			// NEW
			if (targetCycled >= stage.getMonsters().size()) targetCycled = 0;
//			System.out.println(targetCycled);
			if (input.playerCycleTargets && !Game.getGameplay().onGlobalCooldown) {
				newCycledTarget();
				Game.getGameplay().enableGlobalCooldown();
			}
			
		
	}
	public void newCycledTarget() {
		newCycledTarget(2);
	}
	
	public void newCycledTarget(int times) {
		neverCycled = true;
		for (int i = 1; i < times; i++) {
//			if (Gamestats.monsterCount < 1) cycleTarget(null);
			if (stage.getMonsters().size() == 1) cycleTarget(stage.getMonsters().get(0));
			if (stage.getMonsters().size() > 1) {
				cycleTarget(stage.getMonsters().get(targetCycled));
			}
		}
		
	}
	
	private void mouseInput() {
		// TODO: ALL OF THESE CODE SEGMENTS SHOULD BE EXTRACTED INTO SEPARATE METHODS SO THEY 
		// COULD BE ACCESSED BY BOTH THE KEYBOARD INPUT CODE AND THE MOUSE INPUT CODE
		
		// BASIC ATTACK
				for (int i = 0; i < stage.getMonsters().size(); i++) {
					if (Mouse.getB() == 1 && canUseSkills && this.currentTarget == stage.getMonsters().get(i) && !Game.getGameplay().onGlobalCooldown
							&& Mouse.getX() > stage.getMonsters().get(i).x * Game.getScale()
							&& Mouse.getX() < (stage.getMonsters().get(i).x + stage.getMonsters().get(i).width) * Game.getScale()
							&& Mouse.getY() > stage.getMonsters().get(i).y * Game.getScale()
							&& Mouse.getY() < (stage.getMonsters().get(i).y + stage.getMonsters().get(i).height) * Game.getScale()) {
						basicAttack(this, currentTarget, weapon);
						Game.getGameplay().enableGlobalCooldown();
					}
				}
		
		// TARGETING
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			if (Mouse.getB() == 1 && stage.getMonsters().size() > i && !Game.getGameplay().onGlobalCooldown
					&& Mouse.getX() > stage.getMonsters().get(i).x * Game.getScale()
					&& Mouse.getX() < (stage.getMonsters().get(i).x + stage.getMonsters().get(i).width) * Game.getScale()
					&& Mouse.getY() > stage.getMonsters().get(i).y * Game.getScale()
					&& Mouse.getY() < (stage.getMonsters().get(i).y + stage.getMonsters().get(i).height) * Game.getScale()) {
				setTarget(stage.getMonsters().get(i));
				targetCycled = i + 1;
				Game.getGameplay().enableGlobalCooldown();
			}
		}
		
		// HOVER: MONSTER INFO (DETAILS)
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			if (Mouse.getB() == -1 && stage.getMonsters().size() > i
					&& Mouse.getX() > stage.getMonsters().get(i).x * Game.getScale()
					&& Mouse.getX() < (stage.getMonsters().get(i).x + stage.getMonsters().get(i).width) * Game.getScale()
					&& Mouse.getY() > stage.getMonsters().get(i).y * Game.getScale()
					&& Mouse.getY() < (stage.getMonsters().get(i).y + stage.getMonsters().get(i).height) * Game.getScale()) {
				stage.getMonsters().get(i).showDetails = true;
			}
			else stage.getMonsters().get(i).showDetails = false;
		}
		
		// QWER ABILITIES
		for (int i = 0; i < abilities.size(); i++) {
			int[] spellPos = {GUI.screenSpellPos1, GUI.screenSpellPos2, GUI.screenSpellPos3, GUI.screenSpellPos4};
			if (Mouse.getB() == 1 && canUseSkills && (abilities.size() > i)
					&& !Game.getGameplay().onGlobalCooldown
					&& Mouse.getX() > spellPos[i] * Game.getScale()
					&& Mouse.getX() < (spellPos[i] + 30) * Game.getScale()
					&& Mouse.getY() > 620
					&& Mouse.getY() < 680) {
				useAbility(this, abilities.get(i));
				Game.getGameplay().enableGlobalCooldown();
			}
		}
		
		
		// HOVER: QWER ABILITY TOOLTIPS
		for (int i = 0; i < abilities.size(); i++) {
			int[] spellPos = {GUI.screenSpellPos1, GUI.screenSpellPos2, GUI.screenSpellPos3, GUI.screenSpellPos4};
			if (Mouse.getB() == -1 && (abilities.size() > i)
					&& Mouse.getX() > spellPos[i] * Game.getScale()
					&& Mouse.getX() < (spellPos[i] + 30) * Game.getScale()
					&& Mouse.getY() > 620
					&& Mouse.getY() < 680) {
				abilities.get(i).showTooltip = true;
			}
			else abilities.get(i).showTooltip = false;
		}
		
		// END TURN
		if (Mouse.getB() == 1 && !Game.getGameplay().onGlobalCooldown && Game.getGameplay().getIsPlayerTurn() && !Game.getGameplay().getForcedPause()
				&& Mouse.getX() > 395
				&& Mouse.getX() < 512
				&& Mouse.getY() > 609
				&& Mouse.getY() < 623) {
			if (Game.getGameplay().monstersAlive > 0) Game.getGameplay().endTurn(this);
			else {
				Game.getGameplay().setContinueGame(true);
			}
			Game.getGameplay().enableGlobalCooldown(); 
		}
	}
	
	
	
	public void render(Screen screen) {
		// RENDER ABILITIES AT THE ACTION BAR
		int[] spellPosHelper = new int[]{GUI.screenSpellPos1, GUI.screenSpellPos2, GUI.screenSpellPos3, GUI.screenSpellPos4};
		for (int i = 0; i < abilities.size(); i++) {
			screen.renderSprite(spellPosHelper[i], GUI.screenBottomElements + 11, abilities.get(i).getIcon(), 0);
		}
		
		if (this.weapon != null) screen.renderSprite(600, 322, this.weapon.getIcon(), 0);
		else if (this.weapon == null) screen.renderSprite(600, 322, Sprite.noweapon, 0);
		
		// RENDER RADIAL MENU
		int[][] radialMenuIcon = {
				{180, 100 - 5},
				{180 + 26 + 5, 100 + 26},
				{180, 100 + 26 + 26 + 5},
				{180 - 26 - 5, 100 + 26}
		};

		for (int i = 0; i < radialMenu.size(); i++) {
			for (int l = 0; l < 30; l++) {
				for (int k = 0; k < 30; k++) {
					screen.renderPixel(k + radialMenuIcon[i][0], l + radialMenuIcon[i][1], radialMenu.get(i).getFrame());
				}
			}			
		}
		
		if (isRadialMenuUp) {
			if (radialMenu.size() > 0) screen.renderSprite(radialMenuIcon[0][0], radialMenuIcon[0][1], radialMenu.get(0).icon, 1); // UP
			if (radialMenu.size() > 1) screen.renderSprite(radialMenuIcon[1][0], radialMenuIcon[1][1], radialMenu.get(1).icon, 1); // RIGHT
			if (radialMenu.size() > 2) screen.renderSprite(radialMenuIcon[2][0], radialMenuIcon[2][1], radialMenu.get(2).icon, 1); // DOWN
			if (radialMenu.size() > 3) screen.renderSprite(radialMenuIcon[3][0], radialMenuIcon[3][1], radialMenu.get(3).icon, 1); // LEFT
			
		}
		
	}
	
	// LETTER STUFF
	// LETTER MECHANICS
	// MECHANIC: LETTER WINDOW
		private void openLetterWindow() {
			Game.getGameplay().getGUI().createWindow(360, 200, 260, 90, 0xff555555, true, "INVENTORY");
			Game.getGameplay().getGUI().getWindow("inventory").add(8,2);
			Game.getGameplay().getGUI().getWindow("inventory").setLetterContents(this.getLetterInventory());
		//	getContents(gui.getWindow("letters"), this.letterInventory);
		}

		
		// MECHANIC: LETTER BAR
		private void openLetterBar() {
			Game.getGameplay().getGUI().createWindow(160, 5, 320, 70, 0xff555555, true, "LETTERBAR");
			Game.getGameplay().getGUI().getWindow("letterbar").add(10,1);
			Game.getGameplay().getGUI().getWindow("letterbar").add(1, Window.BUTTON_OK);
			Game.getGameplay().getGUI().getWindow("letterbar").setLetterContents(this.getLetterBar());
		//	getContents(gui.getWindow("letterbar"), this.letterBar);
		}
		
		private void letterWindow() {
			if (Mouse.getB() == 1
					&& Game.getGameplay().getGUI().getWindow("inventory") != null
				//	&& gui.getWindow("inventory").getRequestedLetterNum() > 0
					&& Game.getGameplay().getGUI().getWindow("inventory").getRequestedLetter() != null
					&& Game.getGameplay().getGUI().getWindow("inventory").getRequestedLetter().getIsHoveredOver()
					&& !Game.getGameplay().onGlobalCooldown) {
				this.selectLetterById(Game.getGameplay().getGUI().getWindow("inventory").getRequestedLetter().getId());
				Game.getGameplay().enableGlobalCooldown();
			}
			
			if (Mouse.getB() == 3
					&& Game.getGameplay().getGUI().getWindow("inventory") != null
					&& Game.getGameplay().getGUI().getWindow("inventory").getRequestedLetterNum() > 0
					&& Game.getGameplay().getGUI().getWindow("inventory").getRequestedLetter() != null
					&& Game.getGameplay().getGUI().getWindow("inventory").getRequestedLetter().getIsHoveredOver()
					&& !Game.getGameplay().onGlobalCooldown) {
		//		this.despawnLetter(gui.getWindow("inventory").getRequestedLetterNum(), l);
				Game.getGameplay().enableGlobalCooldown();
			}
		}

		private void updateLetterContents() {
			Game.getGameplay().getGUI().getWindow("letterbar").setLetterContents(this.getLetterBar());
			Game.getGameplay().getGUI().getWindow("inventory").setLetterContents(this.getLetterInventory());
		}
		
		private void letterBar() {
			if (Mouse.getB() == 1
					&& Game.getGameplay().getGUI().getWindow("letterbar") != null
				//	&& gui.getWindow("letterbar").getRequestedLetterNum() > -1
					&& Game.getGameplay().getGUI().getWindow("letterbar").getRequestedLetter() != null
					&& Game.getGameplay().getGUI().getWindow("letterbar").getRequestedLetter().getIsHoveredOver()
					&& !Game.getGameplay().onGlobalCooldown) {
				this.deselectLetterById(Game.getGameplay().getGUI().getWindow("letterbar").getRequestedLetter().getId());
				Game.getGameplay().enableGlobalCooldown();
			}
			
			if (Game.getGameplay().getGUI().getWindow("letterbar").getClickedDialogueOption()
					&& !Game.getGameplay().onGlobalCooldown) {
				submitWord();
				Game.getGameplay().enableGlobalCooldown();
			}
		}
		
		// MECHANIC: RADIAL MENU
		private void createRadialMenu(int n) {
			String[] radialString = {"UP", "RIGHT", "DOWN", "LEFT"};
			// ADD ALL LETTERS TO THE RADIAL MENU
			for (int i = 0; i < letterCount[n]; i++) {
				isRadialMenuUp = true;
				addLetterToRadialByValue(n);
			}
			
			// DISPLAY THE RADIAL MENU
			
			// [TEMP DISPLAY CODE]
			for (int i = 0; i < radialMenu.size(); i++) {
				CombatLog.println(radialString[i] + ": " + radialMenu.get(i).getType().toString());
			}
		}
		
		private void cleanRadialMenu() {
			// DROP OTHER(S) BACK
			if (!isRadialMenuUp) {
				for (int i = 0; i < radialMenu.size(); i++) {
					deselectLetterFromRadialMenu(i);
					System.out.println("%!+");
				}
			}
		}
		
		
		private void submitWord() {
			String word = getWordFromBar();
			CombatLog.println("Word submitted: " + word);
			checkWord(word);
		}
		
		private void checkWord(String word) {
			int playerDamage;
			int dominantElement;
			if (lookupWord(word.toLowerCase())) {
				CombatLog.println("Yay! Such a nice word: " + word.toLowerCase() + "!");
				dominantElement = identifyDominantElement(letterBar);
				playerDamage = getWordDamage(letterBar.size(), dominantElement);
				this.clearLetterBar();
				this.dealDamage(this, this.currentTarget, playerDamage);
				Game.getGameplay().endTurn(this);
			} else
				CombatLog.println("Sorry, not a word.");
		}
		

		private static boolean lookupWord(String word) {
			for (int i = 0; i < FileManager.lines.size(); i++) {
				if (FileManager.lines.get(i).equals(word)) return true;
			}
			return false;
		}

		private String getWordFromBar() {
			String word = "";
			for (int i = 0; i < this.getLetterBar().size(); i++) {
				word += (this.getLetterBar().get(i).getValue());
			}
			return word;
		}
		
		private void getContents(Window w, List<Letter> list) {
			w.clean();
			for (int i = 0; i < list.size(); i++) {
				w.add(list.get(i));
			}
		}
	
		
	// LETTER STUFF
	// COMBAT
		
		private int identifyDominantElement(List<Letter> list) {
			// sum damage sources (letters by elements)
			int[] elementConstituents = new int[5]; //N-F-WA-E-WI
			int dominantElement = 0;
			int returnValue = -1;
			for (int i = 0; i < list.size(); i++) {
				switch(list.get(i).getType()) {
				case NEUTRAL: elementConstituents[0]++; break;
				case FIRE: elementConstituents[1]++; break;
				case WATER: elementConstituents[2]++; break;
				case EARTH: elementConstituents[3]++; break;
				case WIND: elementConstituents[4]++; break;
				default: System.out.println("ERROR: Element type unidentified."); break;
				}
			}
			
			// TEST LINE
			CombatLog.println("N: " + elementConstituents[0] +
					" " + "F: " + elementConstituents[1] +
					" " + "WA: " + elementConstituents[2] +
					" " + "E: " + elementConstituents[3] +
					" " + "WI: " + elementConstituents[4]
					);
			
			// check for tier 2 bonus
			// TODO: Finish this, either the rounding is wrong or something definitely is!
			for (int i = 0; i < elementConstituents.length; i++) {
				if (elementConstituents[i] > dominantElement) dominantElement = elementConstituents[i];
			}

			// i > 0 is only so neutral won't count as dominant in a word (every word without any dominant element is neutral inherently!)
			String[] elementName = {"Neutral","Fire","Water","Earth","Wind"};
			for (int i = 0; i < elementConstituents.length; i++) {
				if (elementConstituents[i] == dominantElement && dominantElement > list.size() / 2 && i > 0) {
					returnValue = i;
					CombatLog.println("Dominant element: " + elementName[i]);
				}
			}
			return returnValue;
		}
		
		private int getWordDamage(int wordLength, int dominantElement) {
			double baseDamage = 0;
			double eDamage = 0;
			int healing = 0; // percent (of player hp)
			// BASIC DAMAGE CODE HERE
			baseDamage = wordLength * 1; // value is 1 for testing purposes only, it should be a multiplier later
			
			// is there dominant element?
 			if (dominantElement > 0) {
 				// DOMINANT ELEMENT-RELATED CODE HERE
 				CombatLog.println("The dominant element is " + dominantElement);
 				switch(dominantElement) {
 				case 1:
 					CombatLog.println("FIRE!");
 					if (this.isWindBuffed) {
 						baseDamage *= 2;
 						this.isWindBuffed = false;
 					}
 					else baseDamage *= 1.5;
 					break;
 				case 2:
 					CombatLog.println("WATER!"); 
 					baseDamage *= 0.5;
 					if (this.isWindBuffed) {
 						healing = 20;
 						this.isWindBuffed = false;
 					}
 					else healing = 10;
 					// maybe change it to the proper method you used with spells earlier, but now it seems to work just fine:
 					if (this.pHealth > (100 - healing)) this.restoreHealth(100);
 					else this.restoreHealth(this.pHealth + healing); 
 					CombatLog.println(healing + " percent of player health has been restored.");
 					break;
 				case 3:
 					CombatLog.println("EARTH!");
 					baseDamage *= 1.0;
 					letterStun(this, this.currentTarget);
 					CombatLog.println(this.currentTarget.name + " is stunned for 1 turn.");
 					break;
 				case 4:
 					CombatLog.println("WIND!");
 					baseDamage *= 0.5;
 					letterWindBuff(this);
 					break;
 				}
 			} else
 				CombatLog.println("No dominant element.");
 			
 			// RETURN
			return (int) (baseDamage + eDamage);
		}
		
		// GET UNSTUCK: replace random letters with new ones at the cost of your turn
		protected void getUnstuck() {
			CombatLog.println("Help me!");
			int randomSelected[] = new int[2];
			// Select 2 random letters
			randomSelected[0] = rand.nextInt((((this.letterInventory.size() - 1) - 0) + 1) + 0);
			do {
				randomSelected[1] = rand.nextInt((((this.letterInventory.size() - 1) - 0) + 1) + 0);
			} while(randomSelected[1] == randomSelected[0] || randomSelected[1] == 14);
			CombatLog.println("Letters dropped: " + this.letterInventory.get(randomSelected[0]).getValue() + " " + this.letterInventory.get(randomSelected[1]).getValue());
			
			// Remove them
			removeLetter(randomSelected[0], this.letterInventory);
			removeLetter(randomSelected[1], this.letterInventory);
			
			// Add 2 random letters
			spawnLettersNEW(2);
			
			
		}
		
		
	// LETTER STUFF
	//  LETTER LISTS	
	
	/*
	/	THESE FOUR BELOW ARE vanilla methods, DO NOT use them directly, use the unlock* variants instead!
	*/
	
	private void addLetter(Letter l) {
		letterInventory.add(l);
	}
	
	private void removeLetter(int n, List<Letter> list) {
		list.remove(n);
	}

	private void addLetterToBar(Letter l) {
		// remove from inv?
		letterBar.add(l);
	}
	
	private void removeLetterFromBar(int n) {
		letterBar.remove(n);
		// add to inv?
	}
	
	// "UNLOCK" METHODS (TO USE)
	// 1A. ADD LETTER TO PLAYER INVENTORY
	protected void spawnLetter(char value, Element element) {
		if (this.letterInventory.size() < this.maxLetterAmount) {
			Letter letter = new Letter(value, element);
			this.addLetter(letter);
			CombatLog.println("Letter " + letter.getValue() + " added.");
		}
	}
	
	// 1B. ADD FULL RANDOM LETTER TO PLAYER INVENTORY
	protected void spawnLetter() {
		char randomValue = getRandomCharacter();
		Element randomElement = getRandomElement();
		if (this.letterInventory.size() < this.maxLetterAmount) {
			Letter letter = new Letter(randomValue, randomElement);
			this.addLetter(letter);
			CombatLog.println("Letter " + letter.getValue() + " added.");
		}
	}
	
	// 1C. ADD LETTER WITH RANDOM ELEMENT
	protected void spawnLetter(char value) {
		// TODO: stub
	}
	// 1D. ADD RANDOM LETTER OF GIVEN ELEMENT
	protected void spawnLetter(Element element) {
		// TODO: stub
	}
	
	// 1E. SPAWN A SET OF FULL RANDOM LETTERS
	protected void spawnLetters(int amount) {
		char[] randomCharacters = getRandomCharacters(amount);
		char value;
		Element randomElement;
		if (this.letterInventory.size() < this.maxLetterAmount) {
			for (int i = 0; i < amount; i++) {
				value = randomCharacters[i];
				randomElement = getRandomElement();
				Letter letter = new Letter(value, randomElement);
				this.addLetter(letter);
				this.letterCount[letter.getValue() - 65]++;
				CombatLog.println("Letter " + letter.getValue() + " added.");
			}
		}
	
		printLetterCount();
	}

	protected void printLetterCount() {
		String lettercountstring = "Lettercount: ";
		for (int i = 0; i < letterCount.length; i++) {
			lettercountstring += letterCount[i] + " ";
		}
		CombatLog.println(lettercountstring);
		System.out.println();
	}
	
	// 1F. SPAWN A SET OF FULL RANDOM CONSONANTS
		protected void spawnConsonants(int amount) {
			char[] randomCharacters = new char[amount];
			for (int i = 0; i < amount; i++) {
				randomCharacters[i] = getRandomConsonant();
			}
			char value;
			Element randomElement;
			if (this.letterInventory.size() < this.maxLetterAmount) {
				for (int i = 0; i < amount; i++) {
					value = randomCharacters[i];
					randomElement = getRandomElement();
					Letter letter = new Letter(value, randomElement);
					this.addLetter(letter);
					this.letterCount[letter.getValue() - 65]++;
					CombatLog.println("Letter " + letter.getValue() + " added.");
				}
			}
		}
		
	// 1F. SPAWN A SET OF FULL RANDOM VOWELS
			protected void spawnVowels(int amount) {
				char[] randomCharacters = new char[amount];
				for (int i = 0; i < amount; i++) {
					randomCharacters[i] = getRandomVowel();
				}
				char value;
				Element randomElement;
				if (this.letterInventory.size() < this.maxLetterAmount) {
					for (int i = 0; i < amount; i++) {
						value = randomCharacters[i];
						randomElement = getRandomElement();
						Letter letter = new Letter(value, randomElement);
						this.addLetter(letter);
						this.letterCount[letter.getValue() - 65]++;
						CombatLog.println("Letter " + letter.getValue() + " added.");
					}
				}				
			}
			
	// 1G. SPAWN A SET OF FULL RANDOM LETTERS #2 (NEW)
			protected void spawnLettersNEW_OLD(int amount) {
				char[] randomCharacters = new char[amount];
				char temp;
				int generatedVowels;
				int bonusVowels;
				final int MINIMUM_VOWELS = 6; // (6 for 40%)
				bonusVowels = rand.nextInt(((2 - 0) + 1) + 0);
				bonusVowels--; // TODO: this is lame, just fix it by fixing the line above
				System.out.println("\nVowel Modifier: "+ bonusVowels);
				generatedVowels = 0;
 
				// check the letter inventory
				if (vowelCount < MINIMUM_VOWELS) {
					// get vowels first
					// add random factor so it is not always fixed amount of vowels
					// random bw. 0-2
					
					generatedVowels = MINIMUM_VOWELS - vowelCount + bonusVowels;
					for (int i = 0; i < (generatedVowels); i++) {
						do {
							temp = getRandomVowel();
						} while (letterCount[temp - 65] > 2); // THIS CONTROLS THE MAX AMOUNT PER LETTER
						randomCharacters[i] = temp;
						this.letterCount[temp - 65]++;
						System.out.print("" + randomCharacters[i] + " ");
					}
					
					// get consonants (the rest)
					temp = 0;
					for (int i = 0; i < (amount - generatedVowels); i++) {
						do {
							temp = getRandomConsonant();
						} while (letterCount[temp - 65] > 1);
						randomCharacters[i + generatedVowels] = temp;
						this.letterCount[temp - 65]++;
						System.out.print("" + randomCharacters[i] + " ");
					}
				} else {
					// get consonants
					for (int i = 0; i < amount; i++) {
						do {
							temp = getRandomConsonant();
						} while (letterCount[temp - 65] > 1);
						randomCharacters[i + generatedVowels] = temp;
						this.letterCount[temp - 65]++;
						System.out.print("" + randomCharacters[i] + " ");
					}					
				}
				
				char value;
				String newLetters = "";
				Element randomElement;
				if (this.letterInventory.size() < this.maxLetterAmount) {
					for (int i = 0; i < amount; i++) {
						value = randomCharacters[i];
						randomElement = getRandomElement();
						Letter letter = new Letter(value, randomElement);
						this.addLetter(letter);
						newLetters += letter.getValue() + " ";
					}
				}
				
				CombatLog.println("New letters: " + newLetters);
		
				System.out.println();
				// printLetterCount();
				
				
				
			}
			
	
			
			protected void spawnLettersNEW(int amount) {
				// data
				// TODO: make it into a nice hashmap like the other stuff and read it from file
					
				//	if (letterInventory.size() == 16) letterInventory.removeAll(letterInventory);
					
					updateLetterDroprate();
					updateLetterDroprateBracket();
					updateRollMax();
					
					//	updateLetterlistValueOrder();
					//	getLetterlistAdjustedValues();
					//	updateLetterlistUpperBracket();
						
						char randomCharacters[] = new char[amount];

						for (int i = 0; i < randomCharacters.length; i++) {
							
							// get a random letter according to the drop table
							int randomNum = rand.nextInt(((rollMax - 1) + 1) + 1);
							CombatLog.println("[ROLL " + randomNum + "]");
					
							
							// look through 'bracket )'
							for (int l = 25; l >= 0; l--) {
							    if(randomNum <= letterDroprateBracket[l]) {
							    	randomCharacters[i] = (char) (l + 65);
							    }
							}
							this.letterCount[randomCharacters[i] - 65]++;		
							
							updateVowelCounter(randomCharacters[i]);
							System.out.println(genVowelCount);
							updateLetterDroprate();
							updateLetterDroprateBracket();
							updateRollMax();
						}
						
						char value;
						String newLetters = "";
						Element randomElement;
						if (this.letterInventory.size() < this.maxLetterAmount) {
							 for (int i = 0; i < amount; i++) {
								value = randomCharacters[i];
								lastLetter = value;
								randomElement = getRandomElement();
								Letter letter = new Letter(value, randomElement);
								updateVowelCount();
								this.addLetter(letter);
								
								newLetters += letter.getValue() + " ";
							 }
						}
						
						CombatLog.println("New letter(s): " + newLetters);
						
						// printLetterCount();
						genVowelCount = vowelCount;
						
			}

			private void updateLetterlistUpperBracket() {
				letterlistUpperBr[0] = 9999;
				for (int i = 1; i < 26; i++) {
					letterlistUpperBr[i] = (letterlistUpperBr[i-1] - letterlistAdjustedValue[i-1]);
					System.out.print(letterlistUpperBr[i-1] + ", ");
				}
			}
			
			private void updateLetterDroprateBracket() {
				letterDroprateBracket[0] = letterDroprate[0];
			//	System.out.println(letterDroprateBracket[0]);
				for (int i = 1; i < letterDroprate.length; i++) {
					letterDroprateBracket[i] = letterDroprateBracket[i - 1] + letterDroprate[i];
			//		System.out.println(letterDroprateBracket[i]);
				}
			}
			
			private void updateRollMax() {
				rollMax = 0;
				for (int i = 0; i < 26; i++) {
					rollMax += letterDroprate[i];
				}
				System.out.println("ROLLMAX: " + rollMax);	
			}
			
			private void updateLetterDroprate() {
				// SETTING THE DEFAULT VALUES (ZEROING)
				for (int i = 0; i < letterDroprate.length; i++) {
					letterDroprate[i] = letterBaseDroprate[i];
				}
				
				// VOWELCOUNT-BASED PRIORITY ADJUSTMENT
				if (genVowelCount < 6) {
					letterDroprate[0] *= 3;
					letterDroprate[4] *= 3;
					letterDroprate[8] *= 3;
					letterDroprate[14] *= 3;
					letterDroprate[20] *= 3;
					System.out.println("ERR! NOT ENOUGH VOWELS!");
				}
				
				// LOOKING UP THE LETTERS THAT NEED ADJUSTMENTS (I.E. EVERYTHING THAT HAS 1 OR MORE COPIES IN THE INVENTORY)
				for (int i = 0; i < letterDroprate.length; i++) {
					switch(letterCount[i]) {
					case 0:
						break;
					case 1:
						Math.ceil(letterDroprate[i] /= 2);
						if(letterDroprate[i] == 0) letterDroprate[i]++; // TODO: this is cheating!
						System.out.println("1 of " + (char) (i+65) + ". Droprate is now: " + letterDroprate[i]);
						break;
					case 2:
						Math.ceil(letterDroprate[i] /= 4);
						if(letterDroprate[i] == 0) letterDroprate[i]++;
						System.out.println("2 of " + (char) (i+65) + ". Droprate is now: " + letterDroprate[i]);
						break;
					case 3:
						Math.ceil(letterDroprate[i] /= 8);
						if(letterDroprate[i] == 0) letterDroprate[i]++;
						System.out.println("3 of " + (char) (i+65) + ". Droprate is now: " + letterDroprate[i]);
						break;
					case 4:
						Math.ceil(letterDroprate[i] /= 16);
						if(letterDroprate[i] == 0) letterDroprate[i]++;
						System.out.println("4 of " + (char) (i+65) + ". Droprate is now: " + letterDroprate[i]);
						break;
					default:
						System.out.println("ERR! MORE THAN 4 of letter " + (char) (i+65) + "!");
					}
				}
				
				// MAKING ADJUSTMENTS (HALVING, ETC.)
				
				
			}
	
	// 2. REMOVE LETTER FROM PLAYER INVENTORY (FOR GOOD)
	protected void despawnLetter(int n, List<Letter> l) {
		CombatLog.println("Letter " + letterBar.get(n).getValue() + " removed.");
		this.letterCount[letterBar.get(n).getValue() - 65]--;
		removeLetter(n, l);
	}
	
	protected void despawnLetterById(int n, List<Letter> list) {
		CombatLog.println("Letter " + list.get(n).getValue() + " removed (by id).");
		for (int i = 0; i < list.size(); i++) {			
			if (list.get(i).getId() == n) {
				this.letterCount[letterBar.get(n).getValue() - 65]--;
				removeLetter(i, list);				
			}
		}
	}
	
	// 3. MOVE LETTER FROM INVENTORY TO BAR
	protected void selectLetter(int n) {
		CombatLog.println("Letter " + letterInventory.get(n).getValue() + " got selected.");
		letterBar.add(letterInventory.get(n));
		removeLetter(n, letterInventory);
	}
	
	protected void selectLetterById(int n) {
		for (int i = 0; i < letterInventory.size(); i++) {
			if (letterInventory.get(i).getId() == n) {
				CombatLog.println("Letter " + letterInventory.get(i).getValue() + " got selected (by id).");
				letterBar.add(letterInventory.get(i));
				removeLetter(i, letterInventory);
			}
		}
	}
	
	protected void selectLetterByValue(int n) {
		for (int i = 0; i < letterInventory.size(); i++) {
			if (letterInventory.get(i).getValue() == (char) n+65) {
				CombatLog.println("Letter " + letterInventory.get(i).getValue() + " got selected (by value).");
				letterBar.add(letterInventory.get(i));
				removeLetter(i, letterInventory);
			}
		}

	}
	
	protected void selectLetterFromRadialMenuByOrder(int n) {
		for (int i = 0; i < radialMenu.size(); i++) {
			if (i == n) {
				CombatLog.println("Letter " + radialMenu.get(i).getValue() + " got selected (by order).");
				letterBar.add(radialMenu.get(i));
				removeLetter(i, radialMenu);
			}
		}
	}
	
	
	protected void addLetterToRadialByValue(int n) {
		for (int i = 0; i < letterInventory.size(); i++) {
			if (letterInventory.get(i).getValue() == (char) n+65) {
				CombatLog.println("Letter " + letterInventory.get(i).getValue() + " got selected (by value).");
				radialMenu.add(letterInventory.get(i));
				removeLetter(i, letterInventory);
			}
		}

	}
	
	
	// 2. MOVE LETTER FROM THE BAR BACK TO THE INVENTORY
	protected void deselectLetter(int n) {
		CombatLog.println("Letter " + letterBar.get(n).getValue() + " got deselected.");
		letterInventory.add(letterBar.get(n));
		removeLetter(n, letterBar);
	}
	
	protected void deselectLetterById(int n) {
		for (int i = 0; i < letterBar.size(); i++) {
			if (letterBar.get(i).getId() == n) {
				CombatLog.println("Letter " + letterBar.get(i).getValue() + " got deselected (by id).");		
				letterInventory.add(letterBar.get(i));
				removeLetter(i, letterBar);
			}
		}
	}
	
	protected void deselectLast() {
		int n = letterBar.size() - 1;
		CombatLog.println("Letter " + letterBar.get(n).getValue() + " got deselected.");
		letterInventory.add(letterBar.get(n));
		removeLetter(n, letterBar);
	}
	
	protected void deselectLetterFromRadialMenu(int n) {
		CombatLog.println("Letter " + radialMenu.get(n).getValue() + " got deselected.");
		letterInventory.add(radialMenu.get(n));
		removeLetter(n, radialMenu);
	}
	
	// RANDOMS
	private char getRandomCharacter() {
		int n;
		n = rand.nextInt( ( 'Z' - 'A' ) + 1) + 'A';
		return (char) n;
	}
	
	private char[] getRandomCharacters(int amount) {
		char[] characters = new char[amount];
		boolean[] areVowels = new boolean[amount];
		boolean genDone;
		int vowelCount = 0;
		
		do {
			// a) generation 
			for (int i = 0; i < amount; i++) {
				characters[i] = getRandomCharacter();
			}
			
			// b-1) quality-assurance: flag for vowel/consonant
			for (int i = 0; i < amount; i++) {
				areVowels[i] = isVowel(characters[i]);
			}
			
			// b-2) quality-assurance: check ratio
			for (int i = 0; i < amount; i++) {
				if (areVowels[i] == true) { 
					vowelCount++;
				//	System.out.print(characters[i]);
				}
			}
			
			if (vowelCount * 100 / amount < 40) {
				genDone = false;
				for (int i = 0; i < characters.length; i++) {
					areVowels[i] = false;
				}
				vowelCount = 0;
			} else genDone = true;
		} while (!genDone);
		
		// c) return
		CombatLog.println("All is well: " + vowelCount);
		return characters;
	}
	
	private char getRandomVowel() {
		int n = 0;
		do {
			n = rand.nextInt( ('Z' - 'A') + 1) + 'A';
		}
		while (n >= 66 && n <= 68
				||  n >= 70 && n <= 72
				|| n >= 74 && n <= 78
				|| n >= 80 && n <= 84
				|| n >= 86 && n <= 87
				|| n >= 88 && n <= 90
				);
		
		return (char) n;			
	}
	
	private char getRandomConsonant() {
		int n = 0;
		do {
			n = rand.nextInt( ('Z' - 'A') + 1) + 'A';
		}
		while (n == 'A' || n == 'E' || n == 'I' || n == 'O' || n == 'U');
		
		return (char) n;
	}

	private Element getRandomElement() {
		int n = rand.nextInt( ( 100 - 1 ) + 1) + 1;
		Element e;
		
		if (n <= 8) e = Element.NEUTRAL;
		else if (n <= 31) e = Element.FIRE;
		else if (n <= 54) e = Element.WATER;
		else if (n <= 77) e = Element.EARTH;
		else if (n <= 100) e = Element.WIND;
		else {
			e = Element.NEUTRAL;
			CombatLog.print("ERROR: Random element roll is higher than 100.");
		}
		
		switch (n) {
		case 1:  break;
		case 2:  break;
		case 3:  break;
		case 4:  break;
		case 5:  break;
		default:  break;
		}
		return e;
	}
	
	public boolean isVowel(char c) {
		if (c >= 66 && c <= 68
				||  c >= 70 && c <= 72
				|| c >= 74 && c <= 78
				|| c >= 80 && c <= 84
				|| c >= 86 && c <= 87
				|| c >= 88 && c <= 90
				) return false;
		else return true;
	}
	
	public boolean isConsonant(char c) {
		if (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U') return false;
		else return true;
	}
	
	public Player getPlayer() {
		return this;
	}
	
	public boolean getCanUseSkills() {
		return canUseSkills;
	}

	public int getGoldAmount() {
		return goldAmount;
	}
	
	public List<Letter> getLetterInventory() {
		return letterInventory;
	}
	
	public List<Letter> getLetterBar() {
		return letterBar;
	}
	
	public void setGoldAmount(int n) {
		goldAmount = n;
	}
	
	public void addGold(int n) {
		goldAmount += n;
	}
	
	public void removeGold(int n) {
		goldAmount -= n;
	}

	protected void clearLetterBar() {
	//	int[] idsToClear = new int[letterBar.size()]; 
	//	for (int i = 0; i < letterBar.size(); i++) {
	//		System.out.print("\nDespawning " + i + " (" + letterBar.get(i).getValue() + "[" + letterBar.get(i).getId() + "])");
	//		idsToClear[i] = letterBar.get(i).getId();
	//	}
		
	//	for (int i = 0; i < idsToClear.length; i++)
	//		despawnLetterById(idsToClear[i], letterBar);	
		
		int idsToClear = 0;
		for (int i = 0; i < letterBar.size(); i++)
			idsToClear++;

		for (int i = 0; i < idsToClear; i++)
			despawnLetter(idsToClear - i - 1, letterBar);		
	}
}
