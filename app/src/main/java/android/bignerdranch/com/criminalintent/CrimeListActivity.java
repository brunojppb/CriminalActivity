package android.bignerdranch.com.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by bruno on 11/25/15.
 */
public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
