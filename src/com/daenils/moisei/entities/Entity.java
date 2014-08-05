package com.daenils.moisei.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.daenils.moisei.Game;
import com.daenils.moisei.entities.equipments.Ability;
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
	protected byte abilityCount;
	protected boolean isAlive;
	protected boolean needsRemove;
	protected byte actionPoints, defaultActionPoints;
	protected byte lastActionPoints; // for "flagging" actions via comparison of these values
	protected byte level;
	protected  int[] damage; // temporary way to add a damage range, before implementing a weapon system
	protected int hitDamage;
	protected String lastAttacker;
	protected int lastHealth;
	
	protected boolean isWaiting;
	
	protected boolean isStunned;
	
	protected Entity defaultTarget;
	protected boolean testFlagMonsterAdded;
	protected int targetCycled = 0;

	protected List<Ability> abilities = new ArrayList<Ability>();
	
	protected int dotValue;
	protected int dotTurnCount;
	protected boolean oneDotPerTurn;
	
	public void update() {
	}
	
	public void render(Screen screen) {
	}
	
	protected void basicAttack(Entity e1, Entity e2) {
		if (e2.health > 0) {
			hitDamage = getRandomHitDamage(e1);
//			System.out.println(hitDamage);
			dealDamage(e1, e2, hitDamage);
			compensateForCosts(e1, e2);
			
//			System.out.println(e1 + " hits " + e2 + " for " + hitDamage + " damage.");
//			System.out.println(e2 + " has " + e2.health + " hp left." );
		}
	}
	
	protected void dealDamage(Entity e1, Entity e2, int d) {
		dealDamage(e1, e2, null, d);
	}
	
	protected void dealDamage(Entity e1, Entity e2, Ability a, int d) {
		// deal with this double copied code, for some reason (a != null) won't work
		if(a == null) {
			decreaseHealth(e1, e2, d);
			stillAlive(e2, e1);
		}
		
		else {
			decreaseHealth(e1, e2, d);
			stillAlive(e2, e1);
		}
	}
	
	protected boolean affordToUseAbility(Entity e1, Ability a) {
		return a.getAPcost() <= e1.actionPoints && a.getMPcost() <= e1.mana;
	}
	
	protected void doHealing(Entity e1, Entity e2, Ability a, int h) {
		// it has targeting because in the future it would be nice to have monster heal other monster
			increaseHealth(e1, e2, h);
	}
	
	protected void doUtility(Entity e1, Entity e2, Ability a) {
			if (a.getIsStun()) abilityStun(e1, e2, a);
	}
	
	private void abilityStun(Entity e1, Entity e2, Ability a) {
		// currently this only works for the next turn, cannot just extend it for 2 or more
		e2.actionPoints -= e2.actionPoints * ((double) a.getUtilityValue() / 100.0);
		e2.isStunned = true;
	}

	protected void decreaseHealth(Entity e1, Entity e2, int d) {
		e2.lastHealth = e2.health;
		e2.health -= d;
		e2.lastAttacker = e1.name;
		System.out.print("" + e1.name + " --> " + e2.name + " (" + d + " damage) | ");
		}
	
	protected void increaseHealth(Entity e1, Entity e2, int h) {
		e2.lastHealth = e2.health;
		e2.health += h;
		System.out.print("" + e1.name + " heals " + e2.name + " (" + h + " health) | ");
		}
	
	protected void compensateForCosts(Entity e1, Entity e2) {
		// currently has both entities, but why? check it out, is it really necessary?
		compensateForCosts(e1, e2, null);
		}
	
	protected void compensateForCosts(Entity e1, Entity e2, Ability a) {
		int n = 0;
		if (a == null) n = 1;
		else n = a.getAPcost();
		
		e1.lastActionPoints = e1.actionPoints;
		e1.actionPoints -= n;
		
		if (a != null && a.getMPcost() > 0) e1.mana -= a.getMPcost();
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
	
	protected void useAbility(Entity e1, Ability a) {
		/* 
		 * consider calling this regardless the type: just execute EVERYTHING, since a heal will have
		 * damageValue 0 anyways
		 * 
		 * also you might want to add "Entity target" to the method to pass that along
		 *
		 * 
		 * */
		
		if (affordToUseAbility(e1, a)) {
			if (a.getHealValue() > 0) doHealing(this, this, a, a.getHealValue());
			if (a.getDamageValue() > 0) dealDamage(this, defaultTarget, a, a.getDamageValue());
			if (a.getUtilityValue() > 0) doUtility(this, defaultTarget, a);
			
			if (a.getDotValue() > 0) dealDot(this, defaultTarget, a);
			
			compensateForCosts(this, this, a);
			System.out.print(" | Ability (" + a.getName() + ") used.");
			a.setLastUsed(Gamestats.turnCount);
		}	
	}
	
	protected void dealDot(Entity e1, Entity e2, Ability a) {
		e1.dotValue = a.getDotValue();
		e1.dotTurnCount = a.getTurnCount();
		e1.oneDotPerTurn = true;
	}
	
	protected void dealDots() {
		// this is the actual method that should be called from Player/Monster
		if (dotValue > 0 && dotTurnCount > 0 && defaultTarget != null && !oneDotPerTurn) {
			dealDamage(this, defaultTarget, dotValue);
			System.out.println("Dealt dot damage of " + dotValue + " for " + dotTurnCount + " turns");
			dotTurnCount--;
			oneDotPerTurn = true;
			}
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
	
	// ABILITIES
	protected void addAbility(Ability a) {
		// this is a vanilla method, DO NOT use this directly, use unlockAbility() instead!
		abilities.add(a);
	}
	
	protected void removeAbility(int n) {
		// this is a vanilla method, DO NOT use this directly, use lockAbility() instead!
		abilities.remove(n);
	}
	
	protected void removeLastAbility() {
		if (abilities.size() > 0) lockAbility(this, abilities.size() - 1);
	}
	
	protected void unlockAbility(Entity e, int id) {
		if (e.abilities.size() < e.abilityCount) {
			Ability abi = new Ability(id, e);
			this.addAbility(abi);
			System.out.println(e.name + "'s " + abi.getName() + " unlocked.");
		}
	}
	
	protected void lockAbility(Entity e, int n) {
		System.out.println(e.name + "'s " + abilities.get(n).getName() + " locked.");
		removeAbility(n);
	}
	
	
	// GETTERS
	public Entity getEntity() {
		return this;
	}
	
	public int getHealth() {
		return health;
	}
	
	protected int getMana() {
		return mana;
	}
	
	protected int getXP() {
		return xp;
	}
	
	public boolean getIsAlive() {
		return isAlive;
	}
	
	public boolean getNeedsRemove() {
		return needsRemove;
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
	
	// ADDERS
	public void addHealth(int n) {
		this.health += n;
	}
	
	public void addMana(int n) {
		this.mana += n;
	}
	
	public void addDefaultActionPoints(int n) {
		this.defaultActionPoints += n;
	}
}
