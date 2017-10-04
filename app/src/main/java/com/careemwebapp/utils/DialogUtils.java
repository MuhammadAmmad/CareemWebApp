package com.careemwebapp.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.careemwebapp.R;
import com.careemwebapp.components.CustomDialog;

/**
 * Created by yuliya on 1/26/16.
 */
public class DialogUtils {

    public static void showAlertDialog(Activity context, String message) {
        showAlertDialog(context, message, null);
    }

    public static CustomDialog showAlertDialog(Activity context, String message, DialogInterface.OnClickListener onClickOkay) {
        if (! context.isFinishing()) {

           return new CustomDialog(context)
                   .setDialogMessage(message)
                   .setIsCancelable(false)
                   .setPositiveButton(context.getString(R.string.ok), onClickOkay)
                   .showDialog();
        }
        return null;
    }

    public static CustomDialog showAlertDialog(Activity context, String title, String message,
                                              String positiveText, DialogInterface.OnClickListener onClickPositive,
                                              String negativeText, DialogInterface.OnClickListener onClickNegative,
                                              boolean cancelable) {
        if (! context.isFinishing()) {

            return new CustomDialog(context)
                    .setDialogTitle(title)
                    .setDialogMessage(message)
                    .setIsCancelable(cancelable)
                    .setPositiveButton(!TextUtils.isEmpty(positiveText)
                                    ? positiveText
                                    : context.getString(R.string.ok),
                            onClickPositive)
                    .setNegativeButton(!TextUtils.isEmpty(negativeText)
                                    ? negativeText
                                    : context.getString(R.string.cancel),
                            onClickNegative)
                    .showDialog();
        }
        return null;
    }

    public static void showAlertDialog(Activity context, String message,
            String positiveText, DialogInterface.OnClickListener onClickPositive,
            String negativeText, DialogInterface.OnClickListener onClickNegative,
            boolean cancelable) {
        showAlertDialog(context, null, message, positiveText, onClickPositive, negativeText, onClickNegative, cancelable);
    }

    public static CustomDialog showAlertDialog(Activity context, String title, String message,
                                              String positiveText, DialogInterface.OnClickListener onClickPositive ) {
        if (! context.isFinishing()) {

            return new CustomDialog(context)
                    .setDialogTitle(title)
                    .setDialogMessage(message)
                    .setIsCancelable(false)
                    .setPositiveButton(positiveText, onClickPositive)
                    .showDialog();
        }
        return null;
    }

    public static CustomDialog showAlertDialog(Activity context, String title, String message, String positiveText ) {
        return showAlertDialog(context, title, message, positiveText, null);
    }

    public static void showConnectionDialog(@NonNull final Activity activity) {
        showConnectionDialog(activity, null);
    }

    public static void showConnectionDialog(Activity activity, DialogInterface.OnClickListener listener) {
        if (!activity.isFinishing()) {
            new CustomDialog(activity)
                    .setDialogMessage(activity.getResources().getString(R.string.no_connection_error))
                    .setPositiveButton(activity.getString(R.string.ok), listener)
                    .setIsCancelable(false)
                    .showDialog();
        }
    }

    public static void showChoosePhotoDialog(final Activity context, final ChoosePhotoListener listener, String[] photoSelectOptions) {
        if (! context.isFinishing()) {

            if (photoSelectOptions == null) {
                photoSelectOptions = context.getResources().getStringArray(R.array.photo_options_create);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
            builder.setAdapter(
                    new ArrayAdapter<String>(context,
                            android.R.layout.simple_list_item_1, photoSelectOptions), null)
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });

            final AlertDialog alertDialog = builder.create();

            ListView listView = alertDialog.getListView();
            listView.setAdapter(new ArrayAdapter<String>(context,
                    android.R.layout.simple_list_item_1, photoSelectOptions));
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String option = (String) parent.getItemAtPosition(position);
                    if (option.equals(context.getString(R.string.take_new_photo))) {
                        if (listener != null) {
                            listener.onTakePhoto();
                        }
                    } else if (option.equals(context.getString(R.string.choose_from_existing))) {
                        if (listener != null) {
                            listener.onChoosePhoto();
                        }
                    } else if (option.equals(context.getString(R.string.delete_current_photo))) {
                        if (listener != null) {
                            listener.onDeletePhoto();
                        }
                    }
                    alertDialog.dismiss();
                }
            });
            listView.setDivider(null);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    public interface ChoosePhotoListener {
        void onTakePhoto();
        void onChoosePhoto();
        void onDeletePhoto();
    }
}
