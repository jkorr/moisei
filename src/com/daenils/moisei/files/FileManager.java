package com.daenils.moisei.files;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class FileManager {
	private String path;
	private URL url;
	private InputStream in;
	
	public static File fileAbilities = null;
	public static File fileMonsters = null;

	//CONSTRUCTOR
	public FileManager() {
		loadAbilities();
		loadMonsters();
	}
	
	//LOADERS
	public void loadAbilities() {
		this.path = "/data/abilities.txt";
		this.url = FileManager.class.getResource(path);
		
		try {
			fileAbilities = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.out.println("File (" + url + ") has loaded successfully.");
	}
	
	public void loadMonsters() {
		this.path = "/data/monsters.txt";
		this.url = FileManager.class.getResource(path);
		
		try {
			fileMonsters = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		System.out.println("File (" + url + ") has loaded successfully.");
	}
}