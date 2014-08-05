package com.daenils.moisei.entities.equipments;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.daenils.moisei.entities.Entity;
import com.daenils.moisei.graphics.Sprite;

public class Ability extends Equipment {
	Map<Integer, Integer> mapAbilityTypes = new HashMap<Integer, Integer>();
	Map<Integer, String> mapAbilityNames = new HashMap<Integer, String>();
	Map<Integer, String> mapAbilityDescriptions = new HashMap<Integer, String>(); // not yet used
	Map<Integer, String> mapAbilityIcons = new HashMap<Integer, String>();
	Map<Integer, Integer> mapAbilityAPcosts = new HashMap<Integer, Integer>();
	Map<Integer, Integer> mapAbilityMPcosts = new HashMap<Integer, Integer>();
	Map<Integer, Integer> mapAbilityHealValues = new HashMap<Integer, Integer>();
	Map<Integer, Integer> mapAbilityDamageValues = new HashMap<Integer, Integer>();
	Map<Integer, Integer> mapAbilityUtilityValues = new HashMap<Integer, Integer>();
	Map<Integer, Boolean> mapAbilityIsOTs = new HashMap<Integer, Boolean>();
	Map<Integer, Integer> mapAbilityHotValues = new HashMap<Integer, Integer>();
	Map<Integer, Integer> mapAbilityDotValues = new HashMap<Integer, Integer>();
	Map<Integer, Integer> mapAbilityTurnCounts = new HashMap<Integer, Integer>();
	Map<Integer, Byte> mapAbilityTargetTypes = new HashMap<Integer, Byte>();
	Map<Integer, Boolean> mapAbilityIsStuns = new HashMap<Integer, Boolean>();
	
	
	private int abilityType;
	/*
	 * 0 - heal 
	 * 1 - damage
	 * 2 - utility
	 * 3 - nc-buff 
	 * 4 - nc-debuff
	 * (nc means non-castable)
	 */

	
	// CONSTRUCTORS
	
	public Ability(int id) {
		// CONSTRUCTOR TO GRAB AN ABILITY BY ID
	}
	
	public Ability(String name) {
		// CONSTRUCTOR TO GRAB AN ABILITY BY NAME
	}
	
	public Ability(int id, Entity u) {
		load();
		this.hotbarSlot = -1; // 1-4: Q-W-E-R
		
		this.user = u;
		this.id = id;
		
		this.abilityType = mapAbilityTypes.get(id);
		this.name = mapAbilityNames.get(id);
//		this.description = mapAbilityDescriptions.get(id);
		this.icon = Sprite.getSpellSprite(mapAbilityIcons.get(id));
		this.APcost = mapAbilityAPcosts.get(id);
		this.MPcost = mapAbilityMPcosts.get(id);
		this.healValue = mapAbilityHealValues.get(id);
		this.damageValue = mapAbilityDamageValues.get(id);
		this.utilityValue = mapAbilityUtilityValues.get(id);
		this.isOT = mapAbilityIsOTs.get(id);
		this.hotValue = mapAbilityHotValues.get(id);
		this.dotValue = mapAbilityDotValues.get(id);
		this.turnCount = mapAbilityTurnCounts.get(id);
		this.targetType = mapAbilityTargetTypes.get(id);
		this.isStun = mapAbilityIsStuns.get(id);
		
	}
	
	// GETTERS
	public int getAbilityType() {
		return abilityType;
	}
	
	// LOAD ABILITIES
	public void load() {
		String path = "res/data/abilities.txt";
		
		java.io.File fileAbilities = new java.io.File(path);
		int currentID = 0;
		// currently no delimiter because I was not yet able to solve it
		// but it means that currently no space in ability names
		try {
			Scanner input = new Scanner(fileAbilities);
			while (input.hasNext()) {
				currentID = input.nextInt();
				mapAbilityTypes.put(currentID, input.nextInt());
				mapAbilityNames.put(currentID, input.next());
				mapAbilityIcons.put(currentID, input.next());
				mapAbilityAPcosts.put(currentID, input.nextInt());
				mapAbilityMPcosts.put(currentID, input.nextInt());
				mapAbilityHealValues.put(currentID, input.nextInt());
				mapAbilityDamageValues.put(currentID, input.nextInt());
				mapAbilityUtilityValues.put(currentID, input.nextInt());
				mapAbilityIsOTs.put(currentID, input.nextBoolean());
				mapAbilityHotValues.put(currentID, input.nextInt());
				mapAbilityDotValues.put(currentID, input.nextInt());
				mapAbilityTurnCounts.put(currentID, input.nextInt());
				mapAbilityTargetTypes.put(currentID, input.nextByte());
				mapAbilityIsStuns.put(currentID, input.nextBoolean());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		/*
		
		// ABILITY ONE
		mapAbilityTypes.put(0, 0);
		mapAbilityNames.put(0, "Test Heal");
		mapAbilityAPcosts.put(0, 1);
		mapAbilityMPcosts.put(0, 10);
		mapAbilityHealValues.put(0, 20);
		mapAbilityDamageValues.put(0, 0);
		
		//ABILITY TWO
		mapAbilityTypes.put(1, 1);
		mapAbilityNames.put(1, "Test Fireball");
		mapAbilityAPcosts.put(1, 1);
		mapAbilityMPcosts.put(1, 20);
		mapAbilityHealValues.put(1, 0);
		mapAbilityDamageValues.put(1, 15);
		
		*/
	}

	
}
