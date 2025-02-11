# Table project

### Excel-like table in Java using JavaFX library. The set of functionalities covered by this project represents a subset of the functionalities offered by Microsoft Excel.

This project was built using **Java 8**, but it should work with newer versions as well.

This is a university project, and the text of the project can be found in **`Project.pdf`** (Serbian language).

## Key features:
- Creating an empty table
- Saving the table in **JSON** or **CSV** format (Note: CSV can remember the table content but not cell types)
- Loading an existing table
- Formatting cells
- Sorting by column
- Calculating formula in cells (if a cell is of numeric type and starts with `=`, it is treated as a formula)
- Robust error handling (ensures that every user mistake is detected, providing a detailed error message)

The formula calculation functionality is implemented in **C++**. The logic for formula calculation is compiled into a **dynamic link library (DLL)**, which is then called from the **Java** application using **Java Native Interface (JNI)**.

## Used libraries
- **JavaFX** – For the graphical user interface (GUI)
- **JSON.simple** - For parsing and writing JSON data
