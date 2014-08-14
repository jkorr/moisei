package com.daenils.moisei.entities;

import java.util.Calendar;
import java.util.Date;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.GUI;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Stage;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.entities.equipments.Weapon;

public class Player extends Entity {
	private Keyboard input;
	private boolean canUseSkills;
//	private Ability[] playerAbility = new Ability[4];
	
	protected boolean neverCycled;
	protected int weaponSwitcher = 0;

	public Player(Keyboard input, Entity defaultTarget) {
		this.name = "Player";
		this.type = "player";
		this.id = -1;
		
		this.x = 0;
		this.y = 0;
		
		this.input = input;
		this.canUseSkills = false;	
		this.abilityCount = 4;
		this.weaponCount = 10;
		
//		this.playerAbility[0] = new Ability(1, this);
//		this.playerAbility[1] = new Ability(2, this);
//		this.playerAbility[2] = new Ability(3, this);
//		this.playerAbility[3] = new Ability(1, this);
		
		initAbilities();
		initWeapons();
		
		this.damage = new int[] {3, 7};
		this.maxHealth = 100;
		this.maxMana = 25;
		this.maxActionPoints = 1;
		this.shield = 0;
		this.isAlive = true;
		this.xp = 0;
		this.level = 1;
		this.xpNeeded = 120; // temporarily fixed here
		
		
		this.actionPoints = maxActionPoints;
		this.health = maxHealth;
		this.mana = maxMana;
		
		this.stage = Stage.getStage();
		
		this.currentTarget = defaultTarget;
		setPercentageValues();
	}
	
	public void initAbilities() {
		unlockAbility(this, 1);
		unlockAbility(this, 2);
		unlockAbility(this, 3);
		unlockAbility(this, 0);
	}
	
	public void initWeapons() {
		unlockWeapon(this, 1);
		unlockWeapon(this, 2);
		unlockWeapon(this, 4);
		if (this.weapons.size() > 0) this.weapon = weapons.get(0);
	}
	
	public void update() {
		setPercentageValues();
		updateAbilities();
	//	applyDots();
		
//		System.out.println("A3 LAST: " + abilities.get(3).getLastUsed());
//		System.out.println("A3 CD: " + abilities.get(3).isOnCooldown());
		
		// set a default target
		if (currentTarget == null && Gamestats.monstersAlive > 0) newCycledTarget();
		
		
		// Check if it's the player turn and no cooldown and alive:
		if (Gamestats.isPlayerTurn && !Game.getGameplay().onGlobalCooldown && actionPoints > 0 && isAlive == true && Game.getGameplay().getContinueGame())
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
			if (Gamestats.monstersAlive > 0) Game.getGameplay().endTurn(this);
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
				CombatLog.println(this.name + " switched weapons.");
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
			if (Gamestats.monstersAlive > 0) Game.getGameplay().spawnMonster();
			else Game.getGameplay().newMonsterWave();
		}
		
		// DEBUG: FORCE NEW WAVE
		if (input.debugForceNewWave && !Game.getGameplay().onGlobalCooldown) {
		//	Game.getGameplay().newMonsterWave();
		//	((Monster) currentTarget).isAlive = false;
		//	Game.getGameplay().playerOverride = true;
		//	stage.setRandomStage();

			Game.getGameplay().enableGlobalCooldown(); 
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

	}
	
	
	
	protected void updateAbilities() {
//		for (int i = 0; i < abilities.size(); i++) abilities.get(i).update();
//		removeAbility();
	}

	private void inputTargeting() {
			// OLD
			for (int i = 0; i < Gamestats.monsterCount; i++) {
				if (input.playerTarget[i] && !Game.getGameplay().onGlobalCooldown) {
					setTarget(stage.getMonsters().get(i));
					targetCycled = i + 1;
					Game.getGameplay().enableGlobalCooldown();
				}
			}
		
		
			// NEW
			if (targetCycled >= Gamestats.monsterCount) targetCycled = 0;
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
			if (Gamestats.monsterCount == 1) cycleTarget(stage.getMonsters().get(0));
			if (Gamestats.monsterCount > 1) {
				cycleTarget(stage.getMonsters().get(targetCycled));
			}
		}
		
	}

	
	
	public void render(Screen screen) {
		int[] spellPosHelper = new int[]{GUI.screenSpellPos1, GUI.screenSpellPos2, GUI.screenSpellPos3, GUI.screenSpellPos4};
		for (int i = 0; i < abilities.size(); i++) {
			screen.renderSprite(spellPosHelper[i], GUI.screenBottomElements - 30, abilities.get(i).getIcon(), 0);
		}
	}
	
	public Player getPlayer() {
		return this;
	}
	
	public boolean getCanUseSkills() {
		return canUseSkills;
	}
	
	
	
}
