/* In Class 06
InClass06.zip
Matthew Mango
Raymond Townsend
*/
package com.example.inclass06;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    SeekBar seekBar;
    final static String STATUS_START = "start";
    final static String myResult = "result";
    final static String threadProgress = "progress";
    final static String STATUS_STOP = "Done!";
    TextView textView, complexity_status, average;
    int complexity;
    ArrayList<Double> arrayList = new ArrayList<>();
    ArrayAdapter<Double> adapter;
    ListView listView;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView3);
        complexity_status = findViewById(R.id.complex_status);
        average = findViewById(R.id.average);
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, arrayList);
        listView.setAdapter(adapter);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(20);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Updates the selector text
                complexity = i;
                if(complexity == 1) {
                    textView.setText(i + " Time");
                } else {
                    textView.setText(i + " Times");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //Creates the thread Pool and the handler
        ExecutorService taskPool = Executors.newFixedThreadPool(2);
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                //if a new start message is sent, clear all the data
                if(message.getData().getString(STATUS_START)!= null){
                    /*average.setText("");
                    complexity_status.setText("");
                    progressBar.setProgress(0);
                    arrayList.clear();
                    adapter.notifyDataSetChanged();

                     */
                    average.setText("Average: 0");
                    complexity_status.setText("0/" + complexity);
                    progressBar.setVisibility(View.VISIBLE);

                //Only executes if complexity is nonZero and myResult has a value
                } else if((Double) message.getData().getDouble(myResult) != null && complexity > 0) {
                    complexity_status.setText(message.getData().getInt(threadProgress)+ "/" + String.valueOf(complexity));
                    progressBar.setVisibility(View.VISIBLE);

                    //gets the result and puts it in ArrayList
                    double temp = message.getData().getDouble(myResult);
                    arrayList.add(temp);

                    //Calculates average of ArrayList
                    double sum = 0;
                    double av;
                    for (int i = 0; i < arrayList.size(); i++) {
                        sum += arrayList.get(i);
                    }
                    av = sum / arrayList.size();

                    average.setText("Average: " + av);

                    //Gets the current iteration of the for loop, adds 1 and sets Text
                    int currCount = message.getData().getInt(threadProgress);
                    currCount++;

                    complexity_status.setText(currCount + "/" + complexity);
                    progressBar.setMax(complexity);
                    progressBar.setProgress(currCount);
                    adapter.notifyDataSetChanged();
                } else if((String) message.getData().getString(STATUS_STOP) != null){
                    findViewById(R.id.generate_button).setClickable(true);
                    findViewById(R.id.seekBar).setEnabled(true);
                }
                return false;
            }
        });

        //Generate Button
        findViewById(R.id.generate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                average.setText("");
                complexity_status.setText("");
                progressBar.setProgress(0);
                arrayList.clear();
                adapter.notifyDataSetChanged();
                findViewById(R.id.generate_button).setClickable(false);
                findViewById(R.id.seekBar).setEnabled(false);
                //Starts a thread from the pool
                taskPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        //only runs if complexity is non-zero, and bundles data in a message
                        if(complexity!=0){
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString(STATUS_START, "Start!");
                            message.setData(bundle);
                            handler.sendMessage(message);

                            //Loops as many times as selector indicates.
                            for (int i = 0; i < complexity; i++) {
                                Double result = HeavyWork.getNumber();
                                Bundle bundle2 = new Bundle();
                                bundle2.putDouble(myResult, result);
                                bundle2.putInt(threadProgress, (Integer) i);
                                Message message2 = new Message();
                                message2.setData(bundle2);
                                handler.sendMessage(message2);
                            }
                            //Thread complete message
                            Message message3 = new Message();
                            Bundle bundle3 = new Bundle();
                            bundle3.putString(STATUS_STOP, "Done!");
                            message3.setData(bundle3);
                            handler.sendMessage(message3);
                        }
                    }
                });
            }
        });
    }
}