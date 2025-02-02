import java.util.ArrayList;
import java.util.List;


public class Calculator {

    // public function called to initiate a calculation
    // takes the input in the form of a string and returns a double
    public Double calculate(String inputString) {
        ArrayList<String> parsedInput = parseInput(inputString);

        // check for parsing errors
        if (checkForError(parsedInput))
            return null;

        Double result = performCalculations(parsedInput);
        if (result == null) {
            parsedInput = error("Cannot divide by 0");
        }

        // check for errors discovered during calculation
        if (checkForError(parsedInput))
            return null;
        
        return result;
    }

    // checks if an error has been documented in a list and prints it
    private Boolean checkForError(ArrayList<String> list) {
        if (list.get(0).equals("ERROR")) {
            for (String s : list) {
                System.out.println(s);
            }
            System.out.println("");
            return true;
        }
        return false;
    }

    // parses the input string and converts it into an array list
    private ArrayList<String> parseInput(String inputString) {
        ArrayList<String> parsedInput = new ArrayList<String>();

        // remove whitespace
        inputString = inputString.replace(" ", "");
        inputString = inputString.trim();
        
        String prev = "";
        int endIndex = 0;
        int openParenthNum = 0;
        int closeParenthNum = 0;
        while (inputString.length() > 0) {
            // character is a parenthesis
            if (inputString.charAt(0) == '(' || inputString.charAt(0) == ')') {
                // verify an operator is not before a closed parenthesis
                if (inputString.charAt(0) == ')' && isOperator(prev)) {
                    parsedInput = error("operator before closing parenthesis");
                    return parsedInput;
                }
                // add multiplication operator if an operand follows a closing parenthesis
                if (inputString.length() > 1 && inputString.charAt(0) == ')' && isDigit(inputString.charAt(1))) {
                    inputString = inputString.substring(0, 1) + "*" + inputString.substring(1, inputString.length());
                }

                // tracks the number of open and closed parentheses
                if (inputString.charAt(0) == '(') {
                    openParenthNum++;
                } else {
                    closeParenthNum++;
                }

                // detects if an close parenthesis is placed before it has a counterpart open parenthesis
                if (closeParenthNum > openParenthNum) {
                    parsedInput = error("Incorrect use of parentheses");
                }

                parsedInput.add(inputString.substring(0, 1));
                prev = inputString.substring(0, 1);
                endIndex = 0;
            // character is an operator
            } else if (isOperator(inputString.substring(0, 1))) {

                // verify and operand or closed parenthesis is before the operator
                if (!(prev.equals("operand") || prev.equals(")"))) {
                    parsedInput = error("operator must follow an operand or closing parenthesis");
                    return parsedInput;
                }

                // verify that the operator is being followed by a number or open parenthesis
                if (inputString.length() <= 1 || (!isDigit(inputString.charAt(1)) && !inputString.substring(1, 2).equals("("))) {
                    parsedInput = error("operator must be followed by a number or open parenthesis");
                    return parsedInput;
                }

                parsedInput.add(inputString.substring(0, 1));
                prev = inputString.substring(0, 1);
                endIndex = 0;
            // character is a digit or decimal point
            } else if (isDigit(inputString.charAt(0)) || inputString.charAt(0) == '.') {
                int length = 0;
                Boolean decimal = false;
                // determine length of number
                while (isDigit(inputString.charAt(length)) || inputString.charAt(length) == '.') {
                    if (inputString.charAt(length) == '.') {
                        // verify there is no more than one decimal point
                        if (decimal) {
                            parsedInput = error("cannot have more than one decimal point in a number");
                            return parsedInput;
                        // get the index of the decimal point
                        } else {
                            decimal = true;
                        }
                    }
                    length++;

                    // prevent index out of bounds error during next sequence in loop
                    if (inputString.length() <= length) {
                        break;
                    }
                }
                // add a multiplication operator if the next character is an open parenthesis
                if (inputString.length() > length && inputString.charAt(length) == '(') {
                    inputString = inputString.substring(0, length) + "*" + inputString.substring(length, inputString.length());
                }


                parsedInput.add(inputString.substring(0, length));
                prev = "operand";
                endIndex = length - 1;
            } else {
                parsedInput = error("Invalid character '" + inputString.charAt(0) + "'");
            }

            inputString = inputString.substring(endIndex + 1, inputString.length()); // remove the parsed substring from the input string

        }

        // detects if there is an uneven amount of open and closed parentheses
        if (openParenthNum != closeParenthNum) {
            parsedInput = error("Incorrect use of parentheses");
        }

        return parsedInput;
    }


