package fr.ihm.secureme.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.UserManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import fr.ihm.secureme.R;
import fr.ihm.secureme.callback.ContactFragmentInterface;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import fr.ihm.secureme.model.Contact;

/**
 * Created by nonau on 13/10/15.
 */
public class AddContactDialog extends DialogFragment {

    ContactFragmentInterface mContactFragmentInterface;
    AlertDialog.Builder mBuilder;
    private static final int REQUEST_CONTACT_NUMBER = 123456789;
    EditText numTextField;
    EditText messTextField;
    ImageButton indexImageButton;
    CheckBox gpsCheckbox;
    View mView;
    State mState;
    String mTitle ="";
    private AlertDialog mDialog;
    private Button mPositiveDialogButton;

    enum State {
        EMPTY,
        READY
    }

    public void init (Bundle args) {



        mState = State.EMPTY;
        numTextField = (EditText) mView.findViewById(R.id.num_edittext);
        messTextField = (EditText) mView.findViewById(R.id.mess_edittext);
        indexImageButton = (ImageButton) mView.findViewById(R.id.bt_index);
        gpsCheckbox = (CheckBox) mView.findViewById(R.id.gps_check);
        mTitle = getActivity().getString(R.string.add_text);
        if (args.containsKey("isEdit") && args.getBoolean("isEdit")) {
            mTitle = getActivity().getString(R.string.edit_text);
            numTextField.setText(args.getString("num"));
            messTextField.setText(args.getString("mess"));
            gpsCheckbox.setChecked(args.getBoolean("gps"));
        }

        numTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onTextEntry();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        messTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onTextEntry();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        indexImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, REQUEST_CONTACT_NUMBER);
            }
        });


    }

    private void onTextEntry() {
        switch (mState) {
            case EMPTY:
                Log.e("test", "EMPTY");
                if (numTextField.length() >= 10 && messTextField.length() > 5) {
                    mState = State.READY;
                    mPositiveDialogButton.setEnabled(true);
                }
                else {
                    mState = State.EMPTY;
                }
                break;
            case READY:
                Log.e("test", "READY");
                if (numTextField.length() < 10 || messTextField.length() <= 5) {
                    mState = State.EMPTY;
                    mPositiveDialogButton.setEnabled(false);
                }
                else {
                    mState = State.READY;
                }
                break;

        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView  = inflater.inflate(R.layout.add_contact_dialog, null);
        final Bundle args = getArguments();
        init(args);
        int ressource;
        if (args.containsKey("isEdit") && args.getBoolean("isEdit"))
            ressource = R.drawable.ic_edit_black_24dp;
        else
            ressource = R.drawable.ic_person_add_black_24dp;
        mBuilder.setView(mView)
                .setPositiveButton(mTitle.toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Contact c = new Contact(numTextField.getText().toString(), messTextField.getText().toString(), gpsCheckbox.isChecked());
                        if (args.containsKey("isEdit") && args.getBoolean("isEdit")) {
                            mContactFragmentInterface.dialogEditCallback(c, args.getInt("pos"));
                        } else {
                            mContactFragmentInterface.dialogCallBackHandler(c);
                        }

                    }
                })
                .setIcon(ressource)
                .setTitle(mTitle + " " + getActivity().getString(R.string.an_emergency_contact_text))
                .setNegativeButton(getActivity().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddContactDialog.this.getDialog().cancel();
                        mContactFragmentInterface.dialogCancelHandler();
                    }
                });
        mDialog = mBuilder.create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mPositiveDialogButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                mPositiveDialogButton.setEnabled(false);
                if (args.getBoolean("isEdit")) {
                    mPositiveDialogButton.setEnabled(true);
                }
            }
        });

        return mDialog;
    }

    public void setContactFragmentInterface(ContactFragmentInterface fragmentInterface) {
        mContactFragmentInterface = fragmentInterface;
    }

    public void show(Activity activity, String dial) {
        super.show(activity.getFragmentManager(), dial);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            if(data != null && requestCode == REQUEST_CONTACT_NUMBER) {
                Uri uriOfPhoneNumberRecord = data.getData();
                String idOfPhoneRecord = uriOfPhoneNumberRecord.getLastPathSegment();
                Cursor cursor = getActivity().getContentResolver().query(Phone.CONTENT_URI, new String[]{Phone.NUMBER}, Phone._ID + "=?", new String[]{idOfPhoneRecord}, null);
                if(cursor != null) {
                    if(cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        String formattedPhoneNumber = cursor.getString( cursor.getColumnIndex(Phone.NUMBER) );
                        numTextField.setText(formattedPhoneNumber);
                    }
                    cursor.close();
                }
                onTextEntry();
            }
            else if (resultCode == getActivity().RESULT_CANCELED){
                onTextEntry();
            }
        }
    }

}
