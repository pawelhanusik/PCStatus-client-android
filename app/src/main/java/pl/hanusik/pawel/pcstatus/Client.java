package pl.hanusik.pawel.pcstatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pl.hanusik.pawel.pcstatus.models.Model;
import pl.hanusik.pawel.pcstatus.models.Notification;
import pl.hanusik.pawel.pcstatus.models.Progress;
import pl.hanusik.pawel.pcstatus.models.Task;

public class Client {
    /*
     * Class responsible for handling connection to backend server
     * */

    private final TokenManager tokenManager;
    private final Context context;
    private String baseUrl;
    private String tokenName;

    public enum Error {
        UNKNOWN,
        INVALID_URL,
        UNEXPECTED_HTML_CODE,
        UNAUTHENTICATED
    }

    public Client(Context context) {
        this.context = context;
        this.tokenManager = new TokenManager(this.context);

        this.handleSharedPreferences();
    }

    private void handleSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        this.tokenName = sharedPreferences.getString("prefs_token_name", "android_client");
        this.baseUrl = sharedPreferences.getString("server_url", "http://127.0.0.1:8000");

        sharedPreferences.registerOnSharedPreferenceChangeListener((SharedPreferences sharedPreferences1, String key) -> {
            if (key.equals("token_name")) {
                this.changeTokenName(
                        sharedPreferences1.getString("token_name", "android_client")
                );
            } else if (key.equals("server_url")) {
                this.changeBaseUrl(
                    sharedPreferences1.getString("server_url", "http://127.0.0.1:8000")
                );
            }
        });
    }

    private void changeBaseUrl(String newBaseUrl) {
        this.baseUrl = newBaseUrl;
    }

    private void changeTokenName(String newTokenName) {
        if (this.tokenName.equals(newTokenName)) {
            return;
        }

        this.tokenName = newTokenName;
        this.logout((success) -> {
            if (!success) {
                this.showToast(context.getString(R.string.client_token_name_change_please_logout));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
    }

    public void login(String username, String password, Callback<Boolean> callback) {
        TaskRunner taskRunner = new TaskRunner();
        taskRunner.executeAsync(new Request(
                baseUrl + "api/user/login",
                "username=" + username + "&password=" + password + "&token_name=" + this.tokenName
        ), (Response response) -> {
            boolean result;

            if (!response.success) {
                this.showToast(context.getString(R.string.client_invalid_url));
                result = false;
            } else if (response.code == 200) {
                this.tokenManager.setToken(response.message);
                result = true;
            } else {
                this.showToast(context.getString(R.string.client_invalid_credentials));
                result = false;
            }

            callback.onComplete(result);
        });
    }

    public void logout() {
        this.logout(null);
    }

    public void logout(Callback<Boolean> callback) {
        TaskRunner taskRunner = new TaskRunner();

        Request request = new Request(
                baseUrl + "api/user/logout",
                ""
        );
        request.setToken(this.tokenManager.getToken());

        taskRunner.executeAsync(request, (Response response) -> {
            boolean result;

            if (!response.success) {
                this.showToast(context.getString(R.string.client_invalid_url));
                result = false;
            } else if (response.code == 200) {
                this.tokenManager.clearToken();
                result = true;
            } else {
                this.showToast(context.getString(R.string.client_unknown_error));
                result = false;
            }

            if (callback != null) {
                callback.onComplete(result);
            }
        });
    }

    void getModelsIndex(
            Model.Type modelType,
            Callback<ArrayList<Model>> callback,
            Callback<Error> errorCallback,
            Date fromDate
    ) {
        String modelUrl;

        if (modelType == Model.Type.NOTIFICATION) {
            modelUrl = Notification.getUrl();
        } else if (modelType == Model.Type.PROGRESS) {
            modelUrl = Progress.getUrl();
        } else if (modelType == Model.Type.TASK) {
            modelUrl = Task.getUrl();
        } else {
            this.showToast("Error: Trying to fetch invalid model name.");
            errorCallback.onComplete(Error.UNKNOWN);
            return;
        }

        TaskRunner taskRunner = new TaskRunner();
        String url = baseUrl + "api/" + modelUrl;
        if (fromDate != null) {
            url += "?fromDate=" + DateUtils.getStringFromDate(fromDate);
        }
        Request request = new Request(url);
        request.setToken(this.tokenManager.getToken());
        taskRunner.executeAsync(request, (Response response) -> {
            if (!response.success) {
                this.showToast(context.getString(R.string.client_invalid_url));
                errorCallback.onComplete(Error.INVALID_URL);
            } else if (response.code == 401) {
                errorCallback.onComplete(Error.UNAUTHENTICATED);
            } else if (response.code != 200) {
                this.showToast("Error: HTML code: " + response.code + ".");
                errorCallback.onComplete(Error.UNEXPECTED_HTML_CODE);
            } else {
                Log.d("Client response", response.message);

                try {
                    ArrayList<Model> ret = new ArrayList<>();
                    JSONObject data = new JSONObject(response.message);

                    JSONArray modelsJson = data.getJSONArray("data");
                    int modelsCount = modelsJson.length();
                    for (int i = 0; i < modelsCount; ++i) {
                        JSONObject modelJson = modelsJson.getJSONObject(i);

                        Model model;

                        if (modelType == Model.Type.NOTIFICATION) {
                            model = Notification.fromJson(modelJson);
                        } else if (modelType == Model.Type.PROGRESS) {
                            model = Progress.fromJson(modelJson);
                        } else {
                            model = Task.fromJson(modelJson);
                        }

                        if (model != null) {
                            ret.add(model);
                        }
                    }

                    callback.onComplete(ret);
                } catch (JSONException e) {
                    e.printStackTrace();
                    errorCallback.onComplete(Error.UNKNOWN);
                }
            }
        });
    }

    private static class TaskRunner {
        private final Executor executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        public <T> void executeAsync(Callable<T> callable, Callback<T> callback) {
            executor.execute(() -> {
                final T result;
                try {
                    result = callable.call();
                    handler.post(() -> callback.onComplete(result));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static class Response {
        public boolean success;
        public long code;
        public String message;
    }

    private static class Request implements Callable<Response> {
        private final String url;
        private String postfields = null;
        private String token = null;

        public Request(String url) {
            this.url = url;
        }

        public Request(String url, String postfields) {
            this.url = url;
            this.postfields = postfields;
        }

        public void setToken(String token) {
            this.token = token;
        }

        private String readStream(InputStream is) {
            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[1024];
                while (-1 != (len = is.read(buffer))) {
                    bo.write(buffer, 0, len);
                }
                return bo.toString();
            } catch (IOException e) {
                return "";
            }
        }

        @Override
        public Response call() {
            Response response = new Response();

            URL url = null;
            try {
                url = new URL(this.url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (url == null) {
                response.success = false;
                return response;
            }

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setUseCaches(false);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);

                if (this.token != null) {
                    urlConnection.addRequestProperty("Authorization", "Bearer " + this.token);
                }

                if (this.postfields != null) {
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");

                    OutputStream os = urlConnection.getOutputStream();
                    os.write(postfields.getBytes());
                    os.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (urlConnection == null) {
                response.success = false;
                return response;
            }

            String data = "Err";
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                data = this.readStream(in);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            response.success = true;
            try {
                response.code = urlConnection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();

                response.code = 0;
            }
            response.message = data;

            return response;
        }
    }
}
