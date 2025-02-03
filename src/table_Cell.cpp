#include "table_Cell.h"
#include <stack>
#include <unordered_map>
#include <regex>
#include <sstream>

using namespace std;

// Pomocna funkcija za proveru da li je karakter operator (+, -, *, /)
bool isOperator(char c) {
	return (c == '+' || c == '-' || c == '*' || c == '/');
}

// Pomocna funkcija za izdvajanje broja iz izraza
double extractNumber(const std::string &expression, int &index) {
	double number = 0.0;
	while (index < expression.size() && isdigit(expression[index])) {
		number = (number * 10) + (expression[index] - '0');
		index++;
	}
	return number;
}

// Pomocna funkcija za izracunavanje
double performOperation(double operand1, double operand2, char op) {
	switch (op) {
	case '+':
		return operand1 + operand2;
	case '-':
		return operand1 - operand2;
	case '*':
		return operand1 * operand2;
	case '/':
		return operand1 / operand2;
	default:
		return 0.0;
	}
}

JNIEXPORT jstring JNICALL Java_table_Cell_calculateFormula
(JNIEnv *env, jobject, jstring expressionJava, jstring tableCSVJava, jcharArray columnMarksJava, jint numRows, jint numColumns) {
	// Pretvaranje podataka iz Jave u podatke za C++
	string expression = env->GetStringUTFChars(expressionJava, NULL);
	string tableCSV = env->GetStringUTFChars(tableCSVJava, NULL);
	jchar columnMarks[26];
	env->GetCharArrayRegion(columnMarksJava, 0, 26, columnMarks);
	
	// Alociranje tabele
	string **table = new string*[numRows];
	for (int i = 0; i < numRows; i++) {
		table[i] = new string[numColumns];
	}

	// Dohvatanje vrednosti tabele
	stringstream ss(tableCSV);
	int cntRow = 0, cntCol = 0;
	while (getline(ss, table[cntRow][cntCol], ',')) {
		cntCol++;
		if (cntCol == numColumns) {
			cntCol = 0;
			cntRow++;
		}
	}

	stack<double> operandStack;
	stack<char> operatorStack;
	bool flag = false;   // Flag za proveru da li odredjene stvari mogu da se nalaze nakon nekih drugih stvari u izrazu
	int cnt = 0;		 // Provera da li ima isti broj '(' i ')'

	// Prioriteti operatora
	unordered_map<char, int> precedence;
	precedence['+'] = 1;
	precedence['-'] = 1;
	precedence['*'] = 2;
	precedence['/'] = 2;

	for (int i = 1; i < expression.size(); i++) {
		char c = expression[i];

		if (isspace(c)) continue;

		if (isdigit(c)) {
			if (flag == true) return nullptr;

			// Izdvoji operand iz izraza i stavi ga na stek
			double operand = extractNumber(expression, i);
			operandStack.push(operand);
			i--;
			flag = true;
		}
		else if (isOperator(c)) {
			if (flag == false) return nullptr;

			// Radi izracunavanje sve dok je prioritet novog operatora manji ili jednak prioritetu operatora sa vrha steka
			while (!operatorStack.empty() && precedence[c] <= precedence[operatorStack.top()]) {
				char op = operatorStack.top();
				operatorStack.pop();

				double operand2 = operandStack.top();
				operandStack.pop();

				double operand1 = operandStack.top();
				operandStack.pop();

				double result = performOperation(operand1, operand2, op);
				operandStack.push(result);
			}

			// Stavi na stek novi operator
			operatorStack.push(c);
			flag = false;
		}
		else if (c == '(') {
			if (flag == true) return nullptr;

			// Stavi na stek
			operatorStack.push(c);
			cnt++;
		}
		else if (c == ')') {
			cnt--;
			if (flag == false || cnt < 0) return nullptr;

			// Radi izracunavanje sve dok se ne dodje do '('
			while (operatorStack.top() != '(') {
				char op = operatorStack.top();
				operatorStack.pop();

				double operand2 = operandStack.top();
				operandStack.pop();

				double operand1 = operandStack.top();
				operandStack.pop();

				double result = performOperation(operand1, operand2, op);
				operandStack.push(result);
			}

			// Skini '(' sa steka
			operatorStack.pop();
		}
		else if (isalpha(c)) {
			if (flag == true) return nullptr;

			string cell = "";
			char colChar;
			int col = -1, row = -1;

			// Izdvoji identifikator celije iz izraza
			while (i < expression.size() && (isalpha(expression[i]) || isdigit(expression[i]))) {
				cell += expression[i];
				i++;
			}

			// Provera da li je unet dobar format za celiju i uzimanje vrednosti
			regex rxx("^([A-Z])([0-9]+)$");
			smatch result;
			if (regex_match(cell, result, rxx)) {
				colChar = result.str(1)[0];
				row = stoi(result.str(2));
			}
			else return nullptr;

			// Provera da li uneta kolona postoji i nalazenje numericke vrednosti kolone (A == 0, B == 1, C == 2, ...)
			for (int j = 0; j < numColumns; j++) {
				if (columnMarks[j] == colChar) {
					col = j;
					break;
				}
				else if (j == numColumns - 1) {
					return nullptr;
				}
			}

			// Provera da li unet red postoji
			if (row > numRows || row == 0) return nullptr;

			// Provera da li se u celiji nalazi numericki format
			regex rgx("^-?[0-9]+\.?[0-9]*$");
			if (!regex_match(table[row - 1][col], result, rgx)) return nullptr;

			// Dohvati operand i stavi ga na stek
			double operand = stod(table[row - 1][col]);
			operandStack.push(operand);
			i--;
			flag = true;
		}
	}

	// Provera da li je bio isti broj '(' i ')'
	if (cnt != 0) return nullptr;

	// Racunanje ostatka izraza do kraja
	while (!operatorStack.empty()) {
		char op = operatorStack.top();
		operatorStack.pop();

		if (operandStack.empty()) return nullptr;
		double operand2 = operandStack.top();
		operandStack.pop();

		if (operandStack.empty()) return nullptr;
		double operand1 = operandStack.top();
		operandStack.pop();

		double result = performOperation(operand1, operand2, op);
		operandStack.push(result);
	}

	// Uzimanje vrednosti izraza
	string res = to_string(operandStack.top());

	// Oslobadjanje memorije
	for (int i = 0; i < numRows; i++) {
		delete [] table[i];
	}
	delete [] table;

	return env->NewStringUTF(res.c_str());
}
