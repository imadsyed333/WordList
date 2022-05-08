package imad.syed.wordlist;

import androidx.annotation.NonNull;

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

    public void editWord(String name, String meaning, String type) {
        this.mName = name;
        this.mMeaning = meaning;
        this.mType = type;
    }

    @NonNull
    public String toString() {
        return "[" + getName() + ", type: " + getType() + ", meaning: " + getMeaning() + "]";
    }

    public boolean contains(String input) {
        return this.mName.toLowerCase().contains(input.toLowerCase()) || this.mMeaning.toLowerCase().contains(input.toLowerCase()) || this.mType.toLowerCase().contains(input.toLowerCase());
    }
}
