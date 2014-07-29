package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.input.Keyboard;

public class Player extends Entity {
	private Keyboard input;
	private boolean canUseSkills;
	
	public Player(Keyboard input, Entity defaultTarget) {
		this.id = "Player";
		
		this.x = 0;
		this.y = 0;
		
		this.input = input;
		this.canUseSkills = false;
		
		this.damage = new int[] {5, 12};
		this.health = 100;
		this.isAlive = true;
		this.mana = 25;
		this.xp = 0;
		this.level = 1;
		this.actionPoints = 1;
		this.defaultActionPoints = actionPoints;
		
		
		this.defaultTarget = defaultTarget;
	}
	
	public void update() {
		
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
	}
	
	public void render() {	
	}
	
	public boolean getCanUseSkills() {
		return canUseSkills;
	}
	
}
