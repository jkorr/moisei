package com.daenils.moisei.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.daenils.moisei.Game;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.files.FileManager;
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
	
	private static Map<Integer, String> mapMonsters = new HashMap<Integer, String>();
	
//	private Ability[] monsterAbility = new Ability[2]; // fixed for two now, later probably it will depend on type of monster
	
	private boolean canUseSkills;
	protected boolean forceRemoved;
	protected boolean imUp;
	protected int levelModifier;
	
	private static int monstersAttacked;
	private static int deathCount;
		
	public Monster(int id, int spawnSlot, Entity defaultTarget) {
		load();
		updateSpawnSlots();
		
		this.spawnSlot = spawnSlot;
		Game.getGameplay().spawnSlotFilled[spawnSlot - 1] = true;
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
		this.maxHealth = Integer.parseInt(tempString[4]);
		this.maxMana = Integer.parseInt(tempString[5]);
		this.maxActionPoints = Byte.parseByte(tempString[6]);
		this.shield = Integer.parseInt(tempString[7]);
		this.levelModifier = Integer.parseInt(tempString[8]);
		this.damage = new int[] {Integer.parseInt(tempString[9]), Integer.parseInt(tempString[10])};
		this.abilityCount = Byte.parseByte(tempString[11]);
		initAbilities(tempString[12]);
		
		this.isAlive = true;
		this.level = 1;
		
		this.health = maxHealth;
		this.mana = maxMana;
		this.actionPoints = maxActionPoints;

		this.currentTarget = defaultTarget;
		this.isWaiting = true;
		this.randomWait = new int[] {1, 3};
		this.r = newRandomWait();
		
		this.stage = Stage.getStage();
		
		this.needsRemove = false;
		this.deathCount = 0;
		
		
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
	
	public void load() {
		List<String> lines = new ArrayList<String>();
		
		Scanner in;
		try {
			in = new Scanner(FileManager.fileMonsters);
			while (in.hasNextLine()) {
				lines.add(in.nextLine());
				for (int i = 0; i < lines.size(); i++) {
					String[] toSplit = lines.get(i).split(":");
					mapMonsters.put(Integer.parseInt(toSplit[0]), toSplit[1]);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void initAbilities(String s) {
		System.out.println(s);
		if (!s.equals("none")) {
			String tempString[] = s.split(";");
			
			for (int i = 0; i < tempString.length; i++) {
				unlockAbility(this, Integer.parseInt(tempString[i]));
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
	//	aiBehaviorUseHeal();
		
		// use stun
	//	aiBehaviorUseStun();
		
		// use dots
	//	aiBehaviorUseDots();
		
		// use shield
	//	aiBehaviorUseShield();	
		
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
//		if ((!isAlive && Game.getGameplay().getCurrentTurn() == 1) || forceRemoved) needsRemove = true;
//		else needsRemove = false;
	
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
				useAbility(this, abilities.get(1));
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

