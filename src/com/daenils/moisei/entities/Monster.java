package com.daenils.moisei.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.entities.equipments.Weapon;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;
import com.daenils.moisei.graphics.Stage;

public class Monster extends Entity {
	Random randomPick = new Random();
	
	private int spawnSlot;
	private int[] XY = new int[2];
	private int[] randomWait = new int[2];
	private int r;

	public boolean showDetails; 
	
	public int[] spawnSlot1;
	public int[] spawnSlot2;
	public int[] spawnSlot3;
	public int[] spawnSlot4;
	public int[] spawnSlot5;
	
	private static Map<Integer, String> mapMonsters = new HashMap<Integer, String>();
	
//	private Ability[] monsterAbility = new Ability[2]; // fixed for two now, later probably it will depend on type of monster
	
	private boolean canUseSkills;
	protected boolean forceRemoved;
	protected boolean imUp;
	protected int levelModifier;
	
	private static int monstersAttacked;
	private static int deathCount;
	
	public static int monstersLoaded;
	public static boolean monstersCounted;
	
	
		
	public Monster(int id, int spawnSlot, Entity defaultTarget, Stage stage) {
		this.stage = stage;
		load();
		
		this.localId = spawnSlot;
				
		setXY(spawnSlot);
		this.x = XY[0];
		this.y = XY[1];
		
		this.id = id;
		
		String[] tempString = mapMonsters.get(id).split(",");
		
		this.type = tempString[0];
		this.name = tempString[1];
		this.description = tempString[2];
		this.sprite = Sprite.parseSprite(tempString[3]);
		this.baseHealth = Integer.parseInt(tempString[4]);
		this.baseMana = Integer.parseInt(tempString[5]);
		this.maxActionPoints = Byte.parseByte(tempString[6]);
		this.spellPower = 1; // TODO: decide whether it goes into the file or not
		this.shield = Integer.parseInt(tempString[7]);
		this.levelModifier = Integer.parseInt(tempString[8]);
		this.baseDamage = new int[] {Integer.parseInt(tempString[9]), Integer.parseInt(tempString[10])};
		this.abilityCount = Byte.parseByte(tempString[11]);
		this.weaponCount = 5;
		initAbilities(tempString[12]);
		initWeapons(tempString[13]);
//		System.out.println(tempString[13]);
		
		if (weapons.size() > 0)
		this.weapon = this.weapons.get(0);
		
		this.isAlive = true;
		
		this.maxHealth = baseHealth;
		this.maxMana = baseMana;
		this.damage = baseDamage;
		this.damageDbl[0] = baseDamage[0];
		this.damageDbl[1] = baseDamage[1];
		this.level = 1;
		this.levelUp(defaultTarget.level);
		this.health = maxHealth;
		this.mana = maxMana;
		this.actionPoints = maxActionPoints;

		this.currentTarget = defaultTarget;
		this.isWaiting = true;
		this.randomWait = new int[] {1, 3};
		this.r = newRandomWait();
		
		this.width = sprite.getWidth();
		this.height = sprite.getHeight();
		
		this.needsRemove = false;
		this.deathCount = 0;
		
		setPercentageValues();
		}

