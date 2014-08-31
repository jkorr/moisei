package com.daenils.moisei;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.Graphics;
import java.util.concurrent.locks.Lock;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.daenils.moisei.entities.Gameplay;
import com.daenils.moisei.entities.Gamestats;
import com.daenils.moisei.entities.Monster;
import com.daenils.moisei.entities.MonsterAI;
import com.daenils.moisei.entities.Player;
import com.daenils.moisei.entities.equipments.Ability;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Text;
import com.daenils.moisei.graphics.GUI;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Stage; // probably it should be in its own package later (e.g. moisei.stage.stage)
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;

public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;
	private static int scale = 2;
	private static int width = 640;
	private static int height = (width / 16 * 9);
	private static String title = "Project Moisei";
	private static String version = "0.6";
	private static String projectStage = "f&f alpha";
	private static boolean fpsLock = false;
	private static boolean renderGUI;

	private Thread thread;
	private JFrame frame;

	private Screen screen;
	private Keyboard key;
	private Mouse mouse;

	// I'll see if this works out, but I currently don't have any other idea
	// other than having this as static. I mean it only has ONE instance
	// under any given circumstances, so I guess no harm's done, right?
	private static Gameplay gameplay;
	private GUI gui;
	private Gamestats gamestats;
//	private FileManager filemanager;
	private static Stage stage;

	private Player player;
	private MonsterAI monsterAI;
	private Monster dummyMonster;

	private boolean running = false;

	private BufferedImage image = new BufferedImage(width, height,
			BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
			.getData();

	public Game() {
		Dimension size = new Dimension(width * scale, height * scale);
		setPreferredSize(size);

		screen = new Screen(width, height);
		System.out.println("Rendering screen at the resolution of "
				+ (width * scale) + "x" + (height * scale) + ".");
		frame = new JFrame();
		
		key = new Keyboard();
		mouse = new Mouse();
		
		new FileManager();
		
		// Create statistics file (once per launch)
		FileManager.createStatisticsFile();
		// Create combat log file (once per launch)
		FileManager.createCombatLogFile();
		
		freshGame();
		
		addKeyListener(key);
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
	}
	
	private void freshGame() {
		freshGame(Stage.st1a);
	}
	
	private void freshGame(Stage s) {
		CombatLog.println("A new game has started.");
		stage = new Stage(s);
		gui = new GUI(screen); // needed for windows
		gameplay = new Gameplay(key, mouse, stage, this, gui);
		System.out.println("Gameplay control is running.");
		gamestats = new Gamestats(stage);
		System.out.println("Statistics collection is running.");
		monsterAI = new MonsterAI(stage);
		
//		Later you might want to load the abilities only once, so:
 //		Ability.load();

		renderGUI = true;
		gameplay.setFirst();
	}

	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
		requestFocus();
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
				frame.setTitle(title + " " + version + " | " + updates + " ups, " + frames
						+ " fps" );
				updates = 0;
				frames = 0;
			}
		}
		stop();
	}

	public void update() {
		// render GUI if the game is on
		if (player != null) renderGUI = true;
		
		// don't forget to drop the other objects' update() methods here
		key.update();
		if (stage != null) stage.update();
//		if (player != null) player.update();
		if (monsterAI != null) monsterAI.update();
		// dummyMonster.update();
		if (gamestats != null) gamestats.update();
		if (gameplay != null) gameplay.update();
		if (gui != null) gui.update();
		
		// KEY INPUT
		if (key.debugForceNewWave && stage != null) clearStage();
		if (key.debugAddMonster && stage == null) freshGame(Stage.st1a);

		
		// temporarily here
//		temp_turninfo = "playerturn: " + gameplay.getIsPlayerTurn()
//				+ " | monsterturn: " + gameplay.getIsMonsterTurn();

	}

	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();

		screen.clear();
		screen.render(stage);

		if (stage != null) stage.render(screen);
		if (gameplay != null) gameplay.render(screen);
		if (player != null) player.render(screen);
		if (gui != null) gui.render();
		// dummyMonster.render(screen);

		
		

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
	
	public static boolean isGUIrendered() {
		return renderGUI;
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
		renderGUI = false;
		
		gamestats = null;
		monsterAI = null;
		gui = null;
		gameplay = null;
		stage.resetAll();
		stage.killAll();
		stage = null;
	}
	
	public void createStage() {
		freshGame();
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
