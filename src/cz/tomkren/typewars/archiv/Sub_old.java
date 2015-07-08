package cz.tomkren.typewars.archiv;


import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.Log;
import cz.tomkren.helpers.Paar;
import cz.tomkren.typewars.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Deprecated
public class Sub_old {

    private Map<Integer,Type> table;
    private boolean fail;


    public Sub_old() {
        table = new TreeMap<>();
        fail = false;
    }

    public void setFail() {
        fail = true;
    }

    public boolean isFail() {
        return fail;
    }

    public static Sub_old unify(Type x, Type y) {
        return unify(x, y, new Sub_old());
    }

    private void put( int varId, Type t ) {
        table.put(varId, t);
        Log.it("xx"+varId);
    }


    public Type apply(Type input) {

        //todo tady je asi uplne zbytecny mit to jako mapu, když to pak aplikuju
        //todo takle postupně, ale aspon teda je to seřazený ale to je asi overkill
        //todo pak nak oddementnět dyštak

        Type output = input;

        for(Map.Entry<Integer,Type> e : table.entrySet()) {
            int varId = e.getKey();
            Type type = e.getValue();
            output = output.applyMiniSub(varId, type);
        }

        return output;
    }

    @Override
    public String toString() {

        if (isFail()) {
            return "fail";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        for(Map.Entry<Integer,Type> entry : table.entrySet()) {
            int id = entry.getKey();
            Type type = entry.getValue();

            sb.append(" ").append('x').append(id).append(" = ").append(type).append(" ,");
        }

        if (!table.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");

        return sb.toString();
    }

    // -------------------------------------------------------------

    // pokus 2 předělám podle my old haskell code

    public static Sub_old mgu(Type type1, Type type2) {

        Sub_old ret = new Sub_old();

        List<Paar<Integer,Type>> sub = new ArrayList<>();

        List<Paar<Type,Type>> agenda = new ArrayList<>();

        agenda.add( new Paar<>(type1,type2) );

        while (!agenda.isEmpty() && !ret.isFail()) {
            Paar<Type,Type> p = agenda.remove(agenda.size()-1);
            mgu_process( p._1(), p._2(), agenda, sub, ret );
        }

        if (!ret.isFail()) {

            for (Paar<Integer,Type> p : sub) {
                ret.table.put( p._1(), p._2() );
            }

        }

        return ret;
    }

    private static void mgu_process(Type t1, Type t2, List<Paar<Type,Type>> agenda, List<Paar<Integer,Type>> sub ,Sub_old ret) {

        if (t1 == t2 || areSameVars(t1,t2)) {return;}

        if (t1 instanceof TypeSym && t2 instanceof TypeSym) {
            if (!t1.toString().equals(t2.toString())) {ret.setFail();}
            return;
        }

        if (t1 instanceof TypeVar) {mgu_process_TypeVar((TypeVar)t1, t2, agenda, sub, ret); return;}
        if (t2 instanceof TypeVar) {mgu_process_TypeVar((TypeVar)t2, t1, agenda, sub, ret); return;}

        if (t1 instanceof TypeTerm && t2 instanceof TypeTerm) {
            mgu_process_TypeTerm((TypeTerm)t1,(TypeTerm)t2,agenda,ret);
            return;
        }

        ret.setFail();
    }

    private static void mgu_process_TypeVar(TypeVar var, Type type, List<Paar<Type,Type>> agenda, List<Paar<Integer,Type>> sub , Sub_old ret) {


        if (occurCheck(var,type)) {
            Log.it("OCCUR");
            ret.setFail();
            return;
        }

        // update sub by var = type
        // update agenda by var = type
        // insert var = type to ret

        int varId = var.getId();

        int len = sub.size();
        for (int i = 0; i < len; i++) {
            Paar<Integer,Type> p = sub.get(i);
            sub.set(i, new Paar<>( p._1() , p._2().applyMiniSub(varId, type) ));
        }

        len = agenda.size();
        for (int i = 0; i < len; i++) {
            Paar<Type,Type> p = agenda.get(i);
            agenda.set(i, new Paar<>(p._1().applyMiniSub(varId, type), p._2().applyMiniSub(varId, type) ));
        }

        sub.add(new Paar<>(varId,type));

    }



    private static void mgu_process_TypeTerm(TypeTerm t1, TypeTerm t2, List<Paar<Type,Type>> agenda, Sub_old ret) {

        List<Type> args1 = t1.getArgs();
        List<Type> args2 = t2.getArgs();

        int len = args1.size();
        if (len != args2.size()) {ret.setFail(); return;}

        //agenda.add( new Paar<>(t1.getOp(),t2.getOp()) );

        for (int i = 0; i < len; i++) {
            agenda.add( new Paar<>(args1.get(i),args2.get(i)) );
        }
    }


    private static boolean areSameVars(Type t1, Type t2) {
        return t1 instanceof TypeVar && t2 instanceof TypeVar && ((TypeVar)t1).getId() == ((TypeVar)t2).getId();
    }


    // ------------------------------------------------------------


    private static Sub_old unify(Type x, Type y, Sub_old s) {

        if (s == null) {return null;}
        if (x == y) {return s;}

        if (x instanceof TypeVar && y instanceof TypeVar && ((TypeVar)x).getId() == ((TypeVar)y).getId()) {
            return s;
        }

        if (x instanceof TypeVar) {return unifyVar((TypeVar) x, y, s);}
        if (y instanceof TypeVar) {return unifyVar((TypeVar) y, x, s);}

        if (x instanceof TypeTerm && y instanceof TypeTerm) {
            TypeTerm xt = (TypeTerm) x;
            TypeTerm yt = (TypeTerm) y;

            //return unifyList( xt.getArgs(), yt.getArgs() , unify(xt.getOp(),yt.getOp(),s) );
            return unifyList( xt.getArgs(), yt.getArgs() , s );
        }

        if (x instanceof TypeSym && y instanceof TypeSym) {
            if (x.toString().equals(y.toString())) {
                return s;
            } else {
                //Log.it("--vnitrek "+x.toString()+" "+y.toString());
                return null;
            }
        }

        //Log.it("--ani-jedno");

        return null;
    }

    private static Sub_old unifyVar(TypeVar var, Type t, Sub_old s) {

        int varId = var.getId();
        Type val = s.table.get(varId);

        if (val != null) {
            return unify(val, t, s);
        }

        if (t instanceof TypeVar) {
            TypeVar tVar = (TypeVar) t;
            val = s.table.get(tVar.getId());

            if (val != null) {
                return unify(var, val, s);
            }

        }

        if (occurCheck(var,t)) {
            Log.it("OCCUR");
            return null;
        }

        s.put(varId, t);

        return s;
    }

    private static Sub_old unifyList(List<Type> xs, List<Type> ys, Sub_old s) {
        int len = xs.size();
        if (len != ys.size()) {return null;}

        for (int i = 0; i < len; i++) {
            Type x = xs.get(i);
            Type y = ys.get(i);

            s = unify(x,y,s);

            if (s == null) {
                //Log.it("--list");
                return null;
            }
        }
        return s;
    }


    private static boolean occurCheck(TypeVar var, Type t) {

        if (t instanceof TypeVar) {
            return ((TypeVar)t).getId() == var.getId();
        }

        if (t instanceof TypeSym) {
            return false;
        }

        if (t instanceof TypeTerm) {
            TypeTerm tt = (TypeTerm)t;

            //if (occurCheck(var,tt.getOp())) {return true;}

            for (Type arg : tt.getArgs()) {
                if (occurCheck(var,arg)) {return true;}
            }

            return false;
        }

        throw new Error("occurCheck : should be unreachable");
    }


    // --- tests ---

    public static void unifyTest(Checker ch, String typeStr1, String typeStr2, String result) {
        ch.it( Sub_old.unify(Types.parse(typeStr1), Types.parse(typeStr2)) , result );
    }

    public static void unifyTest(Checker ch, String typeStr1, String typeStr2) {
        ch.it( Sub_old.unify(Types.parse(typeStr1), Types.parse(typeStr2)) );
    }

    public static void oldTest_aimaUnify(Checker ch) {
        unifyTest(ch, "A x1 x2", "A x1 x2", "{}");
        unifyTest(ch, "A x1 x2", "A x1 x3", "{ x2 = x3 }" );
        unifyTest(ch, "A x1 x3", "A x1 x2", "{ x3 = x2 }" );
        unifyTest(ch, "A x1 x3", "B x1 x2", "null" );
        unifyTest(ch, "A x1", "B x1", "null" );
        unifyTest(ch, "A (B Int x3) x2", "A (B x1 x3) x2", "{ x1 = Int }");
        unifyTest(ch, "A (List x1 x3) x2", "A (List x1 x3) Book" ,"{ x2 = Book }" );
        unifyTest(ch, "List Str", "List x3" , "{ x3 = Str }" );
        unifyTest(ch, "A (List Str)", "A (List Str)" , "{}");
        unifyTest(ch, "A (List x3)", "A (List x3)" , "{}");
        unifyTest(ch, "-> (List Int Str) x2", "-> (List x1 x3) Book", "{ x1 = Int , x2 = Book , x3 = Str }");
        unifyTest(ch, "A (List Str)", "A (List x3)", "{ x3 = Str }");
        unifyTest(ch, "A (List Str)", "A (List Str)" , "{}" );
        unifyTest(ch, "(A B)", "(A x1)" , "{ x1 = B }");
        unifyTest(ch, "D (List Int (S  x66) ) x2", "D (List x1 (S (G x12 x15) ) ) (Book x1 x2) ", "null" ); //"{ x1 = Int , x2 = Book , x3 = Str }"
        // Už zde blbě ta stará !!!!!!!!§
        Log.it("\n// Už zde blbě ta stará !!!!!!!!\n");
        unifyTest(ch, "D (List Int (S  x66) ) x4", "D (List x1 (S (G x12 x15) ) ) (Book x1 x2) "); //, "{ x1 = Int , x4 = Book x1 x2 , x66 = G x12 x15 }" );

        unifyTest(ch, "D (List Int (S  x66        ) )  x4",
                "D (List x1  (S (G x12 x15) ) ) (Book x66 x2)");//todo, "{ x1 = Int , x4 = Book x1 x2 , x66 = G x12 x15 }" );

    }

}
