package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;

public class Monster extends Entity {
	private int spawnSlot;
	
	public Monster(int x, int y, Entity defaultTarget) {
		this.x = x;
		this.y = y;
		
		this.sprite = Sprite.monster_demo;
		
		this.health = 60;
		this.mana = 0;
		this.level = 1;
		this.actionPoints = 1;
		this.defaultActionPoints = actionPoints;
		this.damage = new int[] {2, 4};
		
		this.defaultTarget = defaultTarget;
	}
	
	public void update() {
		if (Game.getGameplay().getIsMonsterTurn()) {
			while (actionPoints > 0) basicAttack(this, defaultTarget);
			Game.getGameplay().endTurn(this);
		}
	}
	
	public void render(Screen screen) {
		screen.renderSprite(x, y, sprite);
	}
	
}

