package mage.cards.v;

import mage.MageInt;
import mage.abilities.TriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.keyword.CyclingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.token.HumanSoldierToken;
import mage.game.stack.StackAbility;
import mage.game.stack.StackObject;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ValiantRescuer extends CardImpl {

    public ValiantRescuer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SOLDIER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(1);

        // Whenever you cycle another card for the first time each turn, create a 1/1 white Human Soldier creature token.
        this.addAbility(new ValiantRescuerTriggeredAbility());

        // Cycling {2}
        this.addAbility(new CyclingAbility(new ManaCostsImpl("{2}")));
    }

    private ValiantRescuer(final ValiantRescuer card) {
        super(card);
    }

    @Override
    public ValiantRescuer copy() {
        return new ValiantRescuer(this);
    }
}

class ValiantRescuerTriggeredAbility extends TriggeredAbilityImpl {

    ValiantRescuerTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CreateTokenEffect(new HumanSoldierToken()));
        this.addWatcher(new ValiantRescuerWatcher());
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ACTIVATED_ABILITY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        ValiantRescuerWatcher watcher = game.getState().getWatcher(ValiantRescuerWatcher.class);
        if (watcher == null
                || !watcher.checkSpell(event.getPlayerId(), event.getSourceId())
                || game.getState().getStack().isEmpty()
                || !event.getPlayerId().equals(this.getControllerId())
                || event.getSourceId().equals(this.getSourceId())) {
            return false;
        }
        StackObject item = game.getState().getStack().getFirst();
        return item instanceof StackAbility
                && item.getStackAbility() instanceof CyclingAbility;
    }

    @Override
    public TriggeredAbility copy() {
        return null;
    }
}

class ValiantRescuerWatcher extends Watcher {

    private final Map<UUID, Map<UUID, Integer>> playerMap = new HashMap();

    ValiantRescuerWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (game.getState().getStack().isEmpty()) {
            return;
        }
        StackObject item = game.getState().getStack().getFirst();
        if (item instanceof StackAbility
                && item.getStackAbility() instanceof CyclingAbility) {
            playerMap.computeIfAbsent(event.getPlayerId(), u -> new HashMap());
            playerMap.get(event.getPlayerId()).compute(event.getSourceId(), (u, i) -> i == null ? 1 : i + 1);
        }
    }

    @Override
    public void reset() {
        super.reset();
        playerMap.clear();
    }

    boolean checkSpell(UUID playerId, UUID cardId) {
        if (!playerMap.containsKey(playerId)) {
            return true;
        }
        Map<UUID, Integer> cardMap = playerMap.get(playerId);
        return cardMap.keySet().stream().filter(uuid -> !uuid.equals(cardId)).mapToInt(cardMap::get).sum() < 2;
    }
}
