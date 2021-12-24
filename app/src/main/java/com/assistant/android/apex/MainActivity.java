package com.assistant.android.apex;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ms_square.debugoverlay.DebugOverlay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    public EditText textView;
    public Button speakBtn;
    public Button commandBtn;
    public ImageView micImageView;
    private ImageView shareImageView;
    private ImageView ironImageView;
    private View viewLine;
    private TextView introTextView;
    private TextView batteryTxt;
    private TextView tempTxt;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 100;
    public static final int CAMERA_APP_OPEN_CODE = 560;
    public static int VIEWS_ANIMATION_FLAG = 210;
    public TextToSpeech textToSpeech;

    public MediaPlayer mMediaPlayer;



    /**
     * Device battery status
     * @param savedInstanceState
     */
    private final BroadcastReceiver mBatInfoReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
            int battryPrcnt = level*100/scale;
            batteryTxt.setText(battryPrcnt +"%");
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO 2b: cpu.ram,memory usage
        DebugOverlay.with(getApplication()).install();

        Objects.requireNonNull(getSupportActionBar()).hide();

        textView = (EditText) findViewById(R.id.textView);
        speakBtn = (Button) findViewById(R.id.speakButton);
        commandBtn = (Button) findViewById(R.id.commandButton);
        micImageView = (ImageView) findViewById(R.id.mic);
        shareImageView = (ImageView)findViewById(R.id.share);
        ironImageView = (ImageView) findViewById(R.id.iron_man);
        viewLine = (View) findViewById(R.id.bottomLine);
        introTextView = (TextView) findViewById(R.id.introTextView);
        batteryTxt = (TextView) findViewById(R.id.batteryTextView);
        tempTxt = (TextView) findViewById(R.id.tempTextView);

        /**
         * Intialise battery Reciever
         */
        registerReceiver(mBatInfoReciever,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


        /**
         * get Record Audio permission from user
         */
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},VOICE_RECOGNITION_REQUEST_CODE);
        }

        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        /**
         * Intent to listen to the speech
         */
        final Intent mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        /**
         * Using the RecognizerListener to Listen
         */
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying first match
                textView.setText(matches.get(0));
                try {
                    takeVoiceCommandAction();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        /**
         * recognises voice while one holds the micImage_view
         */
        micImageView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_UP:
                    mSpeechRecognizer.stopListening();
                    textView.setHint("You will see input Voice here");
                    break;

                case MotionEvent.ACTION_DOWN:
                    mSpeechRecognizer.startListening(mRecognizerIntent);
                    textView.setText("");
                    textView.setHint("Listening.....");
                    break;
            }
            return false;
        });


        /**
         * Create an object textToSpeech and adding features into it.
         */
        textToSpeech = new TextToSpeech(getApplicationContext(), i -> {
            /**
             * If no error is found then only it will run
             */
            if(i!=TextToSpeech.ERROR){
                textToSpeech.setLanguage(Locale.US);
            }
        });


        /**
         * Speak Function
         */
        speakBtn.setOnClickListener(view -> {
            speak();
        });

        /**
         * Take Command
         */
        commandBtn.setOnClickListener(view -> {
            try {
                takeVoiceCommandAction();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });


        /**
         * To animate MicViewImage
         */
        if(VIEWS_ANIMATION_FLAG==210) {
            startAnimation();
        }

        introTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this,introduction.class);
            startActivity(intent);
        });


    }

    private void speak(){
        String text = String.valueOf(textView.getText());
        textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(VIEWS_ANIMATION_FLAG==210)
        mMediaPlayer.start();
    }

    /**
     * Clean up the media player by releasing its resources.
     */
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mMediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mMediaPlayer.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mMediaPlayer = null;
        }
    }

    /**
     * Method to take Actions Acc to Voice Command
     */
    private void takeVoiceCommandAction() throws UnsupportedEncodingException {
        String speak = String.valueOf(textView.getText());
        speak = speak.toLowerCase();
        if(speak.contains("your name")){
            String Unique_Identifer_id = "name";
            textToSpeech.speak("My Name is Apex, and i am your virtual assistant",TextToSpeech.QUEUE_FLUSH,null,Unique_Identifer_id);
        }
        else if(speak.contains("camera")){
            String Unique_Identifer_id = "camera";
            textToSpeech.speak("Opening Your Camera",TextToSpeech.QUEUE_FLUSH,null,Unique_Identifer_id);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(intent);
        }
        else if(speak.contains("open other")){
            String Unique_Identifer_id = "other";
            textToSpeech.speak("Opening other",TextToSpeech.QUEUE_FLUSH,null,Unique_Identifer_id);
//            Intent intent = new Intent(Conta);
//            startActivityForResult(intent,);
        }
        else if(speak.equals("hi")|| speak.equals("hello")){
            String Unique_Identifer_id = "hi";
            textToSpeech.speak(speak + " sir.How are you? ",TextToSpeech.QUEUE_FLUSH,null,Unique_Identifer_id);
            textView.setText(speak + " sir");
        }
        else if(speak.equals("fine") || speak.equals("good")){
            String Unique_Identifer_id = "good";
            textToSpeech.speak("It's good to know that.How can i help you?",TextToSpeech.QUEUE_FLUSH,null,Unique_Identifer_id);
        }
        else if(speak.contains("wikipedia")){
            String escapedQuery = URLEncoder.encode(speak, "UTF-8");
            Uri uri = Uri.parse("https://en.wikipedia.org/wiki/" + escapedQuery);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

        }
        else{
            String escapedQuery = URLEncoder.encode(speak, "UTF-8");
            Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private void animate(View view,int k){

        Transition transition = new Fade();
        transition.setDuration(6000);
        transition.addTarget(view.getId());

        TransitionManager.beginDelayedTransition((ViewGroup) view.getRootView(), transition);
        view.setVisibility(View.VISIBLE);

        switch (k){
            case 0:
                view.animate().translationY(600).setDuration(600).setStartDelay(6000);
                break;
            case 1:
                view.animate().translationY(600).setDuration(600).setStartDelay(2000);
                break;
            case 2:
                view.animate().translationY(600).setDuration(600).setStartDelay(2000);
                break;
            case 3:
                view.animate().scaleXBy(200).setDuration(2000).setStartDelay(6000);
                break;
            case 4:
                view.animate().translationY(-605).setDuration(600).setStartDelay(5000);

        }

    }

    private void startAnimation(){
        mMediaPlayer = MediaPlayer.create(this, R.raw.marvel_voice);
        mMediaPlayer.start();

        Handler handlerAnimate = new Handler();
        handlerAnimate.postDelayed(new Runnable() {
            @Override
            public void run() {
                animate(micImageView, 0);
                animate(shareImageView, 1);
                animate(ironImageView, 2);
                animate(viewLine, 3);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animate(introTextView, 4);
                        VIEWS_ANIMATION_FLAG = 250;
                    }
                }, 600);

            }
        }, 15000);

    }
}