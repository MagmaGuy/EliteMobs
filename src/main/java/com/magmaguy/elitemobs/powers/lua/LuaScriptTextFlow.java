package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.shaded.luaj.vm2.Lua;
import com.magmaguy.shaded.luaj.vm2.LuaString;
import com.magmaguy.shaded.luaj.vm2.ast.*;

import java.util.*;
import java.util.function.Consumer;

/**
 * Finds the string literals of a resolved Lua chunk whose value is shown as text.
 * <p>
 * A literal counts when it reaches the text argument of a text call directly, through {@code ..}, as a value of
 * {@code and}/{@code or}, or through a constant local, as {@link LuaScriptText} always did. It also counts when it
 * gets there through a local, a parameter of a local function ({@code say(c, "line")}), a table element or field
 * ({@code LINES[i]}, {@code CFG.PREFIX}, {@code POWERS[id].name}), an {@code ipairs} or {@code pairs} loop variable or
 * the result of a local function ({@code pick(LINES)}).
 * <p>
 * Translating a literal that is also used as a key or compared would change what the script does, so text only flows
 * through a variable, table part or function result when every use of it is shown as text. The exceptions are uses
 * that cannot tell a translation apart: a length, {@code not}, a comparison with {@code nil}, a bare condition and
 * the condition side of {@code and}, also on a part of a table ({@code if lines.again}). A function that only returns
 * an element of one parameter is followed per call, so {@code pick} can also pick block materials elsewhere. Each pass
 * blocks whatever the last pass found unsafe, until nothing changes.
 */
final class LuaScriptTextFlow {

    private final Map<String, Integer> textArguments;
    private final Chunk chunk;

    private final Map<Variable, List<Exp>> definitions = new IdentityHashMap<>();
    private final Map<Variable, List<Element>> elements = new IdentityHashMap<>();
    private final Map<Variable, FuncBody> localFunctions = new IdentityHashMap<>();
    private final Map<FuncBody, List<Exp>> returns = new IdentityHashMap<>();
    private final Map<Variable, List<Write>> tableWrites = new IdentityHashMap<>();
    private final Map<Variable, List<Exp.NameExp>> uses = new IdentityHashMap<>();
    private final Map<Exp.NameExp, Access> useAccess = new IdentityHashMap<>();
    private final Set<Exp.NameExp> harmlessUses = identitySet();
    private final Set<Exp.NameExp> callUses = identitySet();
    private final Set<Exp.NameExp> statementCallUses = identitySet();

    private final Set<Requirement> blocked = new HashSet<>();

    // State of the current pass.
    private Set<Exp.Constant> literals;
    private Set<Exp.NameExp> reached;
    private Set<Requirement> traversed;
    private Set<Requirement> expanding;
    private Set<Variable> shownLocals;

    LuaScriptTextFlow(Chunk chunk, Map<String, Integer> textArguments) {
        this.chunk = chunk;
        this.textArguments = textArguments;
        chunk.accept(new FunctionCollector());
        chunk.accept(new DefinitionCollector());
    }

    Collection<Exp.Constant> literals() {
        while (true) {
            runPass();
            Set<Requirement> unsafe = new HashSet<>();
            for (Requirement requirement : traversed)
                if (!isSafe(requirement)) unsafe.add(requirement);
            if (!blocked.addAll(unsafe)) return literals;
        }
    }

    private void runPass() {
        literals = identitySet();
        reached = identitySet();
        traversed = new HashSet<>();
        expanding = new HashSet<>();
        shownLocals = Collections.newSetFromMap(new IdentityHashMap<>());
        chunk.accept(new Visitor() {
            @Override
            public void visit(Exp.MethodCall call) {
                Integer count = textArguments.get(call.name);
                if (count != null && call.args.exps != null)
                    for (int i = 0; i < Math.min(count, call.args.exps.size()); i++)
                        text((Exp) call.args.exps.get(i));
                super.visit(call);
            }
        });
    }

