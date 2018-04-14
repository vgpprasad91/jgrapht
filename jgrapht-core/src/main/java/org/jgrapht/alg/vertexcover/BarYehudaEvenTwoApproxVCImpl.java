/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.vertexcover;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

/**
 * Implementation of the 2-opt algorithm for a minimum weighted vertex cover by R. Bar-Yehuda and S.
 * Even. A linear time approximation algorithm for the weighted vertex cover problem. J. of
 * Algorithms 2:198-203, 1981. The solution is guaranteed to be within 2 times the optimum solution.
 * An easier-to-read version of this algorithm can be found here: <a href=
 * "https://www.cs.umd.edu/class/spring2011/cmsc651/vc.pdf">https://www.cs.umd.edu/class/spring2011/cmsc651/vc.pdf</a>
 *
 * Note: this class supports pseudo-graphs Runtime: O(|E|) This is a fast algorithm, guaranteed to
 * give a 2-approximation. A solution of higher quality (same approximation ratio) at the expensive
 * of a higher runtime can be obtained using {@link BarYehudaEvenTwoApproxVCImpl}.
 *
 *
 * TODO: Remove the UndirectedSubgraph dependency! Querying vertex degrees on these graphs is
 * actually slow! This does affect the runtime complexity. Better would be to just work on a clone
 * of the original graph!
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class BarYehudaEvenTwoApproxVCImpl<V, E>
    implements MinimumWeightedVertexCoverAlgorithm<V, E>, VertexCoverAlgorithm<V>
{

    private final Graph<V,E> graph;
    private final Map<V, Double> vertexWeightMap;

    /**
     * Temporary constructor to ensure one-version-backwards-compatibility
     * @deprecated this constructor will be removed in the next release
     */
    @Deprecated
    public BarYehudaEvenTwoApproxVCImpl(){
        graph=null;
        vertexWeightMap=null;
    }

    /**
     * Constructs a new BarYehudaEvenTwoApproxVCImpl instance where all vertices have uniform weights.
     * @param graph input graph
     */
    public BarYehudaEvenTwoApproxVCImpl(Graph<V,E> graph) {
        this.graph=GraphTests.requireUndirected(graph);
        this.vertexWeightMap = graph
                .vertexSet().stream().collect(Collectors.toMap(Function.identity(), vertex -> 1.0));
    }

    /**
     * Constructs a new BarYehudaEvenTwoApproxVCImpl instance
     * @param graph input graph
     * @param vertexWeightMap mapping of vertex weights
     */
    public BarYehudaEvenTwoApproxVCImpl(Graph<V,E> graph, Map<V, Double> vertexWeightMap) {
        this.graph=GraphTests.requireUndirected(graph);
        this.vertexWeightMap=Objects.requireNonNull(vertexWeightMap);
    }

    @Override
    public VertexCoverAlgorithm.VertexCover getVertexCover(){
        Set<V> cover = new LinkedHashSet<>();
        double weight = 0;
        Graph<V, E> copy = new AsSubgraph<>(graph, null, null);
        Map<V, Double> W = new HashMap<>();
        for (V v : graph.vertexSet())
            W.put(v, vertexWeightMap.get(v));

        // Main loop
        Set<E> edgeSet = copy.edgeSet();
        while (!edgeSet.isEmpty()) {
            // Pick arbitrary edge
            E e = edgeSet.iterator().next();
            V p = copy.getEdgeSource(e);
            V q = copy.getEdgeTarget(e);

            if (W.get(p) <= W.get(q)) {
                W.put(q, W.get(q) - W.get(p));
                cover.add(p);
                weight += vertexWeightMap.get(p);
                copy.removeVertex(p);
            } else {
                W.put(p, W.get(p) - W.get(q));
                cover.add(q);
                weight += vertexWeightMap.get(q);
                copy.removeVertex(q);
            }
        }
        return new VertexCoverAlgorithm.VertexCoverImpl<>(cover, weight);
    }

    /**
     *
     * @param graph the input graph
     * @param vertexWeightMap map containing non-negative weights for each vertex
     * @return vertex cover
     * @deprecated Replaced by {@link #getVertexCover()}
     */
    @Override
    @Deprecated
    public MinimumVertexCoverAlgorithm.VertexCover<V> getVertexCover(Graph<V, E> graph, Map<V, Double> vertexWeightMap)
    {
        GraphTests.requireUndirected(graph);

        Set<V> cover = new LinkedHashSet<>();
        double weight = 0;
        Graph<V, E> copy = new AsSubgraph<>(graph, null, null);
        Map<V, Double> W = new HashMap<>();
        for (V v : graph.vertexSet())
            W.put(v, vertexWeightMap.get(v));

        // Main loop
        Set<E> edgeSet = copy.edgeSet();
        while (!edgeSet.isEmpty()) {
            // Pick arbitrary edge
            E e = edgeSet.iterator().next();
            V p = copy.getEdgeSource(e);
            V q = copy.getEdgeTarget(e);

            if (W.get(p) <= W.get(q)) {
                W.put(q, W.get(q) - W.get(p));
                cover.add(p);
                weight += vertexWeightMap.get(p);
                copy.removeVertex(p);
            } else {
                W.put(p, W.get(p) - W.get(q));
                cover.add(q);
                weight += vertexWeightMap.get(q);
                copy.removeVertex(q);
            }
        }

        return new MinimumVertexCoverAlgorithm.VertexCoverImpl<>(cover, weight);
    }
}
