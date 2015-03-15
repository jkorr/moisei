package com.daenils.moisei.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.daenils.moisei.CombatLog;
import com.daenils.moisei.Game;
import com.daenils.moisei.entities.Gameplay;

public class FileManager {
	public static List<String> lines = new ArrayList<String>();
	
	public static InputStream inAbilities;
	public static InputStream inMonsters;
	public static InputStream inWeapons;
	public static InputStream inLetters;
	public static InputStream inLetterDroptable;
	public static InputStream inStages;
	
	
	private static File dirLogs = new File("logs");
	private static File fileStatistics = null;
	private static File fileCombatLog = null;

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
//		createStatisticsFile();
			try {
				java.io.PrintWriter out = new java.io.PrintWriter(FileManager.fileStatistics);
				out.println(Gameplay.readGameStats());
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}					
	}
	
	public static void saveCombatLogFile() {		
//		createStatisticsFile();
			try {
				java.io.PrintWriter out2 = new java.io.PrintWriter(FileManager.fileCombatLog);
				out2.println(CombatLog.saveLogToDisk());
				out2.close();
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

}