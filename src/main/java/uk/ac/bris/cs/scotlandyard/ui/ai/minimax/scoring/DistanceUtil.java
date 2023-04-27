package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveUtil;

import java.util.List;
import java.util.Map;

public class DistanceUtil {
    private DistanceUtil() {

    }

    public static List<Integer> getDetectiveDistances(Board board, Move move) {
        return getDetectiveDistances(board, MoveUtil.moveDestination(move));
    }


    public static List<Integer> getDetectiveDistances(Board board, Integer location) {
        Map<Integer, Integer> distanceMap = Dijkstra.dijkstra(false, board, location);
        return board.getPlayers().stream()
                .filter(Piece::isDetective)
                .map(piece -> (Piece.Detective) piece)
                .map(p -> {
                    Integer e = distanceMap.get(board.getDetectiveLocation(p).orElseThrow());
                    if (e == null) {
                        throw new IllegalStateException("Could not find location for detective " + p);
                    }
                    return e;
                })
                .toList();
    }
}
