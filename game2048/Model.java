package game2048;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Objects;
import java.util.Observable;


/** The state of a game of 2048.
 * @author P. N. Hilfinger */
class Model extends Observable {

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to _board[c][r].  Be careful! This is not the usual 2D matrix
     * numbering, where rows are numbered from the top, and the row
     * number is the *first* index. Rather it works like (x, y) coordinates.
     */

    /** Largest piece value. */
    static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    Model(int size) {
        _board = new Tile[size][size];
        _score = _maxScore = 0;
        _gameOver = false;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there. */
    Tile tile(int col, int row) {
        return _board[col][row];
    }

    /** Return the number of squares on one side of the board. */
    int size() {
        return _board.length;
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    boolean gameOver() {
        return _gameOver;
    }

    /** Return the current score. */
    int score() {
        return _score;
    }

    /** Return the current maximum game score (updated at end of game). */
    int maxScore() {
        return _maxScore;
    }

    /** Clear the board to empty and reset the score. */
    void clear() {
        _score = 0;
        _gameOver = false;
        for (Tile[] column : _board) {
            Arrays.fill(column, null);
        }
        setChanged();
    }

    /** Add TILE to the board.  There must be no Tile currently at the
     *  same position. */
    void addTile(Tile tile) {
        assert _board[tile.col()][tile.row()] == null;
        _board[tile.col()][tile.row()] = tile;
        checkGameOver();
        setChanged();
    }

    /** Use setVtile(C, R, SIDE, TILE2, merge = false) if TILE1 is not
     * null and TILE2 is null. Return true iff this changes the board. */
    boolean ifnotnullnull(Tile tile1, Tile tile2, Side side, int c, int r) {
        if (tile1 != null && tile2 == null) {
            setVtile(c, r, side, tile1, false);
            tile1 = vtile(c, r - 1, side);
            if (tile1 == null) {
                return true;
            }
        }
        return false;
    }

    /** Use setVtile(C, R, SIDE, TILE2, merge = false) if TILE1 is not null
     * and TILE2 is not null. Return true iff this changes the board. */
    boolean ifbothnotnull(Tile tile1, Tile tile2, Side side, int c, int r) {
        if (tile1 != null && tile2 != null) {
            setVtile(c, r, side, tile1, true);
            tile1 = vtile(c, r - 1, side);
            if (tile1 == null) {
                return true;
            }
        }
        return false;
    }

    /** Use setVtile(C, R, SIDE, TILE2, merge = false) if TILE1 is not null.
     *  Return true iff this changes the board. */
    boolean ifnotnull(Tile tile1, Tile tile2, Side side, int c, int r) {
        if (tile1 != null) {
            setVtile(c, r, side, tile1, false);
            tile1 = vtile(c, r - 1, side);
            if (tile1 == null) {
                return true;
            }
        }
        return false;
    }



    /** Tilt the board toward SIDE. Return true iff this changes the board. */
    boolean tilt(Side side) {
        boolean changed;
        boolean trymerge;
        changed = false;
        int itercol = 0;
        int iter;
        while (itercol < 4) {
            Tile tile3 = vtile(itercol, 3, side);
            Tile tile2 = vtile(itercol, 2, side);
            Tile tile1 = vtile(itercol, 1, side);
            Tile tile0 = vtile(itercol, 0, side);
            iter = 0;
            while (iter < 3) {
                if (ifnotnullnull(tile2, tile3, side, itercol, 3)) {
                    tile3 = vtile(itercol, 3, side);
                    tile2 = vtile(itercol, 2, side);
                    changed = true;
                }
                if (ifnotnullnull(tile1, tile2, side, itercol, 2)) {
                    tile2 = vtile(itercol, 2, side);
                    tile1 = vtile(itercol, 1, side);
                    changed = true;
                }
                if (ifnotnullnull(tile0, tile1, side, itercol, 1)) {
                    tile1 = vtile(itercol, 1, side);
                    tile0 = vtile(itercol, 0, side);
                    changed = true;
                }
                iter++;
            }
            if (ifbothnotnull(tile2, tile3, side, itercol, 3)) {
                tile3 = vtile(itercol, 3, side);
                tile2 = vtile(itercol, 2, side);
                changed = true;
            }
            if (ifbothnotnull(tile1, tile2, side, itercol, 2)) {
                tile2 = vtile(itercol, 2, side);
                tile1 = vtile(itercol, 1, side);
                changed = true;
            }
            if (ifbothnotnull(tile0, tile1, side, itercol, 1)) {
                tile1 = vtile(itercol, 1, side);
                tile0 = vtile(itercol, 0, side);
                changed = true;
            }
            ifnotnull(tile2, tile3, side, itercol, 3);
            ifnotnull(tile1, tile2, side, itercol, 2);
            ifnotnull(tile0, tile1, side, itercol, 1);
            itercol++;
        }
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** Return the current Tile at (COL, ROW), when sitting with the board
     *  oriented so that SIDE is at the top (farthest) from you. */
    private Tile vtile(int col, int row, Side side) {
        return _board[side.col(col, row, size())][side.row(col, row, size())];
    }

    /** Move TILE to (C, R), merging (if MERGE is true) with any tile
     *  already there, where (COL, ROW) is as seen when sitting with
     *  the board oriented so that SIDE is at the top (farthest)
     *  from you. */
    private void setVtile(int c, int r, Side side, Tile tile, boolean merge) {
        int pcol = side.col(c, r, size()),
            prow = side.row(c, r, size());
        if (tile.col() == pcol && tile.row() == prow) {
            return;
        }
        Tile tile1 = vtile(c, r, side);

        if (tile1 == null) {
            _board[pcol][prow] = tile.move(pcol, prow);
            _board[tile.col()][tile.row()] = null;
        }
        if (merge && Objects.equals(tile.value(), tile1.value())) {
            _board[pcol][prow] = tile.merge(pcol, prow, tile1);
            _board[tile.col()][tile.row()] = null;
            _score   = _score + _board[pcol][prow].value();
        }
    }

    /** Deternmine whether game is over by considering potential
     * merge and nulls on each SIDE and update _gameOver and
     * _maxScore accordingly. Returns a boolean. */
    private boolean checkGameOverhelper(Side side) {
        boolean isgameover = true;
        int itercol;

        itercol = 3;
        while (itercol != -1) {
            Tile t3 = vtile(itercol, 3, side);
            Tile t2 = vtile(itercol, 2, side);
            Tile t1 = vtile(itercol, 1, side);
            Tile t0 = vtile(itercol, 0, side);

            if (t3 != null && t3.value() == MAX_PIECE
                    || t2 != null && t2.value() == MAX_PIECE) {
                isgameover = true;
                return isgameover;
            }
            if (t1 != null && t1.value() == MAX_PIECE
                    || t0 != null && t0.value() == MAX_PIECE) {
                isgameover = true;
                return isgameover;
            }

            if (t2 != null && t3 != null) {
                if (t2.value() == t3.value()) {
                    isgameover = false;
                }
            }
            if (t1 != null && t2 != null) {
                if (t1.value() == t2.value()) {
                    isgameover = false;
                }
            }
            if (t0 != null && t1 != null) {
                if (t0.value() == t1.value()) {
                    isgameover = false;
                }
            }
            if (t0 == null || t1 == null || t2 == null || t3 == null) {
                isgameover = false;

            }
            itercol--;
        }
        return isgameover;
    }

    /** Deternmine whether game is over by using
     * checkGameOverhelper(SIDE) to update _gameOver and
     * _maxScore accordingly. */
    private void checkGameOver() {
        _gameOver = true;
        if (!checkGameOverhelper(Side.NORTH)) {
            _gameOver = false;
        }
        if (!checkGameOverhelper(Side.SOUTH)) {
            _gameOver = false;
        }
        if (!checkGameOverhelper(Side.EAST)) {
            _gameOver = false;
        }
        if (!checkGameOverhelper(Side.WEST)) {
            _gameOver = false;
        }
        if (_gameOver == true) {
            if (_score > _maxScore) {
                _maxScore = _score;
            }
        }
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        out.format("] %d (max: %d)", score(), maxScore());
        return out.toString();
    }

    /** Current contents of the board. */
    private Tile[][] _board;
    /** Current score. */
    private int _score;
    /** Maximum score so far.  Updated when game ends. */
    private int _maxScore;
    /** True iff game is ended. */
    private boolean _gameOver;

}
