package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.GUI;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Stage;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.entities.equipments.Ability;

public class Player extends Entity {
	private Keyboard input;
	private boolean canUseSkills;
//	private Ability[] playerAbility = new Ability[4];
	
	protected boolean neverCycled;

	public Player(Keyboard input, Entity defaultTarget) {
		this.name = "Player";
		this.id = -1;
		
		this.x = 0;
		this.y = 0;
		
		this.input = input;
		this.canUseSkills = false;	
		this.abilityCount = 4;
		
//		this.playerAbility[0] = new Ability(1, this);
//		this.playerAbility[1] = new Ability(2, this);
//		this.playerAbility[2] = new Ability(3, this);
//		this.playerAbility[3] = new Ability(1, this);
		
		initAbilities();
		
		this.damage = new int[] {5, 11};
		this.health = 100;
		this.isAlive = true;
		this.mana = 25;
		this.xp = 0;
		this.level = 1;
		this.actionPoints = 1;
		this.defaultActionPoints = actionPoints;
		
		this.stage = Stage.getStage();
		
		this.defaultTarget = defaultTarget;
		

	}
	
	public void initAbilities() {
		unlockAbility(this, 1);
		unlockAbility(this, 2);
		unlockAbility(this, 3);
	}
	
	public void update() {
		updateAbilities();
		dealDots();
		
		// set a default target
		if (defaultTarget == null) newCycledTarget();
		
		
		// Check if it's the player turn and no cooldown and alive:
		if (Gamestats.isPlayerTurn && !Game.getGameplay().onGlobalCooldown && actionPoints > 0 && isAlive == true)
			canUseSkills = true;
		else canUseSkills = false;
		
		// KEY BINDINGS
		// BASIC ATTACK
		if (input.playerBasicAttack && canUseSkills) {
			basicAttack(this, defaultTarget);
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
		
		
		if (input.playerEndTurn && !Game.getGameplay().onGlobalCooldown && Game.getGameplay().getIsPlayerTurn()) {
			// System.out.println("!!!");
			Game.getGameplay().endTurn(this);
			// every keybind available to the player has to contain this line:
			Game.getGameplay().enableGlobalCooldown(); 
		}
		
		if (input.debugAddMonster && !Game.getGameplay().onGlobalCooldown) {
			if (Gamestats.monstersAlive > 0) Game.getGameplay().spawnMonster();
			else Game.getGameplay().newMonsterWave();
		}
		
		if (input.debugForceNewWave && !Game.getGameplay().onGlobalCooldown) {
			Game.getGameplay().newMonsterWave();
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
