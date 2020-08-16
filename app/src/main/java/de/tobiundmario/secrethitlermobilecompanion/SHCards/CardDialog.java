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

import de.tobiundmario.secrethitlermobilecompanion.R;

public class CardDialog {

    public static void showInputDialog (Context c, String title, String hint, String positive, final InputDialogSubmittedListener positiveListener, final String negative, final Runnable negativeListener) {
        final CustomDialog customDialog = createDialog(c, title, null, positive, null, negative, negativeListener, true);
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
        createDialog(c, title, message, positive, positiveListener, negative, negativeListener, false).getDialog().show();
    }

    private static CustomDialog createDialog(Context c, String title, String message, String positive, final Runnable positiveListener, final String negative, final Runnable negativeListener, boolean input) {
        //Inflate the view
        View dialogView = View.inflate(c, R.layout.card_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();

        //Retrieve the children
        TextView tvTitle = dialogView.findViewById(R.id.tv_title);
        TextView tvDesc = dialogView.findViewById(R.id.tv_description);
        TextView tvPositive = dialogView.findViewById(R.id.tv_positive);
        TextView tvNegative = dialogView.findViewById(R.id.tv_negative);
        View buttonSeparator = dialogView.findViewById(R.id.line_separator_buttons);
        final EditText dialogInput = dialogView.findViewById(R.id.dialog_input);

        if(input) {
            dialogInput.setVisibility(View.VISIBLE);
            dialogInput.requestFocus();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        else dialogInput.setVisibility(View.GONE);

        //Set the texts
        tvTitle.setText(title);

        if(message == null) tvDesc.setVisibility(View.GONE);
        else {
            tvDesc.setVisibility(View.VISIBLE);
            tvDesc.setText(message);
        }

        tvPositive.setText(positive);
        if(negative != null) {
            tvNegative.setText(negative);
            tvNegative.setVisibility(View.VISIBLE);
            buttonSeparator.setVisibility(View.VISIBLE);
        }
        else {
            tvNegative.setVisibility(View.GONE);
            buttonSeparator.setVisibility(View.GONE);
        }

        //Set the listeners
        tvNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(negativeListener != null) negativeListener.run();
                dialog.dismiss();
            }
        });

        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(positiveListener != null) positiveListener.run();
                dialog.dismiss();
            }
        });

        //Setting the view
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return new CustomDialog(dialog, dialogView);
    }

    public interface InputDialogSubmittedListener {
        void onInputDialogSubmitted(EditText inputField, Dialog rootDialog);
    }

    static class CustomDialog {
        private Dialog dialog;
        private View content;

        public CustomDialog(Dialog dialog, View content) {
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
