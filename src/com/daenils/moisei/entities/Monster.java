package com.daenils.moisei.entities;

import java.util.Random;

import com.daenils.moisei.Game;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;
import com.daenils.moisei.graphics.Stage;

public class Monster extends Entity {
	private int spawnSlot;
	private int[] XY = new int[2];
	private int[] randomWait = new int[2];
	private int r;
	
	public int[] spawnSlot1;
	public int[] spawnSlot2;
	public int[] spawnSlot3;
	public int[] spawnSlot4;
	public int[] spawnSlot5;
	
//	private Ability[] monsterAbility = new Ability[2]; // fixed for two now, later probably it will depend on type of monster
	
	private boolean canUseSkills;
	private boolean forceRemoved;
	
	private static int monstersAttacked;
	private static int deathCount;
		
	public Monster(int spawnSlot, Entity defaultTarget) {
		updateSpawnSlots();
		
		this.spawnSlot = spawnSlot;
		Game.getGameplay().spawnSlotFilled[spawnSlot - 1] = true;
		
		this.id = spawnSlot;
		this.name = "DemoM0nster[" + (id) + "]";
		this.type = "mon_default";
		
		
		setXY(spawnSlot);
		this.x = XY[0];
		this.y = XY[1];
		
		this.sprite = Sprite.monster_demo4;
		
		this.health = 30;
		this.shield = 0;
		this.isAlive = true;
		this.mana = 10;
		this.level = 1;
		this.actionPoints = 1;
		this.defaultActionPoints = actionPoints;
		this.damage = new int[] {2, 5};
//		this.monsterAbility[0] = new Ability(1, this);
//		this.monsterAbility[1] = new Ability(3, this);
		
		this.currentTarget = defaultTarget;
		this.isWaiting = true;
		this.randomWait = new int[] {1, 3};
		this.r = newRandomWait();
		
		this.stage = Stage.getStage();
		
		this.needsRemove = false;
		this.deathCount = 0;
		this.abilityCount = 4;
		
		initAbilities();
		
		}

	private void updateSpawnSlots() {
		if (Gamestats.monsterCount == 1) {
			spawnSlot1 = new int[] {580, 290};
			spawnSlot2 = new int[] {335, 320};
		}
		else if (Gamestats.monsterCount == 2) { 
			spawnSlot1 = new int[] {335, 320};
			spawnSlot2 = new int[] {580, 290};
			spawnSlot3 = new int[] {810, 250};
		}
		else if (Gamestats.monsterCount == 3) {
			spawnSlot1 = new int[] {335, 320};
			spawnSlot2 = new int[] {580, 290};
			spawnSlot3 = new int[] {810, 250};
			spawnSlot4 = new int[] {100, 250};
		}
		else if (Gamestats.monsterCount == 4) {
			spawnSlot1 = new int[] {100, 250};
			spawnSlot2 = new int[] {335, 320};
			spawnSlot3 = new int[] {580, 290};
			spawnSlot4 = new int[] {810, 250};
			spawnSlot5 = new int[] {1050, 210};
		}
		else if (Gamestats.monsterCount == 5) {
			spawnSlot1 = new int[] {100, 250};
			spawnSlot2 = new int[] {335, 320};
			spawnSlot3 = new int[] {580, 290};
			spawnSlot4 = new int[] {810, 250};
			spawnSlot5 = new int[] {1050, 210};
		}
		else {
		spawnSlot1 = new int[] {100, 250};
		spawnSlot2 = new int[] {335, 320};
		spawnSlot3 = new int[] {580, 290};
		spawnSlot4 = new int[] {810, 250};
		spawnSlot5 = new int[] {1050, 210};
		}
		
	}
	
