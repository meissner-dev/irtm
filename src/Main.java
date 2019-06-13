import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        System.out.println("Analyzing...");

        Model model = new Model(args, false, 0f);
        model.tokenize();
        model.calculate();

        System.out.println("Analysis succesful.");

        Scanner reader = new Scanner(System.in);
        String input = new String();
        while(!input.equals("exit"))
        {
            System.out.println("Enter getTfidf term: ");
            input = reader.nextLine();
            System.out.println(model.searchVector(input));
        }

        reader.close();
    }
}
