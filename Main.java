import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Calculator calc = new Calculator();
        String inputString;

        // continuely allow user to enter equations until they manually exit
        while (true) {
            System.out.print("Equation: ");
            inputString = input.nextLine();

            // allow user to exit calculator
            if (inputString.equals("exit") || inputString.equals("stop")) {
                input.close();
                System.exit(0);
            }
            Double result = calc.calculate(inputString);
            if (result != null)
                System.out.println(result + "\n");
        
        }
        
    }
}
