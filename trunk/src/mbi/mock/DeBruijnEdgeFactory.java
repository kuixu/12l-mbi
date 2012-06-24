package mbi.mock;

import org.jgrapht.EdgeFactory;

public class DeBruijnEdgeFactory implements EdgeFactory<String, String> {

	@Override
	public String createEdge(String v1, String v2) {
		if(v1.substring(1).equals(v2.substring(0, v2.length()-1))){
			return v1+v2.substring(v2.length()-1, v2.length());
			//return v1+v2;
		}else{
			return null;
		}
	}

}
