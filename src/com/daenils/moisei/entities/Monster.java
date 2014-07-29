package com.daenils.moisei.entities;

import java.util.Random;

import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;

public class Monster extends Entity {
	private byte spawnSlot;
	private int[] XY = new int[2];
	private int[] randomWait = new int[2];
	private boolean gotRandom;
	private int r;
	
	public int[] spawnSlot1 = new int[] {100, 250};
	public int[] spawnSlot2 = new int[] {335, 320};
	public int[] spawnSlot3 = new int[] {580, 290};
	public int[] spawnSlot4 = new int[] {810, 250};
	public int[] spawnSlot5 = new int[] {1050, 210};
	
	public Monster(byte spawnSlot, Entity defaultTarget) {
		this.id = "DemoMonster";
		this.spawnSlot = spawnSlot;
		
		setXY(spawnSlot);
		this.x = XY[0];
		this.y = XY[1];
		
		this.sprite = Sprite.monster_demo3;
		
		this.health = 60;
		this.isAlive = true;
		this.mana = 0;
		this.level = 1;
		this.actionPoints = 1;
		this.defaultActionPoints = actionPoints;
		this.damage = new int[] {8, 15};
		
		this.defaultTarget = defaultTarget;
		this.isWaiting = true;
		this.randomWait = new int[] {1, 4};
		this.gotRandom = false;
		this.r = newRandomWait();
		
		}
	
	public void update() {
	
//		System.out.println("Waiting? " + isWaiting);
		isWaiting = true;
//		System.out.println(isWaiting);
		
		
		if (Gamestats.isMonsterTurn && isAlive && (actionPoints > 0)) {
			monsterWait(1.5);
			if (!isWaiting) {
				while (actionPoints > 0) basicAttack(this, defaultTarget);
			}			
		}
		
		if (Gamestats.isMonsterTurn && isAlive && isWaiting && (actionPoints == 0)) {
			monsterWait(r);
			if (!isWaiting) {
				Game.getGameplay().endTurn(this);
			}			
		}
	
	}
	
	public void render(Screen screen) {
		screen.renderSprite(x, y, sprite, 1);
	}
	
	private int[] setXY(int spawnSlot) {
		if (spawnSlot < 1 || spawnSlot > 5) { XY[0] = 100; XY[1] = 250; }
		else if (spawnSlot == 1)  { XY[0] = spawnSlot1[0]; XY[1] = spawnSlot1[1]; }
		else if (spawnSlot == 2)  { XY[0] = spawnSlot2[0]; XY[1] = spawnSlot2[1]; }
		else if (spawnSlot == 3)  { XY[0] = spawnSlot3[0]; XY[1] = spawnSlot3[1]; }
		else if (spawnSlot == 4)  { XY[0] = spawnSlot4[0]; XY[1] = spawnSlot4[1]; }
		else if (spawnSlot == 5)  { XY[0] = spawnSlot5[0]; XY[1] = spawnSlot5[1]; }
		
		return XY;
	}
	
	public byte getSpawnSlot() {
		return spawnSlot;
	}	
	
	public int getRandomWait() {
		return r;
	}
	
	public int newRandomWait() {
		Random rand = new Random();
		int r = rand.nextInt((randomWait[1] - randomWait[0]) + 1) + randomWait[0];
//		System.out.print("R:" + r + " | ");
		return r;
	}
	
	// WAIT STUFF
	protected void monsterWait(double n) {
		beginMonsterWait();
//		System.out.println(startWaitTimer);
		if (Game.getGameplay().getDeltaWaitTime() >= n) endWait(this);
		
	}
	
	protected void resetMonsterWait() {
		Game.getGameplay().setStartWaitTimer(System.currentTimeMillis());
		Game.getGameplay().setDeltaWaitTime(0);
		r = newRandomWait();
	}
	
	protected void beginMonsterWait() {
		if (!Game.getGameplay().getIsWaitingOn()) {
			resetMonsterWait();
			Game.getGameplay().setIsWaitingOn(true);
		}
				
	}
	
	protected void endWait(Monster m) {
		m.resetMonsterWait();
		m.isWaiting = false;
		Game.getGameplay().setIsWaitingOn(false);
	}
}