	public Monster() {
		// DUMMY MONSTER
	
		this.spawnSlot = -1;
//		Game.getGameplay().spawnSlotFilled[spawnSlot - 1] = true;
		
		this.name = "DemoMonster[" + (spawnSlot) + "]";
		
		this.x = 50;
		this.y = 50;
		
		this.sprite = Sprite.monster_demo3;
		
		this.health = 1;
		this.isAlive = true;
		this.mana = 0;
		this.level = 0;
		this.actionPoints = 0;
		this.defaultActionPoints = actionPoints;
		this.damage = new int[] {0, 0};
		
		this.isWaiting = true;
		this.randomWait = new int[] {0, 0};
		this.r = newRandomWait();
		
		this.stage = Stage.getStage();
		
		}

	public void initAbilities() {
		unlockAbility(this, 1);
		unlockAbility(this, 3);
		unlockAbility(this, 4);
		unlockAbility(this, 8);
	}
	
	public void updateXY() {
		this.x = XY[0];
		this.y = XY[1];
	}
	
	public void update() {
		applyDots();
		
		// the following 3 lines make the dynamic monster changing possible:
		updateSpawnSlots();
		setXY(spawnSlot);
		updateXY();
	
//		System.out.println(id);
//		System.out.println("Waiting? " + isWaiting);
		isWaiting = true;
//		System.out.println(isWaiting);
		
		//if dead 0 ap since checkIfDone() looks for ap
		// extend this into a method for additional death-related stuff
		// e.g. new sprite for dead ppl
		if (!isAlive) { 
			actionPoints = 0;
			sprite = Sprite.monster_demo;
		}
		
		// use heal
		aiBehaviorUseHeal();
		
		// use stun
	//	aiBehaviorUseStun();
		
		// use dots
	//	aiBehaviorUseDots();
		
		// use shield
		aiBehaviorUseShield();	
		
		// basic attack
		if (checkCanUseSkills() && currentTarget.isAlive) {
			monsterWait(r);
		if (!isWaiting) {
				while (actionPoints > 0) {
					basicAttack(this, currentTarget);
					monstersAttacked++;
				}
			}			
		}
		
		// monster entity removal code 
		if ((health <= 0 && Game.getGameplay().getCurrentTurn() == 1) || forceRemoved) needsRemove = true;
		else needsRemove = false;
	
//		System.out.println("MONSTER UPDATE");
		
	}

	private void aiBehaviorUseStun() {

		if (checkCanUseSkills() && (currentTarget.getMana() < 20 && !currentTarget.isStunned) && currentTarget.isAlive) {
			monsterWait(r);
			if (!isWaiting) {
				useAbility(this, abilities.get(1));
			}
		}
	}

	private void aiBehaviorUseHeal() {
		if (checkCanUseSkills() && this.health < 20) {
			monsterWait(r);
			if (!isWaiting) {
				useAbility(this, abilities.get(0));
			}
		}
	}
	
	private void aiBehaviorUseDots() {
		if (checkCanUseSkills() && currentTarget.health < 150) {
			monsterWait(r);
			if (!isWaiting) {
				useAbility(this, abilities.get(2));
			}
		}
	}
	
	private void aiBehaviorUseShield() {
		if (checkCanUseSkills()) {
			monsterWait(r);
			if (!isWaiting) {
				useAbility(this, abilities.get(3));
			}
		}
	}
	
	public void render(Screen screen) {
		screen.renderSprite(x, y, sprite, 1);
	}
	
	private boolean checkCanUseSkills() {
		if (Gamestats.isMonsterTurn && isAlive && actionPoints > 0) return canUseSkills = true;
		else return canUseSkills = false;
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
	
	public int getSpawnSlot() {
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
	
	protected static void resetMonstersAttacked() {
		monstersAttacked = 0;
	}

	public static int getMonstersAttacked() {
		return monstersAttacked;
	}
	
	public static void addDeathCount() {
		deathCount++;
	}
	
	public static int getDeathCount() {
		return deathCount;
	}
	
	public void forceRemove(Entity e1, int n) {
		if (e1 instanceof Monster) {
			if (n == 0) ((Monster) e1).forceRemoved = true; // hard remove
			else if (n == 1) {
				if (e1.getHealth() <= 0) ((Monster) e1).forceRemoved = true; // soft remove
			}
		}
		
	}
}

