package org.openforis.collect.android.gui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.App;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.util.CollectPermissions;

/**
 * @author Stefano Ricci
 */
public class MainActivity extends BaseActivity {

    static final String EXIT_FLAG = "EXIT";

    private SurveySpinnerAdapter surveyAdapter;
    private Spinner surveySpinner;

    private void initialize() {
        try {
            ServiceLocator.init(this);

            surveyAdapter = new SurveySpinnerAdapter(this);

            setContentView(R.layout.activity_main);

            TextView mainTitle = findViewById(R.id.mainTitle);
            mainTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/caviar_dreams_normal.ttf"));

            TextView versionText = findViewById(R.id.appVersion);
            versionText.setText(App.versionFull(this));

            initializeSurveySpinner();

            findViewById(R.id.goToDataEntry).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int selectedSurveyPosition = surveySpinner.getSelectedItemPosition();
                    boolean surveySelected = surveyAdapter.isSurveyItem(selectedSurveyPosition);
                    handleGoToDataEntryButtonClick(surveySelected);
                }
            });
            findViewById(R.id.importDemoSurvey).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //empty survey list, show survey list activity
                    SurveyListActivity.startActivity(MainActivity.this);
                }
            });
            findViewById(R.id.importNewSurvey).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    handleImportNewSurvey();
                }
            });
        } catch (WorkingDirNotWritable ignore) {
            DialogFragment newFragment = new SecondaryStorageNotFoundFragment();
            newFragment.show(getSupportFragmentManager(), "secondaryStorageNotFound");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        if (getIntent().getBooleanExtra(EXIT_FLAG, false)) {
            finish();
            return;
        }

        //if (CollectPermissions.checkStoragePermissionOrRequestIt(this)) {
            initialize();
        //}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!CollectPermissions.isPermissionGranted(grantResults))
            return;

        if (requestCode == CollectPermissions.PermissionRequest.STORAGE.getCode()) {
            initialize();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (surveyAdapter != null) {
            surveyAdapter.reloadSurveys();

            if (surveyAdapter.isSurveyListEmpty()) {
                Views.hide(findViewById(R.id.notEmptySurveyListFrame));
                Views.show(findViewById(R.id.emptySurveyListFrame));
            } else {
                Views.hide(findViewById(R.id.emptySurveyListFrame));
                Views.show(findViewById(R.id.notEmptySurveyListFrame));
            }
        }
    }

    private void initializeSurveySpinner() {
        surveySpinner = findViewById(R.id.surveySpinner);
        surveySpinner.setAdapter(surveyAdapter);

        String currentSurveyName = SurveyImporter.selectedSurvey(this);
        surveySpinner.setSelection(surveyAdapter.getItemPosition(currentSurveyName));

        surveySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (surveyAdapter.isImportSurveyItem(position)) {
                    handleImportNewSurvey();
                } else {
                    SurveyBaseAdapter.SurveyItem selectedSurveyItem = ((SurveyBaseAdapter.SurveyItem) surveyAdapter.getItem(position));
                    String selectedSurveyName = selectedSurveyItem.name;
                    handleSurveySelected(selectedSurveyName);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void handleSurveySelected(String selectedSurveyName) {
        String currentSurveyName = SurveyImporter.selectedSurvey(this);
        if (! selectedSurveyName.equals(currentSurveyName)) {
            SurveyImporter.selectSurvey(selectedSurveyName, this);
            SurveyNodeActivity.restartActivity(this);
        }
    }

    private void handleImportNewSurvey() {
        SurveyListActivity.startActivityAndShowImportDialog(this);
    }

    private void handleGoToDataEntryButtonClick(boolean surveySelected) {
        if (surveySelected) {
            if (ServiceLocator.init(this)) {
                SurveyNodeActivity.restartActivity(this);
            }
        } else {
            SurveyListActivity.startActivity(this);
        }
    }
}
