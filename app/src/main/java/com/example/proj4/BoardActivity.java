package com.example.proj4;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

public class BoardActivity extends AppCompatActivity {

    TableLayout table;
    Button makeGuessButton;
    TextView textView;
    Button switchModesButton;
    private static final int TABLE_WIDTH = 10;
    private static final int TABLE_HEIGHT = 10;
    private static final int P1 = 1;
    private static final int P2 = 2;
    Pair<Integer, Integer> cur_pos_p1;
    Pair<Integer, Integer> cur_pos_p2;
    Pair<Integer, Integer> cur_pos_gopher;
    boolean p1_isGuessed[][] = new boolean[10][10];
    boolean p2_isGuessed[][] = new boolean[10][10];

    HandlerThread player1 = new HandlerThread("p1");
    HandlerThread player2 = new HandlerThread("p2");
    Handler p1_handler;
    Handler p2_handler;
    Handler main_handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        player1.start();
        player2.start();

        main_handler = new Handler();

        p1_handler = new Handler(player1.getLooper())
        {
            @Override
            public void handleMessage(Message msg) {
                Pair<Integer, Integer> next_pos;
                next_pos = getRandomCell();

                if(!cur_pos_p1.equals(next_pos))
                {
                    final Pair<Integer, Integer> finalNext_pos = next_pos;
                    main_handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changeIcon(finalNext_pos, cur_pos_p1, P1);
                            cur_pos_p1 = finalNext_pos;
                            respondToGuess(P1);
                        }
                    });
                }
                else
                {
                    next_pos = getRandomCell();
                }
            }
        };


        p2_handler = new Handler(player1.getLooper())
        {
            @Override
            public void handleMessage(Message msg) {
                Pair<Integer, Integer> next_pos = Pair.create(0,0);
                if(cur_pos_p2.first < 10)
                {
                    if(cur_pos_p2.second != 9)
                    {
                        next_pos = Pair.create(cur_pos_p2.first, cur_pos_p2.second + 1);
                    }
                    else
                    {
                        if(cur_pos_p2.first != 9)
                        {
                            next_pos = Pair.create(cur_pos_p2.first + 1, 0);
                        }
                    }
                }
                final Pair<Integer, Integer> finalNext_pos = next_pos;
                main_handler.post(new Runnable() {
                    @Override
                    public void run() {
                        changeIcon(finalNext_pos, cur_pos_p2, P2);
                        cur_pos_p2 = finalNext_pos;
                        respondToGuess(P2);
                    }
                });

            }
        };

        //Initialize isGuessed array to false
        for (boolean[] row : p1_isGuessed)
            Arrays.fill(row, false);

        for (boolean[] row : p2_isGuessed)
            Arrays.fill(row, false);

        cur_pos_p1 = getRandomCell();
        //Toast.makeText(BoardActivity.this, "P1: " + cur_pos_p1.first + " " + cur_pos_p1.second, Toast.LENGTH_LONG).show();
        cur_pos_p2 = Pair.create(0,0);

        while(cur_pos_p1.equals(cur_pos_p2))
        {
            cur_pos_p1 = getRandomCell();
        }
        //Toast.makeText(BoardActivity.this, "P2: " + cur_pos_p2.first + " " + cur_pos_p2.second, Toast.LENGTH_LONG).show();

        cur_pos_gopher = getRandomCell();

        while(cur_pos_gopher.equals(cur_pos_p1) || cur_pos_gopher.equals(cur_pos_p2))
        {
            cur_pos_gopher = getRandomCell();
        }

        //Toast.makeText(BoardActivity.this, "G: " + cur_pos_gopher.first + " " + cur_pos_gopher.second, Toast.LENGTH_LONG).show();

        makeGuessButton = findViewById(R.id.button1);
        textView = findViewById(R.id.textView2);
        //switchModesButton = findViewById(R.id.button2);

        if(getIntent().getStringExtra("game_mode").equals("2"))
        {
            makeGuessButton.setVisibility(View.GONE);
            //switchModesButton.setVisibility(View.GONE);
        }

        p1_isGuessed[cur_pos_p1.first][cur_pos_p1.second] = true;
        p2_isGuessed[cur_pos_p2.first][cur_pos_p2.second] = true;

        drawBoard();