    /** Collects the literals this expression can show. */
    private void text(Exp exp) {
        if (exp instanceof Exp.Constant constant) {
            if (constant.value instanceof LuaString) literals.add(constant);
        } else if (exp instanceof Exp.ParensExp parens) {
            text(parens.exp);
        } else if (exp instanceof Exp.BinopExp binop) {
            // The left of "and" is a condition, whose literals are compared rather than shown.
            if (binop.op == Lua.OP_CONCAT || binop.op == Lua.OP_OR) text(binop.lhs);
            if (binop.op == Lua.OP_CONCAT || binop.op == Lua.OP_OR || binop.op == Lua.OP_AND) text(binop.rhs);
        } else if (exp instanceof Exp.NameExp name) {
            Variable variable = name.name.variable;
            if (variable == null) return;
            if (variable.isConstant()) {
                // A local only ever assigned one literal: its initial value is the text.
                Exp initial = firstDefinition(variable);
                if (initial instanceof Exp.Constant constant && constant.value instanceof LuaString)
                    literals.add(constant);
                return;
            }
            if (!variable.isLocal()) return;
            through(name, variable, Access.VALUE, () -> {
                if (!shownLocals.add(variable)) return;
                for (Exp definition : definitions.getOrDefault(variable, List.of())) text(definition);
                for (Element element : elements.getOrDefault(variable, List.of()))
                    valuesAt(element.table(), element.access(), this::text);
            });
        } else if (exp instanceof Exp.FieldExp field) {
            valuesAt(field.lhs, Access.field(field.name.name), this::text);
        } else if (exp instanceof Exp.IndexExp index) {
            valuesAt(index.lhs, Access.forKey(index.exp), this::text);
        } else if (exp instanceof Exp.FuncCall call && !(exp instanceof Exp.MethodCall)) {
            FuncBody body = calledLocalFunction(call);
            if (body == null) return;
            through((Exp.NameExp) call.lhs, ((Exp.NameExp) call.lhs).name.variable, Access.RESULT,
                    () -> returns.getOrDefault(body, List.of()).forEach(this::text));
            Exp selected = selectedArgument(call, body);
            if (selected != null) valuesAt(selected, Access.ANY, this::text);
        }
    }

    /** Passes each value the tables this expression can hold store at the access to the consumer. */
    private void valuesAt(Exp table, Access access, Consumer<Exp> onValue) {
        if (table instanceof Exp.ParensExp parens) {
            valuesAt(parens.exp, access, onValue);
        } else if (table instanceof TableConstructor constructor) {
            if (constructor.fields == null) return;
            int position = 0;
            for (Object element : constructor.fields) {
                TableField field = (TableField) element;
                if (field.name == null && field.index == null) position++;
                if (access.matches(field, position)) onValue.accept(field.rhs);
            }
        } else if (table instanceof Exp.NameExp name) {
            Variable variable = name.name.variable;
            if (variable == null || !variable.isLocal() || variable.isConstant()) return;
            through(name, variable, access, () -> {
                for (Exp definition : definitions.getOrDefault(variable, List.of()))
                    valuesAt(definition, access, onValue);
                for (Element element : elements.getOrDefault(variable, List.of()))
                    valuesAt(element.table(), element.access(), inner -> valuesAt(inner, access, onValue));
                for (Write write : tableWrites.getOrDefault(variable, List.of()))
                    if (access.matches(write.key())) onValue.accept(write.value());
            });
        } else if (table instanceof Exp.FieldExp field) {
            valuesAt(field.lhs, Access.field(field.name.name), inner -> valuesAt(inner, access, onValue));
        } else if (table instanceof Exp.IndexExp index) {
            valuesAt(index.lhs, Access.forKey(index.exp), inner -> valuesAt(inner, access, onValue));
        } else if (table instanceof Exp.BinopExp binop) {
            if (binop.op == Lua.OP_OR) valuesAt(binop.lhs, access, onValue);
            if (binop.op == Lua.OP_OR || binop.op == Lua.OP_AND) valuesAt(binop.rhs, access, onValue);
        } else if (table instanceof Exp.FuncCall call && !(table instanceof Exp.MethodCall)) {
            FuncBody body = calledLocalFunction(call);
            if (body == null) return;
            through((Exp.NameExp) call.lhs, ((Exp.NameExp) call.lhs).name.variable, Access.RESULT,
                    () -> returns.getOrDefault(body, List.of()).forEach(value -> valuesAt(value, access, onValue)));
            Exp selected = selectedArgument(call, body);
            if (selected != null) valuesAt(selected, Access.ANY, inner -> valuesAt(inner, access, onValue));
        }
    }

