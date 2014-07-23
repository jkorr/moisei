package com.daenils.moisei;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.Graphics;

import javax.swing.JFrame;

import com.daenils.moisei.graphics.Screen;

public class Game extends Canvas implements Runnable {
	
	private static final long serialVersionUID = 1L;
	private static int scale = 1;
	private static int width = 1280 * scale;
	private static int height = (width / 16 * 9) * scale;
	private static String title = "Moisei";
	
	private Thread thread;
	private JFrame frame;
	
	private Screen screen;
	
	private boolean running = false;
	
	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

public Game() {
	Dimension size = new Dimension(width * scale, height * scale);
	setPreferredSize(size);
	
	screen = new Screen(width, height);	
	frame = new JFrame();
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
	System.out.println("Rendering screen at the resolution of " + (width * scale) + "x" + (height * scale) + ".");

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
			frame.setTitle(title + " | " + updates + " ups, " + frames + " fps");
			updates = 0;
			frames = 0;
		}
	}
	stop();
}

public void update() {
// don't forget to drop the other objects' update() methods here
}

public void render() {
	BufferStrategy bs = getBufferStrategy();
	if (bs == null) {
		createBufferStrategy(3);
		return;
	}
	
	screen.clear();
	screen.render();
	
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
