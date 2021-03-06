package com.daenils.moisei.entities.equipments;

import com.daenils.moisei.entities.Entity;
import com.daenils.moisei.entities.Letter.Element;
import com.daenils.moisei.entities.Player;
import com.daenils.moisei.graphics.Sprite;

public class Equipment {
	protected Sprite icon;
	protected int hotbarSlot; // 1-4: Q-W-E-R
	protected int x, y; // for rendering, but grab it from hotBarSlot
	protected boolean showTooltip;
	
	protected Entity user;
	protected Entity target;
	protected int tick;
	
	// the costs, the heal/damage values probably should be refactored to 'base' (e.g. baseMPcost, baseHealValue, etc.)
	// then make new variables with the prefix 'actual' (e.g. actualMPcost)
	// this way you can later add value scaling by level or something like that
	
	protected int id;
	protected String name;
	protected String description;
	protected int vendorPrice;
	protected boolean isUnique;
	protected int EPcost; 
	protected int MPcost;
	protected int cooldown;
	
	protected int valueType;
	protected Element element;
	
	protected int healValue;
	protected int damageValue;
	protected int utilityValue;
	
	protected boolean isOT;
	protected int hotValue, dotValue, motValue, turnCount;
	
	protected byte targetType; // 0:self, 1:single, 2:dual, 3:aoe-3, 5:aoe-5
	
	protected boolean isStun, isShield, isReduction, isElemChoice;

	// OVER-TURN EFFECT HANDLING
	protected long lastUsed; // turn count
	protected boolean OTActive;
	protected boolean appliedOT;

	// GETTERS	
	public int getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Sprite getIcon() {
		return icon;
	}
	
	public int getEPcost() {
		return EPcost;
	}
	
	public int getMPcost() {
		return MPcost;
	}
	
	public int getCooldown() {
		return cooldown;
	}
	
	public int getHealValue() {
		return healValue;
	}
	
	public int getDamageValue() {
		return damageValue;
	}
	
	public int getUtilityValue() {
		return utilityValue;
	}
	
	public boolean getIsOT() {
		return isOT;
	}
	
	public int getHotValue() {
		return hotValue;
	}
	
	public int getDotValue() {
		return dotValue;
	}
	
	public int getMotValue() {
		return motValue;
	}
	
	public int getTurnCount() {
		return turnCount;
	}
	
	public byte getTargetType() {
		return targetType;
	}
	
	public boolean getIsStun() {
		return isStun;
	}
	
	public boolean getIsShield() {
		return isShield;
	}
	
	public int getTick() {
		return tick;
	}
	
	public int getVendorPrice() {
		return vendorPrice;
	}
	
	public boolean getShowTooltip() {
		return showTooltip;
	}
	
	public Entity getUser() {
		return user;
	}
	
	public Entity getTarget() {
		return target;
	}
	
	// OT/CD GETTERS
	public boolean isOTActive() {
		return OTActive;
	}
	
	public boolean isAppliedOT() {
		return appliedOT;
	}
	
	public long getLastUsed() {
		return lastUsed;
	}
	
	public boolean isUnique() {
		return isUnique;
	}
	
	// SETTERS
	public void setLastUsed(long n) {
		lastUsed = n;
	//	System.out.println("I've been called");
	}

	public void setOTActive(boolean b) {
		OTActive = b;
	}

	public void setAppliedOT(boolean appliedOT) {
		this.appliedOT = appliedOT;
	}
	
	public void increaseTick() {
		tick++;
	}
	
	public void setTick(int n) {
		tick = n;
	}
	
	public void resetTick() {
		setTick(0);
	}
	
	public void setShowTooltip(boolean b) {
		showTooltip = b;
	}
	
	public void setTarget(Entity e) {
		target = e;
	}
	
	public void setUser(Entity e) {
		user = e;
	}
	
	public int parseValueType(String str) {
		if (str.equals("raw")) return 0;
		if (str.equals("per")) return 1;
		if (str.equals("cal")) return 2;
		else return -1;
	}
	
	public int parseEPCost(String str) {
		 for (char i = '0'; i <= '9'; i++) {
			 if (str.startsWith(i + "")) return Integer.parseInt(str);
		 }
		 
			 if (str.equalsIgnoreCase("t0")) return Player.getElementalPowerReq(0);
			 else if (str.equalsIgnoreCase("t1")) return Player.getElementalPowerReq(1);
			 else if (str.equalsIgnoreCase("t2")) return Player.getElementalPowerReq(2);
			 else if (str.equalsIgnoreCase("t3")) return Player.getElementalPowerReq(3);
			 else {
				 System.err.println("ERROR: Invalid spell cost value: " + str);
				 return -99;
			 }
	}
	
	public Element parseElementType(int i) {
		switch (i) {
		case 0: { return Element.FIRE; }
		case 1: { return Element.WATER; }
		case 2: { return Element.EARTH; }
		case 3: { return Element.WIND; }
		default: { return Element.NEUTRAL; }
		}
	}
	
	public Element getElement() {
		return element;
	}
	
	public int getElementType() {
		switch(element) {
		case FIRE: { return 0; }
		case WATER: { return 1; }
		case EARTH: { return 2; }
		case WIND: { return 3; }
		default: return -1;
		}
	}

	public boolean isReduction() {
		return isReduction;
	}

	public void setReduction(boolean isReduction) {
		this.isReduction = isReduction;
	}

	public boolean isElemChoice() {
		return isElemChoice;
	}

	public void setElemChoice(boolean isElemChoice) {
		this.isElemChoice = isElemChoice;
	}
	
}



