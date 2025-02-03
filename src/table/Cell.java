package table;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import application.Table;

/**
 * Class Cell whose objects are used in table, every object represents field in table
 * @author Nemanja Micanovic
 */
public class Cell {
	
	private Format format = new TextFormat();   // Podrazumevani format je tekstualni
	private CellValue value = new CellValue();
	
	/**
	 * Getter for format
	 * @return Format of the Cell object
	 */
	public Format getFormat() {
		return format;
	}
	
	/**
	 * Setting format if it satisfies cell value (Setter for format)
	 * @param toChange Format mark of new format (can only be 't' for text, 'd' for date, 'n' for numeric format)
	 * @param numDec Number of decimals for numeric format (valid only for numeric format)
	 * @return Number that represents status if there was an error (0 = no error)
	 */
	public int setFormat(char toChange, int numDec) {
		String curVal = getValue(false);
		
		// Ako je prazna vrednost celije (prazan string) za format razlicit od tekstualnog, onda optimizacija
		if (toChange != 't' && curVal.equals("")) curVal = " ";
		
		if (toChange == 't') {
			// Tekstualni format nema ogranicenja
			format = new TextFormat();
		}
		else if (toChange == 'd') {
			// Provera vrednosti celije za format datuma i pravljenje novog ako odgovara
			if (checkFormat('d', curVal) == false) {
				return -1;
			}
			
			format = new DateFormat();
		}
		else if (toChange == 'n') {
			// Provera vrednosti celije za numericki format i pravljenje novog ako odgovara
			if (curVal.charAt(0) != '=' && checkFormat('n', curVal) == false) {
				return -2;
			}
			
			if (curVal.charAt(0) == '=') {
				// Poziv funkcije iz C++ za izracunavanje formule
				String result = calculateFormula(curVal, Table.getTableCSV(), Table.getColumnMarks(), Table.getNumRows(), Table.getNumColumns());
				if (result == null) return -3;
				
				// Postavljanje vrednosti izraza
				setValue(result);
			}
			
			format = new NumberFormat();
			format.setNumberOfDecimals(numDec);
		}
		else return -4;
		
		setFormatMark(toChange);
		return 0;
	}
	
	/**
	 * Checking if format satisfies cell value
	 * @param formatMark Format mark of format to check
	 * @param val Value of the cell
	 * @return true - if format satisfies cell value, otherwise false
	 */
	public boolean checkFormat(char formatMark, String val) {
		Format testFormat = null;
		if (formatMark == 't') testFormat = new TextFormat();
		else if (formatMark == 'd') testFormat = new DateFormat();
		else if (formatMark == 'n') testFormat = new NumberFormat();
		else return false;

		return testFormat.checkIfFormatCorrect(val);
	}
	
	/**
	 * Getter for cell value
	 * @param exactNumberOfDecimals If true and if cell format is numeric, then returns cell value on exact number of decimals (valid only for numeric format)
	 * @return Value of the cell
	 */
	public String getValue(boolean exactNumberOfDecimals) {
		// Dohvatanje vrednosti
		String val = value.getValue();
		
		// Ako je numericki format i ako je trazeno da se zaokruzi na tacan broj decimala (parametar exactNumberOfDecimals), onda dodatna obrada u zavisnosti od broja decimala
		if (exactNumberOfDecimals == true && value.getFormatMark() == 'n') {
			// Zaokruzivanje broja na odredjeni broj decimala
			double temp = Double.valueOf(val);
			double multiplier =  Math.pow(10, format.getNumberOfDecimals());
			temp = (double) Math.round(temp * multiplier) / multiplier;
			val = String.valueOf(temp);
			
			if (multiplier == 1) val = val.substring(0, val.length() - 2);
			else {
				// Regex za trazenje koliko ima decimala u broju
				Pattern p = Pattern.compile("^-?[0-9]+.([0-9]*)$");
				Matcher m = p.matcher(val);
				if (m.matches()) {
					// Razlika broja koliko treba da bude decimala i broja koliko ima decimala
					int diff = format.getNumberOfDecimals() - m.group(1).length();
					// Dodavanje nula na kraj ako fali
					for (int i = 0; i < diff; i++)
						val += "0";
				}
			}
		}
		
		return val;
	}
	
	/**
	 * Setter for cell value
	 * @param val New value to set
	 */
	public void setValue(String val) {
		value.setValue(val);
	}
	
	/**
	 * Getter for format mark
	 * @return Format mark of current format of the cell ('t' - text, 'd' - date, 'n' - numeric format)
	 */
	public char getFormatMark() {
		return value.getFormatMark();
	}
	
	/**
	 * Setter for format mark
	 * @param formatMark New format mark to set
	 */
	public void setFormatMark(char formatMark) {
		value.setFormatMark(formatMark);
	}
	
	/**
	 * Native function that is called from C++ and calculates the expression
	 * @param expression Expression to be calculated
	 * @param tableCSV Table in CSV format
	 * @param columnMarks Array of column marks
	 * @param numRows Number of rows in table
	 * @param numColumns Number of columns in table
	 * @return Value of the expression
	 */
	public native String calculateFormula(String expression, String tableCSV, char columnMarks[], int numRows, int numColumns);
	
}
