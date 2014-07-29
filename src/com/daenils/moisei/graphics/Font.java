package com.daenils.moisei.graphics;

public class Font {
	
	private static Spritesheet font0 = new Spritesheet("/textures/gui/fontsheet.png", 182, 112, 14);
	private static Sprite[] characters = Sprite.split(font0);
	
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

	public void render(int x, int y, int spacing, int color, String text, Screen screen) {
		int line = 0;
		int xOffset = 0, yOffset = 0;
		for (int i = 0; i < text.length(); i++) {
			xOffset += 14 + spacing;
			char currentChar = text.charAt(i);
			int index = charIndex.indexOf(currentChar);
			if (currentChar == '\n') {line++; xOffset = 0;}
			if (index == -1) continue;
			screen.renderCharacter(x + xOffset, y + line * 20 + yOffset, characters[index], 1, color);
		}
	}
	
	public void render(int x, int y, String text, Screen screen) {
		render(x, y, 0, 0, text, screen);
	}

}
