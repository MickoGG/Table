package application;

import java.io.*;
import java.util.regex.*;
import javafx.application.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import table.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import table.Cell;

public class Table extends Application {
	
	private static TableView<Cell[]> table = new TableView<>();
	private static ObservableList<Cell[]> data;
	private static char columnMarks[] = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
	private static int numRows = 0;
	private static int numColumns = 0;
	private static Parser parser;
	private static Button formatButton;
	private static Button changeButton;
	private static boolean isSaved = true;
	private static boolean firstRun = true;
	private static Alert errorAlert;
	
	public static int getNumRows() {
		return numRows;
	}
	
	public static int getNumColumns() {
		return numColumns;
	}
	
	public static char[] getColumnMarks() {
		return columnMarks;
	}
	
	public static String getTableCSV() {
		StringBuilder tableCSV = new StringBuilder("");
		for (int i = 0; i < numRows; i++) {
			for (int j = 1; j < numColumns + 1; j++) {
				tableCSV.append(data.get(i)[j].getValue(false));
				if (i == numRows - 1 && j == numColumns) continue;
				else tableCSV.append(',');
			}
		}
		return tableCSV.toString();
	}
	
	// Dohvatanje redova iz tabele
	public ObservableList<Cell[]> getData() {
		return data;
	}
	
	
	// Formatiranje celije/kolone
	private void formatCells() {
		// Pravljenje dijaloga
		Alert formatAlert = createAlert("Enter information", "Enter cell or column that you want to format\n[e.g. A3 - for cell or (A-Z) for whole column]:", null);
		
		// Pravljenje tekstualnih polja
		TextField cellTextField = new TextField();
		cellTextField.setPromptText("Cell/Column");
		TextField formatTextField = new TextField();
		formatTextField.setPromptText("t - text, d - date, n - numeric format");
		formatTextField.setPrefWidth(230);
		TextField numDecTextField = new TextField("0");
		numDecTextField.setPromptText("Number of decimals for number format");
		
		// Dodavanje labela i tekstualnih polja u kontejner
		GridPane gp = (GridPane) formatAlert.getDialogPane().getContent();
		gp.add(new Label("Cell/Column:"), 0, 0);
		gp.add(cellTextField, 1, 0);
		gp.add(new Label("Format mark [t, d, n]:"), 0, 1);
		gp.add(formatTextField, 1, 1);
		gp.add(new Label("Number of decimals\n(for numeric format):"), 0, 2);
		gp.add(numDecTextField, 1, 2);
		
		// Pravljenje i postavljanje tipa dugmeta
		ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		formatAlert.getButtonTypes().setAll(okButtonType);
		
		// Dohvatanje "OK" dugmeta
		Button okButton = (Button) formatAlert.getDialogPane().lookupButton(okButtonType);
		okButton.setOnAction(e -> {
			// Provera da li postoje redovi u tabeli
			if (data == null) {
				errorAlert("You can't format cell/column.", "Table doesn't exist.");
				return;
			}
			
			String str = "";
			char formatMark = ' ';
			int numDec = 0;
			
			// Dohvatanje celije/kolone za formatiranje, zeljenog formata i broja decimala
			try {
				str = cellTextField.getText();
				if (formatTextField.getText().length() > 0) formatMark = formatTextField.getText().charAt(0);
				numDec = Integer.parseInt(numDecTextField.getText());
			} catch (NumberFormatException ex) {
				errorAlert("Wrong input.", "Enter appropriate values.");
				return;
			}
			
            char colName;
    		int rowVal = -1, colVal = -1;
    		
    		// Provera da li je unet odgovarajuci format za celiju/kolonu (A3, C, ...)
            Pattern p = Pattern.compile("^([A-Z])([0-9]*)$");
    		Matcher m = p.matcher(str);
    		if (m.matches()) {
    			// Dohvatanje reda i kolone
    			colName = m.group(1).charAt(0);
    			if (m.group(2).length() > 0) {
    				rowVal = Integer.parseInt(m.group(2));
    			}
    		}
    		else {
    			errorAlert("You didn't enter appropriate format for cell/column.", "e.g. A3 - for cell or (A-Z) for whole column.");
    			return;
    		}
			
    		// Provera da li uneta kolona postoji i nalazenje numericke vrednosti kolone (A == 0, B == 1, C == 2, ...)
    		for (int i = 0; i < numColumns; i++) {
    			if (columnMarks[i] == colName) {
    				colVal = i + 1;
    				break;
    			}
    			else if (i == numColumns - 1) {
    				errorAlert("You didn't enter appropriate column.", "Column must be one of existing.");
        			return;
    			}
    		}
    		
    		// Provera da li unet red postoji
    		if (rowVal > numRows || rowVal == 0) {
    			errorAlert("You didn't enter appropriate row.", "Row must be one of existing.");
    			return;
    		}
    		
    		// Provera da li je uneta odgovarajuca vrednost za format
            if (formatMark != 't' && formatMark != 'd' && formatMark != 'n') {
            	errorAlert("Wrong character for format.", "Enter 't' for text, 'd' for date, 'n' for numeric format.");
            	return;
            }
    		
    		// Ako je format numericki dodatno se parametrizuje brojem decimala koje se prikazuju
    		if (formatMark == 'n' && numDec < 0) {
    			errorAlert("Wront number of decimals!", "Number of decimals must be greater or equal to 0!");
    			return;
    		}
    		
    		// Promena formata u zavisnosti od toga da li se radi o jednoj celiji (rowVal != -1) ili celoj koloni (rowVal == -1)
    		if (rowVal != -1) {
    			// Dohvatanje celije
    			Cell cell = data.get(rowVal - 1)[colVal];
    			// Promena formata ako odgovara za trenutnu vrednost celije inace ispis o gresci
    			int ret = cell.setFormat(formatMark, numDec);
    			if (ret != 0) {
    				if (ret == -2) errorAlert("You can't change format to numeric because current\nvalue of cell doesn't satisfy numeric format!", "Numeric format are numbers (e.g. 123.456).");
    				else if (ret == -1) errorAlert("You can't change format to date because current\nvalue of cell doesn't satisfy date format!", "Date format is: dd.mm.yyyy.");
    				else if (ret == -3) errorAlert("Error during formula calculation.", "Make sure you entered right values.");
    				return;
    			}
    		}
    		else {
    			final int finalColVal = colVal;
    			final char finalFormatMark = formatMark;
    			final int finalNumDec = numDec;
    			
    			// Prebroj koliko ima celija koje nezadovoljavaju zadati format
    			long cnt = data.stream().filter(row -> {
    				// Dohvatanje vrednosti celije
    				String curVal = row[finalColVal].getValue(false);
    				// Provera formata
    				if (row[finalColVal].checkFormat(finalFormatMark, curVal) == false) return true;
    				else return false;
    			}).count();
    			
    			// Pitaj korisnika da li zeli da nastavi ako neka od celija nezadovoljava zadati format
    			if (cnt > 0) {
    				// Pravljenje dijaloga
    				Alert continueAlert = createAlert("Continue", cnt + " cell/s doesn't satisfy given format, do you want\nto continue with remaining cell/s ? ['y' or 'n']", "Enter 'y' for yes or 'n' for no.");
    				
    				// Pravljenje tekstualnog polja
    				TextField continueTextField = new TextField();
    				continueTextField.setPromptText("y or n ?");
    				
    				// Dodavanje labele i tekstualnog polja u kontejner
    				GridPane gp2 = (GridPane) continueAlert.getDialogPane().getContent();
    				gp2.add(new Label("Continue:"), 0, 0);
    				gp2.add(continueTextField, 1, 0);
    				
    				// Pravljenje i postavljanje tipa dugmeta
    				ButtonType okButtonType2 = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    				continueAlert.getButtonTypes().setAll(okButtonType2);
    				
    				// Dohvatanje "OK" dugmeta
    				Button okButton2 = (Button) continueAlert.getDialogPane().lookupButton(okButtonType2);
    				okButton2.setOnAction(e2 -> {
    					char status = ' ';
    					
    					// Dohvatanje statusa (y - zeli da nastavi, n - ne zeli da nastavi)
    					if (continueTextField.getText().length() > 0) status = continueTextField.getText().charAt(0);
    					else {
    						errorAlert("Wrong input!", "Enter 'y' for yes or 'n' for no.");
    						return;
    					}
    					
    					// Provera statusa
    					if (status == 'n') return;
        				else if (status != 'y') {
        					errorAlert("Wrong input!", "Enter 'y' for yes or 'n' for no.");
    						return;
        				}
        				else {
        					// Promeni format svim celijama u koloni koje zadovoljavaju zadati format
        	    			data.stream().filter(row -> {
        	    				// Dohvatanje vrednosti celije
        	    				String curVal = row[finalColVal].getValue(false);
        	    				// Provera formata
        	    				if (row[finalColVal].checkFormat(finalFormatMark, curVal) == true) return true;
        	    				else return false;
        	    			}).forEach(row -> {
        	    				// Promeni format
        	    				row[finalColVal].setFormat(finalFormatMark, finalNumDec);
        	    			});
        				}
    				});
    				
    				// Prikazivanje dijaloga
    				continueAlert.show();
    			}
    			else {
	    			// Promeni format svim celijama u koloni koje zadovoljavaju zadati format
	    			data.stream().filter(row -> {
	    				// Dohvatanje vrednosti celije
	    				String curVal = row[finalColVal].getValue(false);
	    				// Provera formata
	    				if (row[finalColVal].checkFormat(finalFormatMark, curVal) == true) return true;
	    				else return false;
	    			}).forEach(row -> {
	    				// Promeni format
	    				row[finalColVal].setFormat(finalFormatMark, finalNumDec);
	    			});
    			}
    		}
    		
    		// Postavljanje flega
    		isSaved = false;
        });
		
		// Prikazivanje dijaloga i cekanje da se zatvori
		formatAlert.showAndWait();
	}
	
	
	// Promena vrednosti celije
	private void changeCellValue(Cell field, String val) {
		// Ako je field == null onda se pritiskom na dugme menja vrednost i treba da se unese celija koja se menja i nova vrednost, inace se dvoklikom menja vrednost celije i tad vec imamo celiju
		if (field == null) {
			Cell[] f = {null};   // Celija koja se dobija iz unosa
			String[] newValue = {null};   // Nova vrednost koja se dobija iz unosa
			boolean[] ret = {true};   // Fleg koji ukazuje da li je bilo greske (false - ne, true - da)
			
			// Pravljenje dijaloga
			Alert changeAlert = createAlert("Enter information", "Enter cell that you want to\nchange [e.g. A3] and new value:", null);
			
			// Pravljenje tekstualnih polja
			TextField cellTextField = new TextField();
			cellTextField.setPromptText("Cell");
			TextField valueTextField = new TextField();
			valueTextField.setPromptText("Value");
			
			// Dodavanje labela i tekstualnih polja u kontejner
			GridPane gp = (GridPane) changeAlert.getDialogPane().getContent();
			gp.add(new Label("Cell to change:"), 0, 0);
			gp.add(cellTextField, 1, 0);
			gp.add(new Label("New value:"), 0, 1);
			gp.add(valueTextField, 1, 1);
			
			// Pravljenje i postavljanje tipa dugmeta
			ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
			changeAlert.getButtonTypes().setAll(okButtonType);
			
			// Dohvatanje "OK" dugmeta
			Button okButton = (Button) changeAlert.getDialogPane().lookupButton(okButtonType);
			okButton.setOnAction(e2 -> {
				// Provera da li postoje redovi u tabeli
				if (data == null) {
					errorAlert("You can't change cell.", "Table doesn't exist.");
					return;
				}
				
				String cell;
				
				// Dohvatanje celije koja se menja i nove vrednosti
				cell = cellTextField.getText();
				newValue[0] = valueTextField.getText();
				
	            char colName;
	    		int rowVal = -1, colVal = -1;
	    		
	    		// Provera da li je unet odgovarajuci format za celiju (A3, B4, ...)
	            Pattern p = Pattern.compile("^([A-Z])([0-9]+)$");
	    		Matcher m = p.matcher(cell);
	    		if (m.matches()) {
	    			// Dohvatanje reda i kolone
	    			colName = m.group(1).charAt(0);
	    			rowVal = Integer.parseInt(m.group(2));
	    		}
	    		else {
	    			errorAlert("You didn't enter appropriate format for cell.", "Enter column name and row number (e.g. A3).");
	    			return;
	    		}
				
	    		// Provera da li uneta kolona postoji i nalazenje numericke vrednosti kolone (A == 0, B == 1, C == 2, ...)
	    		for (int i = 0; i < numColumns; i++) {
	    			if (columnMarks[i] == colName) {
	    				colVal = i + 1;
	    				break;
	    			}
	    			else if (i == numColumns - 1) {
	    				errorAlert("You didn't enter appropriate column.", "Column must be one of existing.");
	        			return;
	    			}
	    		}
	    		
	    		// Provera da li unet red postoji
	    		if (rowVal > numRows || rowVal == 0) {
	    			errorAlert("You didn't enter appropriate row.", "Row must be one of existing.");
	    			return;
	    		}
	    		
	    		// Dohvatanje odgovarajuce celije
	    		f[0] = data.get(rowVal - 1)[colVal];
	    		// Postavljanje flega da nije bilo nikakve greske
	    		ret[0] = false;
			});
			
			// Prikazivanje dijaloga i cekanje da se zatvori
			changeAlert.showAndWait();
			
			// Provera flega za greske
			if (ret[0] == true) return;
			// Postavljanje celije iz unosa
			field = f[0];
			// Postavljanje nove vrednosti celije iz unosa
			val = newValue[0];
		}
		
		// Ako se ne unese nikakva vrednost (prazan string) za format razlicit od tekstualnog, onda optimizacija
		if (field.getFormatMark() != 't' && val.equals("")) val = " ";
		
		// Promena vrednosti ako odgovara format inace ispis o gresci
		if (field.checkFormat(field.getFormatMark(), val)) {
			field.setValue(val);
		}
		else if (val.charAt(0) == '=' && field.getFormatMark() == 'n') {
			// Rad sa formulama
			// Dohvatanje tabele u CSV formatu
			String tableCSV = getTableCSV();
			
			// Poziv funkcije iz C++ za izracunavanje formule
			String result = field.calculateFormula(val, tableCSV, columnMarks, numRows, numColumns);
			if (result == null) {
				errorAlert("Error during formula calculation.", "Make sure you entered right values.");
				return;
			}
			
			// Postavljanje vrednosti izraza
			field.setValue(result);
		}
		else {
			// Ispis o gresci
			if (field.getFormatMark() == 'n') errorAlert("You didn't enter a value in appropriate format for cell type!", "Cell type is numeric. [Numeric format are numbers (e.g. 123.456)]");
			else if (field.getFormatMark() == 'd') errorAlert("You didn't enter a value in appropriate format for cell type!", "Cell type is date. [Date format is: dd.mm.yyyy.]");
			return;
		}
		
		// Postavljanje flega
		isSaved = false;
	}
	
	
	// Ucitavanje tabele
	private void loadTable() {
		// Pravljenje dijaloga
		Alert loadAlert = createAlert("File name", "Enter file name [.CSV or .JSON]:", null);

		// Pravljenje tekstualnog polja
		TextField loadTextField = new TextField();
		loadTextField.setPromptText("Name");
		
		// Dodavanje labele i tekstualnog polja u kontejner
		GridPane gp = (GridPane) loadAlert.getDialogPane().getContent();
		gp.add(new Label("File name:"), 0, 0);
		gp.add(loadTextField, 1, 0);
		
		// Pravljenje i postavljanje tipa dugmeta
		ButtonType loadButtonType = new ButtonType("LOAD", ButtonBar.ButtonData.OK_DONE);
		loadAlert.getButtonTypes().setAll(loadButtonType);
		
		// Dohvatanje "LOAD" dugmeta
		Button loadButton = (Button) loadAlert.getDialogPane().lookupButton(loadButtonType);
		loadButton.setOnAction(e -> {
			String fileName = " ";
			
			// Dohvatanje imena fajla
			if (loadTextField.getText().length() > 0) fileName = loadTextField.getText();
			else {
				errorAlert("Wrong input!", "Enter file name!");
				return;
			}
			
			// Provera da li je ekstenzija fajla .CSV ili .JSON
			if (fileName.length() > 4 && (fileName.substring(fileName.length() - 4, fileName.length()).equals(".csv") || fileName.substring(fileName.length() - 4, fileName.length()).equals(".CSV"))) {
				parser = new CSVParser(this);
			}
			else if (fileName.length() > 5 && (fileName.substring(fileName.length() - 5, fileName.length()).equals(".json") || fileName.substring(fileName.length() - 5, fileName.length()).equals(".JSON"))) {
				parser = new JSONParser(this);
			}
			else {
				errorAlert("Wrong file format!", "Enter existing file name ending with \".CSV\" or \".JSON\"!");
				return;
			}
			
			// Ucitavanje tabele u zavisnosti od izabranog formata (Povratna vrednost res[0] == broj redova, res[1] == broj kolona, res[2] == ispravnost)
			int[] res = parser.loadTable(new File(fileName));
			
			// Ako je doslo do greske prilikom ucitavanja tabele onda se ne ide dalje
			if (res[2] == 1) return;
			// Uzimanje broja redova i kolona iz ucitane tabele
			numRows = res[0];
			numColumns = res[1];
			
			// Postavljanje flega
			isSaved = true;
		});
		
		// Prikazivanje dijaloga i cekanje da se zatvori
		loadAlert.showAndWait();
	}
	
	
	// Cuvanje tabele
	private boolean saveTable() {
		// Pravljenje dijaloga
		Alert saveAlert = createAlert("Save table", "Enter file name to save:", null);
		
		// Pravljenje tekstualnog polja
		TextField saveTextField = new TextField();
		saveTextField.setPromptText("Name");
		
		// Dodavanje labele i tekstualnog polja u kontejner
		GridPane gp = (GridPane) saveAlert.getDialogPane().getContent();
		gp.add(new Label("File name:"), 0, 0);
		gp.add(saveTextField, 1, 0);
		
		// Pravljenje i postavljanje tipa dugmeta
		ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
		saveAlert.getButtonTypes().remove(ButtonType.OK);
		saveAlert.getButtonTypes().addAll(saveButtonType);
		
		// Povratna vrednost funkcije (true - sve u redu, false - greska)
        boolean[] ret = {true};
		
		// Dohvatanje "Save" dugmeta
		Button saveButton = (Button) saveAlert.getDialogPane().lookupButton(saveButtonType);
		saveButton.setOnAction(e -> {
			String fileName = " ";
			
			// Dohvatanje imena fajla
			if (saveTextField.getText().length() > 0) fileName = saveTextField.getText();
			else {
				errorAlert("Wrong input!", "Enter file name!");
				ret[0] = false;
				return;
			}
			
			// Provera da li je ekstenzija fajla .CSV ili .JSON
			if (fileName.length() > 4 && (fileName.substring(fileName.length() - 4, fileName.length()).equals(".csv") || fileName.substring(fileName.length() - 4, fileName.length()).equals(".CSV"))) {
				parser = new CSVParser(this);
			}
			else if (fileName.length() > 5 && (fileName.substring(fileName.length() - 5, fileName.length()).equals(".json") || fileName.substring(fileName.length() - 5, fileName.length()).equals(".JSON"))) {
				parser = new JSONParser(this);
			}
			else {
				errorAlert("Wrong file format!", "Enter file name ending with \".CSV\" or \".JSON\"!");
				ret[0] = false;
				return;
			}
			
			// Cuvanje tabele u zavisnosti od izabranog formata
	    	boolean saved = parser.saveTable(new File(fileName));
			
	    	// Postavljanje flega
			if (saved) isSaved = true;
			else ret[0] = false;
		});
		
		// Dijalog nije modalan
		saveAlert.initModality(Modality.NONE);
		
		// Prikazivanje dijaloga i cekanje da se zatvori
		saveAlert.showAndWait();
		
		return ret[0];
	}
	
	
	// Pravljenje tabele
	public void createTable(int rows, int columns) {
		// Brisanje tabele ako vec postoji
		if (data != null) data.clear();
		if (table != null) table.getColumns().clear();
		
		// Inicijalizacija redova u tabeli
		data = FXCollections.observableArrayList();   // new Cell[]{ new Cell(), new Cell(), new Cell() }
		
		columns++;   // Mora jedna vise kolona zbog index kolone
		// Popunjavanje tabele praznim celijama
		for (int i = 0; i < rows; i++) {
			Cell[] row = new Cell[columns];
			for (int j = 0; j < columns; j++) 
				row[j] = new Cell();
			row[0].setValue(String.valueOf(i + 1));
			data.add(row);
		}
		
		// Pravljenje prve kolone za index
		TableColumn<Cell[], String> indexColumn = new TableColumn<>("i");
		indexColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].getValue(true)));
		indexColumn.setMinWidth(20);
		table.getColumns().add(indexColumn);
		
		// Sirina kolona
		double columnWidth = (1000 / columns < 70) ? 70 : 1000 / columns;
		
		// Pravljenje ostalih kolona i ubacivanje u tabelu
		for (int i = 1; i < columns; i++) {
			final int columnIndex = i;
			TableColumn<Cell[], String> column = new TableColumn<>(String.valueOf(columnMarks[columnIndex - 1]));
			column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[columnIndex].getValue(true)));
			column.setCellFactory(TextFieldTableCell.forTableColumn());
			column.setOnEditCommit(event -> {
	        	int rowIndex = event.getTablePosition().getRow();
	        	String newValue = event.getNewValue();
	        	Cell cell = data.get(rowIndex)[columnIndex];
	        	
	        	changeCellValue(cell, newValue);
	        	table.refresh();
	        });
			
			// Postavljanje minimalne i preferirane sirine kolone
			column.setMinWidth(20);
			column.setPrefWidth(columnWidth);
			
			// Dodavanje kolone u tabelu
			table.getColumns().add(column);
		}
		
		// Postavljanje da je tabela izmenljiva i postavljanje podataka u tabelu
		table.setEditable(true);
        table.setItems(data);
	}
	
	
	// Pravljenje pocetnog dijaloga za unos broja redova i kolona
	private void createTableDialog() {
		// Pravljenje dijaloga
		Alert alert = createAlert("Create or load table", "Enter number of rows and columns\nto create table:", null);
		
		// Pravljenje tekstualnih polja
		TextField rowsTextField = new TextField();
		rowsTextField.setPromptText("Number of rows");
		TextField columnsTextField = new TextField();
		columnsTextField.setPromptText("Number of columns");
		
		// Dodavanje labela i tekstualnih polja u kontejner
		GridPane gp = (GridPane) alert.getDialogPane().getContent();
		gp.add(new Label("Rows:"), 0, 0);
		gp.add(rowsTextField, 1, 0);
		gp.add(new Label("Columns:"), 0, 1);
		gp.add(columnsTextField, 1, 1);
		
		// Pravljenje i postavljanje tipova dugmadi
		ButtonType createButtonType = new ButtonType("Create Table", ButtonBar.ButtonData.OK_DONE);
		ButtonType loadButtonType = new ButtonType("Load Table", ButtonBar.ButtonData.OK_DONE);
		alert.getButtonTypes().remove(ButtonType.OK);
        alert.getButtonTypes().addAll(loadButtonType, createButtonType);
		
        // Dohvatanje "Create Table" dugmeta
		Button createButton = (Button) alert.getDialogPane().lookupButton(createButtonType);
		createButton.setPrefWidth(100);
		createButton.setOnAction(e -> {
			try {
				// Dohvatanje broja redova i kolona i provera ispravnosti
				numRows = Integer.parseInt(rowsTextField.getText());
	            numColumns = Integer.parseInt(columnsTextField.getText());
	            if (numRows <= 0) {
	            	errorAlert("Wrong number of rows.", "Rows must be greater than 0.");
	            }
	            else if (numColumns <= 0 || numColumns > 26) {
	            	errorAlert("Wrong number of columns.", "Columns must be between 1 and 26.");
	            }
	            else {
	            	createTable(numRows, numColumns);
	            }
			} catch (NumberFormatException ex) {
				errorAlert("Wrong input for rows/columns.", "Enter only numbers.");
			}
        });
		
		// Dohvatanje "Load Table" dugmeta
		Button loadButton = (Button) alert.getDialogPane().lookupButton(loadButtonType);
		loadButton.setOnAction(e -> {
			loadTable();
		});
		
		// Prikazivanje dijaloga
		alert.show();
		firstRun = false;
	}
	
	
	// Pravljenje dijaloga na osnovu parametara funkcije
	private Alert createAlert(String title, String header, String content) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		if (content != null) alert.setContentText(content);
		
		// Pravljenje i postavljanje layout-a za alert
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setPadding(new Insets(20));
		
		alert.getDialogPane().setContent(gp);
		
		return alert;
	}
	
	
	// Pravljenje dijaloga za greske na osnovu parametara funkcije
	private void errorAlert(String header, String content) {
		if (errorAlert != null) errorAlert.close();
		errorAlert = new Alert(AlertType.ERROR);
    	errorAlert.setTitle("Error");
    	errorAlert.setHeaderText(header);
    	errorAlert.setContentText(content);
    	
    	errorAlert.show();
	}
	
	
	// Pravljenje menija
	private MenuBar createMenuBar(Stage primaryStage) {
		MenuBar mb = new MenuBar();
		
		Menu fileMenu = new Menu("File");
		
		// Menu item za pravljenje nove tabele
		MenuItem createMenuItem = new MenuItem("Create new");
		createMenuItem.setOnAction(e -> {
			createTableDialog();
		});
		fileMenu.getItems().add(createMenuItem);
		
		// Menu item za ucitavanje tabele
		MenuItem loadMenuItem = new MenuItem("Load");
		loadMenuItem.setOnAction(e -> {
			loadTable();
		});
		fileMenu.getItems().add(loadMenuItem);
		
		// Menu item za cuvanje tabele
		MenuItem saveMenuItem = new MenuItem("Save");
		saveMenuItem.setOnAction(e -> {
			saveTable();
		});
		fileMenu.getItems().add(saveMenuItem);
		
		// Menu item za izlazak iz programa
		MenuItem exitMenuItem = new MenuItem("Exit");
		exitMenuItem.setOnAction(e -> {
			if (isSaved == false) saveTable();
			primaryStage.close();
		});
		fileMenu.getItems().add(exitMenuItem);
		
		mb.getMenus().add(fileMenu);
		
		return mb;
	}
	
	
	// Pravljenje dugmeta za formatiranje celije/kolone
	private void createFormatButton() {
		formatButton = new Button("Format cell");
		formatButton.setOnAction(e -> {
        	formatCells();
        	table.refresh();
        });
	}
	
	
	// Pravljenje dugmeta za izmenu celije
	private void createChangeButton() {
		changeButton = new Button("Change cell value");
		changeButton.setOnAction(e -> {
        	changeCellValue(null, null);
        	table.refresh();
        });
	}
	
	
	// Potvrda za napustanje programa
	private boolean askForExit() {
		// Pravljenje dijaloga
		Alert exitAlert = createAlert("Exit program", "Do you want to exit program ?", null);
		
		// Pravljenje i postavljanje tipa dugmeta
		ButtonType yesButtonType = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
		exitAlert.getButtonTypes().remove(ButtonType.OK);
		exitAlert.getButtonTypes().addAll(yesButtonType);
		
		// Dohvatanje "Yes" dugmeta
		Button yesButton = (Button) exitAlert.getDialogPane().lookupButton(yesButtonType);
		yesButton.setOnAction(e2 -> {
			return;
		});
		
		// Prikazivanje dijaloga i cekanje da se zatvori
		exitAlert.showAndWait();
		
		if (exitAlert.getResult() == yesButtonType) return true;
		return false;
	}
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			// Pravljenje dugmadi
			createFormatButton();
			createChangeButton();
			
			// Pravljenje kontejnera za dugmad
			HBox buttons = new HBox(10);
			buttons.setPadding(new Insets(10));
			buttons.setAlignment(Pos.CENTER);
			buttons.getChildren().addAll(formatButton, changeButton);
			
			// Pravljenje menija
			MenuBar menuBar = createMenuBar(primaryStage);
			
			// Pravljenje glavnog kontejnera
			BorderPane root = new BorderPane();
	        //root.setPadding(new Insets(10));
			root.setTop(menuBar);
			root.setCenter(table);
	        root.setBottom(buttons);
	        
	        // Pravljenje scene
			Scene scene = new Scene(root, 1280, 720);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			//primaryStage.setResizable(false);
			primaryStage.setTitle("Table");
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(e -> {
				boolean exitStatus = askForExit();
				if (exitStatus == false) {
					new Table().start(new Stage());
					return;
				}
				
				boolean ret = isSaved;
				while (ret == false) {
					ret = saveTable();
				}
				Platform.exit();
			});
			
			if (firstRun == true) createTableDialog();
		} catch(Exception e) {
			errorAlert("Unexpected error!", "Make sure you entered right data.");
		}
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary("CalculateFormulaJNI");
		launch(args);
	}
	
}

// --module-path "C:\Program Files\Java\javafx-sdk-20.0.1\lib" --add-modules javafx.controls,javafx.graphics,javafx.fxml