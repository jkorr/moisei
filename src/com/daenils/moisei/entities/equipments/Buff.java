package com.daenils.moisei.entities.equipments;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.entities.Entity;
import com.daenils.moisei.entities.Player;

public class Buff {
	
	protected int glId, inId;
	private static int nextInId = 0;
	protected String type; // Buff || Debuff
	protected Ability ability;
	protected long startTurn, endTurn;
	protected int duration;
	protected Entity caster, target;
	
	protected boolean tickDone = false, isApplied = false;
	protected int tick = 0;
	protected boolean needsRemoval = false;
	
	protected int wordsCheckValue;
	
	// NEW BUFF
	public Buff(Ability a, Entity c, Entity t, int delay) {
		// IDs
		this.inId = nextInId;
		nextInId++;
		this.glId = a.getID();
		
		this.type = a.getTypeString();
		if (type != "Buff" && type != "Debuff") {
			System.err.println("ERROR! The requested ability is not a buff/debuff.");
			setRemove();
		}
		this.ability = a;
		
		if (delay == 0) this.startTurn = Game.getGameplay().getTurnCount();
		else if (delay > 0) this.startTurn = Game.getGameplay().getTurnCount() + delay;
		else System.err.println("ERROR! Invalid value as Buff delay.");
		this.duration = a.getTurnCount();
		this.endTurn = startTurn + duration;
		
		this.caster = c;
		this.target = t;
		
	
		this.wordsCheckValue = ((Player) caster).getSubmittedWordCount();
		CombatLog.println("Buff/Debuff " + getInId() + ":" + getGlId() + ":" + "start:" + getStartTurn() + ":end:" + getEndTurn() + ":ticksleft:" + getTicksLeft());
	}
	
	public int getTicksLeft() {
		return duration - tick;
	}

	public void update() {
		// LOOK FOR REMOVAL
		if (tick >= duration && duration > 0) setRemove();
		
		if ((Game.getGameplay().getCurrentTurn() <= endTurn && !tickDone) || duration == -1) {
			// DO LOGIC HERE, PROBABLY WRITE THE METHODS INSIDE ABILITY AND USE THEM FROM HERE, USE TICK() TO COUNT UPWARDS

			
			// 0:debuff.dmg
			if (ability.getAbilityType() == 0) {
				if (target.getHealth() < 1) setRemove(); // additional remove condition for debuff
				caster.dealDamage(caster, target, ability, ability.getDamageValue());
				tick();
			}
			
			// 1:buff.mitigation
			if (ability.getAbilityType() == 1) {
				caster.setDamageReduction(ability.getUtilityValue());
				tick();
			}
			
			// 2:buff.heal
			if (ability.getAbilityType() == 2) {
				caster.doHealing(caster, caster, ability, ability.getHealValue());
				tick();
			}
			
			// 3:buff.fixElement
			if (ability.getAbilityType() == 3) {
				if (!((Player) caster).isElementRadialRequested()) ((Player) caster).createRadialMenuFixElement();
				tick();
			}
			
			// 4: buff.worddmg
			if (ability.getAbilityType() == 4) {
				if (!isApplied) {
					((Player) caster).setWordDamageModifier(100);
					isApplied = true;
				}
				
				// remove
				if (((Player) caster).getSubmittedWordCount() > wordsCheckValue) {
					((Player) caster).resetWordDamageModifier();
					setRemove();
				}
			}
			
			// 5: buff.wordheal
			if (ability.getAbilityType() == 5) {
				if (!isApplied) {
					((Player) caster).setWordHeal(ability.getHealValue());
					isApplied = true;
				}
				
				// remove
				if (((Player) caster).getSubmittedWordCount() > wordsCheckValue) {
					((Player) caster).resetWordHeal();
					setRemove();
				}
			}
			
			
			// 6: buff.wordmitigation
			if (ability.getAbilityType() == 6) {
				if (!isApplied) {
					((Player) caster).setWordDamageReduction(ability.getUtilityValue());
					isApplied = true;
				}
				
				// remove
				if (((Player) caster).getSubmittedWordCount() > wordsCheckValue && startTurn+1 < Game.getGameplay().getCurrentTurn()) {
					((Player) caster).resetDamageReduction();
					((Player) caster).resetWordDamageReduction();
					setRemove();
				}
			}
			
			// 7: buff.extraEP
			if (ability.getAbilityType() == 7) {
				if (!isApplied) {
					((Player) caster).setAwardWordExtraEP(true);
					isApplied = true;
				}
				
				// remove
				if (((Player) caster).getSubmittedWordCount() > wordsCheckValue) {
					((Player) caster).setAwardWordExtraEP(false);
					setRemove();
				}
			}
			
			// 12: buff.fireball
			
			// 13: buff.reflectiveMitigation
		


		}
	}
	


	private void tick() {
		tickDone = true;
		tick++;
		CombatLog.println("Buff/Debuff " + getInId() + ":" + getGlId() + ":" + "start:" + getStartTurn() + ":end:" + getEndTurn() + ":ticksleft:" + getTicksLeft());
	}
	
	public void setRemove() {
		// UNDO EFFECTS:
		if (ability.getAbilityType() == 1) {
			caster.setDamageReduction(0);
		}
		
		if (ability.getAbilityType() == 3) {
			((Player) caster).setElementRadialRequested(false);
			((Player) caster).setFixElement(-1);
		}
		
		System.out.println("Buff/Debuff " + inId + " removed.");
		this.needsRemoval = true;
		
	}
	
	private boolean doTheyMatch(char[] wordA, char[] wordB) {
		if (wordA.length != wordB.length) return false;
		else {
			for (int i = 0; i < wordA.length; i++)
				if (wordA[i] != wordB[i]) return false;
		}
		
		return true;
	}
	
	// GETTERS, SETTERS
	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public boolean isTickDone() {
		return tickDone;
	}

	public void setTickDone(boolean tickDone) {
		this.tickDone = tickDone;
	}

	public boolean isNeedsRemoval() {
		return needsRemoval;
	}

	public void setNeedsRemoval(boolean needsRemoval) {
		this.needsRemoval = needsRemoval;
	}

	public String getName() {
		return ability.getName();
	}
	
	public int getGlId() {
		return glId;
	}

	public int getInId() {
		return inId;
	}

	public String getType() {
		return type;
	}

	public long getStartTurn() {
		return startTurn;
	}

	public long getEndTurn() {
		return endTurn;
	}

	public int getDuration() {
		return duration;
	}

	public int getTick() {
		return tick;
	}
	
}
