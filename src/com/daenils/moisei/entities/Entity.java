package com.daenils.moisei.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.Stage;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.entities.equipments.Buff;
import com.daenils.moisei.entities.equipments.Equipment;
import com.daenils.moisei.entities.equipments.Weapon;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;

public class Entity {
	protected String name;
	protected String type;
	protected int id;
	protected int localId;
	protected int x, y;
	protected int width, height;
	protected Sprite sprite;
	protected Stage stage;
	
	protected List<Buff> buffs = new ArrayList<Buff>();
	
	protected int tick = 0;
	
	protected int baseHealth, baseMana;
	protected int[] baseDamage;
	protected double[] damageDbl = new double[2]; // for precision (needed for leveling)
	protected int maxHealth, maxMana;
	protected int health, mana, shield, xp;
	protected int totalXp;
	protected int lastHealth;
	protected String description;
	protected byte abilityCount;
	protected byte weaponCount;
	
	protected boolean isAlive;
	protected boolean needsRemove;
	protected boolean hasSpawned = false;
	
	protected byte actionPoints, maxActionPoints;
	protected byte lastActionPoints; // for "flagging" actions via comparison of these values
	protected int spellPower;
	protected byte level;
	final protected byte LEVELCAP = 3; // TODO: implement it!
	protected  int[] damage; // temporary way to add a damage range, before implementing a weapon system
	protected int hitDamage;
	protected String lastAttacker;
	protected int lastHitReceived = (lastHealth - health) * -1; // temporarily fix the value here
	protected int lastHitChance;
	protected int damageReduction;
	
	public boolean isGettingDamage;
	
	protected Weapon weapon;
	
	protected boolean isWaiting;
	
	protected boolean isStunned;
	protected boolean hasDominantEffect;
	protected boolean isWindBuffed;
	
	protected Entity currentTarget;
	protected boolean testFlagMonsterAdded;
	protected int targetCycled = 0;

	protected List<Ability> abilities = new ArrayList<Ability>();
	protected List<Weapon> weapons = new ArrayList<Weapon>();
	
	protected Map<Integer, Integer> mapWeapons = new HashMap<Integer, Integer>();
	
	// DAMAGE FLASH TIMER
    protected long flashDuration;
    long flashStart;
	private long flashEnd;
	
	// LEVELS AND XP
	protected int xpNeeded; // the amount of xp needed for the next level
	protected int xpGained;
	
	protected byte pHealth;
	protected byte pMana;
	protected byte pAP;
	protected byte pXP;
	
	protected char[] currentWord = new char[10 * 2]; // becuase player's currentWord has to store color codes as well
	protected int[] currentWordColors = new int[10 * 2];
	protected int currentWordLength = 0;
	
	
	// monitor for removal of buffs
	public void update() {
		for (int i = 0; i < buffs.size(); i++)
			buffs.get(i).update();
			
			
		remove();
	}
	
	public void render(Screen screen) {
	}
	
	protected void basicAttack (Entity e1, Entity e2) {
		basicAttack(e1, e2, null);
	}
	
	protected void basicAttack(Entity e1, Entity e2, Weapon w) {
		if (w == null) {
			if (e2.health > 0) {
				if (e1.type == "player") {
				hitDamage = getRandomHitDamage(e1);
//				System.out.println(hitDamage);
				} else {
					hitDamage = ((Monster) e1).currentWordLength;
				}
				
				// check for dmg reduction
				if (e2.damageReduction > 0) hitDamage *= ((100 - e2.damageReduction) / 100.0);
				
				dealDamage(e1, e2, hitDamage);
				compensateForCosts(e1, e2);
				
//				System.out.println(e1 + " hits " + e2 + " for " + hitDamage + " damage.");
//				System.out.println(e2 + " has " + e2.health + " hp left." );
			}	
		}
		else {
			if (w.getWeaponCharges() > 0) {
				doWeaponAbility(w, e2);
			}
			boolean doHit = false;
			if (w.getHitChance() < 100) doHit = isHitSuccessful(w.getHitChance());
			if (doHit || w.getHitChance() >= 100) {
				hitDamage = getRandomHitDamage(e1, w);
				dealDamage(e1, e2, w, hitDamage, false);
				compensateForCosts(e1, e2, w);
			}
			else {
				compensateForCosts(e1, e2, w);
				CombatLog.println(e1.name + " missed.");
			}
		}
		
		
	}
	
