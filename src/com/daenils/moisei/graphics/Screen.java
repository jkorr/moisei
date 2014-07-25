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
		
		renderSpritesheet(15, 15, Spritesheet.fontsheet);
		
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
	
	public void renderSprite(int xp, int yp, Sprite sprite) {
		for (int y = 0; y < sprite.height; y++){
			int ya = y + yp;
			for (int x = 0; x < sprite.width; x++) {
				int xa = x + xp;
				int col = 0xffff00ff;
				
				if (sprite.pixels[x + y * sprite.width] != col) pixels[xa + ya * width] = sprite.pixels[x + y * sprite.width];
			}
		}
	}
	
	public void renderSpritesheet(int xp, int yp, Spritesheet spritesheet) {
		for (int y = 0; y < spritesheet.getHeight(); y++){
			int ya = y + yp;
			for (int x = 0; x < spritesheet.getWidth(); x++) {
				int xa = x + xp;
				int col = 0xffff00ff;
				
				if (spritesheet.pixels[x + y * spritesheet.getWidth()] != col) pixels[xa + ya * width] = spritesheet.pixels[x + y * spritesheet.getWidth()];
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
