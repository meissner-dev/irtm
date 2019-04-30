import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        System.out.println("Analysing...");
        long start = System.currentTimeMillis();

        DBConnector dbc = new DBConnector();
        Model model = new Model(args,dbc);
        dbc.setModel(model);
        model.tokenize();
        dbc.UpdateDatabase();

        System.out.println(System.currentTimeMillis()-start);
        System.out.println("Analysis succesful.");

        Scanner reader = new Scanner(System.in);
        String input = new String();
        while(!input.equals("exit"))
        {
            System.out.println("Enter search term: ");
            input = reader.nextLine();
            System.out.println("Positions: " + model.search(input));
        }

        reader.close();
    }
}
