package com.moutaigua.ultimatetictactoe;

import android.support.annotation.FractionRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by mou on 3/7/17.
 */

public class FirebaseDatabaseHelper {

    public static final String VERSION_TABLE = "version";
    public static final String VERSION_KEY_LATEST_VERSION = "latest_version";

    public static final String USER_TABLE = "users";
    public static final String USER_KEY_USERNAME = "username";
    public static final String USER_KEY_COUNTRY = "country";
    public static final String USER_KEY_TOTAL_PLAYED = "total_games";
    public static final String USER_KEY_WINNING_TIMES = "winning_games";
    public static final String USER_KEY_WINNING_RATE = "winning_rate";
    public static final String USER_KEY_INVITABLE = "invitable";

    public static final String ONLINE_TABLE = "online_users";
    public static final String ONLINE_KEY_STATUS = "status";
    public static final String ONLINE_KEY_INVITABLE = "invitable";
    public static final String ONLINE_KEY_LAST_ACTIVE_TIMESTAMP = "timestamp_last_active";
    public static final String ONLINE_VALUE_STATUS_IN_GAME = "playing";
    public static final String ONLINE_VALUE_STATUS_INACTIVE = "inactive";
    public static final String ONLINE_VALUE_STATUS_AVAILABLE = "available";

    public static final String GAMEROOM_TABLE = "game_rooms";
    public static final String GAMEROOM_KEY_PLAYER1_UUID = "player1_uuid";
    public static final String GAMEROOM_KEY_PLAYER1_STATUS = "player1_status";
    public static final String GAMEROOM_KEY_PLAYER1_LAST_TIME_ACTIVATE = "player1_last_time_active";
    public static final String GAMEROOM_KEY_PLAYER2_UUID = "player2_uuid";
    public static final String GAMEROOM_KEY_PLAYER2_STATUS = "player2_status";
    public static final String GAMEROOM_KEY_PLAYER2_LAST_TIME_ACTIVATE = "player2_last_time_active";
    public static final String GAMEROOM_KEY_FIRST = "who_first";
    public static final String GAMEROOM_KEY_MOVES = "moves";
    public static final String GAMEROOM_KEY_SURRENDEROR = "surrenderor";
    public static final String GAMEROOM_KEY_OPEN = "open";
    public static final String GAMEROOM_VALUE_STATUS_READY = "ready";
    public static final String GAMEROOM_VALUE_STATUS_AWAY = "away";
    public static final String GAMEROOM_VALUE_STATUS_INACTIVE = "inactive";
    public static final String GAMEROOM_VALUE_STATUS_WAIT = "wait";



    private final String LOG_TAG = "FirebaseDatabaseHelper";
    private DatabaseReference databaseRef;
    private ChildEventListener onlineUserListener;
    private ChildEventListener opponentUuidListener;
    private ChildEventListener opponentStatusListener;
    private ValueEventListener myStatusListener;
    private ValueEventListener firstTurnListener;
    private ChildEventListener moveListener;
    private ValueEventListener surrenderorListener;
    private Set<String> onlineUsernames;


    private static FirebaseDatabaseHelper myInstance;

    private FirebaseDatabaseHelper(){
        databaseRef = FirebaseDatabase.getInstance().getReference();
        onlineUsernames = new HashSet<>();
    }

    public static synchronized FirebaseDatabaseHelper getInstance(){
        if( myInstance==null ){
            myInstance = new FirebaseDatabaseHelper();
        }
        return myInstance;
    }

    /****** interface ******/

    public interface Callback {
        void onPostExcute(@Nullable String str);
    }

    public interface BooleanCallback {
        void onTrue(@Nullable String str);
        void onFalse(@Nullable String str);
    }

    public interface UserCallback {
        void onComplete(ArrayList<User> userList);
    }

    public interface GameRoomCallback {
        void onMatchComplete();
        void onOpponentReady();
        void onWhoFirstDecided();
        void onMove(int moveCode);
        void onMyselfAway();
        void onOpponentAway();
        void onOpponentInactive();
        void onOpponentQuit();
        void onOpponentSurrender();
    }



