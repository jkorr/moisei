package com.daenils.moisei.entities;

import java.util.Calendar;
import java.util.Date;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.GUI;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;
import com.daenils.moisei.graphics.Stage;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.entities.equipments.Weapon;

public class Player extends Entity {
	private Keyboard input;
	private Mouse inputM;
	private boolean canUseSkills;
//	private Ability[] playerAbility = new Ability[4];
	
	protected boolean neverCycled;
	protected int weaponSwitcher = 0;
	
	protected int goldAmount;

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
	}
	
	public void initAbilities() {
		unlockAbility(this, 1);
		unlockAbility(this, 2);
		unlockAbility(this, 3);
		unlockAbility(this, 4);
	}
	
	public void initWeapons() {
//		unlockWeapon(this, 1);
//		unlockWeapon(this, 2);
//		unlockWeapon(this, 4);
		if (this.weapons.size() > 0) this.weapon = weapons.get(0);
	}
	
	public void update() {

		setPercentageValues();
		updateAbilities();
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
		
		// Q ABILITY
		if (input.playerQ && canUseSkills && (abilities.size() > 0)) {
			useAbility(this, abilities.get(0));
			Game.getGameplay().enableGlobalCooldown();
		}
		
		// W ABILITY
		if (input.playerW && canUseSkills && (abilities.size() > 1)) {
			useAbility(this, abilities.get(1));
			Game.getGameplay().enableGlobalCooldown();
		}
		
		// E ABILITY
		if (input.playerE && canUseSkills && (abilities.size() > 2)) {
			useAbility(this, abilities.get(2));
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
			if (Game.getGameplay().monstersAlive > 0) Game.getGameplay().endTurn(this);
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
	
	public void setGoldAmount(int n) {
		goldAmount = n;
	}
	
	public void addGold(int n) {
		goldAmount += n;
	}
	
	public void removeGold(int n) {
		goldAmount -= n;
	}
}
