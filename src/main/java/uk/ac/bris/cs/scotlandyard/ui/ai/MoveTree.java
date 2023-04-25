package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class MoveTree {
    private final int source;

    private final List<Node> children;

    public MoveTree(int source, List<Node> children) {
        this.source = source;
        this.children = children;
    }


    /**
     * Generates a subtree of possible moves based on all the moves that can be made
     */
    public static List<Node> generate(Board.GameState board, int depth, boolean allowDoubleMove) {
        if (depth <= 0) {
            return List.of();
        }
        ImmutableSet<Move> availableMoves = board.getAvailableMoves();
        return availableMoves
                .parallelStream()
                .filter(move -> !MyAi.checkDoubleMove(move) || allowDoubleMove) //removes doubles if needed
                .filter(m -> StreamSupport.stream(m.tickets().spliterator(), false).allMatch(t -> t != ScotlandYard.Ticket.SECRET)) // TODO be smarter about secret ticket usage
                .map(move -> new Node(move, generate(board, move, depth - 1, allowDoubleMove), getMoveScore(board, availableMoves, move)))
                .toList();
    }

    /**
     * Generates a subtree of possible moves assuming the given move had been made, i.e. "if we did this move, what would the future state be like?"
     *
     * @param board      the board, which should consider {@code startingAt} a valid move (i.e. {@link Board.GameState#advance(Move)} will not throw an error)
     * @param startingAt the move to "make"
     * @param depth      the max tree depth, which may be {@code <= 0}
     * @return a subtree
     */

    public static MoveTree generate(Board.GameState board, Move startingAt, int depth, boolean allowDoubleMove) {
        if (depth <= 0) {
            return new MoveTree(startingAt.source(), List.of());
        }

        Board.GameState newBoard = board.advance(startingAt);


        List<Node> trees = generate(newBoard, depth - 1, allowDoubleMove);

        return new MoveTree(startingAt.source(), trees);
    }


    public static double getMoveScore(Board board, Collection<Move> alternativeMoves, Move move) {
        int destination = MoveUtil.moveDestination(move);
        double score = Dijkstra.dijkstraScore(getDetectiveDistances(board, move));

        if (move instanceof Move.DoubleMove &&
                alternativeMoves.stream() // if we can get to the same destination using a single move
                        .filter(m -> m instanceof Move.SingleMove) // then we should heavily punish using a double move
                        .anyMatch(m -> MoveUtil.moveDestination(m) == destination)) {
            return score / 10;
        }

        return score;
        /*
         TODO: consider double moves, secret tickets, etc.
            also consider his position (eg dont want to get cornered), and if he has to reveal his move on a turn or not
            Also, the current method of doing the min + avg / 10 seems to discourage risky plays, eg getting close to 1
            detective while evading the other 4. we need to weight it more fairly, not sure how
        */
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


    public int size() {
        return 1 + children.stream().mapToInt(i -> i.child.size()).sum();
    }

    @Override
    public String toString() {
        return "MoveTree{" +
                "source=" + source +
                ", children=" + children +
                '}';
    }

    public int getSource() {
        return source;
    }

    public List<Node> getChildren() {
        return children;
    }

    record Node(Move move, MoveTree child, Double score) {
    }
}
