import org.tartarus.snowball.ext.GermanStemmer;

import java.io.*;
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
    private float threshold;

    public Model(String[] args, boolean useStemmed, float threshold) {
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
        this.threshold = threshold;
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

        if(threshold != 0f)
        {
            thresholdTfidf();
        }
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

    private ArrayList tfidfVector;
    private ArrayList<ArrayList<Float>> reversedTfidf;

    private void reverseTfidf()
    {
        reversedTfidf = new ArrayList<>();
        for (int i = 0; i < tfidf.size(); i++) {
            ArrayList temp = tfidf.get(i);
            for (int j = 0; j < temp.size(); j++) {
                reversedTfidf.get(i).add(j, (float) temp.get(j));
            }
        }
    }

    private void addWordVector(String word)
    {
        if(tfidfVector == null)
        {
            tfidfVector = new ArrayList(Collections.nCopies(allWords.size(), 0f));
        }

        if(allWords.containsKey(word))
        {
            int wordID = allWords.get(word);
            tfidfVector.set(wordID, 1f);
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

    private void thresholdTfidf()
    {
        for(int i = 0; i < tfidf.size(); i++)
        {
            ArrayList<Float> column = tfidf.get(i);
            for (int j = 0; j < column.size(); j++)
            {
                if(column.get(j) < threshold)
                    column.set(j, 0f);
                else
                    column.set(j, 1f);
            }
        }
    }

    public float[] getTfidf(String searchTerm) {
        if(!allWords.containsKey(searchTerm))
            return null;

        int wordID = allWords.get(searchTerm);
        float[] results = new float[docCount];
        for(int i = 0; i < docCount; i++)
        {
            results[i] = tfidf.get(wordID).get(i);
        }

        return results;
    }

    private float scalar(ArrayList vector, ArrayList tfidfVector)
    {
        System.out.println(vector.size() + " vs " + tfidfVector.size());
        float sum = 0;
        for (int i = 0; i < vector.size(); i++) {
            sum += (float) vector.get(i) + (float) tfidfVector.get(i);
        }
        return sum;
    }

    private double magnitude(ArrayList vector)
    {
        float result = 0;
        for (Object o : vector) {
            float value = (float) o;
            result += value * value;
        }

        return Math.sqrt(result) == 0 ? 1 : Math.sqrt(result);
    }

    public String searchVector(String searchTerm)
    {
        if(tfidfVector == null)
        {
            tfidfVector = new ArrayList(Collections.nCopies(allWords.size(), 0f));
        }

        String ret = "";

        int vectorPos = docCount + 1;
        try {
            StringBuilder sb = new StringBuilder();
            InputStream targetStream = new ByteArrayInputStream(searchTerm.getBytes());
            InputStreamReader fr = new InputStreamReader(targetStream);
            int nextChar;
            while ((nextChar = fr.read()) != -1)
            {
                // Word continues
                if (!DELIMITERS.contains((char) nextChar)) {
                    sb.append((char) nextChar);
                } else {
                    // Word ended
                    if (sb.toString().isEmpty())
                        continue;

                    System.out.println("trying to add " + sb.toString());

                    if (useStemmed)
                        addWordVector(stem(sb.toString()));
                    else
                        addWordVector(sb.toString());

                    sb.delete(0, sb.length());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // tfidf aktualisieren
        calculate();

        for (int i = 0; i < docCount; i++) {
            float alpha = scalar(tfidfVector, tfidf.get(i));
            alpha /= magnitude(tfidf.get(i)) * magnitude((tfidfVector));
            //double cosSimilar = Math.cos(alpha);
            ret += fileNames.get(i) + ": " + alpha + "\n";
        }

        return ret;
    }

    public String search(String searchTerm)
    {
        String[] split = searchTerm.split(" ");
        String ret = "Word/s found in: ";

        // Wenn 2 Wörter mit/ohne "und" gesucht werden
        if(split.length == 2 || (split.length > 2 && split[1].equals("und")))
        {
            float[] tfidfT1 = getTfidf(split[0]);
            float[] tfidfT2 = getTfidf(split[split.length-1]);
            for (int i=0; i < tfidfT1.length; i++)
            {
                float tfidf = tfidfT1[i] > 0 && tfidfT2[i] > 0? 1 : 0;
                if(tfidf > 0)
                    ret += fileNames.get(i) + " ";
            }
        }
        // Wenn 2 Wörter mit "oder" gesucht werden
        else if(split.length > 2 && split[1].equals("oder"))
        {
            float[] tfidfT1 = getTfidf(split[0]);
            float[] tfidfT2 = getTfidf(split[2]);
            for (int i=0; i < tfidfT1.length; i++)
            {
                float tfidf = tfidfT1[i] > tfidfT2[i] ? tfidfT1[i] : tfidfT2[i];
                if(tfidf > 0)
                    ret += fileNames.get(i) + " ";
            }
        }
        // Wenn 1 Wort gesucht wird
        else
        {
            float[] tfidf = getTfidf(searchTerm);
            if(tfidf == null)
                return "word not found";

            for (int i=0; i < tfidf.length; i++)
            {
                if(tfidf[i] > 0)
                    ret += fileNames.get(i) + " ";
            }
        }

        for (int i = 0; i < tfidf.size(); i++) {
            System.out.println(i + ". is this big " + tfidf.get(i).size());
        }

        return ret;
    }
}
