package com.cloud;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra
{
	public HashMap<String,Vertex> map;
	public Vertex A,B,C,D,E,F,G,X;
	
	public Dijkstra(String start,String end) {
		map =  new HashMap<String,Vertex>();
		
		X = new Vertex("X");
		A = new Vertex("A");
        B = new Vertex("B");
        C = new Vertex("C");
        D = new Vertex("D");
        E = new Vertex("E");
        F = new Vertex("F");
        G = new Vertex("G");
    
        
        map.put("0",X);
		map.put("4.417",A);
		map.put("4.908",B);
		map.put("4.624",C);
		map.put("4.910",D);
		map.put("4.999",E);
		map.put("4.623",F);
		map.put("4.721",G);
		
		X.adjacencies = new Edge[]{ new Edge(A,8) };
		
	    A.adjacencies = new Edge[]{ new Edge(B,8) };

        B.adjacencies = new Edge[]{ new Edge(C,8) };

        C.adjacencies = new Edge[]{ new Edge(D,15)};

        D.adjacencies = new Edge[]{ new Edge(E,18) };

        E.adjacencies = new Edge[]{ new Edge(D,18) };
        
        E.adjacencies = new Edge[]{ new Edge(G,10) };
        
        G.adjacencies = new Edge[]{ new Edge(E,12)};
        
        computePaths(map.get(start)); // run Dijkstra
        System.out.println("Distance to " + D + ": " + F.minDistance);
        
	}
	

    public static void computePaths(Vertex source)
    {
        source.minDistance = 0.;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
    vertexQueue.add(source);

    while (!vertexQueue.isEmpty()) {
        Vertex u = vertexQueue.poll();

            // Visit each edge exiting u
            for (Edge e : u.adjacencies)
            {
                Vertex v = e.target;
                double weight = e.weight;
                double distanceThroughU = u.minDistance + weight;
        if (distanceThroughU < v.minDistance) {
            vertexQueue.remove(v);

            v.minDistance = distanceThroughU ;
            v.previous = u;
            vertexQueue.add(v);
        }
            }
        }
    }

    public  List<Vertex> getShortestPathTo(String targetString)
    {
    	Vertex target = map.get(targetString); 
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
            path.add(vertex);

        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args)
    {
        // mark all the vertices 
        Vertex A = new Vertex("A");
        Vertex B = new Vertex("B");
        Vertex C = new Vertex("C");
        Vertex D = new Vertex("D");
        Vertex E = new Vertex("E");
        Vertex F = new Vertex("F");
        Vertex G = new Vertex("G");
 
        
        A.adjacencies = new Edge[]{ new Edge(B,2) };

        B.adjacencies = new Edge[]{ new Edge(C,2) };

        C.adjacencies = new Edge[]{ new Edge(D,2)};

        D.adjacencies = new Edge[]{ new Edge(E,2) };

        E.adjacencies = new Edge[]{ new Edge(D,2) };
        
        F.adjacencies = new Edge[]{ new Edge(B,12)};
        
        B.adjacencies = new Edge[]{new Edge(F,16)};
        
        D.adjacencies = new Edge[]{ new Edge(G,14)};
        
        G.adjacencies = new Edge[]{ new Edge(D,28)};

        computePaths(A); // run Dijkstra
        System.out.println("Distance to " + D + ": " + F.minDistance);
//        List<Vertex> path = getShortestPathTo(F);
//        System.out.println("Path: " + path);
    }
    
    
}