	private void updateSpawnSlots() {
		if (stage.getMonsters().size() == 1) {
			spawnSlot1 = new int[] {580 / 2, 290 / 2};
			spawnSlot2 = new int[] {335 / 2, 320 / 2};
		}
		else if (stage.getMonsters().size() == 2) { 
			spawnSlot1 = new int[] {335 / 2, 320 / 2};
			spawnSlot2 = new int[] {580 / 2, 290 / 2};
			spawnSlot3 = new int[] {810 / 2, 250 / 2};
		}
		else if (stage.getMonsters().size() == 3) {
			spawnSlot1 = new int[] {335 / 2, 320 / 2};
			spawnSlot2 = new int[] {580 / 2, 290 / 2};
			spawnSlot3 = new int[] {810 / 2, 250 / 2};
			spawnSlot4 = new int[] {100 / 2, 250 / 2};
		}
		else if (stage.getMonsters().size() == 4) {
			spawnSlot1 = new int[] {100 / 2, 250 / 2};
			spawnSlot2 = new int[] {335 / 2, 320 / 2};
			spawnSlot3 = new int[] {580 / 2, 290 / 2};
			spawnSlot4 = new int[] {810 / 2, 250 / 2};
			spawnSlot5 = new int[] {1050 / 2, 210 / 2};
		}
		else if (stage.getMonsters().size() == 5) {
			spawnSlot1 = new int[] {100 / 2, 250 / 2};
			spawnSlot2 = new int[] {335 / 2, 320 / 2};
			spawnSlot3 = new int[] {580 / 2, 290 / 2};
			spawnSlot4 = new int[] {810 / 2, 250 / 2};
			spawnSlot5 = new int[] {1050 / 2, 210 / 2};
		}
		else {
		spawnSlot1 = new int[] {100 / 2, 250 / 2};
		spawnSlot2 = new int[] {335 / 2, 320 / 2};
		spawnSlot3 = new int[] {580 / 2, 290 / 2};
		spawnSlot4 = new int[] {810 / 2, 250 / 2};
		spawnSlot5 = new int[] {1050 / 2, 210 / 2};
		}
		
	}
	
	public void load() {
		List<String> lines = new ArrayList<String>();
		
		Scanner in;
		in = new Scanner(FileManager.inMonsters);
		while (in.hasNextLine()) {
			lines.add(in.nextLine());
			if (!monstersCounted) monstersLoaded++;
			for (int i = 0; i < lines.size(); i++) {
				String[] toSplit = lines.get(i).split(":");
				mapMonsters.put(Integer.parseInt(toSplit[0]), toSplit[1]);
			}
		}
		in.close();
		monstersCounted = true;
//		System.out.println(monstersLoaded);
	}
	
	public void initAbilities(String s) {
//		System.out.println(s);
		if (!s.equals("none")) {
			String tempString[] = s.split(";");
			
			for (int i = 0; i < tempString.length; i++) {
				unlockAbility(this, Integer.parseInt(tempString[i]));
			}
		}
	}
	
	public void initWeapons(String s) {
//		System.out.println(s);
		if (!s.equals("none")) {
			String tempString[] = s.split(";");
			
			for (int i = 0; i < tempString.length; i++) {
				unlockWeapon(this, Integer.parseInt(tempString[i]));
			}
		}
	}
	
	public void updateXY() {
		this.x = XY[0];
		this.y = XY[1];
	}
	
	public void update() {
	//	applyDots();
	//	if (this.actionPoints > 0) this.imUp = true;
	//	else this.imUp = false;
		
	/*	if ((Math.round((Game.getGameplay().getDeltaTimeStage() / 100000000))) % 5 == 0) {
			this.isGettingDamage = false;
			System.out.println(Math.round((Game.getGameplay().getDeltaTimeStage() / 100000000)));
		}
	*/
		
		if (isGettingDamage && System.nanoTime() > (this.flashStart + this.flashDuration)) this.isGettingDamage = false;
		
		setPercentageValues();
		
		// the following 3 lines make the dynamic monster changing possible:
		updateSpawnSlots();
		setXY(spawnSlot);
		updateXY();
	
	
//		System.out.println(id);
//		System.out.println("Waiting? " + isWaiting);
//		System.out.println(isWaiting);
		
		//if dead 0 ap since checkIfDone() looks for ap
		// extend this into a method for additional death-related stuff
		// e.g. new sprite for dead ppl
		if (!isAlive) { 
			actionPoints = 0;
			sprite = Sprite.monster_generic_dead;
		}
		
	// AI BEHAVIORS
	//	aiBehaviorUseHeal();
	//	aiBehaviorUseStun();
	//	aiBehaviorUseDots();
	//	aiBehaviorUseShield();	
		aiBehaviorUseDamageSpells();
		
		// basic attack
		if (checkCanUseSkills() && currentTarget.isAlive) {
			monsterWait(3);
		if (!isWaiting) {
				if (actionPoints > 0) {
					pickAWord(); 
					for (int i = 0; i < currentWord.length; i++) System.out.print(currentWord[i]);
					basicAttack(this, currentTarget, null);
					monstersAttacked++;
				}
			}			
		}
		isWaiting = true;
	}

