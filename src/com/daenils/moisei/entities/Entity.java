package com.daenils.moisei.entities;

import java.util.Random;

import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;

public class Entity {
	protected int x, y;
	protected int width, height;
	protected Sprite sprite;
	
	protected int health, mana, xp;
	protected byte actionPoints, defaultActionPoints;
	protected byte level;
	protected  int[] damage; // temporary way to add a damage range, before implementing a weapon system
	protected int hitDamage;
	
	protected Entity defaultTarget;
	
	public void update() {
	}
	
	public void render(Screen screen) {
	}
	
	protected void basicAttack(Entity e1, Entity e2) {
		hitDamage = getRandomHitDamage(e1);
//		System.out.println(hitDamage);
		e2.health -= hitDamage;
		e1.actionPoints--;
		System.out.println(e1 + " hits " + e2 + " for " + hitDamage + " damage.");
		System.out.println(e2 + " has " + e2.health + " hp left." );
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
}
