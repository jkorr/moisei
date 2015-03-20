package com.daenils.moisei.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.Stage;
import com.daenils.moisei.entities.Gameplay;

public class FileManager {
	public static Map<String, String> mapProfile = new HashMap<String, String>();
	/* 
	 *		ADDING NEW FIELDS FOR THE PROFILE NEEDS TO HAPPEN AT THREE (3) PLACES:
	 *		a) MAP_PROFILE_LEGEND[]
	 *		b) loadDefaultProfile()
	 *		c) loadBaseValues()
	 */
	public static final String[] MAP_PROFILE_LEGEND = {"name", "+created", "lastseen", 
																									"level", "xp", "gold", 
																										"spells", "gear", "stagesunlocked", 
																											"stagescompleted", "continuestage", "set_music", 
																												"set_sfx", "set_savelogs", "set_devmode"};
	
	public static List<String> lines = new ArrayList<String>();
	
	public static boolean profileExists;
	
	public static InputStream inProfile;
	public static FileInputStream filinProfile;
	
	public static InputStream inAbilities;
	public static InputStream inMonsters;
	public static InputStream inWeapons;
	public static InputStream inLetters;
	public static InputStream inLetterDroptable;
	public static InputStream inStages;
	
	private static File dirLogs = new File("logs");
	private static File dirSave = new File("save");
	private static File fileStatistics = null;
	private static File fileCombatLog = null;
	private static File fileProfile = null;

	//CONSTRUCTOR
	public FileManager() {
	}
	
	public static void load() {
		// TODO: try to unify at least some of this mess. is it really necessary to have
		//	all these different InputStreams not to mention all these loader methods?!
		
		// TODO: also, why do you have the try-catch blocks commented out and why
		// aren't the readers closed?
		
		loadAbilities();
		loadMonsters();
		loadWeapons();
		loadLetters();
		loadLetterDroptable();
	//	loadStages(); // Commented out because for now this one is called separately
	}
	
	//LOADERS
	public static void loadAbilities() {
		String path = "/data/abilities.txt";
		URL url = FileManager.class.getResource(path);
		
//		try {
			inAbilities = FileManager.class.getResourceAsStream(path);
//			fileAbilities = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public static void loadMonsters() {
		String path = "/data/monsters.txt";
		URL url = FileManager.class.getResource(path);
		
//		try {
			inMonsters = FileManager.class.getResourceAsStream(path);
//			fileMonsters = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public static void loadWeapons() {
		String path = "/data/weapons.txt";
		URL url = FileManager.class.getResource(path);
		
//		try {
			inWeapons = FileManager.class.getResourceAsStream(path);
//			fileWeapons = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public static void loadLetters() {
		for (char i = 'A'; i < '['; i++) {
			loadLetter(i + ".txt");
		}
		
		System.out.println("ALL letter files have been loaded successfully.");
	}
	
	public static void loadLetter(String filename) {
		String path = "/data/eowl/" + filename;
		URL url = FileManager.class.getResource(path);

		inLetters = FileManager.class.getResourceAsStream(path);
		System.out.println("File (" + url + ") has been loaded successfully."); // TODO: This is a lie.

		Scanner in;
		in = new Scanner(FileManager.inLetters);
		while (in.hasNextLine()) {
			lines.add(in.nextLine());
		}

		in.close();
	}
	
