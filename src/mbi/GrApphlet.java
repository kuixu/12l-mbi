package mbi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JApplet;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;

public class GrApphlet extends JApplet {

	private static final long serialVersionUID = 1L;
	private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
	private static final Dimension DEFAULT_SIZE = new Dimension(1200, 900);

	private DeBruijnGraph graph;
	
	
	public DeBruijnGraph getGraph() {
		return graph;
	}

	public void setGraph(DeBruijnGraph graph) {
		this.graph = graph;
	}

	public void init() {
	
		JGraphModelAdapter<String, String> jGraphAdapter = new JGraphModelAdapter<String, String>(
				graph);
		JGraph jGraph = new JGraph(jGraphAdapter);
		adjustDisplaySettings(jGraph);
		getContentPane().add(jGraph);
		setSize(DEFAULT_SIZE);
		distributeVertices(jGraphAdapter, graph);
		
	}

	private void adjustDisplaySettings(JGraph jg) {
		jg.setPreferredSize(DEFAULT_SIZE);

		Color c = DEFAULT_BG_COLOR;
		String colorStr = null;

		try {
			colorStr = getParameter("bgcolor");
		} catch (Exception e) {
		}

		if (colorStr != null) {
			c = Color.decode(colorStr);
		}

		jg.setBackground(c);
	}

	private void positionVertexAt(Object vertex, int x, int y,
			JGraphModelAdapter<String, String> m_jgAdapter) {
		DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
		Map attr = cell.getAttributes();
		Rectangle2D b = GraphConstants.getBounds(attr);

		GraphConstants.setBounds(attr, new Rectangle(x, y, (int) b.getWidth(),
				(int) b.getHeight()));

		Map cellAttr = new HashMap();
		cellAttr.put(cell, attr);
		m_jgAdapter.edit(cellAttr, null, null, null);
	}

	private void distributeVertices(JGraphModelAdapter<String, String> ga,
			Graph<String, String> g) {
		int vertices = g.vertexSet().size();
		double step = 2 * Math.PI / vertices;
		double counter = 0;
		double stdSpan = 400;
		for (String vertex : g.vertexSet()) {
			positionVertexAt(vertex, DEFAULT_SIZE.width / 2
					+ (int) (stdSpan * Math.cos(counter)), DEFAULT_SIZE.height
					/ 2 + (int) (stdSpan * Math.sin(counter)), ga);
			counter += step;
		}
	}
}
