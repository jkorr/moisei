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
import com.daenils.moisei.entities.Gamestats;

public class FileManager {
	private String path;
	private URL url;
	
	public static List<String> lines = new ArrayList<String>();
	
	public static InputStream inAbilities;
	public static InputStream inMonsters;
	public static InputStream inWeapons;
	public static InputStream inLetters;
	public static InputStream inLetterDroptable;
	
	public static File fileAbilities = null;
	public static File fileMonsters = null;
	public static File fileWeapons = null;
	public static File fileLetters = null;
	public static File fileLetterDroptable = null;
	
	private static File dirLogs = new File("logs");
	private static File fileStatistics = null;
	private static File fileCombatLog = null;

	//CONSTRUCTOR
	public FileManager() {
		loadAbilities();
		loadMonsters();
		loadWeapons();
		loadLetters();
		loadLetterDroptable();
	}
	
	//LOADERS
	public void loadAbilities() {
		this.path = "/data/abilities.txt";
		this.url = FileManager.class.getResource(path);
		
//		try {
			inAbilities = FileManager.class.getResourceAsStream(path);
//			fileAbilities = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public void loadMonsters() {
		this.path = "/data/monsters.txt";
		this.url = FileManager.class.getResource(path);
		
//		try {
			inMonsters = FileManager.class.getResourceAsStream(path);
//			fileMonsters = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public void loadWeapons() {
		this.path = "/data/weapons.txt";
		this.url = FileManager.class.getResource(path);
		
//		try {
			inWeapons = FileManager.class.getResourceAsStream(path);
//			fileWeapons = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public void loadLetters() {
		for (char i = 'A'; i < '['; i++) {
			loadLetter(i + ".txt");
		}
		
		System.out.println("ALL letter files have been loaded successfully.");
	}
	
	public void loadLetter(String filename) {
		this.path = "/data/eowl/" + filename;
		this.url = FileManager.class.getResource(path);

		inLetters = FileManager.class.getResourceAsStream(path);
		System.out.println("File (" + url + ") has been loaded successfully."); // TODO: This is a lie.

		Scanner in;
		in = new Scanner(FileManager.inLetters);
		while (in.hasNextLine()) {
			lines.add(in.nextLine());
		}

		in.close();
	}
	
	public void loadLetterDroptable() {
		this.path = "/data/droptable_letters.txt";
		this.url = FileManager.class.getResource(path);
		
//		try {
			inLetterDroptable = FileManager.class.getResourceAsStream(path);
//			fileMonsters = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public static void saveStatisticsFile() {		
//		createStatisticsFile();
			try {
				java.io.PrintWriter out = new java.io.PrintWriter(FileManager.fileStatistics);
				out.println(Gamestats.readGameStats());
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