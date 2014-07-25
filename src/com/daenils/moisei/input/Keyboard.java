package com.daenils.moisei.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

	private boolean[] keys = new boolean[300];
	
	// player controls
	public boolean playerQ, playerW, playerE, playerR;
	public boolean playerBasicAttack;
	public boolean playerEndTurn;

	// debug stuff
	public boolean debugTogglePlayerTurn, debugToggleMonsterTurn;
	
	public long[] keysPressTime = new long[300];
	public long[] keysRelTime = new long[300];
	
	public void update() {
		playerQ = keys[KeyEvent.VK_Q];
		playerW = keys[KeyEvent.VK_W];
		playerE = keys[KeyEvent.VK_E];
		playerR = keys[KeyEvent.VK_R];
		
		playerBasicAttack = keys[KeyEvent.VK_SPACE];
		playerEndTurn = keys[KeyEvent.VK_ENTER];
		
		debugTogglePlayerTurn = keys[KeyEvent.VK_PAGE_UP];
		debugToggleMonsterTurn = keys[KeyEvent.VK_PAGE_DOWN];
		
//		for (int i = 0; i < keys.length; i++)
//			if (keys[i]) System.out.println(i); 
	}
	
	public void keyPressed(KeyEvent e) {
		keys[e.getKeyCode()] = true;
		keysPressTime[e.getKeyCode()] = System.currentTimeMillis();
	}

	public void keyReleased(KeyEvent e) {
		keys[e.getKeyCode()] = false;
		keysRelTime[e.getKeyCode()] = System.currentTimeMillis();
	}

	public void keyTyped(KeyEvent e) {	
	}

}