	// LEGACY METHOD (not muted by default)
	protected void dealDamage(Entity e1, Entity e2, int d) {
		if (e1 instanceof Player) Game.getGameplay().displayDamage(d, true);
		else Game.getGameplay().displayDamage(d, false);
		dealDamage(e1, e2, null, d, false);
		e2.setGettinDamage(true);
	}
	
	private void setGettinDamage(boolean b) {
		this.isGettingDamage = true;
		this.flashStart = System.nanoTime();
		this.flashDuration = 75000000;
	}

	// METHOD FOR NON-ABILITIES (mutable by the 4th argument)
	protected void dealDamage(Entity e1, Entity e2, int d, Boolean mute) {
		dealDamage(e1, e2, null, d, mute);
	}
	
	// METHOD FOR ABILITIES (with Ability as 3rd argument and unmuted by default)
	public void dealDamage(Entity e1, Entity e2, Equipment a, int d) {
		dealDamage(e1, e2, a, d, false);
	}
	
	// GENERAL METHOD
	protected void dealDamage(Entity e1, Entity e2, Equipment a, int d, Boolean mute) {
		// deal with this double copied code, for some reason (a != null) won't work
		if(a == null) {
			decreaseHealth(e1, e2, d);
			stillAlive(e2, e1);
			if (!mute) CombatLog.println("" + e1.name + " --> " + e2.name + " (" + d + " damage)");
		}
		
		else {
			decreaseHealth(e1, e2, d);
			stillAlive(e2, e1);
			if (!mute) CombatLog.println("" + e1.name + " hits " + e2.name + " with " + a.getName() + " (" + d + " damage)");
		}
	}
	
//	protected boolean affordToUseAbility(Entity e1, Ability a) {
//		return a.getAPcost() <= e1.actionPoints && (a.getMPcost() * e1.spellPower) <= e1.mana;
//	}
	
	public void doHealing(Entity e1, Entity e2, Equipment a, int h) {
		// it has targeting because in the future it would be nice to have monster heal other monster
		increaseHealth(e1, e2, h);
	}
	
	protected void doUtility(Entity e1, Entity e2, Equipment a) {
			if (a.getIsStun()) abilityStun(e1, e2, a);
//			if (a.getIsDrainMP()) abilityDrainMP(e1, e2, a);
			if (a.getIsShield()) abilityShield(e1, e1, a); // e1 twice = currently self-target only
	}

	private void abilityStun(Entity e1, Entity e2, Equipment a) {
		// currently this only works for the next turn, cannot just extend it for 2 or more
		e2.actionPoints -= e2.actionPoints * ((double) a.getUtilityValue() / 100.0);
		e2.isStunned = true;
		CombatLog.print(e1.name + " stuns " + e2.name + " for the next turn.");
	}
	
	protected void letterStun(Entity e1, Entity e2) {
		// currently this only works for the next turn, cannot just extend it for 2 or more
		e2.actionPoints -= e2.actionPoints;
		e2.isStunned = true;
		CombatLog.print(e1.name + " stuns " + e2.name + " for the next turn.");
	}
	
	protected void letterDominantEffect(Entity e1) {
		this.hasDominantEffect = true;
	}
	
	private void abilityShield(Entity e1, Entity e2, Equipment a) {
		increaseShield(e1, e2, a.getUtilityValue());
	}
	
	protected void decreaseMana(Entity e1, Entity e2, int amount) {
		e2.mana -= amount;
		if (e2.mana < 0) e2.mana = 0; // failsafe line against negative mana effect
		e2.lastAttacker = e1.name;
		}
	
	protected void increaseMana(Entity e1, Entity e2, int amount) {
		if (e2.maxMana >= e2.mana + amount) e2.mana += amount;
		else e2.mana = e2.maxMana;
		}
	
