package mage.cards.p;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.PartnerWithAbility;
import mage.cards.*;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.watchers.Watcher;

import java.util.*;

/**
 * @author TheElk801
 */
public final class PakoArcaneRetriever extends CardImpl {

    public PakoArcaneRetriever(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}{G}");

        this.addSuperType(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.HOUND);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Partner with Haldan, Avid Arcanist
        this.addAbility(new PartnerWithAbility("Haldan, Avid Arcanist"));

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // Whenever Pako, Arcane Retriever attacks, exile the top card of each player's library and put a fetch counter on each of them. Put a +1/+1 counter on Pako for each noncreature card exiled this way.
        this.addAbility(new AttacksTriggeredAbility(
                new PakoArcaneRetrieverEffect(), false
        ), new PakoArcaneRetrieverWatcher());
    }

    private PakoArcaneRetriever(final PakoArcaneRetriever card) {
        super(card);
    }

    @Override
    public PakoArcaneRetriever copy() {
        return new PakoArcaneRetriever(this);
    }

    public static boolean checkWatcher(UUID playerId, Card card, Game game) {
        PakoArcaneRetrieverWatcher watcher = game.getState().getWatcher(PakoArcaneRetrieverWatcher.class);
        return watcher != null && watcher.checkCard(playerId, card, game);
    }
}

class PakoArcaneRetrieverEffect extends OneShotEffect {

    PakoArcaneRetrieverEffect() {
        super(Outcome.Benefit);
        staticText = "exile the top card of each player's library and put a fetch counter on each of them. " +
                "Put a +1/+1 counter on {this} for each noncreature card exiled this way.";
    }

    private PakoArcaneRetrieverEffect(final PakoArcaneRetrieverEffect effect) {
        super(effect);
    }

    @Override
    public PakoArcaneRetrieverEffect copy() {
        return new PakoArcaneRetrieverEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards cards = new CardsImpl();
        game.getState()
                .getPlayersInRange(controller.getId(), game)
                .stream()
                .map(game::getPlayer)
                .map(Player::getLibrary)
                .map(library -> library.getFromTop(game))
                .forEach(cards::add);
        controller.moveCards(cards, Zone.EXILED, source, game);
        cards.removeIf(cardId -> game.getState().getZone(cardId) != Zone.EXILED);
        int counters = cards.count(StaticFilters.FILTER_CARD_NON_CREATURE, game);
        if (cards.isEmpty()) {
            return true;
        }
        cards.getCards(game).stream().forEach(card -> card.addCounters(CounterType.FETCH.createInstance(), source, game));
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null || counters == 0) {
            return true;
        }
        return permanent.addCounters(CounterType.P1P1.createInstance(counters), source, game);
    }
}

class PakoArcaneRetrieverWatcher extends Watcher {

    private final Map<UUID, Set<MageObjectReference>> playerMap = new HashMap();

    PakoArcaneRetrieverWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
    }

    void addCard(UUID playerId, Card card, Game game) {
        playerMap.computeIfAbsent(playerId, u -> new HashSet()).add(new MageObjectReference(card, game));
    }

    boolean checkCard(UUID playerId, Card card, Game game) {
        return card != null && playerMap
                .computeIfAbsent(playerId, u -> new HashSet<MageObjectReference>())
                .stream()
                .anyMatch(mageObjectReference -> mageObjectReference.refersTo(card.getId(), game));
    }
}
