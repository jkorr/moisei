package com.daenils.moisei.entities.equipments;

import com.daenils.moisei.entities.Entity;
import com.daenils.moisei.graphics.Sprite;

public class Equipment {
	protected Sprite icon;
	protected int hotbarSlot; // 1-4: Q-W-E-R
	protected int x, y; // for rendering, but grab it from hotBarSlot
	protected boolean showTooltip;
	
	protected Entity user;
	protected int tick;
	
	// the costs, the heal/damage values probably should be refactored to 'base' (e.g. baseMPcost, baseHealValue, etc.)
	// then make new variables with the prefix 'actual' (e.g. actualMPcost)
	// this way you can later add value scaling by level or something like that
	
	protected int id;
	protected String name;
	protected String description;
	protected int vendorPrice;
	protected boolean isUnique;
	protected int APcost; 
	protected int MPcost;
	protected int cooldown;
	
	protected int healValue;
	protected int damageValue;
	protected int utilityValue;
	
	protected boolean isOT;
	protected int hotValue, dotValue, motValue, turnCount;
	
	protected byte targetType; // 0:self, 1:single, 2:dual, 3:aoe-3, 5:aoe-5
	
	protected boolean isStun, isDrainMP, isShield, isResurrect, isNecro, isAttackBuff, isHealthBuff, isManaBuff, isAPBuff;


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
	
	public int getAPcost() {
		return APcost;
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
	
	public boolean getIsDrainMP() {
		return isDrainMP;
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
	
}



