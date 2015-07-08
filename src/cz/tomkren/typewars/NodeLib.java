package cz.tomkren.typewars;

import cz.tomkren.helpers.F;

import java.util.Arrays;
import java.util.List;

public class NodeLib {

    private final List<ProtoNode> lib;

    public NodeLib(List<ProtoNode> lib) {
        this.lib = F.sort(lib, node -> (double) node.getIns().size() );
    }

    public NodeLib(ProtoNode... lib) {
        this(Arrays.asList(lib));
    }

    public List<ProtoNode> getNodeList() {
        return lib;
    }
}
