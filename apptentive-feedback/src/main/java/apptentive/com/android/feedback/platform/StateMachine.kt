package apptentive.com.android.feedback.platform

internal val defaultHandler = { _: SDKEvent -> }

/**
 * Represents a transition within a finite state machine (FSM).
 *
 * @param event The name of the event that triggers the transition.
 * @param next SDKState The type representing the state to transition to.
 * @param handler An optional handler to be executed when this transition occurs.
 */
internal class Transition(val event: String, val next: SDKState, val handler: (SDKEvent) -> Unit)

/**
 * A DSL (Domain-Specific Language) builder class for defining rules within a finite state machine (FSM).
 *
 * @param state The enum type representing the states of the FSM.
 */
internal open class StateRuleDSL(private var state: SDKState) {
    private var initHandler = defaultHandler
    private var transitions: List<Transition> = listOf()

    /**
     * Sets an init handler to be executed when entering the associated state.
     *
     * @param block A lambda expression representing the enter handler.
     */
    fun initState(block: (Any) -> Unit) { initHandler = block }

    /**
     * Adds a transition to the associated state.
     *
     * @param transition The [Transition] to be added.
     */
    private fun addTransition(transition: Transition) {
        transitions = transitions.filter { tr -> tr.event != transition.event } + listOf(transition)
    }

    /**
     * Adds a transition for a specific event to another state within the FSM.
     *
     * @param event The name of the event that triggers the transition.
     * @param next The next state to transition to.
     * @param handler An optional lambda expression representing an event handler for the transition.
     *                Default is an empty handler.
     */
    fun transition(event: String, next: SDKState, handler: (SDKEvent) -> Unit = defaultHandler) {
        addTransition(Transition(event, next, handler))
    }

    /**
     * Creates a [StateRule] instance based on the defined rules in this DSL.
     *
     * @return A [StateRule] instance representing the rules for the associated state.
     */
    fun rule() = StateRule(state, initHandler, transitions)
}

/**
 * Represents a rule associated with a specific state in a finite state machine (FSM).
 *
 * @param state The state to which this rule applies.
 * @param initHandler An optional handler to be executed when entering this state.
 * @param transitions A list of transitions defined for this state.
 */
internal data class StateRule(
    val state: SDKState,
    val initHandler: (SDKEvent) -> Unit,
    val transitions: List<Transition>
)

/**
 * An interface for defining rules and transitions within a finite state machine (FSM).
 *
 */
internal interface StateMachineDSL {
    fun onState(state: SDKState, block: StateRuleDSL.() -> Unit)
}

/**
 * Represents a finite state machine (FSM) for managing state transitions and rules.
 *
 * @property initialState The initial state of the state machine.
 * @property initializer An optional initializer lambda that allows defining rules using a DSL.
 */
internal open class StateMachine(
    private val initialState: SDKState,
    private val initializer: StateMachineDSL.() -> Unit = { }
) : StateMachineDSL {

    // current state of the state machine
    var state: SDKState = initialState
        protected set

    // list of rules for the state machine.
    // Each rule corresponds to a state and includes enter/exit handlers and transitions
    private val rules: MutableList<StateRule> = mutableListOf()
    // current rule associated with the current state
    private var currentRule: StateRule? = null

    // allows the rules for the state machine using a DSL construct.
    init { this.initializer() }

    // finds the rule for the given state
    private fun findRule(s: SDKState) = rules.find {
        r ->
        r.state == s
    }

    /**
     * Adds a rule for a specific state to the state machine.
     * Ensures that there is only one rule per state
     *
     * @param rule The [StateRule] to be added.
     */
    private fun addRule(rule: StateRule) {
        rules.removeAll { r -> r.state == rule.state }
        rules.add(rule)
    }

    // for test only
    fun reset() {
        state = initialState
        currentRule = null
    }

    /**
     * Processes an incoming event and manages state transitions based on the event.
     *
     * @param event The incoming event of type [Any].
     *
     */
    open fun onEvent(event: SDKEvent) {
        val rule = currentRule ?: findRule(state)
        if (rule != null) {
            val transition = rule.transitions.firstOrNull { tr -> tr.event == event.name }
            if (transition != null) {
                transition.handler(event)
                state = transition.next
                val newRule = findRule(state)
                if (newRule != null) {
                    newRule.initHandler(event)
                    currentRule = newRule
                }
            }
        }
    }

    /**
     * Defines rules for a specific state using a DSL-like construct.
     * [StateRuleDSL] is created with the provided state and the block is executed on it.
     * The resulting rule is then added to the rules list using the `addRule` method
     *
     * @param state The state of type [SDKState] for which rules are being defined.
     * @param block A lambda expression that allows for defining rules for the given state.
     */
    override fun onState(state: SDKState, block: StateRuleDSL.() -> Unit) {
        val dsl = StateRuleDSL(state)
        dsl.block()
        addRule(dsl.rule())
    }
}
