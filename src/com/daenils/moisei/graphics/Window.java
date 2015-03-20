package com.daenils.moisei.graphics;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import com.daenils.moisei.Game;
import com.daenils.moisei.input.Mouse;
import com.daenils.moisei.entities.Letter;
import com.daenils.moisei.entities.equipments.*;

public class Window {
	protected int x, y, width, height;
	protected int bgColor;
	protected String title;
	protected Text font;
	protected String name;
	protected int type; // 0 - default, 1 - letters
	protected List<Equipment> contents = new ArrayList<Equipment>();
	protected List<Letter> letterContents = new ArrayList<Letter>();
	protected Equipment requestedItem, lastRequestedItem;
	protected int requestedLetterNum, lastRequestedLetter;
	protected Letter requestedLetter;
	
	protected int horGrid, verGrid;
	
	protected String displayText;
	protected String dialogueOption;
	private boolean clickedDialogueOption;
	
	protected boolean isBorderless;
	protected boolean needsClosing;
	protected boolean hasGrid;
	protected boolean hasDisplayText;
	protected boolean hasDialogueOptions;
	protected boolean hasContent;
	protected boolean hasLetterContent;
	protected boolean mouseOverItem;
	protected boolean textInputEnabled;
	
//	protected List<Character> listToRender; 
	protected List<Character> inputField = new ArrayList<Character>();
	protected String inputFieldString;
	
	// finals
	public static final int[] ITEM_POSITION1 = {6, 21};
	public static final int ITEM_OFFSET = 31;
	
	public static final String BUTTON_OK = "[OK]";
	public static final String BUTTON_CLOSE = "[Close]";
	public static final String BUTTON_YES = "[Yes]";
	public static final String BUTTON_NO = "[No]";
	
	
	public Window(int x, int y, int width, int height, int bgColor, String title) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.bgColor = bgColor;
		this.title = title;
		this.name = setName(title);
		
