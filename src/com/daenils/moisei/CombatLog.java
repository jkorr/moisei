package com.daenils.moisei;

import java.util.ArrayList;
import java.util.List;

import com.daenils.moisei.files.FileManager;

public class CombatLog {
	private static List<String> combatlog = new ArrayList<String>();
	
	public static void println(String string) {
		String logOpener = "|T" + 0 + " : ";
		
		if (Game.getGameplay() != null) {
			logOpener = "|T" + Game.getGameplay().getTurnCount() + " : ";
		}
		
		string = logOpener.concat(string);
		System.out.print("\n" + string);
		combatlog.add(string);
	}
	
	public static void print(String string) {
		System.out.print(string);
		combatlog.add(string);
	}
	
	public static void printnt(String string) {
		String logOpener = "+T" + 0 + " : ";
		if (Game.getGameplay() != null) {
			logOpener = "+T" + (Game.getGameplay().getTurnCount() + 1) + " : ";
		}
		
		string = logOpener.concat(string);
		System.out.print("\n" + string);
		combatlog.add(string);
	}
	
	public static void printet(String string) {
		String logOpener = "-T" + (Game.getGameplay().getTurnCount()) + " : ";

		string = logOpener.concat(string);
		System.out.print("\n" + string);
		combatlog.add(string);
	}
	
	public static void printpr(String string) {
		String logOpener = "| ";
		
		string = logOpener.concat(string);
		System.out.print("\n" + string);
		combatlog.add(string);
	}
	
	private static void printHeader() {
		String headOpener = "+PROFILE SUMMARY";
		String headCloser = "-PROFILE SUMMARY";
		
		System.out.print("\n" + headOpener);
		combatlog.add(headOpener);
		
		printpr("name: " + FileManager.getProfileData("name"));
		printpr("created: " + FileManager.getProfileData("+created"));
		printpr("lastseen: " + FileManager.getProfileData("lastseen"));
		printpr("level: " + FileManager.getProfileData("level"));
		printpr("xp: " + FileManager.getProfileData("xp"));
		printpr("gold: " + FileManager.getProfileData("gold"));
		printpr("spells: " + FileManager.getProfileData("spells"));
		printpr("gear: " + FileManager.getProfileData("gear"));
		printpr("stagesunlocked: " + FileManager.getProfileData("stagesunlocked"));
		printpr("stagescompleted: " + FileManager.getProfileData("stagescompleted"));
		
		System.out.print("\n" + headCloser);
		combatlog.add(headCloser);
		print("\n");
	}
	
	
	// COMBAT LOG STUFF
	
	public static void init() {
		printHeader();
		printnt("A new game has started.");
	}
	
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
	
	public static String saveLogToDisk() {
		String s = "";
		for (int i = 0; i < combatlog.size(); i++) {
			s = s.concat(combatlog.get(i) + "\n");
		}
		return s;
	}
}
