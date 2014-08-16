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
		if (allDone && Gamestats.monstersAlive > 0) {
			stage.getMonsters().get(0).monsterWait(1.5); // this is not the nicest solution, but for now it'll do
			if (!stage.getMonsters().get(0).isWaiting) {  // basically I pick the first monster and make him wait
				Game.getGameplay().endTurn();				// instead of making a separate way to make the AI wait
				allDone = false;
			}
		} else if (allDone && Gamestats.monstersAlive <= 0) {
			Game.getGameplay().setPlayerTurn(true);
			Game.getGameplay().setMonsterTurn(false);
		}
	}
	
	
}