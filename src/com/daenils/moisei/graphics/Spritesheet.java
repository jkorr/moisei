package com.daenils.moisei.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Spritesheet {
	private String path;
	
	private int size;
	private int width, height;
	
	public int spriteWidth, spriteHeight;
	public int[] pixels;
	
	private Sprite[] sprites;
	
	// SPRITESHEETS
	// lettersheet:
	
	public static Spritesheet lettersheet = new Spritesheet("/textures/letters/letter-grid.png", 180, 180, 30, 30);
	
	public Spritesheet(String path, int w, int h, int sw, int sh) {
		this.path = path;
		width = w;
		height = h;
		size = width * height;
		pixels = new int[width * height];
		
		spriteHeight = sh;
		spriteWidth = sw;
		load();
	}
	
	private void load() {
		try {
		BufferedImage image = ImageIO.read(Spritesheet.class.getResource(path));
		width = image.getWidth();
		height = image.getHeight();
		image.getRGB(0, 0, width, height, pixels, 0, width);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Sprite[] getSprites() {
		return sprites;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getSize() {
		return size;
	}
	
	public int[] getPixels() {
		return pixels;
	}
}
