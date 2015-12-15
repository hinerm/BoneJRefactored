
package protoOps.triplePointAngles;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;

import net.imagej.ops.Op;
import net.imagej.ops.OpEnvironment;

import org.bonej.common.Centroid;
import org.bonej.common.Common;
import org.bonej.common.ImageCheck;
import org.bonej.geometry.Vectors;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import sc.fiji.analyzeSkeleton.*;
import sc.fiji.skeletonize3D.Skeletonize3D_;
import ij.ImagePlus;

/**
 * Skeletonizes the input image, and then calculates the angles at each of its
 * triple points. A triple point is a point where three edges of the skeleton
 * meet at a single vertex. The plugin calculates angles between each of these
 * edges (branches), at each triple point (vertex) in each skeleton (graph) in
 * the image.
 *
 * @author Michael Doube
 * @author Richard Domander
 */
@Plugin(type = Op.class, name = "triplePointAngles")
public class TriplePointAngles implements Op {

	public static final int DEFAULT_NTH_POINT = 0;
	public static final int VERTEX_TO_VERTEX = -1;

	private final AnalyzeSkeleton_ skeletonAnalyzer = new AnalyzeSkeleton_();
	private final Skeletonize3D_ skeletonizer = new Skeletonize3D_();

	@Parameter(type = ItemIO.INPUT)
	private ImagePlus inputImage = null;

	@Parameter(min = "-1", required = false)
	private int nthPoint = DEFAULT_NTH_POINT;

	/**
	 * An array of skeletons containing an array of triple points containing an
	 * array of angles (3) between each branch
	 */
	@Parameter(type = ItemIO.OUTPUT)
	private double results[][][] = null;

	/**
	 * @return The angle array from previous run Returns null if the plugin hasn't
	 *         executed yet, or if there was a problem in the previous run
	 * @see TriplePointAngles#results
	 */
	public double[][][] getResults() {
		return results;
	}

	/**
	 * Sets the input image for processing
	 * 
	 * @throws NullPointerException if image == null
	 * @throws IllegalArgumentException if image is not binary
	 */
	public void setInputImage(ImagePlus image) {
		checkImage(image);

		inputImage = image;
	}

	/**
	 * Sets the distance of the angle measurement from the centroid of the triple
	 * points.
	 *
	 * @param nthPoint number pixels from the triple point centroid
	 * @throws IllegalArgumentException if nthPoint < 0 && nthPoint !=
	 *           TriplePointAngles#VERTEX_TO_VERTEX
	 */
	public void setNthPoint(int nthPoint) {
		checkNthPoint(nthPoint);

		this.nthPoint = nthPoint;
	}

	/**
	 * Calculates the triple point angles of the input image to the results array
	 * 
	 * @throws NullPointerException if this.inputImage == null
	 * @throws IllegalArgumentException if this.inputImage is not binary
	 * @throws IllegalArgumentException if this.inputImage could not be
	 *           skeletonized
	 */
	public void calculateTriplePointAngles() {
		checkImage(inputImage);

		results = null;

		skeletonizer.setup("", inputImage);
		skeletonizer.run(null);

		skeletonAnalyzer.setup("", inputImage);
		skeletonAnalyzer.run();
		Graph[] graphs = skeletonAnalyzer.getGraphs();

		if (graphs == null || graphs.length == 0) {
			throw new IllegalArgumentException(
				"Input image could not be skeletonized");
		}

		ArrayList<ArrayList<double[]>> graphsVertices = new ArrayList<>();

		for (Graph graph : graphs) {
			ArrayList<Vertex> vertices = graph.getVertices();
			ArrayList<double[]> vertexAngles = new ArrayList<>();

			for (Vertex vertex : vertices) {
				if (!isTriplePoint(vertex)) {
					continue;
				}

				ArrayList<Edge> edges = vertex.getBranches();
				Edge edge0 = edges.get(0);
				Edge edge1 = edges.get(1);
				Edge edge2 = edges.get(2);

				double thetas[] = new double[3];
				if (nthPoint == VERTEX_TO_VERTEX) {
					thetas[0] = vertexToVertexAngle(vertex, edge0, edge1);
					thetas[1] = vertexToVertexAngle(vertex, edge0, edge2);
					thetas[2] = vertexToVertexAngle(vertex, edge1, edge2);
				}
				else {
					thetas[0] = vertexAngle(vertex, edge0, edge1);
					thetas[1] = vertexAngle(vertex, edge0, edge2);
					thetas[2] = vertexAngle(vertex, edge1, edge2);
				}

				vertexAngles.add(thetas);
			}
			graphsVertices.add(vertexAngles);
		}

		results = new double[graphsVertices.size()][][];
		final int treeSize = graphsVertices.size();
		for (int g = 0; g < treeSize; g++) {
			ArrayList<double[]> vertexAngles = graphsVertices.get(g);
			final int graphSize = vertexAngles.size();
			results[g] = new double[graphSize][];
			for (int v = 0; v < graphSize; v++) {
				results[g][v] = vertexAngles.get(v);
			}
		}
	}

