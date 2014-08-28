package com.daenils.moisei.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import com.daenils.moisei.Game;

public class Text {

	
	private static Spritesheet font0 = new Spritesheet("/textures/gui/fontsheet0.png", 65, 80, 5, 10);
	private static Spritesheet font1 = new Spritesheet("/textures/gui/fontsheet4.png", 728, 448, 56, 56);
//	private static Spritesheet font2 = new Spritesheet("/textures/gui/fontsheet-celticbit.png", 728, 448, 56);
	
	public static Sprite[] font_default = Sprite.split(font0);
	public static Sprite[] font_kubastaBig = Sprite.split(font1);
//	public static Sprite[] font_celticbit = Sprite.split(font2);
	
	private static String charIndex = "ABCDEFGHIJKLM" + //
										"NOPQRSTUVWXYZ" +
										"abcdefghijklm" + 
										"nopqrstuvwxyz" +
										"0123456789.,;" +
										":?!'\"/\\%|()-+" +
										"[]{}=&@$_#~§*" +
										"<>°€£ίχ";
	public Text() {
		
	}
	
	public void renderNew(String s, int x, int y, int col, String fontname, int size, Graphics g) {
		g.setFont(new Font(fontname, Font.PLAIN, size));
		g.setColor(new Color(col));
		g.drawString(s, x, y);
	}
	
	public void renderXCentered(int y, int spacing, int color, Sprite[] font, double scale, String text, Screen screen) {
		renderXCentered(0, y, spacing, color, font, scale, text, screen);
	}
	
	public void renderXCentered(int xO, int y, int spacing, int color, Sprite[] font, double scale, String text, Screen screen) {
		// first line gets the center position, second line deals with the padding inside the character sprites and the scaling
		int centerPos = 0;
	//	int centerPos = Game.getRenderWidth() / 2 - (int) ((text.length() * spacing) * scale) - spacing;
		centerPos = (-15) + ((Game.getRenderWidth() / 2) - (text.length() * (spacing + (int) scale)) * (int) scale);
	//	centerPos = ((Game.getRenderWidth() / 2) - (text.length() * ((int) (56 * scale) + spacing)));
		int x = (centerPos - (int) (14 / scale) - spacing);
		x += xO;
		if (scale % 4 == 0 || scale == 1) x++;
		
	
		render(x, y, spacing, color, font, scale, text, screen);
	}
	
	// LEGACY METHOD FOR DEFAULT FONT & SIZE
	public void render(int x, int y, int spacing, int color, String text, Screen screen) {
		render(x,y,spacing,color,font_default, 1.1, text,screen);
	}
	
	// METHOD WITH FONT & SCALE SELECTION
	public void render(int x, int y, int spacing, int color, Sprite[] font, double scale, String text, Screen screen) {
		int line = 0;
		int xOffset = 0, yOffset = 0;
		for (int i = 0; i < text.length(); i++) {
			xOffset += 14 + spacing;
			char currentChar = text.charAt(i);
			int index = charIndex.indexOf(currentChar);
			if (currentChar == '\n') {line++; xOffset = 0;}
			if (index == -1) continue;
			screen.renderCharacter(x + xOffset, y + line * 8 + yOffset, font[index], scale, color);
		}
	}

}
