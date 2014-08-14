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
	
	public void render() {
		renderStage(Stage.getStage());

		renderGUI(GUI.screenSpellPos1, GUI.screenBottomElements-30, GUI.gui_spelldefQ);
		renderGUI(GUI.screenSpellPos2, GUI.screenBottomElements-30, GUI.gui_spelldefW);
		renderGUI(GUI.screenSpellPos3, GUI.screenBottomElements-30, GUI.gui_spelldefE);
		renderGUI(GUI.screenSpellPos4, GUI.screenBottomElements-30, GUI.gui_spelldefR);
		renderGUI(GUI.screenTurninfoPos, GUI.screenBottomElements-30, GUI.gui_turninfo);
		renderGUI(GUI.screenPlayerinfoPos, GUI.screenBottomElements-30, GUI.gui_playerinfo);
		renderGUI(0, GUI.screenBottomBack, GUI.gui_back);
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
	
	public void renderCharacter(int xp, int yp, Sprite sprite, int scale, int color) {
		// TODO: implement sprite scaling via int scale (100 default would be nice)
		for (int y = 0; y < sprite.height; y++){
			int ya = y / scale + yp;
			for (int x = 0; x < sprite.width; x++) {
				int xa = x / scale + xp;
				int col = sprite.pixels[x + y * sprite.width];
				
				if (col != 0xffff00ff) pixels[xa + ya * width] = color;
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
