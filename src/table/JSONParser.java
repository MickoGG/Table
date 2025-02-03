package table;

import application.Table;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class JSONParser extends Parser {

	public JSONParser(Table table) {
		super(table);
	}
	
	@Override
	public int[] loadTable(File file) {
		// Povratna vrednost (res[0] == broj redova, res[1] == broj kolona, res[2] == ispravnost)
		int[] res = new int[]{0, 0, 0};
		
		org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
		
		try {
			JSONObject jsonObj = (JSONObject) parser.parse(new FileReader(file));
			
			// Dohvatanje broja redova i kolona iz fajla
			int numRows = Integer.parseInt((String) jsonObj.get("rowLen"));
			int numColumns = Integer.parseInt((String) jsonObj.get("colLen"));
			
			res[0] = numRows;
			res[1] = numColumns;
			
			// Pravljenje tabele
			table.createTable(numRows, numColumns);
			
			// Dohvatanje vrednosti, formata i broja decimala svake celije
			for (int i = 1; i < numColumns + 1; i++) {   // Krece od 1 zbog index kolone
				JSONObject jsonObjCol = (JSONObject) jsonObj.get("col" + i);   // Podaci za i-tu kolonu
				
				JSONArray cellValues = (JSONArray) jsonObjCol.get("cellValues" + i);   // Vrednosti celija u i-toj kolonu
				JSONArray formats = (JSONArray) jsonObjCol.get("formats" + i);   // Formati celija u i-toj kolonu
				JSONArray numOfDecimals = (JSONArray) jsonObjCol.get("numOfDecimals" + i);   // Broj decimala za celije u i-toj koloni
				
				int k = 0;
				// Postavljanje vrednosti celija u i-toj koloni
				for (Object obj : cellValues) {
					table.getData().get(k++)[i].setValue((String) obj);
				}
				
				// Postavljanje formata i broja decimala za celije u i-toj koloni
				for (int j = 0; j < numRows; j++) {
					table.getData().get(j)[i].setFormat(((String) formats.get(j)).charAt(0), Integer.parseInt((String) numOfDecimals.get(j)));
				}
			}
		} catch (IOException e) {
			errorAlert("Error while reading file!", "Make sure you entered right file name.");
			res[2] = 1;
		} catch (ParseException e) {
			errorAlert("Error while reading file!", "Make sure your file is written in right format.");
			res[2] = 1;
		}
		
		return res;
	}
	
	@Override
	// Provera da li postoje redovi u tabeli
	public boolean saveTable(File file) {
		if (table.getData() == null) {
			errorAlert("You can't save.", "Table doesn't exist.");
			return false;
		}
		
		// Povratna vrednost (true - nema greske, false - ima greske)
		boolean ret = true;
		
		try {
			// Otvaranje fajla
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			// Dohvatanje broja redova i kolona tabele
			int numRows = table.getData().size();
			int numColumns = 0;
			if (numRows > 0) numColumns = table.getData().get(0).length - 1;   // -1 zbog index kolone
			
			// Cuvanje tabele u fajl u JSON formatu
			bw.append("{\n");
			bw.append("\t\"rowLen\": \"" + numRows + "\",\n");   // Cuvanje broja redova
			bw.append("\t\"colLen\": \"" + numColumns + "\",\n");   // Cuvanje broja kolona
			
			// Cuvanje vrednosti, formata i broja decimala svake celije po kolonama
			for (int i = 1; i < numColumns + 1; i++) {   // Krece od 1 zbog index kolone
				bw.append("\t\"col" + i + "\": {\n");   // i-ta kolona
				
				// Cuvanje vrednosti za i-tu kolonu
				bw.append("\t\t\"cellValues" + i + "\": [");
				for (int j = 0; j < numRows; j++) {
					bw.append("\"" + table.getData().get(j)[i].getValue(false) + "\"");
					if (j != numRows - 1) bw.append(", ");
				}
				bw.append("],\n");
				
				// Cuvanje formata za i-tu kolonu
				bw.append("\t\t\"formats" + i + "\": [");
				for (int j = 0; j < numRows; j++) {
					bw.append("\"" + table.getData().get(j)[i].getFormatMark() + "\"");
					if (j != numRows - 1) bw.append(", ");
				}
				bw.append("],\n");
				
				// Cuvanje broja decimala za i-tu kolonu
				bw.append("\t\t\"numOfDecimals" + i + "\": [");
				for (int j = 0; j < numRows; j++) {
					int numDec = table.getData().get(j)[i].getFormat().getNumberOfDecimals();
					if (numDec == -1) numDec = 0;
					bw.append("\"" + numDec + "\"");
					if (j != numRows - 1) bw.append(", ");
				}
				bw.append("]\n");
				
				bw.append("\t}");
				if (i != numColumns) bw.append(",");
				bw.append("\n");
			}
			bw.append('}');
			
			// Zatvaranje fajla
			bw.close();
		} catch (IOException e) {
			errorAlert("Error while saving file!", "Make sure you entered right file name.");
			ret = false;
		}
		
		return ret;
	}

}
