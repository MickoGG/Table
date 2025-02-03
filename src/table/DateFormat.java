package table;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFormat extends Format {

	@Override
	public boolean checkIfFormatCorrect(String val) {
		Pattern p = Pattern.compile("^([0-9][0-9]).([0-9][0-9]).([0-9][0-9][0-9][0-9]).$");
		Matcher m = p.matcher(val);
		if (m.matches()) return true;
		return false;
	}

	@Override
	public void setNumberOfDecimals(int n) {
		return;
	}

	@Override
	public int getNumberOfDecimals() {
		return -1;
	}
	
}
