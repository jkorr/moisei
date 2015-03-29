package com.daenils.moisei.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import com.daenils.moisei.Game;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.graphics.Sprite;

public class Letter {
	public enum Element {
		NEUTRAL, FIRE, WATER, EARTH, WIND, MAGIC;
	}
 	
	private static int idCount;
	protected int id;
	protected char value;
	protected Element type; // 0: neutral 1: fire 2: water 3: earth 4: wind
	protected Sprite icon;
	protected int frameColor;
	protected boolean isHoveredOver;
	protected boolean isSelected, isSelectedInRadialMenu;
	
	public static Letter testletter = new Letter('G', Element.FIRE);
	
	public Letter() {
	}

	public Letter(char value, Element letterElement) {
		this.id = idCount;
		idCount++;
		this.value = value;
		this.type = letterElement;
		this.icon = Sprite.letter[(int) value - 65];
		
		if (letterElement == Element.NEUTRAL) this.frameColor = 0xffffffff;
		else if (letterElement == Element.FIRE) this.frameColor = Screen.PALETTE_LIGHT[0];
		else if (letterElement == Element.WATER) this.frameColor = Screen.PALETTE_LIGHT[1];
		else if (letterElement == Element.EARTH) this.frameColor = Screen.PALETTE_LIGHT[2];
		else if (letterElement == Element.WIND) this.frameColor = Screen.PALETTE_LIGHT[3];
		else if (letterElement == Element.MAGIC) this.frameColor = 0xffaa00aa;
		else this.frameColor = 0;
	
	}
	
	// GETTERS
	public char getValue() {
		return value;
	}
	
	public Element getType() {
		return type;
	}
	
	public int getId() {
		return id;
	}
	
	public Sprite getIcon() {
		return icon;
	}
	
	public int getFrame() {
		return frameColor;
	}
	
	public boolean getIsHoveredOver() {
		return isHoveredOver;
	}
	
	public Letter getLetterById(int n) {
		if (this.id == n) return this;
		else return null;
	}
	
	public boolean getIsSelected() {
		return isSelected;
	}
	
	public boolean getIsSelectedInRadialMenu() {
		return isSelectedInRadialMenu;
	}
	
	// SETTERS
	public void setIsHoveredOver(boolean b) {
		isHoveredOver = b;
	}
	
	public void setIsSelected(boolean b) {
		isSelected = b;
	}
	
	public void setSelected() {
		isSelected = true;
	//	selectionId = Game.getGameplay().nextSelectionId;
	}
	
	public void setIsSelectedInRadialMenu(boolean b) {
		isSelectedInRadialMenu = b;
	}

}
