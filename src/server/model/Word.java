package server.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

/*
* Helper class for getting a random word from the words.txt file.
* */

public class Word implements Callable<String> {
    private static File words = new File("src/server/model/assets/words.txt");
    private static ArrayList<String> listOfWords = new ArrayList<>();

    private String generateRandomWord(){
        System.out.println("Getting random word");
        listOfWords = scanFile();
        int index=new Random().nextInt(listOfWords.size());
        return listOfWords.get(index);
    }


    // helper method for big message debugging
    public static ArrayList<String> getAllWords() {
        listOfWords = scanFile();
        return listOfWords;
    }

    private static ArrayList<String> scanFile() {
        ArrayList<String> low = new ArrayList<>();
        String line;
        String fileLocation = words.getAbsolutePath();

        try {
            FileReader fileReader = new FileReader(fileLocation);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                low.add(line);
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return low;
    }

    @Override
    public String call() {
        return generateRandomWord();
    }
}