package imad.syed.wordlist;

public class Word {
    String mName;
    String mMeaning;
    String mType;

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

    public void setWord(String name, String type, String meaning) {
        this.mName = name;
        this.mType = type;
        this.mMeaning = meaning;
    }

    public String toString() {
        return "(Name: " + this.mName + ", Meaning: " + this.mMeaning + ", Type: " + this.mType + ")";
    }
}
