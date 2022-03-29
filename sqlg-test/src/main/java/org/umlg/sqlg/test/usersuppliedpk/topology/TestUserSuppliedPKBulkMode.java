package org.umlg.sqlg.test.usersuppliedpk.topology;

import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.umlg.sqlg.structure.PropertyDefinition;
import org.umlg.sqlg.structure.PropertyType;
import org.umlg.sqlg.structure.SqlgVertex;
import org.umlg.sqlg.structure.topology.VertexLabel;
import org.umlg.sqlg.test.BaseTest;

import java.util.*;

/**
 * @author Pieter Martin (https://github.com/pietermartin)
 * Date: 2018/03/31
 */
public class TestUserSuppliedPKBulkMode extends BaseTest {

    @Test
    public void testBulkUpdateUser() {
        this.sqlgGraph.tx().normalBatchModeOn();
        this.sqlgGraph.addVertex(T.label, "A", "nameA", "a1", "nameB", "b1", "nameC", "c1", "other", "a");
        this.sqlgGraph.addVertex(T.label, "A", "nameA", "a2", "nameB", "b1", "nameC", "c1", "other", "b");
        this.sqlgGraph.tx().commit();

        this.sqlgGraph.tx().normalBatchModeOn();
        List<Vertex> vertices = this.sqlgGraph.traversal().V().hasLabel("A").toList();
        for (Vertex vertex : vertices) {
            vertex.property("other", "c");
        }
        this.sqlgGraph.tx().commit();
        vertices = this.sqlgGraph.traversal().V().hasLabel("A").toList();
        Assert.assertEquals(2, vertices.size());
        for (Vertex vertex : vertices) {
            Assert.assertEquals("c", vertex.value("other"));
        }
    }

    @Test
    public void testBulkUpdateUserSuppliedBulkMode() {
        this.sqlgGraph.getTopology().ensureVertexLabelExist(
                "A",
                new HashMap<>() {{
                    put("nameA", PropertyDefinition.of(PropertyType.STRING));
                    put("nameB", PropertyDefinition.of(PropertyType.STRING));
                    put("nameC", PropertyDefinition.of(PropertyType.STRING));
                    put("other", PropertyDefinition.of(PropertyType.STRING));
                }},
                ListOrderedSet.listOrderedSet(Arrays.asList("nameA", "nameB", "nameC"))
        );
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.tx().normalBatchModeOn();
        this.sqlgGraph.addVertex(T.label, "A", "nameA", "a1", "nameB", "b1", "nameC", "c1", "other", "a");
        this.sqlgGraph.addVertex(T.label, "A", "nameA", "a2", "nameB", "b1", "nameC", "c1", "other", "b");
        this.sqlgGraph.tx().commit();

        this.sqlgGraph.tx().normalBatchModeOn();
        List<Vertex> vertices = this.sqlgGraph.traversal().V().hasLabel("A").toList();
        for (Vertex vertex : vertices) {
            vertex.property("other", "c");
        }
        this.sqlgGraph.tx().commit();

        vertices = this.sqlgGraph.traversal().V().hasLabel("A").toList();
        Assert.assertEquals(2, vertices.size());
        for (Vertex vertex : vertices) {
            Assert.assertEquals("c", vertex.value("other"));
        }

    }

    @Test
    public void testBulkUpdateUserSuppliedBulkModeOnEdges() {
        VertexLabel aVertexLabel = this.sqlgGraph.getTopology().ensureVertexLabelExist("A");
        VertexLabel bVertexLabel = this.sqlgGraph.getTopology().ensureVertexLabelExist("B");
        aVertexLabel.ensureEdgeLabelExist(
                "ab",
                bVertexLabel,
                new HashMap<>() {{
                    put("nameA", PropertyDefinition.of(PropertyType.STRING));
                    put("nameB", PropertyDefinition.of(PropertyType.STRING));
                    put("nameC", PropertyDefinition.of(PropertyType.STRING));
                    put("other", PropertyDefinition.of(PropertyType.STRING));
                }},
                ListOrderedSet.listOrderedSet(Arrays.asList("nameA", "nameB", "nameC"))
        );
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.tx().normalBatchModeOn();
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A");
        Vertex a2 = this.sqlgGraph.addVertex(T.label, "A");
        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B");
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B");
        a1.addEdge("ab", b1, "nameA", "a1", "nameB", "b1", "nameC", "c1", "other", "o1");
        a1.addEdge("ab", b2, "nameA", "a2", "nameB", "b1", "nameC", "c1", "other", "o1");
        a2.addEdge("ab", b1, "nameA", "a3", "nameB", "b1", "nameC", "c1", "other", "o1");
        a2.addEdge("ab", b2, "nameA", "a4", "nameB", "b1", "nameC", "c1", "other", "o1");
        this.sqlgGraph.tx().commit();

        this.sqlgGraph.tx().normalBatchModeOn();
        List<Edge> edges = this.sqlgGraph.traversal().E().hasLabel("ab").toList();
        for (Edge edge : edges) {
            edge.property("other", "c");
        }
        this.sqlgGraph.tx().commit();

        edges = this.sqlgGraph.traversal().E().hasLabel("ab").toList();
        Assert.assertEquals(4, edges.size());
        for (Edge edge : edges) {
            Assert.assertEquals("c", edge.value("other"));
        }

    }

