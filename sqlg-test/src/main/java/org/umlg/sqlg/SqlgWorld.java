package org.umlg.sqlg;

import io.cucumber.java.Scenario;
import org.apache.tinkerpop.gremlin.features.World;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.umlg.sqlg.structure.SqlgGraph;

import java.io.File;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData;

public abstract class SqlgWorld implements World {
    private final SqlgGraph modern;
    private final SqlgGraph classic;
    private final SqlgGraph sink;
    private final SqlgGraph grateful;
    private final SqlgGraph empty;

    public SqlgWorld(final SqlgGraph modern, final SqlgGraph classic, SqlgGraph sink, SqlgGraph grateful, SqlgGraph empty) {
        this.modern = modern;
        this.classic = classic;
        this.sink = sink;
        this.grateful = grateful;
        this.empty = empty;

    }

    @Override
    public GraphTraversalSource getGraphTraversalSource(final GraphData graphData) {
        if (null == graphData)
            return empty.traversal();
        else if (graphData == GraphData.CLASSIC)
            return classic.traversal();
        else if (graphData == GraphData.CREW)
            throw new UnsupportedOperationException("The Crew dataset is not supported by Neo4j because it doesn't support mult/meta-properties");
        else if (graphData == GraphData.MODERN)
            return modern.traversal();
        else if (graphData == GraphData.SINK)
            return sink.traversal();
        else if (graphData == GraphData.GRATEFUL)
            return grateful.traversal();
        else
            throw new UnsupportedOperationException("GraphData not supported: " + graphData.name());
    }

    @Override
    public void beforeEachScenario(final Scenario scenario) {
        cleanEmpty();
    }

    @Override
    public String changePathToDataFile(final String pathToFileFromGremlin) {
        return ".." + File.separator + pathToFileFromGremlin;
    }

    @Override
    public String convertIdToScript(final Object id, final Class<? extends Element> type) {
        return "\"" + id.toString() + "\"";
    }


    private void cleanEmpty() {
        empty.tx().commit();
        classic.tx().commit();
        modern.tx().commit();
        sink.tx().commit();
        grateful.tx().commit();
        final GraphTraversalSource g = empty.traversal();
        g.V().drop().iterate();
        empty.tx().commit();
    }

}
