package com.daenils.moisei.entities;

import com.daenils.moisei.Game;
import com.daenils.moisei.graphics.Stage;

public class MonsterAI {
	
	private Stage stage;
	private boolean allDone;

	public MonsterAI(Stage stage) {
		this.stage = stage;
	}
	
/*
*	public MonsterAI(Stage stage) {
*		this.stage = stage;
*	}
*/
	
	public void update() {
		checkIfAllDone();
		endTurnIfAllDone();
	}

	private void checkIfAllDone() {
		if (Gamestats.isMonsterTurn) {
			int n = 0;
			for(int i = 0; i < stage.getMonsters().size(); i++) {
				if (stage.getMonsters().get(i).actionPoints == 0) {
					n++;
				}
			}
			if (n == Gamestats.monsterCount) allDone = true;
		}
		
	}
	
	private void endTurnIfAllDone() {
		if (allDone) {
			Game.getGameplay().endTurn();
			allDone = false;
		}
	}
	
}