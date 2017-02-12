package com.example.connorseiden.grammardev1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.inet.jortho.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    public final static int MY_PERMISSIONS_REQUEST_READ_SMS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user clicks the Send button */
    public void analyzeTexts(View view) {
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_SMS"}, MY_PERMISSIONS_REQUEST_READ_SMS);
        }
        else{
            runAnalysis();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runAnalysis();
                } else {
                    // permission denied, boo!
                    this.finish();
                    System.exit(0);
                }
                return;
            }
        }
    }

    private void runAnalysis(){
        ArrayList<String> texts = new ArrayList<String>();

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                for(int idx=0;idx<cursor.getColumnCount();idx++)
                {
                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                }
                // use msgData
                texts.add(textParse(msgData));
            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
        }

        Map<String, WordCount> wordMap = new HashMap<String, WordCount>();

        for (String text: texts) {
            String[] words = text.split("[^A-ZÃ…Ã„Ã–a-zÃ¥Ã¤Ã¶]+");
            for(String word: words){
                if(!word.equals("")){
                    WordCount wordC = wordMap.get(word);
                    if (wordC == null) {
                        wordC = new WordCount();
                        wordC.word = word;
                        wordC.count = 0;
                        wordMap.put(word, wordC);
                    }

                    wordC.count++;
                }
            }
        }

        SortedSet<WordCount> sortedWords = new TreeSet<WordCount>(countMap.values());

        String output = "count" + "\t" + "word\n\n";

        int ind = 1;
        for (WordCount word : sortedWords) {

            output += (word.count + "\t" + word.word + "\n");

            if (ind >= 50) {
                break;
            }
            ind++;
        }


//        String example = "";
//
//        for(int i=0;i<100;i++){
//            example += (i + ": " + texts.get(i) + "\n");
//        }
//
        TextView t = (TextView) findViewById(R.id.resultView);
        t.setMovementMethod(new ScrollingMovementMethod());
        t.setText(output);
    }

    private String textParse(String s){
        if(!s.contains("body:") || !s.contains("service_center")){
            return "";
        }
        String t = s.substring((s.indexOf("body:")+5), s.indexOf("service_center:"));

        return t;
    }


    public static class WordCount implements Comparable<WordCount>
    {
        int count;
        String word;

        @Override
        public int hashCode()
        {
            return word.hashCode();
        }

        @Override
        public int compareTo(WordCount b)
        {
            return b.count - count;
        }

        @Override
        public boolean equals(Object obj)
        {
            return word.equals(((WordCount)obj).word);
        }
    }
}