	@Override
	public void run() {
		calculateTriplePointAngles();
	}

	@Override
	public OpEnvironment ops() {
		return null;
	}

	@Override
	public void setEnvironment(OpEnvironment opEnvironment) {

	}

	// region -- Helper methods --
	/**
	 * Checks if the plugin can process the given image
	 *
	 * @throws NullPointerException if image == null
	 * @throws IllegalArgumentException if image is not binary
	 */
	private static void checkImage(ImagePlus image) {
		checkNotNull(image, "Must have an input image");
		checkArgument(ImageCheck.isBinary(image), "Input image must be binary");
	}

	/**
	 * Checks if the the plugin can use the given nthPoint value
	 *
	 * @throws IllegalArgumentException if parameter nthPoint < 0 && nthPoint !=
	 *           TriplePointAngles#VERTEX_TO_VERTEX
	 */
	private static void checkNthPoint(int nthPoint) {
		checkArgument(nthPoint >= 0 || nthPoint == VERTEX_TO_VERTEX,
			"Invalid nth point value");
	}

	private static boolean isVoxel26Connected(Point point, Point voxel) {
		int xDistance = Math.abs(point.x - voxel.x);
		int yDistance = Math.abs(point.y - voxel.y);
		int zDistance = Math.abs(point.z - voxel.z);

		return xDistance <= 1 && yDistance <= 1 && zDistance <= 1;
	}

	private static boolean isTriplePoint(Vertex vertex) {
		return vertex.getBranches().size() == 3;
	}

	private static double vertexToVertexAngle(Vertex vertex, Edge edge0,
		Edge edge1)
	{
		Vertex oppositeVertex0 = edge0.getOppositeVertex(vertex);
		Vertex oppositeVertex1 = edge1.getOppositeVertex(vertex);

		ArrayList<Point> vertexPoints = vertex.getPoints();
		ArrayList<Point> oppositeVertex0Points = oppositeVertex0.getPoints();
		ArrayList<Point> oppositeVertex1Points = oppositeVertex1.getPoints();

		double[] vertexCentroid = Centroid.getCentroid(vertexPoints);
		double[] oppositeVertex0Centroid = Centroid.getCentroid(
			oppositeVertex0Points);
		double[] oppositeVertex1Centroid = Centroid.getCentroid(
			oppositeVertex1Points);

		return Vectors.joinedVectorAngle(oppositeVertex0Centroid[0],
			oppositeVertex0Centroid[1], oppositeVertex0Centroid[2],
			oppositeVertex1Centroid[0], oppositeVertex1Centroid[1],
			oppositeVertex1Centroid[2], vertexCentroid[0], vertexCentroid[1],
			vertexCentroid[2]);
	}

	private double vertexAngle(Vertex vertex, Edge edge0, Edge edge1) {
		Point p0 = getNthPointOfEdge(vertex, edge0);
		Point p1 = getNthPointOfEdge(vertex, edge1);

		double cv[] = Centroid.getCentroid(vertex.getPoints());
		return Vectors.joinedVectorAngle(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z, cv[0],
			cv[1], cv[2]);
	}

	private Point getNthPointOfEdge(Vertex vertex, Edge edge) {
		ArrayList<Point> vertexPoints = vertex.getPoints();
		ArrayList<Point> edgePoints = edge.getSlabs();
		boolean startAtZero = false;
		Point edgeStart = edgePoints.get(0);

		for (Point vertexPoint : vertexPoints) {
			if (isVoxel26Connected(edgeStart, vertexPoint)) {
				startAtZero = true;
				break;
			}
		}

		nthPoint = Common.clamp(nthPoint, 0, edgePoints.size() - 1);

		if (startAtZero) {
			// Vertex is the start vertex of the edge so start counting "up"
			return edgePoints.get(nthPoint);
		}

		// Vertex is the end vertex of the edge so start counting "down"
		return edgePoints.get(edgePoints.size() - nthPoint - 1);
	}
	// endregion
}