    @Test
    public void testVertexLabelUserSuppliedBulkMode() {
        //noinspection Duplicates
        this.sqlgGraph.getTopology().ensureVertexLabelExist(
                "A",
                new HashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.STRING));
                }},
                ListOrderedSet.listOrderedSet(Collections.singletonList("name"))
        );
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.tx().normalBatchModeOn();
        this.sqlgGraph.addVertex(T.label, "A", "name", "a1");
        this.sqlgGraph.addVertex(T.label, "A", "name", "a2");
        this.sqlgGraph.tx().commit();

        Assert.assertEquals(2, this.sqlgGraph.traversal().V().hasLabel("A").toList().size());
    }

    @Test
    public void testVertexAndEdgeLabelUserSuppliedBulkMode() {
        @SuppressWarnings("Duplicates")
        VertexLabel aVertexLabel = this.sqlgGraph.getTopology().ensureVertexLabelExist(
                "A",
                new HashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.STRING));
                }},
                ListOrderedSet.listOrderedSet(Collections.singletonList("name"))
        );
        VertexLabel bVertexLabel = this.sqlgGraph.getTopology().ensureVertexLabelExist(
                "B",
                new HashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.STRING));
                }},
                ListOrderedSet.listOrderedSet(Collections.singletonList("name"))
        );
        aVertexLabel.ensureEdgeLabelExist(
                "ab",
                bVertexLabel,
                new HashMap<>() {{
                    put("uid", PropertyDefinition.of(PropertyType.STRING));
                    put("country", PropertyDefinition.of(PropertyType.STRING));
                }},
                ListOrderedSet.listOrderedSet(Arrays.asList("uid", "country")));
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.tx().normalBatchModeOn();
        for (int i = 0; i < 100; i++) {
            Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "name", "a" + i);
            Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "name", "b" + i);
            a1.addEdge("ab", b1, "uid", UUID.randomUUID().toString(), "country", "SA");
        }
        this.sqlgGraph.tx().commit();

        Assert.assertEquals(100, this.sqlgGraph.traversal().V().hasLabel("A").out().toList().size());
    }

    @Test
    public void testVertexBatchStreamMode() {
        Assume.assumeTrue(this.sqlgGraph.getSqlDialect().supportsStreamingBatchMode());
        this.sqlgGraph.getTopology().ensureVertexLabelExist(
                "A",
                new HashMap<>() {{
                    put("name1", PropertyDefinition.of(PropertyType.STRING));
                    put("name2", PropertyDefinition.of(PropertyType.STRING));
                }},
                ListOrderedSet.listOrderedSet(Arrays.asList("name1", "name2"))
        );
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.tx().streamingBatchModeOn();
        for (int i = 0; i < 100; i++) {
            this.sqlgGraph.streamVertex(T.label, "A", "name1", "a" + i, "name2", "a" + i);
        }
        this.sqlgGraph.tx().commit();
        Assert.assertEquals(100, this.sqlgGraph.traversal().V().hasLabel("A").toList().size());
    }

    @Test
    public void testEdgeBatchStreamMode() {
        Assume.assumeTrue(this.sqlgGraph.getSqlDialect().supportsStreamingBatchMode());
        VertexLabel aVertexLabel = this.sqlgGraph.getTopology().ensureVertexLabelExist(
                "A",
                new HashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.STRING));
                }},
                ListOrderedSet.listOrderedSet(Collections.singletonList("name"))
        );
        VertexLabel bVertexLabel = this.sqlgGraph.getTopology().ensureVertexLabelExist(
                "B",
                new HashMap<>() {{
                    put("name", PropertyDefinition.of(PropertyType.STRING));
                }},
                ListOrderedSet.listOrderedSet(Collections.singletonList("name"))
        );
        aVertexLabel.ensureEdgeLabelExist("ab", bVertexLabel);
        this.sqlgGraph.tx().commit();
        SqlgVertex a1 = (SqlgVertex) this.sqlgGraph.addVertex(T.label, "A", "name", "a");
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.tx().normalBatchModeOn();
        Map<String, Vertex> cache = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            cache.put("b" + i,  this.sqlgGraph.addVertex(T.label, "B", "name", "b" + i));
        }
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.tx().streamingBatchModeOn();
        for (String key : cache.keySet()) {
            SqlgVertex sqlgVertex = (SqlgVertex) cache.get(key);
            a1.streamEdge("ab", sqlgVertex);
        }
        this.sqlgGraph.tx().commit();

        Assert.assertEquals(100, this.sqlgGraph.traversal().V().hasLabel("A").out().toList().size());
    }

}
