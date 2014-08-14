package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.Stage;

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
	public static Entity player;
	private Entity monster1;
	private Stage stage;
	
	// PLAYER STUFF
	public static int playerHP, playerMana, playerShield, playerXP;
	public static int playerMaxHP, playerMaxMana;
	public static boolean playerIsAlive;
	public static byte playerActionPoints, playerMaxActionPoints, playerLevel;
	public static int[] playerDamage; // temporary stuff before implementing weapons
	public static int playerHitDamage;
	public static String playerLastAttacker;
	public static byte playerLastActionPoints;
	public static int playerTargetCycled;
	public static int playerLastHealth;
	
	public static boolean playerCanUseSkills;
	public static boolean playerNeverCycled;

	public static int playerLastHitReceived;
	
	// MONSTER STUFF
	public static int[] monsterHP = new int[6];
	public static int[] monsterMaxHP = new int[6];
	public static int[] monsterMana = new int[6];
	public static int[] monsterMaxMana = new int[6];
	public static int[] monsterShield = new int[6];
	public static boolean[] monsterIsAlive = new boolean[6];
	public static byte[] monsterActionPoints = new byte[6];
	public static byte[] monsterMaxActionPoints = new byte[6];
	public static byte[] monsterLevel = new byte[6];
	public static int[][] monsterDamage = new int[6][2]; // temporary stuff
	public static int[] monsterHitDamage = new int[6];
	public static String[] monsterLastAttacker = new String[6];
	public static String[] monsterId = new String[6];
	public static byte[] monsterLastActionPoints = new byte[6];
	public static int[] monsterLastHealth = new int[6];
	public static int[] monsterSpawnSlot = new int[6];
	public static boolean[] monsterIsWaiting = new boolean[6];
	public static int[] monsterRW = new int[6];
	
	public static int[] spawnSlot1;
	public static int[] spawnSlot2;
	public static int[] spawnSlot3;
	public static int[] spawnSlot4;
	public static int[] spawnSlot5;
	

	public static Entity monsterTarget;
	
	// GAME STUFF
	public static long turnCount;
	public static int waveCount;
	public static boolean isPlayerTurn;
	public static boolean isMonsterTurn;
	
	public static double deltaTurnTime;
	public static double deltaGameTime;
	
	public static int monsterCount;
	public static int monsterDeathCount;
	public static int monstersAttacked;
	public static boolean monstersAllDead;
	public static int monstersAlive;
	public static int monstersVulnerableToDots;
	
	public static boolean[] spawnSlotFilled = new boolean[5];
	
	// SAVED STUFF
	public static long savedTurnCount;
	public static int savedWaveCount;
	public static double savedDeltaGameTime;
	public static int savedMonsterDeathCount;
	
	public static int savedPlayerLevel;
	public static int savedPlayerTotalXP;
	public static int savedPlayerMaxHP;
	public static int savedPlayerMaxMana;
	public static int savedPlayerMaxAP;
	
	// SAVED STUFF -- BUT NOT YET IMPLEMENTED
	public static int savedMonsterCount;
	public static int savedPlayerDamageDealt; // needs a playerDamageDealt as well?
	public static int savedMonsterDamageDealt; // -- " --
	
	public Gamestats(Entity player, Stage stage, Entity monster) {
		this.player = player;
		this.monster1 = monster;
		this.stage = stage;
	}
	
	public void update() {
		monsterCount = player.getStage().getMonsters().size();
		monstersAttacked = Monster.getMonstersAttacked();
		
		// Player
		playerHP = player.getHealth();
		playerMaxHP = player.maxHealth;
		playerMana = player.getMana();
		playerMaxMana = player.maxMana;
		playerShield = player.getShield();
		playerXP = player.getXP();
		playerDamage = player.damage;

		
		playerIsAlive = player.isAlive;
		playerActionPoints = player.actionPoints;
		playerMaxActionPoints = player.maxActionPoints;
		playerLevel = player.level;
		
		playerDamage = player.damage;
		playerHitDamage = player.hitDamage;
		playerLastAttacker = player.lastAttacker;
		playerLastActionPoints = player.lastActionPoints;
		playerLastHealth = player.lastHealth;
		
		playerTargetCycled = player.getTargetCycled();
		playerNeverCycled = ((Player) player).neverCycled;
		playerLastHitReceived = (playerLastHealth - playerHP) * -1;
		if (playerLastHitReceived == 100) playerLastHitReceived = 0;
		
//		playerCanUseSkills = ((Player) player).getCanUseSkills();
		
		// check for the number of alive monsters
		monstersAlive = checkAliveMonsterCount();
		
		for (int i = 0; i < monsterCount; i++) {
			monsterHP[i] = stage.getMonsters().get(i).health;
			monsterMaxHP[i] = stage.getMonsters().get(i).maxHealth;
			monsterMana[i] = stage.getMonsters().get(i).mana;
			monsterMaxMana[i] = stage.getMonsters().get(i).maxMana;
			monsterShield[i] = stage.getMonsters().get(i).shield;
			
			monsterIsAlive[i] = stage.getMonsters().get(i).isAlive;
			monsterActionPoints[i] = stage.getMonsters().get(i).actionPoints;
			monsterMaxActionPoints[i] = stage.getMonsters().get(i).maxActionPoints;
			monsterLevel[i] = stage.getMonsters().get(i).level;
			
			monsterDamage[i][0] = stage.getMonsters().get(i).damage[0];
			monsterDamage[i][1] = stage.getMonsters().get(i).damage[1];
			
			monsterHitDamage[i] = stage.getMonsters().get(i).hitDamage;
			monsterLastAttacker[i] = stage.getMonsters().get(i).lastAttacker;
			monsterLastHealth[i] = stage.getMonsters().get(i).lastHealth;
			monsterId[i] = stage.getMonsters().get(i).name;
			monsterLastActionPoints[i] = stage.getMonsters().get(i).lastActionPoints;
			
			monsterIsWaiting[i] = stage.getMonsters().get(i).isWaiting;
			monsterRW[i] = ((Monster) stage.getMonsters().get(i)).getRandomWait();
		}
		
/*		monsterHP[5] = monster1.getHealth();
		monsterMana[5] = monster1.getMana();
		
		monsterIsAlive[5] = monster1.isAlive;
		monsterActionPoints[5] = monster1.actionPoints;
		monsterDefaultActionPoints[5] = monster1.defaultActionPoints;
		monsterLevel[5] = monster1.level;
		
		monsterDamage[5] = monster1.damage;
		monsterHitDamage[5] = monster1.hitDamage;
		monsterLastAttacker[5] = monster1.lastAttacker;
		monsterLastHealth[5] = monster1.lastHealth;
		monsterLastActionPoints[5] = monster1.lastActionPoints; */
		
		monsterDeathCount = Monster.getDeathCount();
		monstersAllDead = stage.checkIfAllDead();
		
//		monsterTarget = player;
		
	/*	monsterIsWaiting[5] = monster1.isWaiting;
//		monsterSpawnSlot[5] = ((Monster) monster1).getSpawnSlot();
		monsterRW[5] = ((Monster) monster1).getRandomWait(); */
		
/*		spawnSlot1 =  ((Monster) monster1).spawnSlot1;
		spawnSlot2 =  ((Monster) monster1).spawnSlot2;
		spawnSlot3 =  ((Monster) monster1).spawnSlot3;
		spawnSlot4 =  ((Monster) monster1).spawnSlot4;
		spawnSlot5 =  ((Monster) monster1).spawnSlot5; */
		
		// Game
		turnCount = Game.getGameplay().getCurrentTurn();
		waveCount = Game.getGameplay().getWaveCount();
		isPlayerTurn = Game.getGameplay().getIsPlayerTurn();
		isMonsterTurn = Game.getGameplay().getIsMonsterTurn(); 
		
		deltaTurnTime = Game.getGameplay().getDeltaTurnTime();
		deltaGameTime = Game.getGameplay().getDeltaGameTime();
		
		spawnSlotFilled[0] = Game.getGameplay().getSpawnSlotFilled(1);
		spawnSlotFilled[1] = Game.getGameplay().getSpawnSlotFilled(2);
		spawnSlotFilled[2] = Game.getGameplay().getSpawnSlotFilled(3);
		spawnSlotFilled[3] = Game.getGameplay().getSpawnSlotFilled(4);
		spawnSlotFilled[4] = Game.getGameplay().getSpawnSlotFilled(5);
		}
	
		public static void submitStats_endWave() {
			savedTurnCount += turnCount;
			savedWaveCount = waveCount;
			savedDeltaGameTime = deltaGameTime;
			savedMonsterDeathCount += monsterDeathCount;
			savedPlayerLevel = playerLevel;
			savedPlayerTotalXP = playerXP;
			savedPlayerMaxHP = playerMaxHP;
			savedPlayerMaxMana = playerMaxMana;
			savedPlayerMaxAP = playerMaxActionPoints;	
		}
		
		public static void submitStats_endStage() {
			
		}
		
		public static String readGameStats() {
			String s = "Turn count: " + savedTurnCount
					+ "\nWave count: " + savedWaveCount
					+ "\nPlaytime: " + savedDeltaGameTime
					+ "\nKills: " + savedMonsterDeathCount
					+ "\nPlayer level: " + savedPlayerLevel
					+ "\nTotal XP: " + savedPlayerTotalXP
					+ "\nMax HP: " + savedPlayerMaxHP
					+ "\nMax Mana: " + savedPlayerMaxMana
					+ "\nMax AP: " + savedPlayerMaxAP;
			return s;
		}
		
		// GETTERS
		
		public static int getTotalTurnCount() {
			return (int) (savedTurnCount + turnCount);
		}
		
		// TODO: maybe change this one to double for precision
		public static int getTotalGameTime() {
			return (int) (savedDeltaGameTime + deltaGameTime);
		}
		
		public static int getTotalMonsterDeathCount() {
			return savedMonsterDeathCount + monsterDeathCount;
		}
		
		public int checkAliveMonsterCount() {
			int n = 0;
			for (int i =0; i < monsterCount; i++) {
				if (stage.getMonsters().get(i).isAlive) n++;
			}
			return n;
		}
	
		
		
}
