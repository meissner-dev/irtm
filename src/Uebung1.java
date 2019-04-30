/*import org.tartarus.snowball.ext.GermanStemmer;
import java.io.*;
import java.sql.SQLException;
import java.util.*;


public class Uebung1 {
    private static DBConnector dbc;
    private static HashMap<String, Integer> wordCount;
    private static HashMap<Integer,String> wordPlacement;

    private static final ArrayList<Character> DELIMITERS = new ArrayList<>(Arrays.asList(' ', '.', '?', '!', ',', '-', ';', '\n',':','«','»','–'));

    public static void tokenize(String arg0) {

        File textFile = new File(arg0);
        StringBuilder sb = new StringBuilder();
        try(FileInputStream fs = new FileInputStream(textFile);
            InputStreamReader fr = new InputStreamReader(fs)){

            int nextChar;
            int wordCounter=0;
            while((nextChar=fr.read())!=-1){
                if (!DELIMITERS.contains((char) nextChar)) {
                    sb.append((char) nextChar);
                } else {
                    addToDataSet(sb.toString(),wordCounter++);
                    sb.delete(0, sb.length());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static void addToDataSet(String s,int wordCounter) {
        String stemmedString = stem(s);
        if (wordCount.containsKey(stemmedString)) {
            wordCount.put(s, wordCount.get(stemmedString) + 1);
        } else {
            wordCount.put(stemmedString, 1);
        }
        if(!isStopword(s)){
            wordPlacement.put(wordCounter,s);
        }

    }



    public static ArrayList<Integer> search(String searchTerm){
        ArrayList<Integer> wordPositions = new ArrayList<Integer>();
        for(int i=0;i<wordPlacement.size();i++){
            String word =wordPlacement.get(i);
            if(word!=null&&word.equals(searchTerm)){
                wordPositions.add(i);
            }
        }
        return wordPositions;
    }
    public static void makeStopWordList(String arg1){
        try (Scanner scanner = new Scanner(new File(arg1))) {
            while(scanner.hasNext()) {
                stopWords.add(scanner.nextLine());
                dbc.insertInDB("insert into stop");
            }
        } catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        dbc = new DBConnector();
        wordCount = new HashMap<>();
        wordPlacement = new HashMap<>();
        tokenize(args[0]);
    }
}

*/