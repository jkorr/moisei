package com.daenils.moisei;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;

import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Notification;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Text;
import com.daenils.moisei.graphics.Window;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;

public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;
	private static int scale = 2;
	private static int width = 640;
	private static int height = (width / 16 * 9);
	private static String title = "MOISEI";
	private static String version = "0.5.0";
	private static String projectStage = "f&f alpha";
	private static boolean fpsLock = true;
	private static boolean devMode = false;
	private static byte gameState = 0, newGameState = 0; // -1: Blank; 0: Main Menu; 1-4: reserved for menus; 5: game
	private static String[] gameStateString = {"mainmenu", "stageselect", "settings", "spells", "credits", "ingame"};
	private static final String[] MAIN_MENU_STRINGS = {"New Game", "Continue", "Select Stage", "Settings", "Exit Game"};
	private static final String[] LIST_OF_BANNED_NAMES = {"default"};
	
	private Thread thread;
	private JFrame frame;

	private Screen screen;
	private Keyboard key;
	private Mouse mouse;

	// I'll see if this works out, but I currently don't have any other idea
	// other than having this as static. I mean it only has ONE instance
	// under any given circumstances, so I guess no harm's done, right? 
	private static Gameplay gameplay;
	private static Stage stage;

	private boolean running = false;

	private BufferedImage image = new BufferedImage(width, height,
			BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
			.getData();
	
	// MENU STUFF
	private static List<Notification> notifications = new ArrayList<Notification>();
	
	private int menuOptionSelected = -1;
	private boolean onCooldown = false;
	private static boolean textInputEnabled;
	private long cdStart, cdEnd, cdDuration;
	private int stageSelected = 0;
	private String selectionString = stageSelected + "";
	public static final int[] TOPLEFT = {2, 6};
	public static final int[] BOTTOMLEFT = {2, 346};
	private static final int[][] TOPRIGHT = { {521, 4}, {521+54, 4+10}} ;
	private static final int[] BOTTOMRIGHT = {521, 346}; 
	
	// PROFILE STUFF
	private String profileName;
//	private List<Character> profileNameFromInput = new ArrayList<Character>();
	
	public Game() {
		Dimension size = new Dimension(width * scale, height * scale);
		setPreferredSize(size);

		screen = new Screen(width, height);
		System.out.println("Rendering screen at the resolution of "
				+ (width * scale) + "x" + (height * scale) + ".");
		frame = new JFrame();
		
		// LOADING STAGE FROM FILE
		FileManager.loadStages();
		Stage.load(); // loads the stages and sets the stageCount value

		key = new Keyboard();
		mouse = new Mouse();

		// LOOKING FOR EXISTING PROFILE
		FileManager.profileHandler();
		// SET DEVMODE
		if (FileManager.profileContains("set_devmode"))
			if (FileManager.getProfileDataAsInt("set_devmode") == 1)
				this.devMode = true;
		
		addKeyListener(key);
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
	}
	
	private void freshGame() {
	//	if (stage == null && gameState == 5) 
		freshGame(0);
	}
	
	private void freshGame(int s) {
		if (s >= 0 && s <= Stage.getMaxStage()) {
			Screen.killAllWindows();
			newGameState = 5;
			FileManager.load();
			FileManager.createStatisticsFile();
			FileManager.createCombatLogFile();
			
			CombatLog.init();

			stage = new Stage(key, mouse, s);
			gameplay = new Gameplay(stage);
			System.out.println("Gameplay control is running.");
			System.out.println("Statistics collection is running.");
			
			gameplay.setFirst();
		} else {
			System.err.println("ERROR: Stage " + s + " does not exist. Your profile might be corrupted or outdated.");
			showMessage("Stage #" + s + " does not exist.", 0, 2, TOPLEFT, false);
		}
			
	}

	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
		requestFocus();
				
			// EXISTING PROFILE
		if (FileManager.getProfileExists() && !FileManager.getProfileData("name").equals("default")) {
			showMessage("Profile data found: " + FileManager.mapProfile.get("name"), 0, 3, TOPLEFT);
			FileManager.setProfileData("lastseen", FileManager.formatDate(new Date()));
			
			// initializers
			FileManager.errorChecking();
			initializeProfileName();
			updateStageSelectionMenu();

			// NEW PROFILE
		} else if (FileManager.getProfileExists() && FileManager.getProfileData("name").equals("default")) {
			showMessage("No profile found, a new one will be created.", 0, 3, TOPLEFT);
			// ERROR HANDLING
		} else {
			System.err.println("ERROR: profile error.");
		}
		
		
		
		// SET STAGE.UNLOCKEDSTAGECOUNT BASED ON PROFILE
		updateUnlockedStages();
		
		
		
		Text.fillColorList();
		launchMainMenu();
		versionInfo();
		// PROFILE: "technical" string here (no profile found or xy loaded), display for 2-3 seconds only
		
	}


	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 60.0;
		double delta = 0;
		int frames = 0, updates = 0;

		requestFocus();
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				update();
				updates++;
				delta--;
			}
			
			render();
			frames++;
			
			if (fpsLock) try{Thread.sleep((lastTime-System.nanoTime() + (long) ns) / 1000000);} catch(Exception e) {};

			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				frame.setTitle(title + " " + version + " [" + gameStateString[gameState] +"] | " + updates + " ups, " + frames
						+ " fps" );
				updates = 0;
				frames = 0;
			}
		}
		stop();
	}

	public void update() {
		// GLOBAL NOTIFS
		for (int i = 0; i < notifications.size(); i++) {
			notifications.get(i).update();
		}
		removeNotifications();
		
		// UPDATE MENU LOGIC
		if (gameState == 0) updateMainMenu();
		else if (gameState == 1) updateStageSelectionMenu();
		
		if (onCooldown && System.nanoTime() > cdEnd) onCooldown = false;
		
		// 0: MAIN MENU
		if (newGameState == 0 && gameState != 0) {
			if (stage != null) clearStage();
			launchMainMenu();
			gameState = 0;
		}
		
		// 1: STAGE SELECTION
		if (newGameState == 1 && gameState != 1) {
			launchStageSelectionMenu();
			gameState = 1;
		}
		
		// 5: NEW GAME
		if (newGameState == 5 && gameState != 5) {
			if (stage == null) freshGame();
			gameState = 5;
		}
		
		// CATCH 'RETURN-TO-MENU' REQUEST FROM INGAME
		// TODO: careful with saving [differentiate between win and loss!] when impementing profile code!
		if (gameplay != null)
			if (gameplay.isAskingForQuit()) newGameState = 0;
		
		// TEXT INPUT UPDATE FOR PROFILENAME
		if (Screen.windowExists("yourName"))
			updateTextInputForProfileNaming("yourName", Screen.getWindow("yourName").getInputField(), Screen.getWindow("yourName").getInputFieldString());
		
		screen.update();
		
		// don't forget to drop the other objects' update() methods here
		key.update();
		
		if (stage != null) stage.update();
		if (gameplay != null) gameplay.update();
		
		// KEY INPUT
//		if (key.debugForceNewWave) newGameState = 5;
		if (key.debugAddMonster) {
		//	Screen.createWindow(150, 150, 250, 150, 0xffffffff, false, "ExitConfirm");
		//	Screen.getWindow("exitconfirm").add("Are you sure you want to quit this game?");
	//		newGameState = 0;
		}
	}

	private void updateTextInputForProfileNaming(String windowName, List<Character> inputField, String inputFieldStr) {
		if (textInputEnabled && Screen.windowExists(windowName)) {
			for (int i = 0; i < 26; i++) {
				if (key.alphabet[i] && !onCooldown && inputField.size() < 10) {
					inputField.add((char) (i + 65));
					enableCooldown(125);
				} else if (key.removeLast && !onCooldown && inputField.size() > 0) {
					inputField.remove(inputField.size() - 1);
					enableCooldown(125);
				} else if (key.playerEndTurn && !onCooldown) {
					inputFieldStr = "";
					for (int k = 0; i < inputField.size(); i++)
						inputFieldStr += inputField.get(i);
					
					enableCooldown(300);
					
					boolean isValidName = true;
					if (inputFieldStr.length() < 2) isValidName = false;
					for (int k = 0; k < LIST_OF_BANNED_NAMES.length; k++) {
						if (inputFieldStr.toLowerCase().equals(LIST_OF_BANNED_NAMES[k])) isValidName = false;
					}
					
					if (isValidName) {
						System.out.println("Entry \"" + inputFieldStr.toLowerCase() + "\" submitted.");
						Screen.getWindow(windowName).closeTextInput();
						Screen.getWindow(windowName).askForRemoval();
						
						FileManager.setProfileData("name", inputFieldStr.toLowerCase());
						initializeProfileName();
						launchMainMenu();						
					} else {
						showMessage("ERROR: invalid name!", 0, 2, TOPLEFT, false);
					}
				}
			}
		}
	}

	private void updateMainMenu() {
		handleOptionSelection(0);
		
		if (menuOptionSelected == 0 && key.playerEndTurn && !onCooldown) {
			freshGame(FileManager.getProfileDataAsInt("continuestage"));
			showMessage("Loading stage... ", 0, 2, TOPLEFT, true);
		}
		if (menuOptionSelected == 1 && key.playerEndTurn && !onCooldown) {
			showMessage("WARNING: Stage selection is not available in the current build.", 0, 1, TOPLEFT);
			newGameState = 1;
			enableCooldown(300);
		}
		if (menuOptionSelected == 2 && key.playerEndTurn && !onCooldown) {
			showMessage("WARNING: Settings is not available in the current build.", 0, 1, TOPLEFT);
			enableCooldown(300);
		}
		if (menuOptionSelected == 3 && key.playerEndTurn && !onCooldown) {
			System.exit(0);
		}
	}
	
	private void updateStageSelectionMenu() {
		handleOptionSelection(1);
		if (stageSelected < 10) selectionString = "0" + stageSelected;
		else selectionString = stageSelected + "";
		
		if (key.playerEndTurn && !onCooldown) {
			freshGame(stageSelected);
			enableCooldown(300);
		}
	}

	private void handleOptionSelection(int menu) {
		switch(menu) {
			case 0: {
				// DOWN AND UP
				if (key.radialChoice[2] && menuOptionSelected < 3 && !onCooldown) {
					menuOptionSelected++;
					enableCooldown(300);
				} else if (key.radialChoice[0] && menuOptionSelected > 0 && !onCooldown) {
					menuOptionSelected--;
					enableCooldown(300);
				}
				
				// TOP AND BOTTOM
				if (key.radialChoice[0] && menuOptionSelected == 0 && !onCooldown) {
					menuOptionSelected = 3;
					enableCooldown(300);
				} else if (key.radialChoice[2] && menuOptionSelected == 3 && !onCooldown) {
					menuOptionSelected = 0;
					enableCooldown(300);
				}
				break;
			}
			
			
			case 1: {
				// TODO: 	stage unlock is now strictly linear, so if you set an unlock pattern "1001" the
				//				two zeroes will be unlocked as well, as it only checks for the last one!
				
				int localMax;
				if (devMode) localMax = Stage.getMaxStage();
				else localMax = Stage.getUnlockedStageCount() - 1;
				
				// LEFT AND RIGHT
				if (key.radialChoice[3] && stageSelected > 0 && !onCooldown) {
					stageSelected--;
					enableCooldown(300);
				} else if (key.radialChoice[1] && stageSelected < localMax && !onCooldown) {
					stageSelected++;
					enableCooldown(300);
				}
				
				// MIN AND MAX
				if (key.radialChoice[3] && stageSelected == 0 && !onCooldown) {
					stageSelected = localMax;
					enableCooldown(300);
				} else if (key.radialChoice[1] && stageSelected == localMax && !onCooldown) {
					stageSelected = 0;
					enableCooldown(300);
				}
				
				// BACK
				if (key.playerExitToMenu && !onCooldown)
					newGameState = 0;
				
				break;
			}
			default: System.err.println("ERROR");
		}
	}
	
	private void enableCooldown(int dur) {
		// duration in ms
		onCooldown = true;
		cdDuration = dur * 1000000L;
		cdStart = System.nanoTime();
		cdEnd = cdStart + cdDuration;
	//	System.out.println(cdStart + " " + cdDuration + " " +  cdEnd);
	}

	private void launchMainMenu() {
		// NEW GAME || CONTINUE GAME?
		String firstItemStr;
		if (FileManager.getProfileExists() && FileManager.getProfileDataAsBooleanArray("stagescompleted")[0])
			firstItemStr = MAIN_MENU_STRINGS[1];
		else firstItemStr = MAIN_MENU_STRINGS[0];
		
		if (FileManager.getProfileExists() && FileManager.mapProfile.get("name").equals("default")) {
			Screen.killAllWindows();
			Screen.createWindow(280, 120, 200, 150, 0, true, "Your Name");
			Screen.getWindow("yourName").add("  YOUR NAME: ");
			Screen.getWindow("yourName").add(10);
		} else if (FileManager.getProfileExists()){
			enableCooldown(300);
			Screen.killAllWindows();
			Screen.createWindow(280, 120, 200, 150, 0, true, "Main Menu");
			Screen.getWindow("mainMenu").add("- MAIN MENU -"
					+ "\n\n  " + firstItemStr
							+ "\n\n  " + MAIN_MENU_STRINGS[2]
									+ "\n\n  " + MAIN_MENU_STRINGS[3]
											+ "\n\n  " + MAIN_MENU_STRINGS[4]
					);
			
			// "greeting" (non-technical) profile notification here: Welcome back, XY or Welcome!
			showMessage("Welcome, " + profileName + "!", 2, 10, BOTTOMLEFT, true);			
		} else {
			boolean localCheck = false;
			do {
				System.out.println("Waiting for profile...");
				if (FileManager.getProfileExists()) localCheck = true;				
			} while (!localCheck);
			
			if (localCheck) launchMainMenu();
		}
		
	}
	
	private void launchStageSelectionMenu() {
		enableCooldown(300);
		Screen.killAllWindows();
		Screen.createWindow(280, 120, 200, 150, 0, true, "Select Stage");
		Screen.getWindow("selectStage").add("- STAGE SEL -");
	}
	
	public void renderSelectionMarker() {
		int[] sel = {156, 172, 188, 204};
		for (int i = 0; i < sel.length; i++) {
			if (menuOptionSelected == i) new Text().render(283, sel[i], -8, 0xffffffff, Text.font_default, 1, ">", screen);
		}
	}

	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();

		screen.clear();
		
		
		if (stage != null)  {
			screen.render(stage);
			stage.render(screen);
			screen.renderWindows(stage);
		} else {
			screen.render(gameState);
		}
			
			// GLOBAL NOTIFS
			for (int i = 0; i < notifications.size(); i++) {
				notifications.get(i).render(screen);
	
			// TODO: fix this so you won't have to do this in order to render text
			if (gameState == 0) {
				renderSelectionMarker();
			} else if (gameState == 1) {
				new Text().render(261, 137, 10, 0xffffffff, Text.font_kubastaBig, 1, selectionString, screen);
			}
		}
		
		if (gameplay != null) gameplay.render(screen);

		// don't forget to drop the other objects' render() methods here

		for (int i = 0; i < pixels.length; i++)
			pixels[i] = screen.getPixels()[i];

		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		g.dispose();
		bs.show();
	}
	
	public static void changeStage(Stage s) {
		stage = null;
		stage = s;
	}

	public static int getScale() {
		return scale;
	}

	public static int getRenderWidth() {
		return width;
	}

	public static int getRenderHeight() {
		return height;
	}

	public static Gameplay getGameplay() {
		return gameplay;
	}
	
	public static String getTitle() {
		return title;
	}
	
	public static String getVersion() {
		return version;
	}
	
	public static String getProjectStage() {
		return projectStage;
	}
	
	public static boolean isFpsLocked() {
		return fpsLock;
	}
	
	public static boolean isTextInputEnabled() {
		return textInputEnabled;
	}
	
	public static void setTextInputEnabled(boolean b) {
		textInputEnabled = b;
	}
	
	public static String isFpsLockedString() {
		if(Game.isFpsLocked()) return "60FPS";
		else return "FPS UNLOCKED";
	}
	
	public static void toggleFpsLock() {
		if (fpsLock) fpsLock = false;
		else if (!fpsLock) fpsLock = true;
	}
	
	public void resetStage() {
		clearStage();
		createStage();
	}
	
	public void clearStage() {
		CombatLog.println("Game ended by player.\n");
//		showMessage("Game ended by player.", 0, 2, TOPLEFT, false);
		
		gameplay = null;

		stage.killAll();
		stage = null;
	}
	
	public void createStage() {
		freshGame();
	}

	public static void updateUnlockedStages() {
		boolean[] stagesUnlocked = FileManager.getProfileDataAsBooleanArray("stagesunlocked");
		if (!stagesUnlocked[0]) {
			FileManager.setProfileData("stagesunlocked", true, 0);
		}
		for (int i = 0; i < stagesUnlocked.length; i++)
			if (stagesUnlocked[i]) Stage.setUnlockedStageCount(i+1);
		System.out.println("HIGHEST AVAILABLE STAGE: " + Stage.getUnlockedStageCount());
		
		// also update completed here (TODO: move it? make an universal one?)
	}
	
	private void removeNotifications() {
		for (int i = 0; i < notifications.size(); i++) {
			if (notifications.get(i).getNeedsRemoved()) {
				notifications.remove(i);
				System.out.println("Notification removed");
			}
		}
	}
	
	private void versionInfo() {
		showMessage("      " + title + " " + version, 0, -1, TOPRIGHT[0], false);
		showMessage("" + projectStage.toUpperCase(), 0, -1, TOPRIGHT[1], false);
		if (devMode) showMessage("    DEVELOPER MODE", 0, -1, BOTTOMRIGHT, false);
	}
	
	private void showMessage(String msg, int del, int dur, int[] pos) {
		showMessage(msg, del, dur, pos, false);
	}
	
	public static void showMessage(String msg, int del, int dur, int[] pos, boolean hideInGame) {
		// remove the previous notification on that specific spot
		for (int i = 0; i < notifications.size(); i++)
			if (notifications.get(i).getPos()[0] == pos[0] &&
					notifications.get(i).getPos()[1] == pos[1]) notifications.get(i).setToRemove();
		
		notifications.add(new Notification(msg, del, dur, Text.font_default, 0xfffffff, true, pos[0], pos[1], hideInGame));
	}

	public static byte getGameState() {
		return gameState;
	}
	
	// INITIALIZERS (PROFILE VALUES)
	private void initializeProfileName() {
		profileName = ((char) (FileManager.mapProfile.get("name").toLowerCase().charAt(0) - 32)) + FileManager.mapProfile.get("name").substring(1, FileManager.mapProfile.get("name").length());
	}
	
	public static void main(String[] args) {	
		Game game = new Game();
		game.frame.setResizable(false);
		game.frame.setTitle(title);
		game.frame.add(game);
		game.frame.pack();
		game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.frame.setLocationRelativeTo(null);
		game.frame.setVisible(true);

		game.start();		
	}
}
