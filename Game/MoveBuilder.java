package Game;

import Validation.MoveValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;

public class MoveBuilder{
    public MoveBuilder() {
    }

    private static void addMoveIfValid(ArrayList<Move> moves,
                           Character[] pieceNames,
                           Character[] boardNames,
                           String[] squareNames){
        Move move = new Move(null, null, pieceNames, boardNames, squareNames);
        if (MoveValidator.validMove(move)){
            moves.add(move);
        }
    }

    /**
     * Take a source square and generate all (even some nonsensical/invalid) moves from there.
     * @param sourceSquare The source to move from.
     * @return All (even some nonsensical/invalid) moves that piece could do from there.
     */
    public static Move[] setSourceSquare(Square sourceSquare){
        Piece sourcePiece = sourceSquare.board.getPiece(sourceSquare);
        if (sourcePiece == null) return new Move[0];
        ArrayList<Move> moves = new ArrayList<>(10);  // 10 is arbitrary
/*        ArrayList<Character> pieceNames = new ArrayList<>(3);
        ArrayList<Character> boardNames = new ArrayList<>(3);
        ArrayList<String> squareNames = new ArrayList<>(3);*/
        Board sourceBoard = sourceSquare.board;
        Square[] canGoTo = sourceBoard.canGoTo(sourcePiece);

        switch (sourcePiece.value){  // special cases
            case KING:
                //castle
                addMoveIfValid(
                        moves,
                        new Character[]{'K'},
                        new Character[0],
                        new String[0]);
                addMoveIfValid(
                        moves,
                        new Character[]{'Q'},
                        new Character[0],
                        new String[0]);
                break;
            case PAWN:
                Square[] attacks = sourceBoard.attacksSquares(sourcePiece);
                for (Square square:
                     attacks) {
                    addMoveIfValid(
                            moves,
                            new Character[0],
                            new Character[]{sourceBoard.name, 'A'},
                            new String[]{sourceBoard.getSquareName(sourceSquare),
                                sourceBoard.getSquareName(square)});
                    addMoveIfValid(
                            moves,
                            new Character[0],
                            new Character[]{sourceBoard.name, 'B'},
                            new String[]{sourceBoard.getSquareName(sourceSquare),
                                    sourceBoard.getSquareName(square)});
                }
                break;
        }

        for (Square square:
             canGoTo) {
            Piece destinationPiece = sourceBoard.getPiece(square);
            if (destinationPiece == null){  // translate
                addMoveIfValid(
                        moves,
                        new Character[]{sourcePiece.value.name},
                        new Character[]{sourceBoard.name, 'A'},
                        new String[]{sourceBoard.getSquareName(sourceSquare),
                                sourceBoard.getSquareName(square)}
                );
                addMoveIfValid(
                        moves,
                        new Character[]{sourcePiece.value.name},
                        new Character[]{sourceBoard.name, 'B'},
                        new String[]{sourceBoard.getSquareName(sourceSquare),
                                     sourceBoard.getSquareName(square)}
                );
                addMoveIfValid(
                        moves,
                        new Character[]{sourcePiece.value.name},
                        new Character[]{sourceBoard.name, 'C'},
                        new String[]{sourceBoard.getSquareName(sourceSquare),
                                     sourceBoard.getSquareName(square)});
            } else{
                if(destinationPiece.getColor() == sourcePiece.getColor()) continue;
                if(sourceBoard.name == 'C'){  // steal
                    addMoveIfValid(
                            moves,
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{'C', 'A', 'B'},
                            new String[]{sourceBoard.getSquareName(sourceSquare),
                                    sourceBoard.getSquareName(square)});
                    addMoveIfValid(
                            moves,
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{'C', 'B', 'A'},
                            new String[]{sourceBoard.getSquareName(sourceSquare),
                                    sourceBoard.getSquareName(square)});
                } else{  // capture
                    addMoveIfValid(
                            moves,
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{sourceBoard.name, 'A'},
                            new String[]{sourceBoard.getSquareName(sourceSquare),
                                    sourceBoard.getSquareName(square)});
                    addMoveIfValid(
                            moves,
                            new Character[]{sourcePiece.value.name, destinationPiece.value.name},
                            new Character[]{sourceBoard.name, 'B'},
                            new String[]{sourceBoard.getSquareName(sourceSquare),
                                    sourceBoard.getSquareName(square)});
                }
            }
        }
        //swap
        moves.addAll(Arrays.asList(MoveValidator.validSwaps(sourceSquare)));
        // null moves
        addMoveIfValid(
                moves,
                new Character[]{sourcePiece.value.name},
                new Character[]{sourceBoard.name, 'A'},
                new String[]{sourceBoard.getSquareName(sourceSquare),
                             sourceBoard.getSquareName(sourceSquare)}
        );
        addMoveIfValid(
                moves,
                new Character[]{sourcePiece.value.name},
                new Character[]{sourceBoard.name, 'B'},
                new String[]{sourceBoard.getSquareName(sourceSquare),
                             sourceBoard.getSquareName(sourceSquare)}
        );
        return moves.toArray(new Move[moves.size()]);
    }
    /**
     * Reduce the move array to moves that have a certain destination square
     */
    public static Move[] setDestinationSquare(Move[] moves,
                                              Square sourceSquare, Square destinationSquare){
        return Arrays.stream(moves).filter(move -> hasDestination(move, sourceSquare,
                destinationSquare)).toArray(size -> new Move[size]);
    }

    private static boolean hasDestination(Move move, Square sourceSquare, Square destinationSquare){
        switch (MoveType.of(move)) {
            case CASTLE:
                int[] distance = Board.calculateDistance(sourceSquare, destinationSquare);
                if (move.pieceNames[0] == 'K' && distance[0] == +2 && distance[1] == 0) return true;
                if (move.pieceNames[0] == 'Q' && distance[0] == -2 && distance[1] == 0) return true;
                break;
            case SWAP2:
            case SWAP3:
                if (sourceSquare == destinationSquare) return true;
                break;
            case CAPTURE:
            case TRANSLATE:
            case PROMOTION:
            case EN_PASSANT:
                if (move.squareNames[1].equals(
                        destinationSquare.board.getSquareName(destinationSquare)) &&
                    move.boardNames[0].equals(
                        move.boardNames[1])) return true;
        }
        return false;
    }
}
