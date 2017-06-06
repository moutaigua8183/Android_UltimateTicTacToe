package com.moutaigua.ultimatetictactoe;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

/**
 * Created by mou on 3/18/17.
 */

public class FragmentBoard extends Fragment {

    private final String LOG_TAG = "FragmentBoard";
    public static final int EMPTY = -1;
    public static final int PLAYER_1 = 1;
    public static final int PLAYER_2 = 2;
    public static final int TIE = 3;
    public static final int GAME_STATE_PREPARE = 0;
    public static final int GAME_STATE_PROCESSING = 1;
    public static final int GAME_STATE_OVER = 2;

    private ActivityConnector connector;

    private HashMap<Integer, ImageButton> btnGroup;
    private ArrayList<LocalBoard> localBoards;
    private Stack<Integer> moves;
    private ImageView globalBackground;
    private View globalGroupView;
    private int curPlayer;
    private int curGameState;
    private int globalResult;


    public interface ActivityConnector {
        void onBoardClick(int squareCode);
        void onMoveAdded(int squareCode);
    }


    public static FragmentBoard newInstance(ActivityConnector activityConnector) {
        FragmentBoard myFragment = new FragmentBoard();
        myFragment.addInterface(activityConnector);
        return myFragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.board_global, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initFragment();
        reset();
        decideWhoFirst();
    }


