package mbi.mock;

import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

public class DeBruijnEdgeFactory implements EdgeFactory<String, String> {

	private DirectedMultigraph<String, String> graph;

	public void setGraph(DirectedMultigraph<String, String> graph) {
		this.graph = graph;
	}

	@Override
	public String createEdge(String v1, String v2) {
		if (v1.substring(1).equals(v2.substring(0, v2.length() - 1))) {
			String suffix = "";
			if (graph.containsEdge(v1, v2)) {
				Set<String> fromV1 = graph.outgoingEdgesOf(v1);
				Set<String> intoV2 = graph.incomingEdgesOf(v2);
				int nextNo = 1;
				for (String fv1 : fromV1) {
					if (intoV2.contains(fv1)) {
						++nextNo;
					}
				}
				suffix = "(" + nextNo + ")";
			}
			return v1.substring(0,1) + v2.substring(1, v2.length()) + suffix;
		} else {
			return null;
		}
	}

}