    /**
     * The argument of this call when the function only ever returns an element of that parameter and reads it nowhere
     * else, as {@code pick(list)} does, or null. That call then shows only its own argument's elements, whatever other
     * calls do with theirs.
     */
    private Exp selectedArgument(Exp.FuncCall call, FuncBody body) {
        List<Exp> values = returns.get(body);
        if (values == null || values.isEmpty() || call.args.exps == null) return null;
        Variable parameter = null;
        // Only this function's own returns: a closure returning an element could hand it to anyone.
        Set<Exp.NameExp> returnedReads = identitySet();
        for (Exp value : values) {
            if (!(value instanceof Exp.IndexExp index) || !(index.lhs instanceof Exp.NameExp name)) return null;
            if (parameter != null && name.name.variable != parameter) return null;
            parameter = name.name.variable;
            returnedReads.add(name);
        }
        if (parameter == null || parameter.hasassignments) return null;
        int position = -1;
        for (int i = 0; i < body.parlist.names.size(); i++)
            if (((Name) body.parlist.names.get(i)).variable == parameter) position = i;
        if (position < 0 || position >= call.args.exps.size()) return null;
        for (Exp.NameExp use : uses.getOrDefault(parameter, List.of()))
            if (!harmlessUses.contains(use) && !returnedReads.contains(use)) return null;
        return (Exp) call.args.exps.get(position);
    }

    private void through(Exp.NameExp use, Variable variable, Access access, Runnable expand) {
        Requirement requirement = new Requirement(variable, access);
        if (blocked.contains(requirement)) return;
        reached.add(use);
        traversed.add(requirement);
        if (!expanding.add(requirement)) return;
        try {
            expand.run();
        } finally {
            expanding.remove(requirement);
        }
    }

    /** Whether every use of the variable that could see this part of its value showed it as text. */
    private boolean isSafe(Requirement requirement) {
        for (Exp.NameExp use : uses.getOrDefault(requirement.variable(), List.of())) {
            if (harmlessUses.contains(use) || reached.contains(use)) continue;
            Access useKind = useAccess.get(use);
            boolean matters = switch (requirement.access().kind()) {
                case RESULT -> callUses.contains(use) ? !statementCallUses.contains(use) : true;
                case VALUE, ANY -> true;
                case POSITION -> useKind == null || useKind.kind() != Access.Kind.FIELD;
                case FIELD -> useKind == null || !(useKind.kind() == Access.Kind.FIELD
                        && !useKind.key().equals(requirement.access().key()));
            };
            if (matters) return false;
        }
        return true;
    }

    private FuncBody calledLocalFunction(Exp.FuncCall call) {
        if (!(call.lhs instanceof Exp.NameExp name) || name.name.variable == null) return null;
        return localFunctions.get(name.name.variable);
    }

    private Exp firstDefinition(Variable variable) {
        List<Exp> found = definitions.get(variable);
        return found == null || found.isEmpty() ? null : found.getFirst();
    }

    private void define(Variable variable, Exp value) {
        if (variable != null && value != null) definitions.computeIfAbsent(variable, ignored -> new ArrayList<>()).add(value);
    }

    private static <T> Set<T> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private static boolean isNil(Exp exp) {
        return exp instanceof Exp.Constant constant && constant.value.isnil();
    }

    /** Local functions that are never reassigned, and the first value of each of their return statements. */
    private final class FunctionCollector extends Visitor {
        private final Deque<FuncBody> bodies = new ArrayDeque<>();

        @Override
        public void visit(Stat.LocalFuncDef stat) {
            if (stat.name.variable != null && !stat.name.variable.hasassignments)
                localFunctions.put(stat.name.variable, stat.body);
            super.visit(stat);
        }

        @Override
        public void visit(Stat.LocalAssign stat) {
            if (stat.values != null)
                for (int i = 0; i < Math.min(stat.names.size(), stat.values.size()); i++) {
                    Variable variable = ((Name) stat.names.get(i)).variable;
                    if (stat.values.get(i) instanceof Exp.AnonFuncDef function && variable != null && !variable.hasassignments)
                        localFunctions.put(variable, function.body);
                }
            super.visit(stat);
        }

