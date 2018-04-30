package hft.wiinf.de.horario.view;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import hft.wiinf.de.horario.CaptureActivityPortrait;
import hft.wiinf.de.horario.R;
import hft.wiinf.de.horario.model.Event;


public class QRScannerActivity extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "QRScannerFragmentActivity";
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private String qrResult;
    private RelativeLayout mScannerResult_RelativeLayout_Main, mScannerResult_RelativeLayout_ButtonFrame;
    private RelativeLayout mTEST;
    private TextView mScannerResult_TextureView_Headline, mScannerResult_TextureView_EventText;
    private Button mScannerResult_Button_addEvent, mScannerResult_Buttone_saveWithoutassent, mScannerResult_Button_rejectEvent;
    private StringBuffer mScannerResult_StingBuffer_QRResultModefied;
    private Event mEvent;

    @Override
    public void onActivityCreated(Bundle savednstanceState) {
        super.onActivityCreated(savednstanceState);
    }

    //The Scanner start with the Call form CalendarActivity directly
    //ToDo Versuchen die Ansicht immernoch zu verbessern ..
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.activity_reader, container, false);

        return view;


    }

    @SuppressLint("ResourceType")
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mScannerResult_RelativeLayout_Main = view.findViewById(R.id.scanner_result_relativeLayout_main);
        mScannerResult_TextureView_EventText = view.findViewById(R.id.scanner_result_textview_eventText);
        mScannerResult_TextureView_Headline = view.findViewById(R.id.scanner_result_textView_headline);
        mTEST = view.findViewById(R.id.scanner_result_temp_newFragment);

        showCameraPreview();
    }
    private void showCameraPreview() {
        // BEGIN_INCLUDE(startCamera)
        // Check if the Camera permission has been granted
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            Snackbar.make(mScannerResult_RelativeLayout_Main, "HEy das ging ja vorher schon",
                    Snackbar.LENGTH_SHORT).show();
            startScanner();
        } else {
            // Permission is missing and must be requested.
            requestCameraPermission();
        }
        // END_INCLUDE(startCamera)
    }

    /**
     * Requests the {@link android.Manifest.permission#CAMERA} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(mScannerResult_RelativeLayout_Main, "Muss das hier gehen?",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            }).show();

        } else {
            Snackbar.make(mScannerResult_RelativeLayout_Main, "Cam ist nicht errichbar!", Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(mScannerResult_RelativeLayout_Main, "Da geht ja was!!",
                        Snackbar.LENGTH_SHORT)
                        .show();
                startScanner();
            } else {
                // Permission request was denied.
                Snackbar.make(mScannerResult_RelativeLayout_Main, "Du darfst hier nicht rein!!!",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }


    public void startScanner(){
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(CaptureActivityPortrait.class); //Necessary to use the intern Sensor for Orientation
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Termincode scannen\n" +
                "Halte dein Smartphone vor den QR-Code und \n" +
                "scanne ihn ab, um den Termin zu öffnen");
        integrator.setCameraId(0);
        //ToDo Größe des Anzeigebereiches im Hochformat ändern.
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    //Check the Scanner Result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                qrResult = "Canceld";
            } else {
                qrResult = result.getContents();
            }
            displayQRResult();

        }
    }

    @SuppressLint({"SetTextI18n", "LongLogTag"})
    private void displayQRResult() {
        if (getActivity() != null && qrResult != null) {

            mScannerResult_StingBuffer_QRResultModefied = new StringBuffer(qrResult);

            mScannerResult_TextureView_Headline.setText("Headline");

            mScannerResult_TextureView_EventText.setText(qrResult);
            qrResult = null;
        }
    }

    // Put the Scanned Result to the Database
    //ToDo Speichern in die DB muss noch ausgearbeitet werden.
    private void qrResultToDatabase(){
        if (getActivity() != null && qrResult != null) {

            String test = Event.load(Event.class, 1).toString();
            Toast.makeText(getContext(), test, Toast.LENGTH_SHORT).show();
        }

    }
}
