package com.daenils.moisei.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

	private boolean[] keys = new boolean[300];
	
	// player controls
	public boolean playerQ, playerW, playerE, playerR;
	public boolean[] playerTarget = new boolean[5];
	public boolean playerCycleTargets;
	
	public boolean playerBasicAttack;
	
	public boolean playerEndTurn;
	public boolean playerSwitchWeapon;
	
	// gui control
	public boolean playerSwitchGUIView;
	public boolean playerPauseGame;

	// debug stuff
	public boolean debugLockAbility, debugUnlockAbility;
	public boolean debugAddMonster;
	public boolean debugForceNewWave;
	public boolean debugToggleFpsLock, debugShowDebugInfo;
	
	public long[] keysPressTime = new long[300];
	public long[] keysRelTime = new long[300];
	
	public void update() {
		playerQ = keys[KeyEvent.VK_Q];
		playerW = keys[KeyEvent.VK_W];
		playerE = keys[KeyEvent.VK_E];
		playerR = keys[KeyEvent.VK_R];
		
		playerBasicAttack = keys[KeyEvent.VK_SPACE];
		playerEndTurn = keys[KeyEvent.VK_ENTER];
		playerSwitchWeapon = keys[KeyEvent.VK_G];
		
		playerCycleTargets = keys[KeyEvent.VK_0];
		playerTarget[0] = keys[KeyEvent.VK_1];
		playerTarget[1] = keys[KeyEvent.VK_2];
		playerTarget[2] = keys[KeyEvent.VK_3];
		playerTarget[3] = keys[KeyEvent.VK_4];
		playerTarget[4] = keys[KeyEvent.VK_5];
		
		playerSwitchGUIView = keys[KeyEvent.VK_F1];
		playerPauseGame = keys[KeyEvent.VK_P];
		
		debugShowDebugInfo = keys[KeyEvent.VK_F11];
		debugLockAbility = keys[KeyEvent.VK_PAGE_UP];
		debugUnlockAbility = keys[KeyEvent.VK_PAGE_DOWN];
		debugAddMonster = keys[KeyEvent.VK_M];
		debugForceNewWave = keys[KeyEvent.VK_N];
		debugToggleFpsLock = keys[KeyEvent.VK_F12];
		
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
