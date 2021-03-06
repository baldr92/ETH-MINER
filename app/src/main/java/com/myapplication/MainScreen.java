package com.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainScreen extends AppCompatActivity {

    GraphView graph;
    MediaType JSON;
    Button btn,CheckBalanceBtn,ShareBtn,SetBtn,RateBtn;
    Timer timer;
    int onClick=0;
    String Balance,promo,ref,mod,Language="ru",x,y,PrevBalance,ExtRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Encoding(android_id);
        AsyncTaskCheck check = new AsyncTaskCheck();
        check.execute();
        AsyncTaskForRates newTask= new AsyncTaskForRates();
        newTask.execute();
    }

    public void ExtRates(View view)
    {
        Intent intent = new Intent(MainScreen.this,ExRate.class);
        intent.putExtra("json",ExtRate);
        startActivity(intent);
    }

    private class AsyncTaskForRates extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String myurl = "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD,EUR,GBP,CHF,CNY,JPY";
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(myurl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            ExtRate=result;
        }


        @Override
        protected void onPreExecute()
        {

        }

    }

    private class AsyncTaskCheck extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String myurl = "http://ethonline.site/users/login";
            String an = "ETH Miner";
            JSON = MediaType.parse("application/json; charset=utf-8");

            OkHttpClient client = new OkHttpClient();
            JSONObject postdata = new JSONObject();

            try {
                postdata.put("x", x);
                postdata.put("y", y);
                postdata.put("an", an);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(JSON, postdata.toString());
            Request request = new Request.Builder()
                    .url(myurl)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(!result.contains("{\"result\":false}"))
            {
                Balance=Params(result,true);
                PrevBalance=Balance;
            }
            else
            {
                Intent intent = new Intent(MainScreen.this,MainActivity.class);
                intent.putExtra("x",x);
                intent.putExtra("y",y);
                startActivityForResult(intent,2);
            }
            AsyncTaskForGraph task = new AsyncTaskForGraph();
            task.execute();
        }

        @Override
        protected void onPreExecute() {

        }
    }

    private void Encoding(String MyId)
    {
        x = Base64.encodeToString(MyId.getBytes(),Base64.DEFAULT);
        String temp, FirstResult="",Secondresult="", result="";
        String alphabit = "abcdefghijklmnopqrstvuwxyz";
        String digit="0123456789";
        int i=0,position;
        char first, second;

        while(i<MyId.length()-1)
        {
            first=MyId.charAt(i);
            second=MyId.charAt(i+1);
            temp=first+""+second;
            temp=MyRevers(temp);
            FirstResult+=temp;
            i+=2;
        }

        if(MyId.length()%2!=0)
        {
            first=MyId.charAt(i);
            FirstResult+=first+"";
        }

        for(i=0;i<FirstResult.length();i++)
        {
            first=FirstResult.charAt(i);
            if(alphabit.contains(first+""))
            {
                position=alphabit.length()-alphabit.indexOf(first)-1;
                second=alphabit.charAt(position);
                temp=second+"";
                Secondresult+=temp;
            }
            else
            {
                position=digit.length()-digit.indexOf(first)-1;
                second=digit.charAt(position);
                Secondresult+=second+"";
            }
        }

        for(i=0;i<Secondresult.length();i++)
        {
            first=Secondresult.charAt(i);
            temp=first+"";
            if(alphabit.contains(temp)&&i%2!=0)
            {
                result+=temp.toUpperCase();
            }
            else result+=temp;
        }
        result=MyRevers(result);
        y = Base64.encodeToString(result.getBytes(),Base64.DEFAULT);
    }

    private String MyRevers(String s)
    {
        return new StringBuilder(s).reverse().toString();
    }

    public void Setting(View view)
    {
        Intent intent=new Intent(MainScreen.this,SettingsActivity.class);
        intent.putExtra("Lang",Language);
        intent.putExtra("x",x);
        intent.putExtra("y",y);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        if(requestCode==1) {
            String Lang = data.getStringExtra("Lang");
            ChangeLocale(Lang);
        }
        if(requestCode==2)
        {
            Balance=data.getStringExtra("balance");
            PrevBalance=Balance;
            ref=data.getStringExtra("ref");
            promo=data.getStringExtra("promo");
            mod=data.getStringExtra("mod");
        }
    }

    void ChangeLocale(String Lang)
    {
        Locale myLocale = new Locale(Lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        updateView();
        Language=Lang;
    }

    void updateView()
    {
        if(onClick==0) btn.setText(getResources().getString(R.string.Maining));
        else btn.setText(getResources().getString(R.string.Stop));
        CheckBalanceBtn.setText((getResources().getString(R.string.Check_Balance)));
        ShareBtn.setText((getResources().getString(R.string.Share)));
        SetBtn.setText((getResources().getString(R.string.Settings)));
        RateBtn.setText((getResources().getString(R.string.Exchange_Rates)));
    }

    public void MiningActivity(View view)
    {
        if(onClick==0) {
            onClick++;
            timer = new Timer();
            timer.scheduleAtFixedRate(new MyTask(), 60*1000, 60 * 1000);
            btn.setText(R.string.Stop);
        }
        else
        {
            onClick=0;
            timer.cancel();
            btn.setText(R.string.Maining);
        }

    }

    public void Share(View view)
    {
        Intent intent = new Intent(MainScreen.this,ShareActivity.class);
        intent.putExtra("Promo", promo);
        intent.putExtra("Ref",ref);
        intent.putExtra("Mod",mod);
        startActivity(intent);
    }

    public void CheckBalance(View view)
    {
        Intent intent = new Intent(MainScreen.this,CheckBalanceActivity.class);
        intent.putExtra("balance", Balance);
        startActivity(intent);
    }

    private class AsyncTaskForGraph extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String myurl = "https://min-api.cryptocompare.com/data/histoday?fsym=ETH&tsym=USD&limit=30";
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(myurl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            StringChange(result);
        }


        @Override
        protected void onPreExecute()
        {
            setContentView(R.layout.activity_main_screen);
            graph = (GraphView) findViewById(R.id.MyGraph);
            btn =(Button) findViewById(R.id.PlayBtn);
            CheckBalanceBtn=(Button) findViewById(R.id.CheckBal);
            ShareBtn=(Button) findViewById(R.id.ShareBtn);
            SetBtn=(Button) findViewById(R.id.SetBtn);
            RateBtn=(Button) findViewById(R.id.ERateBtn);
            ChangeLocale(Language);
        }

        private Date StringFormat(String time) {
            Date dt = new Date(Long.parseLong(time + "000"));
            return dt;
        }

        private void StringChange(String json) {
            String mas[][] = new String[2][32];
            int i = 0;
            Date FTime;
            String time = "{\"time\":", Sep = ",\"", close = "\"close\":";
            int startTime = json.indexOf(time) + time.length();
            int endTime = json.indexOf(Sep, startTime);
            int startClose = json.indexOf(close) + close.length();
            int endClose = json.indexOf(Sep, startClose);

            mas[0][0] = json.substring(startTime, endTime);
            Date dt = new Date(Long.parseLong(mas[0][0] + "000"));
            mas[1][0] = json.substring(startClose, endClose);

            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                    new DataPoint(dt, Float.parseFloat(mas[1][0]))});
            while (i != 30) {
                startTime = json.indexOf(time, startTime) + time.length();
                endTime = json.indexOf(Sep, startTime);
                startClose = json.indexOf(close, endClose) + close.length();
                endClose = json.indexOf(Sep, startClose);
                i++;
                mas[0][i] = json.substring(startTime, endTime);
                FTime = StringFormat(mas[0][i]);
                mas[1][i] = json.substring(startClose, endClose);
                DataPoint dp = new DataPoint(FTime, Float.parseFloat(mas[1][i]));

                series.appendData(dp, true, 31);
            }
            series.setDrawDataPoints(true);
            graph.addSeries(series);
            Iterator<DataPoint> iter=series.getValues(series.getLowestValueX(),series.getHighestValueX());
            for(i=0;i<21;i++)
            {
                iter.next();
            }
            DataPoint ndp=iter.next();
            graph.getViewport().setScrollable(true);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(ndp.getX());
            graph.getViewport().setMaxX(series.getHighestValueX());

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(series.getLowestValueY());
            graph.getViewport().setMaxY(series.getHighestValueY());

            graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // show normal x values
                        SimpleDateFormat format1 = new SimpleDateFormat("dd.MM");
                        return format1.format(value);
                    } else {
                        // show currency for y values
                        return super.formatLabel(value, isValueX);
                    }
                }
            });
        }

    }

    private class AsyncTaskForMining extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String myurl = "http://ethonline.site/users/activity";
            String an = "ETH Miner";
            JSON = MediaType.parse("application/json; charset=utf-8");

            OkHttpClient client = new OkHttpClient();
            JSONObject postdata = new JSONObject();

            try {
                postdata.put("x", x);
                postdata.put("y", y);
                postdata.put("an", an);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(JSON, postdata.toString());
            Request request = new Request.Builder()
                    .url(myurl)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }


        @Override
        protected void onPostExecute(String result)
        {
            Balance=Params(result, false);
            Balance=Balance.replace(',','.');
            float oldBalance=Float.parseFloat(PrevBalance);
            float NewBalance=Float.parseFloat(Balance);
            float Def=NewBalance-oldBalance;
            PrevBalance=Balance;
            String DefStr=String.format("%.8f", Def);
            DefStr="+"+DefStr;
            Toast toast = Toast.makeText(getApplicationContext(),DefStr, Toast.LENGTH_SHORT);
            toast.show();
        }


        @Override
        protected void onPreExecute() {

        }
    }

    private String Params(String result, boolean flag)
    {
        String balance = ",\"balanse\":",End=",\"";
        String Promo="promo_code\":", Ref="\"refs_count\":",Mod="\"modifier\":";
        int startBalance = result.indexOf(balance) + balance.length();
        int endBalance = result.indexOf(End, startBalance);
        int startRef,endRef,startMod,endMod,startPromo,endPromo;
        startRef=result.indexOf(Ref)+Ref.length();
        endRef=result.indexOf(End,startRef);
        startMod=result.indexOf(Mod)+Mod.length();
        endMod=result.indexOf(End,startMod);
        startPromo=result.indexOf(Promo)+Promo.length();
        endPromo=result.indexOf(End,startPromo);
        promo=result.substring(startPromo,endPromo);
        ref=result.substring(startRef,endRef);
        mod=result.substring(startMod,endMod);
        if(!flag)
        {
            balance=result.substring(startBalance,endBalance);
            balance = String.format("%.8f", Float.parseFloat(balance));
        }
        else balance=result.substring(startBalance+1,endBalance-1);
        return balance;
    }

    private class MyTask extends TimerTask
    {
        public void run()
        {
            AsyncTaskForMining AT= new AsyncTaskForMining();
            AT.execute();
        }
    }
}
