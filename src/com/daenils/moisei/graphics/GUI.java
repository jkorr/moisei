package com.daenils.moisei.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.daenils.moisei.Game;

public class GUI {
	
	private String path;
	protected int x, y, width, height;
	protected int[] pixels;
	
	// GUI ELEMENTS
	
	public static GUI gui_back = new GUI("/textures/gui/gui_back.png", 1280, 180);
	public static GUI gui_turninfo = new GUI("/textures/gui/gui_turninfo.png", 240, 120);
	public static GUI gui_playerinfo = new GUI("/textures/gui/gui_playerinfo.png", 370, 120);
	public static GUI gui_spelldefQ = new GUI("/textures/gui/gui_spell-defQ.png", 120, 120);
	public static GUI gui_spelldefW = new GUI("/textures/gui/gui_spell-defW.png", 120, 120);
	public static GUI gui_spelldefE = new GUI("/textures/gui/gui_spell-defE.png", 120, 120);
	public static GUI gui_spelldefR = new GUI("/textures/gui/gui_spell-defR.png", 120, 120);	
	
	// GUI POSITION HELPERS
	public static int screenBottomBack = Game.getRenderHeight()-GUI.gui_back.height;
	public static int screenBottomElements = Game.getRenderHeight()-GUI.gui_turninfo.height;
	public static int screenSpellPos1 = 30;
	public static int screenSpellPos2 = 30 + (1 * 10) + (1 * 120);
	public static int screenSpellPos3 = 30 + (2 * 10) + (2 * 120);
	public static int screenSpellPos4 = 30 + (3 * 10) + (3 * 120);
	public static int screenTurninfoPos = 30 + (3 * 10) + (4 * 120) + (1 * 50);
	public static int screenPlayerinfoPos = 30 + (3 * 10) + (4 * 120) + (2 * 50) + 240;
	
		
	
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

	public static GUI getGUI(GUI g) {
		return g;
	}
	
}
