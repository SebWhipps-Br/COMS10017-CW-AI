package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoveTree {
    private final int source;

    private final List<Node> children;

    public MoveTree(int source, List<Node> children) {
        this.source = source;
        this.children = children;
    }

    /**
     * Generates the root tree from a board. It must be Mr X's turn
     *
     * @param board the board
     * @param depth the max depth of the tree
     * @return the tree
     */
    public static MoveTree generateRootTree(GameState board, int depth) {

        int mrXLocation = board.getAvailableMoves().stream()
                .filter(move -> move.commencedBy().isMrX())
                .findAny()
                .orElseThrow()
                .source(); // all the moves should start at the same position

        return new MoveTree(mrXLocation, generate(board, depth));
    }


    /**
     * Generates a subtree of possible moves based on all the moves that can be made
     */
    public static void generate(MoveTree tree, Board.GameState board, Piece player, int source, int depth, Double alpha, Double beta, boolean allowDoubleMove) {
        List<Move> moves = board.getAvailableMoves().stream().filter(move -> move.commencedBy().equals(player))
                .filter(move -> (!MyAi.checkDoubleMove(move) || allowDoubleMove)).toList(); //removes doubles if needed
        List<Node> children = moves.stream()
                .parallel()
                .filter(move -> move instanceof Move.SingleMove)
                .map(move -> new Node(move, generate(board, move, depth - 1, allowDoubleMove),
                        getMoveScore(board, move)))
    public static List<Node> generate(GameState board, int depth) {
        if (depth <= 0) {
            return List.of();
        }
        return board.getAvailableMoves()
                .parallelStream()
                .filter(m -> StreamSupport.stream(m.tickets().spliterator(), false).allMatch(t -> t != ScotlandYard.Ticket.SECRET)) // TODO be smarter about secret ticket usage
                .map(move -> new Node(move, generate(board, move, depth - 1), getMoveScore(board, move)))
                .toList();
    }

    /**
     * Generates a subtree of possible moves assuming the given move had been made, i.e. "if we did this move, what would the future state be like?"
     *
     * @param board      the board, which should consider {@code startingAt} a valid move (i.e. {@link GameState#advance(Move)} will not throw an error)
     * @param startingAt the move to "make"
     * @param depth      the max tree depth, which may be {@code <= 0}
     * @return a subtree
     */
    
    public static MoveTree generate(Board.GameState board, Move startingAt, int depth, boolean allowDoubleMove) {
        if (depth <= 0) {
            return new MoveTree(startingAt.source(), List.of());
        }

        Board.GameState newBoard = board.advance(startingAt);

        List<Node> trees = generate(newBoard, depth - 1);
        List<Move> list = newBoard.getAvailableMoves().stream()
                .filter(move -> (!MyAi.checkDoubleMove(move) || allowDoubleMove))
                .toList();
        List<Node> trees = list.stream()
                .parallel()
                .filter(move -> move instanceof Move.SingleMove)
                .map(move ->
                        new Node(move,
                                generate(newBoard, move, depth - 1, allowDoubleMove),
                                getMoveScore(board, move))).toList();

        return new MoveTree(startingAt.source(), trees);
    }


    public static double getMoveScore(Board board, Move move) {
        return Dijkstra.dijkstraScore(getDetectiveDistances(board, move));
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
