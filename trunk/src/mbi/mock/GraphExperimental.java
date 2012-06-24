package mbi.mock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JApplet;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;

import sun.security.provider.certpath.Vertex;

public class GraphExperimental extends JApplet {
	
    private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 1200, 900 );
	
	/**
	 * 
	 * @param length the length of desired sequence
	 * @return
	 */
	
	public static String generateSequence(int length){
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<length; ++i){
			double rand = Math.random();
			if(rand>0.75){
				builder.append("C");
			}else if(rand>0.5){
				builder.append("G");
			}else if(rand>0.25){
				builder.append("T");
			}else{
				builder.append("A");
			}
		}
		return builder.toString();
	}
	
	/**
	 * @param dna input sequence
	 * @param k length of desired k-mer
	 * @param n number of reads
	 * @return
	 */
	public static Set<String> shotgun(String dna, int k, int n){
		Set<String> results = new HashSet<String>();
		for(int i=0; i<n; ++i){
			int startIndex = (int)(Math.random()*dna.length());
			results.add(dna.substring(startIndex, Math.min(startIndex+k, dna.length())));
		}
		return results;
	}

	/**
	 * 
	 * @param reads Set of reads to print
	 */
	public static void printReads(Set<String> reads){
		for(String k : reads){
			System.out.println(k);
		}
	}
	
	public static DirectedGraph<String, String> getTheGraph(String[] reads, int k){
		DirectedGraph<String, String> graph = new DefaultDirectedGraph<String, String>(new DeBruijnEdgeFactory());
		for(String read : reads){
			if(k>read.length()){
				continue;
			}else{
				//String prevKmer=null;
				//String prevNode=null;
				for(int s=0,e=k; e<=read.length(); ++s, ++e){
					//System.out.println(read.substring(s, e));
					String lo = read.substring(s, e-1);
					String ld = read.substring(s+1, e);
					if(!graph.containsVertex(lo)){
						graph.addVertex(lo);
					}
					if(!graph.containsVertex(ld)){
						graph.addVertex(ld);
					}
					if(!graph.containsEdge(lo, ld)){
						graph.addEdge(lo, ld);
						System.out.println("adding edge: "+lo+"-"+ld);
					}
				}
			}
		}
		return graph;
	}
	
	public void init(){
		String[] reads = {"VALENTINA", "SHAPIRO"};
		DirectedGraph<String, String> graph = getTheGraph(reads, 5);
		JGraphModelAdapter<String, String> jGraphAdapter = new JGraphModelAdapter<String, String>(graph);
	
		JGraph jGraph = new JGraph(jGraphAdapter);
		
        adjustDisplaySettings( jGraph );
        getContentPane(  ).add( jGraph );
        resize( DEFAULT_SIZE );

        distributeVertices(jGraphAdapter, graph);
	}

	//SOME STUFF FOR JGRAPH ADAPTER

    private void adjustDisplaySettings( JGraph jg ) {
        jg.setPreferredSize( DEFAULT_SIZE );

        Color  c        = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter( "bgcolor" );
        }
         catch( Exception e ) {}

        if( colorStr != null ) {
            c = Color.decode( colorStr );
        }

        jg.setBackground( c );
    }


    private void positionVertexAt( Object vertex, int x, int y, JGraphModelAdapter<String, String> m_jgAdapter ) {
        DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex );
        Map              attr = cell.getAttributes(  );
        Rectangle2D        b    = GraphConstants.getBounds( attr );

        GraphConstants.setBounds( attr, new Rectangle( x, y, (int)b.getWidth(), (int)b.getHeight() ) );

        Map cellAttr = new HashMap(  );
        cellAttr.put( cell, attr );
        m_jgAdapter.edit( cellAttr, null, null, null );
    }

    private void distributeVertices(JGraphModelAdapter<String, String> ga, Graph<String, String> g){
    	int vertices = g.vertexSet().size();
    	double step = 2*Math.PI/vertices;
    	double counter=0;
    	double stdSpan=400;
    	for(String vertex : g.vertexSet()){
    		positionVertexAt(vertex, DEFAULT_SIZE.width/2+(int)(stdSpan*Math.cos(counter)), 
    				DEFAULT_SIZE.height/2+(int)(stdSpan*Math.sin(counter)), ga);
    		counter+=step;
    	}
    }
}
