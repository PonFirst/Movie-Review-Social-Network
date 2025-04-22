import java.util.*;

/**
 * A generic graph implementation using HashMaps to store adjacency information.
 * This implementation supports both directed and undirected graphs.
 * 
 * @param <T> Type of vertices stored in the graph
 */
public class Graph<T> {
    // Maps each vertex to its adjacency list (neighbors)
    private final Map<T, List<T>> adjacencyMap;
    private final boolean isDirected;
    
    /**
     * Creates a new graph
     * 
     * @param isDirected true if the graph is directed, false otherwise
     */
    public Graph(boolean isDirected) {
        this.adjacencyMap = new HashMap<>();
        this.isDirected = isDirected;
    }
    
    /**
     * Adds a vertex to the graph if it doesn't already exist
     * 
     * @param vertex the vertex to add
     */
    public void addVertex(T vertex) {
        adjacencyMap.putIfAbsent(vertex, new ArrayList<>());
    }
    
    /**
     * Adds an edge from source to destination
     * 
     * @param source the source vertex
     * @param destination the destination vertex
     */
    public void addEdge(T source, T destination) {
        // Add vertices if they don't exist
        addVertex(source);
        addVertex(destination);
        
        // Add the edge
        adjacencyMap.get(source).add(destination);
        
        // If the graph is undirected, add an edge in the opposite direction
        if (!isDirected) {
            adjacencyMap.get(destination).add(source);
        }
    }
    
    /**
     * Removes an edge from source to destination
     * 
     * @param source the source vertex
     * @param destination the destination vertex
     */
    public void removeEdge(T source, T destination) {
        if (adjacencyMap.containsKey(source)) {
            adjacencyMap.get(source).remove(destination);
        }
        
        // If the graph is undirected, remove the edge in the opposite direction
        if (!isDirected && adjacencyMap.containsKey(destination)) {
            adjacencyMap.get(destination).remove(source);
        }
    }
    
    /**
     * Removes a vertex and all its associated edges
     * 
     * @param vertex the vertex to remove
     */
    public void removeVertex(T vertex) {
        // Remove all edges pointing to this vertex
        for (List<T> edges : adjacencyMap.values()) {
            edges.remove(vertex);
        }
        
        // Remove the vertex and its edges
        adjacencyMap.remove(vertex);
    }
    
    /**
     * Gets all neighbors of a vertex
     * 
     * @param vertex the vertex
     * @return a list of all neighbors
     */
    public List<T> getNeighbors(T vertex) {
        return adjacencyMap.getOrDefault(vertex, new ArrayList<>());
    }
    
    /**
     * Checks if the graph contains a vertex
     * 
     * @param vertex the vertex to check
     * @return true if the vertex exists, false otherwise
     */
    public boolean hasVertex(T vertex) {
        return adjacencyMap.containsKey(vertex);
    }
    
    /**
     * Checks if there is an edge from source to destination
     * 
     * @param source the source vertex
     * @param destination the destination vertex
     * @return true if the edge exists, false otherwise
     */
    public boolean hasEdge(T source, T destination) {
        return adjacencyMap.containsKey(source) && adjacencyMap.get(source).contains(destination);
    }
    
    /**
     * Gets all vertices in the graph
     * 
     * @return a set of all vertices
     */
    public Set<T> getVertices() {
        return adjacencyMap.keySet();
    }
    
    /**
     * Performs a breadth-first search starting from the given vertex
     * 
     * @param start the starting vertex
     * @return a list of vertices in BFS order
     */
    public List<T> breadthFirstSearch(T start) {
        if (!adjacencyMap.containsKey(start)) {
            return new ArrayList<>();
        }
        
        List<T> result = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        Queue<T> queue = new LinkedList<>();
        
        visited.add(start);
        queue.add(start);
        
        while (!queue.isEmpty()) {
            T vertex = queue.poll();
            result.add(vertex);
            
            for (T neighbor : adjacencyMap.get(vertex)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Performs a depth-first search starting from the given vertex
     * 
     * @param start the starting vertex
     * @return a list of vertices in DFS order
     */
    public List<T> depthFirstSearch(T start) {
        List<T> result = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        
        if (adjacencyMap.containsKey(start)) {
            dfsHelper(start, visited, result);
        }
        
        return result;
    }
    
    private void dfsHelper(T vertex, Set<T> visited, List<T> result) {
        visited.add(vertex);
        result.add(vertex);
        
        for (T neighbor : adjacencyMap.get(vertex)) {
            if (!visited.contains(neighbor)) {
                dfsHelper(neighbor, visited, result);
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph:\n");
        
        for (Map.Entry<T, List<T>> entry : adjacencyMap.entrySet()) {
            sb.append(entry.getKey().toString()).append(" -> ");
            sb.append(entry.getValue().toString()).append("\n");
        }
        
        return sb.toString();
    }
}