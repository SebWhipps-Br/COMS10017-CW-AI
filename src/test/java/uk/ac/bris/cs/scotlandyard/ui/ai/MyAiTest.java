package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public class MyAiTest {

    // To test the time of pick move
    @Test
    public void testPickSingleMove() throws IOException{
        MyModelFactory modelFactory = new MyModelFactory();

        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        // default detective number of 5
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 10);

        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white, yellow);
        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        MyAi myAi = new MyAi();

        //the time parameter
        Pair<Long, TimeUnit> timeoutPair = new Pair<>((long) 15, TimeUnit.SECONDS);
        //

        System.out.println(myAi.pickMove(currentBoard,timeoutPair));
    }
}