        @Override
        public void visit(FuncBody body) {
            bodies.push(body);
            super.visit(body);
            bodies.pop();
        }

        @Override
        public void visit(Stat.Return stat) {
            if (!bodies.isEmpty() && stat.values != null && !stat.values.isEmpty())
                returns.computeIfAbsent(bodies.peek(), ignored -> new ArrayList<>()).add((Exp) stat.values.getFirst());
            super.visit(stat);
        }
    }

    /** Where each local's values come from, and how each read of a local uses it. */
    private final class DefinitionCollector extends Visitor {
        private final Set<Exp.NameExp> assignmentTargets = identitySet();

        @Override
        public void visit(Stat.LocalAssign stat) {
            if (stat.values != null)
                for (int i = 0; i < Math.min(stat.names.size(), stat.values.size()); i++)
                    define(((Name) stat.names.get(i)).variable, (Exp) stat.values.get(i));
            super.visit(stat);
        }

        @Override
        public void visit(Stat.Assign stat) {
            for (int i = 0; i < stat.vars.size(); i++) {
                Exp target = (Exp) stat.vars.get(i);
                Exp value = stat.exps != null && i < stat.exps.size() ? (Exp) stat.exps.get(i) : null;
                if (target instanceof Exp.NameExp name) {
                    assignmentTargets.add(name);
                    if (name.name.variable != null && name.name.variable.isLocal()) define(name.name.variable, value);
                } else if (target instanceof Exp.FieldExp field && field.lhs instanceof Exp.NameExp table) {
                    write(table, Access.field(field.name.name), value);
                } else if (target instanceof Exp.IndexExp index && index.lhs instanceof Exp.NameExp table) {
                    write(table, Access.forKey(index.exp), value);
                }
            }
            super.visit(stat);
        }

        private void write(Exp.NameExp table, Access key, Exp value) {
            assignmentTargets.add(table);
            Variable variable = table.name.variable;
            if (variable != null && variable.isLocal() && value != null)
                tableWrites.computeIfAbsent(variable, ignored -> new ArrayList<>()).add(new Write(key, value));
        }

        /** {@code for _, v in ipairs(t)} and {@code pairs(t)} give v the elements of t. */
        @Override
        public void visit(Stat.GenericFor stat) {
            if (stat.names.size() >= 2 && stat.exps.size() == 1 && stat.exps.getFirst() instanceof Exp.FuncCall call
                    && !(call instanceof Exp.MethodCall) && call.lhs instanceof Exp.NameExp iterator
                    && iterator.name.variable != null && !iterator.name.variable.isLocal()
                    && call.args.exps != null && call.args.exps.size() == 1) {
                Access access = switch (iterator.name.name) {
                    case "ipairs" -> Access.POSITION;
                    case "pairs" -> Access.ANY;
                    default -> null;
                };
                Variable value = ((Name) stat.names.get(1)).variable;
                Exp table = (Exp) call.args.exps.getFirst();
                if (access != null && value != null) {
                    elements.computeIfAbsent(value, ignored -> new ArrayList<>()).add(new Element(table, access));
                    if (table instanceof Exp.NameExp name) useAccess.put(name, access);
                }
            }
            super.visit(stat);
        }

        @Override
        public void visit(Exp.FuncCall call) {
            FuncBody body = calledLocalFunction(call);
            if (body != null && call.args.exps != null) {
                List<?> parameters = body.parlist.names;
                for (int i = 0; i < Math.min(parameters.size(), call.args.exps.size()); i++)
                    define(((Name) parameters.get(i)).variable, (Exp) call.args.exps.get(i));
            }
            if (call.lhs instanceof Exp.NameExp name) callUses.add(name);
            super.visit(call);
        }

        @Override
        public void visit(Stat.FuncCallStat stat) {
            if (stat.funccall.lhs instanceof Exp.NameExp name && !(stat.funccall instanceof Exp.MethodCall))
                statementCallUses.add(name);
            super.visit(stat);
        }