	public void decreaseHealth(Entity e1, Entity e2, int d) {
		if (e2.shield > 0 && d < e2.shield)
			e2.shield -= d;
		else if (e2.shield > 0 && d >= e2.shield) {
			e2.shield -= d;
			d = e2.shield * -1;
			e2.shield = 0;
			
			// this line copy-paste is not nice, pls do something about it later!
			e2.lastHealth = e2.health;
			e2.health -= d;
			e2.lastAttacker = e1.name;
		} else {
			e2.lastHealth = e2.health;
			e2.health -= d;
			e2.lastAttacker = e1.name;
		}
	}
	

	
	protected void increaseHealth(Entity e1, Entity e2, int h) {
		increaseHealth(e1, e2, h, false);
		}
	
	protected void increaseHealth(Entity e1, Entity e2, int h, boolean mute) {
		if (e2.maxHealth >= e2.health + h) {
			e2.lastHealth = e2.health;
			e2.health += h;
		}
		else {
			e2.lastHealth = e2.health;
			e2.health = e2.maxHealth;
		}
		if (!mute)
			CombatLog.println("" + e1.name + " heals " + e2.name + " (" + h
					+ " health)");
	}
	
	protected void decreaseShield(Entity e1, Entity e2, int s) {
		e2.shield -= s;
	}
	
	protected void increaseShield(Entity e1, Entity e2, int s) {
		e2.shield += s;
		CombatLog.print("" + e1.name + " shields " + e2.name + " for " + s + ".");
	}
	
	// METHOD FOR NON-ABILITIES
	protected void compensateForCosts(Entity e1, Entity e2) {
		// currently has both entities, but why? check it out, is it really necessary?
		compensateForCosts(e1, e2, null);
		}
	
	// METHOD FOR WEAPONS & ABILITIES
	protected void compensateForCosts(Entity e1, Entity e2, Equipment a) {
		int n = 0;
		if (a == null) n = 1;
//		else n = a.getAPcost();

		e1.lastActionPoints = e1.actionPoints;
		e1.actionPoints -= n;
		
		if (e1 instanceof Player) {
			if (a != null && a.getEPcost() > 0) {
				((Player) e1).removeElementalPower(a.getElementType(), a.getEPcost()); 
			} 
		}

		if (a != null && a.getMPcost() > 0)	e1.mana -= a.getMPcost() * e1.spellPower;
		if (a != null && (a instanceof Weapon) && ((Weapon) a).getWeaponCharges() > 0) {
			((Weapon) a).decreaseWeaponCharges();
		}
	}
	
	protected void stillAlive(Entity checked, Entity attacker) {
		if (checked.health < 1) death(checked, attacker);
	}
	
	protected void death(Entity checked, Entity attacker) {
		int buffXp = 0;
		int buffGold = (10 * attacker.level) / 2;
		if (attacker instanceof Player) buffXp = ((Player) attacker).getXpGained();
		checked.isAlive = false;
		
		if (checked instanceof Monster) {
			Monster.addDeathCount();
			((Player) attacker).newCycledTarget();
		}
		
		if (checked instanceof Player) {
			Game.getGameplay().submitStats_endWave();
			FileManager.saveStatisticsFile();
			FileManager.saveCombatLogFile();
		}
		
		if (attacker instanceof Player) {
			((Player) this).addGold(buffGold);
			giveXP(attacker, buffXp);
			((Player) this).checkLevelUp();
			CombatLog.println(checked.name + " died. " + attacker.name + " receives " + buffXp + " XP and " + buffGold + " gold for the kill.");
		}
	}
	
