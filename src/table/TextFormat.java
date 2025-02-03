package table;

public class TextFormat extends Format {

	@Override
	public boolean checkIfFormatCorrect(String val) {
		// Tekstualni format nema ogranicenja
		return true;
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
