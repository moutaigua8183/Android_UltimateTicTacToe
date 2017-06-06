package com.moutaigua.ultimatetictactoe;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.models.Country;

import java.io.InputStream;
import java.util.Locale;

/**
 * Created by mou on 3/15/17.
 */

public class ActivityOnline extends Activity {

    private final String LOG_TAG = "ActivityOnline";
    private final long AWAY_THRESHOLD = 40*1000;
    private final long INACTIVE_THRESHOLD = 59*1000;

    private FirebaseDatabaseHelper.GameRoomCallback callback;
    private GameRoom myGame;
    private FragmentBoard board;
    private FragmentTransaction fragmentTransaction;
    private TextView myDisplay;
    private Button myButton;
    private TextView opponentDisplay;
    private TextView opponentUsername;
    private ImageView opponentFlag;
    private ImageView opponentIndicator;
    private View opponentBlock;

    private FragmentBoard.ActivityConnector boardConnector;
    private CountDownTimer surrenderConfirmCountDownTimer;
    private CountDownTimer moveHintCountDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        callback = new FirebaseDatabaseHelper.GameRoomCallback() {
            @Override
            public void onMatchComplete() {
                DialogHelper.endMatching(true);
                Log.d(LOG_TAG, "Match Succeeds");
                myGame.setMyLastActiveTimeStamp(System.currentTimeMillis());
                FirebaseDatabaseHelper.getInstance().updateMyActiveTime();
                opponentUsername.setText(myGame.getOpponent().getUsername());
                Bitmap flag = getFlagByCountryName(myGame.getOpponent().getCountry());
                opponentFlag.setImageBitmap(flag);
                if( myGame.getWhoAmI()== FragmentBoard.PLAYER_1 ){
                    opponentIndicator.setImageResource(R.drawable.player2_o);
                } else {
                    opponentIndicator.setImageResource(R.drawable.player1_x);
                }
            }

            @Override
            public void onOpponentReady() {
                if( myGame.getOpponentStatus()==null || !myGame.getOpponentStatus().equalsIgnoreCase(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_AWAY) ) {
                    // opponent ready
                    myGame.setOpponentStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY);
                    opponentDisplay.setText(R.string.game_message_ready);
                    opponentDisplay.setTextColor(ContextCompat.getColor(ActivityOnline.this, R.color.online_opponent_ready));
                    if( myGame.getMyStatus()==FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY ){
                        if( myGame.getWhoAmI()==FragmentBoard.PLAYER_1 ) {
                            int firstPlayer = myGame.decideWhoFirst();
                            FirebaseDatabaseHelper.getInstance().setWhoFirst(firstPlayer);
                        }
                    }
                } else {
                    // opponent return active
                    myGame.setOpponentStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY);
                    opponentDisplay.setText("");
                    if( board.getCurrentPlayer()==myGame.getWhoAmI() ) {
                        opponentIndicator.setVisibility(View.GONE);
                    } else {
                        opponentIndicator.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onWhoFirstDecided() {
                if( myGame.getMyStatus()==FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY
                        && myGame.getOpponentStatus()==FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY ){
                    SoundHelper.getInstance(getApplicationContext()).playGameStart();
                    gameStart();
                }
            }

            @Override
            public void onMove(int moveCode) {
                int owner = moveCode / 100;
                int squareCode = moveCode % 100;
                if( owner!=myGame.getWhoAmI() ){
                    SoundHelper.getInstance(getApplicationContext()).playMoveValid();
                    board.addMove(squareCode);
                    opponentIndicator.setVisibility(View.GONE);
                    opponentBlock.setBackgroundResource(R.drawable.online_opponent_info_panel_out_turn);
                    opponentDisplay.setText("");
                }
            }

            @Override
            public void onMyselfAway() {
                myGame.setMyStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_AWAY);
                moveHintCountDown();
            }

            @Override
            public void onOpponentAway() {
                myGame.setOpponentStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_AWAY);
                opponentDisplay.setText(getString(R.string.game_message_away));
                opponentDisplay.setTextColor(ContextCompat.getColor(ActivityOnline.this, R.color.online_opponent_away));
                opponentIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onOpponentInactive() {
                myGame.setOpponentStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_INACTIVE);
                gameReset();
                DialogHelper.openDialogForInactiveOpponent(ActivityOnline.this, callback);
            }