	protected void useAbility(Entity e1, Ability a) {
		if (affordToUseAbility(e1, a)) {
			
			// BUFFS-DEBUFFS
			if (a.getAbilityCat() == 1 || a.getAbilityCat() == 2) {
			//	System.out.println("BUFF / DEBUFF");
				Buff b = new Buff(a, e1, e1.currentTarget, 0);
				if (a.getAbilityCat() == 1) e1.add(b);
				else e1.currentTarget.add(b);
				compensateForCosts(e1, e1.currentTarget, a);
			}
			
			// INSTANTS
			else if (a.getAbilityCat() == 0) {
				// adv.fire
				if (a.getAbilityType() == 8) {
					dealDamage(this, this.getCurrentTarget(), a, calculateDamage(this, this.getCurrentTarget(), a));
					compensateForCosts(e1, e1.currentTarget, a);
				}
				
				// adv.water && ult.water
				if (a.getAbilityType() == 9) {
					if (a.getValueType() == 2) doHealing(this, this, a, calculateDamage(this, this, a));
					if (a.getValueType() == 1) // TODO: finish it
					compensateForCosts(e1, e1.currentTarget, a);
				}
				
				// adv.earth
				if (a.getAbilityType() == 10) {
					abilityShield(this, this, a);
					compensateForCosts(e1, e1.currentTarget, a);
				}
				
				// adv.wind
				if (a.getAbilityType() == 11) {
					if (this instanceof Player) {
						((Player) this).replaceLetters(6);
						compensateForCosts(e1, e1, a);
					}
					else System.err.println("Player only spell casted by NPC.");
					
				}		
				
				// ult.wind 
				if (a.getAbilityType() == 14) {
					
				}
			}
			
			
		}
	}

	private int calculateDamage(Entity e1, Entity e2, Ability a) {
		// e1: caster | e2: target
		if (a.getAbilityType() == 8) {
			return (e1.getLevel() - e2.getLevel()) + (e1.getLevel() * a.getDamageValue());
		}
		else if (a.getAbilityType() == 9) {
			return (int) Math.ceil( Math.pow((e1.getLevel() + a.getHealValue()), 1.33));
		}
		else return -1;
	}
	
	private boolean affordToUseAbility(Entity e1, Ability a) {
		if (((Player) e1).getElementalPower(a.getElementType()) >= a.getEPcost())
			return true;
		else 
			return false;
	}
	
	protected void useAbilityOLD(Entity e1, Ability a) {
		useAbilityOLD(e1, a, false);
	}
	
	protected void useAbilityOLD(Entity e1, Ability a, boolean isFree) {
		/* 
		 * consider calling this regardless the type: just execute EVERYTHING, since a heal will have
		 * damageValue 0 anyways
		 * 
		 * also you might want to add "Entity target" to the method to pass that along
		 *
		 * 
		 * */
		a.setTarget(e1.currentTarget);
		// the last condition (only use healing spell if its 
		if (/* affordToUseAbility(e1, a) && */ !a.isOnCooldown()) {
			
			// special requirements for casting a certain type of spell goes here (use ||):
			if (
					a.getHealValue() > 0 && e1.health < e1.maxHealth || // healing spells can only be used if health isn't full 
					a.getHealValue() <= 0	// default, so all non-healing spells can be used without restrictions
					) {
				
				if (e1.type.startsWith("mon")) {
					doAbility(a, a.getTarget());}
				else {
					if (a.getTargetType() == 1)
						doAbility(a, a.getTarget());
					if (a.getTargetType() == 3) {
						int n = this.currentTarget.localId - 1;
						doAbility(a, stage.getMonsters().get(n));
						
					}
					if (a.getTargetType() == 5) {
						for (int i = 0; i < stage.getMonsters().size(); i++) {
							doAbility(a, stage.getMonsters().get(i));
						}
					}
				}
				
				if (!isFree) compensateForCosts(this, this, a);
					a.setLastUsed(Game.getGameplay().getTurnCount());
					CombatLog.println("Ability (" + a.getName() + ") used.");
				}
			}
	}
	
	private void doAbility(Equipment a, Entity e) {
		a.setLastUsed(Game.getGameplay().getTurnCount()); // TODO: add this reset to resetGame() ! (+ maybe dots/hots as well?)
		if (a.getHealValue() > 0) doHealing(this, this, a, a.getHealValue());
		if (a.getDamageValue() > 0) dealDamage(this, e, a, a.getDamageValue());
		if (a.getUtilityValue() > 0) doUtility(this, e, a);
	}
	
