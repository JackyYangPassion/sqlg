package org.umlg.sqlg.structure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.umlg.sqlg.structure.topology.Topology;

public record Multiplicity(long lower, long upper) {

    public Multiplicity {
        if (lower < -1) {
            throw new IllegalArgumentException("Multiplicity.lower must be >= -1");
        }
        if (upper < -1) {
            throw new IllegalArgumentException("Multiplicity.upper must be >= -1");
        }
    }

    public static Multiplicity from(long lower, long higher) {
        return new Multiplicity(lower, higher);
    }

    public Multiplicity() {
        this(0, 1);
    }

    public boolean isMany() {
        return upper > 1 || upper == -1;
    }

    public boolean isRequired() {
        return lower > 0;
    }

    public boolean hasLimits() {
        return lower > 0 || upper != -1;
    }

    public String toCheckConstraint(String column) {
        Preconditions.checkState(hasLimits());
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (lower > 0) {
            sb.append("(CARDINALITY(").append(column).append(") >= ").append(lower).append(")");
        }
        if (upper != -1) {
            if (lower > 0) {
                sb.append(" AND ");
            }
            sb.append("(CARDINALITY(").append(column).append(") <= ").append(upper).append(")");
        }
        sb.append(")");
        return sb.toString();
    }

    public ObjectNode toNotifyJson() {
        ObjectNode multiplicityObjectNode = new ObjectNode(Topology.OBJECT_MAPPER.getNodeFactory());
        multiplicityObjectNode.put("lower", lower);
        multiplicityObjectNode.put("upper", upper);
        return multiplicityObjectNode;
    }

    public static Multiplicity fromNotifyJson(JsonNode jsonNode) {
        return Multiplicity.from(jsonNode.get("lower").asLong(), jsonNode.get("upper").asLong());
    }


    @Override
    public String toString() {
        return "[" + lower + ", " + upper + "]";
    }
}
