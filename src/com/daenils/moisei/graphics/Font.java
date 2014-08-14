package com.daenils.moisei.graphics;

import com.daenils.moisei.Game;

public class Font {

	
	private static Spritesheet font0 = new Spritesheet("/textures/gui/fontsheet2.png", 182, 112, 14);
	private static Spritesheet font1 = new Spritesheet("/textures/gui/fontsheet3.png", 728, 448, 56);
	private static Spritesheet font2 = new Spritesheet("/textures/gui/fontsheet-celticbit.png", 728, 448, 56);
	
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
	public Font() {
		
	}
	
	public void renderXCentered(int y, int spacing, int color, Sprite[] font, int scale, String text, Screen screen) {
		renderXCentered(0, y, spacing, color, font, scale, text, screen);
	}
	
	public void renderXCentered(int xO, int y, int spacing, int color, Sprite[] font, int scale, String text, Screen screen) {
		// first line gets the center position, second line deals with the padding inside the character sprites and the scaling
		int centerPos = (Game.getRenderWidth() / 2) - (text.length() * (spacing + scale)) * scale;
		int x = centerPos - ((spacing / scale + 20 / scale) + 10) - scale;
		x += xO;
		if (scale % 4 == 0 || scale == 1) x++;
		
	
		render(x, y, spacing, color, font, scale, text, screen);
	}
	
	// LEGACY METHOD FOR DEFAULT FONT & SIZE
	public void render(int x, int y, int spacing, int color, String text, Screen screen) {
		render(x,y,spacing,color,font_default, 1, text,screen);
	}
	
	// METHOD WITH FONT & SCALE SELECTION
	public void render(int x, int y, int spacing, int color, Sprite[] font, int scale, String text, Screen screen) {
		int line = 0;
		int xOffset = 0, yOffset = 0;
		for (int i = 0; i < text.length(); i++) {
			xOffset += 14 + spacing;
			char currentChar = text.charAt(i);
			int index = charIndex.indexOf(currentChar);
			if (currentChar == '\n') {line++; xOffset = 0;}
			if (index == -1) continue;
			screen.renderCharacter(x + xOffset, y + line * 12 + yOffset, font[index], scale, color);
		}
	}

}
