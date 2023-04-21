package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.ArrayList;
import java.util.HashMap;
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
        int mrXLocation = board.getAvailableMoves().stream().filter(move -> move.commencedBy().isMrX()).findFirst().orElseThrow().source(); // all the moves should start at the same position

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
        List<Node> children = moves.stream().map(move -> new Node(move, generate(board, player, source, move, depth), Dijkstra.dijkstraScore(getDetectiveDistances(board, move)))).toList();
        tree.children.addAll(children);
    }

    public static MoveTree generate(Board.GameState board, Piece player, int source, Move startingAt, int depth) {
        if (depth <= 0) {
            return new MoveTree(startingAt.source(), List.of());
        }

        Board.GameState newBoard = board.advance(startingAt);

        List<Move> list = newBoard.getAvailableMoves().stream().toList();
        List<Node> trees = list.stream().map(move ->
                new Node(move,
                        generate(newBoard, move.commencedBy(), move.source(), move, depth - 1),
                        Dijkstra.dijkstraScore(getDetectiveDistances(board, move)))).toList();

        return new MoveTree(source, trees);
    }

    public static List<Integer> getDetectiveDistances (Board board, Move move){
        return getDetectiveDistances(board, moveDestination(move));
    }

    public static List<Integer> getDetectiveDistances (Board board, Integer location){
        Map<Integer,Integer> map = Dijkstra.dijkstra(board, location);
        List<Piece> detectives = board.getPlayers().stream().filter(Piece::isDetective).toList();
        List<Integer> detectiveDistances = new ArrayList<>();
        for (Piece p : detectives) {
            Integer e = map.get(board.getDetectiveLocation((Piece.Detective) p).orElseThrow());
            if(e == null) {
                throw new IllegalStateException("Could not find location for detective " + p);
            }
            detectiveDistances.add(e);
        }
        return detectiveDistances;
    }

    public static Integer getMrXDistance (Board board,Move move){
        HashMap<Integer,Integer> map = (HashMap<Integer, Integer>) Dijkstra.dijkstra(board, moveDestination(move));
        int mrXLocation = board.getAvailableMoves().stream().filter(m -> m.commencedBy().isMrX()).findFirst().orElseThrow().source(); // all the moves should start at the same position
        return map.get(mrXLocation);
    }

    public static Integer getMrXDistance (Board board,Integer location){
        HashMap<Integer,Integer> map = (HashMap<Integer, Integer>) Dijkstra.dijkstra(board, location);
        int mrXLocation = board.getAvailableMoves().stream().filter(m -> m.commencedBy().isMrX()).findFirst().orElseThrow().source(); // all the moves should start at the same position
        return map.get(mrXLocation);
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

    static class Node {
        private final Move move;
        private final MoveTree child;

        private final Double score;

        Node(Move move, MoveTree child, Double score) {
            this.move = move;
            this.child = child;
            this.score = score;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "move=" + move +
                    ", child=" + child +
                    '}';
        }

        public Move getMove() {
            return move;
        }

        public MoveTree getChild() {
            return child;
        }

        public Double getScore() {
            return score;
        }
    }

    private static Integer moveDestination(Move move) {
        Move.Visitor<Integer> destinationChecker = new Move.Visitor<Integer>() {
            @Override
            public Integer visit(Move.SingleMove move) {
                return move.destination;
            }

            @Override
            public Integer visit(Move.DoubleMove move) {
                return move.destination2;
            }
        };
        return move.accept(destinationChecker);
    }

    public int getSource() {
        return source;
    }

    public List<Node> getChildren() {
        return children;
    }
}
