package com.daenils.moisei.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.daenils.moisei.Game;

public class Stage {
	
	private String path;
	private int width, height;
	
	private static int selector = (int) (Math.random() * 10);
	
	protected int[] pixels;
	
	// STAGES
	private static Stage st_demo = new Stage("/textures/stages/st_demo.png");
	private static Stage st_altdemo = new Stage("/textures/stages/st_altdemo.png");
	
	public Stage(String path) {
		this.path = path;
		this.width = Game.getRenderWidth();
		this.height = Game.getRenderHeight();
		load();
	}

	public void update() {
	}
	
	public void load() {
		pixels = new int[width * height];
		try {
			BufferedImage image = ImageIO.read(Stage.class.getResource(path));
			int w = image.getWidth();
			int h = image.getHeight();
			image.getRGB(0, 0, w, h, pixels, 0, w);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Stage (" + Stage.class.getResource(path) + ") is loaded successfully.");
	}
	
	// METHOD to pass a stage to the Screen class
	// Currently it already has a VERY BASIC "give me a random stage" feature, but its just for fun
	public static Stage getStage() {
		if (selector > 5) return st_demo;
		else return st_altdemo;
	}
}
