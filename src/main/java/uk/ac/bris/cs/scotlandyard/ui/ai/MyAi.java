package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.TimeUnit;


import javax.annotation.Nonnull;


import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import static java.util.stream.Collectors.summingDouble;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Dijkstratron"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {

		List<Move> moves = board.getAvailableMoves().asList();
//		System.out.println(moves);
//		System.out.println(moves.get(0));
//		System.out.println(moves.get(0).hashCode());
//		System.out.println(moves.get(0).source());
//		System.out.println(moves.get(0).toString());
//		System.out.println(checkDoubleMove(moves.get(0)));
//
//		System.out.println(getDetectiveLocations(board));
		//dijkstra for each possible move
		Dictionary<Move, Double> moveScores = new Hashtable<>();
		Double bestScore = 0.0; //higher is better
		Move best = moves.get(0);
		for (Move move : moves) {
			Double d = dijkstra(board, move);
			if (d > bestScore) {
				bestScore = d;
				best = move;
			}
			moveScores.put(move, d);

		}

		return best;
		// returns a random move, replace with your own implementation
		//return moves.get(new Random().nextInt(moves.size()));
	}

	private boolean checkDoubleMove(Move move){
		Move.Visitor<Boolean> doubleMoveChecker = new Move.Visitor<Boolean>() {
			@Override
			public Boolean visit(Move.SingleMove move) {
				return false;
			}

			@Override
			public Boolean visit(Move.DoubleMove move) {
				return true;
			}
		};
		return move.accept(doubleMoveChecker);
	}

	private Integer moveDestination(Move move){
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

	private Set<Integer> getDetectiveLocations(Board board) {
		Set<Integer> detectiveLocations = new HashSet<>();
		for (Piece piece : board.getPlayers()) {
			if (piece.isDetective()) {
				detectiveLocations.add( board.getDetectiveLocation((Piece.Detective) piece).get() );
			}
		}
		return detectiveLocations;
	}

	private Double dijkstra(Board board, Move move) {
		Set<Integer> detectiveLocations = getDetectiveLocations(board);

		List<Integer> unvisited = new ArrayList<>(board.getSetup().graph.nodes());
		System.out.println(unvisited.toString());
		System.out.println(board.getSetup().graph.toString());
		System.out.println(board.getSetup().graph.adjacentNodes(move.source()));
		Dictionary<Integer, Double> distances = new Hashtable<>();
		for (Integer node : board.getSetup().graph.nodes()) {
			distances.put(node, Double.POSITIVE_INFINITY);
		}

		Integer destination = moveDestination(move);
		distances.put(destination, 0.0);
		Set<Integer> visited = new HashSet<>();

		LinkedHashSet<Integer> toVisitSet = new LinkedHashSet<>();
		ArrayList<Integer> toVisitArray = new ArrayList<>();

		boolean finished = false;
		Integer current = destination;
		toVisitSet.add(current);
		toVisitArray.add(current);
		List<Integer> adjacentUnvisited;
		Double weight = 1.0;
		int arrayIndex = 0;
		while (!finished) {
			adjacentUnvisited = board.getSetup().graph.adjacentNodes(current).stream().toList();
			for (Integer node : visited) {
				adjacentUnvisited.remove(node);
			}
			for (Integer node : adjacentUnvisited){
				if (toVisitSet.add(node)) toVisitArray.add(node);

				if (distances.get(node) > distances.get(current) + weight){
					distances.put(node, distances.get(current) + weight);
				}
			}
			visited.add(current);
			if (toVisitArray.isEmpty()){
				finished = true;
			} else {
				System.out.println("!   :" +toVisitArray.toString());
				//toVisitArray.remove(0);
				arrayIndex += 1;
				current = toVisitArray.get(arrayIndex);
			}

		}








		/*
		int i = 0;
		while (unvisited.size() > i-1) {

			Integer currentNode = unvisited.get(i);
			System.out.println(currentNode.toString()+"\n");
			Set<Integer> adjacentNodes = board.getSetup().graph.adjacentNodes(currentNode);
			final int weight = 1;
			for (Integer node : adjacentNodes) {
				if (distances.get(node) > distances.get(currentNode) + weight){
					distances.put(node, (distances.get(currentNode) + weight) );
				}
			}
			i += 1;

		}

		 */

		// now lookup how close the detectives are
		Set<Double> detectiveDistances = new HashSet<>();
		for (Integer location : detectiveLocations) {
			detectiveDistances.add(distances.get(location));
		}

		return dijkstraScore(detectiveDistances);

	}
	private Double dijkstraScore(Set<Double> distances){
		//Double sum = distances.stream().collect(summingDouble(x -> x));
		Double shortest = Collections.min(distances);
		return shortest;
	}


}
