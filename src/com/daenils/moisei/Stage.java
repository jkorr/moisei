package com.daenils.moisei;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.daenils.moisei.entities.Entity;
import com.daenils.moisei.entities.Monster;
import com.daenils.moisei.entities.Player;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Screen;
import com.daenils.moisei.input.Keyboard;
import com.daenils.moisei.input.Mouse;

public class Stage {
	private static int stageCount = -1, unlockedStageCount = -1;
	
	private static Map<Integer, String> mapStages = new HashMap<Integer, String>();
	
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
	private int monstersTotal, monstersSpawned, monstersAlive;
	private boolean allDone;
	
	private Keyboard key;
	
	public Stage(Keyboard input, Mouse inputM, int id) {
		
		this.id = id;
		String[] tempString = mapStages.get(id).split(",");
		
		this.worldOverride = Integer.parseInt(tempString[0]);
		this.title = tempString[1];
		this.description = tempString[2];
		this.scriptfile = tempString[3];
		
		
		// SETTING WORLD AND LOADING BACKGROUND
		this.world = id / 10; // TODO: make it so it can handle worldOverride
		loadBackground("/textures/stages/map" + world + ".png");
		// ADDING PLAYER
		setPlayer(new Player(input, inputM, null, this));
		loadMonsters(tempString[4]);
		
		key = input;
	}
	
	
	
	public static void load() {
		List<String> lines = new ArrayList<String>();
		
		Scanner in;
		in = new Scanner(FileManager.inStages);
		while (in.hasNextLine()) {
			lines.add(in.nextLine());
			for (int i = 0; i < lines.size(); i++) {
				String[] toSplit = lines.get(i).split(":");
				mapStages.put(Integer.parseInt(toSplit[0]), toSplit[1]);
			}
			stageCount++;
		}
		in.close();
	}
	
	private void loadBackground(String path) {
		System.out.println("\n@@@ STAGE: " + id + " | FILENAME: " + "map" + world + ".png @@@");
		this.bgPath = path;
		this.bgWidth = Game.getRenderWidth();
		this.bgHeight = Game.getRenderHeight();
		loadImage();
	}
	
	private void loadImage() {
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
	
	private void loadMonsters(String monsterInfo) {
		System.out.println(monsterInfo);
		if (monsterInfo.length() > 1) {
			String tempString[] = monsterInfo.split(";");
			
			for (int i = 0; i < tempString.length; i++) {
				addMonster(Integer.parseInt(tempString[i]));
			}
		} else
			addMonster(Integer.parseInt(monsterInfo));
		
		monstersTotal = monsters.size(); 
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
		
		// UPDATE MONSTERS
		if (monsters.size() > 0)
		if (monsters.get(0).getHasSpawned()) {
				getMonsters().get(0).update();
			}

		// MONSTER SPAWNER
		spawnMonster();
		
	//	System.out.println(getMonsters().size());
		remove();
		
		checkIfAllDone();
		endTurnIfAllDone();
		
	}
	
	public void render(Screen screen) {
		// render player
		player.render(screen);
		
		// maybe put stage render here? from Screen.java
		
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).render(screen);
		}
		
		// RENDER MONSTER	
		if (monsters.size() > 0)
		if (monsters.get(0).getHasSpawned()) {
				monsters.get(0).render(screen);
			}
	}
	
	private void spawnMonster() {
		if (monsters.size() > 0)
			if (!monsters.get(0).getHasSpawned()) {
				monsters.get(0).setSpawned(true);
				CombatLog.println("" + monsters.get(0).getName() + " has spawned.");	
				updateMonsterFlags();
			}
		
	}
	
	private void checkIfAllDone() {
		// TODO: when you are sure you won't need this particular implementation, please do
		// change it so it applies to only ONE monster (as it does right now but in sort of a 
		// "hacky" way
		if (Game.getGameplay().getIsMonsterTurn()) {
			int n = 0;
			for(int i = 0; i < getMonsters().size(); i++) {
				if (getMonsters().get(i).getHealth() > 0 && getMonsters().get(i).getActionPoints() == 0) {
					n++;
				}
			}
	//		System.out.print("\n" + n);
		//	System.out.println("\n " + Game.getGameplay().monstersAlive);
			/*  OLD CODE FOR CHECKING ALL MONSTERS' DONE-NESS
			 * if (n == Game.getGameplay().getMonstersAlive()) allDone = true;
			 */ 
			if (n == 1) allDone = true;
		}
	}
	
	public void updateMonsterFlags() {
		// called once every turn @ Gameplay.newTurn()
		monstersAlive = 0;
		monstersSpawned = 0;
		for (int i = 0; i < monsters.size(); i++) {
			if (monsters.get(i).getIsAlive()) monstersAlive++;
			if (monsters.get(i).getHasSpawned()) monstersSpawned++;
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
			CombatLog.println("" + e.getName() + " added.");
			// this should set the target if a new monster is added
		}
		else {
			entities.add(e);
		}
	}
	
	private void addMonster(int id) {
		Monster emma = new Monster(id, 1, getPlayer(), this);
		add(emma);
	//	Game.getGameplay().enableGlobalCooldown();			
}
	
	private void remove() {
		// TEMP MEASURE
		for (int i = 0; i < monsters.size(); i++)
			if (!monsters.get(i).getIsAlive()) monsters.get(i).setRemove(true);
		
		
		
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
					// ATTEMPT TO FIX TARGETING BUG (NO TARGET FOR 1 TURN)
					if (monsters.size() > 0) getPlayer().setDefaultTarget(monsters.get(0));
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

	public int getId() {
		return this.id;
	}

	public int getWorld() {
		return world;
	}

	public String getTitle() {
		return title;
	}

	public int getMonstersSpawned() {
		return monstersSpawned;
	}

	public int getMonstersAlive() {
		return monstersAlive;
	}
	
	public int getMonstersTotal() {
		return monstersTotal;
	}
	
	public boolean getInputPlayerEndTurn() {
		return key.playerEndTurn;
	}
	
	public boolean getInputPlayerExitToMenu() {
		return key.playerExitToMenu;
	}
	
	public static int getMaxStage() {
		return stageCount;
	}
	
	public int[] getBackground() {
		return background;
	}


	public static int getUnlockedStageCount() {
		return unlockedStageCount;
	}

	public static void setUnlockedStageCount(int n) {
		unlockedStageCount = n;
	}
}
