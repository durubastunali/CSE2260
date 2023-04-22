import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LexicalAnalyzer {

    public static int column = 0, row = 0, currentColumn = 0, lastColumn = -1;
    public static boolean error = false;
    public static ArrayList<String> outputArray = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        System.out.println("Please enter the name of the input file: ");
        Scanner inputScan = new Scanner(System.in);
        String fileName = inputScan.next();
        
        if (!fileName.substring(fileName.length() - 4, fileName.length()).equals(".txt")) 
			fileName = fileName + ".txt";

        File inputFile = new File(fileName);
        Scanner input = new Scanner(inputFile);
        FileWriter outputFile = new FileWriter("output.txt");

        while (input.hasNextLine()) {
            String line = input.nextLine();
            read(line);
            row++;
            column = 0;
        }

        for (int i = 0; i < outputArray.size(); i++) {
            System.out.println(outputArray.get(i));
            outputFile.write(outputArray.get(i)+"\n");
        }

        outputFile.close();
        input.close();
        inputScan.close();
    }

    public static void read(String line) {
        while (column < line.length() && !error) {
            currentColumn = column;
            if (line.charAt(column) == ' ' || line.charAt(column) == '\t');
            else if (line.charAt(column) == '~')
                break;
            else if (isBracket(line.charAt(column)))
                bracket(line.charAt(column));
            else if (isKeyword(line))
                keyword(line);
            else if (isIdentifier(line))
                outputArray.add("IDENTIFIER " + (row + 1) + ":" + (currentColumn + 1));
            else if (isHexNumber(line) || isBiNumber(line) || isNumber(line))
                outputArray.add("NUMBER " + (row + 1) + ":" + (currentColumn + 1));
            else if (isString(line))
                outputArray.add("STRING " + (row + 1) + ":" + (currentColumn + 1));
            else if (isCharacter(line))
                outputArray.add("CHARACTER " + (row + 1) + ":" + (currentColumn + 1));
            else {
                int invalidCol;
                String invalid;
                if(lastColumn != -1) {
                    invalidCol = column;
                    invalid = line.substring(column, lastColumn);
                    while (lastColumn < line.length() && !isBracket(line.charAt(lastColumn)) && line.charAt(lastColumn) != ' ') {
                        invalid = invalid + line.charAt(lastColumn);
                        lastColumn++;
                    }
                }
                else {
                    error = true;
                    invalidCol = column;
                    invalid = "";
                    while (column < line.length() && !isBracket(line.charAt(column)) && line.charAt(column) != ' ') {
                        invalid = invalid + line.charAt(column);
                        column++;
                    }
                }
                outputArray.add("LEXICAL ERROR [" + (row + 1) + ":" + (invalidCol + 1) + "]: Invalid token '" + invalid + "'");
                break;
            }
            column++;
        }
    }

    public static boolean isNumber(String line) {
        boolean dotUnused = true, eUnused = true, signUnused = true;
        int ogCol = column;
        if (line.charAt(column) == '+' || line.charAt(column) == '-' || isDecDigit(line.charAt(column)) || line.charAt(column) == '.') {
        	if (line.charAt(column) == '.')
        		dotUnused = false;
            column++;
            while (column < line.length() && line.charAt(column) != '\t' && line.charAt(column) != ' ' && !isBracket(line.charAt(column))) {
                if (isDecDigit(line.charAt(column)));
                else if ((line.charAt(column) == '.') && dotUnused && column + 1 < line.length() && isDecDigit(line.charAt(column + 1)))
                    dotUnused = false;
                else if ((line.charAt(column) == 'e' || line.charAt(column) == 'E') && eUnused && isDecDigit(line.charAt(column - 1))
                        && (column + 1 < line.length()) && (isDecDigit(line.charAt(column + 1)) || line.charAt(column + 1) == '+'
                        || line.charAt(column + 1) == '-')) {
                    eUnused = false;
                    dotUnused = false;
                }
                else if ((line.charAt(column) == '+' || line.charAt(column) == '-') && signUnused && (line.charAt(column - 1) == 'e'
                        || line.charAt(column - 1) == 'E') && column + 1 < line.length() && isDecDigit(line.charAt(column + 1))) {
                    signUnused = false;
                } else {
                    column=ogCol;
                    return false;
                }
                column++;
            } column--;
            return true;
        }
        column = ogCol;
        return false;
    }

    public static boolean isHexNumber(String line) {
        int ogCol = column;
        if (column + 2 < line.length() && line.substring(column, column + 2).equals("0x") && isHexDigit(line.charAt(column + 2))) {
            column = column + 2;
            while (column < line.length() && !isBracket(line.charAt(column)) && line.charAt(column) != ' ' && line.charAt(column) != '\t') {
                if (isHexDigit(line.charAt(column)));
                else {
                    column = ogCol;
                    return false;
                } column++;
            } column--;
            return true;
        } column = ogCol;
        return false;
    }

    public static boolean isBiNumber(String line) {
        int ogCol = column;
        if (column + 2 < line.length() && line.substring(column, column + 2).equals("0b") && isBiDigit(line.charAt(column + 2))) {
            column = column + 2;
            while (column < line.length() && !isBracket(line.charAt(column)) && line.charAt(column) != ' ' && line.charAt(column) != '\t') {
                if (isBiDigit(line.charAt(column)));
                else {
                    column = ogCol;
                    return false;
                } column++;
            } column--;
            return true;
        } column = ogCol;
        return false;
    }

    public static boolean isIdentifier(String line) {
        int ogCol = column;
        if(line.charAt(column) == '+' || line.charAt(column) == '-' || line.charAt(column) == '.' ){
            column++;
            if(column == line.length() || line.charAt(column) == ' ' || isBracket(line.charAt(column)) || line.charAt(column) == '\t'){
                column--;
                return true;
            }
        }
        else if (line.charAt(column) == '!' || line.charAt(column) == '*'|| line.charAt(column) == '/'|| line.charAt(column) == ':'|| line.charAt(column) == '<'|| line.charAt(column) == '='
                || line.charAt(column) == '>'|| line.charAt(column) == '?' || isLetter(line.charAt(column)) ) {
            column++;
            for(;column < line.length() &&!(line.charAt(column) == ' ' || isBracket(line.charAt(column)) || line.charAt(column) == '\t') ; column++) {
                if( !(isLetter(line.charAt(column)) || isDecDigit(line.charAt(column)) || line.charAt(column) == '.' || line.charAt(column) == '+'  || line.charAt(column) == '-')){
                    column= ogCol;
                    return false;
                }
            }
            column--;
            return true;
        }
        column= ogCol;
        return false;
    }

    public static boolean isString(String line) {
        int ogCol = column;
        if(line.charAt(column) == '"') {
            for(++column; column< line.length(); column++){
                if( line.charAt(column) == '"'){
                    if(column != ogCol +1){
                        column++;
                        if(!(column == line.length() || (isBracket(line.charAt(column))) || line.charAt(column) == ' ' || line.charAt(column) == '\t'))
                            break;
                        column--;
                        return true;
                    }
                    else{
                        column = ogCol;
                        return false;
                    }
                }
                else if(line.charAt(column) == 92){
                    if(!(++column < line.length() && (line.charAt(column) == 92 || line.charAt(column) == '"')))
                        break;
                }
            }
            lastColumn = column;
        }
        column = ogCol;
        return false;
    }

    public static boolean isCharacter(String line) {
        int ogCol = column;
        if(line.charAt(column) == '\'') {
            column++;
            if(column< line.length() && line.charAt(column) == '\\') {
                column++;
                if(column< line.length() && line.charAt(column) == '\\' && ++column < line.length() && line.charAt(column) == '\''){
                    if(++column == line.length() || (isBracket(line.charAt(column))) || line.charAt(column) == ' ' || line.charAt(column) == '\t'){
                        column--;
                        return true;
                    }
                }
                else if(column< line.length() && line.charAt(column) == '\'' && (++column< line.length() && line.charAt(column) == '\'')) {
                    if(++column == line.length() || (isBracket(line.charAt(column))) || line.charAt(column) == ' ' || line.charAt(column) == '\t'){
                        column--;
                        return true;
                    }
                }
            }
            else{
                column++;
                if(column< line.length() && line.charAt(column) == '\''){
                    if(++column == line.length() || (isBracket(line.charAt(column))) || line.charAt(column) == ' ' || line.charAt(column) == '\t'){
                        column--;
                        return true;
                    }
                }
            }
        }
        column = ogCol;
        return false;
    }

    public static void keyword(String line) {
        if (column + 3 < line.length()  && line.substring(column, column+4).equals("true")) {
            outputArray.add("BOOLEAN" + (row + 1) + ":" + (column + 1));
            column = column + 3;
        } if (column + 4 < line.length()  && line.substring(column, column+5).equals("false")) {
            outputArray.add("BOOLEAN " + (row + 1) + ":" + (column + 1));
            column = column + 4;
        } if (column + 5 < line.length()  && line.substring(column, column+6).equals("define")) {
            outputArray.add("DEFINE " + (row + 1) + ":" + (column + 1));
            column = column + 5;
        } if (column + 2 < line.length()  && line.substring(column, column+3).equals("let")) {
            outputArray.add("LET " + (row + 1) + ":" + (column + 1));
            column = column + 2;
        } if (column + 3 < line.length() && line.substring(column, column+4).equals("cond")) {
            outputArray.add("COND " + (row + 1) + ":" + (column + 1));
            column = column + 3;
        } if (column + 1 < line.length() && line.substring(column, column+2).equals("if")) {
            outputArray.add("IF " + (row + 1) + ":" + (column + 1));
            column = column + 1;
        } if (column + 4 < line.length() && line.substring(column, column+5).equals("begin")) {
            outputArray.add("BEGIN " + (row + 1) + ":" + (column + 1));
            column = column + 4;
        }
    }

    public static void bracket(char currentChar) {
        if (currentChar == '(')
            outputArray.add("LEFTPAR " + (row + 1) + ":" + (column + 1));
        else if (currentChar == ')')
            outputArray.add("RIGHTPAR " + (row + 1) + ":" + (column + 1));
        else if (currentChar == '[')
            outputArray.add("LEFTSQUAREB " + (row + 1) + ":" + (column + 1));
        else if (currentChar == ']')
            outputArray.add("RIGHTSQUAREB " + (row + 1) + ":" + (column + 1));
        else if (currentChar == '{')
            outputArray.add("LEFTCURLYB " + (row + 1) + ":" + (column + 1));
        else if (currentChar == '}')
            outputArray.add("RIGHTCURLYB " + (row + 1) + ":" + (column + 1));
    }

    public static boolean isKeyword(String line) {
        if ((column + 3 < line.length()  && line.substring(column, column+4).equals("true") 
        		&& (line.charAt(column+4) != '\t' || column + 4 == line.length() || line.charAt(column+4) == ' ' || isBracket(line.charAt(column+4)))) ||
                (column + 4 < line.length()  && line.substring(column, column+5).equals("false") 
                		&& (line.charAt(column+5) != '\t' || column + 5 == line.length() || line.charAt(column+5) == ' ' || isBracket(line.charAt(column+5)))) ||
                (column + 5 < line.length()  && line.substring(column, column+6).equals("define") 
                		&& (line.charAt(column+6) != '\t' || column + 6 == line.length() || line.charAt(column+6) == ' ' || isBracket(line.charAt(column+6)))) ||
                (column + 2 < line.length()  && line.substring(column, column+3).equals("let") 
                		&& (line.charAt(column+3) != '\t' || column + 3 == line.length() || line.charAt(column+3) == ' ' || isBracket(line.charAt(column+3)))) ||
                (column + 3 < line.length() && line.substring(column, column+4).equals("cond") 
                		&& (line.charAt(column+4) != '\t' || column + 4 == line.length() || line.charAt(column+4) == ' ' || isBracket(line.charAt(column+4)))) ||
                (column + 1 < line.length() && line.substring(column, column+2).equals("if") 
                		&& (line.charAt(column+2) != '\t' || column + 2 == line.length() || line.charAt(column+2) == ' ' || isBracket(line.charAt(column+2)))) ||
                (column + 4 < line.length() && line.substring(column, column+5).equals("begin") 
                		&& (line.charAt(column+5) != '\t' || column + 5 == line.length() || line.charAt(column+5) == ' ' || isBracket(line.charAt(column+5)))))
            return true;
        return false;
    }

    public static boolean isBracket(char currentChar) {
        if (currentChar == '(' || currentChar == ')' || currentChar == '[' || currentChar == ']' ||
                currentChar == '{' || currentChar == '}')
            return true;
        return false;
    }

    public static boolean isBiDigit(char currentChar) {
        if (currentChar == '0' || currentChar == '1')
            return true;
        return false;
    }

    public static boolean isHexDigit(char currentChar) {
        if (isDecDigit(currentChar) || (currentChar > 96 && currentChar < 103) || (currentChar > 64 && currentChar < 71))
            return true;
        return false;
    }

    public static boolean isDecDigit(char currentChar) {
        if (currentChar > 47 && currentChar < 58)
            return true;
        return false;
    }

    public static boolean isLetter(char currentChar) {
        if (currentChar > 96 && currentChar < 123)
            return true;
        return false;
    }
}