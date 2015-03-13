package com.daenils.moisei.entities.equipments;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.daenils.moisei.entities.Entity;
import com.daenils.moisei.files.FileManager;
import com.daenils.moisei.graphics.Sprite;

public class Weapon extends Equipment {
	private static Map<Integer, String> mapWeapons = new HashMap<Integer, String>();
	
	/* weapon categories:
	 * 0 - melee (e.g. swords and daggers)
	 * 1 - ranged (e.g. bows and throwing knives)
	 * 2 - magic (e.g. wands and staves)
	 * */
	
	/* weapon types:
	 * 0 - sword
	 * 1 - dagger
	 * 2 - greathammer
	 * 3 - bow
	 * 4 - throwing knife
	 * 5 - staff
	 * 6 - wand
	 * 
	 * 10 - monster
	 * */
	
	private byte weaponCat;
	private byte weaponType;
	private int minDamage, maxDamage;
	private int hitChance;  // percentage for now, so 100 = always
	private int maxWeaponCharges;
	private int weaponCharges;
	
	
	public Weapon(Entity u, int id) {
		load();
		
		this.user = u;
		this.id = id;
		
		String[] tempString = mapWeapons.get(id).split(",");
		
		// DEFAULT BASIC WEAPON
		this.weaponCat = Byte.parseByte(tempString[0]);
		this.weaponType = Byte.parseByte(tempString[1]);
		this.name = tempString[2];
		this.description = tempString[3];
		this.icon = Sprite.parseSprite(tempString[4]);
		this.vendorPrice = Integer.parseInt(tempString[5]);
		this.isUnique = Boolean.parseBoolean(tempString[6]);
		this.APcost = Integer.parseInt(tempString[7]);
		this.minDamage = Integer.parseInt(tempString[8]);
		this.maxDamage = Integer.parseInt(tempString[9]);
		this.hitChance = Integer.parseInt(tempString[10]);
		this.maxWeaponCharges = Integer.parseInt(tempString[11]);
		this.weaponCharges = this.maxWeaponCharges;
		this.healValue = Integer.parseInt(tempString[12]);
		this.damageValue = Integer.parseInt(tempString[13]);
		this.utilityValue = Integer.parseInt(tempString[14]);
		this.isOT = Boolean.parseBoolean(tempString[15]);
		this.hotValue = Integer.parseInt(tempString[16]);
		this.dotValue = Integer.parseInt(tempString[17]);
		this.motValue = Integer.parseInt(tempString[18]);
		this.turnCount = Integer.parseInt(tempString[19]);
		this.isStun = Boolean.parseBoolean(tempString[20]);
		this.isDrainMP = Boolean.parseBoolean(tempString[21]);
		this.isShield = Boolean.parseBoolean(tempString[22]);
	}
	
	// LOAD WEAPONS
	public static void load() {
		List<String> lines = new ArrayList<String>();
		
		Scanner in;

			in = new Scanner(FileManager.inWeapons);
			while (in.hasNextLine()) {
				lines.add(in.nextLine());
				for (int i = 0; i < lines.size(); i++) {
					String[] toSplit = lines.get(i).split(":");
					mapWeapons.put(Integer.parseInt(toSplit[0]), toSplit[1]);
				}
			}
			in.close();
	}
	
	// GETTERS
	
	public byte getWeaponCat() {
		return weaponCat;
	}
	
	public byte getWeaponType() {
		return weaponType;
	}
	
	public String getWeaponTypeString() {
		 switch (weaponType) {
		 case 0: return "Sword";
		 case 1: return "Dagger";
		 case 2: return "Greathammer";
		 case 3: return "Bow";
		 case 4: return "Throwing knife";
		 case 5: return "Staff";
		 case 6: return "Wand";
		 }
		return "N/A";
	}
	
	public int getMinDamage() {
		return minDamage;
	}
	
	public int getMaxDamage() {
		return maxDamage;
	}
	
	public String getDmgRange() {
		return this.getMinDamage() + "-" + this.getMaxDamage();
	}
	
	public int getHitChance() {
		return hitChance;
	}
	
	public int getWeaponCharges() {
		return weaponCharges;
	}
	
	public int getMaxWeaponCharges() {
		return maxWeaponCharges;
	}
	
	// SETTERS
	public void increaseWeaponCharges() {
		this.weaponCharges++;
	}
	
	public void decreaseWeaponCharges() {
		this.weaponCharges--;
	}
	
}
