package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.input.Keyboard;

public class Player extends Entity {
	private Keyboard input;
	private boolean canUseSkills;
	
	public Player(Keyboard input, Entity defaultTarget) {
		this.x = 0;
		this.y = 0;
		
		this.input = input;
		this.canUseSkills = false;
		
		this.health = 100;
		this.mana = 25;
		this.xp = 0;
		this.level = 1;
		this.actionPoints = 1;
		this.defaultActionPoints = actionPoints;
		this.damage = new int[] {1, 5};
		
		this.defaultTarget = defaultTarget;
	}
	
	public void update() {
		
		// Check if it's the player turn and no cooldown:
		if (Game.getGameplay().getIsPlayerTurn() && !Game.getGameplay().onGlobalCooldown && actionPoints > 0)
			canUseSkills = true;
		else canUseSkills = false;
		
		// KEY BINDINGS
		// BASIC ATTACK
		if (input.playerBasicAttack && canUseSkills) {
			basicAttack(this, defaultTarget);
			Game.getGameplay().enableGlobalCooldown();
		}
		
		
		if (input.playerQ && canUseSkills) {
			System.out.println("Powerfull Fireball casted!");
			Game.getGameplay().enableGlobalCooldown();
		}
		
		if (input.playerEndTurn && !Game.getGameplay().onGlobalCooldown) {
			// System.out.println("!!!");
			Game.getGameplay().endTurn(this);
			// every keybind available to the player has to contain this line:
			Game.getGameplay().enableGlobalCooldown(); 
		}
	}
	
	public void render() {
		
	}
	
}
