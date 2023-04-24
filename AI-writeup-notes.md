# Scotland Yard Report

## What has been done

### Model Part ~ 1 page

All the tests pass!

### AI Part ~ 3 pages

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


TODO: generate tree semi-lazily, 

TODO: Talk about generating tree with all the detective just a single detective