		font = new Text();
	}
	
	public Window(int x, int y, int width, int height, int bgColor, boolean isBorderless, String title) {
		this.isBorderless = isBorderless;
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.bgColor = bgColor;
		this.title = title;
		this.name = setName(title);
		
		font = new Text();
	}
	
	public void update() {
		// CHECK CONTENT
		if (contents.size() > 0) this.hasContent = true;
		else this.hasContent = false;
		
		if (letterContents.size() > 0) this.hasLetterContent = true;
		else this.hasLetterContent = false; 
		
		if (Mouse.getX() > 2 * (x + width - 10) && Mouse.getX() < 2 * (x + width)
				&& Mouse.getY() > 2 * (y) && Mouse.getY() < 2 * (y + 10)
				&& Mouse.getB() == 1) this.needsClosing = true;
	}
	
	public void render(Screen screen) {
		if (!this.isBorderless) {
			screen.renderGUIWindow(x, y, width, height, bgColor);
			screen.renderGUIWindow(x, y, width, height - (height - 17), bgColor / 2); 
			screen.renderGUIWindowBorder(this, 2, 2, 0xffffffff, 0xffffffff);	
			renderTitleTop(screen);
		}
		
		if (this.hasGrid) {
			
			// BG FOR GRID
			int line = 0, pos = 0;
			for (int i = 0; i < verGrid * horGrid; i++) {
				if (i % horGrid == 0 && i > 0) {
					line++;
					pos = 0;
				}
				screen.renderGUIWindow(x + ITEM_POSITION1[0] + pos * ITEM_OFFSET, y + ITEM_POSITION1[1] + line * ITEM_OFFSET, 30,30, bgColor);
				pos++;
			}
			
	//		screen.renderGUIWindow(x + 5, y + 20, width - 0, height - 0, bgColor);
			screen.renderGUIGrid(this, horGrid, verGrid, 1, 1, 0xff454545, 0xff454545);

			if (this.hasContent) {
				// RENDER THE ICONS FOR THE CONTENT 
				line = 0; pos = 0;
				for (int i = 0; i < contents.size(); i++) {
					if (i % horGrid == 0 && i > 0) {
						line++;
						pos = 0;
					}
					screen.renderSprite(x + ITEM_POSITION1[0] + pos * ITEM_OFFSET, y + ITEM_POSITION1[1] + line * ITEM_OFFSET, contents.get(i).getIcon(), 0);
					renderContentInfo(i, pos, line, screen);
					pos++;
				}
			}
			
			if (this.hasLetterContent) {
				// RENDER THE ICONS FOR THE CONTENT 
				line = 0; pos = 0;
				for (int i = 0; i < letterContents.size(); i++) {
					if (i % horGrid == 0 && i > 0) {
						line++;
						pos = 0;
					}
					// RENDER BORDER DEPENDING ON ELEMENT:
					
					for (int l = 0; l < letterContents.get(i).getIcon().height; l++) {
						for (int k = 0; k < letterContents.get(i).getIcon().width; k++) {
							screen.renderPixel(k + x + ITEM_POSITION1[0] + pos * ITEM_OFFSET, l + y + ITEM_POSITION1[1] + line * ITEM_OFFSET, letterContents.get(i).getFrame());
						}
					}
					
					screen.renderSprite(x + ITEM_POSITION1[0] + pos * ITEM_OFFSET, y + ITEM_POSITION1[1] + line * ITEM_OFFSET, letterContents.get(i).getIcon(), 0);
					
					// SELECTION MARKERS
					renderHollowBorder(line, pos, i, screen);
					
					refreshLetterContent(i, pos, line, screen);
					pos++;
				}
			}
			
		}
		
		if (this.inputField.size() > 0) {
			String listString = "";
			for (int i = 0; i < inputField.size(); i++)
				listString += inputField.get(i);
			
			font.render(x + 11, y + 31, -8, 0xffffffff, Text.font_default, 1, listString, screen);
		}
		
		
		if (displayText != null) renderDisplayText(screen);
		if (dialogueOption != null) renderDialogueOption(screen);
	}

	private void renderHollowBorder(int line, int pos, int i, Screen screen) {
		if (letterContents.get(i).getIsSelectedInRadialMenu()) screen.renderBorder(x + ITEM_POSITION1[0] + pos * ITEM_OFFSET, y + ITEM_POSITION1[1] + line * ITEM_OFFSET, 29, 29, 2, 0xffa800ff);
		if (letterContents.get(i).getIsSelected()) screen.renderBorder(x + ITEM_POSITION1[0] + pos * ITEM_OFFSET, y + ITEM_POSITION1[1] + line * ITEM_OFFSET, 29, 29, 2, 0xffffffff);
		
	/*	for (int l = 0; l < 2; l++) {
			for (int k = 0; k < letterContents.get(i).getIcon().width - 4; k++) {
				if (letterContents.get(i).getIsSelectedInRadialMenu()) screen.renderPixel(k + (x+2) + ITEM_POSITION1[0] + pos * ITEM_OFFSET, l + (y+2) + ITEM_POSITION1[1] + line * ITEM_OFFSET, 0xffff00ff);
				if (letterContents.get(i).getIsSelected()) screen.renderPixel(k + (x+2) + ITEM_POSITION1[0] + pos * ITEM_OFFSET, l + (y+2) + ITEM_POSITION1[1] + line * ITEM_OFFSET, 0xffffff00);
			}
		} */
	}
	
	private void renderContentInfo(int i, int pos, int line, Screen screen) {
	//	System.out.print("\n" + contents.get(i).getShowTooltip());
		if (Mouse.getX() > (x + ITEM_POSITION1[0] + pos * ITEM_OFFSET) * 2 && Mouse.getX() < (x + ITEM_POSITION1[0] + pos * ITEM_OFFSET) * 2 + 60
				&& Mouse.getY() > (y + ITEM_POSITION1[1] + line * ITEM_OFFSET) * 2 && Mouse.getY() < (y + ITEM_POSITION1[1] + line * ITEM_OFFSET) * 2+ 60) {
			font.render(x - 6 + 150, height * 2 - 21, -8, 0xffffffff, Text.font_default, 1, contents.get(i).getName(), screen);
			font.render(x - 6 + 150, height * 2 - 10, -8, 0xffffffff, Text.font_default, 1, "$:" + contents.get(i).getVendorPrice(), screen);
			if (Mouse.getB() == -1) contents.get(i).setShowTooltip(true);
			if (Mouse.getB() == 1) shopRequestPurchase(contents.get(i));
		}
		else contents.get(i).setShowTooltip(false);
	}
	
	private void refreshLetterContent(int i, int pos, int line, Screen screen) {
		if (Mouse.getX() > (x + ITEM_POSITION1[0] + pos * ITEM_OFFSET) * 2 && Mouse.getX() < (x + ITEM_POSITION1[0] + pos * ITEM_OFFSET) * 2 + 60
				&& Mouse.getY() > (y + ITEM_POSITION1[1] + line * ITEM_OFFSET) * 2 && Mouse.getY() < (y + ITEM_POSITION1[1] + line * ITEM_OFFSET) * 2+ 60) {
			if (Mouse.getB() == -1) {
				letterContents.get(i).setIsHoveredOver(true);
				font.render(5, 5, -8, 0xffff00ff, Text.font_default, 1, "\n" + letterContents.get(i).getValue()						
						+ ":" + letterContents.get(i).getId()
						+ ":" + i
						+ ":" + this.getRequestedLetterNum()
				//		+ ":" + Game.getGameplay().getStage().getPlayer().isConsonant(letterContents.get(i).getValue())
						, screen);
			//	System.out.print("\n" + letterContents.get(i).getValue());
			}
			if (Mouse.getB() == 1) { 
				setRequestedLetterNum(i);
				setRequestedLetter(letterContents.get(i));
				}
		}
		else letterContents.get(i).setIsHoveredOver(false);
	}
	
	public void shopRequestPurchase(Equipment equipment) {
		setRequestedItem(equipment);
	}

	private void renderTitleTop(Screen screen) {
		font.render((x-6) + 5, y + 5, -8, 0xffffffff, Text.font_default, 1.0, title + " (" + name + ")", screen);
	}
	
	private void renderDisplayText(Screen screen) {
		font.render((x-6) + 5, y + 5 + 15, -8, 0xffffffff, Text.font_default, 1, displayText, screen);
	}
	
	private void renderDialogueOption(Screen screen) {
		font.render(this.x, this.y + this.height - 10, -8, 0xffffffff, Text.font_default, 1, dialogueOption, screen);
		
		if(Mouse.getX() > this.x * 2
				&& Mouse.getX() < (this.x + 30) * 2
				&& Mouse.getY() > (this.y + this.height - 10) * 2
				&& Mouse.getY() < ((this.y + this.height - 10) + 7) * 2
				&& Mouse.getB() == 1) {
			setClickedDialogueOption(true);
		}
		else setClickedDialogueOption(false);
	}
	
	// ADDERS
	// ADD CONTENT
	public void add(String displayText) {
		this.displayText = displayText;
	//	System.out.println(displayText);
	}
	
	// ADD DIALOGUE OPTION
	public void add(int amountOfOptions, String dialogueOption) {
		// TODO: make it support more than one
		this.dialogueOption = dialogueOption;
	}
	
	// ADD GRID
	public void add(int columns, int rows) {
		this.setHorGrid(columns);
		this.setVerGrid(rows);
		this.hasGrid = true;
	}
	
	// ADD CONTENT (EQUIPMENT)
	public void add(Equipment e) {
		this.contents.add(e);
	}
	
	// ADD CONTENT (LETTERS)
		public void add(Letter l) {
			this.letterContents.add(l);
		}
		
	// ADD TEXT INPUT
		public void add(int entryLength) {
			Game.setTextInputEnabled(true);
		}
		
	// CLOSE TEXT INPUT
		public void closeTextInput() {
			Game.setTextInputEnabled(false);
		}
		
	// ADD ARRAYLIST CONTAINER (Char)
	//	public void add(List<Character> list) {
	//		listToRender = list;
	//	}
		
		
	// CLEAN CONTENTS (REMOVE ALL)
		public void clean() {
			for (int i = 0; i < this.letterContents.size(); i++)	this.letterContents.remove(i);
		}

	// SETTERS
	// GENERATE A NAME FOR THE WINDOW FROM ITS TITLE (WITHOUT SPACES AND LOWER CASE INITIAL LETTER)
	public String setName(String s) {
		String[] sComponents = s.split(" ");
		sComponents[0] = sComponents[0].toLowerCase();
		s = "";
		for (int i = 0; i < sComponents.length; i++) {
			s = s.concat(sComponents[i]);
		}
		return s;
	}
	
	public void setHorGrid(int n) {
		horGrid = n;
	}

	public void setVerGrid(int n) {
		verGrid = n;
	}

	public void setRequestedItem(Equipment e) {
		requestedItem = e;
	}
	
	public void setRequestedLetterNum(int l) {
		requestedLetterNum = l;
	}
	
	public void setRequestedLetter(Letter l) {
		requestedLetter = l;
	}
	
	public void setLastRequestedItem(Equipment e) {
		lastRequestedItem = e;
	}
	
	public void setLastRequestedLetter(int l) {
		lastRequestedLetter = l;
	}
	
	public void setLetterContents(List<Letter> list) {
		letterContents = list;
	}
	
	public void askForRemoval() {
		this.needsClosing = true;
	}
	
	// GETTERS
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getBgColor() {
		return bgColor;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getName() {
		return name;
	}
	
	public Equipment getRequestedItem() {
		return requestedItem;
	}
	
	public int getRequestedLetterNum() {
		return requestedLetterNum;
	}
	
	public Letter getRequestedLetter() {
		 return requestedLetter;
	}
	
	public Equipment getLastRequestedItem() {
		return lastRequestedItem;
	}
	
	public int getLastRequestedLetter() {
		return lastRequestedLetter;
	}
	
	public boolean getMouseOverItem() {
		return mouseOverItem;
	}

	public List<Letter> getLetterContents() {
		return letterContents;
		
	}

	public boolean getClickedDialogueOption() {
		return clickedDialogueOption;
	}
	
	public List<Character> getInputField() {
		return inputField;
	}
	
	public String getInputFieldString() {
		return inputFieldString;
	}
	
	public void setInputFieldString(String s) {
		inputFieldString = s;
	}
	
	public void addToInputFieldString(String s) {
		if (inputFieldString.length() > 0) inputFieldString.concat(s);
	}

	public void setClickedDialogueOption(boolean clickedDialogueOption) {
		this.clickedDialogueOption = clickedDialogueOption;
	}
}
