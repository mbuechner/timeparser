package de.ddb.labs.timeparser.data;

/** An element in a rule's input or output specification. Created by {@link PatternParser}. */
public class Token {

    public enum Type {
        GENERIC_VARIABLE, MONTH_REPLACEMENT_VARIABLE, WEEKDAY_REPLACEMENT_VARIABLE, TEXT
    }

    protected final String patternValue;
    protected final Type type;

    public Token(Type type, String patternValue) {
        this.patternValue = patternValue;
        this.type = type;
    }

    public String getPatternValue() {
        return this.patternValue;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        result = prime * result + ((this.patternValue == null) ? 0 : this.patternValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( !(obj instanceof Token) ) {
            return false;
        }
        Token other = (Token) obj;
        if ( this.type != other.type ) {
            return false;
        }
        if ( this.patternValue == null ) {
            if ( other.patternValue != null ) {
                return false;
            }
        } else if ( !this.patternValue.equals(other.patternValue) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Token[" + this.type + ", pattern=" + this.patternValue + "]";
    }

}
