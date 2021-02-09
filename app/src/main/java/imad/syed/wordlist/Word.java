package imad.syed.wordlist;

public class Word {
    private final String mName;
    private final String mMeaning;
    private final String mType;

    Word(String name, String meaning, String type) {
        this.mName = name;
        this.mMeaning = meaning;
        this.mType = type;
    }

    public String getName() {
        return mName;
    }

    public String getMeaning() {
        return mMeaning;
    }

    public String getType() {
        return mType;
    }
}
