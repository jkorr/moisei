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
	protected int abilityCategory; // 0: i 1: b 2: d
	protected int abilityType; // e.g. debuff.dmg, buff.wordheal, etc. 

	/*
	 * 0 - debuff.dmg
	 * 1 - buff.mitigation
	 * 2 - buff.heal
	 * 3 - buff.fixElement
	 * 4 - buff.worddmg
	 * 5 - buff.wordheal
	 * 6 - buff.wordmitigation
	 * 7 - buff.extraEP
	 * 8 - instant.dmg
	 * 9 - instant.heal
	 * 10 - instant.shield(n)
	 * 11 - instant.replaceLetters(n)
	 * 12 - buff.fireball
	 * 13 - buff.reflectiveMitigation
	 * 14 - instant.replaceElements(n)
	 */

	private static boolean abilitiesCounted;
	
	// ABILITY COOLDOWN HANDLING
	private boolean onCooldown;
	
	public boolean showTooltip;
	

	
	// CONSTRUCTORS
	public Ability(int id, Entity u) {	
		load(); // remove if you no longer want to load the abilities more than once
		this.hotbarSlot = -1; // 1-4: Q-W-E-R
		
		this.user = u;
		this.id = id;
		
		String[] tempString = mapAbilities.get(id).split(",");

		this.abilityCategory = Integer.parseInt(tempString[0]);
		this.abilityType = Integer.parseInt(tempString[1]);
		this.element = parseElementType(Integer.parseInt(tempString[2])); 
		this.name = tempString[3];
		this.description = tempString[4];
		this.icon = Sprite.parseSprite(tempString[5]);
		this.EPcost = parseEPCost(tempString[6]);
		this.cooldown = Integer.parseInt(tempString[7]);
		this.valueType = parseValueType(tempString[8]);
		
		this.healValue = Integer.parseInt(tempString[9]);
		this.damageValue = Integer.parseInt(tempString[10]);
		this.utilityValue = Integer.parseInt(tempString[11]);
		
		this.isOT = Boolean.parseBoolean(tempString[12]);
		this.turnCount = Integer.parseInt(tempString[13]);
		
		this.isStun = Boolean.parseBoolean(tempString[14]);
		this.isReduction = Boolean.parseBoolean(tempString[15]);
		this.isElemChoice = Boolean.parseBoolean(tempString[16]);
		this.isShield = Boolean.parseBoolean(tempString[17]);
	}
	
	// GETTERS
	public int getAbilityCat() {
		return abilityCategory;
	}
	
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
	
	public String getTypeString() {
		switch (this.abilityCategory) {
		case 0: { return "Instant"; }
		case 1: { return "Buff"; }
		case 2: { return "Debuff"; }
		default: return "N/A";		
		}
	}
	
	public int getValueType() {
		// 0: raw | 1: per | 2: cal
		return valueType;
	}
	
	// SETTERS
	public void setOnCooldown(boolean b) {
		onCooldown = b;
	}
}
