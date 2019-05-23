import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        System.out.println("Analysing...");
        long start = System.currentTimeMillis();

        Model model = new Model(args);
        model.tokenize();
        model.calculate();

        System.out.println(System.currentTimeMillis()-start);
        System.out.println("Analysis succesful.");

        //model.printOccurences();

        Scanner reader = new Scanner(System.in);
        String input = new String();
        while(!input.equals("exit"))
        {
            System.out.println("Enter search term: ");
            input = reader.nextLine();
            System.out.println(model.search(input));
        }

        reader.close();
    }
}
