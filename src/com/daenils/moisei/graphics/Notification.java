package com.daenils.moisei.graphics;

public class Notification {
    protected int x, y;
    protected int id;
    private static int nextId = 0;
    protected String text;
    protected boolean needsRemoval = false;

    protected long duration;
    long startTime;
	private long endTime;
   
    private Text font = new Text();
    protected Sprite[] f;
    protected int color;

    public Notification(String text, int duration, Sprite[] f, int color, int x, int y) {
        nextId++;
        this.startTime = System.nanoTime();
        
        this.id = nextId;
        this.x = x;
        this.y = y;
        this.text = text;
        this.duration = duration * 1000000000L;
        this.color = color;
        this.f = f;

        this.endTime = startTime + this.duration;
     //   printInfo();
    }

    public void update() {
    //	System.out.println((((System.nanoTime() - endTime) / 1000000000L) - 1) * -1);
        if (System.nanoTime() > endTime) {
            System.out.println("Notification " + id + " hidden.");
            this.setToRemove();
        }
    }

    public void render(Screen screen) {
        if (System.nanoTime() < endTime)
            font.render(x, y, -8, color, f, 1, text, screen);
    }

    public boolean getNeedsRemoved() {
    	return needsRemoval;
    }
    
    public void setToRemove() {
    	this.needsRemoval = true;
    }
 
    private void printInfo() {
    	System.out.println("start: " + startTime);
    	System.out.println("end: " + endTime);
    	System.out.println("dur: " + duration);
    	
    }
}
