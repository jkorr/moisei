package com.daenils.moisei;

import java.util.ArrayList;
import java.util.List;

import com.daenils.moisei.entities.Gamestats;

public class CombatLog {
	private static List<String> combatlog = new ArrayList<String>();
	
	public static void println(String string) {
		String logOpener = "|T" + Gamestats.turnCount + " : ";
		
		string = logOpener.concat(string);
		System.out.print("\n" + string);
		combatlog.add(string);
	}
	
	public static void print(String string) {
		System.out.print(string);
		combatlog.add(string);
	}
	
	public static void printnt(String string) {
		String logOpener = "+T" + (Gamestats.turnCount + 1) + " : ";
		
		string = logOpener.concat(string);
		System.out.print("\n" + string);
		combatlog.add(string);
	}
	
	public static void printet(String string) {
		String logOpener = "-T" + (Gamestats.turnCount) + " : ";

		string = logOpener.concat(string);
		System.out.print("\n" + string);
		combatlog.add(string);
	}
	
	// COMBAT LOG STUFF
	
	public static int getLogLength() {
		return combatlog.size();
	}
	
	public static String getLastLine() {
		return combatlog.get(combatlog.size() - 1);
	}
	
	public static String getLastLines(int n) {
		return combatlog.get(combatlog.size() - n - 1);
	}
	
	public static int getSize() {
		return combatlog.size();
	}
	
	public static void saveLogToDisk() {
		// TODO: save combat logs
	}
}
