package table;

public class CellValue {

	private String value = "";
	private char formatMark = 't';   // Podrazumevani format je tekstualni
	
	public String getValue() {
		return value;
	}

	public void setValue(String val) {
		value = val;
	}

	public char getFormatMark() {
		return formatMark;
	}

	public void setFormatMark(char c) {
		formatMark = c;
	}
	
}
