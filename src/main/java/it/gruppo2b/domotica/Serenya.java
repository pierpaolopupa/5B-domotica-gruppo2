package it.gruppo2b.domotica;

import api.Start;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Serenya {
	
	private static final Logger log = LogManager.getLogger(Serenya.class);

	private static Start api = new Start();
	
	public     static void main(String[] args) {
		api.load();
	}
}
