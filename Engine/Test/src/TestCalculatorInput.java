import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TestCalculatorInput {
    static Pattern doublePattern = Pattern.compile("\\d+.\\d+");
    static Pattern intPattern = Pattern.compile("\\d+");

    public static void main(String[] args) {
        startCalculator(System.in);
    }

    private static void startCalculator(InputStream in) {
        Scanner scanner = new Scanner(in).useLocale(Locale.US);

        //boolean lastWasValue = false;
        while (scanner.hasNext()) {
            String cmd = scanner.next();
            if (doublePattern.matcher(cmd).matches() || intPattern.matcher(cmd)
                                                                  .matches()) { // THIS IS A DOUBLE
                storeValue(Double.parseDouble(cmd));
            } else {
                // Operand
                validateOperand(cmd);
            }

//                if (!lastWasValue) {
//                    double v = scanner.nextDouble();
//                    lastWasValue = true;
//                    storeValue(v);
//                } else {
//                    String next = scanner.next();
//                    validateOperand(next);
//                    lastWasValue = false;
//                }
            //lastWasValue = false;
        }
    }

    private static void storeValue(double v) {
        System.out.println("Value : " + v);
    }

    private static void validateOperand(String operand) {
        switch (operand) {
            case "+":
                System.out.println("stored in plus");
            case "-":
                System.out.println("stored in minus");
        }
    }
}
