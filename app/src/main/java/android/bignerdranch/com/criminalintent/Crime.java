package android.bignerdranch.com.criminalintent;

import java.util.UUID;

/**
 * Created by bruno on 11/24/15.
 */
public class Crime {

    private UUID mId;
    private String mTitle;

    public Crime() {
        this.mId = UUID.randomUUID();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}
