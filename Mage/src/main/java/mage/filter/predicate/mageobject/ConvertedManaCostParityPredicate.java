package mage.filter.predicate.mageobject;

import mage.MageObject;
import mage.filter.predicate.Predicate;
import mage.game.Game;

/**
 * @author TheElk801
 */
public enum ConvertedManaCostParityPredicate implements Predicate<MageObject> {
    EVEN(0),
    ODD(1);
    private final int parity;

    ConvertedManaCostParityPredicate(int parity) {
        this.parity = parity;
    }

    @Override
    public boolean apply(MageObject input, Game game) {
        return input.getConvertedManaCost() % 2 == parity;
    }

    @Override
    public String toString() {
        return "ConvertedManaCostParity" + super.toString();
    }}
