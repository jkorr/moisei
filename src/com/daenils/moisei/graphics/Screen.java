package com.daenils.moisei.graphics;

import com.daenils.moisei.Game;

public class Screen {

	private int width, height;
	private int[] pixels;
	
	public Screen(int width, int height) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
	}
	
	public void render(Stage stage) {

		if (Game.isGUIrendered()) {
			renderStage(stage.getStage());
		renderGUI(GUI.screenSpellPos1, GUI.screenBottomElements+11, GUI.gui_spelldefQ);
		renderGUI(GUI.screenSpellPos2, GUI.screenBottomElements+11, GUI.gui_spelldefW);
		renderGUI(GUI.screenSpellPos3, GUI.screenBottomElements+11, GUI.gui_spelldefE);
		renderGUI(GUI.screenSpellPos4, GUI.screenBottomElements+11, GUI.gui_spelldefR);
//		renderGUI(GUI.screenTurninfoPos, GUI.screenBottomElements-15, GUI.gui_turninfo);
//		renderGUI(GUI.screenPlayerinfoPos, GUI.screenBottomElements-15, GUI.gui_playerinfo);
		renderGUI(0, GUI.screenBottomBack, GUI.gui_back);
		}
	}
	
	public void clear() {
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = 0;
	}
	
	public void clear2() {
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = 0xffff00ff;
	}
	
	public void renderStage(Stage stage) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				pixels[x + y * width] = stage.pixels[x + y * width];
	}
	
	public void renderGUI(int xp, int yp, GUI gui) {
		for (int y = 0; y < gui.height; y++) {
			int ya = y + yp;
			for (int x = 0; x < gui.width; x++) {
				int xa = x + xp;
				int col = 0xffff00ff;
				if (gui.pixels[x + y * gui.width] != col) pixels[xa + ya * width] = gui.pixels[x + y * gui.width];
				}
		}
	}
	
	public void renderSprite(int xp, int yp, Sprite sprite, int scale) {
		// TODO: implement sprite scaling via int scale (100 default would be nice)
		for (int y = 0; y < sprite.height; y++){
			int ya = y + yp;
			for (int x = 0; x < sprite.width; x++) {
				int xa = x + xp;
				int col = 0xffff00ff;
				
				if (sprite.pixels[x + y * sprite.width] != col) pixels[xa + ya * width] = sprite.pixels[x + y * sprite.width];
			}
		}
	}
	
	public void renderPixel(int xp, int yp, int color) {
		pixels[xp + yp* width] = color;
	}
	
	public void renderGUIWindow(int x, int y, int width, int height, int bgcolor) {
		for (int k = 0; k < height; k++)
			for (int i = 0; i < width; i++) {
				pixels[(x + i) + (y + k) * this.width] = bgcolor; 
			}
	}
	
	public void renderGUIWindowBorder(Window w, int horWidth, int verWidth, int horColor, int verColor) {
		// TOP
		renderVerLn(w.x, w.y, w.width, horWidth, horColor);
		
		// BOTTOM
		renderVerLn(w.x, w.y + w.height, w.width, horWidth, horColor);
		
		// LEFT
		renderHorLn(w.x, w.y, w.height, verWidth, verColor);
		
		// RIGHT
		renderHorLn(w.x + w.width, w.y, w.height + verWidth, verWidth, verColor);
	}
	
	public void renderGUIGrid(Window w, int columns, int rows, int horWidth, int verWidth, int horColor, int verColor) {
		// 2x2 for testing purposes only
		for (int i = 0; i < rows + 1; i++) {
			renderVerLn(w.x + 5, (w.y + 20) + i * 31, columns * 31, 1, verColor);			
		}
		
		for (int i = 0; i < columns + 1; i++) {
			renderHorLn((w.x + 5) + i * 31, (w.y + 20), rows * 31 + 1, 1, horColor);
		}
		
	}

	private void renderVerLn(int x, int y, int length, int width, int color) {
		for (int k = 0; k < width; k++)
			for (int i = 0; i < length; i++) {
				pixels[(x + i) + (y + k) * this.width] = color; 
			}
	}
	
	private void renderHorLn(int x, int y, int length, int width, int color) {
		for (int k = 0; k < width; k++)
			for (int i = 0; i < length; i++) {
				pixels[(x + k) + (y + i) * this.width] = color; 
			}
	}
	
	public void renderCharacter(int xp, int yp, Sprite sprite, double scale, int color) {
		for (int y = 0; y < sprite.height; y++){
			double ya = y / scale + yp;
			for (int x = 0; x < sprite.width; x++) {
				double xa = x / scale + xp;
				int col = sprite.pixels[x + y * sprite.width];
				
				if (col != 0xffff00ff) pixels[(int) xa + (int) ya * width] = color;
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
