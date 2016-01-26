package com.ykode.rxsample;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.CharSequence;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import com.jakewharton.rxbinding.widget.RxTextView;

import android.app.Activity;
import android.app.Fragment;

import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Action1;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private EditText userNameEdit;
        private EditText emailEdit;
        private Button registerButton;

        private CompositeSubscription compoSubs; 

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            userNameEdit = (EditText) rootView.findViewById(R.id.edtUserName);
            emailEdit = (EditText) rootView.findViewById(R.id.edtEmail);
            registerButton = (Button) rootView.findViewById(R.id.buttonRegister);

            return rootView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            final Pattern emailPattern = Pattern.compile(
                    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

            compoSubs = new CompositeSubscription();
            
            Observable<Boolean> userNameValid = RxTextView.textChanges(userNameEdit)
                    .map(new Func1<CharSequence, Boolean>() {
                      @Override
                      public Boolean call(final CharSequence t) {
                        return t.length() > 4;
                      }
                    });

            Observable<Boolean> emailValid = RxTextView.textChanges(emailEdit)
                    .map(new Func1<CharSequence, Boolean>() {
                      @Override
                      public Boolean call(final CharSequence t) {
                        return emailPattern.matcher(t).matches();
                      }
                    }); 

            final Action1<Boolean> logF = new Action1<Boolean>() {
              @Override
              public void call(final Boolean b) {
                Log.d("[Rx]", "Email " + (b ? "Valid" : "Invalid"));
              }
            };

            final Func1<Boolean, Integer> colorF = new Func1<Boolean, Integer>() {
              @Override
              public final Integer call(final Boolean b) {
                return (b.booleanValue() ? Color.BLACK : Color.RED);
              }
            }; 

            compoSubs.add(emailValid.distinctUntilChanged()
                    .doOnNext(logF)
                    .map(colorF)
                    .subscribe(new Action1<Integer>() {
                      @Override
                      public void call(final Integer c) {
                        emailEdit.setTextColor(c);
                      }
                    })
                  );

            compoSubs.add(userNameValid.distinctUntilChanged()
                    .doOnNext(logF)
                    .map(colorF)
                    .subscribe(new Action1<Integer>() {
                      @Override
                      public void call(final Integer c) {
                        userNameEdit.setTextColor(c);
                      }
                    })
                  );

            Observable<Boolean> registerEnabled =
              Observable.combineLatest(userNameValid, emailValid,
                new Func2<Boolean, Boolean, Boolean>() {
                  @Override
                  public final Boolean call(final Boolean a, Boolean b) {
                    return a && b;
                  }
                });

            compoSubs.add(registerEnabled.distinctUntilChanged()
                    .doOnNext(new Action1<Boolean>() {
                      @Override
                      public void call(final Boolean b) {
                        Log.d("[Rx]", "Button " + (b.booleanValue() ? "Enabled" : "Disabled"));
                      }
                    })
                    .subscribe( new Action1<Boolean>() {
                      @Override
                      public void call(final Boolean enabled) {
                        registerButton.setEnabled(enabled.booleanValue());
                      }
                    })
            );
        }
        
        @Override
        public void onDestroy() {
          super.onDestroy();
          compoSubs.unsubscribe();
        }
    }
}
