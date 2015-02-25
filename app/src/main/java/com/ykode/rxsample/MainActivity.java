package com.ykode.rxsample;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
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
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private EditText userNameEdit;
        private EditText emailEdit;
        private Button registerButton;

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

            Observable<Boolean> userNameValid = WidgetObservable.text(userNameEdit)
                    .map(e -> e.text())
                    .map(t -> t.length() > 4);

            Observable<Boolean> emailValid = WidgetObservable.text(emailEdit)
                    .map(e -> e.text())
                    .map(t -> emailPattern.matcher(t).matches());

            emailValid.distinctUntilChanged()
                    .doOnNext( b -> Log.d("[Rx]", "Email " + (b ? "Valid" : "Invalid")))
                    .map(b -> b ? Color.BLACK : Color.RED)
                    .subscribe(color -> emailEdit.setTextColor(color));

            userNameValid.distinctUntilChanged()
                    .doOnNext( b -> Log.d("[Rx]", "Uname " + (b ? "Valid" : "Invalid")))
                    .map(b -> b ? Color.BLACK : Color.RED)
                    .subscribe(color -> userNameEdit.setTextColor(color));

            Observable<Boolean> registerEnabled =
                    Observable.combineLatest(userNameValid, emailValid, (a,b) -> a && b);
            registerEnabled.distinctUntilChanged()
                    .doOnNext( b -> Log.d("[Rx]", "Button " + (b ? "Enabled" : "Disabled")))
                    .subscribe( enabled -> registerButton.setEnabled(enabled));
        }
    }
}
