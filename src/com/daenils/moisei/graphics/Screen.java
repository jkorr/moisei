package com.daenils.moisei.graphics;

import com.daenils.moisei.Game;
import com.daenils.moisei.Stage;
import com.daenils.moisei.entities.Letter.Element;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class Screen {

	private int width, height;
	private int[] pixels;
	private String path;
	
	public static final int[] PALETTE_LIGHT = {0xffE5554C, 0xff4C70E5, 0xff82E54C, 0xffE5E5E5, 0xffffffff};
	public  static final int[] PALETTE_BASE = {0xffBF1B11, 0xff113ABF, 0xff4EBF11, 0xffBFBFBF, 0xffffffff};
	public  static final int[] PALETTE_DARK = {0xff592320, 0xff202D59, 0xff345920, 0xff595959, 0xffffffff};
	
	// GUI WINDOW STUFF
	protected static List<Window> windows = new ArrayList<Window>();
	protected static boolean noWindows;
	
	// GUI ELEMENTS
	// TODO: new letter inv art goes here probably
	
	public Screen(int width, int height) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
	}
	
	public void update() {
		// UPDATE NOWINDOWS
		if (windows.size() > 0) noWindows = false;
		else noWindows = true;
				
		// UPDATE WINDOWS
		for (int i = 0; i < windows.size(); i++) {
			windows.get(i).update();
		}
						
		// LOOK FOR REMOVAL
		for (int i = 0; i < windows.size(); i++) {
		// System.out.println(windows.get(i).needsClosing);
			if (windows.size() > 0 && windows.get(i).needsClosing)
				removeWindow(windows.get(i));
			}
		}
	
	public void render() {
		// render blue screen
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				pixels[x + y * width] = 0xff0000ff;
			}
	}
	
	public void render(Stage stage) {
			renderStage(stage);
	}
	
	public void renderWindows(Stage stage) {		
		// DISPLAY ALL WINDOWS THAT EXIST
		for (int i = 0; i < windows.size(); i++) windows.get(i).render(this);
	}
	
	public void render(int n) {
		renderMenu(n);
		
		// DISPLAY ALL WINDOWS THAT EXIST
		for (int i = 0; i < windows.size(); i++) windows.get(i).render(this);
	}
	
	public void renderMenu(int menuId) { 
		/* 0: main menu * 1: options * 2: spells * 3: equipment * 4: credits	 */
		int col = 0;
		switch(menuId) {
			case 0: {
				col = 0xff22221F;
				break;
			}
			case 1: {
				col = 0xff353630;
				break;
			}
		default: {
				col = 0xff1A4383;
				System.out.println("ERROR: " + menuId);
			}
		}
		
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
	/*			if ((x % 2 == 0 && y % 2 == 0))  pixels[x + y * width] = 0xff110033;
				else */ pixels[x + y * width] = col;
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
			for (int x = 0; x < width; x++) {
	/*			if ((x % 2 == 0 && y % 2 == 0))  pixels[x + y * width] = 0xff110033;
				else */ pixels[x + y * width] = stage.getBackground()[x + y * width];
			}
	}
	
	public void renderSprite(int xp, int yp, Sprite sprite, int scale) {
		// TODO: implement sprite scaling via int scale (100 default would be nice)
		for (int y = 0; y < sprite.height; y++){
			int ya = y + yp;
			for (int x = 0; x < sprite.width; x++) {
				int xa = x + xp;
				int col = 0xffff00ff, col2 = 0xffffff55;
				
				if (sprite.pixels[x + y * sprite.width] != col 
						&& sprite.pixels[x + y * sprite.width] != col2) pixels[xa + ya * width] = sprite.pixels[x + y * sprite.width];
			}
		}
	}
	
	public void renderElementalCircle(int xp, int yp, Element e, int phase) {
		int col = 0;
		Sprite sprite = Sprite.elementalCircle[phase];
		switch (e) {
		case FIRE: { col = 0xffE5554C; break;	 }
		case WATER: { col = 0xff4C70E5; break;	 }
		case EARTH: { col = 0xff82E54C; break;	 }
		case WIND: { col = 0xffE5E5E5; break;	 }
		}
		
		for (int y = 0; y < sprite.height; y++){
			int ya = y + yp;
			for (int x = 0; x < sprite.width; x++) {
				int xa = x + xp;
				int c = 0xffff00ff, c2 = 0xffffff55;
				
				if (sprite.pixels[x + y * sprite.width] != c 
						&& sprite.pixels[x + y * sprite.width] != c2) pixels[xa + ya * width] = sprite.pixels[x + y * sprite.width];
				if (sprite.pixels[x + y * sprite.width] == 0xffff55ff) pixels[xa + ya * width] = col;
			}
		}
	}
	
	public void renderSpriteAsColor(int xp, int yp, Sprite sprite, int scale, int c) {
		// TODO: implement sprite scaling via int scale (100 default would be nice)
		for (int y = 0; y < sprite.height; y++){
			int ya = y + yp;
			for (int x = 0; x < sprite.width; x++) {
				int xa = x + xp;
				int col = 0xffff00ff;
				
				if (sprite.pixels[x + y * sprite.width] != col) pixels[xa + ya * width] = c;
			}
		}
	}

	public void renderPixel(int xp, int yp, int color) {
		pixels[xp + yp* width] = color;
	}
	
	public void renderBorder(int x, int y, int w, int h, int borderwidth, int color) {
		for (int m = 0; m < borderwidth; m++) {
			for (int i = 0; i < h-m; i++) { 
				renderPixel(x+m, y+i, color);
				renderPixel(x+w-m, y+i, color);
				for (int k = 0; k < w+1-m; k++) {
					renderPixel(x+k, y+m, color);
					renderPixel(x+k, y+h-m, color);
				}
			}
		}
	}
	
	public void renderBgFill(int col) {
		for (int k = 0; k < Game.getRenderHeight(); k++)
			for (int i = 0; i < Game.getRenderWidth(); i++) {
				pixels[(0 + i) + (0 + k) * this.width] = col; 
			}
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
	
	// CREATE A CLEAN WINDOW
	// LEGACY CONSTRUCTOR (BEFORE ADDING isBorderless)
	public void createWindow(int x, int y, int width, int height, int bgColor, String title) {
		createWindow(x, y, width, height, bgColor, false, title);
	}
	
	public static void createWindow(int x, int y, int width, int height, int bgColor, boolean isBorderless, String title) {
		Window winnie = new Window(x, y, width, height, bgColor, isBorderless, title);
	//	winnie.add(1, Window.BUTTON_CLOSE);
	//	winnie.add(4, 1);
		windows.add(winnie);
	}
	
	// CREATE A WINDOW WITH DISPLAYTEXT
	public void createWindow(int x, int y, int width, int height, int bgColor, String title, String displayText) {
		Window winnie = new Window(x, y, width, height, bgColor, title);
		winnie.add(displayText);
		windows.add(winnie);
	}
	
	// CREATE A WINDOW FOR LETTERS
	public void createWindow(int x, int y, int width, int height, int bgColor, String title, int n) {
		Window winnie = new Window(x, y, width, height, bgColor, title);
		winnie.type = n;
		windows.add(winnie);
	}
	
	// WINDOW STUFF
		public void addWindow(Window w) {
			windows.add(w);
		}
		
		public void removeWindow(Window w) {
			System.out.print("\nWindow '" + w.name + "' is closed.");
			windows.remove(w);
		}

		
		public String newLnLeftPad(int n) {
			String returnString = "\n";
			for (int i = 0; i < n; i++) returnString = returnString.concat("\t");
			return returnString;  
		}
		
		public int getWindowCount() {
			return windows.size();
		}
		
		public Window getWindow(int n) {
			return windows.get(n);
		}
		
		public static Window getWindow(String name) {
			if (windows.size() > 0)
				for (int i = 0; i < windows.size(); i++) {
					if (windows.get(i).getName().equals(name)) return windows.get(i);
				}
			return null;
		}
		
		public static boolean getNoWindows() {
			return noWindows;
		}
		
		public static boolean windowExists(String name) {
			for (int i = 0; i < windows.size(); i++) {
				if (windows.get(i).getName().equals(name)) return true;
			}
			
//			if (mapWindows.containsKey(name)) return true;
			return false;
		}
	
	public static void killAllWindows() {
		for (int i = 0; i < windows.size(); i++) windows.remove(i);
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
