package com.moutaigua.ultimatetictactoe;

import java.util.Random;
import java.util.Stack;

/**
 * Created by mou on 3/16/17.
 */

public class GameRoom {

    private String roomId;
    private int whoAmI;
    private String myUuidKey;
    private String myStatusKey;
    private String myLastActiveTimeStampKey;
    private String myStatus;
    private long myLastActiveTimeStamp;
    private User opponent;
    private String opponentUuidKey;
    private String opponentStatusKey;
    private String opponentLastActiveTimestampKey;
    private String opponentStatus;
    private long opponentLastActiveTimeStamp;
    private int whoFirst;
    private Stack<Integer> moves;


    public GameRoom() {
        whoFirst = FragmentBoard.EMPTY;
        this.moves = new Stack<>();
    }

    public int decideWhoFirst() {
        Random rand = new Random();
        return rand.nextInt(2)==0 ? FragmentBoard.PLAYER_1 : FragmentBoard.PLAYER_2;
    }

    public void setGameOver(){
        myStatus = FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_WAIT;
        opponentStatus = FirebaseDatabaseHelper.GAMEROOM_VALUE_STATUS_WAIT;
        setWhoFirst(FragmentBoard.EMPTY);
        moves.clear();
    }

    public void reset() {
        this.roomId = null;
        setWhoAmI(FragmentBoard.EMPTY);
        myStatus = null;
        myLastActiveTimeStamp = 0;
        opponentStatus = null;
        opponentLastActiveTimeStamp = 0;
        opponent = null;
        whoFirst = FragmentBoard.EMPTY;
        moves.clear();
    }


    /**** Getters and Setters ****/

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getWhoAmI() {
        return whoAmI;
    }

    public void setWhoAmI(int whoAmI) {
        this.whoAmI = whoAmI;
        if( whoAmI== FragmentBoard.PLAYER_1 ){
            this.myUuidKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER1_UUID;
            this.myStatusKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER1_STATUS;
            this.myLastActiveTimeStampKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER1_LAST_TIME_ACTIVATE;
            this.opponentUuidKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER2_UUID;
            this.opponentStatusKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER2_STATUS;
            this.opponentLastActiveTimestampKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER2_LAST_TIME_ACTIVATE;
        } else if (whoAmI== FragmentBoard.PLAYER_2) {
            this.myUuidKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER2_UUID;
            this.myStatusKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER2_STATUS;
            this.myLastActiveTimeStampKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER2_LAST_TIME_ACTIVATE;
            this.opponentUuidKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER1_UUID;
            this.opponentStatusKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER1_STATUS;
            this.opponentLastActiveTimestampKey = FirebaseDatabaseHelper.GAMEROOM_KEY_PLAYER1_LAST_TIME_ACTIVATE;
        } else {
            this.myUuidKey = null;
            this.myStatusKey = null;
            this.myLastActiveTimeStampKey = null;
            this.opponentUuidKey = null;
            this.opponentStatusKey = null;
            this.opponentLastActiveTimestampKey = null;
        }
    }

    public String getMyUuidKey() {
        return myUuidKey;
    }

    public String getMyStatusKey() {
        return myStatusKey;
    }

    public String getMyLastActiveTimeStampKey() {
        return myLastActiveTimeStampKey;
    }

    public String getOpponentUuidKey() {
        return opponentUuidKey;
    }

    public String getOpponentStatusKey() {
        return opponentStatusKey;
    }

    public String getOpponentLastActiveTimestampKey() {
        return opponentLastActiveTimestampKey;
    }

    public String getMyStatus() {
        return myStatus;
    }

    public void setMyStatus(String myStatus) {
        this.myStatus = myStatus;
    }

    public long getMyLastActiveTimeStamp() {
        return myLastActiveTimeStamp;
    }

    public void setMyLastActiveTimeStamp(long myLastActiveTimeStamp) {
        this.myLastActiveTimeStamp = myLastActiveTimeStamp;
    }

    public String getOpponentStatus() {
        return opponentStatus;
    }

    public void setOpponentStatus(String opponentStatus) {
        this.opponentStatus = opponentStatus;
    }

    public long getOpponentLastActiveTimeStamp() {
        return opponentLastActiveTimeStamp;
    }

    public void setOpponentLastActiveTimeStamp(long opponentLastActiveTimeStamp) {
        this.opponentLastActiveTimeStamp = opponentLastActiveTimeStamp;
    }

    public User getOpponent() {
        return opponent;
    }

    public void setOpponent(User opponent) {
        this.opponent = opponent;
    }

    public int getWhoFirst() {
        return whoFirst;
    }

    public void setWhoFirst(int player) {
        this.whoFirst = player;
    }

    public Stack<Integer> getMoves() {
        return moves;
    }

    public void clearMoves(Stack<Integer> moves) {
        this.moves.clear();
    }
}
