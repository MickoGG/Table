package table;

import java.io.File;
import application.Table;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public abstract class Parser {
	
	public Parser(Table table) {
		this.table = table;
	}
	
	protected Table table;
	
	public abstract int[] loadTable(File file);
	
	public abstract boolean saveTable(File file);
	
	protected void errorAlert(String header, String content) {
		Alert errorAlert = new Alert(AlertType.ERROR);
    	errorAlert.setTitle("Error");
    	errorAlert.setHeaderText(header);
    	errorAlert.setContentText(content);
    	
    	errorAlert.show();
	}
	
}
