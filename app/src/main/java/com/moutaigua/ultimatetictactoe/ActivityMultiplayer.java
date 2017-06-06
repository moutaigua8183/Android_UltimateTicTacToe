package com.moutaigua.ultimatetictactoe;

import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by mou on 3/12/17.
 */

public class ActivityMultiplayer extends FragmentActivity {


    private FragmentBoard board;
    private FragmentTransaction fragmentTransaction;
    private TextView lower_display;
    private Button lower_button;
    private TextView lower_player1_score;
    private TextView lower_player2_score;
    private TextView upper_display;
    private Button upper_button;
    private TextView upper_player1_score;
    private TextView upper_player2_score;

    private int player1_score;
    private int player2_score;
    private boolean player1Ready;
    private boolean player2Ready;

    private FragmentBoard.ActivityConnector boardConnector;
    private CountDownTimer countDownTimer;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        initControlPanel();
        initBoardFragment();
        gameReset();

    }

    @Override
    public void onBackPressed() {
        SoundHelper.getInstance(getApplicationContext()).playButtonClick();
        DialogHelper.openDialogForMultiplerExit(this);
    }


    private void initControlPanel(){
        lower_button = (Button) findViewById(R.id.activity_multiplayer_btn_lower_restart);
        lower_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                if( board.getGameState()== FragmentBoard.GAME_STATE_PREPARE ){
                    if( !player1Ready ){
                        player1Ready = true;
                        lower_button.setText(getString(R.string.game_button_after_ready));
                        lower_button.setEnabled(false);
                        lower_display.setText("");
                    }
                    if( player1Ready&&player2Ready ){
                        SoundHelper.getInstance(getApplicationContext()).playGameStart();
                        gameStart();
                    }
                } else if( board.getGameState()== FragmentBoard.GAME_STATE_PROCESSING ){
                    if( lower_button.getText().toString()
                            .equalsIgnoreCase(getString(R.string.game_button_surrender)) ) {
                        surrenderConfirmCountDown(FragmentBoard.PLAYER_1);
                    } else {
                        countDownTimer.cancel();
                        board.setGameState(FragmentBoard.GAME_STATE_OVER);
                        board.setGameResult(FragmentBoard.PLAYER_2);
                        onPlayerWin(FragmentBoard.PLAYER_2);
                    }
                } else if(board.getGameState()== FragmentBoard.GAME_STATE_OVER){
                    board.reset();
                    gameReset();
                }
            }
        });
        upper_button = (Button) findViewById(R.id.activity_multiplayer_btn_upper_restart);
        upper_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                if( board.getGameState()== FragmentBoard.GAME_STATE_PREPARE ){
                    if( !player2Ready ){
                        player2Ready = true;
                        upper_button.setText(getString(R.string.game_button_after_ready));
                        upper_button.setEnabled(false);
                        upper_display.setText("");
                    }
                    if( player1Ready&&player2Ready ){
                        SoundHelper.getInstance(getApplicationContext()).playGameStart();
                        gameStart();
                    }
                } else if( board.getGameState()== FragmentBoard.GAME_STATE_PROCESSING ){
                    if( upper_button.getText().toString()
                            .equalsIgnoreCase(getString(R.string.game_button_surrender)) ) {
                        surrenderConfirmCountDown(FragmentBoard.PLAYER_2);
                    } else {
                        countDownTimer.cancel();
                        board.setGameState(FragmentBoard.GAME_STATE_OVER);
                        board.setGameResult(FragmentBoard.PLAYER_1);
                        onPlayerWin(FragmentBoard.PLAYER_1);
                    }
                } else if(board.getGameState()== FragmentBoard.GAME_STATE_OVER){
                    board.reset();
                    gameReset();
                }
            }
        });
        Typeface newFont = Typeface.createFromAsset(getAssets(),"fonts/Sansation_Light.ttf");
        lower_display = (TextView) findViewById(R.id.activity_multiplayer_txtview_lower_display);
        lower_display.setTypeface(newFont);
        upper_display = (TextView) findViewById(R.id.activity_multiplayer_txtview_player2_display);
        upper_display.setTypeface(newFont);

        player1_score = 0;
        player2_score = 0;
        lower_player1_score = (TextView) findViewById(R.id.activity_multiplayer_txtview_lower_player1_score);
        lower_player1_score.setText(String.valueOf(player1_score));
        lower_player2_score = (TextView) findViewById(R.id.activity_multiplayer_txtview_lower_player2_score);
        lower_player2_score.setText(String.valueOf(player2_score));
        upper_player1_score = (TextView) findViewById(R.id.activity_multiplayer_txtview_upper_player1_score);
        upper_player1_score.setText(String.valueOf(player1_score));
        upper_player2_score = (TextView) findViewById(R.id.activity_multiplayer_txtview_upper_player2_score);
        upper_player2_score.setText(String.valueOf(player2_score));
    }

    private void initBoardFragment() {
        boardConnector = new FragmentBoard.ActivityConnector() {
            @Override
            public void onBoardClick(int squareCode) {
                // set game state PROCESSING to start the game
                if( board.getGameState()== FragmentBoard.GAME_STATE_PREPARE ) {
                    if( !player1Ready ){
                        SoundHelper.getInstance(getApplicationContext()).playGameHint();
                        lower_display.setText(getString(R.string.game_message_me_ready_first));
                    }
                    if( !player2Ready ){
                        SoundHelper.getInstance(getApplicationContext()).playGameHint();
                        upper_display.setText(getString(R.string.game_message_me_ready_first));
                    }
                    if( player1Ready && player2Ready ) {
                        board.setGameState(FragmentBoard.GAME_STATE_PROCESSING);
                    }
                }
                if( board.getGameState()== FragmentBoard.GAME_STATE_PROCESSING ) {
                    // add one move
                    board.addMove(squareCode);
                }
            }

            @Override
            public void onMoveAdded(int squareCode) {
                SoundHelper.getInstance(getApplicationContext()).playMoveValid();
                if( board.getGameState()== FragmentBoard.GAME_STATE_PROCESSING ){
                    // game continues
                    if( board.getCurrentPlayer()== FragmentBoard.PLAYER_1 ){
                        lower_display.setText(getString(R.string.game_message_your_turn));
                        upper_display.setText("");
                    } else {
                        lower_display.setText("");
                        upper_display.setText(getString(R.string.game_message_your_turn));
                    }
                } else {
                    SoundHelper.getInstance(getApplicationContext()).playGameOver();
                    onPlayerWin(board.getGameResult());
                    upper_button.setText(getString(R.string.game_button_start_new_game));
                    lower_button.setText(getString(R.string.game_button_start_new_game));
                }
            }
        };
        board = FragmentBoard.newInstance(boardConnector);
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.activity_multiplayer_board_fragment_holder, board);
        fragmentTransaction.commit();
    }

    private void gameStart(){
        board.decideWhoFirst();
        board.setGameState(FragmentBoard.GAME_STATE_PROCESSING);
        lower_button.setEnabled(true);
        lower_button.setText(getString(R.string.game_button_surrender));
        lower_display.setText("");
        upper_button.setEnabled(true);
        upper_button.setText(getString(R.string.game_button_surrender));
        upper_display.setText("");
        if( board.getCurrentPlayer()== FragmentBoard.PLAYER_1 ){
            lower_display.setText(getString(R.string.game_message_your_turn));
        } else {
            upper_display.setText(getString(R.string.game_message_your_turn));
        }
    }

    private void gameReset(){
        player1Ready = false;
        player2Ready = false;
        upper_button.setText(getString(R.string.game_button_to_ready));
        lower_button.setText(getString(R.string.game_button_to_ready));
        upper_display.setText("");
        lower_display.setText("");
    }

    private void surrenderConfirmCountDown(final int player){
        final TextView disp;
        final Button btn;
        if( player== FragmentBoard.PLAYER_1 ){
            disp = lower_display;
            btn = lower_button;
        } else {
            disp = upper_display;
            btn = upper_button;
        }
        disp.setText(getString(R.string.game_message_surrender));
        countDownTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                btn.setText(getString(R.string.game_button_confirm)+" ("+  millisUntilFinished/1000 + ")");
            }

            public void onFinish() {
                btn.setText(getString(R.string.game_button_surrender));
                if( board.getCurrentPlayer()==player ){
                    disp.setText(getString(R.string.game_message_your_turn));
                } else {
                    disp.setText("");
                }
            }
        };
        countDownTimer.start();
    }

    public boolean isPlayer1Ready() {
        return player1Ready;
    }

    public boolean isPlayer2Ready() {
        return player2Ready;
    }

    private void onPlayerWin(int player){
        switch (player){
            case FragmentBoard.PLAYER_1:
                upper_display.setText(getString(R.string.game_message_lose));
                lower_display.setText(getString(R.string.game_message_win));
                player1_score++;
                upper_player1_score.setText(String.valueOf(player1_score));
                lower_player1_score.setText(String.valueOf(player1_score));
                break;
            case FragmentBoard.PLAYER_2:
                upper_display.setText(getString(R.string.game_message_win));
                lower_display.setText(getString(R.string.game_message_lose));
                player2_score++;
                upper_player2_score.setText(String.valueOf(player2_score));
                lower_player2_score.setText(String.valueOf(player2_score));
                break;
            case FragmentBoard.EMPTY:
                upper_display.setText(getString(R.string.game_message_tie));
                lower_display.setText(getString(R.string.game_message_tie));
        }
    }






}
