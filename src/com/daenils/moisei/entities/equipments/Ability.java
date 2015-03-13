package com.daenils.moisei.entities.equipments;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.entities.Entity;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Sprite;

public class Ability extends Equipment {
	private static int abilityCount;
	
	private static Map<Integer, String> mapAbilities = new HashMap<Integer, String>();
	private int abilityType;

	private static boolean abilitiesCounted;
	
	// ABILITY COOLDOWN HANDLING
	private boolean onCooldown;
	
	public boolean showTooltip;
	
	/*
	 * 0 - heal 
	 * 1 - damage
	 * 2 - utility
	 * 3 - nc-buff 
	 * 4 - nc-debuff
	 * (nc means non-castable)
	 */

	
	// CONSTRUCTORS
	public Ability(int id, Entity u) {	
		load(); // remove if you no longer want to load the abilities more than once
		this.hotbarSlot = -1; // 1-4: Q-W-E-R
		
		this.user = u;
		this.id = id;
		
		String[] tempString = mapAbilities.get(id).split(",");

		this.abilityType = Byte.parseByte(tempString[0]);
		this.name = tempString[1];
		this.description = tempString[2];
		this.icon = Sprite.parseSprite(tempString[3]);
		this.APcost = Integer.parseInt(tempString[4]);
		this.MPcost = Integer.parseInt(tempString[5]);
		this.cooldown = Integer.parseInt(tempString[6]);
		this.healValue = Integer.parseInt(tempString[7]);
		this.damageValue = Integer.parseInt(tempString[8]);
		this.utilityValue = Integer.parseInt(tempString[9]);
		this.isOT = Boolean.parseBoolean(tempString[10]);
		this.hotValue = Integer.parseInt(tempString[11]);
		this.dotValue = Integer.parseInt(tempString[12]);
		this.motValue = Integer.parseInt(tempString[13]);
		this.turnCount = Integer.parseInt(tempString[14]);
		this.targetType = Byte.parseByte(tempString[15]);
		this.isStun = Boolean.parseBoolean(tempString[16]);
		this.isDrainMP = Boolean.parseBoolean(tempString[17]);
		this.isShield = Boolean.parseBoolean(tempString[18]);
	}
	
	// GETTERS
	public int getAbilityType() {
		return abilityType;
	}
	
	// LOAD ABILITIES
	/*
	 * Later you might want to make this static and call it from Game.java -- only once, after the Game has launched.
	 * But since that would make hot swapping ability values no longer possible, let's keep it this way for now.
	 */
	public static void load() {
		List<String> lines = new ArrayList<String>();
				
		Scanner in;
		in = new Scanner(FileManager.inAbilities);
		while (in.hasNextLine()) {
		lines.add(in.nextLine());
		if (!abilitiesCounted) abilityCount++;
		for (int i = 0; i < lines.size(); i++) {
			String[] toSplit = lines.get(i).split(":");
			mapAbilities.put(Integer.parseInt(toSplit[0]), toSplit[1]);
		}
}
		
		in.close();
		abilitiesCounted = true;
		
	}

	// GETTERS
	
	public static int getAbilityCount() {
		return abilityCount;
	}
	
	public boolean isOnCooldown() {
		return onCooldown;
	}
	
	public String isOnCooldownText() {
		if (onCooldown) return "CD!";
		else return "";
	}
	
	// SETTERS
	public void setOnCooldown(boolean b) {
		onCooldown = b;
	}
}