    private void initFragment(){
        // local boards
        localBoards = new ArrayList<>();
        for(int i=0; i<9; ++i){
            String eachlocalBoardBgId = "board_global_" + i + "_imgview_bg";
            int eachLocalBoardBgResId = getActivity().getResources()
                    .getIdentifier(eachlocalBoardBgId, "id", getActivity().getPackageName());
            ImageView eachLocalBoardBg = (ImageView) getActivity().findViewById(eachLocalBoardBgResId);
            String eachlocalBoardBtnGroupId = "board_global_" + i + "_button_group";
            int eachLocalBoardBtnGroupResId = getActivity().getResources()
                    .getIdentifier(eachlocalBoardBtnGroupId, "id", getActivity().getPackageName());
            View eachLocalBoardBtnGroup = getActivity().findViewById(eachLocalBoardBtnGroupResId);
            LocalBoard eachBoard = new LocalBoard(eachLocalBoardBg, eachLocalBoardBtnGroup);
            localBoards.add(eachBoard);
        }
        // buttons
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int squareCode = (int) view.getTag();
                connector.onBoardClick(squareCode);
            }
        };
        btnGroup = new HashMap<>();
        for(int localBoardIndex=0; localBoardIndex<9; ++localBoardIndex){
            String eachlocalBoardId = "board_global_" + localBoardIndex + "_button_group";
            int eachLocalBoardResId = getActivity().getResources()
                    .getIdentifier(eachlocalBoardId, "id", getActivity().getPackageName());
            LinearLayout eachLocalBoard = (LinearLayout) getActivity().findViewById(eachLocalBoardResId);
            for(int btnIndex=0; btnIndex<9; ++btnIndex){
                String eachBtnId = "board_local_imgbtn_" + btnIndex;
                int eachBtnResId = getActivity().getResources()
                        .getIdentifier(eachBtnId, "id", getActivity().getPackageName());
                ImageButton btn = (ImageButton) eachLocalBoard.findViewById(eachBtnResId);
                btn.setTag(localBoardIndex*10+btnIndex);
                btn.setOnClickListener(listener);
                int squareCode = localBoardIndex*10 + btnIndex;
                btnGroup.put(squareCode, btn);
            }
        }
        // moves
        moves = new Stack<>();
        // global background
        globalBackground = (ImageView) getActivity().findViewById(R.id.board_imgview_bg);
        globalGroupView = getActivity().findViewById(R.id.board_global);
    }

    private void addInterface(ActivityConnector activityConnector){
        this.connector = activityConnector;
    }

    private void analyzeGlobalBoard(){
        int[][] gBoard = new int[3][3];
        for(int i=0; i<3; ++i){
            for(int j=0; j<3; ++j){
                gBoard[i][j] = localBoards.get(3*i+j).getResult();
            }
        }
        if( gBoard[0][0] == gBoard[0][1] && gBoard[0][1] == gBoard[0][2] && gBoard[0][2] != EMPTY && gBoard[0][2] != TIE ){
            globalResult = gBoard[0][0];
        } else if( gBoard[1][0] == gBoard[1][1] && gBoard[1][1] == gBoard[1][2] && gBoard[1][2] != EMPTY && gBoard[1][2] != TIE ){
            globalResult = gBoard[1][0];
        } else if( gBoard[2][0] == gBoard[2][1] && gBoard[2][1] == gBoard[2][2] && gBoard[2][2] != EMPTY && gBoard[2][2] != TIE ){
            globalResult = gBoard[2][0];
        } else if( gBoard[0][0] == gBoard[1][0] && gBoard[1][0] == gBoard[2][0] && gBoard[2][0] != EMPTY && gBoard[2][0] != TIE ){
            globalResult = gBoard[0][0];
        } else if( gBoard[0][1] == gBoard[1][1] && gBoard[1][1] == gBoard[2][1] && gBoard[2][1] != EMPTY && gBoard[2][1] != TIE ){
            globalResult = gBoard[0][1];
        } else if( gBoard[0][2] == gBoard[1][2] && gBoard[1][2] == gBoard[2][2] && gBoard[2][2] != EMPTY && gBoard[2][2] != TIE ){
            globalResult = gBoard[0][2];
        } else if( gBoard[0][0] == gBoard[1][1] && gBoard[1][1] == gBoard[2][2] && gBoard[2][2] != EMPTY && gBoard[2][2] != TIE ){
            globalResult = gBoard[0][0];
        } else if( gBoard[2][0] == gBoard[1][1] && gBoard[1][1] == gBoard[0][2] && gBoard[0][2] != EMPTY && gBoard[0][2] != TIE ){
            globalResult = gBoard[2][0];
        } else {
            if (gBoard[0][0] != EMPTY && gBoard[0][1] != EMPTY && gBoard[0][2] != EMPTY
                    && gBoard[1][0] != EMPTY && gBoard[1][1] != EMPTY && gBoard[1][2] != EMPTY
                    && gBoard[2][0] != EMPTY && gBoard[2][1] != EMPTY && gBoard[2][2] != EMPTY){
                globalResult = TIE;
            } else {
                globalResult = EMPTY;
            }
        }
        if( globalResult != EMPTY ){
            curGameState = GAME_STATE_OVER;
        }
    }

    private void showNextValidAreas(int pos){
        if(localBoards.get(pos).getResult()==EMPTY){
            for(int i=0; i<9; ++i){
                if( i==pos ){
                    localBoards.get(i).setValidTo(curPlayer);
                } else {
                    localBoards.get(i).setInvalid();
                }
            }
        } else {
            for(int i=0; i<9; ++i) {
                if (localBoards.get(i).getResult() == EMPTY) {
                    localBoards.get(i).setValidTo(curPlayer);
                } else {
                    localBoards.get(i).setInvalid();
                }
            }
        }
    }

    private void clearValidAreas(){
        for(int i=0; i<9; ++i){
            localBoards.get(i).setInvalid();
        }
    }


    /**** Call in Activity ****/

    public void setButtonOnClickListener(View.OnClickListener listener){
        Iterator iter = btnGroup.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry pair = (Map.Entry)iter.next();
            ImageButton eachBtn = (ImageButton)pair.getValue();
            eachBtn.setOnClickListener(listener);
        }
    }

    public void setWhoFirst(int player){
        this.curPlayer = player;
    }

    public void decideWhoFirst(){
        Random rand = new Random();
        curPlayer = rand.nextInt(2)==0 ? PLAYER_1 : PLAYER_2;
    }

    public int getCurrentPlayer(){
        return curPlayer;
    }

    //set game state PROCESSING to start the game
    public void setGameState(int state){
        this.curGameState = state;
    }

    public int getGameState(){
        return curGameState;
    }

    public void setGameResult(int result){
        this.globalResult = result;
        if( globalResult!=EMPTY ){
            if( globalResult == FragmentBoard.PLAYER_1 ){
                clearValidAreas();
                globalBackground.setImageResource(R.drawable.player1_x_win);
                globalBackground.bringToFront();
            } else if( globalResult == FragmentBoard.PLAYER_2 ){
                clearValidAreas();
                globalBackground.setImageResource(R.drawable.player2_o_win);
                globalBackground.bringToFront();
            } else {
                clearValidAreas();
                globalBackground.setImageResource(R.drawable.global_tie);
                globalBackground.bringToFront();
            }
        }
    }

    public int getGameResult(){
        return globalResult;
    }

    public void addMove(int squareCode){
        if( curGameState==GAME_STATE_PROCESSING ) {
            int localBoardIndex = squareCode / 10;
            int localPos = squareCode % 10;
            if (!localBoards.get(localBoardIndex).isValid()) {
                return;
            }
            if (!localBoards.get(localBoardIndex).isCellEmpty(localPos)) {
                return;
            }
            moves.add(curPlayer * 100 + squareCode);
            localBoards.get(localBoardIndex).putOneMove(localPos, curPlayer);
            // get imageBtn view by spotCode
            ImageButton imageBtn = btnGroup.get(squareCode);
            if (curPlayer == PLAYER_1) {
                imageBtn.setBackgroundResource(R.drawable.player1_x);
                curPlayer = PLAYER_2;
            } else {
                imageBtn.setBackgroundResource(R.drawable.player2_o);
                curPlayer = PLAYER_1;
            }
            analyzeGlobalBoard();
            if (curGameState == GAME_STATE_PROCESSING) {
                showNextValidAreas(localPos);
            } else if( globalResult == FragmentBoard.PLAYER_1 ){
                clearValidAreas();
                globalBackground.setImageResource(R.drawable.player1_x_win);
                globalBackground.bringToFront();
            } else if( globalResult == FragmentBoard.PLAYER_2 ){
                clearValidAreas();
                globalBackground.setImageResource(R.drawable.player2_o_win);
                globalBackground.bringToFront();
            } else {
                clearValidAreas();
                globalBackground.setImageResource(R.drawable.global_tie);
                globalBackground.bringToFront();
            }
            connector.onMoveAdded(squareCode);
        }
    }

    public void reset(){
        curPlayer = EMPTY;
        curGameState = GAME_STATE_PREPARE;
        globalResult = EMPTY;
        moves.clear();
        for(LocalBoard eachLocalBoard : localBoards){
            eachLocalBoard.reset();
        }
        Iterator iter = btnGroup.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry pair = (Map.Entry)iter.next();
            ImageButton eachBtn = (ImageButton)pair.getValue();
            eachBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.board_cell_bg));
        }
        globalBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white_blur));
        globalBackground.setImageResource(android.R.color.transparent);
        globalGroupView.bringToFront();
    }


    private class LocalBoard {
        private int[][] board;
        private int result;
        private int steps;
        private boolean isValid;
        private ImageView background;
        private View buttonGroupView;

        public LocalBoard(ImageView imageView, View btnGroupView) {
            this.board = new int[3][3];
            for(int i=0; i<3; ++i){
                for(int j=0; j<3; ++j){
                    board[i][j] = EMPTY;
                }
            }
            result = EMPTY;
            steps = 0;
            isValid = true;
            background = imageView;
            buttonGroupView = btnGroupView;
        }

        public void putOneMove(int pos, int player){
            if( result==EMPTY ) {
                int row = pos / 3;
                int col = pos % 3;
                board[row][col] = player;
                ++steps;
                analyze();
                if( result!=EMPTY ) {
                    setWinner(result);
                }
            }
        }

        private void analyze(){
            if( board[0][0] == board[0][1] && board[0][1] == board[0][2] && board[0][2] != EMPTY ){
                result = board[0][0];
            } else if( board[1][0] == board[1][1] && board[1][1] == board[1][2] && board[1][2] != EMPTY ){
                result = board[1][0];
            } else if( board[2][0] == board[2][1] && board[2][1] == board[2][2] && board[2][2] != EMPTY ){
                result = board[2][0];
            } else if( board[0][0] == board[1][0] && board[1][0] == board[2][0] && board[2][0] != EMPTY ){
                result = board[0][0];
            } else if( board[0][1] == board[1][1] && board[1][1] == board[2][1] && board[2][1] != EMPTY ){
                result = board[0][1];
            } else if( board[0][2] == board[1][2] && board[1][2] == board[2][2] && board[2][2] != EMPTY ){
                result = board[0][2];
            } else if( board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[2][2] != EMPTY ){
                result = board[0][0];
            } else if( board[2][0] == board[1][1] && board[1][1] == board[0][2] && board[0][2] != EMPTY ){
                result = board[2][0];
            } else if( steps==9 ){
                result = TIE;
            }
            if( result!=EMPTY ){
                isValid = false;
            }
        }

        private void setWinner(int result){
            background.bringToFront();
            if( result==PLAYER_1 ){
                background.setImageResource(R.drawable.player1_x);
            } else if( result==PLAYER_2 ){
                background.setImageResource(R.drawable.player2_o);
            } else if( result==TIE ){
                background.setImageResource(R.drawable.logo_blackwhite);
            }
        }

        public int getResult(){
            return result;
        }

        public void setValidTo(int player){
            isValid = true;
            if(player==PLAYER_1){
                background.setBackgroundColor( ContextCompat.getColor(getActivity(), R.color.player1_secondary) );
            } else {
                background.setBackgroundColor( ContextCompat.getColor(getActivity(), R.color.player2_secondary) );
            }
        }

        public void setInvalid() {
            isValid = false;
            background.setBackgroundColor( ContextCompat.getColor(getActivity(), android.R.color.white) );
        }

        public boolean isValid() {
            return isValid;
        }

        public boolean isCellEmpty(int pos){
            int row = pos / 3;
            int col = pos % 3;
            return board[row][col] == EMPTY;
        }

        public void reset(){
            for(int i=0; i<3; ++i){
                for(int j=0; j<3; ++j){
                    board[i][j] = EMPTY;
                }
            }
            result = EMPTY;
            steps = 0;
            isValid = true;
            background.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
            background.setImageResource(android.R.color.transparent);
            buttonGroupView.bringToFront();
        }

    }
}
