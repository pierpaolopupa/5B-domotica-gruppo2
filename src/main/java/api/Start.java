package api;

import it.gruppo2b.domotica.gui.Gui;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Start {
	
	private static final Logger log = LogManager.getLogger(Start.class);
	
	public void load() {
		Application.launch(Gui.class);
	}
	
	public void start() {
		
	}
	
	public void shutdown() {
		
	}
}
