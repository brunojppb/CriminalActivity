package android.bignerdranch.com.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by bruno on 11/25/15.
 */
public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.Callbacks,
        CrimeFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main_container;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        // If there is no Detail Container, it should be running on a
        // mobile phone. So, instantiate a simple Activity(CrimePagerActivity)
        if(findViewById(R.id.detail_fragment_container) == null) {
            Intent it = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(it);
        }
        // There is a Detail Container. It should be running on a tablet
        // show the CrimeActivity into the Detail Container
        else {
            Fragment newCrimeFragmentDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_fragment_container, newCrimeFragmentDetail)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment =
                (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }
}