    /***** setter *****/

    public void setLatestVersion(String version){
        DatabaseReference eachUser = databaseRef.child(VERSION_TABLE);
        HashMap<String, Object> newUser = new HashMap<>();
        newUser.put(VERSION_KEY_LATEST_VERSION, version);
        eachUser.updateChildren(newUser);
    }

    public void setOnline(User user){
        DatabaseReference eachUser = databaseRef.child(this.ONLINE_TABLE).child(user.getUsername());
        HashMap<String, Object> newUser = new HashMap<>();
        newUser.put(this.ONLINE_KEY_INVITABLE, user.isInvitable());
        newUser.put(this.ONLINE_KEY_STATUS, this.ONLINE_VALUE_STATUS_AVAILABLE);
        newUser.put(this.ONLINE_KEY_LAST_ACTIVE_TIMESTAMP, System.currentTimeMillis());
        eachUser.updateChildren(newUser);
    }

    public void setOffline(User user){
        DatabaseReference eachUser = databaseRef.child(this.ONLINE_TABLE).child(user.getUsername());
        eachUser.removeValue();
    }

    public void setMyInvitable(boolean b){
        String myUuid = GlobalData.getInstance().getMe().getUuid();
        DatabaseReference eachUser = databaseRef.child(this.USER_TABLE).child(myUuid);
        HashMap<String, Object> newUserData = new HashMap<>();
        newUserData.put(this.ONLINE_KEY_INVITABLE, b);
        eachUser.updateChildren(newUserData);
        String myUsername = GlobalData.getInstance().getMe().getUsername();
        DatabaseReference eachOnlineUser = databaseRef.child(this.ONLINE_TABLE).child(myUsername);
        HashMap<String, Object> newOnlineData = new HashMap<>();
        newOnlineData.put(this.ONLINE_KEY_INVITABLE, b);
        eachOnlineUser.updateChildren(newOnlineData);
    }

    public void registerMyInfo(String roomId){
        GlobalData.getInstance().getMyRoom().setMyLastActiveTimeStamp( System.currentTimeMillis() );
        HashMap<String, Object> myInfo = new HashMap<>();
        myInfo.put(GAMEROOM_KEY_PLAYER2_UUID, GlobalData.getInstance().getMe().getUuid() );
        myInfo.put( GAMEROOM_KEY_PLAYER2_STATUS, GAMEROOM_VALUE_STATUS_WAIT);
        myInfo.put( GAMEROOM_KEY_PLAYER2_LAST_TIME_ACTIVATE, GlobalData.getInstance().getMyRoom().getMyLastActiveTimeStamp());
        databaseRef.child(GAMEROOM_TABLE).child(roomId).updateChildren(myInfo);
    }

    public void setMyGameStatus(String status){
        databaseRef.child(GAMEROOM_TABLE)
                .child(GlobalData.getInstance().getMyRoom().getRoomId())
                .child(GlobalData.getInstance().getMyRoom().getMyStatusKey())
                .setValue( status );
    }

    public void closeRoom(){
        databaseRef.child(GAMEROOM_TABLE)
                .child(GlobalData.getInstance().getMyRoom().getRoomId())
                .child(GAMEROOM_KEY_OPEN)
                .setValue(false);
    }

    public void updateMyActiveTime() {
        databaseRef.child(GAMEROOM_TABLE)
                .child(GlobalData.getInstance().getMyRoom().getRoomId())
                .child(GlobalData.getInstance().getMyRoom().getMyLastActiveTimeStampKey())
                .setValue(GlobalData.getInstance().getMyRoom().getMyLastActiveTimeStamp());
        databaseRef.child(ONLINE_TABLE)
                .child(GlobalData.getInstance().getMe().getUsername())
                .child(ONLINE_KEY_LAST_ACTIVE_TIMESTAMP)
                .setValue(GlobalData.getInstance().getMyRoom().getMyLastActiveTimeStamp());
    }

