package cz.tomkren.typewars.archiv;


import cz.tomkren.helpers.F;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.reusable.Query;
import cz.tomkren.typewars.SmartSym;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class RootNode {

    private final SmartSym sym;
    private List<Profile> profiles;
    private BigInteger num;

    public BigInteger getNum() {
        return num;
    }

    public RootNode(SmartSym sym, Query dadQuery) {

        this.sym = sym;
        this.num = BigInteger.ZERO;
        this.profiles = new ArrayList<>();

        List<List<Integer>> allSimpleProfiles = possibleSimpleProfiles(dadQuery.getTreeSize(), sym.getArity());

        if (allSimpleProfiles.size() == 0) {
            return;
        }

        List<Type> argTypes = sym.getArgTypes();

        for (List<Integer> simpleProfile : allSimpleProfiles) {
            List<Query> sonQueries = F.zipWith(argTypes, simpleProfile, Query::new);
            profiles.add(new Profile(dadQuery, sonQueries));
        }

    }

    // TODO projít a znovu pochopit, pokud jde tak zjednodušit (možná by stačil jen ten else, ale nevim zatim)
    public static List<List<Integer>> possibleSimpleProfiles(int fatherSize, int numArgs) {

        int size = fatherSize - 1;
        List<List<Integer>> ret = new ArrayList<>();

        if (numArgs == 0 || size < numArgs) {
            return ret;
        }

        if (numArgs == 1) {
            ret.add(F.singleton(size));
        } else {

            int n = size - (numArgs - 1);
            for (int i = 1; i <= n; i++) {

                List<List<Integer>> subResults = possibleSimpleProfiles(size - i + 1, numArgs - 1);

                for (List<Integer> subResult : subResults) {
                    List<Integer> newResult = new ArrayList<>();
                    newResult.add(i);
                    newResult.addAll(subResult);
                    ret.add(newResult);
                }
            }

        }

        return ret;
    }

}

    /*

    public Type getRootType() {
        throw new TODO();
    }

    private RootNode(SmartSym sym, TypeNode parent, List<List<Integer>> allSimpleProfiles) {
        this.sym = sym;
        this.parent = parent;
        this.profiles = F.map(allSimpleProfiles, this::mkProfile);
    }

    private ProfileNode mkProfile(List<Integer> simpleProfile) {
        assert simpleProfile.size() == sym.getArgTypes().size();

        List<AB<Integer,Type>> signatures = F.zip(simpleProfile, sym.getArgTypes());
        return new ProfileNode(this, signatures);
    }*/





    //public int getArity() {return sym.getArity();}
    //public int getTreeSize() {return grandpa.getTreeSize();}