    // takes an equation in the form of an array list of operators, parentheses, and operands. it then calculates the result of the equation
    private Double performCalculations(ArrayList<String> list) {

        // performs operations within the list until it no longer has parentheses
        ArrayList<String> sublist = new ArrayList<String>();
        while (list.contains("(")) {
            
            sublist.clear();
            sublist.addAll(list);
            int startIndex = sublist.lastIndexOf("("); // get last open parenthesis
            
            // get next closed parenthesis
            int endIndex = 0;
            for (int i = startIndex + 1; i < list.size(); i++) {
                if (sublist.get(i).equals(")")) {
                    endIndex = i;
                    break;
                }
                endIndex = -1;
            }

            sublist = new ArrayList<String>(sublist.subList(startIndex + 1, endIndex)); // make a sub list ranging from the last open parenthesis to next closed parenthesis
            
            list.set(startIndex, operationHandler(sublist)); // performs the necessary operations in the sublist and sets the result to the starting index in the main list
            // removes indices from the main list that were added to the sublist and calculated (except for the starting index which is where the result now is)
            for (int i = startIndex + 1; i <= endIndex; i++) {
                list.remove(startIndex + 1);
            }
        }


        String result = operationHandler(list); // performs any remaining operations in the list now that parentheses have been handled
        if (result == null)
            return null;

        return Double.parseDouble(result);
    }

    // determines the order of operations to perform (absent parentheses) and performs them
    private String operationHandler(ArrayList<String> list) {
        while (list.contains("^")) {
            int resultIndex = list.indexOf("^") - 1;
                list.set(list.indexOf("^") - 1, performCalculation(list.subList(list.indexOf("^") - 1, list.indexOf("^") + 2)).toString()); 
                list.remove(resultIndex + 1);
                list.remove(resultIndex + 1);
        }
        // checks for multiplication and division to perform and performs them until none are left
        while (list.contains("*") || list.contains("/")) {
            // next multiplication occurs before next division
            if (list.indexOf("*") < list.indexOf("/") && list.contains("*") || list.contains("*") && !list.contains("/")) {
                int resultIndex = list.indexOf("*") - 1;
                list.set(list.indexOf("*") - 1, performCalculation(list.subList(list.indexOf("*") - 1, list.indexOf("*") + 2)).toString()); 
                list.remove(resultIndex + 1);
                list.remove(resultIndex + 1);
            // next division occurs before next multiplication
            } else {
                // check for division by zero
                if (list.get(list.indexOf("/") + 1).equals("0")) {
                    return null;
                }
                int resultIndex = list.indexOf("/") - 1;
                list.set(list.indexOf("/") - 1, performCalculation(list.subList(list.indexOf("/") - 1, list.indexOf("/") + 2)).toString());
                list.remove(resultIndex + 1);
                list.remove(resultIndex + 1);
            }
        }
        // checks for addition and subtraction to perform and performs them until none are left
        while (list.contains("+") || list.contains("-")) {
            // next addition occurs before next subtraction
            if (list.indexOf("+") < list.indexOf("-") && list.contains("+") || list.contains("+") && !list.contains("-")) {
                int resultIndex = list.indexOf("+") - 1;
                list.set(list.indexOf("+") - 1, performCalculation(list.subList(list.indexOf("+") - 1, list.indexOf("+") + 2)).toString());
                list.remove(resultIndex + 1);
                list.remove(resultIndex + 1);
            // next subtraction occurs before next addition
            } else {
                int resultIndex = list.indexOf("-") - 1;
                list.set(list.indexOf("-") - 1, performCalculation(list.subList(list.indexOf("-") - 1, list.indexOf("-") + 2)).toString());
                list.remove(resultIndex + 1);
                list.remove(resultIndex + 1);
            }
        }
        return list.get(0);
    }

    // checks which operation to perform and then performs it
    private Double performCalculation(List<String> list) {

        // addition operation
        if (list.get(1).equals("+")) {
            Double operand1 = Double.parseDouble(list.get(0));
            Double operand2 = Double.parseDouble(list.get(2));
            return operand1 + operand2;
        // subtraction operation
        } else if (list.get(1).equals("-")) {
            Double operand1 = Double.parseDouble(list.get(0));
            Double operand2 = Double.parseDouble(list.get(2));
            return operand1 - operand2;
        // multiplication operation
        } else if (list.get(1).equals("*")) {
            Double operand1 = Double.parseDouble(list.get(0));
            Double operand2 = Double.parseDouble(list.get(2));
            return operand1 * operand2;
        // division operation
        } else if (list.get(1).equals("/")) {
            Double operand1 = Double.parseDouble(list.get(0));
            Double operand2 = Double.parseDouble(list.get(2));
            return operand1 / operand2;
        } else if (list.get(1).equals("^")) {
            Double operand1 = Double.parseDouble(list.get(0));
            Double operand2 = Double.parseDouble(list.get(2));
            return Math.pow(operand1, operand2);
        } 
        return null;
    }

    // returns true if string is an opertor, otherwise false
    private Boolean isOperator(String s) {
        return (s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("^"));
    }

    // returns true if char is a digit, otherwise false
    private Boolean isDigit(char c) {
        return (c >= 48 && c <= 57);
    }

    // creates an error message in the array list
    private ArrayList<String> error(String errorMessage) {
        ArrayList<String> error = new ArrayList<String>();
        error.add("ERROR");
        error.add(errorMessage);
        return error;
    }
}