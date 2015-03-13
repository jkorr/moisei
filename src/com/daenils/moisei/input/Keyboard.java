package com.daenils.moisei.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

	private boolean[] keys = new boolean[300];
	
	// player controls
	public boolean playerQ, playerW, playerE, playerR;
	public boolean[] playerTarget = new boolean[5];
	public boolean playerCycleTargets;
	
	public boolean[] alphabet = new boolean[26];
	public boolean[] radialChoice = new boolean[5];
	public boolean removeLast;
	
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
		alphabet[0] = keys[KeyEvent.VK_A];
		alphabet[1] = keys[KeyEvent.VK_B];
		alphabet[2] = keys[KeyEvent.VK_C];
		alphabet[3] = keys[KeyEvent.VK_D];
		alphabet[4] = keys[KeyEvent.VK_E];
		alphabet[5] = keys[KeyEvent.VK_F];
		alphabet[6] = keys[KeyEvent.VK_G];
		alphabet[7] = keys[KeyEvent.VK_H];
		alphabet[8] = keys[KeyEvent.VK_I];
		alphabet[9] = keys[KeyEvent.VK_J];
		alphabet[10] = keys[KeyEvent.VK_K];
		alphabet[11] = keys[KeyEvent.VK_L];
		alphabet[12] = keys[KeyEvent.VK_M];
		alphabet[13] = keys[KeyEvent.VK_N];
		alphabet[14] = keys[KeyEvent.VK_O];
		alphabet[15] = keys[KeyEvent.VK_P];
		alphabet[16] = keys[KeyEvent.VK_Q];
		alphabet[17] = keys[KeyEvent.VK_R];
		alphabet[18] = keys[KeyEvent.VK_S];
		alphabet[19] = keys[KeyEvent.VK_T];
		alphabet[20] = keys[KeyEvent.VK_U];
		alphabet[21] = keys[KeyEvent.VK_V];
		alphabet[22] = keys[KeyEvent.VK_W];
		alphabet[23] = keys[KeyEvent.VK_X];
		alphabet[24] = keys[KeyEvent.VK_Y];
		alphabet[25] = keys[KeyEvent.VK_Z];
		removeLast = keys[KeyEvent.VK_BACK_SPACE];
		
		radialChoice[0] = keys[KeyEvent.VK_UP];
		radialChoice[1] = keys[KeyEvent.VK_RIGHT];
		radialChoice[2] = keys[KeyEvent.VK_DOWN];
		radialChoice[3] = keys[KeyEvent.VK_LEFT];
		radialChoice[4] = keys[KeyEvent.VK_SPACE];
		
		playerQ = keys[KeyEvent.VK_6];
		playerW = keys[KeyEvent.VK_7];
		playerE = keys[KeyEvent.VK_8];
		playerR = keys[KeyEvent.VK_9];
		
		playerBasicAttack = keys[KeyEvent.VK_SPACE];
		playerEndTurn = keys[KeyEvent.VK_ENTER];
		playerSwitchWeapon = keys[KeyEvent.VK_G];
		
		playerCycleTargets = keys[KeyEvent.VK_0];
		playerTarget[0] = keys[KeyEvent.VK_1];
		playerTarget[1] = keys[KeyEvent.VK_2];
		playerTarget[2] = keys[KeyEvent.VK_3];
		playerTarget[3] = keys[KeyEvent.VK_4];
		playerTarget[4] = keys[KeyEvent.VK_5];
		
		playerSwitchGUIView = keys[KeyEvent.VK_F5];
		playerPauseGame = keys[KeyEvent.VK_F1];
		
		debugShowDebugInfo = keys[KeyEvent.VK_F11];
		debugLockAbility = keys[KeyEvent.VK_PAGE_UP];
		debugUnlockAbility = keys[KeyEvent.VK_PAGE_DOWN];
		debugAddMonster = keys[KeyEvent.VK_F2];
		debugForceNewWave = keys[KeyEvent.VK_F3];
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
