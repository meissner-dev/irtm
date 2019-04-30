import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        long start = System.currentTimeMillis();
        DBConnector dbc = new DBConnector();//does all db shenannigans
        System.out.println("connected: " + (System.currentTimeMillis()-start));
        Model model = new Model(args,dbc); //creates STOPWORDS in constructor
        System.out.println("model created: " + (System.currentTimeMillis()-start));
        model.tokenize();//stems words and adds them to words table
        System.out.println("tokenized: " + (System.currentTimeMillis()-start));
        dbc.UpdateDatabase();
        System.out.println("updated: " + (System.currentTimeMillis()-start));

        Scanner reader = new Scanner(System.in);  // Reading from System.in
        String input = "";
        while(!input.equals("exit"))
        {
            System.out.println("Enter search term: Heu");
            input = reader.nextLine();
            model.search(input);
        }

        reader.close();
    }
}
