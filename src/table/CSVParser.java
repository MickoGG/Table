package table;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;
import application.Table;

public class CSVParser extends Parser {

	public CSVParser(Table table) {
		super(table);
	}

	@Override
	public int[] loadTable(File file) {
		// Povratna vrednost (res[0] == broj redova, res[1] == broj kolona, res[2] == ispravnost)
		int[] res = new int[]{0, 0, 0};
		
		try {
			// Otvaranje fajla
			BufferedReader br = new BufferedReader(new FileReader(file));
			// Oznaka trenutne pozicije u fajlu, poziv br.reset() se vraca na ovu poziciju
			br.mark(999999);
			// Pravljenje toka od linija fajla
			Stream<String> s = br.lines();
			
			// Dohvatanje broja redova i kolona iz fajla
			int numRows = (int) s.count();
			br.reset();   // reset na br.mark()
			String line = br.readLine();
			int numColumns = (line != null) ? line.split(",").length : 0;
			br.reset();   // reset na br.mark()
			
			res[0] = numRows;
			res[1] = numColumns;
			
			// Pravljenje tabele
			table.createTable(numRows, numColumns);
			
			s = br.lines();
			int[] cnt = {0};
			
			// Ucitavanje tabele iz fajla
			s.forEach(row -> {
				String[] rowData = row.split(",");
				for (int i = 1; i < numColumns + 1; i++) {
					if (rowData[i - 1].equals(" ")) table.getData().get(cnt[0])[i].setValue("");
					else table.getData().get(cnt[0])[i].setValue(rowData[i - 1]);
				}
				cnt[0]++;
			});
			
			// Zatvaranje fajla
			br.close();
		} catch (IOException e) {
			errorAlert("Error while reading file!", "Make sure you entered right file name.");
			res[2] = 1;
		}
		
		return res;
	}
	
	@Override
	public boolean saveTable(File file) {
		// Provera da li postoje redovi u tabeli
		if (table.getData() == null) {
			errorAlert("You can't save.", "Table doesn't exist.");
			return false;
		}
		
		// Povratna vrednost (true - nema greske, false - ima greske)
		boolean ret = true;
		try {
			// Otvaranje fajla
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			// Cuvanje tabele u fajl
			table.getData().stream().forEach(row -> {
				try {
					for (Cell cell : row) {
						if (cell == row[0]) continue;   // Preskoci ako je index kolona
						if (cell.getValue(false).equals("")) bw.append(" ");
						else bw.append(cell.getValue(false));
						if (cell != row[row.length - 1]) bw.append(',');
					}
					bw.append('\n');
				} catch (IOException e) {
					errorAlert("Error while saving file!", "Unexpected error.");
					return;
				}
			});
			
			// Zatvaranje fajla
			bw.close();
		} catch (IOException e) {
			errorAlert("Error while saving file!", "Make sure you entered right file name.");
			ret = false;
		}
		
		return ret;
	}
	
}
