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
		if (allDone) {
			monsterAIWait(1.5);
			if (!isWaiting) {
				Game.getGameplay().endTurn();
				allDone = false;
			}
		}
	}
	
	// WAIT STUFF
		protected void monsterAIWait(double n) {
			beginMonsterAIWait();
//			System.out.println(startWaitTimer);
			if (Game.getGameplay().getDeltaWaitTime() >= n) endWait(this);
			
		}
		
		protected void resetMonsterAIWait() {
			Game.getGameplay().setStartWaitTimer(System.currentTimeMillis());
			Game.getGameplay().setDeltaWaitTime(0);
//			r = newRandomAIWait();
		}
		
		protected void beginMonsterAIWait() {
			if (!Game.getGameplay().getIsWaitingOn()) {
				resetMonsterAIWait();
				Game.getGameplay().setIsWaitingOn(true);
			}
					
		}
		
		protected void endWait(MonsterAI m) {
			m.resetMonsterAIWait();
			m.isWaiting = false;
			Game.getGameplay().setIsWaitingOn(false);
		}
	
}