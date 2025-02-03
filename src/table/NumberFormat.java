package table;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class NumberFormat extends Format {

	private int numDec = 0;
	
	@Override
	public boolean checkIfFormatCorrect(String val) {
		try {
			Double.parseDouble(val);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
		
//		Pattern p = Pattern.compile("^-?[0-9]+.?[0-9]*$");
//		Matcher m = p.matcher(val);
//		if (m.matches()) return true;
//		return false;
	}

	@Override
	public void setNumberOfDecimals(int n) {
		numDec = n;
	}

	@Override
	public int getNumberOfDecimals() {
		return numDec;
	}
	
}