	private void doWeaponAbility(Equipment a, Entity e) {
		doAbility(a, e);
	}
	
	public void applyOTs(Equipment a) {
//		if ((a.getLastUsed() + a.getTurnCount()) >= Gamestats.turnCount - 1) tick = 0;
		
		if (health > 0 && a.getDotValue() > 0) {
			
			if (a.getTargetType() == 1) {
				dealDamage(this, a.getTarget(), a.getDotValue(), true);			
				OTTicked(a.getDotValue());
			}
			if (a.getTargetType() == 3) {
				int n = a.getTarget().localId - 1;
				dealDamage(this, a.getTarget(), a.getDotValue(), true);
				OTTicked(a.getDotValue());
			}
			if (a.getTargetType() == 5) {
				for (int i = 0; i < stage.getMonsters().size(); i++) {
					dealDamage(this, a.getTarget(), a.getDotValue(), true);
					OTTicked(a.getDotValue());
				}
			}			
		}
		
		if (health > 0 && a.getHotValue() > 0) {
			increaseHealth(this, this, a.getHotValue(), true);
			this.tick++;
			CombatLog.println("[Tick " + tick + "] " + this.name + " heals HoT of " + a.getHotValue());
		}
		
		if (health > 0 && a.getMotValue() > 0) {
			increaseMana(this, this, a.getMotValue());
			this.tick++;
			CombatLog.println("[Tick " + tick + "] " + this.name + " restores mana MoT of " + a.getMotValue());
		}
	}

	private void OTTicked(int otValue) {
		this.tick++;
		CombatLog.println("[Tick " + tick + "] " + this.name + " dealt a DoT of " + otValue);
	}
	
	private void giveXP(Entity e, int n) {
		e.xp += n;
		e.totalXp += n;
	}

	public void setDefaultTarget(Entity e) {
		this.currentTarget = e;	
	}
	
	// LEGACY METHOD (NO WEAPON)
	protected int getRandomHitDamage(Entity e) {
		return getRandomHitDamage(e, null);
	}
	
	// NEW METHOD (HAS WEAPON)
	protected int getRandomHitDamage(Entity e, Weapon w) {
		Random rand = new Random();
		if (w != null) {
	    int r = rand.nextInt((w.getMaxDamage() - w.getMinDamage()) + 1) + w.getMinDamage();		
		return r;
		}
		else {
			int r = rand.nextInt((e.damage[1] - e.damage[0]) + 1) + e.damage[0];	
			return r;
		}
	}
	
	protected boolean isHitSuccessful(int hitchance) {
		Random rand = new Random();
		int r = rand.nextInt((100 - 0) + 1) - 0;
		CombatLog.println("Hit chance roll: " + r);
		lastHitChance = r;
		if (r >= (100 - hitchance)) return true;
		else return false;
	}
	
	public void resetActionPoints(Entity e) {
		e.actionPoints = maxActionPoints;
	}
	
	protected void resetMonsterWait() {
	
	}
	
	public void resetCooldowns(Entity e) {
		for (int i = 0; i < e.abilities.size(); i++) {
			e.abilities.get(i).setLastUsed(0);
			e.abilities.get(i).setOnCooldown(false);
		}
	}
	
	// ABILITIES
	private void addAbility(Ability a) {
		// this is a vanilla method, DO NOT use this directly, use unlockAbility() instead!
		abilities.add(a);
	}
	
