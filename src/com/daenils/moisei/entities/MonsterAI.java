package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.Stage;

public class MonsterAI {
	
	private Stage stage;
	private boolean allDone;
	private boolean isWaiting = true;

	public MonsterAI(Stage stage) {
		this.stage = stage;
	}
	
/*
*	public MonsterAI(Stage stage) {
*		this.stage = stage;
*	}
*/
	
	public void update() {
		
		for (int i = 0; i < stage.getMonsters().size(); i++) {
			stage.getMonsters().get(i).update();
		}
		
		checkIfAllDone();
		endTurnIfAllDone();
	}

	private void checkIfAllDone() {
		if (Game.getGameplay().getIsMonsterTurn()) {
			int n = 0;
			for(int i = 0; i < stage.getMonsters().size(); i++) {
				if (stage.getMonsters().get(i).health > 0 && stage.getMonsters().get(i).actionPoints == 0) {
					n++;
				}
			}
	//		System.out.print("\n" + n);
		//	System.out.println("\n " + Game.getGameplay().monstersAlive);
			if (n == Game.getGameplay().monstersAlive) allDone = true;
		}
		
	}
	
	private void endTurnIfAllDone() {
		if (allDone && Game.getGameplay().monstersAlive > 0) {
				Game.getGameplay().endTurn();				
				allDone = false;
		} else if (allDone && Game.getGameplay().monstersAlive <= 0) {
			Game.getGameplay().setPlayerTurn(true);
			Game.getGameplay().setMonsterTurn(false);
		}
	}
	
	
}