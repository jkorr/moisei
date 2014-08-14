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
import com.daenils.moisei.graphics.Font;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Stage; // probably it should be in its own package later (e.g. moisei.stage.stage)
import com.daenils.moisei.input.Keyboard;

public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;
	private static int scale = 1;
	private static int width = 1280 * scale;
	private static int height = (width / 16 * 9) * scale;
	private static String title = "Project Moisei";
	private static String version = "0.5";
	private static String projectStage = "internal alpha";
	private static boolean fpsLock = false;

	private Thread thread;
	private JFrame frame;

	private Screen screen;
	private Keyboard key;

	// I'll see if this works out, but I currently don't have any other idea
	// other than having this as static. I mean it only has ONE instance
	// under any given circumstances, so I guess no harm's done, right?
	private static Gameplay gameplay;
	private Gamestats gamestats;
	private FileManager filemanager;
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

		filemanager = new FileManager();
		FileManager.createStatisticsFile();
		FileManager.createCombatLogFile();

		stage = new Stage(Stage.st_1a, player);
		stage = Stage.getStage(); // currently needed for targeting to work,
									// might wanna look into it later

		System.out.println("Gameplay control is running.");
		
		gameplay = new Gameplay(key, stage);
/*
 * 		Later you might want to load the abilities only once, so:
 * 		Ability.load();	
 */

	//	dummyMonster = new Monster(); // WTF CODE?
		player = new Player(key, null);
		monsterAI = new MonsterAI(stage);
		// monster1.setDefaultTarget(player); // repeated due to lack of better
		// solution for now (chicken-egg issue otherwise)

		gamestats = new Gamestats(player, stage, dummyMonster);
		System.out.println("Statistics collection is running.");

		gameplay.setFirst();

		String temp_turninfo = "playerturn: " + gameplay.getIsPlayerTurn()
				+ " | monsterturn: " + gameplay.getIsMonsterTurn();

		addKeyListener(key);
		
		// Create statistics file (once per launch)

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
		// don't forget to drop the other objects' update() methods here
		key.update();
		stage.update();
		player.update();
		monsterAI.update();
		// dummyMonster.update();
		gamestats.update();
		gameplay.update();
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

		screen.clear();
		screen.render();

		stage.render(screen);

		player.render(screen);
		// dummyMonster.render(screen);

		gameplay.render(screen);

		// don't forget to drop the other objects' render() methods here

		for (int i = 0; i < pixels.length; i++)
			pixels[i] = screen.getPixels()[i];

		Graphics g = bs.getDrawGraphics();
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
		if(Game.isFpsLocked()) return "FPS LOCKED";
		else return "FPS UNLOCKED";
	}
	
	public static void toggleFpsLock() {
		if (fpsLock) fpsLock = false;
		else if (!fpsLock) fpsLock = true;
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
