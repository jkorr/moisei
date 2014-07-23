package com.daenils.moisei.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.daenils.moisei.Game;

public class GUI {
	
	private String path;
	private int width, height;
	protected int[] pixels;
	
	// GUI ELEMENTS
	// (Currently there's only the one, the dummy GUI in one file)
	private static GUI dummy_gui = new GUI("/textures/gui/dummy_gui.png");
	
	private GUI(String path) {
		this.path = path;
		this.width = Game.getRenderWidth();
		this.height = Game.getRenderHeight();
		load();
	}
	
	public GUI(int w, int h) {
		this.width = w;
		this.height = h;
		pixels = new int[w * h];
	}
	
	public void render() {
	}
	
	public void load() {
		pixels = new int[width * height];
		try {
			BufferedImage image = ImageIO.read(GUI.class.getResource(path));
			int w = image.getWidth();
			int h = image.getHeight();
			image.getRGB(0, 0, w, h, pixels, 0, w);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("GUI (" + GUI.class.getResource(path) + ") is loaded successfully.");
	}

	public static GUI getGUI() {
		return dummy_gui;
	}
	
}
