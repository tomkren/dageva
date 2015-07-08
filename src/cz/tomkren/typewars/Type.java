package cz.tomkren.typewars;


import cz.tomkren.helpers.AB;

import java.util.Set;

public interface Type {

    public Type applyMiniSub(int varId, Type type);
    public Type applySub(Sub sub);

    public AB<Type,Integer> freshenVars(int startVarId, Sub newVars);

    public void getVarIds(Set<Integer> ret);

}
