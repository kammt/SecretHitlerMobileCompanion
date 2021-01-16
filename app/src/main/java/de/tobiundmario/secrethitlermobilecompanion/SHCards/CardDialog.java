package de.tobiundmario.secrethitlermobilecompanion.SHCards;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import de.tobiundmario.secrethitlermobilecompanion.R;

public final class CardDialog {

    private static final int message = 0;
    private static final int input = 1;
    public static final int trackCreation = 2;

    private CardDialog() {}

    public static void showInputDialog (Context c, String title, String hint, String positive, final InputDialogSubmittedListener positiveListener, final String negative, final Runnable negativeListener) {
        final CustomDialog customDialog = createDialog(c, title, null, positive, null, negative, negativeListener, input);
        final Dialog dialog = customDialog.getDialog();
        final View contentView = customDialog.getContent();

        final EditText dialogInput = contentView.findViewById(R.id.dialog_input);
        dialogInput.setHint(hint);
        TextView positiveButton = contentView.findViewById(R.id.tv_positive);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positiveListener.onInputDialogSubmitted(dialogInput, dialog);
            }
        });

        customDialog.getDialog().show();
    }

    public static void showMessageDialog(Context c, String title, String message, String positive, final Runnable positiveListener, final String negative, final Runnable negativeListener) {
        createDialog(c, title, message, positive, positiveListener, negative, negativeListener, CardDialog.message).getDialog().show();
    }


    public static CustomDialog createDialog(Context c, String title, String message, String positive, final Runnable positiveListener, final String negative, final Runnable negativeListener, int type) {
        //Inflate the view
        View dialogView = View.inflate(c, R.layout.card_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();

        //Retrieve the children
        TextView tvDesc = dialogView.findViewById(R.id.tv_description);

        processLayoutChanges(type, dialog, dialogView);

        //Set the Title and Description
        ((TextView) dialogView.findViewById(R.id.tv_title)).setText(title);
        if(message == null) tvDesc.setVisibility(View.GONE);
        else {
            tvDesc.setVisibility(View.VISIBLE);
            tvDesc.setText(message);
        }
        //Set the listeners and text
        setupTextViewButtons(dialogView, new String[] {positive, negative}, new Runnable[] {positiveListener, negativeListener}, dialog);

        //Setting the view
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return new CustomDialog(dialog, dialogView);
    }

    private static void processLayoutChanges(int type, Dialog dialog, View dialogView) {
        final EditText dialogInput = dialogView.findViewById(R.id.dialog_input);
        ConstraintLayout container_newTrack = dialogView.findViewById(R.id.container_newTrack);

        if(type == input) {
            dialogInput.setVisibility(View.VISIBLE);
            dialogInput.requestFocus();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            container_newTrack.setVisibility(View.GONE);
        }
        else  if (type == CardDialog.message) {
            dialogInput.setVisibility(View.GONE);
            container_newTrack.setVisibility(View.GONE);
        } else {
            container_newTrack.setVisibility(View.VISIBLE);
            dialogInput.setVisibility(View.GONE);
            dialogView.findViewById(R.id.tv_description).setVisibility(View.GONE);
        }
    }

    private static void setNegativeButtonText(TextView tvNegative, String negative, View buttonSeparator) {
        if(negative != null) {
            tvNegative.setText(negative);
            tvNegative.setVisibility(View.VISIBLE);
            buttonSeparator.setVisibility(View.VISIBLE);
        } else {
            tvNegative.setVisibility(View.GONE);
            buttonSeparator.setVisibility(View.GONE);
        }
    }

    private static void setupTextViewButtons(View dialogView, String[] buttonText, final Runnable[] buttonListeners, final Dialog dialog) {
        TextView tvPositive = dialogView.findViewById(R.id.tv_positive);
        TextView tvNegative = dialogView.findViewById(R.id.tv_negative);

        tvPositive.setText(buttonText[0]);
        setNegativeButtonText(tvNegative, buttonText[1], dialogView.findViewById(R.id.line_separator_buttons));

        tvNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable negativeListener = buttonListeners[1];
                if(negativeListener != null) negativeListener.run();
                dialog.dismiss();
            }
        });

        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable positiveListener = buttonListeners[0];
                if(positiveListener != null) positiveListener.run();
                dialog.dismiss();
            }
        });
    }

    public interface InputDialogSubmittedListener {
        void onInputDialogSubmitted(EditText inputField, Dialog rootDialog);
    }

    static class CustomDialog {
        private Dialog dialog;
        private View content;

        CustomDialog(Dialog dialog, View content) {
            this.dialog = dialog;
            this.content = content;
        }

        public Dialog getDialog() {
            return dialog;
        }

        public View getContent() {
            return content;
        }
    }
}
