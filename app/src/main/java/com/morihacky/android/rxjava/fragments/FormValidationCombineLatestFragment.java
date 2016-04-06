package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.RxUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscription;

import static android.text.TextUtils.isEmpty;
import static android.util.Patterns.EMAIL_ADDRESS;

@SuppressWarnings("ALL")
public class FormValidationCombineLatestFragment
      extends BaseFragment {

    @Bind(R.id.btn_demo_form_valid) TextView btnValidForm;
    @Bind(R.id.demo_combl_email) EditText email;
    @Bind(R.id.demo_combl_password) EditText password;
    @Bind(R.id.demo_combl_num) EditText number;

    private Observable<Boolean> validEmailObservable;
    private Observable<Boolean> validPasswordObservable;
    private Observable<Boolean> validNumberObservable;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest, container, false);
        ButterKnife.bind(this, layout);

        validEmailObservable =
            getObservableFromEditText(email)
                .map(newEmail -> isEmailValid(newEmail));

        validPasswordObservable =
            getObservableFromEditText(password)
                .map(newPassword -> isPasswordValid(newPassword));

        validNumberObservable =
            getObservableFromEditText(number)
                .map(newNumber -> isNumberValid(newNumber));

        combineLatestEvents();

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void combineLatestEvents() {
        _subscription = Observable.combineLatest(
                validEmailObservable,
                validPasswordObservable,
                validNumberObservable,
                (emailValid, passwordValid, numberValid) -> {
                    if (!emailValid) {
                        email.setError("Invalid email!");
                    }
                    if (!passwordValid) {
                        password.setError("Invalid password!");
                    }
                    if (!numberValid) {
                        number.setError("Invalid number!");
                    }
                    return emailValid && passwordValid && numberValid;
                })
                .subscribe(new Observer<Boolean>() {
                  @Override
                  public void onCompleted() {
                  }

                  @Override
                  public void onError(Throwable e) {
                      e.printStackTrace();
                  }

                  @Override
                  public void onNext(Boolean formValid) {
                      if (formValid) {
                          btnValidForm.setBackgroundColor(getResources().getColor(R.color.blue));
                      } else {
                          btnValidForm.setBackgroundColor(getResources().getColor(R.color.gray));
                      }
                  }
              });
    }

    public boolean isNumberValid(CharSequence number) {
        if (isEmpty(number)) return false;
        int num = Integer.parseInt(number.toString());
        return num > 0 && num <= 100;
    }

    public boolean isPasswordValid(CharSequence password) {
        return !isEmpty(password) && password.length() > 8;
    }

    public boolean isEmailValid(CharSequence email) {
        return !isEmpty(email) && EMAIL_ADDRESS.matcher(email).matches();
    }





    private Subscription _subscription = null;

    public Observable<CharSequence> getObservableFromEditText(EditText editText) {
        return RxTextView.textChanges(editText).skip(1);
    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.unsubscribeIfNotNull(_subscription);
    }
}
