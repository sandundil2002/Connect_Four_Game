package lk.ijse.dep.service;

import java.util.*;

public class AiPlayer extends Player {
    boolean trueOrFalse;
    Winner winner;
    Random random = new Random();

    public AiPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int col) {
        Mcts mcts = new Mcts(board.getBoardImpl());
        col = mcts.startMCTS();
        board.updateMove(col,Piece.GREEN);
        board.getBoardUI().update(col,false);
        Winner winner = board.findWinner();

        if (winner.getWinningPiece() != Piece.EMPTY){
            board.getBoardUI().notifyWinner(winner);
        } else if (!board.existLegalMoves()){
            board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
        }
    }

    static class Mcts {
        private final BoardImpl board;

        private final Piece AiID = Piece.GREEN;

        private final Piece HumanID = Piece.BLUE;

        public Mcts(BoardImpl board) {
            this.board = board;
        }

        public int startMCTS(){
            int count = 0;
            Node tree = new Node(board);

            while (count < 4000){
                count++;
                Node promisingNode = selectPromisingNode(tree);
                Node selected = promisingNode;
                if (selected.board.getStatus()){
                    selected = expandNodeAndReturnRandom(promisingNode);
                }
                Piece resultPiece = simulateLightPlayout(selected);
                backPropagation(resultPiece,selected);
            }
            Node best = tree.getChildWithMaxScore();
            return best.board.cols;
        }

        private void backPropagation(Piece resultPiece, Node selected) {
            Node node = selected;

            while (node != null){
                node.visits++;
                if (node.board.piece == resultPiece){
                    node.score++;
                }
                node = node.parent;
            }
        }

        private Piece simulateLightPlayout(Node promisingNode) {
            Node node = new Node(promisingNode.board);
            node.parent = promisingNode.parent;
            Winner winner = node.board.findWinner();

            if (winner.getWinningPiece() == Piece.BLUE){
                node.parent.score=Integer.MIN_VALUE;
                return node.board.findWinner().getWinningPiece();
            }

            while (node.board.getStatus()){
                BoardImpl nextMove = node.board.getRandomLeagalNextMove();
                Node child = new Node(nextMove);
                child.parent = node;
                node.addChild(child);
                node = child;
            }
            return node.board.findWinner().getWinningPiece();
        }

        private Node expandNodeAndReturnRandom(Node node) {
            Node result = node;
            BoardImpl board = node.board;
            List<BoardImpl> legalMoves = board.getAllLegalNextMoves();

            for (int i = 0; i < legalMoves.size(); i++) {
                BoardImpl move = legalMoves.get(i);
                Node child = new Node(move);
                child.parent = node;
                node.addChild(child);
                result = child;
            }
            int random = Board.RANDOM_GENERATOR.nextInt(node.children.size());
            return node.children.get(random);
        }

        private Node selectPromisingNode(Node tree) {
            Node node = tree;
            while (node.children.size() != 0){
                node = UCT.findBestNodeWithUCT(node);
            }
            return node;
        }
    }

    static class Node {
        public BoardImpl board;
        public int visits;
        public int score;
        List<Node> children = new ArrayList<>();
        Node parent = null;

        public Node(BoardImpl board) {
            this.board = board;
        }
        Node getChildWithMaxScore() {
            Node result = children.get(0);
            for (int i = 1; i < children.size(); i++) {
                if (children.get(i).score > result.score) {
                    result = children.get(i);
                }
            }
            return result;
        }
        void addChild(Node node) {
            children.add(node);
        }

    }

    static class UCT {
        public static double uctValue(
                int totalVisit, double nodeWinScore, int nodeVisit) {
            if (nodeVisit == 0) {
                return Integer.MAX_VALUE;
            }
            return ((double) nodeWinScore / (double) nodeVisit) + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
        }

        public static Node findBestNodeWithUCT(Node node) {
            int parentVisit = node.visits;
            return Collections.max(node.children, Comparator.comparing(c -> uctValue(parentVisit, c.score, c.visits)));
        }
    }
}