# Scotland Yard Report

## Model Part ~ 1 page

All the tests pass!

### MyGameStateFactory
#### The advance() method:
We used the visitor pattern in a private helper method called visitMrXMove
...
### MyModelFactory
#### The build() Method:
registerObserver and unregisterObserver methods were made more complex by the usage of ImmutableSet<Observer>
since the set had to be reconstructed each time, rather than just altering the original set


## AI Part ~ 2 pages


We started by making a tree of all possible moves. Initially, for faster prototyping we had to rely on some implementation detail, by casting
the `Board` to a `Board.GameState` to get access to the `advance(Move)` method. This meant that we could build the tree quite easily:
look at all current possible moves (by using `Board#getAvailableMoves`), add them to a tree, then for each possible move, advance as if the move had been played,
then recurse. 
While not in our control, the immutability of Board made this a lot easier as we didn't need to worry about "backtracking" the board state.
This was very inefficient - with a maximum depth of 3, the tree contained over sixty-thousand nodes and took ~5 seconds to generate. 
While we didn't want to prematurely optimise, we knew some sort of pruning was going to be very important.


Research on the minimax and alpha-beta pruning algorithms led us to decide that we needed a scoring function, to evaluate the effectiveness of any given move.

This took some time to conceptualise and plan. We also decided that given the detectives' limited knowledge of the game state (they cannot see where Mr X currently is,
they only know which tickets he used), detective AI couldn't be very complicated - the scoring function determines effectiveness of a move, 
but the detectives don't actually know how effective their move was. Adding proper scoring to the detective AI would give the effect of them cheating. 
Therefore, detective AI wasn't a priority when developing.

We used Dijkstra's algorithm for the basis of our scoring function, starting from the possible move's destination
and then looking at the distance from that destination to all the detective's locations, giving a list of numbers.
Determining an algorithm to combine these numbers into a single score was a little tricky. For example, take the slightly contrived situation where a move meant that 5/6 detectives were 8 squares away, but 1 detective was 1 square away.

* Looking at the mean score would give a score of 6.84 even though it's actually a very risky move, so this isn't sufficient
* Looking at the median score gives a score of a 8, which is even worse than mean.
* Looking at only the minimum score would give a score of 1, which is overly harsh, as it ignores the fact that 5/6 detectives have been evaded very well.

We ended up using a combination of minimum and mean: `min(distances) + mean(distances) / 10`. In this situation this gives an output of `1.684`

An issue we discovered with this scoring system was that since Mr.X is just aiming to be as far away from the detectives as possible,
he often ends up in the corner of the map and then it is fairly difficult for him to escape once the detectives get close.
So to stop him going towards the corners we could add a pre-calculated value for each location,
which is based on their distance from the centre of the board, the number of adjacent locations and the tickets that can be used to move from that location

We could also consider the value of tickets and how many we have remaining, this would probably make a better scoring function,
however theoretically a tree of infinite depth would factor this in already, due to the fact we are using 'board.getAvailableMoves()' which will only return moves possible at the potential board state that we are evaluating,
therefore we decided that a better idea would be to increase the efficiency and allow us more depth in our gameTree

After initially generating a huge game tree, we realised there were several optimisations to reduce the size of the tree without losing depth:
* Removing double moves from the tree when the Mr.X isn't too close to the detectives.
  * We could also not consider single moves at all when Mr.X is close to losing, but single moves may be more effective when Mr.X is in a corner, and there aren't that many of them anyway
* Removing secret moves from the tree, since they are just duplicates of other moves, then a secret move can be made when the best move for Mr.X is still not great.

Additionally

Constructing a tree:
We looked at other examples of making a game-tree for two player games and adapted these concepts for Scotland Yard.
So when constructing a tree we had a decision to make, do we consider all detectives separately (one after the other) or group there moves together.
//Insert a diagram of tree

Optimizations: 
TODO: generate tree semi-lazily, 


    WHY DOES getPlayers() RETURN A Piece!!!

### Potential Further Improvements

Using a dynamic tree depth: 
  Let the best move be calculated for as long as possible before the move timeout, with no predetermined depth.
  This would help because it would get the maximum depth possible for each turn, currently the depth is fixed and some moves happen quickly and some slowly.

Use a tree memory system, where the tree is generated with up until the maximum time to make a move and then a move is made. The subtree from which the move is made is remembered and stored.
Then on the next turn you can pick up from here remove the branches that are now impossible due to the detectives moves, then continue building the tree for as much time as possible before making another move

