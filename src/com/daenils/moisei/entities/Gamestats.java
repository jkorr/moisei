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
	protected static Entity player;
	private Entity monster1;
	private Stage stage;
	
	// PLAYER STUFF
	public static int playerHP, playerMana, playerXP;
	public static boolean playerIsAlive;
	public static byte playerActionPoints, playerDefaultActionPoints, playerLevel;
	public static int[] playerDamage; // temporary stuff before implementing weapons
	public static int playerHitDamage;
	public static String playerLastAttacker;
	public static byte playerLastActionPoints;
	public static int playerTargetCycled;
	public static int playerLastHealth;
	
	public static boolean playerCanUseSkills;
	public static boolean playerNeverCycled;
	
	// MONSTER STUFF
	public static int[] monsterHP = new int[6];
	public static int[] monsterMana = new int[6];
	public static boolean[] monsterIsAlive = new boolean[6];
	public static byte[] monsterActionPoints = new byte[6];
	public static byte[] monsterDefaultActionPoints = new byte[6];
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
	
	public static boolean spawnSlotFilled1;
	public static boolean spawnSlotFilled2;
	public static boolean spawnSlotFilled3;
	public static boolean spawnSlotFilled4;
	public static boolean spawnSlotFilled5;
	
	// SAVED STUFF
	public static long savedTurnCount;
	public static double savedDeltaGameTime;
	public static int savedMonsterDeathCount;
	
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
		playerLastHealth = player.lastHealth;
		
		playerTargetCycled = player.getTargetCycled();
		playerNeverCycled = ((Player) player).neverCycled;
		
//		playerCanUseSkills = ((Player) player).getCanUseSkills();
		
		// check for the number of alive monsters
		monstersAlive = checkAliveMonsterCount();
		
		for (int i = 0; i < monsterCount; i++) {
			monsterHP[i] = stage.getMonsters().get(i).health;
			monsterMana[i] = stage.getMonsters().get(i).mana;
			
			monsterIsAlive[i] = stage.getMonsters().get(i).isAlive;
			monsterActionPoints[i] = stage.getMonsters().get(i).actionPoints;
			monsterDefaultActionPoints[i] = stage.getMonsters().get(i).defaultActionPoints;
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
		
		monsterHP[5] = monster1.getHealth();
		monsterMana[5] = monster1.getMana();
		
		monsterIsAlive[5] = monster1.isAlive;
		monsterActionPoints[5] = monster1.actionPoints;
		monsterDefaultActionPoints[5] = monster1.defaultActionPoints;
		monsterLevel[5] = monster1.level;
		
		monsterDamage[5] = monster1.damage;
		monsterHitDamage[5] = monster1.hitDamage;
		monsterLastAttacker[5] = monster1.lastAttacker;
		monsterLastHealth[5] = monster1.lastHealth;
		monsterLastActionPoints[5] = monster1.lastActionPoints;
		
		monsterDeathCount = Monster.getDeathCount();
		monstersAllDead = stage.checkIfAllDead();
		
//		monsterTarget = player;
		
		monsterIsWaiting[5] = monster1.isWaiting;
//		monsterSpawnSlot[5] = ((Monster) monster1).getSpawnSlot();
		monsterRW[5] = ((Monster) monster1).getRandomWait();
		
		spawnSlot1 =  ((Monster) monster1).spawnSlot1;
		spawnSlot2 =  ((Monster) monster1).spawnSlot2;
		spawnSlot3 =  ((Monster) monster1).spawnSlot3;
		spawnSlot4 =  ((Monster) monster1).spawnSlot4;
		spawnSlot5 =  ((Monster) monster1).spawnSlot5;
		
		// Game
		turnCount = Game.getGameplay().getCurrentTurn();
		waveCount = Game.getGameplay().getWaveCount();
		isPlayerTurn = Game.getGameplay().getIsPlayerTurn();
		isMonsterTurn = Game.getGameplay().getIsMonsterTurn(); 
		
		deltaTurnTime = Game.getGameplay().getDeltaTurnTime();
		deltaGameTime = Game.getGameplay().getDeltaGameTime();
		
		spawnSlotFilled1 = Game.getGameplay().getSpawnSlotFilled(1);
		spawnSlotFilled2 = Game.getGameplay().getSpawnSlotFilled(2);
		spawnSlotFilled3 = Game.getGameplay().getSpawnSlotFilled(3);
		spawnSlotFilled4 = Game.getGameplay().getSpawnSlotFilled(4);
		spawnSlotFilled5 = Game.getGameplay().getSpawnSlotFilled(5);
		}
	
		public static void submitGameStats() {
			savedTurnCount += turnCount;
			savedDeltaGameTime += deltaGameTime;
			savedMonsterDeathCount += monsterDeathCount;
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
