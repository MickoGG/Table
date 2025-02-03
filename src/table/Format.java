package table;

/**
 * Format for cell
 * @author Nemanja Micanovic
 *
 */
public abstract class Format {

	public abstract boolean checkIfFormatCorrect(String val);     // Poziva se za sve formate

	public abstract void setNumberOfDecimals(int n);              // Poziva se samo za numericki format

	public abstract int getNumberOfDecimals();                	  // Poziva se samo za numericki format
	
}
