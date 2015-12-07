package android.bignerdranch.com.criminalintent;

import android.app.Activity;
import android.bignerdranch.com.criminalintent.util.PictureUtils;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by bruno on 11/25/15.
 */
public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID    = "crime_id";
    private static final String DIALOG_DATE     =  "DialogDate";
    private static final String DIALOG_TIME     =  "DialogTime";
    private static final int REQUEST_DATE       = 0;
    private static final int REQUEST_TIME       = 1;
    private static final int REQUEST_CONTACT    = 2;
    private static final int REQUEST_PHOTO      = 3;

    private Crime mCrime;
    private File mPhotoFile;

    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mCheckBox;
    private Button mSendReportButton;
    private Button mAddSuspectButton;
    private ImageButton mCameraButton;
    private ImageView mPhotoView;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    void returnResult() {
        getActivity().setResult(Activity.RESULT_OK, null);
    }

    // =====================================================================
    // Creation life cycle
    // =====================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.getInstance(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.getInstance(getActivity()).getPhotoFile(mCrime);
        // tell to the FragmentManager to send calbacks from Activity to
        // the fragment
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.getInstance(getActivity()).updateCrime(mCrime);
    }

    // With fragments, we do not inflate our view inside the OnCreate Method
    // there is a special method to do that for us.
    //this method will inflate the view and return the view to the hosting activity(the container)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // Get all fragment's views
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        mSendReportButton = (Button) v.findViewById(R.id.crime_report);
        mAddSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mCameraButton = (ImageButton) v.findViewById(R.id.crime_camera);
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);

        mTitleField.setText(mCrime.getTitle());


        //set an implicit intent to send the report
        //ask the user which app he would like to use
        mSendReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Intent.ACTION_SEND);
                it.setType("text/plain");
                it.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                it.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                it.createChooser(it, getString(R.string.send_report));
                startActivity(it);
            }
        });


        // Set an implicit intent to get a contact and set as the suspect
        final Intent pickContactIt = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        // Set an implicit intent to get the camera app and take pictures for us
        final Intent takePicturesIt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Verify if the device has a contact app to perform queries
        // if not, the app will crash
        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContactIt, PackageManager.MATCH_DEFAULT_ONLY) == null){
            mAddSuspectButton.setEnabled(false);
            mAddSuspectButton.setText(R.string.no_contacts_app);
        }

        // test if the app can take pictues, checking for the file path and camera app
        if(mPhotoFile != null && takePicturesIt.resolveActivity(packageManager) != null){
            Uri uri = Uri.fromFile(mPhotoFile);
            takePicturesIt.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(takePicturesIt, REQUEST_PHOTO);
            }
        });


        mAddSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContactIt, REQUEST_CONTACT);
            }
        });
        if(mCrime.getSuspect() != null){
            mAddSuspectButton.setText(mCrime.getSuspect());
        }

        updatePhotoView();

        // Set a listener to the text field to respond to events
        // before the text changes, when the text changes and after the text changes
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton.setText(this.dateAsString(mCrime.getDate()));
        // disable the button to not answer events(clicks);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fragmentManager, DIALOG_DATE);
            }
        });


        mTimeButton.setText(this.timeAsString(mCrime.getDate()));
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });


        mCheckBox.setChecked(mCrime.isSolved());
        // add a listener to the checkbox to change the crime status
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });
        return v;
    }

    // =====================================================================
    // Options Menu
    // =====================================================================
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.crime_menu, menu);
        MenuItem deleteItem = menu.findItem(R.id.menu_item_delete_Crime);
        if(deleteItem != null){
            Log.d("Item OK", "ITEM");
        }else{
            Log.d("Item NULL", "ITEM");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_Crime:
                CrimeLab.getInstance(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mDateButton.setText(this.dateAsString(date));
        }
        if (requestCode == REQUEST_TIME) {
            Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            Calendar crimeCalendar = Calendar.getInstance();
            crimeCalendar.setTime(mCrime.getDate());
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(time);
            crimeCalendar.set(Calendar.HOUR, timeCalendar.get(Calendar.HOUR));
            crimeCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            mCrime.setDate(crimeCalendar.getTime());
            mTimeButton.setText(this.timeAsString(mCrime.getDate()));
        }

        if(requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            //specify which fields you want your query to return values for
            String[] queryFields = new String[] { ContactsContract.Contacts.DISPLAY_NAME };
            // Perform your query
            Cursor c = getActivity()
                    .getContentResolver()
                    .query(contactUri, queryFields, null, null, null);

            try {
                //Double-check that actually get results
                if(c.getCount() == 0) return;

                //Pull out the first column of the first row of data
                //which is the suspect name
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mAddSuspectButton.setText(suspect);
            }
            // Always close the cursor
            finally {
                c.close();
            }
        }

        if(requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }
    }

    private String timeAsString(Date date) {
        // set the text of the button with actual date
        DateFormat dateFormatter = new DateFormat();
        return dateFormatter.format("HH:mm", date).toString();
    }

    private String dateAsString(Date date) {
        // set the text of the button with actual date
        DateFormat dateFormatter = new DateFormat();
        return dateFormatter.format("EEEE M dd, yyyy", date).toString();
    }

    public void updatePhotoView() {
        if(mPhotoFile  == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        }
        else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private String getCrimeReport() {
        String solvedString = null;
        if(mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        }
        else{
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        }
        else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;

    }
}
