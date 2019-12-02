package br.com.bossini.usjt_ccp3an_bua_weather_forecast;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView weatherRecyclerView;
    private WeatherAdapter adapter;
    private List<Weather> previsoes;
    private RequestQueue requestQueue;
    // private EditText locationEditText;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_CODE_GPS = 1001;
    private double latitudeAtual, longitudeAtual;
    private TextView latTextView;
    private TextView longTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // locationEditText = findViewById(R.id.locationEditText);
        latTextView = findViewById(R.id.textView);
        longTextView = findViewById(R.id.textView2);
        requestQueue = Volley.newRequestQueue(this);
        weatherRecyclerView =
                findViewById(R.id.weatherRecyclerView);
        previsoes =
                new ArrayList<>();
        adapter = new WeatherAdapter(this, previsoes);
        LinearLayoutManager llm =
                new LinearLayoutManager(this);
        weatherRecyclerView.setAdapter(adapter);
        weatherRecyclerView.setLayoutManager(llm);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                latitudeAtual = lat;
                longitudeAtual = lon;
                latTextView.setText(String.format("Latitude: %.2f", lat));
                longTextView.setText(String.format("Longitude: %.2f", lon));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                // String cidade = locationEditText.getText().toString();
                obtemPrevisoes(latitudeAtual, longitudeAtual);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GPS) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0, 0, locationListener);
                }
            } else {
                Toast.makeText(this, getString(R.string.no_gps_no_app),
                        Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    public void obtemPrevisoes(double lat, double lon) {
        String url = getString(
                R.string.web_service_url_0,
                lat,
                lon,
                getString(R.string.api_key)
        );
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                (resultado) -> {
                    //processar o json
                    previsoes.clear();
                    try {
//                        JSONArray list = resultado.getJSONArray("list");
//                        for (int i = 0; i < list.length(); i++){
                        long dt = resultado.getLong("dt");
                        JSONObject main = resultado.getJSONObject("main");
                        double temp_min =
                                main.getDouble("temp_min");
                        double temp_max =
                                main.getDouble("temp_max");
                        double humidity =
                                main.getDouble("humidity");
                        JSONArray weather = resultado.getJSONArray("weather");
                        String description =
                                weather.getJSONObject(0).
                                        getString("description");
                        String icon =
                                weather.getJSONObject(0).getString("icon");
                        Weather w =
                                new Weather(dt, temp_min,
                                        temp_max, humidity, description, icon);
                        previsoes.add(w);
//                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                (excecao) -> {
                    Toast.makeText(
                            this,
                            getString(R.string.connect_error),
                            Toast.LENGTH_SHORT
                    ).show();
                    excecao.printStackTrace();
                }
        );
        requestQueue.add(req);
    }
}

class WeatherViewHolder extends RecyclerView.ViewHolder {
    public ImageView conditionImageView;
    public TextView dayTextView;
    public TextView lowTextView;
    public TextView highTextView;
    public TextView humidityTextView;

    public WeatherViewHolder(View raiz) {
        super(raiz);
        this.conditionImageView =
                raiz.findViewById(R.id.conditionImageView);
        this.dayTextView =
                raiz.findViewById(R.id.dayTextView);
        this.lowTextView =
                raiz.findViewById(R.id.lowTextView);
        this.highTextView =
                raiz.findViewById(R.id.highTextView);
        this.humidityTextView =
                raiz.findViewById(R.id.humidityTextView);
    }
}

class WeatherAdapter
        extends RecyclerView.Adapter<WeatherViewHolder> {

    private Context context;
    private List<Weather> previsoes;

    public WeatherAdapter(Context context, List<Weather> previsoes) {
        this.context = context;
        this.previsoes = previsoes;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =
                LayoutInflater.from(context);
        View raiz = inflater.inflate(
                R.layout.list_item,
                parent,
                false
        );
        return new WeatherViewHolder(raiz);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        Weather w = previsoes.get(position);
        holder.lowTextView.setText(
                context.getString(
                        R.string.low_temp,
                        w.minTemp
                )
        );
        holder.highTextView.setText(
                context.getString(
                        R.string.high_temp,
                        w.maxTemp
                )
        );
        holder.humidityTextView.setText(
                context.getString(
                        R.string.humidity,
                        w.humidity
                )
        );
        holder.dayTextView.setText(
                context.getString(
                        R.string.day_description,
                        w.dayOfWeek,
                        w.description
                )
        );
        Glide.with(context).load(w.iconURL).into(holder.conditionImageView);
    }

    @Override
    public int getItemCount() {
        return previsoes.size();
    }
}
