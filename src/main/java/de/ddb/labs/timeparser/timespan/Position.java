package de.ddb.labs.timeparser.timespan;

/**
 * A pos within an input string that is being parsed by {@link TimeSpanParser}. {@link InputStringReader} will update a {@link Position} once a requested
 * string can be accepted.
 */
class Position {
    private int pos = 0;

    public int get() {
        return this.pos;
    }

    public void move(int move) {
        this.pos += move;
    }

    public void update(Position position) {
        this.pos = position.pos;
    }

    public Position copy() {
        Position p = new Position();
        p.pos = this.pos;
        return p;
    }

    @Override
    public String toString() {
        return "Position[" + this.pos + "]";
    }
}
