package com.daenils.moisei.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.entities.Entity;
import com.daenils.moisei.entities.Monster;
import com.daenils.moisei.entities.Player;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;

public class Stage {
	public static final int MAX_STAGE = 40;
	private Player player;
	
	// BACKGROUND
	private String bgPath;
	private int bgWidth, bgHeight;
	protected int[] background;
	
	protected int id, world, worldOverride;
	protected String title, description;
	protected String scriptfile;
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Monster> monsters = new ArrayList<Monster>();
	private boolean allDone;
	
	public Stage(Keyboard input, Mouse inputM, int id) {
		// SETTING STAGE
		this.id = id;
		this.world = id / 10;
		loadBackground("/textures/stages/map" + world + ".png");
		// ADDING PLAYER
		setPlayer(new Player(input, inputM, null, this));
	}
	
	public void loadBackground(String path) {
		System.out.println("\n@@@ STAGE: " + id + " | FILENAME: " + "map" + world + ".png @@@");
		this.bgPath = path;
		this.bgWidth = Game.getRenderWidth();
		this.bgHeight = Game.getRenderHeight();
		loadImage();
	}
	
	public void loadImage() {
		background = new int[bgWidth * bgHeight];
		try {
			BufferedImage image = ImageIO.read(Stage.class.getResource(bgPath));
			int w = image.getWidth();
			int h = image.getHeight();
			image.getRGB(0, 0, w, h, background, 0, w);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Stage (" + Stage.class.getResource(bgPath) + ") is loaded successfully.");
	}

	public void update() {
		player.update();
		
		if (this.player.getCurrentTarget() == null && monsters.size() > 0) {
			player.setDefaultTarget(monsters.get(0));
			System.out.print("\nPlayer has targeted the monster.");
		}
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).update();
		}
		
		for (int i = 0; i < getMonsters().size(); i++) {
			getMonsters().get(i).update();
		}
		
	//	System.out.println(getMonsters().size());
		remove();
		
		checkIfAllDone();
		endTurnIfAllDone();
		
	}
	
	public void render(Screen screen) {
		// render player
		player.render(screen);
		
		// maybe put stage render here? from Screen.java
		
		// render monsters
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).render(screen);
		}
		for (int i = 0; i < monsters.size(); i++) {
			monsters.get(i).render(screen);
		}
	}
	
	private void checkIfAllDone() {
		if (Game.getGameplay().getIsMonsterTurn()) {
			int n = 0;
			for(int i = 0; i < getMonsters().size(); i++) {
				if (getMonsters().get(i).getHealth() > 0 && getMonsters().get(i).getActionPoints() == 0) {
					n++;
				}
			}
	//		System.out.print("\n" + n);
		//	System.out.println("\n " + Game.getGameplay().monstersAlive);
			if (n == Game.getGameplay().getMonstersAlive()) allDone = true;
		}
		
	}
	
	private void endTurnIfAllDone() {
		if (allDone && Game.getGameplay().getMonstersAlive() > 0) {
				Game.getGameplay().monsterEndTurn();				
				allDone = false;
		} else if (allDone && Game.getGameplay().getMonstersAlive() <= 0) {
			Game.getGameplay().setPlayerTurn(true);
			Game.getGameplay().setMonsterTurn(false);
		}
	}
	
	public void add(Entity e) {
		e.init(this);
		if (e instanceof Monster) {
			monsters.add((Monster) e);
			// this should set the target if a new monster is added
		}
		else {
			entities.add(e);
		}
	}
	
	private void remove() {
			for (int i = 0; i < entities.size(); i++) {
				if (entities.get(i).getNeedsRemove()) {
					entities.remove(i);
					System.out.println("Entity removed");
				}
			}
			for (int i = 0; i < monsters.size(); i++) {
				if (monsters.get(i).getNeedsRemove()) {
					monsters.remove(i);
					CombatLog.println("Monster removed");
					}
			}
	}
	
	public List<Monster> getMonsters() {
		return monsters;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public boolean checkIfAllDead() {
		boolean returnValue = false;
		if (Game.getGameplay().getTurnCount() > 0) { 		// "Gamestats.turnCount > 0" is a temporary fix (?)
			int n = 0;
			for (int i = 0; i < monsters.size(); i++) {
				if (monsters.get(i).getHealth() <= 0) n++; 
			}
			if (n == monsters.size()) returnValue = true;
			else returnValue = false;
		}
//			System.out.println(returnValue);
			return returnValue;
	}
	
	public void setPlayer(Player p) {
		player = p;
		System.out.print("\nPlayer has been added to stage.");
	}
	
	public void forceRemove(Entity e) {
		monsters.remove(e);
		CombatLog.println("Monster removed");
	}
	


	public void killAll() {
		for (int i = 0; i < monsters.size(); i++)
			monsters.remove(i);
		
		Screen.killAllWindows();
	}
	
}