        @Override
        public void visit(Exp.FieldExp field) {
            if (field.lhs instanceof Exp.NameExp name) useAccess.put(name, Access.field(field.name.name));
            super.visit(field);
        }

        @Override
        public void visit(Exp.IndexExp index) {
            if (index.lhs instanceof Exp.NameExp name) useAccess.put(name, Access.forKey(index.exp));
            super.visit(index);
        }

        @Override
        public void visit(Exp.UnopExp unop) {
            if (unop.op == Lua.OP_LEN || unop.op == Lua.OP_NOT) harmless(unop.rhs);
            super.visit(unop);
        }

        @Override
        public void visit(Exp.BinopExp binop) {
            if (binop.op == Lua.OP_AND) harmless(binop.lhs);
            if ((binop.op == Lua.OP_EQ || binop.op == Lua.OP_NEQ) && (isNil(binop.lhs) || isNil(binop.rhs))) {
                harmless(binop.lhs);
                harmless(binop.rhs);
            }
            super.visit(binop);
        }

        @Override
        public void visit(Stat.IfThenElse stat) {
            harmless(stat.ifexp);
            if (stat.elseifexps != null) for (Object condition : stat.elseifexps) harmless((Exp) condition);
            super.visit(stat);
        }

        @Override
        public void visit(Stat.WhileDo stat) {
            harmless(stat.exp);
            super.visit(stat);
        }

        @Override
        public void visit(Stat.RepeatUntil stat) {
            harmless(stat.exp);
            super.visit(stat);
        }

        @Override
        public void visit(Exp.NameExp name) {
            Variable variable = name.name.variable;
            if (variable != null && variable.isLocal() && !assignmentTargets.contains(name))
                uses.computeIfAbsent(variable, ignored -> new ArrayList<>()).add(name);
            super.visit(name);
        }

        /** Marks the local this expression reads; testing a part of a table only reads that part as a condition. */
        private void harmless(Exp exp) {
            while (true) {
                if (exp instanceof Exp.ParensExp parens) exp = parens.exp;
                else if (exp instanceof Exp.FieldExp field) exp = field.lhs;
                else if (exp instanceof Exp.IndexExp index) exp = index.lhs;
                else break;
            }
            if (exp instanceof Exp.NameExp name) harmlessUses.add(name);
        }
    }

    /**
     * A part of a value: the value itself, a function's result, list elements, one named field or, read through a key
     * only known at run time, any entry.
     */
    private record Access(Kind kind, String key) {
        static final Access VALUE = new Access(Kind.VALUE, null);
        static final Access RESULT = new Access(Kind.RESULT, null);
        static final Access POSITION = new Access(Kind.POSITION, null);
        static final Access ANY = new Access(Kind.ANY, null);

        static Access field(String name) {
            return new Access(Kind.FIELD, name);
        }

        /** {@code t["name"]} reads a field, {@code t[1]} reads by position and {@code t[k]} can read any entry. */
        static Access forKey(Exp key) {
            if (!(key instanceof Exp.Constant constant)) return ANY;
            return constant.value instanceof LuaString ? field(constant.value.tojstring()) : POSITION;
        }

        boolean matches(TableField field, int position) {
            if (kind == Kind.ANY) return true;
            if (kind == Kind.FIELD) return key.equals(field.name)
                    || field.index instanceof Exp.Constant constant && constant.value instanceof LuaString
                    && key.equals(constant.value.tojstring());
            return kind == Kind.POSITION && field.name == null && field.index == null && position > 0;
        }

        /**
         * Whether a value written at this key can be read here. A value written through a run-time key only counts for
         * a read that can see any entry, whose requirement then covers every other read of the table.
         */
        boolean matches(Access written) {
            return kind == Kind.ANY || written.equals(this);
        }

        enum Kind {VALUE, RESULT, POSITION, FIELD, ANY}
    }

    private record Requirement(Variable variable, Access access) {
        @Override
        public boolean equals(Object other) {
            return other instanceof Requirement requirement && requirement.variable == variable
                    && requirement.access.equals(access);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(variable) * 31 + access.hashCode();
        }
    }

    private record Write(Access key, Exp value) {
    }

    /** A loop variable's values: the entries of the table at the access. */
    private record Element(Exp table, Access access) {
    }
}
