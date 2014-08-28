package com.daenils.moisei.graphics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.daenils.moisei.Game;
import com.daenils.moisei.entities.Gamestats;

public class GUI {
	
	private String path;
	protected int x, y, width, height;
	protected int[] pixels;
	
	private Text font = new Text();
	
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
	
	public GUI() {
		
	}
	
	public void render(Graphics g) {
		// TEXT RENDERING WILL MOVE HERE FROM GAMEPLAY.JAVA
//		font.renderNew("Player H\tealth " + Game.getGameplay().getStage().getPlayer().getHealth(), Game.getRenderWidth(), Game.getRenderHeight(), 0, "Kubasta", 16, g);
		
	//	renderVersionInfo(g);
	//	g.drawLine(25, 25, 50, 25);
		
		
		
	}
	
	private void renderVersionInfo(Graphics g) {
		font.renderNew(Game.getTitle() + " " + Game.getVersion(), 1157, 10, 0, "Kubasta", 16, g);
		font.renderNew(newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.getProjectStage().length() + 1) + Game.getProjectStage().toUpperCase(), 1157, 22, 0, "Kubasta", 16, g);
		font.renderNew(Game.isFpsLockedString(), 1157, 34, 0, "Kubasta", 16, g);
	//	font.render(1147, 0, -8, 0, Game.getTitle() + " " + Game.getVersion()
	//			+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.getProjectStage().length() + 1) + Game.getProjectStage().toUpperCase()
	//			+ newLnLeftPad((Game.getTitle().length() + Game.getVersion().length()) - Game.isFpsLockedString().length() + 1) + Game.isFpsLockedString(), screen);
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

	public static GUI getGUI(GUI g) {
		return g;
	}
	
}