	private void aiBehaviorUseDamageSpells() {
		if (checkCanUseSkills() && currentTarget.isAlive) {
			for (int i = 0; i < this.abilities.size(); i++) {
				if (this.abilities.get(i).getID() == 2) aiBehaviorUseFireball(i);
			}
		}
	}

	private void aiBehaviorUseFireball(int i) {
//		System.out.println("Attempting to cast fireball...");
			monsterWait(3);
			if (!isWaiting) {
				useAbility(this, abilities.get(i));
			}
	}

	private void aiBehaviorUseStun() {

		if (checkCanUseSkills() && (currentTarget.getMana() < 20 && !currentTarget.isStunned) && currentTarget.isAlive) {
			monsterWait(3);
			if (!isWaiting) {
				useAbility(this, abilities.get(1));
			}
		}
	}

	private void aiBehaviorUseHeal() {
		if (checkCanUseSkills() && this.health < 20) {
			monsterWait(3);
			if (!isWaiting) {
				useAbility(this, abilities.get(0));
			}
		}
	}
	
	private void aiBehaviorUseDots() {
		if (checkCanUseSkills() && currentTarget.health < 150) {
			monsterWait(3);
			if (!isWaiting) {
				useAbility(this, abilities.get(1));
			}
		}
	}
	
	private void aiBehaviorUseShield() {
		if (checkCanUseSkills()) {
			monsterWait(3);
			if (!isWaiting) {
				useAbility(this, abilities.get(3));
			}
		}
	}
	
	private void pickAWord() {
	 	resetCurrentWord();
		int max = 0, n = 0, length = 0;
		do {
			max = FileManager.lines.size();
			n = randomPick.nextInt(((max - 0) + 0) + 1);
			length = FileManager.lines.get(n).length();
			System.out.println("\nWORD: " + FileManager.lines.get(n));
		} while (length > 10);
		for (int i = 0; i < length; i++) {
			currentWord[i] = FileManager.lines.get(n).toUpperCase().charAt(i);
		}
		for (int i = 0; i < currentWord.length; i++)
			if (currentWord[i] != 0) this.currentWordLength++;
	}
	
	public void render(Screen screen) {
		if (this.isGettingDamage) screen.renderSpriteAsColor(x, y, sprite, 1, 0xffffffff);
		else screen.renderSprite(x, y, sprite, 1);
	}
	
	private boolean checkCanUseSkills() {
		if (Game.getGameplay().getIsMonsterTurn() && isAlive && actionPoints > 0 && Game.getGameplay().getContinueGame()) return canUseSkills = true;
		else return canUseSkills = false;
	}
	
	
/*	private int[] setXY(int spawnSlot) {
		if (spawnSlot < 1 || spawnSlot > 5) { XY[0] = 100 / 2; XY[1] = 250 / 2; }
		else if (spawnSlot == 1)  { XY[0] = spawnSlot1[0]; XY[1] = spawnSlot1[1]; }
		else if (spawnSlot == 2)  { XY[0] = spawnSlot2[0]; XY[1] = spawnSlot2[1]; }
		else if (spawnSlot == 3)  { XY[0] = spawnSlot3[0]; XY[1] = spawnSlot3[1]; }
		else if (spawnSlot == 4)  { XY[0] = spawnSlot4[0]; XY[1] = spawnSlot4[1]; }
		else if (spawnSlot == 5)  { XY[0] = spawnSlot5[0]; XY[1] = spawnSlot5[1]; }
		
		return XY;
	} */
	
	private int[] setXY(int spawnSlot) {
		XY[0] = 390; XY[1] = 134;
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

