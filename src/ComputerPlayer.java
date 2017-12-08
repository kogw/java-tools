import java.util.HashSet;
import java.util.Set;


public class ComputerPlayer extends Player {
	
	/**
	 * Determines all possible empty locations remaining on board.
	 * 
	 * @param board the current game board to analyze
	 * @return a Set of all empty coordinates
	 */
	private static Set<Pair> generateRemainingLocations(Board board) {
		Set<Pair> remaining = new HashSet<>();
		for (int r = 0; r < board.getRows(); ++r) {
			for (int c = 0; c < board.getCols(); ++c) {
				String value = board.getBoard().get(r).get(c);
				if (value.equals(Board.EMPTY)) {
					Pair pair = new Pair(r, c);
					remaining.add(pair);
				}
			}
		}
		return remaining;
	}
	
	
	public ComputerPlayer(String computerIcon, String playerIcon, Board board) {
		super(computerIcon);
	}
	
	/**
	 * Determines the next best move for the computer to make.
	 * Runs a min-max search to find the optimal path to avoid defeat.
	 * 
	 * @param board the current game board, before the computer has chosen its move
	 * @return the Pair representing its next move; in the format (row, column)
	 */
	public Pair getNextMove(Board board) {
		// minimax search from currentNode
		GameTreeNode root = buildGameTree(GameTreeNode.AdversaryType.MAX, board, getId());
		miniMaxSearch(root);
		
		GameTreeNode maxChild = null;
		int maxScore = Integer.MIN_VALUE;
		for (GameTreeNode child : root.getChildren()) {
			if (child.getRank() > maxScore) {
				maxScore = child.getRank();
				maxChild = child;
			}
		}
		
		return maxChild.getMove();
	}

	/**
	 * Descends the min-max game tree rooted at current.
	 * Searches for an optimal path;
	 * i.e., one that maximizes its score (from the perspective of a MAX node),
	 * or one that minimizes its score (from the perpsective of a MIN node).
	 * 
	 * @param current root of the min-max game tree
	 * @return the integer representation of the score from current's perspective
	 */
	private int miniMaxSearch(GameTreeNode current) {
		GameState state = current.getState();
		if (state.terminal()) {
			int score = state.utility(this);
			current.setRank(score);
			return score;
		}
		
		int optimalResult;
		if (current.getAdversaryType() == GameTreeNode.AdversaryType.MAX) {
			optimalResult = Integer.MIN_VALUE;
			for (GameTreeNode child : current.getChildren()) {
				int v = miniMaxSearch(child);
				if (v > optimalResult) {
					current.setRank(current.getRank() + child.getRank());	
				}
				optimalResult = Math.max(optimalResult, v);
			}
			for (GameTreeNode child : current.getChildren()) {
				int maxScore = Math.max(current.getRank(), child.getRank());
				current.setRank(maxScore);
			}
		}
		else {
			optimalResult = Integer.MAX_VALUE;
			for (GameTreeNode child : current.getChildren()) {
				int v = miniMaxSearch(child);
				if (v < optimalResult) {
					current.setRank(current.getRank() + child.getRank());	
				}
				optimalResult = Math.min(optimalResult, v);
			}
			for (GameTreeNode child : current.getChildren()) {
				int minScore = Math.min(current.getRank(), child.getRank());
				current.setRank(minScore);
			}
		}

		return optimalResult;
	}
	
	/**
	 * Recursively builds a game tree, starting with currentBoard, and player's turn.
	 * Each node's child is its competitor;
	 * i.e., a MAX node of player "O" will have MIN node children with each possible next move of O.
	 * Stops adding children when a terminal state of the board is reached.
	 * 
	 * @param adversaryType the type of the game node (MAX or MIN)
	 * @param currentBoard the current game board to determine a move off of
	 * @param player top-level player, whose current turn it is
	 * @return the root of the game tree
	 */
	private GameTreeNode buildGameTree(GameTreeNode.AdversaryType adversaryType, Board currentBoard, String player) {
		GameTreeNode node = new GameTreeNode(currentBoard, adversaryType);
		
		if (!currentBoard.terminal()) {
			Set<Pair> remainingLocations = generateRemainingLocations(currentBoard);
			
			for (Pair pair : remainingLocations) {
				int row = pair.getFirst();
				int col = pair.getSecond();
				Board copy = new Board(currentBoard);
				copy.place(row, col, player);
				String otherPlayer = Board.getOtherPlayer(player);
				GameTreeNode.AdversaryType otherType = GameTreeNode.reverseAdversaryType(adversaryType);
				GameTreeNode child = buildGameTree(otherType, copy, otherPlayer);
				child.setMove(row, col);
				node.addChild(child);
			}
		}
		
		return node;
	}
}
