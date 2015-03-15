package com.daenils.moisei.graphics;

import java.util.HashMap;
import java.util.Map;

import javax.swing.colorchooser.ColorSelectionModel;

import com.daenils.moisei.Game;

public class Notification {
    protected int x, y;
    protected int id;
    private static int nextId = 0;
    protected String text;
    protected String text10;
    protected boolean needsRemoval = false;

    protected long duration, delay;
    private long startTime;
	private long endTime;
   
    private Text font = new Text();
    protected Sprite[] f;
    protected int color, spacing;
    protected boolean hasShadow, showOnlyInMenus;

    public Notification(String text, int del, int dur, Sprite[] f, int color, boolean hasShadow, int x, int y, boolean onlyMenus) {
        nextId++;
        if (del == 0) this.startTime = System.nanoTime();
        else this.startTime = System.nanoTime() + (del * 1000000000L);
        
        if (f == Text.font_kubastaBig) this.spacing = 4;
        else this.spacing = -8;
        
        this.id = nextId;
        this.x = x;
        this.y = y;
        
        this.text = text;
        if (text.startsWith("WARNING")) text = "W:" + text.split(":")[1];
        else text = "N:" + text;
        if (text.length() >= 10) this.text10 = text.toUpperCase().replaceAll(" ", "").substring(0, 10);
        else this.text10 = text.toUpperCase().replaceAll(" ", "");
        
        this.delay = del;
        
        if (dur != -1) this.duration = dur * 1000000000L;
        else this.duration = dur;
        
        
        this.color = color;
        this.f = f;
        this.hasShadow = hasShadow;
        this.showOnlyInMenus = onlyMenus;
        
        this.endTime = this.startTime + this.duration;

       printInfo();
    }
    
    public void update() {
    //	System.out.println((((System.nanoTime() - endTime) / 1000000000L) - 1) * -1);
        if (System.nanoTime() > endTime && this.duration != -1) {
            System.out.println("Notification " + id + " (\"" + text + "\") hidden.");
            this.setToRemove();
        }
        if (this.showOnlyInMenus && Game.getGameState() == 5) {
        	this.setToRemove();
        }
        	 
    }

    public void render(Screen screen) {
        if ((System.nanoTime() >= this.startTime && System.nanoTime() < endTime) || (this.duration == -1 && System.nanoTime() >= this.startTime)) {
        	
        	if (color == -1) {
        		if (this.hasShadow)
        			font.renderColored(x, y+1, spacing, 0xff555555, f, 1, text, screen);
        		
        		font.renderColored((x), y, spacing, color, f, 1, text, screen);                     		
        	}
        	else {
        		if (this.hasShadow)
        			font.render(x, y+1, spacing, 0xff555555, f, 1, text, screen);
        		font.render(x, y, spacing, color, f, 1, text, screen);
        	}
        }
    }
    
    public boolean getNeedsRemoved() {
    	return needsRemoval;
    }
    
    public void setToRemove() {
    	this.needsRemoval = true;
    }
 
    private void printInfo() {
    	if (this.duration == -1)
			System.out.println("Perma-notification created [" + this.id + ":" + startTime + ":" + duration + ":" + endTime + ":" + text10 + "]");
		else
			System.out.println("Notification created [" + this.id + ":" + startTime + ":" + (duration / 1000000) + "ms" + ":" + endTime + ":" + text10 + "]");
    }
    
    public int[] getPos() {
    	int[] value = {this.x, this.y};
    	return value;
    }

    public long getDuration() {
    	return this.duration;
    }
}
