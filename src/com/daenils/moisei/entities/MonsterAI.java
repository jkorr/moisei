package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.Stage;

public class MonsterAI {
	
//	private Stage stage;
	private boolean allDone;

	public MonsterAI() {
	}
	
/*
*	public MonsterAI(Stage stage) {
*		this.stage = stage;
*	}
*/
	
	public void update() {
		checkIfAllDone();
		
		if (allDone) {
			Game.getGameplay().endTurn();
			allDone = false;
		}
		
	}

	private void checkIfAllDone() {
		if (Gamestats.isMonsterTurn) {			
			if (Gamestats.monsterCount == Monster.getMonstersAttacked()) {
				allDone = true;
			}
		}
		
	}
	
}