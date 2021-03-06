/**
 * This is a fragment to reject an event and try to send a message (sms) to organizer.
 * @author Team: Horario
 */

package hft.wiinf.de.horario.view;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import hft.wiinf.de.horario.R;
import hft.wiinf.de.horario.TabActivity;
import hft.wiinf.de.horario.controller.EventController;
import hft.wiinf.de.horario.controller.NotificationController;
import hft.wiinf.de.horario.controller.SendSmsController;
import hft.wiinf.de.horario.model.Event;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class EventRejectEventFragment extends Fragment {

    private static final String TAG = "EventRejectEvent";
    EditText reason_for_rejection;
    TextView reject_event_header, reject_event_description;
    Spinner spinner_reason;
    Button button_reject_event, button_dialog_delete, button_dialog_back;
    AlertDialog mDialog;
    Event selectedEvent;
    Event event;
    StringBuffer eventToStringBuffer;

    String phNumber, rejectMessage, shortTitle;
    Long creatorEventId;

    public EventRejectEventFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_reject_event, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //initialize GUI-Elements
        reason_for_rejection = view.findViewById(R.id.reject_event_editText_note);
        reason_for_rejection.setImeOptions(EditorInfo.IME_ACTION_DONE);
        reason_for_rejection.setRawInputType(InputType.TYPE_CLASS_TEXT);
        reject_event_description = view.findViewById(R.id.reject_event_textView_description);
        reject_event_header = view.findViewById(R.id.reject_event_textView_header);
        spinner_reason = view.findViewById(R.id.reject_event_spinner_reason);
        button_reject_event = view.findViewById(R.id.reject_event_button_reject);
        button_dialog_delete = view.findViewById(R.id.dialog_button_event_delete);
        button_dialog_back = view.findViewById(R.id.dialog_button_event_back);
        setSelectedEvent(EventController.getEventById(getEventID()));
        buildDescriptionEvent(EventController.getEventById(getEventID()));

        //initialize adapter
        ArrayAdapter reasonAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.reason_for_rejection, android.R.layout.simple_spinner_item);
        reasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_reason.setAdapter(reasonAdapter);

        //Make EditText-Field editable
        reason_for_rejection.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                reason_for_rejection.setFocusable(true);
                reason_for_rejection.setFocusableInTouchMode(true);
                return false;
            }
        });

        reason_for_rejection.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            //on click: close the keyboard after input is done
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                reason_for_rejection.setFocusable(false);
                reason_for_rejection.setFocusableInTouchMode(false);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;

            }
        });

        button_reject_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkForInput()) {
                    askForPermissionToDelete();
                }
            }
        });
    }

    /**
     * This method creates an AlertDialog to ask for final decision (yes or no).
     * This method return nothing. Next Steps depends on what is clicked (yes or no).
     * If "yes", method is restarting the TabActivity and calendar shows up.
     * If "no", method is going back to layout from EventRejectEventFragment.
     *
     */
    public void askForPermissionToDelete() {
        //Build dialog
        final AlertDialog.Builder dialogAskForFinalDecission = new AlertDialog.Builder(getContext());
        dialogAskForFinalDecission.setView(R.layout.dialog_afterrejectevent);
        dialogAskForFinalDecission.setTitle(R.string.titleDialogRejectEvent);
        dialogAskForFinalDecission.setCancelable(true);

        mDialog = dialogAskForFinalDecission.create();
        mDialog.show();

        //button listener on both buttons
        mDialog.findViewById(R.id.dialog_button_event_delete)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        event = EventController.getEventById((getEventID()));

                        //delete alarm for notification
                        NotificationController.deleteAlarmNotification(getContext(), event);
                        //If an Event of a recurring event is cancelled, all events
                        // of the recurring event are deleted. This way the user can Scan the
                        // Event again and confirm it again.

                        if (event.getStartEvent() != null) {
                            Event event1 = event.getStartEvent();
                            EventController.deleteEvent(event1);
                        } else {
                            EventController.deleteEvent(event);
                        }
                        //SMS
                        rejectMessage = spinner_reason.getSelectedItem().toString() + "!" + reason_for_rejection.getText().toString();
                        creatorEventId = event.getCreatorEventId();
                        Log.i("Absagegrund", rejectMessage);
                        SendSmsController.sendSMS(getContext(), phNumber, rejectMessage, false, creatorEventId, shortTitle);

                        Toast.makeText(getContext(), R.string.reject_event_hint, Toast.LENGTH_SHORT).show();
                        //restart Activity
                        Intent intent = new Intent(getActivity(), TabActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        //if button "no" has been clicked, cancel dialog.
        mDialog.findViewById(R.id.dialog_button_event_back)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.cancel();
                    }
                });

    }

    /**
     * Method to get Arguments from SavedEventDetailsFragment, AcceptedEventDetailsFragment or TabActivity
     * This Method checks which value is for EventId
     * @return the creatorEventId: Id which the event has in the database of organizer
     */
    public Long getEventID() {
        Bundle MYEventIdBundle = getArguments();
        Long MYEventIdLongResult = MYEventIdBundle.getLong("EventId");
        return MYEventIdLongResult;
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    /**
     * This method formats the output which is shown on Dialog
     *
     * @param selectedEvent: Id which the event has in the database of organizer
     */
    private void buildDescriptionEvent(Event selectedEvent) {
        //Put StringBuffer in an Array and split the Values to new String Variables
        //Index: 0 = CreatorID; 1 = StartDate; 2 = EndDate; 3 = StartTime; 4 = EndTime;
        //       5 = Repetition; 6 = ShortTitle; 7 = Place; 8 = Description;  9 = EventCreatorName
        String[] eventStringBufferArray = String.valueOf(stringBufferGenerator()).split("\\|");
        String startDate = eventStringBufferArray[1].trim();
        String endDate = eventStringBufferArray[2].trim();
        String startTime = eventStringBufferArray[3].trim();
        String endTime = eventStringBufferArray[4].trim();
        String repetition = eventStringBufferArray[5].toUpperCase().trim();
        shortTitle = eventStringBufferArray[6].trim();
        String place = eventStringBufferArray[7].trim();
        String description = eventStringBufferArray[8].trim();
        String eventCreatorName = eventStringBufferArray[9].trim();
        phNumber = selectedEvent.getCreator().getPhoneNumber();

        // Change the DataBase Repetition Information in a German String for the Repetition Element
        // like "Daily" into "täglich" and so on
        switch (repetition) {
            case "YEARLY":
                repetition = "jährlich";
                break;
            case "MONTHLY":
                repetition = "monatlich";
                break;
            case "WEEKLY":
                repetition = "wöchentlich";
                break;
            case "DAILY":
                repetition = "täglich";
                break;
            case "NONE":
                repetition = "";
                break;
            default:
                repetition = "ohne Wiederholung";
        }

        // Event shortTitel in Headline with StartDate
        reject_event_header.setText(eventCreatorName + "\n" + shortTitle + ", " + startDate);
        // Check for a Repetition Event and Change the Description Output with and without
        // Repetition Element inside.
        if (repetition.equals("")) {
            reject_event_description.setText("Am " + startDate + " findet von " + startTime + " bis "
                    + endTime + " Uhr in Raum " + place + " " + shortTitle + " statt." + "\n" + "Termindetails sind: "
                    + description);
        } else {
            reject_event_description.setText("Vom " + startDate + " bis " + endDate +
                    " findet " + repetition + " um " + startTime + "Uhr bis " + endTime + "Uhr in Raum "
                    + place + " " + shortTitle + " statt." + "\n" + "Termindetails sind: " + description);
        }
    }

    public StringBuffer stringBufferGenerator() {

        //Modify the Dateformat form den DB to get a more readable Form for Date and Time disjunct
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm");

        //Splitting String Element is the Pipe Symbol (on the Keyboard ALT Gr + <> Button = |)
        String stringSplitSymbol = " | "; //

        // Merge the Data Base Information to one Single StringBuffer with the Format:
        // CreatorID (not EventID!!), StartDate, EndDate, StartTime, EndTime, Repetition, ShortTitle
        // Place, Description and Name of EventCreator
        eventToStringBuffer = new StringBuffer();
        eventToStringBuffer.append(selectedEvent.getId() + stringSplitSymbol);
        eventToStringBuffer.append(simpleDateFormat.format(selectedEvent.getStartTime()) + stringSplitSymbol);
        eventToStringBuffer.append(simpleDateFormat.format(selectedEvent.getEndDate()) + stringSplitSymbol);
        eventToStringBuffer.append(simpleTimeFormat.format(selectedEvent.getStartTime()) + stringSplitSymbol);
        eventToStringBuffer.append(simpleTimeFormat.format(selectedEvent.getEndTime()) + stringSplitSymbol);
        eventToStringBuffer.append(selectedEvent.getRepetition() + stringSplitSymbol);
        eventToStringBuffer.append(selectedEvent.getShortTitle() + stringSplitSymbol);
        eventToStringBuffer.append(selectedEvent.getPlace() + stringSplitSymbol);
        eventToStringBuffer.append(selectedEvent.getDescription() + stringSplitSymbol);
        eventToStringBuffer.append(selectedEvent.getCreator().getName());

        return eventToStringBuffer;

    }

    /**
     * This method checks if user input is valid. If input is not valid show Toast
     * @return false: if input is not valid
     * @return true: if input is valid
     */
    private boolean checkForInput() {
        if (reason_for_rejection.getText().length() == 0) {
            Toast.makeText(getContext(), R.string.reject_event_reason, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (reason_for_rejection.getText().length() > 100) {
            Toast.makeText(getContext(), R.string.reject_event_reason_free_text_field_to_long, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (reason_for_rejection.getText().toString().startsWith(" ")) {
            Toast.makeText(getContext(), R.string.reject_event_reason_free_text_field_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!reason_for_rejection.getText().toString().matches("(\\w|\\.)(\\w|\\s|\\.)*")) {
            Toast.makeText(getContext(), R.string.reject_event_reason_special_characters, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onPause() {
        if (mDialog != null) {
            mDialog.dismiss();
        }

        super.onPause();

    }
}
