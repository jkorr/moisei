package com.daenils.moisei.entities;

import com.daenils.moisei.Game;

/* --------------
 * GAMESTATS.JAVA
 * --------------
 * 
 * DO NOT FORGET:
 * NEVER CHANGE ANYTHING IN THESE VALUES FROM THE OUTSIDE!
 * ONLY UPDATE THESE VALUES INSIDE!
 *  
 */

public class Gamestats {
	private Entity player;
	private Entity monster1;
	
	// PLAYER STUFF
	public static int playerHP, playerMana, playerXP;
	public static boolean playerIsAlive;
	public static byte playerActionPoints, playerDefaultActionPoints, playerLevel;
	public static int[] playerDamage; // temporary stuff before implementing weapons
	public static int playerHitDamage;
	public static String playerLastAttacker;
	public static byte playerLastActionPoints;
	
	public static boolean playerCanUseSkills;
	
	// MONSTER STUFF
	public static int monsterHP, monsterMana;
	public static boolean monsterIsAlive;
	public static byte monsterActionPoints, monsterDefaultActionPoints, monsterLevel;
	public static int[] monsterDamage; // temporary stuff
	public static int monsterHitDamage;
	public static String monsterLastAttacker;
	public static byte monsterLastActionPoints;
	
	public static int monsterSpawnSlot;
	
	public static int[] spawnSlot1;
	public static  int[] spawnSlot2;
	public static int[] spawnSlot3;
	public static int[] spawnSlot4;
	public static int[] spawnSlot5;
	
	public static boolean monsterIsWaiting;
	public static int monsterRW;
	
	// GAME STUFF
	public static long turnCount;
	public static boolean isPlayerTurn;
	public static boolean isMonsterTurn;
	
	public static double deltaTurnTime;
	public static double deltaGameTime; // not yet implemented, timer for the whole game

	
	public Gamestats(Entity player, Entity monster) {
		this.player = player;
		this.monster1 = monster;

	}
	
	public void update() {
		// Player
		playerHP = player.getHealth();
		playerMana = player.getMana();
		playerXP = player.getXP();
		playerDamage = player.damage;

		
		playerIsAlive = player.isAlive;
		playerActionPoints = player.actionPoints;
		playerDefaultActionPoints = player.defaultActionPoints;
		playerLevel = player.level;
		
		playerDamage = player.damage;
		playerHitDamage = player.hitDamage;
		playerLastAttacker = player.lastAttacker;
		playerLastActionPoints = player.lastActionPoints;
		
//		playerCanUseSkills = ((Player) player).getCanUseSkills();
		
		// Monster
		// all of these probably needs to be []
		monsterHP = monster1.getHealth();
		monsterMana = monster1.getMana();
		
		monsterIsAlive = monster1.isAlive;
		monsterActionPoints = monster1.actionPoints;
		monsterDefaultActionPoints = monster1.defaultActionPoints;
		monsterLevel = monster1.level;
		
		monsterDamage = monster1.damage;
		monsterHitDamage = monster1.hitDamage;
		monsterLastAttacker = monster1.lastAttacker;
		monsterLastActionPoints = monster1.lastActionPoints;
		
		monsterIsWaiting = monster1.isWaiting;
//		monsterSpawnSlot = ((Monster) monster1).getSpawnSlot();
		monsterRW = ((Monster) monster1).getRandomWait();
		
		spawnSlot1 =  ((Monster) monster1).spawnSlot1;
		spawnSlot2 =  ((Monster) monster1).spawnSlot2;
		spawnSlot3 =  ((Monster) monster1).spawnSlot3;
		spawnSlot4 =  ((Monster) monster1).spawnSlot4;
		spawnSlot5 =  ((Monster) monster1).spawnSlot5;
		
		// Game
		turnCount = Game.getGameplay().getCurrentTurn();
		isPlayerTurn = Game.getGameplay().getIsPlayerTurn();
		isMonsterTurn = Game.getGameplay().getIsMonsterTurn(); 
		
		deltaTurnTime = Game.getGameplay().getDeltaTurnTime();

		}
	
}
