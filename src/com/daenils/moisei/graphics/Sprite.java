package com.daenils.moisei.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class Sprite {
	private String path, name;
	protected int width, height;
	
	protected int[] pixels;
	// had to make it static unfortunately(?)
	private static Map<String, Sprite> mapSprites = new HashMap<String, Sprite>();
	
	/* 
	 * --------------
	 * GAME ASSETS
	 * 1. MONSTERS
	 * --------------
	 */

//	public static Sprite monster_demo = new Sprite("/textures/entities/monster_demo.png", 64, 104);
//	public static Sprite monster_demo2 = new Sprite("/textures/entities/monster_demo2.png", 64, 104);
	public static Sprite monster_demo3 = new Sprite("/textures/entities/monster_demo3.png", 64, 104);
	public static Sprite monster_demo4 = new Sprite("/textures/entities/monster_demo4.gif", 64, 104);
	public static Sprite unt = new Sprite("/textures/entities/unt.gif", 64, 104);
	public static Sprite monster_generic_dead = new Sprite("/textures/entities/monster_generic_dead.gif", 64, 104);
	
	/* 
	 * --------------
	 * 2. SPELLS
	 * --------------
	 */
	
	public static Sprite spell0 = new Sprite("/textures/gui/spells/spell0.png", 30, 30);
	public static Sprite spell1 = new Sprite("/textures/gui/spells/spell1.png", 30, 30);
	public static Sprite spell2 = new Sprite("/textures/gui/spells/spell2.png", 30, 30);
	public static Sprite spell3 = new Sprite("/textures/gui/spells/spell3.png", 30, 30);
	
	/* 
	 * --------------
	 * 3. WEAPONS
	 * --------------
	 */
	
	public static Sprite noweapon = new Sprite("/textures/gui/weapons/noweapon.png", 30, 30);
	public static Sprite weapon0 = new Sprite("/textures/gui/weapons/weapon0.png", 30, 30);
	public static Sprite weapon1 = new Sprite("/textures/gui/weapons/weapon1.png", 30, 30);
	public static Sprite weapon2 = new Sprite("/textures/gui/weapons/weapon2.png", 30, 30);
	public static Sprite weapon3 = new Sprite("/textures/gui/weapons/weapon3.png", 30, 30);
	
	/* 
	 * --------------
	 * 4. LETTERS
	 * --------------
	 */
	
	public static Sprite[] letter = Sprite.split(Spritesheet.lettersheet);
	
	public Sprite(String path, int w, int h) {
		this.width = w;
		this.height = h;
		this.path = path;
		this.name = path.split("/")[path.split("/").length - 1];
		
		if (path.contains(".gif")) mapSprites.put(this.name.split(".gif")[0],this);
		if (path.contains(".png")) mapSprites.put(this.name.split(".png")[0],this);

		
		load();
	}

	public Sprite(int[] pixels, int spriteWidth, int spriteHeight) {
		this.pixels = new int[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			this.pixels[i] = pixels[i];
		}
		this.width = spriteWidth;
		this.height = spriteHeight;
	}

	public void update() {	
	}

	public void load() {
		pixels = new int[width * height];
	//	System.out.println("HEY: " + Sprite.class.getResource(path));
		try {
			BufferedImage image = ImageIO.read(Sprite.class.getResource(path));

			int w = image.getWidth();
			int h = image.getHeight();
		
			image.getRGB(0, 0, w, h, pixels, 0, w);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Sprite (" + Sprite.class.getResource(path) + ") is loaded successfully.");
}
	
	public static Sprite[] split(Spritesheet sheet) {
		int amount = (sheet.getWidth() * sheet.getHeight()) / (sheet.spriteWidth * sheet.spriteHeight);
		Sprite[] sprites = new Sprite[amount];
		int current = 0;
		int[] pixels = new int[sheet.spriteWidth * sheet.spriteHeight];
		
		for (int yp = 0; yp < sheet.getHeight() / sheet.spriteHeight; yp++) {
			for (int xp = 0; xp < sheet.getWidth() / sheet.spriteWidth; xp++) {
				
				for (int y = 0; y < sheet.spriteHeight; y++) {
					for (int x = 0; x < sheet.spriteWidth; x++) {
						int xo = x + xp * sheet.spriteWidth;
						int yo = y + yp * sheet.spriteHeight;
						pixels[x + y * sheet.spriteWidth] = sheet.getPixels()[xo + yo * sheet.getWidth()];
					}
				}
				sprites[current++] = new Sprite(pixels, sheet.spriteWidth, sheet.spriteHeight);
			}
		}
		
		return sprites;
		 
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public static Sprite parseSprite(String string) {
//		System.out.println("Requesting sprite " + string + ":");
		return mapSprites.get(string);
	}
}
