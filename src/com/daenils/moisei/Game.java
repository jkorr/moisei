package com.daenils.moisei;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.daenils.moisei.entities.Gameplay;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Notification;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Stage; // probably it should be in its own package later (e.g. moisei.stage.stage)
import com.daenils.moisei.graphics.Text;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;

public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;
	private static int scale = 2;
	private static int width = 640;
	private static int height = (width / 16 * 9);
	private static String title = "      MOISEI";
	private static String version = "0.5.0";
	private static String projectStage = "f&f alpha";
	private static boolean fpsLock = true;
	private static byte gameState = 0, newGameState = 0; // -1: Blank; 0: Main Menu; 1-4: reserved for menus; 5: game
	

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
	private List<Notification> notifications = new ArrayList<Notification>();
	
	private int menuOptionSelected = -1;
	private boolean onCooldown = false;
	private long cdStart, cdEnd, cdDuration;
	private int stageSelected = 0;
	private String selectionString = stageSelected + "";

	public Game() {
		Dimension size = new Dimension(width * scale, height * scale);
		setPreferredSize(size);

		screen = new Screen(width, height);
		System.out.println("Rendering screen at the resolution of "
				+ (width * scale) + "x" + (height * scale) + ".");
		frame = new JFrame();
		
		key = new Keyboard();
		mouse = new Mouse();

		
		
		addKeyListener(key);
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
	}
	
	private void freshGame() {
	//	if (stage == null && gameState == 5) 
		freshGame(0);
	}
	
	private void freshGame(int s) {
		if (s >= 0 && s < Stage.MAX_STAGE) {
			Screen.killAllWindows();
			FileManager.load();
			FileManager.createStatisticsFile();
			FileManager.createCombatLogFile();
			
			CombatLog.println("A new game has started.");
			stage = new Stage(key, mouse, s);
			gameplay = new Gameplay(stage);
			System.out.println("Gameplay control is running.");
			System.out.println("Statistics collection is running.");
			
			gameplay.setFirst();
		} else
			System.err.println("Stage " + s + " does not exist.");
	}

	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
		requestFocus();
		
		Text.fillColorList();
		launchMainMenu();
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
				frame.setTitle(title + " " + version + " [" + gameState +"] | " + updates + " ups, " + frames
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
			System.out.println("0");
		}
		
		// 1: STAGE SELECTION
		if (newGameState == 1 && gameState != 1) {
			launchStageSelectionMenu();
			gameState = 1;
		}
		
		// 5: NEW GAME
		if (newGameState == 5 && gameState != 5) {
			freshGame();
			gameState = 5;
			System.out.println("5");
		}
		
		screen.update();
		
		// don't forget to drop the other objects' update() methods here
		key.update();
		
		if (stage != null) stage.update();
		if (gameplay != null) gameplay.update();
		
		
		// KEY INPUT
//		if (key.debugForceNewWave) newGameState = 5;
		if (key.debugAddMonster) newGameState = 0;
	}
	
	private void updateMainMenu() {
		handleOptionSelection(0);
		
		if (menuOptionSelected == 0 && key.playerEndTurn && !onCooldown) {
			newGameState = 5;
		}
		if (menuOptionSelected == 1 && key.playerEndTurn && !onCooldown) {
			System.out.println("Stage selection will be added later on.");
			newGameState = 1;
			enableCooldown(300);
		}
		if (menuOptionSelected == 2 && key.playerEndTurn && !onCooldown) {
			System.out.println("Settings will be added later on.");
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
		}
	}

	private void handleOptionSelection(int menu) {
		switch(menu) {
			case 0: {
				if (key.radialChoice[2] && menuOptionSelected < 3 && !onCooldown) {
					menuOptionSelected++;
					enableCooldown(300);
				} else if (key.radialChoice[0] && menuOptionSelected > 0 && !onCooldown) {
					menuOptionSelected--;
					enableCooldown(300);
				}
				break;
			}
			case 1: {
				if (key.radialChoice[3] && stageSelected > 0 && !onCooldown) {
					stageSelected--;
					enableCooldown(300);
				} else if (key.radialChoice[1] && stageSelected < 40 && !onCooldown) {
					stageSelected++;
					enableCooldown(300);
				}
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
		Screen.killAllWindows();
		String[] menuOptionString = {"New Game", "Continue", "Select Stage", "Settings", "Exit Game"};
		Screen.createWindow(280, 120, 200, 150, 0, true, "Main Menu");
		Screen.getWindow("mainMenu").add("- MAIN MENU -"
				+ "\n\n  " + menuOptionString[0]
				+ "\n\n  " + menuOptionString[2]
				+ "\n\n  " + menuOptionString[3]
				+ "\n\n  " + menuOptionString[4]
						);
		
		Notification welcome = new Notification("Welcome back, [playerName]!", 10, Text.font_default, 0xffffffff, true, 2, 346);
		notifications.add(welcome);
	}
	
	private void launchStageSelectionMenu() {
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
		} else {
			screen.render(gameState);
			// GLOBAL NOTIFS
			for (int i = 0; i < notifications.size(); i++) {
				notifications.get(i).render(screen);
			}
	
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
		
		gameplay = null;

		stage.killAll();
		stage = null;
	}
	
	public void createStage() {
		freshGame();
	}
	
	private void removeNotifications() {
		for (int i = 0; i < notifications.size(); i++) {
			if (notifications.get(i).getNeedsRemoved()) {
				notifications.remove(i);
				System.out.println("Notification removed");
			}
		}
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
