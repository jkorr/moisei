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
	private Stage stage;
	
	private static int playerLevel, playerXP, playerMaxHP, playerMaxMP, playerMaxAP;
	
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
	
	public Gamestats(Stage stage) {
		this.stage = stage;
	}
	
	public void update() {
		if (stage.getPlayer() != null) {
			playerLevel = stage.getPlayer().level;
			playerXP = stage.getPlayer().getXP();
			playerMaxHP = stage.getPlayer().maxHealth;
			playerMaxMP = stage.getPlayer().maxMana; 
			playerMaxAP = stage.getPlayer().maxActionPoints;	
		}
	
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
			savedPlayerMaxMana = playerMaxMP;
			savedPlayerMaxAP = playerMaxAP;
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
