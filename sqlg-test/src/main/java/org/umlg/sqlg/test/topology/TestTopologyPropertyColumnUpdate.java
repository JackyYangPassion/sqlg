package org.umlg.sqlg.test.topology;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.umlg.sqlg.structure.*;
import org.umlg.sqlg.structure.topology.PropertyColumn;
import org.umlg.sqlg.structure.topology.Topology;
import org.umlg.sqlg.structure.topology.VertexLabel;
import org.umlg.sqlg.test.BaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class TestTopologyPropertyColumnUpdate extends BaseTest {

    private final List<Triple<TopologyInf, TopologyInf, TopologyChangeAction>> topologyListenerTriple = new ArrayList<>();

    @Before
    public void before() throws Exception {
        super.before();
        this.topologyListenerTriple.clear();
    }

    @Test
    public void testPropertyUpdateMultiplicityFrom1to0() {
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("column1", PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1)));
                }});
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A");
            Assert.fail("NOT NULL should have prevented this");
        } catch (Exception e) {
            this.sqlgGraph.tx().rollback();
        }


        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("column1");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //Change the property from required to optional
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(0, 1)));
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A");
        } catch (Exception e) {
            Assert.fail("NOT NULL should have been dropped");
            this.sqlgGraph.tx().rollback();
        }
        this.sqlgGraph.tx().rollback();

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("column1");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(0, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().upper());

        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1)));
        List<Long> lowers = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<Long>values(Topology.SQLG_SCHEMA_PROPERTY_MULTIPLICITY_LOWER).toList();
        Assert.assertEquals(1, lowers.size());
        Assert.assertEquals(1L, lowers.get(0).longValue());
        this.sqlgGraph.tx().rollback();

        if (this.sqlgGraph.getSqlDialect().supportsTransactionalSchema()) {
            lowers = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<Long>values(Topology.SQLG_SCHEMA_PROPERTY_MULTIPLICITY_LOWER).toList();
            Assert.assertEquals(1, lowers.size());
            Assert.assertEquals(0L, lowers.get(0).longValue());
        }
    }

    @Test
    public void testPropertyUpdateMultiplicityFrom0to1() {
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("column1", PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(0, 1)));
                }});
        this.sqlgGraph.tx().commit();
        Vertex v = this.sqlgGraph.addVertex(T.label, "A");
        this.sqlgGraph.tx().rollback();

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("column1");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //Change the property from required to optional
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1)));
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A");
            Assert.fail("NOT NULL should have been dropped");
        } catch (Exception e) {
            this.sqlgGraph.tx().rollback();
        }
        this.sqlgGraph.tx().rollback();

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("column1");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().upper());

        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(0, 1)));
        List<Long> lowers = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<Long>values(Topology.SQLG_SCHEMA_PROPERTY_MULTIPLICITY_LOWER).toList();
        Assert.assertEquals(1, lowers.size());
        Assert.assertEquals(0L, lowers.get(0).longValue());
        this.sqlgGraph.tx().rollback();

        if (this.sqlgGraph.getSqlDialect().supportsTransactionalSchema()) {
            lowers = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<Long>values(Topology.SQLG_SCHEMA_PROPERTY_MULTIPLICITY_LOWER).toList();
            Assert.assertEquals(1, lowers.size());
            Assert.assertEquals(1L, lowers.get(0).longValue());
        }
    }

    @Test
    public void testPropertyUpdateDropCheckConstraint() {
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(0, 1), null, "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'a')"));
                }});
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", "a");
            Assert.fail("check constraint should have prevented this");
        } catch (Exception e) {
            this.sqlgGraph.tx().rollback();
        }

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //drop the check constraint
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(0, 1)));
        this.sqlgGraph.tx().commit();
        Vertex v = this.sqlgGraph.addVertex(T.label, "A", "name", "a");
        this.sqlgGraph.tx().commit();
        v.remove();
        this.sqlgGraph.tx().commit();

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(0, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().upper());

        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1), null, "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'a')"));
        List<String> checkConstraints = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_CHECK_CONSTRAINT).toList();
        Assert.assertEquals(1, checkConstraints.size());
        Assert.assertEquals("(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'a')", checkConstraints.get(0));
        this.sqlgGraph.tx().rollback();

        if (this.sqlgGraph.getSqlDialect().supportsTransactionalSchema()) {
            checkConstraints = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_CHECK_CONSTRAINT).toList();
            Assert.assertEquals(0, checkConstraints.size());
        }
    }

    @Test
    public void testPropertyUpdateAddCheckConstraint() {
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(0, 1)));
                }});
        this.sqlgGraph.tx().commit();

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //add the check constraint
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(0, 1), null, "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'a')"));
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", "a");
            Assert.fail("check constraint should have prevented this!!!");
        } catch (Exception e) {
            this.sqlgGraph.tx().commit();
        }

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(0, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().upper());

        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1)));
        List<String> checkConstraints = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_CHECK_CONSTRAINT).toList();
        Assert.assertEquals(0, checkConstraints.size());
        this.sqlgGraph.tx().rollback();

        if (this.sqlgGraph.getSqlDialect().supportsTransactionalSchema()) {
            checkConstraints = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_CHECK_CONSTRAINT).toList();
            Assert.assertEquals(1, checkConstraints.size());
            Assert.assertEquals("(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'a')", checkConstraints.get(0));
        }
    }

    @Test
    public void testPropertyUpdateChangeCheckConstraint() {
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(0, 1), null, "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'a')"));
                }});
        this.sqlgGraph.tx().commit();

        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", "a");
            Assert.fail("check constraint should have prevented this.");
        } catch (Exception e) {
            this.sqlgGraph.tx().rollback();
        }

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //add the check constraint
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(0, 1), null, "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'b')"));
        this.sqlgGraph.tx().commit();
        Vertex v = this.sqlgGraph.addVertex(T.label, "A", "name", "a");
        this.sqlgGraph.tx().commit();
        v.remove();
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", "b");
            Assert.fail("check constraint should have prevented this!!!");
        } catch (Exception e) {
            this.sqlgGraph.tx().commit();
        }

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(0, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().upper());

        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1), null, "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'a')"));

        List<String> checkConstraints = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_CHECK_CONSTRAINT).toList();
        Assert.assertEquals(1, checkConstraints.size());
        Assert.assertEquals("(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'a')", checkConstraints.get(0));
        this.sqlgGraph.tx().rollback();

        if (this.sqlgGraph.getSqlDialect().supportsTransactionalSchema()) {
            checkConstraints = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_CHECK_CONSTRAINT).toList();
            Assert.assertEquals(1, checkConstraints.size());
            Assert.assertEquals("(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " <> 'b')", checkConstraints.get(0));
        }
    }

    @Test
    public void testPropertyUpdateArrayMultiplicityAndCheckConstraintH2() {
        Assume.assumeTrue(isH2());
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.STRING_ARRAY, Multiplicity.of(2, 4), null,
                            "(ARRAY_CONTAINS (" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + ", 'a1'))")
                    );
                }});
        this.sqlgGraph.tx().commit();

        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", new String[]{"b1", "b2"});
            Assert.fail("check constraint should have prevented this.");
        } catch (Exception e) {
            this.sqlgGraph.tx().rollback();
        }

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //add the check constraint
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.STRING_ARRAY, Multiplicity.of(2, 4), null,
                "(ARRAY_CONTAINS (" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + ", 'b1'))")
        );
        this.sqlgGraph.tx().commit();
        Vertex v = this.sqlgGraph.addVertex(T.label, "A", "name", new String[]{"b1", "b2"});
        this.sqlgGraph.tx().commit();
        v.remove();
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", new String[]{"a1"});
            Assert.fail("check constraint should have prevented this!!!");
        } catch (Exception e) {
            this.sqlgGraph.tx().commit();
        }

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(2, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(4, column1.getPropertyDefinition().multiplicity().upper());
    }

    @Test
    public void testPropertyUpdateArrayMultiplicityAndCheckConstraintHsqldb() {
        Assume.assumeTrue(isHsqldb());
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.STRING_ARRAY, Multiplicity.of(2, 4), null,
                            "(POSITION_ARRAY ('{a1}' IN " + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + ") = -1)")
                    );
                }});
        this.sqlgGraph.tx().commit();

        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", new String[]{"b1", "b2"});
            Assert.fail("check constraint should have prevented this.");
        } catch (Exception e) {
            this.sqlgGraph.tx().rollback();
        }

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //add the check constraint
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.STRING_ARRAY, Multiplicity.of(2, 4), null,
                "(POSITION_ARRAY ('{b1}' IN " + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + ") != -1)")
        );
        this.sqlgGraph.tx().commit();
        Vertex v = this.sqlgGraph.addVertex(T.label, "A", "name", new String[]{"b1", "b2"});
        this.sqlgGraph.tx().commit();
        v.remove();
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", new String[]{"a1"});
            Assert.fail("check constraint should have prevented this!!!");
        } catch (Exception e) {
            this.sqlgGraph.tx().commit();
        }

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(2, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(4, column1.getPropertyDefinition().multiplicity().upper());
    }

    @Test
    public void testPropertyUpdateArrayMultiplicityAndCheckConstraint() {
        Assume.assumeTrue(isPostgres());
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.STRING_ARRAY, Multiplicity.of(2, 4), null, "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " @> '{a1}')"));
                }});
        this.sqlgGraph.tx().commit();

        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", new String[]{"b1", "b2"});
            Assert.fail("check constraint should have prevented this.");
        } catch (Exception e) {
            this.sqlgGraph.tx().rollback();
        }

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //add the check constraint
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.STRING_ARRAY, Multiplicity.of(2, 4), null, "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " @> '{b2}')"));
        this.sqlgGraph.tx().commit();
        Vertex v = this.sqlgGraph.addVertex(T.label, "A", "name", new String[]{"b1", "b2"});
        this.sqlgGraph.tx().commit();
        v.remove();
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A", "name", new String[]{"a1"});
            Assert.fail("check constraint should have prevented this!!!");
        } catch (Exception e) {
            this.sqlgGraph.tx().commit();
        }

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(2, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(4, column1.getPropertyDefinition().multiplicity().upper());

        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.STRING_ARRAY, Multiplicity.of(2, 4), null, "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " @> '{a1}')"));
        List<String> checkConstraints = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_CHECK_CONSTRAINT).toList();
        Assert.assertEquals(1, checkConstraints.size());
        Assert.assertEquals("(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " @> '{a1}')", checkConstraints.get(0));
        this.sqlgGraph.tx().rollback();

        if (this.sqlgGraph.getSqlDialect().supportsTransactionalSchema()) {
            checkConstraints = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_CHECK_CONSTRAINT).toList();
            Assert.assertEquals(1, checkConstraints.size());
            Assert.assertEquals("(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("name") + " @> '{b2}')", checkConstraints.get(0));
        }
    }

    @Test
    public void testPropertyUpdateDefaultLiteral() {
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1), "'a'"));
                }});
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.addVertex(T.label, "A");
        this.sqlgGraph.tx().commit();
        String name = this.sqlgGraph.traversal().V().hasLabel("A").tryNext().orElseThrow().value("name");
        Assert.assertEquals("a", name);
        this.sqlgGraph.traversal().V().drop().iterate();
        this.sqlgGraph.tx().commit();

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //add the check constraint
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1), "'b'"));
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.addVertex(T.label, "A");
        this.sqlgGraph.tx().commit();
        name = this.sqlgGraph.traversal().V().hasLabel("A").tryNext().orElseThrow().value("name");
        Assert.assertEquals("b", name);

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().upper());

        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1), "'a'"));
        List<String> defaultLiterals = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_DEFAULT_LITERAL).toList();
        Assert.assertEquals(1, defaultLiterals.size());
        this.sqlgGraph.tx().rollback();

        if (this.sqlgGraph.getSqlDialect().supportsTransactionalSchema()) {
            defaultLiterals = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_DEFAULT_LITERAL).toList();
            Assert.assertEquals(1, defaultLiterals.size());
            Assert.assertEquals("'b'", defaultLiterals.get(0));
        }
    }

    @Test
    public void testPropertyRemoveDefaultLiteral() {
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1), "'a'"));
                }});
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.addVertex(T.label, "A");
        this.sqlgGraph.tx().commit();
        String name = this.sqlgGraph.traversal().V().hasLabel("A").tryNext().orElseThrow().value("name");
        Assert.assertEquals("a", name);
        this.sqlgGraph.traversal().V().drop().iterate();
        this.sqlgGraph.tx().commit();

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //add the check constraint
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1)));
        this.sqlgGraph.tx().commit();
        try {
            this.sqlgGraph.addVertex(T.label, "A");
            Assert.fail("required column should have prevented this.");
        } catch (Exception e) {
            this.sqlgGraph.tx().rollback();

        }

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().upper());

        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1), "'a'"));
        List<String> defaultLiterals = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_DEFAULT_LITERAL).toList();
        Assert.assertEquals(1, defaultLiterals.size());
        this.sqlgGraph.tx().rollback();

        if (this.sqlgGraph.getSqlDialect().supportsTransactionalSchema()) {
            defaultLiterals = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_DEFAULT_LITERAL).toList();
            Assert.assertEquals(0, defaultLiterals.size());
        }
    }

    @Test
    public void testPropertyAddDefaultLiteral() {
        TestTopologyChangeListener.TopologyListenerTest topologyListenerTest = new TestTopologyChangeListener.TopologyListenerTest(topologyListenerTriple);
        this.sqlgGraph.getTopology().registerListener(topologyListenerTest);
        this.sqlgGraph.getTopology().getPublicSchema()
                .ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1)));
                }});
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.addVertex(T.label, "A", "name", "a");
        this.sqlgGraph.tx().commit();

        Optional<VertexLabel> aVertexLabelOptional = this.sqlgGraph.getTopology().getPublicSchema().getVertexLabel("A");
        Preconditions.checkState(aVertexLabelOptional.isPresent());
        VertexLabel aVertexLabel = aVertexLabelOptional.get();
        Optional<PropertyColumn> column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        PropertyColumn column1 = column1Optional.get();
        //add the check constraint
        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1), "'b'"));
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.addVertex(T.label, "A");
        this.sqlgGraph.tx().commit();

        List<Vertex> b = this.sqlgGraph.traversal().V().hasLabel("A").has("name", "b").toList();
        Assert.assertEquals(1, b.size());

        Assert.assertEquals(2, this.topologyListenerTriple.size());
        Assert.assertNotEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getMiddle());
        Assert.assertEquals(TopologyChangeAction.UPDATE, this.topologyListenerTriple.get(1).getRight());
        column1Optional = aVertexLabel.getProperty("name");
        Preconditions.checkState(column1Optional.isPresent());
        column1 = column1Optional.get();
        Assert.assertEquals(column1, this.topologyListenerTriple.get(1).getLeft());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().lower());
        Assert.assertEquals(1, column1.getPropertyDefinition().multiplicity().upper());

        column1.updatePropertyDefinition(PropertyDefinition.of(PropertyType.varChar(10), Multiplicity.of(1, 1)));
        List<String> defaultLiterals = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_DEFAULT_LITERAL).toList();
        Assert.assertEquals(0, defaultLiterals.size());
        this.sqlgGraph.tx().rollback();

        if (this.sqlgGraph.getSqlDialect().supportsTransactionalSchema()) {
            defaultLiterals = this.sqlgGraph.topology().V().hasLabel(Topology.SQLG_SCHEMA + "." + Topology.SQLG_SCHEMA_PROPERTY).<String>values(Topology.SQLG_SCHEMA_PROPERTY_DEFAULT_LITERAL).toList();
            Assert.assertEquals(1, defaultLiterals.size());
            Assert.assertEquals("'b'", defaultLiterals.get(0));
        }
    }

    /**
     * Works for HSQLDB
     */
    public List<String> checkConstraintName(SqlgGraph sqlgGraph, String schema, String table, String column) {
        List<String> result = new ArrayList<>();
        Connection conn = sqlgGraph.tx().getConnection();
        String sql = "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE\n" +
                "WHERE TABLE_SCHEMA = ? and TABLE_NAME = ? AND COLUMN_NAME = ?;";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, table);
            statement.setString(3, column);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
