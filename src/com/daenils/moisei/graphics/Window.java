package com.daenils.moisei.graphics;

import java.awt.Font;
import com.daenils.moisei.input.Mouse;

public class Window {
	private Screen screen;
	protected int x, y, width, height;
	protected int bgColor;
	protected String title;
	protected Text font;
	protected String name;
	
	protected int horGrid, verGrid;
	
	protected String displayText;
	protected String dialogueOption;
	
	protected boolean needsClosing;
	protected boolean hasGrid;
	protected boolean hasDisplayText;
	protected boolean hasDialogueOptions;
	
	// finals
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
		if (Mouse.getX() > 2 * (x + width - 10) && Mouse.getX() < 2 * (x + width)
				&& Mouse.getY() > 2 * (y) && Mouse.getY() < 2 * (y + 10)
				&& Mouse.getB() == 1) this.needsClosing = true;
	}
	
	public void render() {
		screen.renderGUIWindow(x, y, width, height, bgColor);
		screen.renderGUIWindow(x, y, width, height - (height - 17), bgColor / 2); 
		screen.renderGUIWindowBorder(this, 2, 2, 0xffffffff, 0xffffffff);
		if (this.hasGrid) screen.renderGUIGrid(this, horGrid, verGrid, 1, 1, 0xffffffff, 0xffffffff);

		renderTitleTop();
		if (displayText != null) renderDisplayText();
		if (dialogueOption != null) renderDialogueOption();
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
}