    public void setSurrenderor(int player){
        databaseRef.child(GAMEROOM_TABLE)
                .child(GlobalData.getInstance().getMyRoom().getRoomId())
                .child(GAMEROOM_KEY_SURRENDEROR)
                .setValue(player);
    }

    public void setWhoFirst(int player){
        databaseRef.child(GAMEROOM_TABLE)
                .child(GlobalData.getInstance().getMyRoom().getRoomId())
                .child(GAMEROOM_KEY_FIRST)
                .setValue(player);
    }

    public void updateWinningRate() {
        User me = GlobalData.getInstance().getMe();
        DatabaseReference myInfo = databaseRef.child(USER_TABLE).child(me.getUuid());
        HashMap<String, Object> newGameData = new HashMap<>();
        newGameData.put(USER_KEY_TOTAL_PLAYED, me.getTotalGameNum());
        newGameData.put(USER_KEY_WINNING_TIMES, me.getWinningGameNum());
        newGameData.put(USER_KEY_WINNING_RATE, me.getWinningRate());
        myInfo.updateChildren(newGameData);
    }


    /****** getter ******/

    public void isLatestVersion(final String version, final BooleanCallback callback){
        Query query = databaseRef.child(this.VERSION_TABLE).child(this.VERSION_KEY_LATEST_VERSION);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( !dataSnapshot.getValue().toString().equalsIgnoreCase(version) ){
                    callback.onFalse(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getUserProfile(final String uuid, final UserCallback callback){
        Query query = databaseRef.child(this.USER_TABLE).child(uuid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User();
                user.setUuid( uuid );
                user.setUsername( dataSnapshot.child(USER_KEY_USERNAME).getValue().toString() );
                user.setCountry( dataSnapshot.child(USER_KEY_COUNTRY).getValue().toString() );
                user.setTotalGameNum( Integer.valueOf(dataSnapshot.child(USER_KEY_TOTAL_PLAYED).getValue().toString()) );
                user.setWinningGameNum( Integer.valueOf(dataSnapshot.child(USER_KEY_WINNING_TIMES).getValue().toString()) );
                user.setWinningRate( Float.valueOf(dataSnapshot.child(USER_KEY_WINNING_RATE).getValue().toString()) );
                user.setInvitable( (boolean)dataSnapshot.child(USER_KEY_INVITABLE).getValue() );
                ArrayList<User> userList = new ArrayList<>();
                userList.add(user);
                callback.onComplete(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void isUuidExist(final String uuid, final BooleanCallback callback){
        Query query = databaseRef.child(this.USER_TABLE).child(uuid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ) {
                    callback.onTrue(null);
                } else {
                    callback.onFalse(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void isUsernameExist(final String username, final BooleanCallback callback){
        Query query = databaseRef.child(this.USER_TABLE);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot eachUser = iterator.next();
                    String thisUsername = eachUser.child(USER_KEY_USERNAME).getValue().toString();
                    if( thisUsername.equalsIgnoreCase(username) ){
                        callback.onTrue(null);
                        return;
                    }
                }
                callback.onFalse(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getTop20Users(final UserCallback callback){
        Query query = databaseRef.child(this.USER_TABLE).orderByChild(USER_KEY_WINNING_RATE).limitToLast(20);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> userList = new ArrayList<User>();
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot eachUser = iterator.next();
                    String thisUsername = eachUser.child(USER_KEY_USERNAME).getValue().toString();
                    String thisCountry = eachUser.child(USER_KEY_COUNTRY).getValue().toString();
                    float thisWinningRate = Float.valueOf( eachUser.child(USER_KEY_WINNING_RATE).getValue().toString() );
                    User user = new User();
                    user.setUsername(thisUsername);
                    user.setCountry(thisCountry);
                    user.setWinningRate(thisWinningRate);
                    userList.add(user);
                }
                callback.onComplete(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getWaitingRoom(final BooleanCallback callback){
        Query query = databaseRef.child(GAMEROOM_TABLE);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ){
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while(iterator.hasNext()){
                        DataSnapshot eachRoom = iterator.next();
                        String eachRoomId = eachRoom.getKey().toString();
                        if(eachRoom.child(GAMEROOM_KEY_PLAYER2_UUID).getValue().toString().isEmpty()
                                && Boolean.valueOf(eachRoom.child(GAMEROOM_KEY_OPEN).getValue().toString()) ){
                            callback.onTrue(eachRoomId);
                            return;
                        }
                    }
                    callback.onFalse(null);
                } else {
                    callback.onFalse(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /****** add *******/

    public void addNewUser(final User user){
        Query query = databaseRef.child(this.USER_TABLE).child(user.getUuid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( !dataSnapshot.exists() ){
                    DatabaseReference eachUser = databaseRef.child(USER_TABLE).child(user.getUuid());
                    HashMap<String, Object> newUser = new HashMap<>();
                    newUser.put(USER_KEY_USERNAME, user.getUsername());
                    newUser.put(USER_KEY_COUNTRY, user.getCountry());
                    newUser.put(USER_KEY_TOTAL_PLAYED, 0);
                    newUser.put(USER_KEY_WINNING_TIMES, 0);
                    newUser.put(USER_KEY_WINNING_RATE, 0);
                    newUser.put(USER_KEY_INVITABLE, true);
                    eachUser.updateChildren(newUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void createNewRoom(Callback callback){
        GameRoom myGame = GlobalData.getInstance().getMyRoom();
        myGame.setMyLastActiveTimeStamp( System.currentTimeMillis() );
        DatabaseReference newRoom = databaseRef.child(GAMEROOM_TABLE).push();
        String roomId = newRoom.getKey();
        HashMap<String, Object> newRoomInfo = new HashMap<>();
        newRoomInfo.put(GAMEROOM_KEY_PLAYER1_UUID, GlobalData.getInstance().getMe().getUuid() );
        newRoomInfo.put( GAMEROOM_KEY_PLAYER1_STATUS, GAMEROOM_VALUE_STATUS_WAIT);
        newRoomInfo.put( GAMEROOM_KEY_PLAYER1_LAST_TIME_ACTIVATE, myGame.getMyLastActiveTimeStamp() );
        newRoomInfo.put(GAMEROOM_KEY_PLAYER2_UUID, "" );
        newRoomInfo.put( GAMEROOM_KEY_PLAYER2_STATUS, "");
        newRoomInfo.put( GAMEROOM_KEY_PLAYER2_LAST_TIME_ACTIVATE, -1 );
        newRoomInfo.put( GAMEROOM_KEY_FIRST, FragmentBoard.EMPTY );
        newRoomInfo.put( GAMEROOM_KEY_SURRENDEROR, FragmentBoard.EMPTY );
        newRoomInfo.put( GAMEROOM_KEY_OPEN, true );
        newRoom.updateChildren(newRoomInfo);
        callback.onPostExcute(roomId);
    }

    public void addNewMove(int moveCode){
        databaseRef.child(GAMEROOM_TABLE)
                .child(GlobalData.getInstance().getMyRoom().getRoomId())
                .child(GAMEROOM_KEY_MOVES)
                .child( String.valueOf(GlobalData.getInstance().getMyRoom().getMoves().size()) )
                .setValue(moveCode);
    }


    /****** delete *******/

    public void deteleRoom(String roomId){
        databaseRef.child(GAMEROOM_TABLE).child(roomId).removeValue();
    }

    public void leaveRoom(){
        GameRoom myGame = GlobalData.getInstance().getMyRoom();
        HashMap<String, Object> myInfo = new HashMap<>();
        myInfo.put( myGame.getMyUuidKey(), "" );
        myInfo.put( myGame.getMyStatusKey(), "");
        myInfo.put( myGame.getMyLastActiveTimeStampKey(), -1 );
        databaseRef.child(GAMEROOM_TABLE).child(myGame.getRoomId()).updateChildren(myInfo);
    }

    public void clearMoves(String roomId){
        databaseRef.child(GAMEROOM_TABLE).child(roomId).child(GAMEROOM_KEY_MOVES).removeValue();
    }




    /****** listener *******/

    public void startListenOnlineUsers(final TextView textView) {
        onlineUserListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onlineUsernames.add(dataSnapshot.getKey().toString());
                textView.setText("Total online users: " + onlineUsernames.size());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                onlineUsernames.remove(dataSnapshot.getKey().toString());
                textView.setText("Total online users: " + onlineUsernames.size());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, databaseError.getMessage());
            }
        };
        databaseRef.child(ONLINE_TABLE).addChildEventListener(onlineUserListener);
    }

    public void stopListenOnlineUsers(){
        databaseRef.removeEventListener(onlineUserListener);
        onlineUserListener = null;
    }

    public void startListenOpponentUuid(final GameRoomCallback callback){
        opponentUuidListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GameRoom myGame = GlobalData.getInstance().getMyRoom();
                if( dataSnapshot.getKey().toString().equalsIgnoreCase( myGame.getOpponentUuidKey() )
                        && !dataSnapshot.getValue().toString().isEmpty() ){
                    // join an existing room
                    String oppoUuid = dataSnapshot.getValue().toString();
                    FirebaseDatabaseHelper.getInstance().getUserProfile(oppoUuid, new UserCallback() {
                        @Override
                        public void onComplete(ArrayList<User> userList) {
                            GlobalData.getInstance().getMyRoom().setOpponent( userList.get(0) );
                            Log.d(LOG_TAG, "Opponent is " + GlobalData.getInstance().getMyRoom().getOpponent().getUsername());
                            callback.onMatchComplete();
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                GameRoom myGame = GlobalData.getInstance().getMyRoom();
                if( dataSnapshot.getKey().toString().equalsIgnoreCase( myGame.getOpponentUuidKey() ) ){
                    if( dataSnapshot.getValue().toString().isEmpty() ){
                        // opponent quit
                        callback.onOpponentQuit();
                    } else {
                        // opponent join
                        String oppoUuid = dataSnapshot.getValue().toString();
                        FirebaseDatabaseHelper.getInstance().getUserProfile(oppoUuid, new UserCallback() {
                            @Override
                            public void onComplete(ArrayList<User> userList) {
                                GlobalData.getInstance().getMyRoom().setOpponent( userList.get(0) );
                                Log.d(LOG_TAG, "Opponent is " + GlobalData.getInstance().getMyRoom().getOpponent().getUsername());
                                callback.onMatchComplete();
                            }
                        });

                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        String myRoomId = GlobalData.getInstance().getMyRoom().getRoomId();
        databaseRef.child(GAMEROOM_TABLE).child(myRoomId).addChildEventListener(opponentUuidListener);
    }

    public void stopListenOpponentUuid(){
        databaseRef.removeEventListener(opponentUuidListener);
        opponentUuidListener = null;
    }

    public void startListenOpponentStatus(final GameRoomCallback callback){
        opponentStatusListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                GameRoom myGame = GlobalData.getInstance().getMyRoom();
                if( dataSnapshot.getKey().toString().equalsIgnoreCase( myGame.getOpponentStatusKey() ) ){
                   if( dataSnapshot.getValue().toString().equalsIgnoreCase(GAMEROOM_VALUE_STATUS_READY) ) {
                       callback.onOpponentReady();
                   } else if( dataSnapshot.getValue().toString().equalsIgnoreCase(GAMEROOM_VALUE_STATUS_AWAY) ){
                       // opponent away
                       callback.onOpponentAway();
                   } else if( dataSnapshot.getValue().toString().equalsIgnoreCase(GAMEROOM_VALUE_STATUS_INACTIVE) ){
                       // opponent inactive
                       callback.onOpponentInactive();
                   } else if( dataSnapshot.getValue().toString().equalsIgnoreCase(GAMEROOM_VALUE_STATUS_WAIT) ){
                        myGame.setOpponentStatus(GAMEROOM_VALUE_STATUS_WAIT);
                   }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        String myRoomId = GlobalData.getInstance().getMyRoom().getRoomId();
        databaseRef.child(GAMEROOM_TABLE).child(myRoomId).addChildEventListener(opponentStatusListener);
    }

    public void stopListenOpponentStatus(){
        databaseRef.removeEventListener(opponentStatusListener);
        opponentStatusListener = null;
    }

    public void startListenMyStatus(final GameRoomCallback callback){
        myStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists()  ){
                    if( dataSnapshot.getValue().toString().equalsIgnoreCase(GAMEROOM_VALUE_STATUS_AWAY) ){
                        callback.onMyselfAway();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseRef.child(GAMEROOM_TABLE)
                .child(GlobalData.getInstance().getMyRoom().getRoomId())
                .child(GlobalData.getInstance().getMyRoom().getMyStatusKey())
                .addValueEventListener(myStatusListener);
    }

    public void stopListenMyStatus(){
        databaseRef.removeEventListener(myStatusListener);
        myStatusListener = null;
    }

    public void startListenWhoFirst(final GameRoomCallback callback){
        firstTurnListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ){
                    int player = Integer.valueOf( dataSnapshot.getValue().toString() );
                    if( player== FragmentBoard.EMPTY ){
                        return;
                    }
                    GameRoom myGame = GlobalData.getInstance().getMyRoom();
                    myGame.setWhoFirst( player );
                    callback.onWhoFirstDecided();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        String myRoomId = GlobalData.getInstance().getMyRoom().getRoomId();
        databaseRef.child(GAMEROOM_TABLE).child(myRoomId).child(GAMEROOM_KEY_FIRST).addValueEventListener(firstTurnListener);
    }

    public void stopListenWhoFirst(){
        databaseRef.removeEventListener(firstTurnListener);
        firstTurnListener = null;
    }

    public void startListenMoves(final GameRoomCallback callback) {
        moveListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                int moveCode = Integer.valueOf( dataSnapshot.getValue().toString() );
                callback.onMove(moveCode);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, databaseError.getMessage());
            }
        };
        String myRoomId = GlobalData.getInstance().getMyRoom().getRoomId();
        databaseRef.child(GAMEROOM_TABLE).child(myRoomId).child(GAMEROOM_KEY_MOVES).addChildEventListener(moveListener);
    }

    public void stopListenMoves() {
        databaseRef.removeEventListener(moveListener);
        moveListener = null;
    }

    public void startListenSurrenderor(final GameRoomCallback callback){
        surrenderorListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() && !dataSnapshot.getValue().toString().isEmpty() ){
                    int player = Integer.valueOf( dataSnapshot.getValue().toString() );
                    GameRoom myGame = GlobalData.getInstance().getMyRoom();
                    if( player!=myGame.getWhoAmI() && player!= FragmentBoard.EMPTY ){
                        callback.onOpponentSurrender();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        String myRoomId = GlobalData.getInstance().getMyRoom().getRoomId();
        databaseRef.child(GAMEROOM_TABLE).child(myRoomId).child(GAMEROOM_KEY_SURRENDEROR).addValueEventListener(surrenderorListener);
    }

    public void stopListenSurrenderor(){
        databaseRef.removeEventListener(surrenderorListener);
        surrenderorListener = null;
    }

    public void startGameRoomListeners(GameRoomCallback callback){
        startListenOpponentStatus(callback);
        startListenMyStatus(callback);
        startListenWhoFirst(callback);
        startListenSurrenderor(callback);
        startListenMoves(callback);
        startListenOpponentUuid(callback);
    }

    public void stopGameRoomListeners(){
        stopListenOpponentStatus();
        stopListenMyStatus();
        stopListenWhoFirst();
        stopListenSurrenderor();
        stopListenMoves();
        stopListenOpponentUuid();
    }






}
