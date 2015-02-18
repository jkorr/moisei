package com.daenils.moisei.graphics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.daenils.moisei.Game;
import com.daenils.moisei.entities.Gamestats;

public class GUI {
	private Screen screen;
	
	private String path;
	protected int x, y, width, height;
	protected int[] pixels;
	
	private Text font = new Text();
	
	// GUI WINDOW STUFF
	protected List<Window> windows = new ArrayList<Window>();
	protected Map<String, Integer> mapWindows = new HashMap<String, Integer>();
	protected static boolean noWindows;
	
	// GUI ELEMENTS
	
	public static GUI gui_back = new GUI("/textures/gui/gui_back.png", 640, 60);
	public static GUI gui_turninfo = new GUI("/textures/gui/gui_turninfo.png", 120, 60);
	public static GUI gui_playerinfo = new GUI("/textures/gui/gui_playerinfo.png", 185, 60);
	public static GUI gui_spelldefQ = new GUI("/textures/gui/gui_spell-defQ.png", 30, 30);
	public static GUI gui_spelldefW = new GUI("/textures/gui/gui_spell-defW.png", 30, 30);
	public static GUI gui_spelldefE = new GUI("/textures/gui/gui_spell-defE.png", 30, 30);
	public static GUI gui_spelldefR = new GUI("/textures/gui/gui_spell-defR.png", 30, 30);	
	
	// GUI POSITION HELPERS
	public static int screenBottomBack = Game.getRenderHeight()-GUI.gui_back.height;
	public static int screenBottomElements = Game.getRenderHeight()-GUI.gui_turninfo.height;
	public static int screenSpellPos1 = 10;
	public static int screenSpellPos2 = screenSpellPos1 + (1 * 5) + (1 * 30);
	public static int screenSpellPos3 = screenSpellPos1 + (2 * 5) + (2 * 30);
	public static int screenSpellPos4 = screenSpellPos1 + (3 * 5) + (3 * 30);
	public static int screenTurninfoPos = screenSpellPos1 + (3 * 5) + (4 * 60) + (1 * 25);
	public static int screenPlayerinfoPos = screenSpellPos1 + (3 * 5) + (4 * 60) + (2 * 25) + 120;
	
		
	
// (Currently there's only the one, the dummy GUI in one file)
//	public static GUI dummy_gui = new GUI("/textures/gui/dummy_gui.png", 1280, 720);
	
	
	private GUI(String path, int w, int h) {
		this.path = path;
		this.width = w;
		this.height = h;
		load();
	}
	
	public GUI(int w, int h) {
		this.width = w;
		this.height = h;
		pixels = new int[w * h];
	}
	
	public GUI(Screen screen) {
		this.screen = screen;
	//	createWindow(200, 120, 240, 120, 0xff4444ee, "Test Window", "Hello World! \n\n I am Daenil and this is my humble\nturn-based combat game, Project Moisei \n(working title only).\n\n\n HAVE FUN!");
	}

	// CREATE A CLEAN WINDOW
	// LEGACY CONSTRUCTOR (BEFORE ADDING isBorderless)
	public void createWindow(int x, int y, int width, int height, int bgColor, String title) {
		createWindow(x, y, width, height, bgColor, false, title);
	}
	
	public void createWindow(int x, int y, int width, int height, int bgColor, boolean isBorderless, String title) {
		Window winnie = new Window(screen, x, y, width, height, bgColor, isBorderless, title);
	//	winnie.add(1, Window.BUTTON_CLOSE);
	//	winnie.add(4, 1);
		mapWindows.put(winnie.name, windows.size());
		windows.add(winnie);
	}
	
	// CREATE A WINDOW WITH DISPLAYTEXT
	public void createWindow(int x, int y, int width, int height, int bgColor, String title, String displayText) {
		Window winnie = new Window(screen, x, y, width, height, bgColor, title);
		winnie.add(displayText);
		windows.add(winnie);
	}
	
	// CREATE A WINDOW FOR LETTERS
	public void createWindow(int x, int y, int width, int height, int bgColor, String title, int n) {
		Window winnie = new Window(screen, x, y, width, height, bgColor, title);
		winnie.type = n;
		windows.add(winnie);
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
	//		System.out.println(windows.get(i).needsClosing);
			if (windows.size() > 0 && windows.get(i).needsClosing)
				removeWindow(windows.get(i));
		}
	}
	
	public void render() {
		// DISPLAY ALL WINDOWS THAT EXIST
		for (int i = 0; i < windows.size(); i++) windows.get(i).render();
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
	
	public int getWindowCount() {
		return windows.size();
	}
	
	public Window getWindow(int n) {
		return windows.get(n);
	}
	
	public Window getWindow(String name) {
		if (windows.size() > 0)
			return windows.get(mapWindows.get(name));
		else return null;
	}
	
	public static boolean getNoWindows() {
		return noWindows;
	}
	
}
