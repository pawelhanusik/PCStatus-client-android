package pl.hanusik.pawel.pcstatus;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import pl.hanusik.pawel.pcstatus.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Client client = new Client(this);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                client.login(
                    usernameEditText.getText().toString(),
                    passwordEditText.getText().toString(),
                    (wasLoginSuccessful) -> {
                        loadingProgressBar.setVisibility(View.GONE);

                        if (wasLoginSuccessful) {
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            showLoginFailed();
                        }
                    }
                );
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    private void showLoginFailed() {
        Toast.makeText(
                getApplicationContext(),
                getString(R.string.client_invalid_credentials),
                Toast.LENGTH_SHORT
        ).show();
    }
}