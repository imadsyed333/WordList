package imad.syed.wordlist;

public class Word {
    private final String mName;
    private final String mMeaning;

    Word(String name, String meaning) {
        this.mName = name;
        this.mMeaning = meaning;
    }

    public String getName() {
        return mName;
    }

    public String getMeaning() {
        return mMeaning;
    }
}
