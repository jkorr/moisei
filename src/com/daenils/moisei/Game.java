package com.daenils.moisei;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.Graphics;

import javax.swing.JFrame;

import com.daenils.moisei.entities.Gameplay;
import com.daenils.moisei.entities.Monster;
import com.daenils.moisei.entities.Player;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.input.Keyboard;

public class Game extends Canvas implements Runnable {
	
	private static final long serialVersionUID = 1L;
	private static int scale = 1;
	private static int width = 1280 * scale;
	private static int height = (width / 16 * 9) * scale;
	private static String title = "Moisei";
	
	private Thread thread;
	private JFrame frame;
	
	private Screen screen;
	private Keyboard key;
	
	// I'll see if this works out, but I currently don't have any other idea
	// other than having this as static. I mean it only has ONE instance
	// under any given circumstances, so I guess no harm's done, right?
	private static Gameplay gameplay;
	private String temp_turninfo;
	
	private Player player;
	private Monster monster1;
	
	private boolean running = false;
	
	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

public Game() {
	Dimension size = new Dimension(width * scale, height * scale);
	setPreferredSize(size);
	
	screen = new Screen(width, height);	
	System.out.println("Rendering screen at the resolution of " + (width * scale) + "x" + (height * scale) + ".");
	frame = new JFrame();
	key = new Keyboard();
	

	monster1 = new Monster(580, 290, player);
	player = new Player(key, monster1);
	monster1.setDefaultTarget(player); // repeated due to lack of better solution for now (chicken-egg issue otherwise)

	gameplay = new Gameplay(key);
	gameplay.setFirst();

	String temp_turninfo = "playerturn: " + gameplay.getIsPlayerTurn() + " | monsterturn: " + gameplay.getIsMonsterTurn();

	
	System.out.println("Gameplay control is running.");
	
	addKeyListener(key);
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
		delta += ( now - lastTime ) / ns;
		lastTime = now;
		while (delta >= 1) {
			update();
			updates++;
			delta--;
		}
		render();
		frames++;
		
		if (System.currentTimeMillis() - timer > 1000) {
			timer += 1000;
			frame.setTitle(title + " | " + updates + " ups, " + frames + " fps | " + temp_turninfo);
			updates = 0;
			frames = 0;
		}
	}
	stop();
}

public void update() {
// don't forget to drop the other objects' update() methods here
	key.update();
	gameplay.update();
	player.update();
	monster1.update();
	
	// temporarily here
	temp_turninfo = "playerturn: " + gameplay.getIsPlayerTurn() + " | monsterturn: " + gameplay.getIsMonsterTurn();

}

public void render() {
	BufferStrategy bs = getBufferStrategy();
	if (bs == null) {
		createBufferStrategy(3);
		return;
	}
	
	screen.clear();
	screen.render();
	
	player.render();
	monster1.render(screen);
	
// don't forget to drop the other objects' render() methods here

	
	for (int i = 0; i < pixels.length; i++)
		pixels[i] = screen.getPixels()[i];
	
	Graphics g = bs.getDrawGraphics();
	g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
	
	g.dispose();
	bs.show();
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
