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

public class Stage {
	private Stage playStage;
	private Player player;
	
	private String path;
	private int width, height;
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Monster> monsters = new ArrayList<Monster>();
	
//	private static int selector = (int) (Math.random() * 10);
	
	protected int[] pixels;
	
	// STAGES
//	private static Stage st_demo = new Stage("/textures/stages/st_demo.png");
//	private static Stage st_altdemo = new Stage("/textures/stages/st_altdemo.png");
	public static Stage st1 = new Stage("/textures/stages/st1.png");
	public static Stage st1a = new Stage("/textures/stages/st1a.png");
	public static Stage st1b = new Stage("/textures/stages/st1b.png");
	public static Stage st1c = new Stage("/textures/stages/st1c.png");
	public static Stage stx = new Stage("/textures/stages/stx.png");
	
	public Stage(Stage stage) {
		this.playStage = stage;
	}
	
	public Stage(String path) {
		this.path = path;
		this.width = Game.getRenderWidth();
		this.height = Game.getRenderHeight();
		load();
	}

	public void update() {
		player.update();
		
		if (this.player == null && monsters.size() > 0) {
			player = (Player) (monsters.get(0).getCurrentTarget());
			System.out.print("\nPlayer has been added to stage.");
		}
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).update();
		}
		
	//	System.out.println(getMonsters().size());
		remove();
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
	
	public void load() {
		pixels = new int[width * height];
		try {
			BufferedImage image = ImageIO.read(Stage.class.getResource(path));
			int w = image.getWidth();
			int h = image.getHeight();
			image.getRGB(0, 0, w, h, pixels, 0, w);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Stage (" + Stage.class.getResource(path) + ") is loaded successfully.");
	}
	
	// METHOD to pass a stage to the Screen class
	// Currently it already has a VERY BASIC "give me a random stage" feature, but its just for fun
	public Stage getStage() {
		 return playStage;
			//	return st_1a;
	}
	
	public void setRandomStage() {
		int selector = (int) (Math.random() * 10);
		System.out.println(selector);
		if (selector > 5) playStage = st1c;
		else playStage = st1a;
//		if (selector > 1) return st_1;
//		resetAll();
	}
	
	public void setPlayer(Player p) {
		player = p;
	}
	
	public void forceRemove(Entity e) {
		monsters.remove(e);
		CombatLog.println("Monster removed");
	}
	
	public void resetAll() {
		playStage = null;
	}

	public void killAll() {
		for (int i = 0; i < monsters.size(); i++)
			monsters.remove(i);
	}
	
}
