package de.tobiundmario.secrethitlermobilecompanion.SHCards;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;

import de.tobiundmario.secrethitlermobilecompanion.ExceptionHandler;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.SharedPreferencesManager;
import de.tobiundmario.secrethitlermobilecompanion.SpinnerAdapters.TrackActionSpinnerAdapter;

public final class CardDialog {

    private static int message = 0;
    private static int input = 1;
    private static int trackCreation = 2;

    private static View.OnClickListener listener_forward_one;
    private static View.OnClickListener listener_create_track = null;
    private static View.OnClickListener listener_backward_two = null;

    private CardDialog() {}

    public static void destroy() {
        listener_forward_one = null;
        listener_create_track = null;
        listener_backward_two = null;
    }

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

    public static void showTrackCreationDialog(final Context c) {
        String positive = c.getString(R.string.dialog_mismatching_claims_btn_continue);
        String negative = c.getString(R.string.dialog_mismatching_claims_btn_cancel);

        final CustomDialog customDialog = createDialog(c, c.getString(R.string.new_fascistTrack), null, positive, null, negative, null, trackCreation);

        final Dialog dialog = customDialog.getDialog();
        final View dialogView = customDialog.getContent();

        final ConstraintLayout container_actions = dialogView.findViewById(R.id.container_actions);
        container_actions.setVisibility(View.GONE);

        final ConstraintLayout container_general = dialogView.findViewById(R.id.container_general);
        container_general.setVisibility(View.VISIBLE);

        final CheckBox cb_manualMode = dialogView.findViewById(R.id.checkBox_manualMode);
        final TextView tvPositive = dialogView.findViewById(R.id.tv_positive);
        final TextView tvNegative = dialogView.findViewById(R.id.tv_negative);

        final EditText input_name = dialogView.findViewById(R.id.et_trackName);
        final EditText input_fpolicies = dialogView.findViewById(R.id.et_fpolicies);
        final EditText input_lpolicies = dialogView.findViewById(R.id.et_lpolicies);
        final EditText input_electionTrackerLength = dialogView.findViewById(R.id.et_electionTrackerLength);
        final TextView title_eTrackerLength = dialogView.findViewById(R.id.et_title_eTracker);

        final LinearLayout ll_actions = dialogView.findViewById(R.id.actions);

        cb_manualMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    tvPositive.setText(c.getString(R.string.done));

                    input_electionTrackerLength.setVisibility(View.GONE);
                    title_eTrackerLength.setVisibility(View.GONE);
                } else {
                    tvPositive.setText(c.getString(R.string.dialog_mismatching_claims_btn_continue));

                    input_electionTrackerLength.setVisibility(View.VISIBLE);
                    title_eTrackerLength.setVisibility(View.VISIBLE);
                }
            }
        });

        listener_forward_one = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean manual = cb_manualMode.isChecked();
                boolean error = false;
                String name = input_name.getText().toString();
                String fpolicies = input_fpolicies.getText().toString();
                String lpolicies = input_lpolicies.getText().toString();
                String eTrackerLength = input_electionTrackerLength.getText().toString();

                if(name.equals("")) {
                    input_name.setError(c.getString(R.string.cannot_be_empty));
                    error = true;
                }

                if(fpolicies.equals("") || Integer.parseInt(fpolicies) <= 0) {
                    input_fpolicies.setError(c.getString(R.string.error_invalid_value));
                    error = true;
                }

                if(lpolicies.equals("") || Integer.parseInt(lpolicies) <= 0) {
                    input_lpolicies.setError(c.getString(R.string.error_invalid_value));
                    error = true;
                }

                if (!manual && (eTrackerLength.equals("") || Integer.parseInt(eTrackerLength) <= 0)) {
                    input_electionTrackerLength.setError(c.getString(R.string.error_invalid_value));
                    error = true;
                }


                if(error) return;

                if(manual) {
                    listener_create_track.onClick(tvPositive);
                    dialog.dismiss();
                } else {
                    //Show the new contianer
                    container_actions.setVisibility(View.VISIBLE);
                    container_general.setVisibility(View.GONE);

                    //Change the buttons
                    tvPositive.setText(c.getString(R.string.done));
                    tvPositive.setOnClickListener(listener_create_track);

                    tvNegative.setText(c.getString(R.string.back));
                    tvNegative.setOnClickListener(listener_backward_two);

                    //Create the LayoutParams for the spinners
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.bottomMargin = 50;

                    //We now check if there are too little or too many spinners in the layout
                    int fascistPolicies = Integer.parseInt(fpolicies);
                    int children = ll_actions.getChildCount();

                    if(children > fascistPolicies) {
                        //Too many spinners, we need to remove some
                        for (int i = children - 1; i > fascistPolicies - 1; i--) {
                            ll_actions.removeViewAt(i);
                        }
                    } else if(children < fascistPolicies) {
                        //Too little spinners, we add some
                        for (int i = children; i < fascistPolicies; i++) {
                            Spinner spinner = new Spinner(c);
                            spinner.setAdapter(new TrackActionSpinnerAdapter(c));
                            spinner.setLayoutParams(params);

                            spinner.setDropDownWidth(spinner.getLayoutParams().width);

                            ll_actions.addView(spinner);
                        }
                    }
                }
            }
        };

        listener_backward_two = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container_actions.setVisibility(View.GONE);
                container_general.setVisibility(View.VISIBLE);

                //Change the buttons
                tvPositive.setText(c.getString(R.string.dialog_mismatching_claims_btn_continue));
                tvPositive.setOnClickListener(listener_forward_one);

                tvNegative.setText(c.getString(R.string.dialog_mismatching_claims_btn_cancel));
                tvNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        };

        listener_create_track = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FascistTrack fascistTrack = new FascistTrack();

                String name = input_name.getText().toString();
                fascistTrack.setName(name);

                int fpolicies = Integer.parseInt(input_fpolicies.getText().toString());
                fascistTrack.setFasPolicies(fpolicies);
                int lpolicies = Integer.parseInt(input_lpolicies.getText().toString());
                fascistTrack.setLibPolicies(lpolicies);

                boolean manual = cb_manualMode.isChecked();

                if(!manual) {
                    int[] actions = new int[fpolicies];
                    for (int i = 0; i < ll_actions.getChildCount(); i++) {
                        Spinner spinnerAtPos = (Spinner) ll_actions.getChildAt(i);

                        int selection = (int) spinnerAtPos.getSelectedItem();
                        actions[i] = selection;
                    }
                    fascistTrack.setActions(actions);

                    fascistTrack.setElectionTrackerLength(Integer.parseInt(input_electionTrackerLength.getText().toString()));
                } else {
                    fascistTrack.setManualMode(true);
                }

                try {
                    SharedPreferencesManager.writeFascistTrack(fascistTrack, c);
                } catch (JSONException e) {
                    ExceptionHandler.showErrorSnackbar(e, "CardDialog.showTrackCreationDialog() (SharedPreferencesManager.writeFascistTrack())");
                }

                dialog.dismiss();
            }
        };

        tvPositive.setOnClickListener(listener_forward_one);


        dialog.show();
    }

    private static CustomDialog createDialog(Context c, String title, String message, String positive, final Runnable positiveListener, final String negative, final Runnable negativeListener, int type) {
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
            tvDesc.setVisibility(View.GONE);
        }

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