//        switchModesButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                makeGuessButton.setVisibility(View.GONE);
//                switchModesButton.setVisibility(View.GONE);
//
//                for(int i = 0; i < 100; i++)
//                {
//                    Message message1 = p1_handler.obtainMessage(BoardActivity.P1);
//                    p1_handler.sendMessageDelayed(message1, 2*i*100);
//                    Message message2 = p2_handler.obtainMessage(BoardActivity.P2);
//                    p2_handler.sendMessageDelayed(message2, (2*i + 1) *100);
//                }
//
//            }
//        });

        makeGuessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(makeGuessButton.getText().equals("Player 1 Guess"))
                {
                    Message message1 = p1_handler.obtainMessage(BoardActivity.P1);
                    p1_handler.sendMessage(message1);
                    makeGuessButton.setText("Player 2 Guess");
                }
                else
                {
                    Message message2 = p2_handler.obtainMessage(BoardActivity.P2);
                    p2_handler.sendMessage(message2);
                    makeGuessButton.setText("Player 1 Guess");
                }
            }
        });

        if(makeGuessButton.getVisibility() == View.GONE)
        {
            for(int i = 0; i < 100; i++)
            {
                Message message1 = p1_handler.obtainMessage(BoardActivity.P1);
                p1_handler.sendMessageDelayed(message1, 2*i*800);
                Message message2 = p2_handler.obtainMessage(BoardActivity.P2);
                p2_handler.sendMessageDelayed(message2, (2*i + 1) *800);
            }
        }
    }

    public void respondToGuess(int player)
    {
        Pair<Integer, Integer> cur_pos_p;
        String toastMessage = "";
        if(player == 1)
        {
            cur_pos_p = cur_pos_p1;
        }
        else
        {
            cur_pos_p = cur_pos_p2;
        }

        //Success
        if(cur_pos_p.equals(cur_pos_gopher))
        {
            //SUCCESS
            toastMessage = "Success!";
            p1_handler.removeMessages(P1);
            p2_handler.removeMessages(P2);
            if(makeGuessButton.getVisibility() == View.VISIBLE)
            {
                //makeGuessButton.setText("Player " + player + " won!");
                makeGuessButton.setEnabled(false);
            }
        }


        //Near miss
        Pair<Integer, Integer> nearAbove = Pair.create(cur_pos_gopher.first - 1, cur_pos_gopher.second);
        Pair<Integer, Integer> nearAboveRight = Pair.create(cur_pos_gopher.first - 1, cur_pos_gopher.second + 1);
        Pair<Integer, Integer> nearAboveLeft = Pair.create(cur_pos_gopher.first - 1, cur_pos_gopher.second - 1);
        Pair<Integer, Integer> nearRight = Pair.create(cur_pos_gopher.first, cur_pos_gopher.second + 1);
        Pair<Integer, Integer> nearLeft = Pair.create(cur_pos_gopher.first, cur_pos_gopher.second - 1);
        Pair<Integer, Integer> nearBelow = Pair.create(cur_pos_gopher.first + 1, cur_pos_gopher.second);
        Pair<Integer, Integer> nearBelowRight = Pair.create(cur_pos_gopher.first + 1, cur_pos_gopher.second + 1);
        Pair<Integer, Integer> nearBelowLeft = Pair.create(cur_pos_gopher.first + 1, cur_pos_gopher.second - 1);

        if(cur_pos_p.equals(nearAbove) || cur_pos_p.equals(nearAboveRight) || cur_pos_p.equals(nearAboveLeft) ||
                cur_pos_p.equals(nearRight) || cur_pos_p.equals(nearLeft) || cur_pos_p.equals(nearBelow) ||
                cur_pos_p.equals(nearBelowRight) || cur_pos_p.equals(nearBelowLeft))
        {
            //NEAR MISS
            toastMessage = "Near Miss!";
        }

        //Close guess
        Pair<Integer, Integer> closeAbove = Pair.create(cur_pos_gopher.first - 2, cur_pos_gopher.second);
        nearAboveRight = Pair.create(cur_pos_gopher.first - 2, cur_pos_gopher.second + 1);
        Pair<Integer, Integer> closeAboveRight = Pair.create(cur_pos_gopher.first - 2, cur_pos_gopher.second + 2);
        Pair<Integer, Integer> closeRightAbove = Pair.create(cur_pos_gopher.first - 1, cur_pos_gopher.second + 2);
        Pair<Integer, Integer> closeRight = Pair.create(cur_pos_gopher.first, cur_pos_gopher.second + 2);
        Pair<Integer, Integer> closeRightBelow = Pair.create(cur_pos_gopher.first + 1, cur_pos_gopher.second + 2);
        Pair<Integer, Integer> closeBelowRight = Pair.create(cur_pos_gopher.first + 2, cur_pos_gopher.second + 2);
        nearBelowRight = Pair.create(cur_pos_gopher.first + 2, cur_pos_gopher.second + 1);
        Pair<Integer, Integer> closeBelow = Pair.create(cur_pos_gopher.first + 2, cur_pos_gopher.second);
        nearBelowLeft = Pair.create(cur_pos_gopher.first + 2, cur_pos_gopher.second - 1);
        Pair<Integer, Integer> closeBelowLeft = Pair.create(cur_pos_gopher.first + 2, cur_pos_gopher.second - 2);
        Pair<Integer, Integer> closeLeftBelow = Pair.create(cur_pos_gopher.first + 1, cur_pos_gopher.second - 2);
        Pair<Integer, Integer> closeLeft = Pair.create(cur_pos_gopher.first, cur_pos_gopher.second - 2);
        Pair<Integer, Integer> closeLeftAbove = Pair.create(cur_pos_gopher.first - 1, cur_pos_gopher.second - 2);
        Pair<Integer, Integer> closeAboveLeft = Pair.create(cur_pos_gopher.first - 2, cur_pos_gopher.second - 2);
        nearAboveLeft = Pair.create(cur_pos_gopher.first - 2, cur_pos_gopher.second - 1);




        if(cur_pos_p.equals(closeAbove) || cur_pos_p.equals(closeAboveRight) || cur_pos_p.equals(closeAboveLeft) ||
                cur_pos_p.equals(closeRight) || cur_pos_p.equals(closeLeft) || cur_pos_p.equals(closeBelow) ||
                cur_pos_p.equals(closeBelowRight) || cur_pos_p.equals(closeBelowLeft) || cur_pos_p.equals(closeRightAbove) ||
                cur_pos_p.equals(closeRightBelow) || cur_pos_p.equals(closeLeftBelow) || cur_pos_p.equals(closeLeftAbove) ||
                cur_pos_p.equals(nearAboveRight) || cur_pos_p.equals(nearBelowRight) || cur_pos_p.equals(nearBelowLeft) || cur_pos_p.equals(nearAboveLeft))
        {
            //CLOSE GUESS
            toastMessage = "Close Guess!";
        }

        //Disaster
        if(p1_isGuessed[cur_pos_p.first][cur_pos_p.second] && p2_isGuessed[cur_pos_p.first][cur_pos_p.second])
        {
            //DISASTER
            toastMessage = "Disaster!";
        }

        if(toastMessage.equals(""))
        {
            toastMessage = "Complete Miss!";
        }


//        Toast toast= Toast.makeText(BoardActivity.this, "Player " + player + " " + toastMessage, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
//        toast.show();


        textView.setText("Player " + player + " " + toastMessage);







    }

    public Pair<Integer, Integer> getRandomCell()
    {
        Random r = new Random();
        int random_row = r.nextInt(TABLE_HEIGHT - 0) + 0;
        int random_column = r.nextInt(TABLE_WIDTH - 0) + 0;

        Pair<Integer, Integer> p = Pair.create(random_row, random_column);
        return p;

    }

    public void drawBoard()
    {
        table = findViewById(R.id.gameBoard);

        // Populate the table with stuff
        for (int x = 0; x < TABLE_HEIGHT; x++) {
            final int row = x;
            TableRow r = new TableRow(this);
            table.addView(r);
            for (int y = 0; y < TABLE_WIDTH; y++) {
                final int col = y;
                ImageView imageView = new ImageView(this);

                if (row == cur_pos_p1.first && col == cur_pos_p1.second)
                {
                    imageView.setImageResource(R.drawable.p1);
                }
                else if(row == cur_pos_p2.first && col == cur_pos_p2.second)
                {
                    imageView.setImageResource(R.drawable.p2);
                }
                else if(row == cur_pos_gopher.first && col == cur_pos_gopher.second)
                {
                    imageView.setImageResource(R.drawable.gopher);
                }
                else
                {
                    imageView.setImageResource(R.drawable.circle);
                }
                r.addView(imageView);
            }
        }
    }

    public void changeIcon(Pair<Integer, Integer> next_pos, Pair<Integer, Integer> cur_pos, int player)
    {
        TableRow row = (TableRow) table.getChildAt(next_pos.first);
        //View cell = (ImageView) row.getChildAt(next_pos.second);
        ImageView cell = (ImageView) row.getChildAt(next_pos.second);
        if(player == 1)
        {
            cell.setImageResource(R.drawable.p1);
            p1_isGuessed[next_pos.first][next_pos.second] = true;
        }
        else
        {
            cell.setImageResource(R.drawable.p2);
            p2_isGuessed[next_pos.first][next_pos.second] = true;
        }


        row = (TableRow) table.getChildAt(cur_pos.first);
        cell = (ImageView) row.getChildAt(cur_pos.second);
        //((ImageView) cell).setImageResource(R.drawable.p1_guessed);

        if(player == 1)
        {
            if(p2_isGuessed[cur_pos.first][cur_pos.second])
            {
                cell.setImageResource(R.drawable.both_guessed);
            }
            else
            {
                cell.setImageResource(R.drawable.p1_guessed);
            }


        }
        else
        {
            if(p1_isGuessed[cur_pos.first][cur_pos.second])
            {
                cell.setImageResource(R.drawable.both_guessed);
            }
            else
            {
                cell.setImageResource(R.drawable.p2_guessed);
            }

        }

    }
}
