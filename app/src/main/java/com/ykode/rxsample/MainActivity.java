package com.ykode.rxsample;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import com.jakewharton.rxbinding.widget.RxTextView;

import android.app.Activity;
import android.app.Fragment;

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
                    .map(t -> t.length() > 4);

            Observable<Boolean> emailValid = RxTextView.textChanges(emailEdit)
                    .map(t -> emailPattern.matcher(t).matches());

            compoSubs.add(emailValid.distinctUntilChanged()
                    .doOnNext( b -> Log.d("[Rx]", "Email " + (b ? "Valid" : "Invalid")))
                    .map(b -> b ? Color.BLACK : Color.RED)
                    .subscribe(color -> emailEdit.setTextColor(color)));

            compoSubs.add(userNameValid.distinctUntilChanged()
                    .doOnNext( b -> Log.d("[Rx]", "Uname " + (b ? "Valid" : "Invalid")))
                    .map(b -> b ? Color.BLACK : Color.RED)
                    .subscribe(color -> userNameEdit.setTextColor(color)));

            Observable<Boolean> registerEnabled =
                    Observable.combineLatest(userNameValid, emailValid, (a,b) -> a && b);
            compoSubs.add(registerEnabled.distinctUntilChanged()
                    .doOnNext( b -> Log.d("[Rx]", "Button " + (b ? "Enabled" : "Disabled")))
                    .subscribe( enabled -> registerButton.setEnabled(enabled)));
        }
        
        @Override
        public void onDestroy() {
          super.onDestroy();
          compoSubs.unsubscribe();
        }
    }
}
