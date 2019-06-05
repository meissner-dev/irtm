import org.tartarus.snowball.ext.GermanStemmer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.*;

public class Model {
    private static final ArrayList<Character> DELIMITERS = new ArrayList<>(Arrays.asList(' ', '.', '?', '!', ',', '-', ';', '\n', ':', '«', '»', '–', ':', '\r'));

    // Übung 2
    private ArrayList<String> stopWordList;
    private int docCount, wordCount;
    private HashMap <Integer, File> allDocs;
    private HashMap <String, Integer> allWords;
    private ArrayList<ArrayList<Integer>> occurences;
    private ArrayList<ArrayList<Float>> tfidf;
    private ArrayList<Float> idf;
    private ArrayList<Integer> ntmax;
    private File stopFile;
    private ArrayList<String> fileNames;
    private boolean useStemmed;

    public Model(String[] args, boolean useStemmed) {
        // Init Übung 2
        allDocs = new HashMap<>();
        allWords = new HashMap<>();
        occurences = new ArrayList<>();
        tfidf = new ArrayList<>();
        idf = new ArrayList<>();
        ntmax = new ArrayList<>();

        this.stopFile = new File(args[0]);
        fileNames = new ArrayList<>(args.length-1);

        for(int i = 1; i < args.length; i++)
        {
            allDocs.put(docCount++, new File(args[i]));
            String fileName = args[i];
            fileName = fileName.substring(0, fileName.length()-4);
            fileNames.add(fileName);
        }
        makeStopWordList(stopFile);

        this.useStemmed = useStemmed;
    }

    private String stem(String germanWord)
    {
        GermanStemmer myStem = new GermanStemmer();
        myStem.setCurrent(germanWord);
        myStem.stem();
        return myStem.getCurrent();
    }

    public void tokenize() {
        for(int i=0; i < docCount; i++)
        {
            File textFile = allDocs.get(i);
            StringBuilder sb = new StringBuilder();
            try (FileInputStream fs = new FileInputStream(textFile);
                 InputStreamReader fr = new InputStreamReader(fs)) {
                int nextChar;
                while ((nextChar = fr.read()) != -1) {
                    // Word continues
                    if (!DELIMITERS.contains((char) nextChar)) {
                        sb.append((char) nextChar);
                    } else {
                        // Word ended
                        if (sb.toString().isEmpty())
                            continue;

                        if(useStemmed)
                            addWord(i, stem(sb.toString()));
                        else
                            addWord(i, sb.toString());

                        sb.delete(0, sb.length());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void makeStopWordList(File stopFile) {
        stopWordList = new ArrayList<>();
        try (Scanner scanner = new Scanner(stopFile)) {
            String word;
            while (scanner.hasNext()) {
                word = scanner.next();
                if(!stopWordList.contains(word))
                {
                    stopWordList.add(word);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void calculate()
    {
        calculateIDF();
        calculateNtmax();
        calculateTFIDF();
    }

    public void addWord(int docID, String word) {
        // Word already in lists
        if(allWords.containsKey(word))
        {
            int wordID = allWords.get(word);
            int occInDoc = occurences.get(wordID).get(docID);
            occurences.get(wordID).set(docID, occInDoc+1);
        }
        // Add new word
        else
        {
            allWords.put(word, wordCount);
            occurences.add(wordCount, new ArrayList<>(docCount));
            tfidf.add(wordCount, new ArrayList<>(docCount));
            for(int j=0; j < docCount; j++)
            {
                occurences.get(wordCount).add(0);
                tfidf.get(wordCount).add(0f);
            }
            occurences.get(wordCount).add(docID, 1);

            wordCount++;
        }
    }

    private void calculateIDF()
    {
        for(int i=0; i < wordCount; i++)
        {
            float nt = 0;
            for (int j=0; j < docCount; j++)
            {
                if(occurences.get(i).get(j) > 0)
                {
                    nt++;
                }
            }
            float N = docCount;
            float idftVal = (float) Math.log(N/nt) + 1;
            idf.add(i, idftVal);
        }
    }

    private void calculateNtmax()
    {
        for(int j=0; j < docCount; j++)
        {
            int currentMax = 0;
            for(int i=0; i < wordCount; i++)
            {
                int occInDoc = occurences.get(i).get(j);
                currentMax = (occInDoc > currentMax) ? occInDoc : currentMax;
            }

            ntmax.add(j, currentMax);
        }
    }

    private void calculateTFIDF()
    {
        for(int i=0; i < wordCount; i++)
        {
            for(int j=0; j < docCount; j++)
            {
                float currentTF = (float) occurences.get(i).get(j) / (float) ntmax.get(j);
                float currentTFIDF = currentTF * idf.get(j);
                tfidf.get(i).add(j, currentTFIDF);
            }
        }
    }

    public String search(String searchTerm) {
        if(!allWords.containsKey(searchTerm))
            return "Word not found";

        int wordID = allWords.get(searchTerm);
        String ret = "Results: ";
        for(int j = 0; j < docCount; j++)
        {
            ret += "\n" + fileNames.get(j) + " = " + tfidf.get(wordID).get(j);
        }

        return ret;
    }
}
