package com.daenils.moisei.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.entities.Letter.Element;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Notification;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;
import com.daenils.moisei.graphics.Stage;
import com.daenils.moisei.graphics.Text;
import com.daenils.moisei.graphics.Window;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;

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
	
	private int radialMenuSize;
	private int radialMenuX = 305, radialMenuY = 130;
	private int[][] radialMenuIcon = {
			{radialMenuX, radialMenuY - 5}, // FIRE
			{radialMenuX + 26 + 5, radialMenuY + 26}, // WIND
			{radialMenuX, radialMenuY + 26 + 26 + 5}, // WATER
			{radialMenuX - 26 - 5, radialMenuY + 26}, // EARTH
			{radialMenuX, radialMenuY + 26} // NEUTRAL
	};
	
	private int[] letterBaseDroprate = {81, 15, 27, 43, 120, 23, 
			20, 59, 73, 3, 7, 40,
				26, 70, 77, 18, 3, 60,
					63, 91, 29, 11, 21, 5,
						21, 3};
	
	protected int[] letterDroprate = new int[26];
	protected int[] letterDroprateBracket = new int[26];
	protected int rollMax = -1;
	protected int genVowelCount = 0;
	
	private int selectedInRadialMenuCount = 0;
	
	private int[] elementBaseDroprate = {	25,			// NEUTRAL 
												100, 		// FIRE
												100, 		// WATER
												100, 		// EARTH
												75,};		// WIND
													
	
	protected int[] elementDroprate = new int[5];
	protected int[] elementDroprateBracket = new int[5];
	
	// NEW GAMEPLAY STUFF (ELEMENTAL POWER)
	protected int[] elementalPower = new int[4]; // fi wa ea wi
	protected int[] elementalPowerCap = new int[4]; // each item equals to the amount of points required to have that segment filled -> add all of them to get the cap for the bar
	protected int elementalPowerCapSum;

	public Player(Keyboard input, Mouse inputM, Entity defaultTarget, Stage stage) {
		// FILL TEST CAPS
		fillBaseElementalPowerCaps();
		
		this.name = "Player";
		this.type = "player";
		this.id = -1;
		this.sprite = Sprite.player0;
		
		this.stage = stage;
		
		setXY();
		
		this.input = input;
		this.inputM = inputM;
		this.canUseSkills = false;	
		this.abilityCount = 4;
		this.weaponCount = 10;
		
		
		
//		this.playerAbility[0] = new Ability(1, this);
//		this.playerAbility[1] = new Ability(2, this);
//		this.playerAbility[2] = new Ability(3, this);
//		this.playerAbility[3] = new Ability(1, this);
		
	//	initAbilities();
	//	initWeapons();
		
		this.baseHealth = 50;
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
		
		this.setSpawned(true);
		
/*		spawnLetter('C', Element.NEUTRAL);
		spawnLetter('K', Element.FIRE);
		spawnLetter('A', Element.NEUTRAL);
		spawnLetter('X', Element.WATER);
		spawnLetter('E', Element.FIRE);
		spawnLetter('B', Element.EARTH);
		spawnLetter('A', Element.WIND); */
	
	}

	private void fillBaseElementalPowerCaps() {
		for (int i = 0; i < elementalPowerCap.length; i++) 	{
			elementalPowerCap[i] = (i+1) * 3;
			elementalPowerCapSum += elementalPowerCap[i];
		}
	}
	
	private void initLetters() {
	//	for (int i = 0; i < 10; i++) {
	//		spawnLetter();
	//	}
		spawnLettersNEW(initialLetterSpawn);
		
	}

	public void initAbilities() {
//		unlockAbility(this, 1);
//		unlockAbility(this, 2);
//		unlockAbility(this, 3);
//		unlockAbility(this, 0);
	}
	
	public void initWeapons() {
//		unlockWeapon(this, 1);
//		unlockWeapon(this, 2);
//		unlockWeapon(this, 4);
		if (this.weapons.size() > 0) this.weapon = weapons.get(0);
	}
	
	public void update() {
		
		if (isGettingDamage && System.nanoTime() > (this.flashStart + this.flashDuration)) this.isGettingDamage = false;

	//	System.out.println(Letter.mapLetterDroptable);
	//	System.out.println(Letter.mapLetterDroptableSorted);
		if (actionPoints == 0) Game.getGameplay().endTurn(this);

		if (!letterWindowHasOpened) {
			openLetterWindow();
		//	openLetterBar();
			letterWindowHasOpened = true;
		}
		
		updateLetterContents();
		letterWindow();
	//	letterBar();
		
		radialMenuSize = 0;
		for (int i= 0; i < letterInventory.size(); i++)
			if (letterInventory.get(i).getIsSelectedInRadialMenu()) radialMenuSize++;
		
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
/*		if (input.playerBasicAttack && canUseSkills) {
			basicAttack(this, currentTarget, weapon);
			Game.getGameplay().enableGlobalCooldown();
		}*/
		
		// ALPHABET
		for (int i = 0; i < 26; i++) {
			
			if (input.alphabet[i] && canUseSkills && !isRadialMenuUp && Game.getGameplay().getIsPlayerTurn()) {
		//		CombatLog.println("Letter " + (char) (i+65) + ".");
				if (letterCount[i] == 1 && !checkLetterByValue(i)) {
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
		// TODO: change it to a switch?
		for (int i = 0; i < 5; i++) {
			if (input.radialChoice[i] && canUseSkills && isRadialMenuUp) {
				selectLetterFromRadialMenuByElement(i);
				isRadialMenuUp = false;
				}
		}
		
//		System.out.println(radialMenuSize);
		
		// Q ABILITY
	//	if (input.playerQ && canUseSkills && (abilities.size() > 0)) {
		if (input.playerQ && canUseSkills) {
		//	useAbility(this, abilities.get(0));
		//	getUnstuck();
			Game.getGameplay().testNotif();
		//	Game.getGameplay().endTurn(this);
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
			if (Game.getGameplay().monstersAlive > 0 && this.isAlive) {
				this.resetCurrentWord();
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
/*		if (input.playerSwitchWeapon && !Game.getGameplay().onGlobalCooldown && Game.getGameplay().getIsPlayerTurn()) {
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
		*/
		
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
	//	if (input.debugAddMonster && !Game.getGameplay().onGlobalCooldown) {
	//		if (Game.getGameplay().monstersAlive > 0) Game.getGameplay().spawnMonster();
	//		else Game.getGameplay().newMonsterWave();
	//	}
		
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
		if (Screen.getNoWindows()) mouseInput();

		
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
	
	private void setXY() {
		x = 190; y = 134;
	}
	
	public void render(Screen screen) {
		// RENDER SELF
		if (this.isGettingDamage) screen.renderSpriteAsColor(x, y, sprite, 1, 0xffffffff);
		else screen.renderSprite(x, y, sprite, 1);
		
		// RENDER RADIAL MENU
		int[] frameColors = {0xffff0000, 0xffbbbbbb, 0xff0000ff, 0xff00ff00, 0xffffffff};
		
		// render empty
		if (isRadialMenuUp) {
			for (int i = 0; i < 5; i++) {
				screen.renderSprite( radialMenuIcon[0][0], radialMenuIcon[0][1], Sprite.letter[27+5], 1);
				screen.renderSprite( radialMenuIcon[1][0], radialMenuIcon[1][1], Sprite.letter[30+5], 1);
				screen.renderSprite( radialMenuIcon[2][0], radialMenuIcon[2][1], Sprite.letter[28+5], 1);
				screen.renderSprite( radialMenuIcon[3][0], radialMenuIcon[3][1], Sprite.letter[29+5], 1);
				screen.renderSprite( radialMenuIcon[4][0], radialMenuIcon[4][1], Sprite.letter[26+5], 1);
				
				/*	for (int l = 0; l < 30; l++) {
					for (int k = 0; k < 30; k++) {
						screen.renderPixel(k + radialMenuIcon[i][0], l + radialMenuIcon[i][1], frameColors[i]);
					}
				} */	
			}
		}
		
		for (int i = 0; i < letterInventory.size(); i++) {
			if (letterInventory.get(i).getIsSelectedInRadialMenu()) {
				switch(letterInventory.get(i).getType()) {
				case NEUTRAL:
					renderRadialMenuItem(screen, i, 4);
					break;
				case FIRE:
					renderRadialMenuItem(screen, i, 0);
					break;
				case WATER:
					renderRadialMenuItem(screen, i, 2);
					break;
				case EARTH:
					renderRadialMenuItem(screen, i, 3);
					break;
				case WIND:
					renderRadialMenuItem(screen, i, 1);
					break;
				default:	
					break;
				}
			}
		}
		
		
		
		
	}
	
	// RENDERCODE FOR RADIAL MENU ITEMS
	private void renderRadialMenuItem(Screen screen, int i, int e) {
		for (int l = 0; l < 30; l++) {
			for (int k = 0; k < 30; k++) {
				screen.renderPixel(k + radialMenuIcon[e][0], l + radialMenuIcon[e][1], letterInventory.get(i).getFrame());
			}
		}		
		screen.renderSprite(radialMenuIcon[e][0], radialMenuIcon[e][1], letterInventory.get(i).getIcon(), 1);
	}
	
	// LETTER STUFF
	// LETTER MECHANICS
	// MECHANIC: LETTER WINDOW

		private void openLetterWindow() {
			Screen.createWindow(191, 271, 260, 0, 0xff555555, true, "INVENTORY");
			Screen.getWindow("inventory").add(8,2);
			Screen.getWindow("inventory").setLetterContents(this.getLetterInventory());
		//	getContents(gui.getWindow("letters"), this.letterInventory);
		}

		
		// MECHANIC: LETTER BAR
/*		private void openLetterBar() {
			Screen.createWindow(160, 303, 320, 0, 0xff555555, true, "LETTERBAR");
			Screen.getWindow("letterbar").add(10,1);
		//	Screen.getWindow("letterbar").add(1, Window.BUTTON_OK);
			Screen.getWindow("letterbar").setLetterContents(this.getLetterBar());
		//	getContents(gui.getWindow("letterbar"), this.letterBar);
		} */
		
		private void letterWindow() {
			if (Mouse.getB() == 1
					&& Screen.getWindow("inventory") != null
				//	&& gui.getWindow("inventory").getRequestedLetterNum() > 0
					&& Screen.getWindow("inventory").getRequestedLetter() != null
					&& Screen.getWindow("inventory").getRequestedLetter().getIsHoveredOver()
					&& !Game.getGameplay().onGlobalCooldown) {
		//		this.selectLetterById(Screen.getWindow("inventory").getRequestedLetter().getId());
				Game.getGameplay().enableGlobalCooldown();
			}
			
			if (Mouse.getB() == 3
					&& Screen.getWindow("inventory") != null
					&& Screen.getWindow("inventory").getRequestedLetterNum() > 0
					&& Screen.getWindow("inventory").getRequestedLetter() != null
					&& Screen.getWindow("inventory").getRequestedLetter().getIsHoveredOver()
					&& !Game.getGameplay().onGlobalCooldown) {
		//		this.despawnLetter(gui.getWindow("inventory").getRequestedLetterNum(), l);
				Game.getGameplay().enableGlobalCooldown();
			}
		}

		private void updateLetterContents() {
	//		Screen.getWindow("letterbar").setLetterContents(this.getLetterBar());
			Screen.getWindow("inventory").setLetterContents(this.getLetterInventory());
		}
		
		private void letterBar() {
			if (Mouse.getB() == 1
					&& Screen.getWindow("letterbar") != null
				//	&& gui.getWindow("letterbar").getRequestedLetterNum() > -1
					&& Screen.getWindow("letterbar").getRequestedLetter() != null
					&& Screen.getWindow("letterbar").getRequestedLetter().getIsHoveredOver()
					&& !Game.getGameplay().onGlobalCooldown) {
				this.deselectLetterById(Screen.getWindow("letterbar").getRequestedLetter().getId());
				Game.getGameplay().enableGlobalCooldown();
			}
			
			if (Screen.getWindow("letterbar").getClickedDialogueOption()
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
				
				int ic = 0;
				for (int l = 0; l < letterInventory.size(); l++) {
					if (letterInventory.get(l).getValue() == (char) n+65 && !letterInventory.get(l).getIsSelectedInRadialMenu()) {
						addLetterToRadialByValue(n);
						ic++;
					}
				}
		//		System.out.println(ic);
		
			
			}
			// CHECK IF ONLY ONE ELEMENT IS IN THE RADIALMENU AND SELECT THAT IF SO
			checkSelectedInRadialMenuCount();
			if (selectedInRadialMenuCount < 2) {
				for (int i = 0; i < letterInventory.size(); i++) {
					selectLetterFromRadialMenuByOrder(i);
					isRadialMenuUp = false;
				}
			}
			
			// [TEMP DISPLAY CODE]
			for (int i = 0; i < radialMenuSize; i++) {
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
			//CURRENTWORD STUFF
			int i = 0, k = 0;
			
			while (i < word.length()) {
				if (k % 2 == 0) {
					this.currentWord[k] = (char) (currentWordColors[i] + 48);
					k++;
				} else {
					this.currentWord[k] = word.charAt(i);
					i++;
					k++;					
				}
			}
			
			this.currentWordLength = word.length() * 2;
			
			
			CombatLog.println("Word submitted: " + word);
			checkWord(word);
		}
		
		private void checkWord(String word) {
			int playerDamage;
			int[] dominantElement;
			if (lookupWord(word.toLowerCase())) {
				CombatLog.println("Yay! Such a nice word: " + word.toLowerCase() + "!");
				dominantElement = identifyDominantElement(letterBar);
				playerDamage = getWordDamage(letterBar.size(), dominantElement);
				countElementalPowerPoints();
				removeSelectedLetters();
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
				switch (this.getLetterBar().get(i).getFrame()) {
				case 0xffffffff: currentWordColors[i] = 1; break;			// N
				case 0xffff0000: currentWordColors[i] = 2; break;		// FIRE
				case 0xff0000ff: currentWordColors[i] = 4; break; 		// WATER
				case 0xff00ff00: currentWordColors[i] = 3; break;		// EARTH
				case 0xffbbbbbb: currentWordColors[i] = 5; break;		// WIND
				default: currentWordColors[i] = 0; break;
				}
			}
			
			return word;
		}
	
		
		private void countElementalPowerPoints() {
			for (int i = 0; i < currentWordColors.length; i++) {
				switch(currentWordColors[i]) {
				case 2:
					// FIRE
					if (elementalPower[0] < elementalPowerCapSum){
						addElementalPower(0);						
					}
					break;
				case 4:
					// WATER
					if (elementalPower[1] < elementalPowerCapSum){						
						addElementalPower(1);
					}
					break;
				case 3:
					// EARTH
					if (elementalPower[2] < elementalPowerCapSum){						
						addElementalPower(2);
					}
					break;
				case 5:
					// WIND
					if (elementalPower[3] < elementalPowerCapSum){						
						addElementalPower(3);
					}
					break;
				default:	
					// NOTHING
					System.out.println("ERR? Nothing happens.");
					break;
				}
			}
			
		}

		private void getContents(Window w, List<Letter> list) {
			w.clean();
			for (int i = 0; i < list.size(); i++) {
				w.add(list.get(i));
			}
		}
	
		
	// LETTER STUFF
	// COMBAT
		
		private int[] identifyDominantElement(List<Letter> list) {
			// sum damage sources (letters by elements)
			int[] elementConstituents = new int[5]; //N-F-WA-E-WI
			int dominantElement[] = {0, 0, 0};
			int returnValue[] = {-1, -1, -1};
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
				if (elementConstituents[i] > dominantElement[0]) dominantElement[0] = elementConstituents[i];
			}

			// i > 0 is only so neutral won't count as dominant in a word (every word without any dominant element is neutral inherently!)
			String[] elementName = {"Neutral","Fire","Water","Earth","Wind"};
			for (int i = 0; i < elementConstituents.length; i++) {
				if (elementConstituents[i] == dominantElement[0] && dominantElement[0] > list.size() / 2 && i > 0) {
					returnValue[0] = i;
					CombatLog.println("Dominant element: " + elementName[i]);					
				}
				else if (elementConstituents[i] == dominantElement[0] && dominantElement[0] >= Math.ceil(list.size()) / 2 && i > 0)
					if (returnValue[1] == -1) {
						returnValue[1] = i;
						CombatLog.println("Minor Dom element 1: " + elementName[i]);
					}
					else {
						returnValue[2] = i;
						CombatLog.println("Minor Dom element 2: " + elementName[i]);
					}
			}
			return returnValue;
		}
		
		private int getWordDamage(int wordLength, int[] dominantElement) {
			double baseDamage = 0;
			double eDamage = 0;
			int healing = 0; // percent (of player hp)
			double[] temp;
			// BASIC DAMAGE CODE HERE
			baseDamage = wordLength * 1; // value is 1 for testing purposes only, it should be a multiplier later
			
			// is there dominant element?
 			if (dominantElement[0] > 0) {
 				// DOMINANT ELEMENT-RELATED CODE HERE
 				letterDominantEffect(this);
 				CombatLog.println("The dominant element is " + dominantElement[0]);
 				temp = lookUpWordEffect(dominantElement[0], baseDamage, healing);
 				baseDamage = temp[0];
 				healing = (int) temp[1];
 			} else
 				CombatLog.println("No dominant element.");
 			
 			if (dominantElement[1] > 0) {
 				CombatLog.println("The minor dominant element 1 is " + dominantElement[1]);
 				temp = lookUpWordEffect(dominantElement[1], baseDamage, healing);
 				baseDamage = temp[0];
 				healing = (int) temp[1];
 			}
 			if (dominantElement[2] > 0) {
 				CombatLog.println("The minor dominant element 2 is " + dominantElement[2]);
 				temp = lookUpWordEffect(dominantElement[2], baseDamage, healing);
 				baseDamage = temp[0];
 				healing = (int) temp[1];
 			}
 			
 			// RETURN
			return (int) (baseDamage + eDamage);
		}
		
		private double[] lookUpWordEffect(int n, double baseDamage, int healing) {
			double[] results = new double[2];
			switch(n) {
				case 1:
					CombatLog.println("FIRE!");
					if (this.hasDominantEffect) {
						baseDamage *= 2;
						this.hasDominantEffect = false;
					}
					else baseDamage *= 1.5;
					break;
				case 2:
					CombatLog.println("WATER!"); 
					if (this.hasDominantEffect) {
						baseDamage *= 0.5;
						healing = 10;
						this.hasDominantEffect = false;
					}
					else healing = 5;
					// maybe change it to the proper method you used with spells earlier, but now it seems to work just fine:
					if (this.pHealth > (100 - healing)) this.restoreHealth(100);
					else this.restoreHealth(this.pHealth + healing); 
					Game.getGameplay().displayHealing(healing, true);
					CombatLog.println(healing + " percent of player health has been restored.");
					break;
				case 3:
					CombatLog.println("EARTH!");
					if (this.hasDominantEffect) {
						baseDamage *= 1.0;
						letterStun(this, this.currentTarget);
						CombatLog.println(this.currentTarget.name + " is stunned for 1 turn.");
						this.hasDominantEffect = false;
					} else baseDamage *= 1.5;
					break;
				case 4:
					CombatLog.println("WIND!");
					if (this.hasDominantEffect) {
						baseDamage = 0;
						this.damageReduction = 90;
						this.hasDominantEffect = false;
					} else {
						baseDamage *= 0.5;
						this.damageReduction = 40;						
					}
					break;
				}
			
			results[0] = baseDamage;
			results[1] = healing;
			return results;
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
				//			CombatLog.println("[ROLL " + randomNum + "]");
					
							
							// look through 'bracket )'
							for (int l = 25; l >= 0; l--) {
							    if(randomNum <= letterDroprateBracket[l]) {
							    	randomCharacters[i] = (char) (l + 65);
							    }
							}
							this.letterCount[randomCharacters[i] - 65]++;		
							
							updateVowelCounter(randomCharacters[i]);
				//			System.out.println(genVowelCount);
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
				
						
			}
			
			private void updateElementDroprate() {
				// SETTING THE DEFAULT VALUES (ZEROING)
				for (int i = 0; i < elementDroprate.length; i++) {
					elementDroprate[i] = elementBaseDroprate[i];
				}
			}
			
			private void updateElementDroprateBracket() {
				elementDroprateBracket[0] = elementDroprate[0];
			//	System.out.println(letterDroprateBracket[0]);
				for (int i = 1; i < elementDroprate.length; i++) {
					elementDroprateBracket[i] = elementDroprateBracket[i - 1] + elementDroprate[i];
			//		System.out.println(letterDroprateBracket[i]);
				}
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
		//		System.out.println("ROLLMAX: " + rollMax);	
			}
			
			private void updateLetterDroprate() {
				// SETTING THE DEFAULT VALUES (ZEROING)
				for (int i = 0; i < letterDroprate.length; i++) {
					letterDroprate[i] = letterBaseDroprate[i];
				}
				
				// VOWELCOUNT-BASED PRIORITY ADJUSTMENT
				int dropMod = 4;
				if (genVowelCount < 5) {
					System.out.print("[!] VOWEL DROPRATE + (" + genVowelCount + ")] ");
					letterDroprate[0] *= dropMod;
					letterDroprate[4] *= dropMod;
					letterDroprate[8] *= dropMod;
					letterDroprate[14] *= dropMod;
					letterDroprate[20] *= dropMod;
					System.out.print("[ERR #01: NOT ENOUGH VOWELS (" + genVowelCount + ")] ");
				} else {
					System.out.print("[!] VOWEL DROPRATE - (" + genVowelCount + ")] ");
					letterDroprate[0] /= 2;
					letterDroprate[4] /= 2;
					letterDroprate[8] /= 2;
					letterDroprate[14] /= 2;
					letterDroprate[20] /= 2;
				}
				
				// LOOKING UP THE LETTERS THAT NEED ADJUSTMENTS (I.E. EVERYTHING THAT HAS 1 OR MORE COPIES IN THE INVENTORY)
				// MAKING ADJUSTMENTS (HALVING, ETC.)
				for (int i = 0; i < letterDroprate.length; i++) {
					switch(letterCount[i]) {
					case 0:
						break;
					case 1:
						Math.ceil(letterDroprate[i] /= 2);
						if(letterDroprate[i] == 0) letterDroprate[i]++; // TODO: this is cheating!
	//					System.out.println("1 of " + (char) (i+65) + ". Droprate is now: " + letterDroprate[i]);
						break;
					case 2:
						Math.ceil(letterDroprate[i] /= 4);
						if(letterDroprate[i] == 0) letterDroprate[i]++;
	//					System.out.println("2 of " + (char) (i+65) + ". Droprate is now: " + letterDroprate[i]);
						break;
					case 3:
						Math.ceil(letterDroprate[i] /= 8);
						if(letterDroprate[i] == 0) letterDroprate[i]++;
	//					System.out.println("3 of " + (char) (i+65) + ". Droprate is now: " + letterDroprate[i]);
						break;
					case 4:
						Math.ceil(letterDroprate[i] = 0);
	//					if(letterDroprate[i] == 0) letterDroprate[i]++;
	//					System.out.println("4 of " + (char) (i+65) + ". Droprate is now: " + letterDroprate[i]);
						break;
					default:
						System.out.print("[ERR #02: 5+ LETTER " + (char) (i+65) + "] ");
					}
				}				
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
				letterInventory.get(i).setIsSelected(true);
				letterBar.add(letterInventory.get(i));
		//		removeLetter(i, letterInventory);
			}
		}
	}
	
	protected void selectLetterFromRadialMenuByOrder(int n) {
		int[] selection = new int[4];
		int ic = 0;
		for (int i = 0; i < letterInventory.size(); i++) {
			if (letterInventory.get(i).getIsSelectedInRadialMenu()) {
			//	System.out.println("ic: " + ic);
				selection[ic] = i;
				ic++;
			}
			if (letterInventory.get(i).getIsSelectedInRadialMenu() && selection[n] == i && (ic - 1) == n) {
				System.out.println("s: " + selection[n] + " | i = " + i + " | n = " + n + " | ic = " + ic);
				CombatLog.println("Letter " + letterInventory.get(i).getValue() + " got selected (by order).");
				letterInventory.get(i).setIsSelected(true);
				letterBar.add(letterInventory.get(i));
			//	removeLetter(i, radialMenu);
			}
			letterInventory.get(i).setIsSelectedInRadialMenu(false);
		}
	}
	
	protected void selectLetterFromRadialMenuByElement(int n) {
		switch(n) {
		case 0:
			// fire
			checkForElement(Element.FIRE);
			break;
		case 1:
			// wind
			checkForElement(Element.WIND);
			break;
		case 2:
			// water
			checkForElement(Element.WATER);
			break;
		case 3:
			// earth
			checkForElement(Element.EARTH);
			break;
		case 4:
			// neutral
			checkForElement(Element.NEUTRAL);
			break;
		default:
			break;
		}
	}
	
	private void selectionByElement(Element e) {
		for (int i = 0; i < letterInventory.size(); i++) {
			if (letterInventory.get(i).getIsSelectedInRadialMenu() && letterInventory.get(i).getType() == e) {
				CombatLog.println("Letter " + letterInventory.get(i).getValue() + " got selected (by element).");
				letterInventory.get(i).setIsSelected(true);
				letterBar.add(letterInventory.get(i));
			}
			letterInventory.get(i).setIsSelectedInRadialMenu(false);
		}
	}
	
	private void checkSelectedInRadialMenuCount() {
		selectedInRadialMenuCount = 0;
		for (int i = 0; i < letterInventory.size(); i++)
			if (letterInventory.get(i).getIsSelectedInRadialMenu()) selectedInRadialMenuCount++;
	}
	
	private void checkForElement(Element e) {
		if (radialMenuHasElement(e)) {
			selectionByElement(e);
		} else {
			System.out.println("No " + e.name());
			disableRadialMenu();
		}
	}
	
	private void disableRadialMenu() {
		for (int i = 0; i < letterInventory.size(); i++)
		letterInventory.get(i).setIsSelectedInRadialMenu(false);
	}
	
	protected boolean checkLetterByValue(int n) {
		for (int i = 0; i < letterInventory.size(); i++) {
			if (letterInventory.get(i).getValue() == (char) n+65)
				return letterInventory.get(i).getIsSelected();
	//		else {
	//		System.out.println("ERR! LETTER CHECKING FAILED, AUTOMATIC FALSE SENT.");
	//		}
		}
		return false;
	}
	
	protected void addLetterToRadialByValue(int n) {
		for (int i = 0; i < letterInventory.size(); i++) {
			if (letterInventory.get(i).getValue() == (char) n+65
					&& !letterInventory.get(i).getIsSelected()
					&& !radialMenuHasElement(letterInventory.get(i).getType())
					){
				letterInventory.get(i).setIsSelectedInRadialMenu(true);
				CombatLog.println("Letter " + letterInventory.get(i).getValue() + " got selected (by value).");
			//	radialMenu.add(letterInventory.get(i));
			//	removeLetter(i, letterInventory);
			}
		}

	}
	
	private boolean radialMenuHasElement(Element e) {
		// 0: neutral 1: fire 2: water 3: earth 4: wind
		boolean[] elementsIn = new boolean[5];
		for (int i = 0; i < letterInventory.size(); i++)
			if (letterInventory.get(i).getIsSelectedInRadialMenu()) {
				switch(letterInventory.get(i).getType()) {
				case NEUTRAL:
					elementsIn[0] = true;
					break;
				case FIRE:
					elementsIn[1] = true;
					break;
				case WATER:
					elementsIn[2] = true;
					break;
				case EARTH:
					elementsIn[3] = true;
					break;
				case WIND:
					elementsIn[4] = true;
					break;
				default:
					System.out.println("ERR #03: WRONG ELEMENT IN THE RADIAL MENU.");
				}
			}
		
		if (e == Element.NEUTRAL && elementsIn[0] == false) return false;
		if (e == Element.FIRE && elementsIn[1] == false) return false;
		if (e == Element.WATER && elementsIn[2] == false) return false;
		if (e == Element.EARTH && elementsIn[3] == false) return false;
		if (e == Element.WIND && elementsIn[4] == false) return false;
		else return true;
	}
	
	// 2. MOVE LETTER FROM THE BAR BACK TO THE INVENTORY
	protected void deselectLetter(int n) {
		CombatLog.println("Letter " + letterBar.get(n).getValue() + " got deselected.");
		letterInventory.add(letterBar.get(n));
		removeLetter(n, letterBar);
	}
	
	protected void deselectLetterById(int n) {
		for (int i = 0; i < letterInventory.size(); i++) {
			if (letterInventory.get(i).getId() == n) {
				CombatLog.println("Letter " + letterInventory.get(i).getValue() + " got deselected (by id).");						
				letterInventory.get(i).setIsSelected(false);
			}
		}
	}
	
	protected void deselectLast() {
		int n = letterBar.size() - 1;
		CombatLog.println("Letter " + letterBar.get(n).getValue() + " got deselected.");
		deselectLetterById(letterBar.get(n).id);
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

/*	private Element getRandomElement() {
		// TODO: rewrite this like the new letter generation code (use that more dynamic loot table here as well)
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
		
		return e;
	} */
	
	private Element getRandomElement() {
		Element e = Element.NEUTRAL;
		Element[] eList = {Element.NEUTRAL, Element.FIRE, Element.WATER, Element.EARTH, Element.WIND, Element.MAGIC};
		int eRollMax = 0, n = -1;
		
		updateElementDroprate();
		updateElementDroprateBracket();
		// UPDATE eROLLMAX:
		for (int i = 0; i < elementDroprate.length; i++) {
			eRollMax += elementDroprate[i];
		} 
		
		
		n = rand.nextInt(((eRollMax - 1) + 1) + 1);
		
		// LOOK THROUGH BRACKET
		for (int l = (elementDroprate.length - 1); l >= 0; l--) {
		    if (n <= elementDroprateBracket[l]) {
		    	e = eList[l];
		   	}
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
	
	public boolean getInputPlayerEndTurn() {
		return input.playerEndTurn;
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

	protected void removeSelectedLetters() {
		int ic = 0;
		
		//	System.out.println(ic);
			do {
				ic = 0;
				for (int i = 0; i < letterInventory.size(); i++) {
					if (letterInventory.get(i).getIsSelected()) ic++;
				}
				
				for (int i = 0; i < letterInventory.size(); i++) {
					if (letterInventory.get(i).getIsSelected()) letterInventory.remove(i);				
				}
			} while (ic > 0);
			
			updateVowelCount();
			genVowelCount = vowelCount;
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
	
	public int getElementalPower(int i) {
		return elementalPower[i];
	}
	
	public int getElementalPowerCap(int i) {
		return elementalPowerCap[i];
	}
	
	public void setElementalPower(int i, int value) {
		elementalPower[i] = value;
	}
	
	public void setElementalPowerCap(int i, int value) {
		elementalPowerCap[i] = value;
	}
	
	public void addElementalPower(int i, int value) {
		elementalPower[i] += value;
	}
	
	public void addElementalPower(int i) {
		elementalPower[i]++;
	}
	
	public void removeElementalPower(int i, int value) {
		elementalPower[i] -= value;
	}
	
	public void emptyElementalPower(int i) {
		elementalPower[i] = 0;
	}
}
