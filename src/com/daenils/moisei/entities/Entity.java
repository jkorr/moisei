package com.daenils.moisei.entities;

import java.util.Random;

import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;
import com.daenils.moisei.graphics.Stage;

public class Entity {
	protected String name;
	protected int id;
	protected int x, y;
	protected int width, height;
	protected Sprite sprite;
	protected Stage stage;
	
	protected int health, mana, xp;
	protected boolean isAlive;
	protected byte actionPoints, defaultActionPoints;
	protected byte lastActionPoints; // for "flagging" actions via comparison of these values
	protected byte level;
	protected  int[] damage; // temporary way to add a damage range, before implementing a weapon system
	protected int hitDamage;
	protected String lastAttacker;
	
	protected boolean isWaiting;
	
	protected Entity defaultTarget;
	protected boolean testFlagMonsterAdded;
	protected int targetCycled = 0;

	
	public void update() {
	}
	
	public void render(Screen screen) {
	}
	
	protected void basicAttack(Entity e1, Entity e2) {
		if (e2.health > 0) {
			hitDamage = getRandomHitDamage(e1);
//			System.out.println(hitDamage);
			e2.health -= hitDamage;
			e2.lastAttacker = e1.name;
			e1.lastActionPoints = e1.actionPoints;
			e1.actionPoints--;
			System.out.print("" + e1.name + " --> " + e2.name + " (" + hitDamage + " damage) | ");
//			System.out.println(e1 + " hits " + e2 + " for " + hitDamage + " damage.");
//			System.out.println(e2 + " has " + e2.health + " hp left." );
			stillAlive(e2, e1);
		}
	}
	
	protected void stillAlive(Entity checked, Entity attacker) {
		if (checked.health < 1) death(checked, attacker);
	}
	
	protected void death(Entity checked, Entity attacker) {
		checked.isAlive = false;
		
		if (checked instanceof Monster) {
			Game.getGameplay().setSpawnSlotFilled(((Monster) checked).getSpawnSlot(), false);
			Monster.addDeathCount();
			((Player) attacker).newCycledTarget();
		}
 
		System.out.print(checked.name + " died.");
		giveXP(attacker);
	}
	
	private void giveXP(Entity e) {
		e.xp += 10;
	}

	public void setDefaultTarget(Entity e) {
		this.defaultTarget = e;	
	}
	
	protected int getRandomHitDamage(Entity e) {
		Random rand = new Random();
	    int r = rand.nextInt((e.damage[1] - e.damage[0]) + 1) + e.damage[0];		
		return r;
	}
	
	protected void resetActionPoints(Entity e) {
		e.actionPoints = defaultActionPoints;
	}
	
	protected void resetMonsterWait() {
	
	}
	
	// GETTERS
	protected int getHealth() {
		return health;
	}
	
	protected int getMana() {
		return mana;
	}
	
	protected int getXP() {
		return xp;
	}
	
	public boolean isAlive() {
		return isAlive;
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public Entity getTarget() {
		return this;
	}
	
	public int getTargetCycled() {
		return targetCycled;
	}
	
	// SETTERS
	protected void setWait(Boolean b) {
		isWaiting = b;
	}
	
	protected void setTarget(Entity e) {
		this.defaultTarget = e.getTarget();
		System.out.print(" | Targeted " + e.name + " | ");
	}
	
	protected void cycleTarget(Entity e) {
		setTarget(e);
		targetCycled++;
	}
	
	public void init(Stage s) {
		this.stage = s;
	}
	
	
}
