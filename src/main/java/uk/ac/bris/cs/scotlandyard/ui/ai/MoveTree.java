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

    public static MoveTree generate(Board.GameState board, int depth) {
        Piece mrX = board.getPlayers().stream().filter(Piece::isMrX).findFirst().orElseThrow();
        int mrXLocation = board.getAvailableMoves().stream()
                .filter(move -> move.commencedBy().isMrX())
                .findFirst().orElseThrow().source(); // all the moves should start at the same position

        MoveTree root = new MoveTree(mrXLocation, new ArrayList<>());
        generate(root, board, mrX, mrXLocation, depth, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);

        return root;
    }

    /**
     * Generates a subtree of possible moves, and adds them to the given tree.
     * <p>
     * Alpha =
     */
    public static void generate(MoveTree tree, Board.GameState board, Piece player, int source, int depth, Double alpha, Double beta) {
        List<Move> moves = board.getAvailableMoves().stream().filter(move -> move.commencedBy().equals(player)).toList();
        List<Node> children = moves.stream()
                .parallel()
                .map(move -> new Node(move, generate(board, move, depth - 1),
                        getMoveScore(board, move)))
                .toList();
        tree.children.addAll(children);
    }

    public static MoveTree generate(Board.GameState board, Move startingAt, int depth) {
        if (depth <= 0) {
            return new MoveTree(startingAt.source(), List.of());
        }

        Board.GameState newBoard = board.advance(startingAt);

        List<Move> list = newBoard.getAvailableMoves().stream().toList();
        List<Node> trees = list.stream()
                .parallel()
                .map(move ->
                        new Node(move,
                                generate(newBoard, move, depth - 1),
                                getMoveScore(board, move))).toList();

        return new MoveTree(startingAt.source(), trees);
    }

    public static Double getMoveScore(Board board, Move move) {
        double score =  Dijkstra.dijkstraScore(getDetectiveDistances(board, move));
        if(move instanceof Move.DoubleMove) {
            return  score / 2;
        }
        return score;
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