            @Override
            public void onOpponentQuit() {
                board.reset();
                DialogHelper.openDialogAfterOpponentExit(ActivityOnline.this, callback);
            }

            @Override
            public void onOpponentSurrender() {
                board.setGameState(FragmentBoard.GAME_STATE_OVER);
                board.setGameResult(myGame.getWhoAmI());
                opponentIndicator.setVisibility(View.GONE);
                opponentBlock.setBackgroundResource(R.drawable.online_opponent_info_panel_out_turn);
                myButton.setText(getString(R.string.game_button_start_new_game));
                myDisplay.setText(getString(R.string.game_message_opponent_surrender));
                myGame.setMyStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_WAIT);
                myGame.setMyLastActiveTimeStamp( System.currentTimeMillis() );
                GlobalData.getInstance().updateWinningRate(true);
                FirebaseDatabaseHelper.getInstance().updateWinningRate();
                FirebaseDatabaseHelper.getInstance().setMyGameStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_WAIT);
            }

        };
        DialogHelper.startMatching(this, callback);


        initActivity();
        initBoardFragment();

    }


    @Override
    public void onBackPressed() {
        SoundHelper.getInstance(getApplicationContext()).playButtonClick();
        if( board.getGameState()== FragmentBoard.GAME_STATE_PROCESSING ) {
            DialogHelper.openExitAlertInOnlineRoom(this);
        } else {
            DialogHelper.openExitConfirmInOnlineRoom(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabaseHelper.getInstance().stopGameRoomListeners();
        GlobalData.getInstance().getMyRoom().reset();
    }



    private void initActivity(){
        myGame = GlobalData.getInstance().getMyRoom();
        myButton = (Button) findViewById(R.id.activity_online_btn_restart);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                if( board.getGameState()== FragmentBoard.GAME_STATE_PREPARE ){
                    if( myGame.getMyStatus()!= FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY ){
                        myGame.setMyStatus( FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY );
                        myGame.setMyLastActiveTimeStamp( System.currentTimeMillis() );
                        myButton.setText(getString(R.string.game_button_after_ready));
                        myButton.setEnabled(false);
                        myDisplay.setText("");
                        FirebaseDatabaseHelper.getInstance().setMyGameStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY);
                        FirebaseDatabaseHelper.getInstance().updateMyActiveTime();
                    }
                    // when both ready
                    if( myGame.getOpponentStatus()==FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY ){
                        if( myGame.getWhoAmI()==FragmentBoard.PLAYER_1 ) {
                            int firstPlayer = myGame.decideWhoFirst();
                            FirebaseDatabaseHelper.getInstance().setWhoFirst(firstPlayer);
                        }
                    }
                } else if( board.getGameState()== FragmentBoard.GAME_STATE_PROCESSING ){
                    if( myButton.getText().toString()
                            .equalsIgnoreCase(getString(R.string.game_button_surrender)) ) {
                        surrenderConfirmCountDown();
                    } else {
                        // surrender
                        surrenderConfirmCountDownTimer.cancel();
                        board.setGameState(FragmentBoard.GAME_STATE_OVER);
                        int winner = myGame.getWhoAmI()== FragmentBoard.PLAYER_1 ? FragmentBoard.PLAYER_2 : FragmentBoard.PLAYER_1;
                        board.setGameResult(winner);
                        opponentBlock.setBackgroundResource(R.drawable.online_opponent_info_panel_out_turn);
                        myButton.setText(getString(R.string.game_button_start_new_game));
                        myDisplay.setText(getString(R.string.game_message_lose));
                        myGame.setMyLastActiveTimeStamp( System.currentTimeMillis() );
                        myGame.setMyStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_WAIT);
                        GlobalData.getInstance().updateWinningRate(false);
                        FirebaseDatabaseHelper.getInstance().updateWinningRate();
                        FirebaseDatabaseHelper.getInstance().setSurrenderor( myGame.getWhoAmI() );
                        FirebaseDatabaseHelper.getInstance().updateMyActiveTime();
                        FirebaseDatabaseHelper.getInstance().setMyGameStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_WAIT);
                    }
                } else if(board.getGameState()== FragmentBoard.GAME_STATE_OVER){
                    gameReset();
                }
            }
        });
        Typeface newFont = Typeface.createFromAsset(getAssets(),"fonts/Sansation_Light.ttf");
        myDisplay = (TextView) findViewById(R.id.activity_online_txtview_my_display);
        myDisplay.setTypeface(newFont);
        opponentDisplay = (TextView) findViewById(R.id.activity_online_txtview_opponent_display);
        opponentDisplay.setTypeface(newFont);
        opponentUsername = (TextView) findViewById(R.id.activity_online_txtview_opponent_name);
        opponentUsername.setTypeface(newFont);
        opponentFlag = (ImageView) findViewById(R.id.activity_online_imgview_opponent_flag);
        opponentIndicator = (ImageView) findViewById(R.id.activity_online_imgview_opponent_indicator);
        opponentBlock = findViewById(R.id.activity_online_layout_opponent_block);
    }

    private void initBoardFragment() {
        boardConnector = new FragmentBoard.ActivityConnector() {
            @Override
            public void onBoardClick(int squareCode) {
                // set game state PROCESSING to start the game
                if( board.getGameState()== FragmentBoard.GAME_STATE_PREPARE ) {
                    if (myGame.getMyStatus() != FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY) {
                        SoundHelper.getInstance(getApplicationContext()).playGameHint();
                        myDisplay.setText(getString(R.string.game_message_me_ready_first));
                        return;
                    } else if (myGame.getOpponentStatus() != FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY) {
                        SoundHelper.getInstance(getApplicationContext()).playGameHint();
                        myDisplay.setText(getString(R.string.game_message_opponent_not_ready));
                        return;
                    } else {
                        board.setGameState(FragmentBoard.GAME_STATE_PROCESSING);
                    }
                }
                if( board.getGameState()== FragmentBoard.GAME_STATE_PROCESSING ) {
                    if( board.getCurrentPlayer()==myGame.getWhoAmI() ) {
                        // add one move
                        board.addMove(squareCode);
                    }
                }
            }

            @Override
            public void onMoveAdded(int squareCode) {
                SoundHelper.getInstance(getApplicationContext()).playMoveValid();
                int previousPlayer;
                if( board.getCurrentPlayer()== FragmentBoard.PLAYER_1 ){
                    previousPlayer = FragmentBoard.PLAYER_2;
                } else {
                    previousPlayer = FragmentBoard.PLAYER_1;
                }
                int moveCode = previousPlayer*100 + squareCode;
                myGame.getMoves().add(moveCode);
                FirebaseDatabaseHelper.getInstance().addNewMove(moveCode);
                myGame.setMyLastActiveTimeStamp(System.currentTimeMillis());
                FirebaseDatabaseHelper.getInstance().updateMyActiveTime();
                if( myGame.getMyStatus().equalsIgnoreCase(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_AWAY)){
                    myDisplay.setText("");
                    myGame.setMyStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_READY);
                    moveHintCountDownTimer.cancel();
                }
                if( board.getGameState()== FragmentBoard.GAME_STATE_PROCESSING ){
                    // game continues
                    if( board.getCurrentPlayer()==myGame.getWhoAmI() ){
                        myDisplay.setText(getString(R.string.game_message_your_turn));
                        opponentIndicator.setVisibility(View.GONE);
                        opponentBlock.setBackgroundResource(R.drawable.online_opponent_info_panel_out_turn);
                    } else {
                        myDisplay.setText("");
                        opponentIndicator.setVisibility(View.VISIBLE);
                        opponentBlock.setBackgroundResource(R.drawable.online_opponent_info_panel_in_turn);
                    }
                } else {
                    SoundHelper.getInstance(getApplicationContext()).playGameOver();
                    opponentBlock.setBackgroundResource(R.drawable.online_opponent_info_panel_out_turn);
                    opponentIndicator.setVisibility(View.GONE);
                    myButton.setText(getString(R.string.game_button_start_new_game));
                    if( board.getGameResult()== FragmentBoard.TIE ){
                        myDisplay.setText(getString(R.string.game_message_tie));
                        GlobalData.getInstance().updateWinningRate(false);
                    } else if( board.getGameResult()==myGame.getWhoAmI() ){
                        myDisplay.setText(getString(R.string.game_message_win));
                        GlobalData.getInstance().updateWinningRate(true);
                    } else {
                        myDisplay.setText(getString(R.string.game_message_lose));
                        GlobalData.getInstance().updateWinningRate(false);
                    }
                    myGame.setGameOver();
                    FirebaseDatabaseHelper.getInstance().updateWinningRate();
                    FirebaseDatabaseHelper.getInstance().setMyGameStatus(FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_WAIT);
                    FirebaseDatabaseHelper.getInstance().setWhoFirst(FragmentBoard.EMPTY);
                }
            }
        };
        board = FragmentBoard.newInstance(boardConnector);
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.activity_online_board_fragment_holder, board);
        fragmentTransaction.commit();
    }

    private void gameStart(){
        board.setWhoFirst(myGame.getWhoFirst());
        board.setGameState(FragmentBoard.GAME_STATE_PROCESSING);
        myButton.setEnabled(true);
        myButton.setText(getString(R.string.game_button_surrender));
        myDisplay.setText("");
        opponentDisplay.setText("");
        if( board.getCurrentPlayer()==myGame.getWhoAmI() ){
            myDisplay.setText(getString(R.string.game_message_your_turn));
        } else {
            opponentBlock.setBackgroundResource(R.drawable.online_opponent_info_panel_in_turn);
            opponentIndicator.setVisibility(View.VISIBLE);
        }
        FirebaseDatabaseHelper.getInstance().updateMyActiveTime();
    }

    private void gameReset(){
        board.reset();
        myButton.setText(getString(R.string.game_button_to_ready));
        myDisplay.setText("");
        opponentDisplay.setText("");
        opponentBlock.setBackgroundResource(R.drawable.online_opponent_info_panel_out_turn);
        opponentIndicator.setVisibility(View.GONE);
        FirebaseDatabaseHelper.getInstance().setWhoFirst(FragmentBoard.EMPTY);
        FirebaseDatabaseHelper.getInstance().setSurrenderor(FragmentBoard.EMPTY);
        FirebaseDatabaseHelper.getInstance().clearMoves(myGame.getRoomId());
    }

    private void surrenderConfirmCountDown(){
        myDisplay.setText(getString(R.string.game_message_surrender));
        if( surrenderConfirmCountDownTimer==null ) {
            surrenderConfirmCountDownTimer = new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    myButton.setText(getString(R.string.game_button_confirm) + " (" + millisUntilFinished / 1000 + ")");
                }

                public void onFinish() {
                    myButton.setText(getString(R.string.game_button_surrender));
                    if (board.getCurrentPlayer() == myGame.getWhoAmI()) {
                        myDisplay.setText(getString(R.string.game_message_your_turn));
                    } else {
                        myDisplay.setText("");
                    }
                }
            };
        }
        surrenderConfirmCountDownTimer.start();
    }

    private void moveHintCountDown(){
        if( moveHintCountDownTimer==null ) {
            moveHintCountDownTimer = new CountDownTimer(INACTIVE_THRESHOLD-AWAY_THRESHOLD, 1000) {
                public void onTick(long millisUntilFinished) {
                    myDisplay.setText(getString(R.string.game_message_make_move_hint) + "(" + millisUntilFinished / 1000 + ")");
                }

                public void onFinish() {
                    gameReset();
                    DialogHelper.openDialogForMyInactiveness(ActivityOnline.this, callback);
                }
            };
        }
        moveHintCountDownTimer.start();
    }

    private Bitmap getFlagByCountryName(String countryName){
        CountryPicker picker = CountryPicker.newInstance("Select Country");
        Country country = picker.getCountryByName(this, countryName);
        String flagFileName = "flag_" + country.getCode().toLowerCase(Locale.ENGLISH);
        int resId = getResources().getIdentifier(flagFileName, "raw", getPackageName());
        InputStream inputStream = getResources().openRawResource(resId);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }





}