	public static void loadLetterDroptable() {
		String path = "/data/droptable_letters.txt";
		URL url = FileManager.class.getResource(path);
		
//		try {
			inLetterDroptable = FileManager.class.getResourceAsStream(path);
//			fileMonsters = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public static void loadStages() {
		String path = "/data/stages.txt";
		URL url = FileManager.class.getResource(path);

//		try {
			inStages = FileManager.class.getResourceAsStream(path);
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public static void saveStatisticsFile() {		
			try {
				java.io.PrintWriter out = new java.io.PrintWriter(FileManager.fileStatistics);
				out.println(Gameplay.readGameStats());
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}					
	}
	
	public static void saveCombatLogFile() {		
			try {
				java.io.PrintWriter out = new java.io.PrintWriter(FileManager.fileCombatLog);
				out.println(CombatLog.saveLogToDisk());
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}					
	}
	
	public static void createStatisticsFile() {
		Date d = new Date();
		int c = 1;
		String fileName = d.toString().split(" ")[5] + d.toString().split(" ")[1] + d.toString().split(" ")[2] + "-" + c + ".stats";
		String filePath = "logs/";
		
		dirLogs.mkdir();
		
		fileStatistics = new File(filePath + fileName);
		while (fileStatistics.exists()) {
			fileName = d.toString().split(" ")[5] + d.toString().split(" ")[1] + d.toString().split(" ")[2] + "-" + c++ + ".stats";
			fileStatistics = new File(filePath + fileName);
		}		
		System.out.println("Statistics file created.");
	}
	
	public static void createCombatLogFile() {
		Date d = new Date();
		int c = 1;
		String fileName = d.toString().split(" ")[5] + d.toString().split(" ")[1] + d.toString().split(" ")[2] + "-" + c + ".log";
		String filePath = "logs/";
		
		dirLogs.mkdir();
		
		fileCombatLog = new File(filePath + fileName);
		while (fileCombatLog.exists()) {
			fileName = d.toString().split(" ")[5] + d.toString().split(" ")[1] + d.toString().split(" ")[2] + "-" + c++ + ".log";
			fileCombatLog = new File(filePath + fileName);
		}		
		System.out.println("Combat log file created.");
	}
	
	public static void profileHandler() {
		createProfile();
		if (fileProfile.exists() && fileProfile.length() > 110) {
			System.out.println("Profile data found.");
			profileExists = true;
			loadProfile();
			loadProfileToMap();
			
			printMap();
		}
		else if (fileProfile.exists() && fileProfile.length() <= 110) {
			System.err.println("Corrupted profile data was found, creating a new one.");
			saveEmptyProfile();
			loadBaseValues();
		} else {
			System.err.println("No profile data was found.");
			saveEmptyProfile();
			loadBaseValues();			
		}
	}
	
	public static void createProfile() {
		String fileName = "profile.sav";
		String filePath = "save/";
		
		dirSave.mkdir();
		
		fileProfile = new File(filePath + fileName);
		System.out.println("Profile file successfully initialized.");
	}
	
	public static void saveEmptyProfile() {
		try {
			java.io.PrintWriter out = new java.io.PrintWriter(FileManager.fileProfile);
			out.println(loadDefaultProfile());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		profileHandler();
	}
	
	public static void loadProfile() {
		String path = "./save/profile.sav";
	//	URL url = FileManager.class.getResource(path);
		
		try {
			filinProfile = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Profile (" + path + ") has loaded successfully.");
	}
	
	private static String loadDefaultProfile() {
		Date d = new Date();
		return "name:default"
				+ "\n+created:" + formatDate(d)
				+ "\nlastseen:" + formatDate(d)
				+ "\nlevel:0"
				+ "\nxp:0"
				+ "\ngold:0"
				+ "\nspells:0000000000000000" // TODO: maybe-placeholder and will be like loadStageString()
				+ "\ngear:0,0"
				+ "\nstagesunlocked:" + loadStageString(true)
				+ "\nstagescompleted:" + loadStageString(true)
				+ "\ncontinuestage:0"
				+ "\nset_music:80"
				+ "\nset_sfx:100"
				+ "\nset_savelogs:1";
//				+ "\nset_devmode:0";
	}
	
	private static void loadBaseValues() {
		String[] baseValue = {
				"baseName", "0000-00-00 00.00.00", "1991-04-05 00.00.01", 
				"1", "0", "0", 
				"1111000000000000", "0,0", loadStageString(false), 
				loadStageString(true), "0", "80", 
				"100", "1"
		};
		
		for (int i = 3; i < MAP_PROFILE_LEGEND.length - 3; i++) {
			setProfileData(MAP_PROFILE_LEGEND[i], baseValue[i]);
		}
	}
	
	private static String loadStageString(boolean isEmpty) {
		String returnStr = "";
		if (isEmpty) returnStr = "0";
		else returnStr = "1";
		for (int i = 0; i < Stage.getMaxStage(); i++) {
			returnStr += "0";
		}
		return returnStr;
	}
	
	public static void loadProfileToMap() {
		boolean correctionNeeded = false;
		String reason = "";
		List<String> line = new ArrayList<String>();
		
		Scanner in;
		in = new Scanner(FileManager.filinProfile);
		while (in.hasNextLine()) {
			line.add(in.nextLine());
			for (int i = 0; i < line.size(); i++) {
				if (line.get(i).endsWith(":")) {
					line.set(i, line.get(i) + "0");
				}
				String[] toSplit = line.get(i).split(":");
				 
				
				// error-checking: stagesunlocked
				if (line.get(i).startsWith("stagesunlocked")) {
					if (line.get(i).length() < (Stage.getMaxStage() + 1 + 15)) {
						for (int k = 0; k < (Stage.getMaxStage() + 1 + 15) - line.get(i).length(); k++) 
							toSplit[1] += "0";
						
						correctionNeeded = true;
						reason = "'stagesunlocked' was too short!";						
					}
				}
				
				// error-checking: stagescompleted
				if (line.get(i).startsWith("stagescompleted")) {
					if (line.get(i).length() < (Stage.getMaxStage() + 1 + 16)) {
						for (int k = 0; k < (Stage.getMaxStage() + 1 + 16) - line.get(i).length(); k++) 
							toSplit[1] += "0";
						
						correctionNeeded = true;
						reason = "'stagescompleted' was too short!";						
					}
				}
				
				mapProfile.put(toSplit[0], toSplit[1]);
			}
		}
		in.close();
		
		if (correctionNeeded) {
			System.err.println("Adjusting profile.sav to address compatibility issues from new stages: " + reason);
		}
	}
	
	// SETTERS
	// STRING-STRING
	public static void setProfileData(String field, String value) {
		if (field.startsWith("+")) {
			System.err.println("ERROR: Attempting to modify final field in profile data!");
		} else if (!isValidField(field)) {
			System.err.println("ERROR: Attempting to modify a field that does not exist or is invalid for some reason.");
		} else {
			mapProfile.put(field, value); 	// updates the map
			saveProfileFile();						// updates the file (TODO: this might not needs to happen ALL THE TIME)
		}
	}
	
	// STRING-INT
	public static void setProfileData(String field, int value, boolean isIncrement) {
		if (isIncrement) setProfileData(field, (Integer.parseInt(getProfileData(field)) + value) + "");
		else setProfileData(field, value + "");
	}
	
	// STRING-BOOLEAN[]
	// for spells and stages
	public static void setProfileData(String field, boolean[] value) {
		String valueString = "";
		for (int i = 0; i < value.length; i++) {
			if (value[i]) valueString += "1";
			else valueString += "0";
		}
		System.out.println("\n" + valueString);
		setProfileData(field, valueString);
	}
	
	// SET BOOLEANVALUE
	public static void setProfileData(String field, boolean value, int index) {
		boolean[] b = getProfileDataAsBooleanArray(field);
		b[index] = value;
		setProfileData(field, b);
	}
	
	
	// TODO: write this last one
	// STRING-INT[2]
	// for gear

	
	private static void saveProfileFile() {
		try {
			java.io.PrintWriter out = new java.io.PrintWriter(FileManager.fileProfile);
			for (int i = 0; i < mapProfile.size(); i++) {
				out.println(MAP_PROFILE_LEGEND[i] + ":" + mapProfile.get(MAP_PROFILE_LEGEND[i]));
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}			
	}
	
	public static void errorChecking() {
		if (getProfileData("name").equals("0")) {
			setProfileData("name", "default");
			System.err.println("ERROR: Profile name corrupted.");
		}
		
		if (getProfileData("+created").equals("0")) {
			System.err.println("ERROR: Profile creation date corrupted.");
		}
	
		if (getProfileData("gear").equals("0")) {
			System.err.println("ERROR: Gear info is corrupted.");
			setProfileData("gear", "0,0");
		}
	}
	
	public static String formatDate(Date d) {
		int month = -1;
		String monthStr = "?";
		switch (d.toString().split(" ")[1]) {
			case "Jan": { month = 1; break; }
			case "Feb": { month = 2; break; }
			case "Mar": { month = 3; break; }
			case "Apr": { month = 4; break; }
			case "May": { month = 5; break; }
			case "Jun": { month = 6; break; }
			case "Jul": { month = 7; break; }
			case "Aug": { month = 8; break; }
			case "Sep": { month = 9; break; }
			case "Oct": { month = 10; break; }
			case "Nov": { month = 11; break; }
			case "Dec": { month = 12; break; }
		}
		
		if (month < 10) monthStr = "0" + month;
		else monthStr = "" + month;
		
		return d.toString().split(" ")[5] + "-" + monthStr + "-" + d.toString().split(" ")[2] + " " + d.toString().split(" ")[3].split(":")[0] + "." + d.toString().split(" ")[3].split(":")[1] + "." + d.toString().split(" ")[3].split(":")[2];
	//	return "YYYY-MM-DD HH.MM.SS";
	}
	
	public static void printMap() {
			System.out.println(mapProfile.get("created"));
	}
	
	// PROFILE MAP :: EASILY ACCESSIBLE GETTERS
	// EXISTS
	public static boolean getProfileExists() {
		return profileExists;
	}
	
	// CONTAINS FIELD
	public static boolean profileContains(String field) {
		if (mapProfile.containsKey(field)) return true;
		else return false;
	}
	
	// FIELD VALIDATOR (for field lookup method)
	private static boolean isValidField(String field) {
		boolean rValue = false;
		for (int i = 0; i < MAP_PROFILE_LEGEND.length; i++) {
			if (MAP_PROFILE_LEGEND[i].equals(field)) rValue = true;			
		}
		return rValue;
	}
	
	// UNIVERSAL FIELD LOOKUP
	public static String getProfileData(String field) {
		return mapProfile.get(field);
	}
	
	// INT FIELD LOOKUP
	public static int getProfileDataAsInt(String field) {
		return Integer.parseInt(getProfileData(field));
	}
	
	// STRING-BOOLEAN[]
	public static boolean[] getProfileDataAsBooleanArray(String field) {
		String fieldString = getProfileData(field);
		boolean[] b = new boolean[fieldString.length()];
		for (int i = 0; i < fieldString.length(); i++) {
			if (fieldString.charAt(i) == '0') b[i] = false;
			else if (fieldString.charAt(i) == '1') b[i] = true;
			else System.err.println("ERROR: INVALID VALUE IN BOOLEAN ARRAY.");
		}
		return b;
	}
}