package cz.tomkren.typewars;


import cz.tomkren.helpers.AB;

import java.util.Set;

public class TypeSym implements Type {

    private final String sym;

    public TypeSym(String sym) {
        this.sym = sym;
    }

    @Override
    public Type applyMiniSub(int varId, Type type) {
        return this;
    }

    @Override
    public Type applySub(Sub sub) {
        return this;
    }

    @Override
    public AB<Type, Integer> freshenVars(int startVarId, Sub newVars) {
        return new AB<>(this,startVarId);
    }

    @Override
    public void getVarIds(Set<Integer> ret) {}

    public String toString() {
        return sym;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeSym typeSym = (TypeSym) o;

        if (sym != null ? !sym.equals(typeSym.sym) : typeSym.sym != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sym != null ? sym.hashCode() : 0;
    }

}