	private void removeAbility(int n) {
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
			CombatLog.println(e.name + "'s " + abi.getName() + " unlocked.");
		}
	}
	
	protected void lockAbility(Entity e, int n) {
		CombatLog.println(e.name + "'s " + abilities.get(n).getName() + " locked.");
		removeAbility(n);
	}
	
	// WEAPONS
	protected void addWeapon(Weapon a) {
		// this is a vanilla method, DO NOT use this directly, use unlockWeapon() instead!
		weapons.add(a);
	}
	
	protected void removeWeapon(int n) {
		// this is a vanilla method, DO NOT use this directly, use lockWeapon() instead!
		weapons.remove(n);
	}
	
	protected void removeLastWeapon() {
		if (weapons.size() > 0) lockWeapon(this, weapons.size() - 1);
	}
	
	protected void unlockWeapon(Entity e, int id) {
		if (e.weapons.size() < e.weaponCount) {
			Weapon wep = new Weapon(e, id);
			this.mapWeapons.put(id, weapons.size());
			this.addWeapon(wep);
			CombatLog.println(e.name + "'s " + wep.getName() + " unlocked.");
		}
	}
	
	protected void lockWeapon(Entity e, int n) {
		CombatLog.println(e.name + "'s " + weapons.get(n).getName() + " locked.");
		removeWeapon(n);
	}
	
	protected void resetCurrentWord() {
		for (int i = 0; i < this.currentWord.length; i++) {
			this.currentWord[i] = 0;
			this.currentWordColors[i] = 0;
		}
		this.currentWordLength = 0;
	}
	
	// LEVEL UP STUFF
	protected void levelUpMaxHealthAndMana() {
		this.maxHealth = (int) Math.round((this.maxHealth * 1.1));
		if ( this.maxMana > 0) this.maxMana = (int) Math.round(((this.maxMana * 1.15) + this.level * 2.25));
	}
	
	protected void levelUp(byte level) {
		for (int i = 0; i < level - 1; i++) {
			this.levelUp();
		}
	}	
	

	
	public void levelUp() {
		this.level++;
		this.levelUpStats();
		if (this instanceof Player) {
			Game.getGameplay().setNotificationLevelUp();
			restoreHealth();
			restoreMana();
			CombatLog.println("Congratulations, you have reached level " + level + "!");
		}
	}
	
	private void levelUpStats() {
		// HEALTH & MANA
		this.levelUpMaxHealthAndMana();
		
		// WEAPON DAMAGE
	//	System.out.println("DD: " + this.damageDbl[0]);
		this.damageDbl[0] = ((this.damageDbl[0] * 1.12)); // extra variable to keep the decimal places
		this.damageDbl[1] = ((this.damageDbl[1] * 1.08));

		this.damage[0] = (int) Math.round(this.damageDbl[0]); 
		this.damage[1] = (int) Math.round(this.damageDbl[1]);
		
		// SPELLDAMAGE
	
		this.spellPower = (int) Math.ceil(this.level * 0.3);
	}
	

	public boolean hasWeaponID(int id) {
		if (mapWeapons.get(id) != null) return true;
		else return false;
	}
	
	// GETTERS
	public String getName() {
		return name;
	}
	
	public Entity getEntity() {
		return this;
	}
	
	public int getHealth() {
		return health;
	}
	
	public int getShield() {
		return shield;
	}
	
	public int getMana() {
		return mana;
	}
	
	public int getXp() {
		return xp;
	}
	
	public int getTotalXp() {
		return totalXp;
	}

	public int getXpNeeded() {
		return xpNeeded;
	}

	
	public int getXpGained() {
		return xpGained;
	}
	
	public boolean getIsAlive() {
		return isAlive;
	}
	
	public boolean getNeedsRemove() {
		return needsRemove;
	}
	
	public int getLastHitReceived() {
		return lastHitReceived;
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public Entity getTarget() {
		return this;
	}
	
	public Entity getCurrentTarget() {
		return currentTarget;
	}
	
	public int getTargetCycled() {
		return targetCycled;
	}
	
	public Weapon getWeapon() {
		return weapon;
	}
	
	public int getLastHitChance() {
		return lastHitChance;
	}
	
	public int getActionPoints() {
		return actionPoints;
	}
	
	public int getMaxActionPoints() {
		return maxActionPoints;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getPHealth() {
		return pHealth;
	}
	
	public int getPMana() {
		return pMana;
	}
	
	public int getPXP() {
		return pXP;
	}
	
	public boolean getHasSpawned() {
		return hasSpawned;
	}
	
	public boolean isWaiting() {
		return isWaiting;
	}

	public int getDamage(int i) {
		return damage[i];
	}
	
	public int getHitDamage() {
		return hitDamage;
	}
	
	public Ability getAbility(int i) {
		return abilities.get(i);
	}

	public int getMaxHealth() {
		return maxHealth;
	}
	
	public int getMaxMana() {
		return maxMana;
	}
	
	public int getAbilitiesSize() {
		return abilities.size();
	}
	
	public char getCurrentWord(int i) {
		return currentWord[i];
	}
	
	public char[] getCurrentWord() {
		return currentWord;
	}
	
	public int getCurrentWordLength() {
		return currentWordLength;
	}
	
	public int getDamageReduction() {
		return damageReduction;
	}
	
	public int getSpellPower() {
		return spellPower;
	}
	
	public boolean isStunned() {
		return isStunned;
	}
	
	public int getTick() {
		return tick;
	}
	
	// SETTERS
	
	protected void setWait(Boolean b) {
		isWaiting = b;
	}
	
	public void setSpawned(boolean b) {
		hasSpawned = b;
	}
	
	public void setRemove(boolean b) {
		needsRemove = b;
	}
	
	public void setTick(int n) {
		tick = n;
	}
	
	public void setDamageReduction(int n) {
		damageReduction = n;
	}
	
	public void resetDamageReduction() {
		damageReduction = 0;
	}
	
	public void setHealth(int n) {
		health = n;
	}
	
	public void setMana(int n) {
		mana= n;
	}
	
	public void setIsStunned(boolean b) {
		isStunned = b;
	}
	
	public int getBuffsSize() {
		return buffs.size();
	}
	
	public void setTarget(Entity e) {
		if (e != null) {
			if (e.getTarget().isAlive) {
				this.currentTarget = e.getTarget();
				CombatLog.println("Targeted " + e.name);
			}			
		} else
			this.currentTarget = null;
	}
	
	protected void cycleTarget(Entity e) {
		setTarget(e);
		targetCycled++;
	}
	
	public void init(Stage s) {
		this.stage = s;
	}
	
	protected void restoreHealth() {
		restoreHealth(100);
	}
	
	protected void restoreHealth(int percentage) {
		this.health = (int) (this.maxHealth * (percentage / 100.0));
	}
	
	protected void restoreMana() {
		restoreMana(100);
	}
	
	protected void restoreMana(int percentage) {
		this.mana = (int) (this.maxMana * (percentage / 100.0));
	}
	
	public void setPercentageValues() {
		pHealth = (byte) (((double) health / (double) maxHealth) * 100.0);
		pMana = (byte) (((double) mana / (double) maxMana) * 100.0);
		pAP = (byte) (((double) actionPoints / (double) maxActionPoints) * 100.0);
		if (this instanceof Player) pXP = (byte) (((double) xp / (double) ((Player) this).getXpNeeded()) * 100.0);
	}
	
	// ADDERS (TO THE MAX, NOT CURRENT)
	public void addHealth(int n) {
		this.maxHealth += n;
		if (this.maxHealth >= this.health + n) {
			increaseHealth(this, this, n, false);
		}
	}
	
	public void addMana(int n) {
		this.maxMana += n;
		if (this.maxMana >= this.mana + n) {
			increaseMana(this, this, n);
		}
	}
	
	public void addMaxActionPoints(int n) {
		this.maxActionPoints += n;
	}
	
	// BUFFS
	public void add(Buff b) {
		buffs.add(b);
	}
	
	protected void remove() {
		for (int i = 0; i < buffs.size(); i++)
			if (buffs.get(i).isNeedsRemoval()) buffs.remove(i);
	}
	
	public Buff getBuff(int i) {
			return buffs.get(i);
	}
	
	public String getBuffName(int i) {
		if (buffs.size() > i)
			return buffs.get(i).getName();
		else return "";
	}
	
	public int getBuffTicksLeft(int i) {
		if (buffs.size() > i)
			return buffs.get(i).getTicksLeft();
		else return 0;
	}
}
