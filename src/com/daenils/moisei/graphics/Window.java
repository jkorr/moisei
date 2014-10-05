package com.daenils.moisei.graphics;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import com.daenils.moisei.input.Mouse;
import com.daenils.moisei.entities.Letter;
import com.daenils.moisei.entities.equipments.*;

public class Window {
	private Screen screen;
	protected int x, y, width, height;
	protected int bgColor;
	protected String title;
	protected Text font;
	protected String name;
	protected List<Equipment> contents = new ArrayList<Equipment>();
	protected List<Letter> letterContents = new ArrayList<Letter>();
	protected Equipment requestedItem, lastRequestedItem;
	
	protected int horGrid, verGrid;
	
	protected String displayText;
	protected String dialogueOption;
	
	protected boolean needsClosing;
	protected boolean hasGrid;
	protected boolean hasDisplayText;
	protected boolean hasDialogueOptions;
	protected boolean hasContent;
	protected boolean hasLetterContent;
	protected boolean mouseOverItem;
	
	// finals
	public static final int[] ITEM_POSITION1 = {6, 21};
	public static final int ITEM_OFFSET = 31;
	
	public static final String BUTTON_OK = "[OK]";
	public static final String BUTTON_CLOSE = "[Close]";
	public static final String BUTTON_YES = "[Yes]";
	public static final String BUTTON_NO = "[No]";
	
	public Window(Screen screen, int x, int y, int width, int height, int bgColor, String title) {
		this.screen = screen;
		
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
	
	public void render() {
		screen.renderGUIWindow(x, y, width, height, bgColor);
		screen.renderGUIWindow(x, y, width, height - (height - 17), bgColor / 2); 
		screen.renderGUIWindowBorder(this, 2, 2, 0xffffffff, 0xffffffff);
		if (this.hasGrid) {
			screen.renderGUIGrid(this, horGrid, verGrid, 1, 1, 0xffffffff, 0xffffffff);

			if (this.hasContent) {
				// RENDER THE ICONS FOR THE CONTENT 
				int line = 0, pos = 0;
				for (int i = 0; i < contents.size(); i++) {
					if (i % horGrid == 0 && i > 0) {
						line++;
						pos = 0;
					}
					screen.renderSprite(x + ITEM_POSITION1[0] + pos * ITEM_OFFSET, y + ITEM_POSITION1[1] + line * ITEM_OFFSET, contents.get(i).getIcon(), 0);
					renderContentInfo(i, pos, line);
					pos++;
				}
			}
			
			if (this.hasLetterContent) {
				// RENDER THE ICONS FOR THE CONTENT 
				int line = 0, pos = 0;
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
			//		renderContentInfo(i, pos, line);
					pos++;
				}
			}
		}
		
		
		renderTitleTop();
		if (displayText != null) renderDisplayText();
		if (dialogueOption != null) renderDialogueOption();
	}
	
	private void renderContentInfo(int i, int pos, int line) {
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
	
	public void shopRequestPurchase(Equipment equipment) {
		setRequestedItem(equipment);
	}

	private void renderTitleTop() {
		font.render((x-6) + 5, y + 5, -8, 0xffffffff, Text.font_default, 1.0, title + " (" + name + ")", screen);
	}
	
	private void renderDisplayText() {
		font.render((x-6) + 5, y + 5 + 15, -8, 0xffffffff, Text.font_default, 1, displayText, screen);
	}
	
	private void renderDialogueOption() {
		font.render(x - 6 + 20, height * 2 - 14, -8, 0xffffffff, Text.font_default, 1, dialogueOption, screen);
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
	
	public void setLastRequestedItem(Equipment e) {
		lastRequestedItem = e;
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
	
	public Equipment getLastRequestedItem() {
		return lastRequestedItem;
	}
	
	public boolean getMouseOverItem() {
		return mouseOverItem;
	}
}
