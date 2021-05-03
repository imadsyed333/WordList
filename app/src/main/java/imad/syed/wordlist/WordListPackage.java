package imad.syed.wordlist;

import java.util.ArrayList;

public class WordListPackage {
    public String id;
    public ArrayList<Word> words;

    public WordListPackage(String id, ArrayList<Word> words) {
        this.id = id;
        this.words = words;
    }

    public ArrayList<Word> getWords () {
        return this.words;
    }

    public String getId () {
        return this.id;
    }
}
