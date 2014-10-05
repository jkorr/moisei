package com.daenils.moisei.entities;

import com.daenils.moisei.graphics.Sprite;

public class Letter {
	public enum Element {
		NEUTRAL, FIRE, WATER, EARTH, WIND;
	}

	protected char value;
	protected Element type; // 0: neutral 1: fire 2: water 3: earth 4: wind
	protected Sprite icon;
	protected int frameColor;
	
	public static Letter testletter = new Letter('G', Element.FIRE);
	
	public Letter() {
	}
	
	public Letter(char value, Element letterElement) {
		this.value = value;
		this.type = letterElement;
		this.icon = Sprite.letter[(int) value - 65];
		
		if (letterElement == Element.NEUTRAL) this.frameColor = 0xffffffff;
		else if (letterElement == Element.FIRE) this.frameColor = 0xffff0000;
		else if (letterElement == Element.WATER) this.frameColor = 0xff0000ff;
		else if (letterElement == Element.EARTH) this.frameColor = 0xff00ff00;
		else if (letterElement == Element.WIND) this.frameColor = 0xffbbbbbb;
		else this.frameColor = 0;
	
	}
	

	// GETTERS
	public Sprite getIcon() {
		return icon;
	}
	
	public int getFrame() {
		return frameColor;
	}

}
