package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.Stage;
import com.daenils.moisei.input.Keyboard;

public class Player extends Entity {
	private Keyboard input;
	private boolean canUseSkills;
	protected boolean neverCycled;
	
	public Player(Keyboard input, Entity defaultTarget) {
		this.name = "Player";
		this.id = -1;
		
		this.x = 0;
		this.y = 0;
		
		this.input = input;
		this.canUseSkills = false;
		
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
	
	public void update() {
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
		
		
		if (input.playerQ && canUseSkills) {
			System.out.print("Player casts a Fireball! | ");
			Game.getGameplay().enableGlobalCooldown();
		}
		
		if (input.playerEndTurn && !Game.getGameplay().onGlobalCooldown && Game.getGameplay().getIsPlayerTurn()) {
			// System.out.println("!!!");
			Game.getGameplay().endTurn(this);
			// every keybind available to the player has to contain this line:
			Game.getGameplay().enableGlobalCooldown(); 
		}
		
		if (input.debugAddMonster && !Game.getGameplay().onGlobalCooldown) {
			Game.getGameplay().spawnMonster();
		}

		inputTargeting();

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

	public void render() {	
	}
	
	public boolean getCanUseSkills() {
		return canUseSkills;
	}
	
	
	
}
