package com.daenils.moisei.graphics;

public class Screen {

	private int width, height;
	private int[] pixels;
	
	public Screen(int width, int height) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
	}
	
	public void render() {
		renderStage(Stage.getStage());
		renderGUI(GUI.getGUI());
		
	}
	
	public void clear() {
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = 0;
	}
	
	public void renderStage(Stage stage) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				pixels[x + y * width] = stage.pixels[x + y * width];
	}
	
	public void renderGUI(GUI gui) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int col = 0xffff00ff;
				if (gui.pixels[x + y * width] != col) pixels[x + y * width] = gui.pixels[x + y * width];
				}
		}
	}
	
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public int[] getPixels() {
		return pixels;
	}
	
}
